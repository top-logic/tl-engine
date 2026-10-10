// @vitest-environment jsdom
import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { renderToStaticMarkup } from 'react-dom/server'
import { turnsJsonToMessages, type SubagentMessageStatus } from './api'
import { addNote, applyChildEvent } from './chat-store'
import { bundle } from './i18n'
import { cardsForTurn, SubagentCard, type SubagentCardModel } from './subagent-card'
import { showSubagentCard } from './thread'
import type { ChatMessage, SubagentActivity, SubagentRef } from './types'

const labels = bundle('en')

function job(over: Partial<SubagentActivity> = {}): SubagentActivity {
  return {
    subagentId: 'sa-1',
    targetAgentName: 'researcher',
    status: 'RUNNING',
    detail: 'find X',
    totalTokens: 0,
    durationMs: 0,
    hasTranscript: true,
    parentToolCallId: 'call-1',
    task: 'find X',
    ...over,
  }
}

function spawnCall(toolUseId: string): ChatMessage {
  return { type: 'tool_call', id: `${toolUseId}-call`, toolUseId, name: 'spawn_subagent', input: {}, status: 'done', depth: 0, timestamp: 0 }
}

function ref(over: Partial<SubagentRef> = {}): SubagentRef {
  return {
    type: 'subagent_ref',
    id: 'p1',
    subagentId: 'sa-1',
    agentName: 'researcher',
    task: 'find X',
    parentToolCallId: 'call-1',
    childRunId: 'run-1',
    status: 'DONE',
    summary: 'X is 42.',
    timestamp: 0,
    ...over,
  }
}

describe('a running subagent\'s live state', () => {
  it('keeps its newest steps and the end of its text, bounded', () => {
    let live = applyChildEvent(undefined, 'turn_started', { turnNumber: 1 })
    for (let i = 0; i < 30; i++) {
      live = applyChildEvent(live, 'tool_started', { toolCallId: `t${i}`, toolName: 'fetch', argsJson: `{"page":${i}}` })
    }
    live = applyChildEvent(live, 'text_delta', { text: 'x'.repeat(1000) })

    expect(live.steps).toHaveLength(20)
    expect(live.steps[19]).toMatchObject({ toolUseId: 't29', status: 'running' })
    expect(live.text.length).toBeLessThanOrEqual(400)
  })

  it('returns the same object for an event that changes nothing', () => {
    const live = applyChildEvent(undefined, 'turn_started', { turnNumber: 1 })
    expect(applyChildEvent(live, 'thinking_delta', { text: 'hm' })).toBe(live)
  })
})

describe('the cards of a turn', () => {
  it('draws a running job at the call that started it, and only that turn\'s jobs', () => {
    const cards = cardsForTurn([spawnCall('call-1')], [job(), job({ subagentId: 'sa-2', parentToolCallId: 'call-9' })], {}, new Set())

    expect(cards.map((card) => card.subagentId)).toEqual(['sa-1'])
    expect(cards[0]).toMatchObject({ agentName: 'researcher', task: 'find X', status: 'RUNNING' })
  })

  it('says a job waits while a question or an approval it asked is open', () => {
    const cards = cardsForTurn([spawnCall('call-1')], [job()], {}, new Set(['sa-1']))
    expect(cards[0].status).toBe('WAITING')
  })

  it('rebuilds an ended subagent from its recorded part when the server holds no job', () => {
    const cards = cardsForTurn([spawnCall('call-1'), ref()], [], {}, new Set())

    expect(cards).toHaveLength(1)
    expect(cards[0]).toMatchObject({ status: 'DONE', summary: 'X is 42.', childRunId: 'run-1', hasTranscript: true })
  })

  it('does not take a new server life\'s sa-1 for the recorded sa-1 of another call', () => {
    const cards = cardsForTurn([spawnCall('call-1'), ref()], [job({ parentToolCallId: 'call-other' })], {}, new Set())

    expect(cards).toHaveLength(1)
    expect(cards[0]).toMatchObject({ status: 'DONE', childRunId: 'run-1' })
  })

  it('reads the recorded part of a reloaded transcript', () => {
    const messages = turnsJsonToMessages(JSON.stringify([
      {
        id: 't1',
        turnNumber: 1,
        timestamp: 1,
        userParts: [],
        assistantParts: [
          {
            id: 'p1',
            type: 'subagent',
            timestamp: 2,
            subagentData: { subagentId: 'sa-1', agentName: 'researcher', task: 'find X', parentToolCallId: 'call-1', childRunId: 'run-1', status: 'FAILED', summary: 'boom' },
          },
        ],
      },
    ]))

    expect(messages.find((m) => m.type === 'subagent_ref')).toMatchObject({ subagentId: 'sa-1', status: 'FAILED', childRunId: 'run-1' })
  })
})

