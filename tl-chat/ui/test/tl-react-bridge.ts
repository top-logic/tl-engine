/**
 * `tl-react-bridge` as the vitest run sees it.
 *
 * The bridge is the host application's module, external to this bundle and absent from
 * `node_modules`; the browser resolves it through a `<module-script specifier="tl-react-bridge">`.
 * The store's tests import modules that import it, so the run needs something under that name.
 *
 * `React` is the real one (a dev dependency), so a test may render a component with
 * `react-dom/server` and assert on what it produced. Everything else is the smallest stand-in that
 * behaves: the control state a test sets, a command sink it can read back, and a marker element in
 * place of a mounted child control - mounting one needs a module registry, an SSE subscription and
 * a store of its own, none of which exists outside a browser.
 */

import RealReact from 'react'

export const React = RealReact

/** The control state `useTLState()` hands to the component under test. */
let controlState: Record<string, unknown> = {}

/** Every command the component under test dispatched, in order. */
let dispatched: { name: string; args?: Record<string, unknown> }[] = []

/** Sets what `useTLState()` returns; call it before rendering. */
export function setTLState(state: Record<string, unknown>): void {
  controlState = state
}

/** The commands dispatched so far, and a reset. */
export function tlCommands(): { name: string; args?: Record<string, unknown> }[] {
  return dispatched
}

export function resetTLBridge(): void {
  controlState = {}
  dispatched = []
}

export function register(): void {
  /* no element registry in a test run */
}

export function useTLState(): Record<string, unknown> {
  return controlState
}

const dispatch = async (name: string, args?: Record<string, unknown>): Promise<unknown> => {
  dispatched.push({ name, args })
  return undefined
}

export function useTLCommand(): (
  command: string,
  args?: Record<string, unknown>,
) => Promise<unknown> {
  return dispatch
}

/** A child control as a marker element: what it is, not what it would render. */
export function TLChild({ control }: { control: unknown }) {
  const descriptor = (control ?? {}) as { controlId?: string; module?: string }
  return RealReact.createElement('tl-child', {
    'data-control-id': descriptor.controlId ?? '',
    'data-module': descriptor.module ?? '',
  })
}
