import { describe, expect, it } from 'vitest'
import { bundle } from './i18n'

describe('the composer placeholder', () => {
  /**
   * "/" is reserved for skills (#416).
   *
   * It used to open the agent picker, and the placeholder said so. The picker was always in the "+"
   * menu as well, so nothing was lost by taking it off "/" - but a placeholder that still offers it
   * would promise a key that now types a slash.
   */
  it('offers "@" for a source and nothing for "/"', () => {
    for (const language of ['de', 'en'] as const) {
      const hint = bundle(language).askAnythingWithAt
      expect(hint).toContain('@')
      expect(hint).not.toContain('/')
    }
  })
})
