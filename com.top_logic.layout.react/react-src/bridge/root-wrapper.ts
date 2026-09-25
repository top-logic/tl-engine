import React, { useSyncExternalStore } from 'react';
import type { ComponentType, ReactNode } from 'react';

/**
 * A component that wraps every React root of the bridge, e.g. a context provider of a component
 * library (a theme provider, an intl provider, an emotion cache provider).
 */
export type RootWrapper = ComponentType<{ children?: ReactNode }>;

/**
 * Options for {@link registerRootWrapper}.
 */
export interface RootWrapperOptions {
  /**
   * Position of the wrapper in the chain. The wrapper with the lowest order is the outermost one;
   * wrappers with equal order nest in registration order (the first registered is the outer one).
   * Defaults to {@link DEFAULT_ROOT_WRAPPER_ORDER}.
   */
  order?: number;
}

/** Order of a wrapper registered without an explicit {@link RootWrapperOptions.order}. */
export const DEFAULT_ROOT_WRAPPER_ORDER = 0;

interface Entry {
  wrapper: RootWrapper;
  order: number;
  seq: number;
}

/** Registered wrappers, sorted outermost first. Replaced (never mutated) on each registration. */
let _wrappers: readonly Entry[] = [];

/** Registration counter that keeps wrappers of equal order in registration order. */
let _seq = 0;

/** Re-render callbacks of the mounted {@link RootWrappers} components. */
const _subscribers = new Set<() => void>();

/**
 * Registers a component that wraps the content of every React root the bridge renders.
 *
 * <p>This is the hook for a customer component library that needs its context providers (theme,
 * intl, style cache, …) above all components it renders. The wrapper applies to:</p>
 * <ul>
 * <li>every control mounted through {@link mount} (the top-level controls of a page),</li>
 * <li>the per-window tooltip host,</li>
 * <li>all nested controls, since they render through {@link TLChild} inside their parent's React
 *     tree and therefore see the parent root's context,</li>
 * <li>all portals ({@code createPortal}), which inherit the context of the tree they are created
 *     in.</li>
 * </ul>
 *
 * <p>A wrapper registered after roots are already mounted takes effect immediately: the mounted
 * roots re-render with the extended wrapper chain. Since that changes the element tree above the
 * control, the control's subtree is remounted and loses its local React state (the server state
 * of the controls is kept). Libraries therefore register their wrappers when their script loads,
 * before the page's controls are mounted.</p>
 *
 * @param wrapper the component to render around the content of each root; it must render its
 *        {@code children}
 * @param options the position of the wrapper in the chain
 */
export function registerRootWrapper(wrapper: RootWrapper, options?: RootWrapperOptions): void {
  const entry: Entry = { wrapper, order: options?.order ?? DEFAULT_ROOT_WRAPPER_ORDER, seq: _seq++ };
  _wrappers = [..._wrappers, entry].sort((a, b) => a.order - b.order || a.seq - b.seq);
  for (const cb of _subscribers) {
    cb();
  }
}

function subscribeWrappers(callback: () => void): () => void {
  _subscribers.add(callback);
  return () => _subscribers.delete(callback);
}

function getWrappers(): readonly Entry[] {
  return _wrappers;
}

/**
 * Renders its children inside all wrappers registered with {@link registerRootWrapper}, outermost
 * first, and re-renders when a wrapper is registered.
 */
const RootWrappers: React.FC<{ children?: ReactNode }> = ({ children }) => {
  const wrappers = useSyncExternalStore(subscribeWrappers, getWrappers);
  let content: ReactNode = children;
  for (let i = wrappers.length - 1; i >= 0; i--) {
    content = React.createElement(wrappers[i].wrapper, null, content);
  }
  return React.createElement(React.Fragment, null, content);
};

/**
 * Wraps the content of a React root created by the bridge in the registered root wrappers.
 *
 * <p>Every {@code root.render(...)} of the bridge passes its content through this function.</p>
 */
export function wrapRoot(content: ReactNode): React.ReactElement {
  return React.createElement(RootWrappers, null, content);
}
