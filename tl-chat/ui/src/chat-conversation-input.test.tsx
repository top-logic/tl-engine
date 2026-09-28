// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { TLChatConversation } from './chat-conversation'
import { chatStore, readBootstrap } from './chat-store'
import { resetTLBridge, setTLState, tlCommands } from '../test/tl-react-bridge'

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

beforeEach(() => {
  resetTLBridge()
  globalThis.fetch = vi.fn(async (url: string) => {
    const id = /\/chat\/sessions\/([^/?]+)/.exec(url)?.[1]
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
    // The chat this window had open on another project's page.
    await chatStore(CHAT, readBootstrap(state({}))).actions.selectSession('other')
    await render({ selected: '' })
    expect(sessionReports()).toEqual(['other'])

    // The host pushed it back: the channel is empty again, and the server counts the refusal.
    await render({ selected: '', selectedCorrections: 1 })

    expect(chatStore(CHAT, readBootstrap(state({}))).getState().sessionId).toBeNull()
  })
})
