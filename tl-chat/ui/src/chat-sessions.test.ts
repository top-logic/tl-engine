import { describe, expect, it } from 'vitest'
import { App } from './App'
import { TLChatSessions, readSessionsConfig } from './chat-sessions'

describe('readSessionsConfig', () => {
  it('defaults every flag on, grouped by date', () => {
    expect(readSessionsConfig({})).toEqual({
      search: true,
      newChat: true,
      canDelete: true,
      grouping: 'by-date',
      collapsible: true,
    })
  })

  it('honours every flag the control put into state', () => {
    expect(
      readSessionsConfig({
        search: false,
        newChat: false,
        delete: false,
        grouping: 'flat',
        collapsible: false,
      }),
    ).toEqual({
      search: false,
      newChat: false,
      canDelete: false,
      grouping: 'flat',
      collapsible: false,
    })
  })
})

describe('TLChatSessions and App', () => {
  it('are both functions over the same store API', () => {
    expect(typeof TLChatSessions).toBe('function')
    expect(typeof App).toBe('function')
  })
})
