import { describe, expect, it, vi } from 'vitest'

// The sanitizer wants a DOM and this suite runs without one. Nothing here asserts on sanitizing -
// what is under test is which parts of a turn the thread renders while a run is going.
vi.mock('dompurify', () => ({ default: { sanitize: (html: string) => html } }))

import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { Thread } from './thread'
import { chatReducer, type ChatAction } from './reducer'
import { bundle } from './i18n'
import type { ChatMessage, TraceMode } from './types'

const labels = bundle('en')

function build(actions: ChatAction[]): ChatMessage[] {
  let messages: ChatMessage[] = []
  for (const action of actions) messages = chatReducer(messages, action)
  return messages
}

/** The transcript as the browser renders it, for one set of messages and one busy state. */
function render(messages: ChatMessage[], busy: boolean, traceMode: TraceMode = 'summary'): string {
  return renderToStaticMarkup(
    React.createElement(Thread, {
      messages,
      traceMode,
      welcomeName: 'Nutzer',
      busy,
      regenerating: false,
      feedback: [],
      labels,
      language: 'en' as const,
      effort: '',
      onEditSend: async () => true,
      onRegenerate: () => undefined,
      onRate: () => undefined,
      onOpenRail: () => undefined,
      railCollapsed: true,
      onToggleRail: () => undefined,
      title: 'Titel',
    }),
  )
}

/** The moments of a turn that answers without calling a single tool. */
const SENT: ChatAction[] = [{ type: 'ADD_USER', id: 'u1', text: 'Was ist die Hauptstadt?' }]
const RUN_STARTED: ChatAction[] = [...SENT, { type: 'START_RUN', runId: 'r1' }]
const TURN_STARTED: ChatAction[] = [
  ...RUN_STARTED,
  { type: 'ADD_ASSISTANT', id: 'a1', text: '', isStreaming: true },
]
const THINKING: ChatAction[] = [...TURN_STARTED, { type: 'APPEND_THINKING', delta: 'Erst überlegen' }]
const FIRST_DELTA: ChatAction[] = [...TURN_STARTED, { type: 'APPEND_TEXT', id: 'a1', delta: 'Lissabon' }]

/**
 * A turn with no tool call, from the send to the first token (Gitea #429).
 *
 * The thread is where the answer will be, so it is where the wait belongs. Before this the run
 * said nothing there at all: the only sign of life was the composer's stop button.
 */
describe('a running turn', () => {
  it('says it is working from the send on, before any frame has arrived', () => {
    const html = render(build(SENT), true)
    expect(html).toContain(labels.working)
    expect(html).toContain('answer is-pending')
  })

  it('keeps saying it after run_started, which carries no duration yet', () => {
    const html = render(build(RUN_STARTED), true)
    expect(html).toContain(labels.working)
    expect(html).toContain('answer is-pending')
  })

  /**
   * `turn_started` opens an empty streaming message. Rendered, it draws nothing: the streaming
   * caret hangs off the last markdown element and an empty answer has none.
   */
  it('holds the answer’s place while the streaming message is still empty', () => {
    const html = render(build(TURN_STARTED), true)
    expect(html).toContain('answer is-pending')
    expect(html).not.toContain('answer is-streaming')
  })

  it('holds it through a turn that only thinks', () => {
    const html = render(build(THINKING), true)
    // Thinking is a step, so in summary mode the run summary takes over the working line.
    expect(html).toContain('run-summary')
    expect(html).toContain(labels.working)
    expect(html).toContain('answer is-pending')
  })

  it('gives the place back to the answer on the first delta', () => {
    const html = render(build(FIRST_DELTA), true)
    expect(html).not.toContain('answer is-pending')
    expect(html).toContain('answer is-streaming')
    expect(html).toContain('Lissabon')
  })

  /** A run that failed before any text says so in the thread, and claims no steps it never took. */
  it('shows the failure where the answer would have been', () => {
    const html = render(
      build([
        ...TURN_STARTED,
        { type: 'ADD_ERROR', message: 'upstream 529', code: 'OVERLOADED' },
        { type: 'END_RUN', runId: 'r1' },
      ]),
      false,
    )
    expect(html).toContain('error-card')
    // The sentence comes from the kind, so a run read back from the server - which keeps the kind
    // and the provider's words, not a translation - renders the same card.
    expect(html).toContain(labels.providerErrorOverloaded)
    expect(html).toContain('upstream 529')
    expect(html).not.toContain('answer is-pending')
    expect(html).not.toContain(labels.runSteps(1))
  })

  /** A failure the provider said nothing about still reads as a sentence, not as an empty box. */
  it('names the failure even when the provider sent no message', () => {
    const html = render(
      build([...TURN_STARTED, { type: 'ADD_ERROR', message: '' }, { type: 'END_RUN', runId: 'r1' }]),
      false,
    )
    expect(html).toContain(labels.providerError)
    expect(html).not.toContain('error-card-detail')
  })

  /**
   * A run that continued on another model says so above the answer - quietly, and without claiming
   * to have taken a step it did not take (#344).
   */
  it('shows a model switch as a notice, not as an error or a step', () => {
    const html = render(
      build([
        ...TURN_STARTED,
        {
          type: 'ADD_NOTICE',
          kind: 'model_switched',
          detail: 'claude-sonnet-5 -> gpt-5: OVERLOADED: upstream 529',
        },
        { type: 'APPEND_TEXT', id: 'a1', delta: 'Lissabon' },
        { type: 'END_RUN', runId: 'r1' },
      ]),
      false,
    )
    expect(html).toContain('notice-line')
    expect(html).toContain(labels.modelSwitched)
    expect(html).toContain('claude-sonnet-5 -&gt; gpt-5: OVERLOADED: upstream 529')
    expect(html).not.toContain('error-card')
    // The switch is not work the agent did: counted as a step it would make the turn claim one.
    expect(html).not.toContain(labels.runSteps(1))
  })

  it('reports the duration once the run has ended', () => {
    const html = render(build([...FIRST_DELTA, { type: 'END_RUN', runId: 'r1' }]), false)
    expect(html).toContain('Worked for')
    expect(html).not.toContain(labels.working)
  })

  /**
   * Only the turn at the bottom is the running one. `busy` reached every turn, so each finished
   * answer above the running one went back to saying "Working…".
   */
  it('leaves the finished turns above it alone', () => {
    const html = render(
      build([
        { type: 'ADD_USER', id: 'u1', text: 'Erste Frage' },
        { type: 'START_RUN', runId: 'r1' },
        { type: 'ADD_ASSISTANT', id: 'a1', text: 'Erste Antwort', isStreaming: false },
        { type: 'END_RUN', runId: 'r1' },
        { type: 'ADD_USER', id: 'u2', text: 'Zweite Frage' },
      ]),
      true,
    )
    expect(html).toContain('Worked for')
    // Exactly one turn is working: the one at the bottom, with the placeholder under it.
    expect(html.split(labels.working).length - 1).toBe(1)
    expect(html.split('answer is-pending').length - 1).toBe(1)
  })
})
