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
let dispatched: { name: string; args?: Record<string, unknown>; options?: CommandOptions }[] = []

/** Sets what `useTLState()` returns; call it before rendering. */
export function setTLState(state: Record<string, unknown>): void {
  controlState = state
}

/** The commands dispatched so far, and a reset. */
export function tlCommands(): { name: string; args?: Record<string, unknown>; options?: CommandOptions }[] {
  return dispatched
}

export function resetTLBridge(): void {
  controlState = {}
  dispatched = []
  patchListeners.clear()
  uploads.length = 0
  uploadAnswer = async () => ({ success: true })
  pageLeaving = false
  commandAnswer = async () => undefined
}

/** Whether `isPageLeaving` says the page is leaving. */
let pageLeaving = false

/** See `isPageLeaving` of the real bridge. */
export function isPageLeaving(): boolean {
  return pageLeaving
}

/** Sets what `isPageLeaving` answers. */
export function setPageLeaving(leaving: boolean): void {
  pageLeaving = leaving
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

/** See `CommandResponse` of the real bridge. */
export type CommandResponse = Record<string, unknown>

/** Options of a command, as the bridge's `useTLCommand` function takes them. */
export interface CommandOptions {
  flushFields?: boolean
  ignoreStaleUi?: boolean
}

/** What a dispatched command is answered with; `undefined` by default, like a failed command. */
let commandAnswer: (name: string, args?: Record<string, unknown>) => Promise<CommandResponse | undefined> =
  async () => undefined

/** Sets how a dispatched command is answered, until the next reset. */
export function answerCommands(
  answer: (name: string, args?: Record<string, unknown>) => Promise<CommandResponse | undefined>,
): void {
  commandAnswer = answer
}

/**
 * The command function `useTLCommand` hands out: records the command, then answers it as
 * `answerCommands` set.
 */
export const tlCommand = async (
  name: string,
  args?: Record<string, unknown>,
  options?: CommandOptions,
): Promise<CommandResponse | undefined> => {
  dispatched.push({ name, args, options })
  return commandAnswer(name, args)
}

export function useTLCommand(): (
  command: string,
  args?: Record<string, unknown>,
  options?: CommandOptions,
) => Promise<CommandResponse | undefined> {
  return tlCommand
}

/** The uploads `useTLUpload`'s function sent since the last reset: the files of each. */
export const uploads: File[][] = []

/** What an upload is answered with; a plain success by default. */
let uploadAnswer: (files: File[]) => Promise<CommandResponse | undefined> = async () => ({ success: true })

/** Sets how an upload is answered, until the next reset. */
export function answerUploads(answer: (files: File[]) => Promise<CommandResponse | undefined>): void {
  uploadAnswer = answer
}

/** The upload function `useTLUpload` hands out: records the files, then answers as `answerUploads` set. */
export const tlUpload = async (formData: FormData): Promise<CommandResponse | undefined> => {
  const files = formData.getAll('file').filter((value): value is File => value instanceof File)
  uploads.push(files)
  return uploadAnswer(files)
}

export function useTLUpload(): (formData: FormData) => Promise<CommandResponse | undefined> {
  return tlUpload
}

/** What `useTLDataUrl` hands out: a data URL of a control named for the test. */
export const TL_DATA_URL = '/react-api/data?controlId=test-control&windowName=test-window'

export function useTLDataUrl(): string {
  return TL_DATA_URL
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
