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
  patchListeners.clear()
  expectedEvents = 0
  staleReloadAnswer = true
  staleReloads = 0
  pageLeaving = false
}

/** What `reloadForStaleUi` answers, and how often it was called. */
let staleReloadAnswer = true
let staleReloads = 0

/** Whether `isPageLeaving` says the page is leaving. */
let pageLeaving = false

/** See `reloadForStaleUi` of the real bridge: answers what `setStaleReload` set, `true` by default. */
export function reloadForStaleUi(): boolean {
  staleReloads++
  return staleReloadAnswer
}

/** Sets what `reloadForStaleUi` answers. */
export function setStaleReload(answer: boolean): void {
  staleReloadAnswer = answer
}

/** How often `reloadForStaleUi` was called since the last reset. */
export function staleReloadCalls(): number {
  return staleReloads
}

/** See `isPageLeaving` of the real bridge. */
export function isPageLeaving(): boolean {
  return pageLeaving
}

/** Sets what `isPageLeaving` answers. */
export function setPageLeaving(leaving: boolean): void {
  pageLeaving = leaving
}

/** How often the chat told the window's stream to check that its events arrive. */
let expectedEvents = 0

/** See `expectEvents` of the real bridge. */
export function expectEvents(): void {
  expectedEvents++
}

/** How often `expectEvents` was called since the last reset. */
export function expectEventsCalls(): number {
  return expectedEvents
}

/** The listeners `subscribe` added, by the id the window's stream addresses its patches to. */
const patchListeners = new Map<string, Set<(patch: Record<string, unknown>) => void>>()

export function subscribe(controlId: string, listener: (patch: Record<string, unknown>) => void): void {
  const set = patchListeners.get(controlId) ?? new Set()
  set.add(listener)
  patchListeners.set(controlId, set)
}

export function unsubscribe(controlId: string, listener: (patch: Record<string, unknown>) => void): void {
  const set = patchListeners.get(controlId)
  set?.delete(listener)
  if (set?.size === 0) patchListeners.delete(controlId)
}

/** Whether anybody listens to `controlId` now. */
export function isSubscribed(controlId: string): boolean {
  return (patchListeners.get(controlId)?.size ?? 0) > 0
}

/** Delivers one patch of the window's event stream, as the bridge does. */
export function emitPatch(controlId: string, patch: Record<string, unknown>): void {
  for (const listener of [...(patchListeners.get(controlId) ?? [])]) listener(patch)
}

export function register(): void {
  /* no element registry in a test run */
}

export function useTLState(): Record<string, unknown> {
  return controlState
}

/** The bridge's own composition: the component's classes, then the configured `cssClass`. */
export function rootClassName(
  state: Record<string, unknown>,
  ...classes: (string | false | null | undefined)[]
): string {
  const configured = state.cssClass
  const all = typeof configured === 'string' ? [...classes, configured] : classes
  return all.filter(Boolean).join(' ')
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

/** Ends the fill chain, which a test has no layout for: renders the children as they are. */
export function FillBarrier({ children }: { children?: RealReact.ReactNode }) {
  return RealReact.createElement(RealReact.Fragment, null, children)
}

/** A child control as a marker element: what it is, not what it would render. */
export function TLChild({ control }: { control: unknown }) {
  const descriptor = (control ?? {}) as { controlId?: string; module?: string }
  return RealReact.createElement('tl-child', {
    'data-control-id': descriptor.controlId ?? '',
    'data-module': descriptor.module ?? '',
  })
}
