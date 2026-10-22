import { afterEach, describe, expect, it, vi } from 'vitest'
import { randomId } from './ids'

describe('randomId', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('makes version 4 UUIDs where the page is no secure context', () => {
    // Plain HTTP under a host name other than localhost: no crypto.randomUUID.
    const real = globalThis.crypto
    vi.stubGlobal('crypto', { getRandomValues: (array: Uint8Array) => real.getRandomValues(array) })

    const ids = new Set(Array.from({ length: 100 }, () => randomId()))

    expect(ids.size).toBe(100)
    for (const id of ids) {
      expect(id).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/)
    }
  })
})
