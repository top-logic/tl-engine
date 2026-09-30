// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { TLChatConversation } from './chat-conversation'
import { chatStore, readBootstrap, withTitle } from './chat-store'
import { resetTLBridge, setTLState } from '../test/tl-react-bridge'

/**
 * The header of an open conversation after its chat was renamed somewhere else (#596).
 *
 * The rename happens on the server - the session list's row menu calls `aiSessionRename` - and the
 * element hears of it only as a moved `sessionsRevision` in its control state (`SessionListWatch`).
 * Mounted in a live DOM, because what has to change is the text of the header.
 */

let root: Root | null = null
let container: HTMLElement | null = null

/** What `GET /chat/sessions` lists, and the title a transcript load answers with, by id. */
let listed: { id: string; title: string }[]

/** How often the chat list was read. */
let listReads: number

function state(chatId: string, extra: Record<string, unknown>): Record<string, unknown> {
  return { chatId, hasInput: true, authenticated: true, windowId: 'w1', language: 'de', ...extra }
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
  listReads = 0
  globalThis.fetch = vi.fn(async (url: string) => {
    const id = /\/chat\/sessions\/([^/?]+)/.exec(url)?.[1]
    let body: unknown = {}
    if (id) {
      body = { id, turnsJson: '', title: listed.find((session) => session.id === id)?.title }
    } else if (url.includes('/chat/sessions')) {
      listReads++
      body = { sessions: listed }
    } else if (url.includes('/chat/capabilities')) {
      body = { agents: [], models: [], mcpServers: [] }
    }
    return { ok: true, status: 200, json: async () => body }
  }) as unknown as typeof fetch
  ;(globalThis as unknown as { EventSource: unknown }).EventSource = class {
    addEventListener(): void {}
    close(): void {}
  }
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
  it('shows the new title in the header as soon as the server counts the change', async () => {
    await render('rename-push', { selected: 'first', sessionsRevision: 0 })
    expect(header()).toBe('Alter Titel')

    rename('first', 'Neuer Titel')
    await render('rename-push', { selected: 'first', sessionsRevision: 1 })

    expect(header()).toBe('Neuer Titel')
  })

  it('does not read the list again for the count it mounts with', async () => {
    await render('rename-mount', { selected: 'first', sessionsRevision: 7 })
    const reads = listReads

    await render('rename-mount', { selected: 'first', sessionsRevision: 7 })

    expect(listReads).toBe(reads)
  })

  it('shows the new title after switching away and back, even without the count', async () => {
    // The window missed the change - a host that runs no refresh service, say. Opening the chat
    // reads its transcript, and that answer carries the title the server has now.
    await render('rename-switch', { selected: 'first' })
    expect(header()).toBe('Alter Titel')

    rename('first', 'Neuer Titel')
    await render('rename-switch', { selected: 'second' })
    await render('rename-switch', { selected: 'first' })

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
