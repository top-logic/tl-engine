// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { TLChatConversation } from './chat-conversation'
import { chatStore, readBootstrap } from './chat-store'
import { answerCommands, resetTLBridge, setTLState, tlCommands } from '../test/tl-react-bridge'
import { eventually } from '../test/eventually'

/**
 * The `draft` of a `<chat-conversation>` belongs to the chat it shows (#30552): switching the
 * element to another chat puts that chat's text into the composer and onto the `draft` channel, and
 * switching back brings the first chat's text back - without taking the focus.
 *
 * Mounted in a live DOM, because what is asserted is the editor's text, the focus and what the
 * element reports onto the channel.
 */

let root: Root | null = null
let container: HTMLElement | null = null
let serial = 0
let chatId = ''

/** Somewhere else on the page the user is working while the chat switches. */
let elsewhere: HTMLInputElement | null = null

function render(extra: Record<string, unknown>): void {
  setTLState({ chatId, authenticated: true, language: 'de', ...extra })
  root!.render(React.createElement(TLChatConversation, { controlId: 'c1', state: {} }))
}

/** Shows the given chat, and waits until it is open: text typed before belongs to the chat left. */
async function show(sessionId: string, extra: Record<string, unknown> = {}): Promise<void> {
  render({ selected: sessionId, ...extra })
  await eventually(() => expect(chatStore(chatId, readBootstrap({ chatId })).getState().sessionId).toBe(sessionId))
  await eventually(() => expect(field()).toBeTruthy())
}

function field(): HTMLElement {
  return container!.querySelector('.token-input') as HTMLElement
}

/** The text the editor shows, without the invisible tail it keeps for the caret. */
function shown(): string {
  return (field().textContent ?? '').replace(/​/g, '')
}

/** The user types `text` into the empty editor. */
function type(text: string): void {
  const el = field()
  el.focus()
  el.append(document.createTextNode(text))
  el.dispatchEvent(new window.InputEvent('input', { bubbles: true, data: text }))
}

/** The texts the element reported onto the `draft` channel, in order. */
function draftReports(): unknown[] {
  return tlCommands()
    .filter((command) => command.name === 'publishDraft')
    .map((command) => command.args?.text)
}

/** The text the element reported last onto the `draft` channel. */
function lastDraftReport(): unknown {
  const reports = draftReports()
  return reports[reports.length - 1]
}

beforeEach(() => {
  resetTLBridge()
  chatId = `draft-${++serial}`
  answerCommands(async (name) => (name === 'selectSession' ? { success: true, opened: true } : { success: true }))
  globalThis.fetch = vi.fn(async (url: string) => {
    const id = /key=session%2F([^&]+)/.exec(url)?.[1]
    return { ok: true, status: 200, json: async () => (id ? { id, turnsJson: '', title: id } : {}) }
  }) as unknown as typeof fetch
  container = document.createElement('div')
  document.body.appendChild(container)
  elsewhere = document.createElement('input')
  document.body.appendChild(elsewhere)
  root = createRoot(container)
})

afterEach(() => {
  root?.unmount()
  container?.remove()
  elsewhere?.remove()
  root = null
  container = null
  elsewhere = null
})

describe('the draft of a conversation', () => {
  it('shows the text of the chat on screen, reports it, and leaves the focus where it is', async () => {
    await show('a')
    type('Frage zu A')
    await eventually(() => expect(lastDraftReport()).toBe('Frage zu A'))

    elsewhere!.focus()
    await show('b')
    expect(shown()).toBe('')
    await eventually(() => expect(lastDraftReport()).toBe(''))
    expect(document.activeElement).toBe(elsewhere)

    await show('a')
    expect(shown()).toBe('Frage zu A')
    await eventually(() => expect(lastDraftReport()).toBe('Frage zu A'))
    expect(document.activeElement).toBe(elsewhere)
  })

  it('applies a write of the channel to the chat on screen only', async () => {
    await show('a')
    type('Frage zu A')
    await eventually(() => expect(lastDraftReport()).toBe('Frage zu A'))

    await show('b')
    expect(shown()).toBe('')
    render({ selected: 'b', draftText: 'Bitte erkläre B.' })
    await eventually(() => expect(shown()).toBe('Bitte erkläre B.'))

    await show('a', { draftText: 'Bitte erkläre B.' })
    expect(shown()).toBe('Frage zu A')
    await show('b', { draftText: 'Bitte erkläre B.' })
    expect(shown()).toBe('Bitte erkläre B.')
  })
})