describe('the card', () => {
  function html(card: Partial<SubagentCardModel>): string {
    const model: SubagentCardModel = {
      subagentId: 'sa-1',
      agentName: 'researcher',
      task: 'find X',
      status: 'RUNNING',
      summary: '',
      totalTokens: 0,
      durationMs: 0,
      hasTranscript: true,
      ...card,
    }
    return renderToStaticMarkup(React.createElement(SubagentCard, { card: model, labels, onLoadTranscript: async () => [] }))
  }

  it('names the subagent, its task, its state and its current step', () => {
    const out = html({
      live: { steps: [{ toolUseId: 't1', name: 'webSearch', summary: '{"q":"X"}', status: 'running' }], text: '', turns: 2, tokens: 1500, startedAt: Date.now() },
    })

    expect(out).toContain('researcher')
    expect(out).toContain('find X')
    expect(out).toContain('running')
    expect(out).toContain('webSearch {&quot;q&quot;:&quot;X&quot;}')
    expect(out).toContain('2 turns')
  })

  it('shows what the subagent wrote as text, never as markup', () => {
    const out = html({ status: 'DONE', summary: '<img src=x onerror=alert(1)>' })
    expect(out).toContain('&lt;img src=x onerror=alert(1)&gt;')
    expect(out).not.toContain('<img')
  })
})

describe('a line pointing at a card', () => {
  /* The woken line names a subagent; its name scrolls to that card, in this transcript only. */
  it('finds the card by its id and starting call, scrolls to it and marks it', () => {
    document.body.innerHTML = `
      <div class="thread">
        <section data-subagent-id="sa-1" data-parent-call="call-old"><button class="subagent-card-head">old</button></section>
        <section data-subagent-id="sa-1" data-parent-call="call-1"><button class="subagent-card-head">new</button></section>
        <p id="line"></p>
      </div>
      <div class="thread"><section data-subagent-id="sa-9" data-parent-call="call-9"></section></div>`
    const scrolled: Element[] = []
    Element.prototype.scrollIntoView = function (this: Element) {
      scrolled.push(this)
    }
    const line = document.getElementById('line')!

    expect(showSubagentCard(line, { subagentId: 'sa-1', parentToolCallId: 'call-1' })).toBe(true)
    const target = document.querySelector('[data-parent-call="call-1"]')!
    expect(scrolled).toEqual([target])
    expect(target.classList.contains('is-highlighted')).toBe(true)
    expect(document.activeElement?.textContent).toBe('new')

    expect(showSubagentCard(line, { subagentId: 'sa-9', parentToolCallId: 'call-9' })).toBe(false)
  })
})

