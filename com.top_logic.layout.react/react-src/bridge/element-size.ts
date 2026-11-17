/**
 * The size of an element as state, for a control that chooses its arrangement by the room its own element has rather
 * than by the size of the browser window: in a narrow dialog or a sidebar of a wide window the room is small as well.
 */
import React from 'react';

/**
 * The width of the element in pixels (its border box, `offsetWidth`), measured before the first paint and again
 * whenever the element changes its size.
 *
 * An element that is not laid out (not yet mounted, hidden, in an environment without layout) measures `0`; a caller
 * deciding by the width treats `0` as "not known".
 *
 * @param ref The element to measure.
 */
export function useElementWidth(ref: React.RefObject<HTMLElement | null>): number {
  const [width, setWidth] = React.useState(0);
  React.useLayoutEffect(() => {
    const element = ref.current;
    if (!element) {
      return undefined;
    }
    const measure = () => {
      const next = element.offsetWidth;
      setWidth((previous) => (previous === next ? previous : next));
    };
    measure();
    if (typeof ResizeObserver === 'undefined') {
      return undefined;
    }
    const observer = new ResizeObserver(measure);
    observer.observe(element);
    return () => observer.disconnect();
  }, [ref]);
  return width;
}
