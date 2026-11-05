import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { keepUnsent, takeUnsent } from './unsent'

/**
 * The text of a send the page was reloaded over waits in the tab's `sessionStorage` for the reloaded
 * composer, once per chat (#574).
 */
describe('the message a reload interrupted', () => {
  const globals = globalThis as unknown as Record<string, unknown>
  let windowBefore: unknown

  beforeEach(() => {
    windowBefore = globals.window
    const items = new Map<string, string>()
    globals.window = {
      sessionStorage: {
        getItem: (key: string) => items.get(key) ?? null,
        setItem: (key: string, value: string) => items.set(key, value),
        removeItem: (key: string) => items.delete(key),
      },
    }
  })

  afterEach(() => {
    globals.window = windowBefore
  })

  it('is handed to the reloaded page once', () => {
    keepUnsent('chat', 'Hallo')

    expect(takeUnsent('chat')).toBe('Hallo')
    expect(takeUnsent('chat')).toBeNull()
  })

  it('belongs to its own chat', () => {
    keepUnsent('chat', 'Hallo')

    expect(takeUnsent('dock')).toBeNull()
    expect(takeUnsent('chat')).toBe('Hallo')
  })

  it('keeps nothing for a message of blanks', () => {
    keepUnsent('chat', '   ')

    expect(takeUnsent('chat')).toBeNull()
  })
})
