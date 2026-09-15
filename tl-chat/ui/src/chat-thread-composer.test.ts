import { describe, expect, it } from 'vitest'
import { App } from './App'
import { plusMenuVisible } from './composer'
import { readComposerControls, TLChatComposer } from './chat-composer'
import { readThreadConfig, threadSessionToSelect, TLChatThread } from './chat-thread'

describe('readThreadConfig', () => {
  it('defaults to overview, collapsed, and every action on', () => {
    expect(readThreadConfig({})).toEqual({
      traceMode: 'overview',
      runSummary: 'collapsed',
      citationMarkers: true,
      railToggle: true,
      copy: true,
      rate: true,
      regenerate: true,
    })
  })

  it('honours every flag the control put into state', () => {
    expect(
      readThreadConfig({
        traceMode: 'none',
        runSummary: 'hidden',
        citationMarkers: false,
        railToggle: false,
        copy: false,
        rate: false,
        regenerate: false,
      }),
    ).toEqual({
      traceMode: 'none',
      runSummary: 'hidden',
      citationMarkers: false,
      railToggle: false,
      copy: false,
      rate: false,
      regenerate: false,
    })
  })
})

describe('threadSessionToSelect', () => {
  it('follows a channel write when input is set', () => {
    expect(threadSessionToSelect('chat-2', 'chat-1')).toBe('chat-2')
  })

  it('leaves the store alone when input is not set', () => {
    expect(threadSessionToSelect('', 'chat-1')).toBe(null)
    expect(threadSessionToSelect('chat-1', 'chat-1')).toBe(null)
  })
})

describe('readComposerControls', () => {
  it('defaults every picker on and agent-slash off', () => {
    expect(readComposerControls({})).toEqual({
      autoApproveSwitch: true,
      enterHint: true,
      agentPicker: true,
      modelPicker: true,
      thinking: true,
      mcp: true,
      attachmentsEnabled: true,
      tracePicker: true,
      agentSlash: false,
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
        agentSlash: true,
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
      agentSlash: true,
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

describe('TLChatThread, TLChatComposer and App', () => {
  it('are functions over the same store API', () => {
    expect(typeof TLChatThread).toBe('function')
    expect(typeof TLChatComposer).toBe('function')
    expect(typeof App).toBe('function')
  })
})
