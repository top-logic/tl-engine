import { describe, expect, it } from 'vitest'
import { parseTasksJson, type AgentTask } from './progress-model'
import { composeOutboundText } from './send-prefix'

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
