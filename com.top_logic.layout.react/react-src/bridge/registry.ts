import type { ComponentType } from 'react';
import type { TLCellProps } from './types';

/** Components registered with {@link register}, by module name. */
const _components = new Map<string, ComponentType<TLCellProps>>();

/** Components registered with {@link replace}, by module name. Consulted before {@link _components}. */
const _replacements = new Map<string, ComponentType<TLCellProps>>();

/**
 * Registers a React component under the given module name.
 *
 * <p>The module name is the name under which the server-side control addresses the component that
 * renders its state. Registering a name a second time overrides the first registration and logs a
 * warning. A component registered with {@link replace} under the same name takes precedence.</p>
 */
export function register(name: string, component: ComponentType<TLCellProps>): void {
  if (_components.has(name)) {
    console.warn('[TLReact] Component registered twice, the later registration wins:', name);
  }
  _components.set(name, component);
}

/**
 * Replaces the component that renders the given module name with the given component.
 *
 * <p>This is the hook for a customer component library: an adapter module renders a TopLogic
 * control's state (e.g. {@code "TLButton"}) with a component of the library. The replacement takes
 * precedence over every component registered with {@link register} under the same name,
 * independent of the order in which the scripts are loaded. Replacing a name a second time
 * overrides the first replacement and logs a warning.</p>
 *
 * <p>The replacement receives the same props as the component it replaces ({@link TLCellProps}:
 * the control ID and the control state) and runs in the same control context, so it uses the same
 * hooks ({@code useTLState}, {@code useTLCommand}, …).</p>
 *
 * <p>A top-level control looks up its component when it is mounted, a nested control each time it
 * is rendered; an adapter module therefore registers its replacements when its script loads,
 * before the page's controls are mounted.</p>
 */
export function replace(name: string, component: ComponentType<TLCellProps>): void {
  if (_replacements.has(name)) {
    console.warn('[TLReact] Component replaced twice, the later replacement wins:', name);
  }
  _replacements.set(name, component);
}

/**
 * Returns the component that renders the given module name: the one registered with
 * {@link replace}, else the one registered with {@link register}, else undefined.
 */
export function getComponent(name: string): ComponentType<TLCellProps> | undefined {
  return _replacements.get(name) ?? _components.get(name);
}
