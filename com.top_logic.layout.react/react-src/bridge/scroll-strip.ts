import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';

/** An end of a scroll strip: `start` is the left end, `end` the right one. */
export type StripEdge = 'start' | 'end';

/** The left end of a scroll strip, see {@link StripEdge}. */
export const EDGE_START: StripEdge = 'start';

/** The right end of a scroll strip, see {@link StripEdge}. */
export const EDGE_END: StripEdge = 'end';

/**
 * The share of the visible width one {@link ScrollStrip.scrollPage} moves: about one strip width,
 * keeping a part of the previous view for orientation.
 */
export const PAGE_FRACTION = 0.8;

/** Tolerance (px) against sub-pixel rounding when comparing scroll positions and extents. */
const EPSILON = 0.5;

/** The horizontal scroll geometry of a strip, as the element reports it. */
export interface StripGeometry {
  /** The current horizontal scroll offset. */
  scrollLeft: number;

  /** The visible width of the strip. */
  clientWidth: number;

  /** The width of the whole content of the strip. */
  scrollWidth: number;
}

/** The horizontal extent of an element inside the scrolled content of a strip. */
export interface StripExtent {
  /** Left edge, measured from the start of the content (not of the visible part). */
  start: number;

  /** Right edge, measured from the start of the content. */
  end: number;
}

/** In which directions content of a strip is hidden. */
export interface StripOverflow {
  /** Content is hidden before the visible part. */
  canScrollStart: boolean;

  /** Content is hidden after the visible part. */
  canScrollEnd: boolean;
}

/** The largest scroll offset of the given geometry. */
function maxScroll(g: StripGeometry): number {
  return Math.max(0, g.scrollWidth - g.clientWidth);
}

/** Limits a scroll offset to the range the given geometry allows. */
function clamp(g: StripGeometry, scrollLeft: number): number {
  return Math.min(maxScroll(g), Math.max(0, scrollLeft));
}

/** In which directions the content of a strip with the given geometry is hidden. */
export function stripOverflow(g: StripGeometry): StripOverflow {
  return {
    canScrollStart: g.scrollLeft > EPSILON,
    canScrollEnd: g.scrollLeft < maxScroll(g) - EPSILON,
  };
}

/**
 * The scroll offset showing the element with the given extent completely, moving as little as
 * possible: the current offset for an element already in view, otherwise the offset putting the
 * element at the edge it was hidden behind. An element wider than the strip is shown from its start.
 */
export function revealScrollLeft(g: StripGeometry, extent: StripExtent): number {
  const visibleEnd = g.scrollLeft + g.clientWidth;
  if (extent.start < g.scrollLeft - EPSILON || extent.end - extent.start > g.clientWidth) {
    return clamp(g, extent.start);
  }
  if (extent.end > visibleEnd + EPSILON) {
    return clamp(g, extent.end - g.clientWidth);
  }
  return g.scrollLeft;
}

/** The scroll offset one page towards the given edge, see {@link PAGE_FRACTION}. */
export function pageScrollLeft(g: StripGeometry, edge: StripEdge): number {
  const step = g.clientWidth * PAGE_FRACTION;
  return clamp(g, g.scrollLeft + (edge === EDGE_START ? -step : step));
}

/** The geometry of the given strip element. */
function geometryOf(list: HTMLElement): StripGeometry {
  return { scrollLeft: list.scrollLeft, clientWidth: list.clientWidth, scrollWidth: list.scrollWidth };
}

/** The extent of the given element in the scrolled content of the given strip element. */
function extentOf(list: HTMLElement, element: Element): StripExtent {
  const origin = list.getBoundingClientRect().left + list.clientLeft - list.scrollLeft;
  const box = element.getBoundingClientRect();
  return { start: box.left - origin, end: box.right - origin };
}

