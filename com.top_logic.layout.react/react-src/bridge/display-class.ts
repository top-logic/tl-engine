import React, { createContext, useContext } from 'react';

/**
 * The display class of the application: `compact` on a small screen, `regular` otherwise.
 *
 * <p>The app shell measures it - the one place the breakpoint stands (TLAppShell) - and gives it to everything
 * inside. A component rendered outside a shell is `regular`.</p>
 */
export type DisplayClass = 'compact' | 'regular';

const DisplayClassContext = createContext<DisplayClass>('regular');

/** Gives the components inside the display class the shell measured. */
export function DisplayClassProvider({ value, children }: React.PropsWithChildren<{ value: DisplayClass }>) {
  return React.createElement(DisplayClassContext.Provider, { value }, children);
}

/** The display class of the shell around the rendered component; `regular` outside a shell. */
export function useDisplayClass(): DisplayClass {
  return useContext(DisplayClassContext);
}