describe('writing to a subagent from its card', () => {
  let mounted: { root: Root; container: HTMLElement } | null = null

  afterEach(() => {
    mounted?.root.unmount()
    mounted?.container.remove()
    mounted = null
  })

  function settle(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 20))
  }

  /** Mounts an open card and answers every message with `answer`. */
  async function openCard(card: Partial<SubagentCardModel>, answer: SubagentMessageStatus | null) {
    const sent: [string, string, string][] = []
    const container = document.createElement('div')
    document.body.append(container)
    const root = createRoot(container)
    mounted = { root, container }
    const model: SubagentCardModel = {
      subagentId: 'sa-1',
      agentName: 'researcher',
      task: 'find X',
      status: 'RUNNING',
      summary: '',
      totalTokens: 0,
      durationMs: 0,
      hasTranscript: true,
      parentToolCallId: 'call-1',
      ...card,
    }
    const onSendMessage =
      answer === null
        ? undefined
        : async (id: string, call: string, text: string) => {
            sent.push([id, call, text])
            return answer
          }
    root.render(React.createElement(SubagentCard, { card: model, labels, onLoadTranscript: async () => [], onSendMessage }))
    await settle()
    ;(container.querySelector('.subagent-card-head') as HTMLElement).click()
    await settle()
    return { container, sent }
  }

  async function write(container: HTMLElement, text: string) {
    const input = container.querySelector('.subagent-card-input') as HTMLInputElement
    const setValue = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value')!.set!
    setValue.call(input, text)
    input.dispatchEvent(new Event('input', { bubbles: true }))
    await settle()
    ;(container.querySelector('.subagent-card-compose button[type=submit]') as HTMLButtonElement).click()
    await settle()
    return input
  }

  /* The message goes to this subagent - named by its id and the call that started it. */
  it('sends to the subagent the card shows and empties the field', async () => {
    const { container, sent } = await openCard({}, 'sent')
    const input = await write(container, '  Only 2026.  ')

    expect(sent).toEqual([['sa-1', 'call-1', 'Only 2026.']])
    expect(input.value).toBe('')
    expect(input.getAttribute('aria-label')).toBe('Message to sa-1 (researcher)')
    expect(container.textContent).toContain(labels.subagentMessageHint)
  })

  /* An ended subagent reads nothing more: the user is told, and keeps what they wrote. */
  it('says so when the subagent has ended meanwhile', async () => {
    const { container } = await openCard({}, 'ended')
    const input = await write(container, 'Only 2026.')

    expect(container.querySelector('[role=alert]')?.textContent).toBe(labels.subagentMessageEnded)
    expect(input.value).toBe('Only 2026.')
  })

  /* Nothing to write to: an ended subagent, or a host that offers no way to send. */
  it('offers no field once the subagent has ended, nor without a way to send', async () => {
    expect((await openCard({ status: 'DONE', summary: 'X is 42.' }, 'sent')).container.querySelector('.subagent-card-input')).toBeNull()
    mounted?.root.unmount()
    mounted?.container.remove()
    expect((await openCard({}, null)).container.querySelector('.subagent-card-input')).toBeNull()
  })

  /* Each message keeps its receipt: sent until the subagent has read it, then read. */
  it('shows each message with its receipt', async () => {
    let live = addNote(undefined, 'm1', 'Only 2026.')
    live = addNote(live, 'm2', 'And by venue.')
    live = applyChildEvent(live, 'user_message_read', { messages: [{ id: 'm1', text: 'Only 2026.' }] })
    const { container } = await openCard({ live }, 'sent')

    const notes = Array.from(container.querySelectorAll('.subagent-card-notes li')).map((li) => li.textContent)
    expect(notes).toEqual([`Only 2026.${labels.subagentMessageRead}`, `And by venue.${labels.subagentMessageSent}`])
  })
})

describe('the receipt of a message', () => {
  /* Another window of the chat sent it: it reads the message from the frame, already read. */
  it('adds a message another window sent, as read', () => {
    const live = applyChildEvent(undefined, 'user_message_read', { messages: [{ id: 'm9', text: 'From the other tab.' }] })
    expect(live.notes).toEqual([{ id: 'm9', text: 'From the other tab.', read: true }])
  })

  it('reads the user\'s message in a subagent\'s transcript as the user\'s own words', () => {
    const messages = turnsJsonToMessages(JSON.stringify([
      {
        id: 't1',
        timestamp: 0,
        userParts: [],
        assistantParts: [
          {
            type: 'system',
            id: 'p1',
            timestamp: 0,
            systemData: { eventType: 'USER_MESSAGE', details: '[Message from the user] The user ... task:\n\nOnly 2026.\n\nAnd by venue.' },
          },
        ],
      },
    ]))
    expect(messages).toContainEqual(expect.objectContaining({ type: 'user', text: 'Only 2026.\n\nAnd by venue.' }))
  })
})

