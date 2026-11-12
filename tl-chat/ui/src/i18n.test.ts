import { describe, expect, it } from 'vitest'
import { bundle, PROVIDER_ERROR_NO_CREDENTIALS, providerErrorText } from './i18n'

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

describe('the failed-run strip', () => {
  /**
   * #410. Every failure used to arrive as an unknown provider kind, so a write the application
   * refused told the reader the model had erred and to send again - which refuses it again. The
   * server now names an application failure as one, and the strip has to read differently for it.
   */
  it('does not blame the model for a failure of the application', () => {
    for (const language of ['de', 'en'] as const) {
      const labels = bundle(language)
      const application = providerErrorText('APPLICATION_ERROR', language)

      expect(application).toBe(labels.applicationError)
      expect(application).not.toBe(labels.providerError)
    }
  })

  /**
   * A provider without credentials is announced in the application's own words, which name the
   * secret to set - not as a key the provider rejected.
   */
  it('reads the server message for a provider without credentials', () => {
    const message = 'Das Secret tl_mistral_api_key ist nicht hinterlegt.'
    expect(providerErrorText(PROVIDER_ERROR_NO_CREDENTIALS, 'de', `  ${message} `)).toBe(message)
    for (const language of ['de', 'en'] as const) {
      const labels = bundle(language)
      expect(providerErrorText(PROVIDER_ERROR_NO_CREDENTIALS, language, '')).toBe(labels.providerErrorNoCredentials)
      expect(providerErrorText(PROVIDER_ERROR_NO_CREDENTIALS, language)).toBe(labels.providerErrorNoCredentials)
    }
  })

  /** A key the provider rejected keeps its own sentence; the provider's words are only the detail. */
  it('names a rejected key as such', () => {
    for (const language of ['de', 'en'] as const) {
      expect(providerErrorText('AUTHENTICATION', language, 'invalid x-api-key')).toBe(bundle(language).providerErrorAuth)
    }
  })

  /** A kind nobody knows is still the model's report, and still says so. */
  it('falls back to the provider wording for an unknown kind', () => {
    expect(providerErrorText('UNKNOWN', 'en')).toBe(bundle('en').providerError)
    expect(providerErrorText(undefined, 'de')).toBe(bundle('de').providerError)
  })
})
