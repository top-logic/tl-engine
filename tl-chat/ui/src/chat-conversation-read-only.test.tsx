// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { readOnlyConversation, STATE_READ_ONLY, TLChatConversation } from './chat-conversation'
import { bundle } from './i18n'
import { answerCommands, resetTLBridge, setTLState } from '../test/tl-react-bridge'
import { eventually } from '../test/eventually'

/**
 * A conversation the account may read but not change (`readOnly` in the control state): the
 * transcript with its copy action, and in place of the composer a short note - no input line, no
 * approval or question cards, no stop, no editing, regenerating or rating.
 *
 * Mounted in a live DOM, because what is asserted is which controls the page offers.
 */

const LABELS = bundle('de')

/** One answered turn, so that the transcript offers its actions. */
const TRANSCRIPT = JSON.stringify([
  {
    id: 't1',
    turnNumber: 1,
    timestamp: 1000,
    userParts: [{ id: 'u1', type: 'text', timestamp: 1000, textData: { text: 'Was ist offen?' } }],
    assistantParts: [{ id: 'a1', type: 'text', timestamp: 1100, textData: { text: 'Nichts mehr.' } }],
  },
])

let root: Root | null = null
let container: HTMLElement | null = null
let serial = 0

function render(extra: Record<string, unknown>): void {
  setTLState({ chatId: `read-only-${++serial}`, authenticated: true, language: 'de', selected: 'shared', ...extra })
  root!.render(React.createElement(TLChatConversation, { controlId: 'c1', state: {} }))
}

/** The labels of the buttons the page offers. */
function buttons(): string[] {
  return [...(container?.querySelectorAll('button') ?? [])].map((button) => button.getAttribute('aria-label') ?? '')
}

beforeEach(() => {
  resetTLBridge()
  answerCommands(async (name) => (name === 'selectSession' ? { success: true, opened: true } : { success: true }))
  globalThis.fetch = vi.fn(async (url: string) => {
    const id = /key=session%2F([^&]+)/.exec(url)?.[1]
    return { ok: true, status: 200, json: async () => (id ? { id, turnsJson: TRANSCRIPT, title: 'Geteilt' } : {}) }
  }) as unknown as typeof fetch
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

describe('a read-only conversation', () => {
  it('shows the transcript with its copy action and a note in place of the composer', async () => {
    render({ [STATE_READ_ONLY]: true })

    await eventually(() => expect(buttons()).toContain(LABELS.copyAnswer))
    expect(container!.querySelector('.composer')).toBeNull()
    expect(container!.querySelector('.read-only-note')?.textContent).toBe(LABELS.readOnly)
    expect(buttons()).not.toContain(LABELS.goodAnswer)
    expect(buttons()).not.toContain(LABELS.badAnswer)
    expect(buttons()).not.toContain(LABELS.regenerate)
    expect(buttons()).not.toContain(LABELS.editAndSendAgain)
  })

  it('offers the composer and the actions that change the chat when it may be written', async () => {
    render({ [STATE_READ_ONLY]: false })

    await eventually(() => expect(buttons()).toContain(LABELS.copyAnswer))
    expect(container!.querySelector('.composer')).not.toBeNull()
    expect(container!.querySelector('.read-only-note')).toBeNull()
    expect(buttons()).toContain(LABELS.goodAnswer)
    expect(buttons()).toContain(LABELS.regenerate)
    expect(buttons()).toContain(LABELS.editAndSendAgain)
  })
})

describe('readOnlyConversation', () => {
  it('is read-only only for a chat that is shown', () => {
    expect(readOnlyConversation({ [STATE_READ_ONLY]: true }, 'shared')).toBe(true)
    expect(readOnlyConversation({ [STATE_READ_ONLY]: true }, null)).toBe(false)
    expect(readOnlyConversation({ [STATE_READ_ONLY]: true }, '')).toBe(false)
    expect(readOnlyConversation({ [STATE_READ_ONLY]: false }, 'shared')).toBe(false)
    expect(readOnlyConversation({}, 'shared')).toBe(false)
  })

  it('has a note in both languages', () => {
    expect(bundle('en').readOnly).toBe('Read only')
    expect(bundle('de').readOnly).toBe('Nur lesen')
  })
})
