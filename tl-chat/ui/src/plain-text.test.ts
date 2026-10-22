import { describe, expect, it } from 'vitest'
import { argsSummary, plainText } from './plain-text'

describe('a subagent\'s text as a plain line', () => {
  /* What a model writes as a report, shown on a card: the words, without the markers (#769). */
  it('takes out the Markdown markers and keeps the words', () => {
    expect(plainText('## Ergebnis\n- **Gesamt:** 4 Veranstaltungen\n- `demo.events:Event` ist der Typ'))
      .toBe('Ergebnis\nGesamt: 4 Veranstaltungen\ndemo.events:Event ist der Typ')
    expect(plainText('Siehe [das Programm](https://example.com/p) und *bitte* __jetzt__.'))
      .toBe('Siehe das Programm und bitte jetzt.')
    expect(plainText('```\nselect\n```')).toBe('select')
  })

  it('leaves a sentence without markers as it is', () => {
    expect(plainText('3 * 4 = 12, snake_case_name bleibt.')).toBe('3 * 4 = 12, snake_case_name bleibt.')
  })
})

describe('the arguments of a step as a plain line', () => {
  /* What a tool was called with, without the JSON around it (#772). */
  it('shows the values, not the syntax', () => {
    expect(argsSummary('{"tids":["GenericObject:2974","GenericObject:281"]}')).toBe('GenericObject:2974, GenericObject:281')
    expect(argsSummary('{"query":"Veranstaltung","moduleFilter":[],"limit":5,"nested":{"a":1}}')).toBe('Veranstaltung, 5')
    expect(argsSummary('{}')).toBe('')
    expect(argsSummary(undefined)).toBe('')
  })

  it('cuts a long line and keeps what is no JSON object as text', () => {
    expect(argsSummary(JSON.stringify({ text: 'x'.repeat(200) }))).toHaveLength(81)
    expect(argsSummary('not json {')).toBe('not json {')
    expect(argsSummary('"just a string"')).toBe('just a string')
  })
})
