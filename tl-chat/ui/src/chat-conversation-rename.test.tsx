// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { TLChatConversation } from './chat-conversation'
import { chatStore, readBootstrap, withTitle } from './chat-store'
import { answerCommands, resetTLBridge, setTLState } from '../test/tl-react-bridge'

/**
 * The header of an open conversation after its chat was renamed somewhere else (#596).
 *
 * The rename happens on the server - the session list's row menu calls `aiSessionRename` - and the
 * element hears of it as the chat list in its control state, which the server pushes again
 * (`SessionListWatch`).
 * Mounted in a live DOM, because what has to change is the text of the header.
 */

let root: Root | null = null
let container: HTMLElement | null = null

/** The chats the server lists, and the title a transcript read answers with, by id. */
let listed: { id: string; title: string }[]

/** Every URL the store asked `fetch` for. */
let requested: string[]

function state(chatId: string, extra: Record<string, unknown>): Record<string, unknown> {
  return { chatId, hasInput: true, authenticated: true, language: 'de', ...extra }
}

async function render(chatId: string, extra: Record<string, unknown>): Promise<void> {
  setTLState(state(chatId, extra))
  root!.render(React.createElement(TLChatConversation, { controlId: 'c1', state: {} }))
  for (let round = 0; round < 10; round++) await new Promise((resolve) => setTimeout(resolve, 0))
}

function header(): string | null | undefined {
  return container?.querySelector('.thread-bar h1')?.textContent
}

function rename(id: string, title: string): void {
  listed = listed.map((session) => (session.id === id ? { ...session, title } : session))
}

beforeEach(() => {
  resetTLBridge()
  listed = [
    { id: 'first', title: 'Alter Titel' },
    { id: 'second', title: 'Anderer Chat' },
  ]
  requested = []
  answerCommands(async (name) => (name === 'selectSession' ? { success: true, opened: true } : { success: true }))
  globalThis.fetch = vi.fn(async (url: string) => {
    requested.push(url)
    const id = /key=session%2F([^&]+)/.exec(url)?.[1]
    const body = id ? { id, turnsJson: '', title: listed.find((session) => session.id === id)?.title } : {}
    return { ok: true, status: 200, json: async () => body }
  }) as unknown as typeof fetch
  for (const key of Object.keys(window.sessionStorage)) window.sessionStorage.removeItem(key)
  container = document.createElement('div')
  document.body.appendChild(container)
  root = createRoot(container)
})

afterEach(() => {
  root?.unmount()
  container?.remove()
  root = null
  container = null
})

describe('a chat renamed while it is open', () => {
  it('shows the new title in the header as soon as the server sends the list again', async () => {
    await render('rename-push', { selected: 'first', sessions: listed })
    expect(header()).toBe('Alter Titel')

    rename('first', 'Neuer Titel')
    await render('rename-push', { selected: 'first', sessions: listed })

    expect(header()).toBe('Neuer Titel')
  })

  it('reads no chat list of its own: the list comes with the state', async () => {
    await render('rename-mount', { selected: 'first', sessions: listed })

    expect(requested.filter((url) => !/key=session%2F/.test(url))).toEqual([])
  })

  it('shows the new title after switching away and back, even without the list', async () => {
    // The window missed the change - a host that runs no refresh service, say. Opening the chat
    // reads its transcript, and that answer carries the title the server has now.
    const before = listed
    await render('rename-switch', { selected: 'first', sessions: before })
    expect(header()).toBe('Alter Titel')

    rename('first', 'Neuer Titel')
    await render('rename-switch', { selected: 'second', sessions: before })
    await render('rename-switch', { selected: 'first', sessions: before })

    expect(header()).toBe('Neuer Titel')
    expect(chatStore('rename-switch', readBootstrap(state('rename-switch', {}))).getState().sessionTitle).toBe(
      'Neuer Titel',
    )
  })
})

describe('withTitle', () => {
  const sessions = [
    { id: 'a', title: 'A' },
    { id: 'b', title: 'B' },
  ]

  it('retitles the one chat', () => {
    expect(withTitle(sessions, 'b', 'Neu')).toEqual([
      { id: 'a', title: 'A' },
      { id: 'b', title: 'Neu' },
    ])
  })

  it('hands back the same list when nothing differs', () => {
    expect(withTitle(sessions, 'b', 'B')).toBe(sessions)
    expect(withTitle(sessions, 'unknown', 'X')).toBe(sessions)
    expect(withTitle(sessions, 'a', undefined)).toBe(sessions)
    expect(withTitle(sessions, 'a', '')).toBe(sessions)
  })
})
