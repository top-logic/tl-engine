/**
 * The transient overlays of the page - a snackbar, a toast - and the areas they occupy.
 *
 * <p>A transient overlay stands over the page for a short while, at a fixed place of the browser
 * window. An element floating over the page itself - a floating action button - moves aside while it
 * would be covered by one. The overlay reports itself here, whoever draws it (the TopLogic snackbar
 * or the snackbar of a component library), and the floating element reads the areas of all
 * overlays reported: the two know nothing of each other's markup.</p>
 *
 * <p>The areas are measured when an overlay is reported, when it changes its size, when an
 * animation or transition of it ends (an overlay sliding in reaches its place then) and when the
 * browser window is resized.</p>
 */
import { useEffect, useLayoutEffect, useRef, useState, useSyncExternalStore } from 'react';
import type { FloatingEdge } from './floating-anchor';
import { tokenPx } from './token-px';

/** The space kept between a floating element and a transient overlay it moves away from. */
const CLEARANCE_TOKEN = '--tl-space-sm';

/** The space of {@link CLEARANCE_TOKEN} without the design system. */
const CLEARANCE_FALLBACK_PX = 12;

/** The reported overlays. */
const _overlays = new Set<HTMLElement>();

/** Called when the areas change. */
const _listeners = new Set<() => void>();

/** The areas of no overlays: one array, so that it is the same in every render. */
const NO_RECTS: readonly DOMRect[] = [];

/** The areas last measured, in the order the overlays were reported. */
let _rects: readonly DOMRect[] = NO_RECTS;

/** Watches the size of the reported overlays, created on the first report where supported. */
let _resizeObserver: ResizeObserver | null = null;

/** Measures the reported overlays again and tells the listeners. */
function remeasure(): void {
  _rects = _overlays.size === 0 ? NO_RECTS : [..._overlays].map(overlay => overlay.getBoundingClientRect());
  for (const listener of [..._listeners]) {
    listener();
  }
}

function subscribe(listener: () => void): () => void {
  if (_listeners.size === 0) {
    window.addEventListener('resize', remeasure);
  }
  _listeners.add(listener);
  return () => {
    _listeners.delete(listener);
    if (_listeners.size === 0) {
      window.removeEventListener('resize', remeasure);
    }
  };
}

function getSnapshot(): readonly DOMRect[] {
  return _rects;
}

/**
 * Reports the given element as a transient overlay until the returned function is called.
 *
 * @param element The element of the overlay, the box it occupies.
 * @return Withdraws the report.
 */
export function registerTransientOverlay(element: HTMLElement): () => void {
  _overlays.add(element);
  if (_resizeObserver == null && typeof ResizeObserver !== 'undefined') {
    _resizeObserver = new ResizeObserver(remeasure);
  }
  _resizeObserver?.observe(element);
  element.addEventListener('animationend', remeasure);
  element.addEventListener('transitionend', remeasure);
  remeasure();
  return () => {
    if (!_overlays.delete(element)) {
      return;
    }
    _resizeObserver?.unobserve(element);
    element.removeEventListener('animationend', remeasure);
    element.removeEventListener('transitionend', remeasure);
    remeasure();
  };
}

/**
 * Reports the element of the given ref as a transient overlay while {@code active}: a snackbar while
 * it shows a message.
 *
 * @param ref Holds the element of the overlay once it is rendered.
 * @param active Whether the overlay is shown.
 */
export function useTransientOverlay(ref: { readonly current: HTMLElement | null }, active: boolean): void {
  useEffect(() => {
    const element = ref.current;
    if (!active || element == null) {
      return;
    }
    return registerTransientOverlay(element);
  }, [ref, active]);
}

/**
 * The areas the transient overlays of the page occupy, in coordinates of the browser window.
 *
 * <p>The returned array keeps its identity until an area is measured again.</p>
 */
export function useTransientOverlayRects(): readonly DOMRect[] {
  return useSyncExternalStore(subscribe, getSnapshot);
}

/** The part of a box the clearance computation reads: its vertical and horizontal extent. */
export interface ClearanceBox {
  readonly top: number;
  readonly bottom: number;
  readonly left: number;
  readonly right: number;
}

/**
 * The vertical shift moving a box at the given edge of its frame out of the way of the given
 * overlays: a box at the bottom edge moves up (a negative shift) until it stands the given gap above
 * every overlay it overlaps, a box at the top edge moves down below them. A box overlapping no
 * overlay keeps its place (zero). An overlay of no extent, one not rendered, is ignored.
 *
 * @param box The box at its place, unshifted.
 * @param overlays The areas of the transient overlays.
 * @param edge The edge of the frame the box is placed against.
 * @param gap The space kept to an overlay moved away from.
 */
export function clearance(box: ClearanceBox, overlays: readonly ClearanceBox[], edge: FloatingEdge, gap: number): number {
  let shift = 0;
  for (const overlay of overlays) {
    if (overlay.bottom <= overlay.top || overlay.right <= overlay.left) continue;
    const across = box.left < overlay.right && box.right > overlay.left;
    const along = box.top < overlay.bottom && box.bottom > overlay.top;
    if (!across || !along) continue;
    shift = edge === 'bottom'
      ? Math.min(shift, overlay.top - gap - box.bottom)
      : Math.max(shift, overlay.bottom + gap - box.top);
  }
  return shift;
}

/**
 * The vertical shift keeping the element of the given ref - an element floating at the given edge of
 * its frame - clear of the transient overlays of the page ({@link clearance}, with the gap of the
 * token `--tl-space-sm`). The caller moves the element by it (`translateY`).
 *
 * <p>The element is measured at its unshifted place whenever an overlay is reported, withdrawn or
 * measured again, and whenever one of the given layout values changes: the values deciding on the
 * size and place of the element (its anchor, the number of its actions, whether it is open).</p>
 *
 * @param ref Holds the floating element once it is rendered.
 * @param edge The edge of the frame the element is placed against.
 * @param layout The values the size and place of the element depend on.
 */
export function useOverlayClearance(
  ref: { readonly current: HTMLElement | null },
  edge: FloatingEdge,
  layout: readonly unknown[],
): number {
  const overlays = useTransientOverlayRects();
  const [shift, setShift] = useState(0);
  const shiftRef = useRef(0);
  shiftRef.current = shift;
  useLayoutEffect(() => {
    const element = ref.current;
    if (element == null) return;
    const rect = element.getBoundingClientRect();
    const unshifted: ClearanceBox = {
      top: rect.top - shiftRef.current,
      bottom: rect.bottom - shiftRef.current,
      left: rect.left,
      right: rect.right,
    };
    const next = clearance(unshifted, overlays, edge, tokenPx(element, CLEARANCE_TOKEN, CLEARANCE_FALLBACK_PX));
    if (next !== shiftRef.current) {
      setShift(next);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ref, overlays, edge, ...layout]);
  return shift;
}
