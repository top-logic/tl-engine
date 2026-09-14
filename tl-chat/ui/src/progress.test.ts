import { describe, expect, it } from 'vitest'
import { progressBadge, progressSteps, unseenBadge } from './progress-model'
import { composeOutboundText } from './send-prefix'
import type { ChatMessage } from './types'

function tool(id: string, status: 'running' | 'done' | 'error', name = 'web-search.webSearch'): ChatMessage {
  return {
    type: 'tool_call',
    id,
    toolUseId: id,
    name,
    input: {},
    status,
    depth: 0,
    timestamp: 1,
  }
}

describe('progressSteps', () => {
  it('reads tools after the last user message', () => {
    const messages: ChatMessage[] = [
      { type: 'user', id: 'u0', text: 'old', timestamp: 1 },
      tool('old', 'done'),
      { type: 'user', id: 'u1', text: 'now', timestamp: 2 },
      tool('a', 'done'),
      tool('b', 'running'),
    ]
    const steps = progressSteps(messages)
    expect(steps.map((s) => s.toolUseId)).toEqual(['a', 'b'])
    expect(steps[0].status).toBe('done')
    expect(steps[1].status).toBe('running')
  })

  it('marks a pending approval as waiting', () => {
    const messages: ChatMessage[] = [
      { type: 'user', id: 'u1', text: 'go', timestamp: 1 },
      tool('t1', 'running', 'model-modification.createModule'),
      {
        type: 'approval_request',
        id: 'a1',
        approvalRequestId: 'a1',
        toolCallId: 't1',
        toolName: 'model-modification.createModule',
        argsJson: '{}',
        displayReason: 'write',
        outcome: 'pending',
        timestamp: 2,
      },
    ]
    expect(progressSteps(messages)[0].status).toBe('waiting')
    expect(progressBadge(messages)).toBe(1)
  })
})

describe('unseenBadge', () => {
  it('is the rise since the rail was last opened', () => {
    expect(unseenBadge(3, 1)).toBe(2)
    expect(unseenBadge(1, 1)).toBe(0)
    expect(unseenBadge(0, 2)).toBe(0)
  })
})

describe('composeOutboundText', () => {
  it('prepends context and chips before the typed text', () => {
    expect(
      composeOutboundText('hello', 'project X', [{ id: 'c1', label: '§01', payload: 'quoted' }]),
    ).toBe('<context>project X</context>\n<quote label="§01">quoted</quote>\nhello')
  })

  it('returns the typed text alone when nothing else is set', () => {
    expect(composeOutboundText('hello', undefined, [])).toBe('hello')
  })
})
