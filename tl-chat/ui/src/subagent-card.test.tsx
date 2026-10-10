// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { turnsJsonToMessages } from './api'
import { applyChildEvent } from './chat-store'
import { bundle } from './i18n'
import { cardsForTurn, SubagentCard, type SubagentCardModel } from './subagent-card'
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
