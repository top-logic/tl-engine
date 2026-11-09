/**
 * The one way a transient surface - a menu, a select popup, a toolbar overflow - is placed: at an
 * anchor element or a viewport point, flipped when it does not fit, shifted to stay inside the
 * viewport, following scroll and resize while open. The side a surface opens on is kept while it is
 * open (see {@link PopoverOptions.layoutKey}). Surface, layer and shadow are the design system's
 * (tl-popover); this hook only yields the position, and - for a surface opened by hovering its
 * anchor - the moments the pointer asks for it to open and close. Opening and closing are the
 * caller's.
 */
import React from 'react';
import { useFloating, useHover, safePolygon, autoUpdate, offset, flip, shift, size } from '@floating-ui/react';
import type { Middleware, Placement } from '@floating-ui/react';
import { tokenPx } from './token-px';
import { HOVER_DELAY_MS, CLOSE_DELAY_MS_INTERACTIVE } from './tooltip-host';

/**
 * Where a surface stands: at an element, or at a viewport point. A point with a `height` is a vertical
 * span from `y` downwards - the extent of a drag image around the pointer: the surface stands below
 * it, or above it where there is no room below, and never on it.
 */
export type PopoverAnchor = Element | { x: number; y: number; height?: number };

/**
 * The side of the anchor a surface stands on, and its alignment along that side: `start` and `end`
 * align the surface with the start or end edge of the anchor, no alignment centers it. A surface
 * that does not fit on its side flips to the opposite one and shifts along the side to stay inside
 * the viewport.
 */
export type PopoverPlacement =
  | 'top' | 'top-start' | 'top-end'
  | 'bottom' | 'bottom-start' | 'bottom-end'
  | 'left' | 'left-start' | 'left-end'
  | 'right' | 'right-start' | 'right-end';

/**
 * A surface opened by hovering its anchor element.
 *
 * <p>The pointer resting on the anchor asks for the surface to open, after the delay a tooltip
 * waits. While the surface is open, the pointer may travel from the anchor into the surface along
 * the triangle between the pointer and the near edge of the surface without the surface closing;
 * the pointer leaving both asks for it to close, after the delay an interactive tooltip waits.</p>
 */
export interface PopoverHover {
  /** Asks for the surface to open ({@code true}) or close ({@code false}). */
  onOpenChange: (open: boolean) => void;
}

export interface PopoverOptions {
  open: boolean;
  anchor: PopoverAnchor | null;
  /** Where the surface stands at its anchor, `bottom-start` if not given. */
  placement?: PopoverPlacement;
  /** Opens and closes the surface on hovering its anchor, which must then be an element. */
  hover?: PopoverHover;

  /**
   * Whether the surface is held to the room on the side of its anchor it stands on: its height is at most that room
   * (the viewport edge keeping the gap), and at most --tl-size-popover-max, so that a surface too high for either
   * side scrolls instead of covering its anchor. Set on the floating element itself (see {@link fittedMaxHeight}).
   *
   * <p>Not for a surface that caps its height lower in the design system - the popup of a select shows eight rows:
   * the inline height would lift that cap.</p>
   */
  fitHeight?: boolean;

  /**
   * What the surface lays out, for the choice of its side. The side is decided when the surface opens - flip takes
   * the side where the surface fits, else the one with more room, with the height it has before the room caps it -
   * and kept while it is open: a surface that shrinks (a search narrowing its list) does not jump to the other side,
   * it only flips where its side has no room left. A new value decides the side anew in the same way, so that a
   * surface switching to a taller page moves to the side with more room instead of scrolling where it stands. A new
   * anchor decides anew as well. Absent: the side is decided once per opening.
   */
  layoutKey?: unknown;
}

/** The side a surface keeps while it is open, with what it was decided for (see {@link PopoverOptions.layoutKey}). */
interface KeptSide {
  layoutKey: unknown;
  anchor: Element | null;
  x: number | null;
  y: number | null;
  h: number;
  placement: Placement;
}

/**
 * The gap between anchor and surface, on whichever side of the anchor the surface stands:
 * --tl-space-xs as computed on the root element, converted to pixels. Read once per opening (see
 * usePopover), not on every render.
 */
function gapPx(): number {
  return tokenPx(document.documentElement, '--tl-space-xs', 8);
}

/**
 * The max height of a surface held to the room at its anchor (see {@link PopoverOptions.fitHeight}): the room in
 * whole pixels, never below zero, and at most --tl-size-popover-max. The token stays a reference for the browser to
 * resolve; the design system's cap on tl-popover is the same token.
 */
export function fittedMaxHeight(availableHeight: number): string {
  return `min(${Math.max(0, Math.floor(availableHeight))}px, var(--tl-size-popover-max))`;
}

/**
 * Writes the room size() found onto the floating element. Directly, not through React state: size() measures the
 * element again right after and, if the cap changed its height, has the placement computed once more with it - so
 * flip decides with the height the surface really has, in the same pass. One function for all surfaces, so that
 * useFloating, which compares the middleware of two renders, sees the same middleware.
 */
function applyFittedHeight({ availableHeight, elements }: { availableHeight: number; elements: { floating: HTMLElement } }): void {
  elements.floating.style.maxHeight = fittedMaxHeight(availableHeight);
}

