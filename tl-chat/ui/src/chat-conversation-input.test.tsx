// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { TLChatConversation } from './chat-conversation'
import { chatStore, readBootstrap } from './chat-store'
import { answerCommands, resetTLBridge, setTLState, TL_DATA_URL, tlCommand, tlCommands } from '../test/tl-react-bridge'

/**
 * `<chat-conversation input=…>` against a host that binds one conversation to what the page is
 * about, as BOS/Consulting binds one per project (#576).
 *
 * Mounted in a live DOM because both faults live in the element's effects: what it reports onto
 * the channel when it mounts, and what it does when the server says the channel refused a report.
 * The server side of the refusal is `TestSessionSelection`.
 */

const CHAT = 'project-chat'

let root: Root | null = null
let container: HTMLElement | null = null

/** The control state of the element; `authenticated: false` keeps the store off the network. */
function state(extra: Record<string, unknown>): Record<string, unknown> {
  return { chatId: CHAT, hasInput: true, authenticated: false, windowId: 'w1', ...extra }
}

async function render(extra: Record<string, unknown>): Promise<void> {
  setTLState(state(extra))
  root!.render(React.createElement(TLChatConversation, { controlId: 'c1', state: {} }))
  // The commit, then the effects' own awaits: a session load, then the report of what it opened.
  for (let round = 0; round < 10; round++) await new Promise((resolve) => setTimeout(resolve, 0))
}

function sessionReports(): unknown[] {
  return tlCommands()
    .filter((command) => command.name === 'publishSession')
    .map((command) => command.args?.id)
}

/** The chat a transcript read names. */
function transcriptOf(url: string): string | undefined {
  return /key=session%2F([^&]+)/.exec(url)?.[1]
}

beforeEach(() => {
  resetTLBridge()
  // The server opens every chat it is asked to.
  answerCommands(async (name) => (name === 'selectSession' ? { success: true, opened: true } : { success: true }))
  globalThis.fetch = vi.fn(async (url: string) => {
    const id = transcriptOf(url)
    return { ok: true, status: 200, json: async () => (id ? { id, turnsJson: '' } : {}) }
  }) as unknown as typeof fetch
  container = document.createElement('div')
  document.body.appendChild(container)
  root = createRoot(container)
})

afterEach(async () => {
  root?.unmount()
  container?.remove()
  root = null
  container = null
})

describe('the input channel of a conversation', () => {
  it('does not clear the channel the host set while the chat it names is still loading', async () => {
    await render({ selected: 'own' })

    expect(sessionReports()).not.toContain('')
    expect(chatStore(CHAT, readBootstrap(state({}))).getState().sessionId).toBe('own')
  })

  it('leaves a chat the host refused, although the channel was empty before and after', async () => {
    // The chat this window had open on another project's page, through the element there.
    const store = chatStore(CHAT, readBootstrap(state({})))
    const leave = store.subscribe(() => undefined, tlCommand, TL_DATA_URL)
    await store.actions.selectSession('other')
    await render({ selected: '' })
    leave()
    expect(sessionReports()).toEqual(['other'])

    // The host pushed it back: the channel is empty again, and the server counts the refusal.
    await render({ selected: '', selectedCorrections: 1 })

    expect(chatStore(CHAT, readBootstrap(state({}))).getState().sessionId).toBeNull()
  })

  it('does not go back to the chat the channel names while it answers a chat the store opened', async () => {
    await render({ selected: 'project' })
    const store = chatStore(CHAT, readBootstrap(state({})))
    expect(store.getState().sessionId).toBe('project')

    // The store opens another chat itself - a pick from the rail - and reports it. The channel has
    // not answered yet, so the element still holds the chat that was left.
    await store.actions.selectSession('picked')
    await render({ selected: 'project' })

    expect(store.getState().sessionId).toBe('picked')
    expect(sessionReports()).toEqual(['project', 'picked'])
  })

  it('does not load the chat it already shows again when it mounts on it', async () => {
    // The dock mounts its conversation on every page, on the chat the page's own element had open.
    const store = chatStore(CHAT, readBootstrap(state({})))
    const leave = store.subscribe(() => undefined, tlCommand, TL_DATA_URL)
    await store.actions.selectSession('open')
    const loads = () => vi.mocked(globalThis.fetch).mock.calls.filter(([url]) => transcriptOf(String(url)) === 'open').length
    const before = loads()

    await render({ selected: 'open' })
    leave()

    expect(loads()).toBe(before)
    expect(store.getState().sessionId).toBe('open')
  })

  it('does not reopen the chat this tab had on another page when the channel is empty', async () => {
    // A signed-in page that lists the remembered chat, so the store would restore it.
    const signedIn = { authenticated: true, windowId: 'w2', sessions: [{ id: 'previous-page', title: 'A' }] }
    const opened: string[] = []
    globalThis.fetch = vi.fn(async (url: string) => {
      const id = transcriptOf(url)
      if (id) opened.push(id)
      return { ok: true, status: 200, json: async () => (id ? { id, turnsJson: '' } : {}) }
    }) as unknown as typeof fetch
    for (const key of Object.keys(window.sessionStorage)) window.sessionStorage.removeItem(key)
    window.sessionStorage.setItem(`tl-chat-session:${CHAT}-restore`, 'previous-page')
    const chatId = CHAT + '-restore'

    await render({ ...signedIn, chatId, selected: '' })

    expect(opened).toEqual([])
    expect(chatStore(chatId, readBootstrap(state({ ...signedIn, chatId }))).getState().sessionId).toBeNull()
  })

  it('shows no chat when the channel is emptied while the one it named is still loading', async () => {
    // The reused window names the previous page's chat for a moment, then this page's: none (#577).
    let arrive: () => void = () => undefined
    const answer = globalThis.fetch
    globalThis.fetch = vi.fn(async (url: string, init?: unknown) => {
      if (transcriptOf(url) === 'previous-page') await new Promise<void>((resolve) => (arrive = resolve))
      return (answer as (url: string, init?: unknown) => Promise<unknown>)(url, init)
    }) as unknown as typeof fetch
    await render({ selected: 'previous-page' })
    await render({ selected: '' })
    arrive()
    await render({ selected: '' })

    expect(chatStore(CHAT, readBootstrap(state({}))).getState().sessionId).toBeNull()
    expect(sessionReports()).not.toContain('previous-page')
  })

  it('stays on its chat when the channel names another one for a moment', async () => {
    await render({ selected: 'this-page' })
    const store = chatStore(CHAT, readBootstrap(state({})))
    expect(store.getState().sessionId).toBe('this-page')

    // The previous page's chat is named for a moment, and its transcript arrives last (#577).
    let arrive: () => void = () => undefined
    const answer = globalThis.fetch
    globalThis.fetch = vi.fn(async (url: string, init?: unknown) => {
      if (transcriptOf(url) === 'previous-page') await new Promise<void>((resolve) => (arrive = resolve))
      return (answer as (url: string, init?: unknown) => Promise<unknown>)(url, init)
    }) as unknown as typeof fetch
    await render({ selected: 'previous-page' })
    await render({ selected: 'this-page' })
    arrive()
    await render({ selected: 'this-page' })

    expect(store.getState().sessionId).toBe('this-page')
    expect(sessionReports()).not.toContain('previous-page')
  })
})
