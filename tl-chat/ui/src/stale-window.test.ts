import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { chatFetch, reloadStaleWindow, StaleWindowError } from './stale-window'
import { resetTLBridge, setPageLeaving, setStaleReload, staleReloadCalls } from '../test/tl-react-bridge'

/**
 * The chat reloads a page the server no longer knows through the bridge, so that the chat and the
 * page's own React commands share one guard, and a page that is leaving is left alone.
 */
describe('reloading a stale window', () => {
  const globals = globalThis as unknown as Record<string, unknown>
  let fetchBefore: unknown

  /** Lets every `fetch` answer as the server does for a window it cannot resolve. */
  function answerStale(): void {
    const answer = {
      ok: false,
      status: 409,
      json: async () => ({ status: 'error', error: 'stale_window' }),
      clone: () => answer,
    }
    globals.fetch = vi.fn(async () => answer)
  }

  beforeEach(() => {
    resetTLBridge()
    fetchBefore = globals.fetch
  })

  afterEach(() => {
    globals.fetch = fetchBefore
  })

  it('asks the bridge to reload', () => {
    expect(reloadStaleWindow()).toBe(true)
    expect(staleReloadCalls()).toBe(1)
  })

  it('answers what the bridge answers when its guard refuses', () => {
    setStaleReload(false)

    expect(reloadStaleWindow()).toBe(false)
    expect(staleReloadCalls()).toBe(1)
  })

  it('does not reload a page that is leaving, which is replaced anyway', () => {
    setPageLeaving(true)

    expect(reloadStaleWindow()).toBe(true)
    expect(staleReloadCalls()).toBe(0)
  })

  it('tells a caller of a stale answer that the page is reloading', async () => {
    answerStale()

    const error = await chatFetch('/chat/send').catch((e: unknown) => e)

    expect(error).toBeInstanceOf(StaleWindowError)
    expect((error as StaleWindowError).reloading).toBe(true)
    expect(staleReloadCalls()).toBe(1)
  })

  it('tells a caller of a stale answer that the guard refused the reload', async () => {
    answerStale()
    setStaleReload(false)

    const error = await chatFetch('/chat/send').catch((e: unknown) => e)

    expect(error).toBeInstanceOf(StaleWindowError)
    expect((error as StaleWindowError).reloading).toBe(false)
  })

  it('hands every other answer back without reloading', async () => {
    const answer = { ok: true, status: 200, json: async () => ({}), clone: () => answer }
    globals.fetch = vi.fn(async () => answer)

    await expect(chatFetch('/chat/send')).resolves.toBe(answer)
    expect(staleReloadCalls()).toBe(0)
  })
})
