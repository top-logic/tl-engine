/**
 * The one way a transient surface - a menu, a select popup, a toolbar overflow - is placed: at an
 * anchor element or a viewport point, flipped when it does not fit, shifted to stay inside the
 * viewport, following scroll and resize while open. Surface, layer and shadow are the design
 * system's (tl-popover); this hook only yields the position. Closing is the caller's.
 */
import React from 'react';
import { useFloating, autoUpdate, offset, flip, shift } from '@floating-ui/react';
import { tokenPx } from './token-px';

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

export interface PopoverOptions {
  open: boolean;
  anchor: PopoverAnchor | null;
  /** Where the surface stands at its anchor, `bottom-start` if not given. */
  placement?: PopoverPlacement;
}

/**
 * The gap between anchor and surface, on whichever side of the anchor the surface stands:
 * --tl-space-xs as computed on the root element, converted to pixels. Read once per opening (see
 * usePopover), not on every render.
 */
function gapPx(): number {
  return tokenPx(document.documentElement, '--tl-space-xs', 8);
}

export function usePopover(opts: PopoverOptions): { setFloating: (el: HTMLElement | null) => void; style: React.CSSProperties } {
  const { open, anchor, placement = 'bottom-start' } = opts;
  // Two getComputedStyle calls per render would run per keystroke in a select: read on opening only.
  const gap = React.useMemo(gapPx, [open]);
  const { refs, floatingStyles } = useFloating({
    open,
    placement,
    strategy: 'fixed',
    middleware: [offset(gap), flip(), shift({ padding: gap })],
    whileElementsMounted: open ? autoUpdate : undefined,
  });
  // floating-ui keeps the position reference as state: set it on every change, element or point,
  // or a menu once opened at a point stays there when it is next opened at an element.
  const x = anchor && !(anchor instanceof Element) ? anchor.x : null;
  const y = anchor && !(anchor instanceof Element) ? anchor.y : null;
  const h = anchor && !(anchor instanceof Element) ? anchor.height ?? 0 : 0;
  React.useLayoutEffect(() => {
    if (!open || !anchor) return;
    if (anchor instanceof Element) { refs.setPositionReference(anchor); return; }
    const r = { x: anchor.x, y: anchor.y, left: anchor.x, top: anchor.y, right: anchor.x, bottom: anchor.y + h, width: 0, height: h };
    refs.setPositionReference({ getBoundingClientRect: () => r });
  }, [open, anchor instanceof Element ? anchor : null, x, y, h, refs]);
  // Only an open surface is observed: a mounted-but-hidden overflow menu must not run autoUpdate.
  const setFloating = React.useCallback((el: HTMLElement | null) => refs.setFloating(open ? el : null), [open, refs]);
  return { setFloating, style: floatingStyles };
}