/** Whether the user asked for reduced motion: then no scroll is animated. */
function prefersReducedMotion(): boolean {
  return typeof window.matchMedia === 'function' && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

/** Scrolls the given strip element to the given offset, animated unless {@link prefersReducedMotion}. */
function scrollStripTo(list: HTMLElement, scrollLeft: number, smooth: boolean): void {
  if (Math.abs(scrollLeft - list.scrollLeft) <= EPSILON) {
    return;
  }
  if (typeof list.scrollTo === 'function') {
    list.scrollTo({ left: scrollLeft, behavior: smooth && !prefersReducedMotion() ? 'smooth' : 'auto' });
  } else {
    list.scrollLeft = scrollLeft;
  }
}

/** What {@link useScrollStrip} needs to know about the strip. */
export interface ScrollStripOptions {
  /**
   * Identifies the element to keep in view, e.g. the ID of the selected tab. Each change reveals
   * the element {@link target} finds.
   */
  revealKey?: unknown;

  /** Finds the element to keep in view inside the strip element, or none. */
  target?: (list: HTMLElement) => Element | null;
}

/** The result of {@link useScrollStrip}. */
export interface ScrollStrip extends StripOverflow {
  /** The ref to put on the horizontally scrolling element. */
  listRef: (element: HTMLElement | null) => void;

  /** Scrolls the strip by one page towards the given edge (animated). */
  scrollPage: (edge: StripEdge) => void;

  /** Scrolls the strip, and only the strip, so that the given element in it is fully visible. */
  reveal: (element: Element, smooth?: boolean) => void;
}

const NO_OVERFLOW: StripOverflow = { canScrollStart: false, canScrollEnd: false };

/**
 * A horizontally scrolling strip with buttons at its ends, as the tab strip uses it.
 *
 * <p>Tracks in which directions content is hidden ({@link StripOverflow}), through the scroll
 * events of the element and a resize observer on the element and its children, so that the buttons
 * at the ends show only while there is something to scroll to. Keeps the element given by
 * {@link ScrollStripOptions.target} in view: on mount, whenever its size or content changes, and
 * whenever {@link ScrollStripOptions.revealKey} changes.</p>
 *
 * <p>Revealing sets the scroll offset of the strip itself, never through
 * {@link Element.scrollIntoView}, which would scroll every scrolling ancestor as well.</p>
 */
export function useScrollStrip({ revealKey, target }: ScrollStripOptions = {}): ScrollStrip {
  const [list, setList] = useState<HTMLElement | null>(null);
  const [overflow, setOverflow] = useState<StripOverflow>(NO_OVERFLOW);
  const targetRef = useRef(target);
  targetRef.current = target;

  const update = useCallback((element: HTMLElement) => {
    const next = stripOverflow(geometryOf(element));
    setOverflow(prev =>
      prev.canScrollStart === next.canScrollStart && prev.canScrollEnd === next.canScrollEnd ? prev : next);
  }, []);

  const reveal = useCallback((element: Element, smooth = false) => {
    if (!list || !list.contains(element)) {
      return;
    }
    scrollStripTo(list, revealScrollLeft(geometryOf(list), extentOf(list, element)), smooth);
    update(list);
  }, [list, update]);

  const revealTarget = useCallback((smooth: boolean) => {
    if (!list) {
      return;
    }
    const element = targetRef.current?.(list);
    if (element) {
      reveal(element, smooth);
    } else {
      update(list);
    }
  }, [list, reveal, update]);

  useEffect(() => {
    if (!list) {
      return;
    }
    const onScroll = () => update(list);
    list.addEventListener('scroll', onScroll, { passive: true });
    if (typeof ResizeObserver === 'undefined') {
      return () => list.removeEventListener('scroll', onScroll);
    }
    const resize = new ResizeObserver(() => revealTarget(false));
    const observeAll = () => {
      resize.disconnect();
      resize.observe(list);
      for (const child of Array.from(list.children)) {
        resize.observe(child);
      }
    };
    observeAll();
    // Children coming and going change the content width without resizing any observed element.
    const children = new MutationObserver(observeAll);
    children.observe(list, { childList: true });
    return () => {
      list.removeEventListener('scroll', onScroll);
      resize.disconnect();
      children.disconnect();
    };
  }, [list, update, revealTarget]);

  // The first reveal of a strip jumps, a later one (a selection made by the user) glides.
  const revealed = useRef(false);
  useLayoutEffect(() => {
    if (!list) {
      revealed.current = false;
      return;
    }
    revealTarget(revealed.current);
    revealed.current = true;
  }, [list, revealKey, revealTarget]);

  const scrollPage = useCallback((edge: StripEdge) => {
    if (list) {
      scrollStripTo(list, pageScrollLeft(geometryOf(list), edge), true);
    }
  }, [list]);

  return { ...overflow, listRef: setList, scrollPage, reveal };
}
