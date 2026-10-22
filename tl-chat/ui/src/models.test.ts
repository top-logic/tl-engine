import { describe, expect, it } from 'vitest'
import { modelsFor } from './models'
import type { ModelOption } from './types'

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