describe('a synchronous delegation', () => {
  function delegateCall(status: 'running' | 'done' = 'running'): ChatMessage {
    return {
      type: 'tool_call', id: 'd-call', toolUseId: 'call-d', name: 'delegate_to_researcher',
      input: { task: 'find X' }, status, depth: 0, timestamp: 0,
    }
  }

  /* Live: its steps stand on its own card at the delegate_to_* call, not among the lead's steps. */
  it('gets a card at its call from what the window saw live', () => {
    let live = applyChildEvent(undefined, 'tool_started', { toolCallId: 't1', toolName: 'webSearch', argsJson: '{"q":"X"}' })
    live = applyChildEvent(live, 'text_delta', { text: 'X is 42.' })

    const running = cardsForTurn([delegateCall()], [], {}, new Set(), { 'call-d': live })
    expect(running).toHaveLength(1)
    expect(running[0]).toMatchObject({ kind: 'delegation', agentName: 'researcher', task: 'find X', status: 'RUNNING', parentToolCallId: 'call-d' })
    expect(running[0].live?.steps.map((step) => step.name)).toEqual(['webSearch'])

    const ended = cardsForTurn([delegateCall('done')], [], {}, new Set(), { 'call-d': { ...live, ended: 'DONE' } })
    expect(ended[0]).toMatchObject({ status: 'DONE', summary: 'X is 42.' })
  })

  /* After a reload: the recorded part gives the card, its steps and how it ended. */
  it('is rebuilt from its recorded part, with its steps', () => {
    const messages = turnsJsonToMessages(JSON.stringify([
      {
        id: 't1',
        timestamp: 0,
        userParts: [],
        assistantParts: [
          { type: 'tool_call', id: 'call-d', timestamp: 0, toolCallData: { toolName: 'delegate_to_researcher', argsJson: '{"task":"find X"}', status: 'DONE', result: 'X is 42.' } },
          {
            type: 'delegation',
            id: 'delegation-1',
            timestamp: 0,
            delegationData: {
              toAgent: 'researcher', inputSummary: 'find X', resultSummary: 'X is 42.', childSessionId: 'run-9',
              durationMs: 1200, turnsUsed: 2, stopReason: 'COMPLETE', parentToolCallId: 'call-d',
              totalUsage: { inputTokens: 100, outputTokens: 50 },
              childParts: [{ type: 'tool_call', id: 'c1', timestamp: 0, toolCallData: { toolName: 'webSearch', argsJson: '{"q":"X"}', status: 'DONE' } }],
            },
          },
        ],
      },
    ]))

    const cards = cardsForTurn(messages, [], {}, new Set())
    expect(cards).toHaveLength(1)
    expect(cards[0]).toMatchObject({
      kind: 'delegation', agentName: 'researcher', status: 'DONE', summary: 'X is 42.', childRunId: 'run-9', hasTranscript: true, totalTokens: 150,
    })
    expect(cards[0].live?.steps).toEqual([{ toolUseId: 'c1', name: 'webSearch', summary: '{"q":"X"}', status: 'done' }])
  })

  /* The lead waits for it: nothing to write to it, and its card says it is a delegation. */
  it('offers no field and names itself a delegation', () => {
    const html = renderToStaticMarkup(React.createElement(SubagentCard, {
      card: {
        subagentId: '', kind: 'delegation', agentName: 'researcher', task: 'find X', status: 'RUNNING', summary: '',
        totalTokens: 0, durationMs: 0, hasTranscript: false, parentToolCallId: 'call-d',
      },
      labels,
      onLoadTranscript: async () => [],
      onSendMessage: async () => 'sent' as const,
    }))
    expect(html).toContain(labels.delegationTag)
    expect(html).not.toContain('subagent-card-input')
  })
})
