import { React } from 'tl-react-bridge';

/**
 * Keyboard navigation among the items of a container by a roving tab stop.
 *
 * Exactly one item is in the tab order ({@link RovingFocus.tabIndexOf} gives it `0`, every other
 * item `-1`): the item focused last while it is displayed, otherwise the preferred one (e.g. the
 * selected item), otherwise the first. Within the container the arrow keys move the focus:
 *
 * - ArrowLeft / ArrowRight: the previous / next item in document order.
 * - ArrowUp / ArrowDown: the item in the row above / below, nearest by its horizontal center,
 *   determined from the rendered positions of the items - so the keys follow a wrapping row or a
 *   grid as it is laid out at the moment. In a container whose items all stand in one row, they
 *   move to the previous / next item like ArrowLeft / ArrowRight.
 * - Home / End: the first / last item.
 *
 * Only a key pressed on an item itself moves the focus; a key pressed in an element inside an item
 * (a text input, a button) is that element's.
 */
export interface RovingFocus {
  /** The `tabIndex` of the item at the given position. */
  tabIndexOf(index: number): number;

  /** Keyboard handler of the container. */
  onKeyDown(event: React.KeyboardEvent): void;

  /** Focus handler of the container, tracking the item that has the focus. */
  onFocus(event: React.FocusEvent): void;
}

/** Distance in pixels below which two item edges count as one row. */
const ROW_TOLERANCE_PX = 1;

/**
 * Manages a roving tab stop among the items of a container.
 *
 * @param containerRef
 *        The container element.
 * @param itemSelector
 *        Selects the items within the container, evaluated on the container (`:scope > …` for its
 *        direct children); in document order, the positions of the items.
 * @param count
 *        The number of items.
 * @param preferred
 *        The position of the item in the tab order while none was focused, e.g. the selected one;
 *        `-1` for the first. A change of it moves the tab stop to it.
 */
export function useRovingFocus(
  containerRef: React.RefObject<HTMLElement | null>,
  itemSelector: string,
  count: number,
  preferred: number,
): RovingFocus {
  const [focused, setFocused] = React.useState<number>(-1);

  React.useEffect(() => {
    // The preferred item (a new selection) becomes the tab stop.
    setFocused(-1);
  }, [preferred]);

  const tabStop = focused >= 0 && focused < count
    ? focused
    : (preferred >= 0 && preferred < count ? preferred : 0);

  const items = React.useCallback((): HTMLElement[] => {
    const container = containerRef.current;
    return container ? Array.from(container.querySelectorAll<HTMLElement>(itemSelector)) : [];
  }, [containerRef, itemSelector]);

  const onFocus = React.useCallback((event: React.FocusEvent) => {
    const all = items();
    const index = all.findIndex(item => item.contains(event.target as Node));
    if (index >= 0) {
      setFocused(index);
    }
  }, [items]);

  const onKeyDown = React.useCallback((event: React.KeyboardEvent) => {
    const all = items();
    const current = all.indexOf(event.target as HTMLElement);
    if (current < 0) {
      return;
    }
    let next: number;
    switch (event.key) {
      case 'ArrowLeft':
        next = current - 1;
        break;
      case 'ArrowRight':
        next = current + 1;
        break;
      case 'ArrowUp':
        next = verticalNeighbor(all, current, -1);
        break;
      case 'ArrowDown':
        next = verticalNeighbor(all, current, 1);
        break;
      case 'Home':
        next = 0;
        break;
      case 'End':
        next = all.length - 1;
        break;
      default:
        return;
    }
    event.preventDefault();
    if (next >= 0 && next < all.length && next !== current) {
      all[next].focus();
      setFocused(next);
    }
  }, [items]);

  const tabIndexOf = React.useCallback((index: number) => (index === tabStop ? 0 : -1), [tabStop]);

  return { tabIndexOf, onKeyDown, onFocus };
}

/**
 * The position of the item in the row above (`direction` -1) or below (1) the given one that is
 * nearest to it by horizontal center; the previous or next item where all items stand in one row,
 * `-1` where there is no row in that direction.
 */
function verticalNeighbor(items: HTMLElement[], current: number, direction: -1 | 1): number {
  const rects = items.map(item => item.getBoundingClientRect());
  const origin = rects[current];

  const oneRow = rects.every(rect => Math.abs(rect.top - origin.top) <= ROW_TOLERANCE_PX);
  if (oneRow) {
    return current + direction;
  }

  // The row next to the current one in the given direction: the nearest top beyond the current one.
  let rowTop: number | null = null;
  for (const rect of rects) {
    const beyond = direction > 0
      ? rect.top > origin.top + ROW_TOLERANCE_PX
      : rect.top < origin.top - ROW_TOLERANCE_PX;
    if (beyond && (rowTop === null || (direction > 0 ? rect.top < rowTop : rect.top > rowTop))) {
      rowTop = rect.top;
    }
  }
  if (rowTop === null) {
    return -1;
  }

  const center = origin.left + origin.width / 2;
  let best = -1;
  let bestDistance = Infinity;
  rects.forEach((rect, index) => {
    if (Math.abs(rect.top - rowTop!) <= ROW_TOLERANCE_PX) {
      const distance = Math.abs(rect.left + rect.width / 2 - center);
      if (distance < bestDistance) {
        best = index;
        bestDistance = distance;
      }
    }
  });
  return best;
}
