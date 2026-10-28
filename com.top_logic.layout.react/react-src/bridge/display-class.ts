import React, { createContext, useContext, useSyncExternalStore } from 'react';

/**
 * The display class of the application: `compact` on a small screen, `regular` otherwise.
 *
 * <p>It is a property of the browser window, not of a component: every component of the window sees the same
 * class - the controls in the app shell, the dialogs the dialog manager shows beside the shell, and the content of
 * a portal or of the tooltip host. This module is the one place the breakpoint stands; the app shell reports the
 * class to the server and marks its root with it (data-tl-display).</p>
 */
export type DisplayClass = 'compact' | 'regular';

/** Viewport width (px) at or below which the window is `compact`. */
export const COMPACT_MAX_WIDTH = 768;

const QUERY = `(max-width: ${COMPACT_MAX_WIDTH}px)`;

/** An explicit class for the components inside a {@link DisplayClassProvider}; `null` outside one. */
const DisplayClassContext = createContext<DisplayClass | null>(null);

/**
 * Gives the components inside a fixed display class instead of the one of the window, e.g. to render a component
 * in its compact form in a test or in a narrow preview.
 */
export function DisplayClassProvider({ value, children }: React.PropsWithChildren<{ value: DisplayClass }>) {
  return React.createElement(DisplayClassContext.Provider, { value }, children);
}

function query(): MediaQueryList | null {
  return typeof window !== 'undefined' && typeof window.matchMedia === 'function' ? window.matchMedia(QUERY) : null;
}

function subscribe(onChange: () => void): () => void {
  const list = query();
  if (list == null) {
    return () => {};
  }
  list.addEventListener('change', onChange);
  return () => list.removeEventListener('change', onChange);
}

/** The display class of the browser window, as it is now. */
export function windowDisplayClass(): DisplayClass {
  return query()?.matches ? 'compact' : 'regular';
}

/**
 * The display class the rendered component is shown in: the one of the browser window, re-rendering the component
 * when the window crosses the breakpoint, unless a {@link DisplayClassProvider} above it fixes another one.
 */
export function useDisplayClass(): DisplayClass {
  const fixed = useContext(DisplayClassContext);
  const measured = useSyncExternalStore(subscribe, windowDisplayClass, () => 'regular' as DisplayClass);
  return fixed ?? measured;
}
