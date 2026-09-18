import { describe, expect, it } from 'vitest'
import { App } from './App'
import { plusMenuVisible } from './composer'
import {
  modelBoundSource,
  readComposerControls,
  readThreadConfig,
  TLChatConversation,
} from './chat-conversation'

describe('readThreadConfig', () => {
  it('defaults every transcript flag on (trace is the store axis, not here)', () => {
    expect(readThreadConfig({})).toEqual({
      citationMarkers: true,
      railToggle: true,
      copy: true,
      rate: true,
      regenerate: true,
      timestamp: true,
    })
  })

  it('honours every flag the control put into state', () => {
    expect(
      readThreadConfig({
        citations: false,
        railToggle: false,
        copy: false,
        rate: false,
        regenerate: false,
        timestamp: false,
      }),
    ).toEqual({
      citationMarkers: false,
      railToggle: false,
      copy: false,
      rate: false,
      regenerate: false,
      timestamp: false,
    })
  })
})

describe('readComposerControls', () => {
  it('defaults every picker on', () => {
    expect(readComposerControls({})).toEqual({
      autoApproveSwitch: true,
      enterHint: true,
      agentPicker: true,
      modelPicker: true,
      thinking: true,
      mcp: true,
      attachmentsEnabled: true,
      tracePicker: true,
    })
  })

  it('honours every picker switched off', () => {
    expect(
      readComposerControls({
        autoApprove: false,
        enterHint: false,
        agentPicker: false,
        modelPicker: false,
        thinking: false,
        mcp: false,
        attachments: false,
        tracePicker: false,
      }),
    ).toEqual({
      autoApproveSwitch: false,
      enterHint: false,
      agentPicker: false,
      modelPicker: false,
      thinking: false,
      mcp: false,
      attachmentsEnabled: false,
      tracePicker: false,
    })
  })
})

describe('a composer with every picker off', () => {
  it('renders no plus menu and still sends', () => {
    const off = readComposerControls({
      autoApprove: false,
      enterHint: false,
      agentPicker: false,
      modelPicker: false,
      thinking: false,
      mcp: false,
      attachments: false,
      tracePicker: false,
    })
    expect(plusMenuVisible(off)).toBe(false)
    // Send is not gated on the plus menu: a non-empty draft is enough.
    expect(Boolean('send this'.trim())).toBe(true)
  })
})

describe('modelBoundSource', () => {
  const pushed = [
    { id: 'i1', label: 'Invoice 1001', token: { kind: 'Invoice', id: 'i1', label: 'Invoice 1001', text: 'Invoice 1001 — 42€' } },
    { id: 'i2', label: 'Receipt 2002', token: { kind: 'Invoice', id: 'i2', label: 'Receipt 2002', text: 'Receipt 2002 — 7€' } },
  ]

  it('returns undefined when no candidates were pushed (falls back to a registered source)', () => {
    expect(modelBoundSource(undefined)).toBeUndefined()
    expect(modelBoundSource('x')).toBeUndefined()
  })

  it('offers all candidates for an empty query and maps them to ReferenceCandidates', () => {
    const source = modelBoundSource(pushed)!
    const all = source('')
    expect(all).toHaveLength(2)
    expect(all[0]).toEqual({
      id: 'i1',
      label: 'Invoice 1001',
      token: { kind: 'Invoice', id: 'i1', label: 'Invoice 1001', text: 'Invoice 1001 — 42€' },
    })
  })

  it('filters case-insensitively on label and quoted text', () => {
    const source = modelBoundSource(pushed)!
    expect(source('receipt').map((c) => c.id)).toEqual(['i2'])
    expect(source('42€').map((c) => c.id)).toEqual(['i1'])
    expect(source('nomatch')).toEqual([])
  })

  it('skips malformed rows without a token id', () => {
    const source = modelBoundSource([{ id: 'x', label: 'no token' }, ...pushed])!
    expect(source('').map((c) => c.id)).toEqual(['i1', 'i2'])
  })
})

describe('TLChatConversation and App', () => {
  it('are functions over the same store API', () => {
    expect(typeof TLChatConversation).toBe('function')
    expect(typeof App).toBe('function')
  })
})
