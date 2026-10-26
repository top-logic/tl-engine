// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { readComposerControls, readThreadConfig } from './chat-conversation'
import { Composer } from './composer'
import {
  ALL_FEATURES,
  gateComposerControls,
  gateThreadConfig,
  readFeatures,
  sameFeatures,
  type ConversationFeatures,
} from './features'
import { bundle } from './i18n'
import { chatReducer, type ChatAction } from './reducer'
import { Thread } from './thread'
import type { ChatMessage } from './types'

/**
 * What the chat offers in a conversation is what its answerer supports: everything for the
 * framework's own agent, what an external backend states for one of its conversations. One features
 * object decides it, with no knowledge in the browser of who answers.
 *
 * A live DOM, because the transcript sanitizes its markdown through DOMPurify, which needs one.
 */

const NONE: ConversationFeatures = {
  regenerate: false,
  editMessages: false,
  agentPicker: false,
  modelPicker: false,
  effortPicker: false,
  mcpPicker: false,
  attachments: false,
  approvals: false,
}

describe('readFeatures', () => {
  it('reads an absent answer as everything', () => {
    expect(readFeatures(undefined)).toEqual(ALL_FEATURES)
    expect(readFeatures(null)).toEqual(ALL_FEATURES)
    expect(readFeatures({})).toEqual(ALL_FEATURES)
  })

  it('counts a function as unsupported only where it says false', () => {
    expect(readFeatures(NONE)).toEqual(NONE)
    expect(readFeatures({ regenerate: false, attachments: true })).toEqual({ ...ALL_FEATURES, regenerate: false })
  })

  it('compares by value', () => {
    expect(sameFeatures(readFeatures({}), ALL_FEATURES)).toBe(true)
    expect(sameFeatures(NONE, ALL_FEATURES)).toBe(false)
  })
})

describe('the gates', () => {
  const configured = readThreadConfig({})
  const controls = readComposerControls({})

  it('leave everything the element configured when everything is supported', () => {
    expect(gateThreadConfig(configured, ALL_FEATURES)).toEqual(configured)
    expect(gateComposerControls(controls, ALL_FEATURES)).toEqual(controls)
  })

  it('take away what the conversation does not support, and nothing else', () => {
    expect(gateThreadConfig(configured, NONE)).toEqual({ ...configured, regenerate: false })
    expect(gateComposerControls(controls, NONE)).toEqual({
      ...controls,
      agentPicker: false,
      modelPicker: false,
      thinking: false,
      mcp: false,
      attachmentsEnabled: false,
      autoApproveSwitch: false,
    })
  })

  it('never switch on what the element switched off', () => {
    const off = readComposerControls({ agentPicker: false, attachments: false })
    const gated = gateComposerControls(off, ALL_FEATURES)
    expect(gated.agentPicker).toBe(false)
    expect(gated.attachmentsEnabled).toBe(false)
    expect(gateThreadConfig(readThreadConfig({ regenerate: false }), ALL_FEATURES).regenerate).toBe(false)
  })
})

function composerProps() {
  return {
    at: { windowId: 'w1', chatId: 'features' },
    agents: [],
    models: [{ id: 'claude-opus-5', label: 'Opus' }],
    agent: 'assistant',
    model: 'claude-opus-5',
    onAgent: () => {},
    onModel: () => {},
    mcpServers: [],
    selectedMcp: [],
    onToggleMcp: () => {},
    traceMode: 'none',
    onTraceMode: () => {},
    effort: '',
    effortLevels: ['low', 'high'],
    onEffort: () => {},
    labels: bundle('en'),
    autoApprove: false,
    onToggleAutoApprove: () => {},
    pendingApprovals: [],
    pendingQuestions: [],
    dispatch: () => {},
    promptImprovementEnabled: false,
    subagents: [],
    onCancelSubagent: async () => {},
    onStopSubagents: async () => {},
    onLoadSubagentTranscript: async () => [],
    busy: false,
    connection: 'open',
    notice: '',
    noticeDetail: '',
    noticeTone: 'info',
    onDismissNotice: () => {},
    onSend: async () => true,
    onStop: () => {},
  }
}

function composer(features: ConversationFeatures): string {
  return renderToStaticMarkup(
    React.createElement(Composer, {
      ...composerProps(),
      ...gateComposerControls(readComposerControls({}), features),
    } as never),
  )
}

function thread(features: ConversationFeatures): string {
  const actions: ChatAction[] = [
    { type: 'ADD_USER', id: 'u1', text: 'Hello?' },
    { type: 'START_RUN', runId: 'r1' },
    { type: 'ADD_ASSISTANT', id: 'a1', text: 'Hello.', isStreaming: false },
    { type: 'END_RUN', runId: 'r1' },
  ]
  let messages: ChatMessage[] = []
  for (const action of actions) messages = chatReducer(messages, action)
  const labels = bundle('en')
  return renderToStaticMarkup(
    React.createElement(Thread, {
      messages,
      traceMode: 'none',
      welcomeName: 'User',
      busy: false,
      regenerating: false,
      feedback: [],
      labels,
      language: 'en',
      effort: '',
      onEditSend: async () => true,
      onRegenerate: () => undefined,
      onRate: () => undefined,
      onOpenRail: () => undefined,
      railCollapsed: true,
      onToggleRail: () => undefined,
      title: 'Title',
      canRegenerate: gateThreadConfig(readThreadConfig({}), features).regenerate,
      canEdit: features.editMessages,
    } as never),
  )
}

describe('the composer of a conversation', () => {
  it('offers files, the model menu and the auto-approve switch where everything is supported', () => {
    const markup = composer(ALL_FEATURES)
    expect(markup).toContain('type="file"')
    expect(markup).toContain('class="model-menu"')
    expect(markup).toContain('approve-switch')
  })

  it('offers none of them where nothing beyond sending is supported', () => {
    const markup = composer(NONE)
    expect(markup).not.toContain('type="file"')
    expect(markup).not.toContain('class="model-menu"')
    expect(markup).not.toContain('approve-switch')
    // Sending stays.
    expect(markup).toContain('token-input')
  })
})

describe('the transcript of a conversation', () => {
  const labels = bundle('en')

  it('offers edit and regenerate where they are supported', () => {
    const markup = thread(ALL_FEATURES)
    expect(markup).toContain(labels.editAndSendAgain)
    expect(markup).toContain(labels.regenerate)
  })

  it('offers neither where they are not', () => {
    const markup = thread(NONE)
    expect(markup).toContain('Hello?')
    expect(markup).not.toContain(labels.editAndSendAgain)
    expect(markup).not.toContain(labels.regenerate)
  })
})
