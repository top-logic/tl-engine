import { describe, expect, it } from 'vitest'
import { toolLabel } from './models'

describe('the name a tool is shown under', () => {
  it('reads an ordinary camel-case id as a sentence', () => {
    expect(toolLabel('model-modification.createModule')).toBe('Create module')
    expect(toolLabel('outlineModel')).toBe('Outline model')
  })

  /**
   * The regression: everything after the first word was lower-cased, so the TL-Script tool
   * introduced itself on the approval card as "Execute tlscript".
   */
  it('leaves a run of capitals alone', () => {
    expect(toolLabel('script.executeTLScript')).toBe('Execute TLScript')
    expect(toolLabel('fetchURL')).toBe('Fetch URL')
    // Intended, and asserted so that nobody "fixes" it: an id that opens with a run of capitals has
    // no boundary to split on that is not a guess - "MCP Connect" and "M C P Connect" are the same
    // guess with different luck - so the id is shown as it is written.
    expect(toolLabel('MCPConnect')).toBe('MCPConnect')
  })

  it('has nothing to say about an empty id', () => {
    expect(toolLabel('')).toBe('')
  })
})
