import type { ComponentType } from 'react';
import type { TLCellProps } from './types';

const _components = new Map<string, ComponentType<TLCellProps>>();

/**
 * Registers a React component under the given module name.
 */
export function register(name: string, component: ComponentType<TLCellProps>): void {
  _components.set(name, component);
}

/**
 * Returns the component registered under the given module name, or undefined.
 */
export function getComponent(name: string): ComponentType<TLCellProps> | undefined {
  return _components.get(name);
}