/**
 * The placement rules: offset by the gap, flipped to the other side where it does not fit, and shifted
 * along both axes so that, where neither side has room, the surface overlaps its anchor instead of
 * leaving the viewport. With `fitHeight` (see {@link PopoverOptions.fitHeight}), the height is held to the
 * room on the side flip has chosen, keeping the gap to the viewport edge as shift does.
 *
 * <p>The height is fitted between flip and shift: after shift, which moves the surface along the side axis
 * as well, size() takes the whole height of the viewport as the room, and the surface would cover its anchor
 * instead of scrolling.</p>
 */
export function popoverMiddleware(gap: number, fitHeight = false) {
  return [
    offset(gap),
    flip(),
    ...(fitHeight ? [size({ padding: gap, apply: applyFittedHeight })] : []),
    shift({ padding: gap, crossAxis: true }),
  ];
}

/**
 * Hands the side a computation ends on to the given receiver, while the side of a surface is to be decided. One
 * function for all surfaces, as {@link applyFittedHeight}: useFloating keeps the middleware of the first render.
 */
function sideReporter(receiver: React.RefObject<((placement: Placement) => void) | null>): Middleware {
  return {
    name: 'reportSide',
    fn: ({ placement }) => {
      receiver.current?.(placement);
      return {};
    },
  };
}

export function usePopover(opts: PopoverOptions): { setFloating: (el: HTMLElement | null) => void; style: React.CSSProperties } {
  const { open, anchor, placement = 'bottom-start', hover, fitHeight = false, layoutKey } = opts;
  // Two getComputedStyle calls per render would run per keystroke in a select: read on opening only.
  const gap = React.useMemo(gapPx, [open]);
  const hoverRef = React.useRef(hover);
  hoverRef.current = hover;
  const anchorElement = anchor instanceof Element ? anchor : null;
  const x = anchor && !(anchor instanceof Element) ? anchor.x : null;
  const y = anchor && !(anchor instanceof Element) ? anchor.y : null;
  const h = anchor && !(anchor instanceof Element) ? anchor.height ?? 0 : 0;

  // The side is kept while it was decided for this opening, this layout and this anchor. Otherwise the computations
  // decide it, starting from the given placement, and report the side they end on; the last one before the next
  // render is kept - it is the one asked for after the position reference was set (see below).
  const [kept, setKept] = React.useState<KeptSide | null>(null);
  const keptPlacement = open && kept !== null && Object.is(kept.layoutKey, layoutKey) && kept.anchor === anchorElement
    && kept.x === x && kept.y === y && kept.h === h ? kept.placement : null;
  const sideReceiver = React.useRef<((side: Placement) => void) | null>(null);
  sideReceiver.current = open && keptPlacement === null
    ? side => setKept({ layoutKey, anchor: anchorElement, x, y, h, placement: side })
    : null;
  React.useLayoutEffect(() => {
    if (!open) {
      setKept(null);
    }
  }, [open]);

  const { refs, floatingStyles, context, update } = useFloating({
    open,
    onOpenChange: next => hoverRef.current?.onOpenChange(next),
    placement: keptPlacement ?? placement,
    strategy: 'fixed',
    middleware: [...popoverMiddleware(gap, fitHeight), sideReporter(sideReceiver)],
    whileElementsMounted: open ? autoUpdate : undefined,
  });
  useHover(context, {
    enabled: hover != null,
    delay: { open: HOVER_DELAY_MS, close: CLOSE_DELAY_MS_INTERACTIVE },
    handleClose: safePolygon(),
  });
  // The hover listeners go to the element floating-ui knows as the reference; the position is set
  // separately below, as for every anchor.
  const hoverAnchor = hover != null && anchor instanceof Element ? anchor : null;
  React.useLayoutEffect(() => {
    if (hoverAnchor) refs.setReference(hoverAnchor);
  }, [hoverAnchor, refs]);
  // floating-ui keeps the position reference as state: set it on every change, element or point,
  // or a menu once opened at a point stays there when it is next opened at an element.
  React.useLayoutEffect(() => {
    if (!open || !anchor) return;
    if (anchor instanceof Element) { refs.setPositionReference(anchor); return; }
    const r = { x: anchor.x, y: anchor.y, left: anchor.x, top: anchor.y, right: anchor.x, bottom: anchor.y + h, width: 0, height: h };
    refs.setPositionReference({ getBoundingClientRect: () => r });
  }, [open, anchor instanceof Element ? anchor : null, x, y, h, refs]);
  // A decision is made with the height the surface has before the room caps it: the cap of the side it stood on
  // would make it fit there, and flip would never see that the other side has more room. After the position
  // reference is set, so that the computation asked for here places the surface at its anchor of now.
  const deciding = open && keptPlacement === null;
  React.useLayoutEffect(() => {
    const floating = refs.floating.current;
    if (!deciding || !fitHeight || floating === null) {
      return;
    }
    floating.style.maxHeight = '';
    update();
  }, [deciding, fitHeight, layoutKey, refs, update]);
  // Only an open surface is observed: a mounted-but-hidden overflow menu must not run autoUpdate.
  const setFloating = React.useCallback((el: HTMLElement | null) => refs.setFloating(open ? el : null), [open, refs]);
  return { setFloating, style: floatingStyles };
}
