import { describe, expect, it } from 'vitest'
import { defaultExtensions, hostRailSections } from './extensions'
import { citationMarkerIndexes } from './citations'
import {
  draftIsEmpty,
  draftToText,
  parseDraft,
  referenceSummary,
  serializeReference,
  withoutContextPrefix,
} from './reference'

describe('defaultExtensions', () => {
  it('leaves App as the shipped chat when the host passes nothing', () => {
    expect(defaultExtensions).toEqual({})
    expect(hostRailSections(undefined)).toEqual([])
    expect(hostRailSections({})).toEqual([])
  })
})

describe('citationMarkerIndexes', () => {
  it('lists unique [n] markers in order of first appearance', () => {
    expect(citationMarkerIndexes('See [2] then [1] then [2].')).toEqual([2, 1])
  })

  it('is empty when the answer cited nothing', () => {
    expect(citationMarkerIndexes('No markers here.')).toEqual([])
  })
})

describe('reference tokens', () => {
  it('serialises a token at its position, with the location when the host set one', () => {
    const token = {
      kind: 'invoice',
      id: 'inv-1',
      label: 'Reise Berlin',
      location: 'Betrag',
      text: '184,90 €',
    }
    expect(serializeReference(token)).toBe(
      '<quote label="Reise Berlin" ref="inv-1" location="Betrag" kind="invoice">184,90 €</quote>',
    )
  })

  it('escapes what would otherwise close the element', () => {
    const token = { kind: '', id: 'a"b', label: '<b> & "c"', location: '', text: 'x < y & z' }
    const wire = serializeReference(token)
    expect(wire).toBe('<quote label="&lt;b&gt; &amp; &quot;c&quot;" ref="a&quot;b">x &lt; y &amp; z</quote>')
    const back = parseDraft(wire)[0]
    expect(back.type).toBe('ref')
    expect(back.type === 'ref' && back.token.label).toBe('<b> & "c"')
    expect(back.type === 'ref' && back.token.text).toBe('x < y & z')
  })

  it('round-trips two references in one sentence, in order', () => {
    const draft =
      'Kürze <quote label="A" ref="1">erster Absatz</quote> und ergänze ' +
      '<quote label="B" ref="2">zweiter Absatz</quote>.'
    const parts = parseDraft(draft)
    expect(parts.map((part) => part.type)).toEqual(['text', 'ref', 'text', 'ref', 'text'])
    expect(parts.filter((p) => p.type === 'ref').map((p) => (p.type === 'ref' ? p.token.id : ''))).toEqual([
      '1',
      '2',
    ])
    expect(draftToText(parts)).toBe(draft)
  })

  it('reads the location back and summarises a token in one line', () => {
    const [part] = parseDraft('<quote label="Plan.md" ref="p" location="L87-93">x</quote>')
    expect(part.type === 'ref' && part.token.location).toBe('L87-93')
    expect(part.type === 'ref' && referenceSummary(part.token)).toBe('Plan.md L87-93')
  })

  it('a draft that is only a reference is not empty, one that is only spaces is', () => {
    expect(draftIsEmpty('<quote label="A" ref="1">text</quote>')).toBe(false)
    expect(draftIsEmpty('   \n ')).toBe(true)
    expect(draftIsEmpty('')).toBe(true)
  })

  it('drops the context prefix the framework adds, and nothing else', () => {
    expect(withoutContextPrefix('<context>p</context>\nHallo <quote label="A" ref="1">x</quote>')).toBe(
      'Hallo <quote label="A" ref="1">x</quote>',
    )
    expect(withoutContextPrefix('Hallo')).toBe('Hallo')
  })
})
