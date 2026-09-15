import { describe, expect, it } from 'vitest'
import { modelsFor, toolLabel } from './models'
import type { ModelOption } from './types'

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

describe('the models offered for the selected agent', () => {
  const models: ModelOption[] = [
    { id: 'granted-model', grantedToRole: true },
    { id: 'this-agents-model', grantedToRole: false },
    { id: 'another-agents-model', grantedToRole: false },
  ]

  /**
   * The payload lists what any agent the account may run accepts, because the menu is fetched once
   * and the agent changes without another round trip. Offering another agent's model is offering a
   * turn that comes back 403.
   */
  it('drops a model that only another agent is assigned', () => {
    expect(modelsFor(models, 'this-agents-model').map((m) => m.id)).toEqual([
      'granted-model',
      'this-agents-model',
    ])
  })

  it('keeps everything while nothing says otherwise', () => {
    expect(modelsFor([{ id: 'a' }, { id: 'b' }], undefined).map((m) => m.id)).toEqual(['a', 'b'])
  })

  it('offers only what the roles grant when no agent is selected', () => {
    expect(modelsFor(models, undefined).map((m) => m.id)).toEqual(['granted-model'])
  })
})
