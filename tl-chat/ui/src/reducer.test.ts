import { describe, expect, it } from 'vitest'
import { chatReducer, type ChatAction } from './reducer'
import type { AssistantText, ChatMessage } from './types'

function run(actions: ChatAction[]): ChatMessage[] {
  return actions.reduce(chatReducer, [] as ChatMessage[])
}

function answer(messages: ChatMessage[]): AssistantText {
  const found = messages.find((m): m is AssistantText => m.type === 'assistant_text')
  if (!found) throw new Error('no assistant message')
  return found
}

describe('the streamed answer', () => {
  const CONTENT = 'Die Hauptstadt von Portugal ist Lissabon.'

  /**
   * The regression behind the truncated answers.
   *
   * `message_complete` used to overtake the text deltas still queued on the server, and the client
   * threw its payload away, so the last fragment was lost and the reader saw a sentence that
   * stopped mid-word. The authoritative text now wins, whenever it arrives.
   */
  it('ends as the authoritative text even when a delta arrives after message_complete', () => {
    const messages = run([
      { type: 'ADD_ASSISTANT', id: 'a1', text: '', isStreaming: true },
      { type: 'APPEND_TEXT', id: 'a1', delta: 'Die Hauptstadt von Portugal ist' },
      { type: 'APPEND_TEXT', id: 'a1', delta: ' L' },
      { type: 'SET_TEXT', id: 'a1', text: CONTENT },
      { type: 'APPEND_TEXT', id: 'a1', delta: 'issabon.' },
    ])
    expect(answer(messages).text).toBe(CONTENT)
  })

  /**
   * The order the server actually produces.
   *
   * Measured on every run: `text_delta …, turn_completed, run_completed, message_complete`. The
   * run's end finalizes the message, and only then does the authoritative text arrive - so it has
   * to land on a message that is already closed. The earlier version of this test asserted
   * `SET_TEXT` before `FINALIZE_TEXT`, an order the product never produces, which is how the
   * client half of the fix could sit there unable to fire.
   */
  it('is the authoritative text when it arrives after the run has finalized the message', () => {
    const messages = run([
      { type: 'ADD_ASSISTANT', id: 'a1', text: '', isStreaming: true },
      { type: 'APPEND_TEXT', id: 'a1', delta: 'Die Hauptstadt von Portugal ist' },
      { type: 'APPEND_TEXT', id: 'a1', delta: ' L' },
      { type: 'FINALIZE_TEXT', id: 'a1' },
      { type: 'SET_TEXT', id: 'a1', text: CONTENT },
    ])
    expect(answer(messages).text).toBe(CONTENT)
    expect(answer(messages).isStreaming).toBe(false)
  })

  it('is the authoritative text when it arrives before the message is finalized', () => {
    const messages = run([
      { type: 'ADD_ASSISTANT', id: 'a1', text: '', isStreaming: true },
      { type: 'APPEND_TEXT', id: 'a1', delta: 'Die Hauptstadt von Portugal ist' },
      { type: 'APPEND_TEXT', id: 'a1', delta: ' L' },
      { type: 'SET_TEXT', id: 'a1', text: CONTENT },
      { type: 'FINALIZE_TEXT', id: 'a1' },
    ])
    expect(answer(messages).text).toBe(CONTENT)
    expect(answer(messages).isStreaming).toBe(false)
  })

  it('keeps the deltas when no authoritative text arrives', () => {
    const messages = run([
      { type: 'ADD_ASSISTANT', id: 'a1', text: '', isStreaming: true },
      { type: 'APPEND_TEXT', id: 'a1', delta: 'Teil eins' },
      { type: 'APPEND_TEXT', id: 'a1', delta: ' und zwei' },
      { type: 'FINALIZE_TEXT', id: 'a1' },
    ])
    expect(answer(messages).text).toBe('Teil eins und zwei')
  })

  it('leaves other answers alone', () => {
    const messages = run([
      { type: 'ADD_ASSISTANT', id: 'a1', text: 'erste', isStreaming: false },
      { type: 'ADD_ASSISTANT', id: 'a2', text: '', isStreaming: true },
      { type: 'SET_TEXT', id: 'a2', text: 'zweite' },
    ])
    const texts = messages.filter((m): m is AssistantText => m.type === 'assistant_text').map((m) => m.text)
    expect(texts).toEqual(['erste', 'zweite'])
  })
})

describe('removing turns', () => {
  it('drops a user message and everything after it', () => {
    const messages = run([
      { type: 'ADD_USER', id: 'u1', text: 'eins' },
      { type: 'ADD_ASSISTANT', id: 'a1', text: 'antwort eins', isStreaming: false },
      { type: 'ADD_USER', id: 'u2', text: 'zwei' },
      { type: 'ADD_ASSISTANT', id: 'a2', text: 'antwort zwei', isStreaming: false },
      { type: 'REMOVE_FROM_USER', id: 'u2' },
    ])
    expect(messages.map((m) => m.id)).toEqual(['u1', 'a1'])
  })

  it('takes back a single message that was never sent', () => {
    const messages = run([
      { type: 'ADD_USER', id: 'u1', text: 'eins' },
      { type: 'ADD_USER', id: 'u2', text: 'zwei' },
      { type: 'REMOVE_MESSAGE', id: 'u1' },
    ])
    expect(messages.map((m) => m.id)).toEqual(['u2'])
  })
})

describe('the cost of a run', () => {
  it('is put on the line of the run it was reported for', () => {
    const messages = run([
      { type: 'START_RUN', runId: 'r1' },
      { type: 'START_RUN', runId: 'r2' },
      { type: 'SET_RUN_COST', runId: 'r2', cost: 0.0024, currency: 'USD' },
    ])
    const metas = messages.filter((m) => m.type === 'run_meta')
    expect(metas.map((m) => ('cost' in m ? m.cost : undefined))).toEqual([undefined, 0.0024])
    expect(metas.map((m) => ('currency' in m ? m.currency : undefined))).toEqual([undefined, 'USD'])
  })
})
