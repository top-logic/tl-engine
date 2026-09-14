import { describe, expect, it } from 'vitest'
import { defaultExtensions, hostRailSections } from './extensions'
import { citationMarkerIndexes } from './citations'

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
