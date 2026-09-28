import { describe, expect, it, vi } from 'vitest'

// The sanitizer wants a DOM and this suite runs without one. Nothing here asserts on sanitizing -
// what is under test is which parts of a turn the thread renders while a run is going.
vi.mock('dompurify', () => ({ default: { sanitize: (html: string) => html } }))

import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { readableToolOutput, Thread } from './thread'
import { chatReducer, type ChatAction } from './reducer'
import { bundle, type Language } from './i18n'
import { turnsJsonToMessages } from './api'
import type { ChatMessage, TraceMode } from './types'

const labels = bundle('en')

function build(actions: ChatAction[]): ChatMessage[] {
  let messages: ChatMessage[] = []
  for (const action of actions) messages = chatReducer(messages, action)
  return messages
}

/** The transcript as the browser renders it, for one set of messages and one busy state. */
function render(
  messages: ChatMessage[],
  busy: boolean,
  traceMode: TraceMode = 'summary',
  language: Language = 'en',
): string {
  return renderToStaticMarkup(
    React.createElement(Thread, {
      messages,
      traceMode,
      welcomeName: 'Nutzer',
      busy,
      regenerating: false,
      feedback: [],
      labels: bundle(language),
      language,
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

  /**
   * A run the user stopped says so under what it streamed, in the reader's language, and is neither
   * an error nor a step (#549).
   */
  it('shows a cancel as a notice under the partial answer', () => {
    const html = render(
      build([
        ...FIRST_DELTA,
        { type: 'ADD_NOTICE', kind: 'cancelled', detail: '' },
        { type: 'END_RUN', runId: 'r1' },
      ]),
      false,
    )
    expect(html).toContain('notice-line')
    expect(html).toContain(labels.cancelled)
    expect(html.indexOf(labels.cancelled)).toBeGreaterThan(html.indexOf('Lissabon'))
    expect(html).not.toContain('notice-line-detail')
    expect(html).not.toContain('error-card')
    expect(html).not.toContain(labels.runSteps(1))
  })

  /**
   * The same cancel read back from the server - after a reload, or in another window of the
   * account - reads exactly as it did live: the recorded `CANCELLED` event as the one notice, in the
   * reader's language, and not the server's English details.
   */
  it('shows a reloaded cancel as the same notice, in the reader\'s language', () => {
    const turns = [
      {
        id: 't1',
        turnNumber: 1,
        timestamp: 1000,
        userParts: [{ id: 'u', type: 'text', timestamp: 1000, textData: { text: 'Zähl langsam.' } }],
        assistantParts: [
          { id: 'a', type: 'text', timestamp: 2000, textData: { text: 'Eins, zwei, ' } },
          {
            id: 'c',
            type: 'system',
            timestamp: 3000,
            systemData: { eventType: 'CANCELLED', details: 'User cancelled processing' },
          },
        ],
      },
    ]
    const html = render(turnsJsonToMessages(JSON.stringify(turns)), false, 'summary', 'de')
    expect(html).toContain(bundle('de').cancelled)
    expect(html.indexOf(bundle('de').cancelled)).toBeGreaterThan(html.indexOf('Eins, zwei,'))
    expect(html).not.toContain('User cancelled processing')
    expect(html).not.toContain('error-card')
  })

  /**
   * The defect of this change: the guard rolled the turn back, the model answered anyway, and the
   * thread showed the answer alone. The notice belongs in the turn, under its steps, so the reader
   * sees it while reading what the agent claims to have done.
   */
  it('shows a rolled-back turn as a warning notice inside that turn', () => {
    const html = render(
      build([
        ...TURN_STARTED,
        { type: 'ADD_TOOL', toolUseId: 'tc1', name: 'browserScreenshot', input: {} },
        { type: 'UPDATE_TOOL', toolUseId: 'tc1', status: 'done' },
        {
          type: 'ADD_NOTICE',
          kind: 'transaction_rolled_back',
          detail: 'Pre-commit security check failed',
        },
        { type: 'APPEND_TEXT', id: 'a1', delta: 'Screenshot gespeichert.' },
        { type: 'END_RUN', runId: 'r1' },
      ]),
      false,
    )
    expect(html).toContain('notice-line is-warning')
    expect(html).toContain(labels.transactionRolledBack)
    expect(html).toContain(`${labels.reason}: Pre-commit security check failed`)
    // Not a failed run: the danger card is what a turn that produced nothing wears.
    expect(html).not.toContain('error-card')
    // And not a step either - the rollback is not work the agent did.
    expect(html).toContain(labels.runSteps(1))
  })

  /** A model switch keeps the quiet line it had: nothing the reader has to act on (#344). */
  it('keeps the model-switch notice free of the warning accent', () => {
    const html = render(
      build([
        ...TURN_STARTED,
        { type: 'ADD_NOTICE', kind: 'model_switched', detail: 'a -> b: OVERLOADED' },
        { type: 'END_RUN', runId: 'r1' },
      ]),
      false,
    )
    expect(html).toContain('notice-line')
    expect(html).not.toContain('is-warning')
  })

  /**
   * A commit failure may report a reason of any length, and a notice that pushed the answer off
   * the screen would be worse than none. Cut to a head with an expander, as tool arguments are.
   */
  it('clamps a very long rollback reason', () => {
    const reason = 'x'.repeat(500)
    const html = render(
      build([
        ...TURN_STARTED,
        { type: 'ADD_NOTICE', kind: 'transaction_rolled_back', detail: reason },
        { type: 'END_RUN', runId: 'r1' },
      ]),
      false,
    )
    expect(html).not.toContain(reason)
    expect(html).toContain('x'.repeat(300))
    expect(html).toContain(labels.showMore(200))
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

describe('readableToolOutput', () => {
  it('drops the untrusted-content tags and keeps what stands between them (#552)', () => {
    const output =
      'Replaced 1 occurrence. It reads, around the first change:\n' +
      '<untrusted_model_data source="GenericObject:2575#description">\n' +
      '  1| Bahnfahrt Frankfurt - Berlin\n' +
      '</untrusted_model_data>'
    expect(readableToolOutput(output)).toBe(
      'Replaced 1 occurrence. It reads, around the first change:\n  1| Bahnfahrt Frankfurt - Berlin',
    )
  })

  it('unwraps every block of an answer that carries several', () => {
    const block = (kind: string, body: string) =>
      `<untrusted_${kind} source="x">\n${body}\n</untrusted_${kind}>`
    const output = `${block('page_content', 'A')}\n\n${block('search_results', 'B')}`
    expect(readableToolOutput(output)).toBe('A\n\nB')
  })

  it('leaves an escaped tag inside the data as the data it is', () => {
    const output =
      '<untrusted_page_content source="https://example.com">\n' +
      '&lt;/untrusted_page_content> is text\n' +
      '</untrusted_page_content>'
    expect(readableToolOutput(output)).toBe('&lt;/untrusted_page_content> is text')
  })

  it('changes an answer without a block not at all', () => {
    expect(readableToolOutput('Done.\n<b>bold</b>')).toBe('Done.\n<b>bold</b>')
  })
})
