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
    // Thinking is a step, so in summary mode the run summary takes over the working line - and
    // says what the model is thinking about rather than only that it works (tl-ai#602).
    expect(html).toContain('run-summary')
    expect(html).toContain(`${labels.thinking}: Erst überlegen`)
    expect(html).toContain('answer is-pending')
  })

  /**
   * A model that thinks for minutes streams its summary paragraph by paragraph. The line follows
   * the newest paragraph, so it moves while the model works, and it stays one short line.
   */
  it('follows the newest paragraph of a long thought, cut to one line', () => {
    const long = 'Now I am weighing the rent against the service charges, '.repeat(3)
    const html = render(
      build([
        ...TURN_STARTED,
        { type: 'APPEND_THINKING', delta: '**Reading the materials**\n\nFirst paragraph.' },
        { type: 'APPEND_THINKING', delta: `\n\n${long}` },
      ]),
      true,
    )
    expect(html).not.toContain('First paragraph.')
    expect(html).toContain(`${labels.thinking}: Now I am weighing the rent`)
    expect(html).toContain('…')
    expect(html).not.toContain(long.trim())
  })

  it('says it is working again once the thought turns into a step', () => {
    const html = render(
      build([
        ...THINKING,
        { type: 'ADD_TOOL', toolUseId: 'toolu_1', name: 'recordStatement', input: {} },
      ]),
      true,
    )
    expect(html).toContain(labels.working)
    expect(html).not.toContain(`${labels.thinking}: Erst überlegen`)
  })

  it('shows no thought once the run is over', () => {
    const html = render(build([...THINKING, { type: 'END_RUN', runId: 'r1' }]), false)
    expect(html).not.toContain(`${labels.thinking}: Erst überlegen`)
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
   * A turn the server restart cut off while it waited for an approval (#598), as the startup sweep
   * leaves it: the user's message and what the agent said stay, the approval is closed rather than
   * offered again, and one notice under the answer says why it stops there - in the reader's
   * language, not the server's English details.
   */
  it('shows a turn the restart cut off, with its approval closed', () => {
    const turns = [
      {
        id: 't1',
        turnNumber: 1,
        timestamp: 1000,
        userParts: [{ id: 'u', type: 'text', timestamp: 1000, textData: { text: 'Leg den Beleg an.' } }],
        assistantParts: [
          { id: 'a', type: 'text', timestamp: 2000, textData: { text: 'Ich lege ihn jetzt an.' } },
          {
            id: 'ap',
            type: 'approval',
            timestamp: 2500,
            approvalData: {
              toolCallId: 'call-1',
              toolName: 'changeData',
              argsJson: '{"script":"new()"}',
              displayReason: 'Changes shared data.',
              mode: 'DATA_WRITE',
              decision: 'INTERRUPTED',
            },
          },
          {
            id: 'i',
            type: 'system',
            timestamp: 2500,
            systemData: { eventType: 'INTERRUPTED', details: 'The server stopped before this run finished.' },
          },
        ],
      },
    ]
    const messages = turnsJsonToMessages(JSON.stringify(turns))
    const approval = messages.find((m) => m.type === 'approval_request')
    expect(approval).toMatchObject({ outcome: 'INTERRUPTED' })
    // Not offered again: the composer asks only what is still pending.
    expect(messages.some((m) => m.type === 'approval_request' && m.outcome === 'pending')).toBe(false)

    const de = bundle('de')
    const html = render(messages, false, 'detailed', 'de')
    expect(html).toContain('Leg den Beleg an.')
    expect(html).toContain('Ich lege ihn jetzt an.')
    expect(html).toContain(de.interrupted)
    expect(html.indexOf(de.interrupted)).toBeGreaterThan(html.indexOf('Ich lege ihn jetzt an.'))
    expect(html).not.toContain('The server stopped before this run finished.')
    expect(html).not.toContain(de.cancelled)
    expect(html).not.toContain('error-card')

    // Not "done in 1 step": the turn stopped, it did not finish.
    const summary = render(messages, false, 'summary', 'de')
    expect(summary).toContain(de.runStepsStopped(1))
    expect(summary).not.toContain(de.runSteps(1))
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

/*
 * A turn that no message of the user started: subagents reported back while the agent was idle, and
 * the agent went on by itself. It stands as its own turn, opened by a line in place of the bubble -
 * live and after a reload alike.
 */
describe('a turn started for subagent news', () => {
  const turns = [
    {
      id: 't1',
      turnNumber: 1,
      timestamp: 1000,
      userParts: [{ id: 'u', type: 'text', timestamp: 1000, textData: { text: 'Recherchiere X im Hintergrund.' } }],
      assistantParts: [{ id: 'a', type: 'text', timestamp: 2000, textData: { text: 'Ein Subagent arbeitet daran.' } }],
    },
    {
      id: 't2',
      turnNumber: 2,
      timestamp: 5000,
      userParts: [],
      assistantParts: [
        {
          id: 's',
          type: 'system',
          timestamp: 5000,
          systemData: { eventType: 'SUBAGENT_RESULT', details: '[Subagent results] sa-1: X is 42.' },
        },
        { id: 'b', type: 'text', timestamp: 6000, textData: { text: 'Der Subagent hat X gefunden: 42.' } },
      ],
    },
  ]

  it('reads back as its own turn, opened by the wake line', () => {
    const messages = turnsJsonToMessages(JSON.stringify(turns))
    expect(messages.filter((m) => m.type === 'user')).toHaveLength(1)
    expect(messages.find((m) => m.type === 'notice')).toMatchObject({ kind: 'lead_woken' })

    const de = bundle('de')
    const html = render(messages, false, 'summary', 'de')
    expect(html).toContain(de.leadWoken)
    expect(html.indexOf(de.leadWoken)).toBeGreaterThan(html.indexOf('Ein Subagent arbeitet daran.'))
    expect(html.indexOf(de.leadWoken)).toBeLessThan(html.indexOf('Der Subagent hat X gefunden: 42.'))
    // The delivery itself is for the model, not for the reader.
    expect(html).not.toContain('X is 42.')
    expect(html.match(/class="run-block"/g)).toHaveLength(2)
  })

  /* Who reported back and why, so the reader knows which card to look at (#721). */
  it('names the subagents and why they reported back, after a reload', () => {
    const named = structuredClone(turns)
    named[1].assistantParts.splice(1, 0, {
      id: 'w',
      type: 'system',
      timestamp: 5000,
      systemData: {
        eventType: 'LEAD_WOKEN',
        details: JSON.stringify([
          { subagentId: 'sa-1', agentName: 'Counter', reason: 'FINISHED', parentToolCallId: 'call-1', question: '' },
          { subagentId: 'sa-2', agentName: 'Writer', reason: 'ASKS', parentToolCallId: 'call-2', question: 'Welche Veranstaltung?' },
          { subagentId: 'sa-3', agentName: 'Lister', reason: 'FAILED', parentToolCallId: 'call-3', question: '' },
        ]),
      },
    })
    const messages = turnsJsonToMessages(JSON.stringify(named))
    expect(messages.find((m) => m.type === 'notice')).toMatchObject({
      kind: 'lead_woken',
      woken: [{ subagentId: 'sa-1', reason: 'FINISHED' }, { subagentId: 'sa-2', reason: 'ASKS' }, { subagentId: 'sa-3', reason: 'FAILED' }],
    })

    const de = bundle('de')
    const text = render(messages, false, 'summary', 'de').replace(/<[^>]+>/g, '')
    expect(text).toContain(`sa-1 (Counter) ${de.wokenFinished}`)
    expect(text).toContain(`sa-2 (Writer) ${de.wokenAsks} Welche Veranstaltung?`)
    expect(text).toContain(`sa-3 (Lister) ${de.wokenFailed}`)
    expect(text).toContain(de.leadWokenContinues)
    expect(text).not.toContain(de.leadWoken)
  })

  /* A subagent stopped at its limit did not finish; the line must not say it is done (#790). */
  it('says a subagent reached its limit rather than that it is done', () => {
    const named = structuredClone(turns)
    named[1].assistantParts.splice(1, 0, {
      id: 'w',
      type: 'system',
      timestamp: 5000,
      systemData: {
        eventType: 'LEAD_WOKEN',
        details: JSON.stringify([
          { subagentId: 'sa-2', agentName: 'Agent', reason: 'LIMIT', parentToolCallId: 'call-2', question: '' },
        ]),
      },
    })
    const messages = turnsJsonToMessages(JSON.stringify(named))
    expect(messages.find((m) => m.type === 'notice')).toMatchObject({ woken: [{ subagentId: 'sa-2', reason: 'LIMIT' }] })

    const german = render(messages, false, 'summary', 'de').replace(/<[^>]+>/g, '')
    expect(german).toContain('sa-2 (Agent) hat sein Limit erreicht')
    expect(german).not.toContain(bundle('de').wokenFinished)
    expect(render(messages, false).replace(/<[^>]+>/g, '')).toContain('sa-2 (Agent) reached its limit')
  })

  it('names them live as well, and says the old sentence for a log without names', () => {
    const live = build([
      { type: 'ADD_USER', id: 'u1', text: 'Recherchiere X im Hintergrund.' },
      {
        type: 'ADD_NOTICE',
        kind: 'lead_woken',
        detail: '',
        woken: [{ subagentId: 'sa-1', agentName: 'Counter', reason: 'CANCELLED', parentToolCallId: 'call-1', question: '' }],
      },
    ])
    expect(render(live, false).replace(/<[^>]+>/g, '')).toContain(`sa-1 (Counter) ${labels.wokenCancelled}`)

    const old = build([{ type: 'ADD_NOTICE', kind: 'lead_woken', detail: '', woken: [] }])
    expect(render(old, false)).toContain(labels.leadWoken)
  })

  it('opens a turn of its own live as well', () => {
    const messages = build([
      { type: 'ADD_USER', id: 'u1', text: 'Recherchiere X im Hintergrund.' },
      { type: 'ADD_ASSISTANT', id: 'a1', text: 'Ein Subagent arbeitet daran.', isStreaming: false },
      { type: 'ADD_NOTICE', kind: 'lead_woken', detail: '' },
      { type: 'ADD_ASSISTANT', id: 'a2', text: 'Der Subagent hat X gefunden: 42.', isStreaming: false },
    ])
    const html = render(messages, false)
    expect(html.match(/class="run-block"/g)).toHaveLength(2)
    expect(html.indexOf(labels.leadWoken)).toBeGreaterThan(html.indexOf('Ein Subagent arbeitet daran.'))
    expect(html.indexOf(labels.leadWoken)).toBeLessThan(html.indexOf('Der Subagent hat X gefunden: 42.'))
  })
})
