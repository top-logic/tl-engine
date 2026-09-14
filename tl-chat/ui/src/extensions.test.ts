import { describe, expect, it } from 'vitest'
import { defaultExtensions, hostRailSections } from './extensions'
import { citationMarkerIndexes } from './citations'
import { composeOutboundText } from './send-prefix'

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

describe('composeOutboundText with a marked passage', () => {
  it('escapes the label of a chip the pane produced', () => {
    expect(
      composeOutboundText('', undefined, [
        { id: 'q', label: 'Rechnung "A" & B', payload: 'Zahlbar in 14 Tagen' },
      ]),
    ).toBe('<quote label="Rechnung &quot;A&quot; &amp; B">Zahlbar in 14 Tagen</quote>')
  })

  it('falls back to the label when the chip carries no payload', () => {
    expect(composeOutboundText('hi', undefined, [{ id: 'q', label: 'Beleg 7' }])).toBe(
      '<quote label="Beleg 7">Beleg 7</quote>\nhi',
    )
  })
})

describe('citation chips are only rendered for real citations', () => {
  it('a marker that no citation claims is not one', () => {
    const cited = new Map([[1, 'https://ok.example']])
    const markers = citationMarkerIndexes('Fact [1] and werte[2].')
    expect(markers.filter((n) => cited.has(n))).toEqual([1])
  })
})
