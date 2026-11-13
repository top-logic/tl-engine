import React, { createContext, useContext, useEffect, useState } from 'react';

/**
 * The display class of the application: `compact` on a small screen, `regular` otherwise.
 *
 * <p>The app shell measures it (useMeasuredDisplayClass) and gives it to everything inside. A component rendered
 * outside a shell is `regular`, unless it measures the display class itself - a dialog, which is displayed beside the
 * shell rather than within it.</p>
 */
export type DisplayClass = 'compact' | 'regular';

/**
 * Viewport width (px) at or below which the UI is compact.
 *
 * The one place the breakpoint stands: the shell reports the display class measured against it to the server, gives it
 * to the components inside (useDisplayClass) and marks its root (data-tl-display). No stylesheet repeats it.
 */
const COMPACT_MAX_WIDTH = 768;

const COMPACT_QUERY = `(max-width: ${COMPACT_MAX_WIDTH}px)`;

/** The media query of the breakpoint, or null where the environment has none. */
function compactQuery(): MediaQueryList | null {
  return typeof window.matchMedia === 'function' ? window.matchMedia(COMPACT_QUERY) : null;
}

function displayClassOf(query: MediaQueryList | null): DisplayClass {
  return query?.matches ? 'compact' : 'regular';
}

const DisplayClassContext = createContext<DisplayClass>('regular');

/** Gives the components inside the display class the shell measured. */
export function DisplayClassProvider({ value, children }: React.PropsWithChildren<{ value: DisplayClass }>) {
  return React.createElement(DisplayClassContext.Provider, { value }, children);
}

/** The display class of the shell around the rendered component; `regular` outside a shell. */
export function useDisplayClass(): DisplayClass {
  return useContext(DisplayClassContext);
}

/**
 * The display class of the browser window, measured against the breakpoint and updated when the browser window
 * crosses it.
 */
export function useMeasuredDisplayClass(): DisplayClass {
  const [displayClass, setDisplayClass] = useState<DisplayClass>(() => displayClassOf(compactQuery()));
  useEffect(() => {
    const query = compactQuery();
    if (!query) {
      return;
    }
    const update = () => setDisplayClass(displayClassOf(query));
    update();
    query.addEventListener('change', update);
    return () => query.removeEventListener('change', update);
  }, []);
  return displayClass;
}
