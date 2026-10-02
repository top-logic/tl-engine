/**
 * The one way a transient surface - a menu, a select popup, a toolbar overflow - is placed: at an
 * anchor element or a viewport point, flipped when it does not fit, shifted to stay inside the
 * viewport, following scroll and resize while open. Surface, layer and shadow are the design
 * system's (tl-popover); this hook only yields the position. Closing is the caller's.
 */
import React from 'react';
import { useFloating, autoUpdate, offset, flip, shift } from '@floating-ui/react';

export type PopoverAnchor = Element | { x: number; y: number };

export interface PopoverOptions {
  open: boolean;
  anchor: PopoverAnchor | null;
  placement?: 'bottom-start' | 'bottom-end' | 'top-start';
}

/**
 * The gap between anchor and surface: --tl-space-xs as computed on the root element, converted to
 * pixels. Read once per opening (see usePopover), not on every render.
 */
function gapPx(): number {
  const root = getComputedStyle(document.documentElement);
  const v = root.getPropertyValue('--tl-space-xs').trim();
  const rem = parseFloat(root.fontSize) || 16;
  const n = parseFloat(v);
  return isNaN(n) ? 8 : v.endsWith('rem') ? n * rem : n;
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
  React.useLayoutEffect(() => {
    if (!open || !anchor) return;
    if (anchor instanceof Element) { refs.setPositionReference(anchor); return; }
    const r = { x: anchor.x, y: anchor.y, left: anchor.x, top: anchor.y, right: anchor.x, bottom: anchor.y, width: 0, height: 0 };
    refs.setPositionReference({ getBoundingClientRect: () => r });
  }, [open, anchor instanceof Element ? anchor : null, x, y, refs]);
  // Only an open surface is observed: a mounted-but-hidden overflow menu must not run autoUpdate.
  const setFloating = React.useCallback((el: HTMLElement | null) => refs.setFloating(open ? el : null), [open, refs]);
  return { setFloating, style: floatingStyles };
}
