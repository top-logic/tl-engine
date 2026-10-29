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
import { useEffect, useSyncExternalStore } from 'react';

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
