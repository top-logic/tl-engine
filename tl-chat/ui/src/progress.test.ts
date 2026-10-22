import { describe, expect, it } from 'vitest'
import { parseTasksJson, progressBadge, progressSteps, unseenBadge, type AgentTask } from './progress-model'
import { composeOutboundText } from './send-prefix'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { ProgressSection } from './progress'
import { bundle } from './i18n'

function task(id: string, status: string, subject = 'Discover model', activeForm?: string): AgentTask {
  return { id, status, subject, activeForm }
}

describe('parseTasksJson', () => {
  it('reads the tasks array and drops deleted rows', () => {
    const json = JSON.stringify({
      highWaterMark: 3,
      tasks: [
        task('1', 'completed', 'Discover'),
        task('2', 'deleted', 'Gone'),
        task('3', 'in_progress', 'Build', 'Creating elements'),
      ],
    })
    expect(parseTasksJson(json).map((item) => item.id)).toEqual(['1', '3'])
  })

  it('is empty when the payload is missing or not JSON', () => {
    expect(parseTasksJson(undefined)).toEqual([])
    expect(parseTasksJson('')).toEqual([])
    expect(parseTasksJson('{')).toEqual([])
    expect(parseTasksJson('{"tasks":null}')).toEqual([])
  })
})

describe('progressSteps', () => {
  it('uses subjects, and activeForm while a task is in progress', () => {
    const steps = progressSteps([
      task('1', 'completed', 'Discover existing model'),
      task('2', 'in_progress', 'Create fleet management model', 'Creating model elements'),
      task('3', 'pending', 'Verify created model'),
    ])
    expect(steps.map((s) => s.label)).toEqual([
      'Discover existing model',
      'Creating model elements',
      'Verify created model',
    ])
    expect(steps.map((s) => s.status)).toEqual(['done', 'running', 'waiting'])
  })

  it('counts pending and in-progress rows on the badge', () => {
    expect(
      progressBadge([
        task('1', 'completed'),
        task('2', 'in_progress'),
        task('3', 'pending'),
      ]),
    ).toBe(2)
  })

  it('marks the running step as waiting for approval while one is open', () => {
    const steps = progressSteps(
      [task('1', 'completed', 'Read'), task('2', 'in_progress', 'Write', 'Writing')],
      [{ id: 'a1', toolName: 'createTask' }],
    )
    expect(steps.map((s) => s.status)).toEqual(['done', 'approval'])
    expect(steps[1].tools).toEqual(['createTask'])
  })

  it('reports an approval that arrived before the agent wrote any task', () => {
    const steps = progressSteps([], [{ id: 'a1', toolName: 'createTask' }])
    expect(steps).toEqual([{ id: 'approval', label: '', status: 'approval', tools: ['createTask'] }])
    expect(progressBadge([], [{ id: 'a1', toolName: 'createTask' }])).toBe(1)
  })

  /* #550: the rail names the tool the way the approval card does - its id, set as code. */
  it('names the tools it waits for by their ids, as code', () => {
    const html = renderToStaticMarkup(
      React.createElement(ProgressSection, {
        tasks: [],
        approvals: [
          { id: 'a1', toolName: 'editText' },
          { id: 'a2', toolName: 'remember' },
        ],
        labels: bundle('de'),
      }),
    )
    expect(html).toContain('<code class="tool-id">editText</code>, <code class="tool-id">remember</code>')
    expect(html).toContain('title="editText, remember"')
    expect(html).not.toContain('Edit text')
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
  it('prepends the context prefix before the draft', () => {
    expect(composeOutboundText('hello', 'project X')).toBe('<context>project X</context>\nhello')
  })

  it('returns the draft alone when no context is set', () => {
    expect(composeOutboundText('hello', undefined)).toBe('hello')
  })

  it('leaves an inline reference where the user put it', () => {
    const draft = 'Kürze <quote label="§ 3" ref="inv-1">Zahlbar in 14 Tagen.</quote> und erkläre warum.'
    expect(composeOutboundText(draft, undefined)).toBe(draft)
  })
})
