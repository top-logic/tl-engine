/**
 * `tl-react-bridge` as the vitest run sees it.
 *
 * The bridge is the host application's module, external to this bundle and absent from
 * `node_modules`; the browser resolves it through a `<module-script specifier="tl-react-bridge">`.
 * The store's tests import modules that import it, so the run needs something under that name.
 *
 * It is deliberately only what importing those modules touches: `React.createElement`, because a
 * module-level `<svg/>` icon is built as the module loads, and the hook names they destructure at
 * module level without calling. Rendering is not what these tests do - a test that needs a rendered
 * component needs a real React, not this.
 */

type Element = { type: unknown; props: unknown; children: unknown[] }

function createElement(type: unknown, props: unknown, ...children: unknown[]): Element {
  return { type, props, children }
}

function absent(name: string): () => never {
  return () => {
    throw new Error(`tl-react-bridge stub: ${name} is not available outside a React render`)
  }
}

export const React = {
  createElement,
  Fragment: Symbol('Fragment'),
  forwardRef: <T>(render: T): T => render,
  useCallback: absent('useCallback'),
  useEffect: absent('useEffect'),
  useImperativeHandle: absent('useImperativeHandle'),
  useLayoutEffect: absent('useLayoutEffect'),
  useMemo: absent('useMemo'),
  useReducer: absent('useReducer'),
  useRef: absent('useRef'),
  useState: absent('useState'),
  useSyncExternalStore: absent('useSyncExternalStore'),
}

export function register(): void {
  /* no element registry in a test run */
}

export function useTLState(): Record<string, unknown> {
  return {}
}
