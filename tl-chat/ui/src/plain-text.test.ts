import { describe, expect, it } from 'vitest'
import { plainText } from './plain-text'

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
