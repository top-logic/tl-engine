import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  chatIdOf,
  chatStore,
  eventBelongsToChat,
  liveChatCount,
  readBootstrap,
  type ChatBootstrap,
} from './chat-store'
import { bundle } from './i18n'
import { unseenBadge } from './progress-model'
import { cardsForTurn } from './subagent-card'
import type { ChatMessage, SubagentRef } from './types'

/**
 * The store's world, without a browser.
 *
 * `chat-store.ts` is deliberately free of the DOM apart from timers and the stream, so the whole
 * surface a test has to stand in for is `window.setTimeout`, `window.location`, `fetch` and
 * `EventSource`. Rendering is not what these tests are about - what a chat element does with the
 * state is `App`'s business, and the regression gate for that is the untouched suites beside this
 * one.
 */

/** Every `EventSource` the store opened, in the order it opened them. */
let streams: FakeEventSource[] = []

/** The `PUT /chat/preferences` bodies the store wrote, in order. */
let preferenceWrites: unknown[] = []

/** The `text` of every `POST /chat/send`, in order - what the model would actually receive. */
let sentTexts: string[] = []

/** The whole body of every `POST /chat/send`, in order. */
let sentBodies: Record<string, unknown>[] = []

/** Every URL the store asked `fetch` for, in order. */
let requested: string[] = []

/** The body of every `POST /chat/cancel`, in order. */
let cancelBodies: Record<string, unknown>[] = []

/** Every `POST /chat/subagents/{id}/message`, in order: its URL and body. */
let subagentMessages: { url: string; body: Record<string, unknown> }[] = []

class FakeEventSource {
  static readonly CLOSED = 2

  readonly url: string

  closed = false

  private readonly handlers = new Map<string, ((event: MessageEvent) => void)[]>()

  onerror: (() => void) | null = null

  readyState = 1

  constructor(url: string) {
    this.url = url
    streams.push(this)
  }

  addEventListener(name: string, handler: EventListener): void {
    const list = this.handlers.get(name) ?? []
    list.push(handler as (event: MessageEvent) => void)
    this.handlers.set(name, list)
  }

  close(): void {
    this.closed = true
    this.readyState = FakeEventSource.CLOSED
  }

  /** Delivers one SSE frame, as the servlet would. */
  emit(name: string, data: unknown = {}): void {
    for (const handler of this.handlers.get(name) ?? []) {
      handler({ data: JSON.stringify(data) } as MessageEvent)
    }
  }
}

const BOOT: ChatBootstrap = {
  windowId: 'w1',
  authenticated: true,
  userName: 'Sandra',
  welcomeName: 'Sandra',
  language: 'de',
  traceMode: 'summary',
  layoutKey: '',
  railOpen: null,
  railOpenDefault: false,
  paneWidth: 0,
  paneWidthDefault: 0,
  badgeSeen: 0,
  sectionChoices: {},
}

/** Lets every scheduled microtask and the pending `await`s of the bootstrap run. */
async function settle(): Promise<void> {
  for (let round = 0; round < 5; round++) {
    await new Promise((resolve) => setTimeout(resolve, 0))
  }
}

beforeEach(() => {
  streams = []
  preferenceWrites = []
  sentTexts = []
  sentBodies = []
  requested = []
  cancelBodies = []
  subagentMessages = []
  const globals = globalThis as unknown as Record<string, unknown>
  globals.window = {
    location: { pathname: '/app/view/chat' },
    setTimeout: (fn: () => void, ms?: number) => setTimeout(fn, ms) as unknown as number,
    clearTimeout: (id: number) => clearTimeout(id),
  }
  globals.EventSource = FakeEventSource
  globals.fetch = vi.fn(async (url: string, init?: { method?: string; body?: string }) => {
    requested.push(url)
    if (init?.method === 'PUT' && url.includes('/chat/preferences')) {
      preferenceWrites.push(JSON.parse(init.body ?? '{}'))
      return { ok: true, status: 200, json: async () => ({}) }
    }
    if (url.includes('/chat/capabilities')) {
      return { ok: true, status: 200, json: async () => ({ agents: [], models: [], mcpServers: [] }) }
    }
    if (/\/chat\/subagents\/[^/]+\/message/.test(url)) {
      subagentMessages.push({ url, body: JSON.parse(init?.body ?? '{}') })
      return { ok: true, status: 200, json: async () => ({ status: 'sent', messageId: 'm1' }) }
    }
    if (url.includes('/chat/cancel')) {
      cancelBodies.push(JSON.parse(init?.body ?? '{}'))
      return { ok: true, status: 200, json: async () => ({ cancelled: true }) }
    }
    if (url.includes('/chat/send')) {
      const body = JSON.parse(init?.body ?? '{}')
      sentBodies.push(body)
      sentTexts.push(body.text)
      return { ok: true, status: 200, json: async () => ({ status: 'processing' }) }
    }
    // A created session, then the load that activates it. The order matters: the id-bearing path
    // is a prefix match of the list path.
    if (/\/chat\/sessions\/[^/?]/.test(url)) {
      return { ok: true, status: 200, json: async () => ({ id: 's1', turnsJson: '' }) }
    }
    if (url.includes('/chat/sessions')) {
      return { ok: true, status: 200, json: async () => ({ sessions: [], id: 's1', title: 'Ein Chat' }) }
    }
    return { ok: true, status: 200, json: async () => ({}) }
  })
})

afterEach(() => {
  expect(liveChatCount()).toBe(0)
})

describe('one store per chat id', () => {
  it('gives two elements on one id the same store and one stream', async () => {
    const first = chatStore('alpha', BOOT)
    const releaseFirst = first.subscribe(() => undefined)
    const second = chatStore('alpha', BOOT)
    expect(second).toBe(first)

    const releaseSecond = second.subscribe(() => undefined)
    first.attach(BOOT)
    second.attach(BOOT)
    await settle()

    expect(streams).toHaveLength(1)
    expect(liveChatCount()).toBe(1)

    releaseFirst()
    releaseSecond()
  })

  it('does not register a store that was never subscribed', () => {
    chatStore('ghost', BOOT)
    expect(liveChatCount()).toBe(0)
  })

  it('gives two ids two stores and two streams', async () => {
    const alpha = chatStore('alpha', BOOT)
    const beta = chatStore('beta', BOOT)
    expect(beta).not.toBe(alpha)

    const releaseAlpha = alpha.subscribe(() => undefined)
    const releaseBeta = beta.subscribe(() => undefined)
    alpha.attach(BOOT)
    beta.attach(BOOT)
    await settle()

    expect(streams).toHaveLength(2)
    expect(liveChatCount()).toBe(2)

    releaseAlpha()
    releaseBeta()
  })

  it('closes the stream when the last element leaves, and not before', async () => {
    const store = chatStore('alpha', BOOT)
    const releaseFirst = store.subscribe(() => undefined)
    const releaseSecond = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    expect(streams).toHaveLength(1)

    releaseFirst()
    expect(streams[0].closed).toBe(false)

    releaseSecond()
    expect(streams[0].closed).toBe(true)
    expect(liveChatCount()).toBe(0)
  })

  /**
   * The elements of one chat are all built in the same commit, before any of them subscribes. The
   * second one must get the store the first one built - otherwise a `<chat-rail/>` beside a
   * `<chat-conversation/>` on one `chat-id` keeps a store of its own, and the two show different
   * conversations with a stream each.
   */
  it('gives two elements built before either subscribes the same store', async () => {
    const conversation = chatStore('alpha', BOOT)
    const rail = chatStore('alpha', BOOT)
    expect(rail).toBe(conversation)

    const releaseConversation = conversation.subscribe(() => undefined)
    const releaseRail = rail.subscribe(() => undefined)
    conversation.attach(BOOT)
    await settle()

    expect(streams).toHaveLength(1)
    expect(liveChatCount()).toBe(1)

    releaseConversation()
    releaseRail()
  })

  /** A rail built in that same commit reaches the transcript's store, not one beside it. */
  it('lets a rail built before the transcript subscribed announce itself to it', () => {
    const conversation = chatStore('alpha', BOOT)
    const rail = chatStore('alpha', BOOT)
    const releaseConversation = conversation.subscribe(() => undefined)
    const releaseRail = rail.subscribe(() => undefined)

    const unregister = rail.actions.registerRail()
    expect(conversation.getState().railMounted).toBe(true)

    unregister()
    expect(conversation.getState().railMounted).toBe(false)

    releaseConversation()
    releaseRail()
  })

  /**
   * The reason a chat with no `chat-id` keys its store by the control id rather than by a fixed
   * name.
   *
   * Consulting's `CopilotRegistry` keeps a *list* of live panels, so one window can show two
   * copilots. Two `App`s that fell back to one key would answer each other's questions into one
   * transcript - silently, and only for the host that has two.
   */
  it('testTwoAppsInOneWindowDoNotShareATranscript', async () => {
    const left = chatIdOf({ windowId: 'w1' }, 'copilot-left')
    const right = chatIdOf({ windowId: 'w1' }, 'copilot-right')
    expect(left).not.toBe(right)
    expect([left, right]).not.toContain('default')

    const leftStore = chatStore(left, BOOT)
    const rightStore = chatStore(right, BOOT)
    const releaseLeft = leftStore.subscribe(() => undefined)
    const releaseRight = rightStore.subscribe(() => undefined)

    leftStore.actions.dispatch({ type: 'ADD_USER', id: 'u1', text: 'links' })
    rightStore.actions.dispatch({ type: 'ADD_USER', id: 'u2', text: 'rechts' })

    expect(leftStore.getState().messages.map((m) => m.id)).toEqual(['u1'])
    expect(rightStore.getState().messages.map((m) => m.id)).toEqual(['u2'])

    await settle()
    releaseLeft()
    releaseRight()
  })

  /**
   * React remounts an effect on the same component in development, so the instance a released store
   * left behind can be handed a new subscriber. It has to come back, stream and all.
   */
  it('reconnects when the same store is subscribed again', async () => {
    const store = chatStore('alpha', BOOT)
    const first = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    expect(streams).toHaveLength(1)

    first()
    expect(streams[0].closed).toBe(true)

    const second = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    expect(streams).toHaveLength(2)
    expect(streams[1].closed).toBe(false)

    second()
    expect(streams[1].closed).toBe(true)
  })

  /** A configured `chat-id` is what later elements use to find each other. */
  it('prefers a configured chat id over the control id', () => {
    expect(chatIdOf({ chatId: 'beratung' }, 'control-7')).toBe('beratung')
    expect(chatIdOf({ chatId: '  ' }, 'control-7')).toBe('control-7')
    expect(chatIdOf({}, 'control-7')).toBe('control-7')
  })

  /**
   * {@code TLChatConversation} and {@code App} both call {@code useChat(chatId, boot)}. Two
   * subscribers on one id are two elements on one conversation: one stream, one session list.
   */
  it('two elements on one chat-id see the same session list', async () => {
    const sessions = chatStore('main', BOOT)
    const releaseSessions = sessions.subscribe(() => undefined)
    const app = chatStore('main', BOOT)
    expect(app).toBe(sessions)

    const releaseApp = app.subscribe(() => undefined)
    sessions.attach(BOOT)
    await settle()

    expect(app.getState().sessions).toBe(sessions.getState().sessions)
    expect(streams).toHaveLength(1)

    releaseSessions()
    releaseApp()
  })
})

describe('the transcript', () => {
  it('is what chatReducer makes of the dispatched actions', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    store.actions.dispatch({ type: 'ADD_USER', id: 'u1', text: 'Wie hoch ist die Rechnung?' })
    store.actions.dispatch({ type: 'ADD_ASSISTANT', id: 'a1', text: '', isStreaming: true })
    store.actions.dispatch({ type: 'APPEND_TEXT', id: 'a1', delta: 'Sie liegt bei ' })
    store.actions.dispatch({ type: 'APPEND_TEXT', id: 'a1', delta: '1.240 €.' })
    store.actions.dispatch({ type: 'FINALIZE_TEXT', id: 'a1' })

    const messages = store.getState().messages
    expect(messages.map((m) => m.type)).toEqual(['user', 'assistant_text'])
    expect(messages[1]).toMatchObject({ text: 'Sie liegt bei 1.240 €.', isStreaming: false })

    await settle()
    release()
  })

  it('tells its subscribers once per change', async () => {
    const store = chatStore('alpha', BOOT)
    const seen = vi.fn()
    const release = store.subscribe(seen)

    store.actions.dispatch({ type: 'ADD_USER', id: 'u1', text: 'eins' })
    expect(seen).toHaveBeenCalledTimes(1)
    // START_RUN is idempotent per run id, so the second one changes nothing and says nothing.
    store.actions.dispatch({ type: 'START_RUN', runId: 'r1' })
    store.actions.dispatch({ type: 'START_RUN', runId: 'r1' })
    expect(seen).toHaveBeenCalledTimes(2)

    await settle()
    release()
  })
})

/**
 * Pietro's own turn counter, copied from
 * `Consulting@crew/pietro-adopt-chat:ui-modules/src/modules/consulting-chat.tsx`.
 *
 * It is the reason this matters: the host does not read the transcript for display, it counts it,
 * and an extra message shifts every number it writes into the prompt.
 */
function turnCounter(seen: number[]) {
  return (ctx: { messages: ChatMessage[] }) => {
    const turn = ctx.messages.filter((message) => message.type === 'user').length + 1
    seen.push(turn)
    return `turn: ${turn}`
  }
}

describe('the host\'s context prefix', () => {
  /**
   * The prefix describes the conversation the message is being added to, not the one it is already
   * in.
   *
   * `App` got this for free: it handed `contextTag` a ref assigned during render, and a `useReducer`
   * dispatch in the same handler cannot have updated it. The store's dispatch *does* update its
   * state at once, so composing the outbound text after it handed the host the outgoing message -
   * and Pietro, which counts user messages, wrote turn 2 into the first message of a fresh chat.
   */
  it('contextTag sees the transcript without the outgoing message', async () => {
    const fresh = chatStore('alpha', BOOT)
    const releaseFresh = fresh.subscribe(() => undefined)
    const firstTurn: number[] = []

    await fresh.actions.send('allererste Frage', [], { contextTag: turnCounter(firstTurn) })

    expect(firstTurn).toEqual([1])
    expect(sentTexts[0]).toBe('<context>turn: 1</context>\nallererste Frage')

    await settle()
    releaseFresh()

    const ongoing = chatStore('beta', BOOT)
    const releaseOngoing = ongoing.subscribe(() => undefined)
    // One completed exchange already in the transcript.
    ongoing.actions.dispatch({ type: 'ADD_USER', id: 'u1', text: 'erste Frage' })
    ongoing.actions.dispatch({ type: 'ADD_ASSISTANT', id: 'a1', text: 'Antwort', isStreaming: false })
    const secondTurn: number[] = []

    await ongoing.actions.send('zweite Frage', [], { contextTag: turnCounter(secondTurn) })

    expect(secondTurn).toEqual([2])
    expect(sentTexts[1]).toBe('<context>turn: 2</context>\nzweite Frage')

    await settle()
    releaseOngoing()
  })

  it('sends the draft unchanged when the host contributes no prefix', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    await store.actions.send('ohne Prefix', [])

    expect(sentTexts).toEqual(['ohne Prefix'])

    await settle()
    release()
  })
})

/**
 * What a page states about the situation travels beside the message, never inside it (#437).
 *
 * The facts are computed on the server, out of the chat element's `<context>`; the store only
 * carries them back with the send, so the harness can render the stable ones into the system prompt.
 */
describe('context facts', () => {
  it('carries the facts with the message and leaves the text alone', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    const facts = [
      { name: 'Project', value: 'Airport expansion', stable: true },
      { name: 'Open invoice', value: 'none', stable: false },
    ]

    store.actions.setContextFacts(facts)
    await store.actions.send('Was steht an?', [])

    expect(sentTexts).toEqual(['Was steht an?'])
    expect(sentBodies[0].facts).toEqual(facts)

    await settle()
    release()
  })

  it('sends no facts field when the page states nothing', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    await store.actions.send('Was steht an?', [])

    expect(sentBodies[0].facts).toBeUndefined()

    await settle()
    release()
  })

  it('ignores a repeated set, so an unchanged page does not re-render the chat', async () => {
    const store = chatStore('alpha', BOOT)
    let renders = 0
    const release = store.subscribe(() => {
      renders += 1
    })
    const facts = [{ name: 'Project', value: 'Airport expansion', stable: true }]

    store.actions.setContextFacts(facts)
    const afterFirst = renders
    store.actions.setContextFacts([{ name: 'Project', value: 'Airport expansion', stable: true }])

    expect(renders).toBe(afterFirst)
    expect(store.getState().contextFacts).toEqual(facts)

    await settle()
    release()
  })
})

describe('the stream', () => {
  /*
   * A background subagent asks while its lead may be resting (#695). The card has to say whose
   * question it is, so the store keeps who asks - from the question frame, and for an approval from
   * the subagent id of the delegation frame, named through the subagent list.
   */
  it('keeps which subagent asks a question or an approval', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('subagent_started', { subagentId: 'sa-2', targetAgentName: 'researcher' })
    stream.emit('user_interaction', {
      requestId: 'interaction-x-1',
      question: 'Which region?',
      type: 'text',
      askedBySubagentId: 'sa-2',
      askedByAgent: 'researcher',
    })
    stream.emit('delegation_event', {
      subagentId: 'sa-2',
      eventType: 'approval_required',
      eventData: JSON.stringify({ approvalRequestId: 'ap-1', toolName: 'createObject', argsJson: '{}' }),
    })
    stream.emit('user_interaction', { requestId: 'interaction-y-1', question: 'Main agent asks', type: 'text' })

    const messages = store.getState().messages
    expect(messages.find((m) => m.id === 'interaction-x-1')).toMatchObject({
      askedBy: { subagentId: 'sa-2', agent: 'researcher' },
    })
    expect(messages.find((m) => m.id === 'ap-1')).toMatchObject({
      askedBy: { subagentId: 'sa-2', agent: 'researcher' },
    })
    expect(messages.find((m) => m.id === 'interaction-y-1')).not.toHaveProperty('askedBy.subagentId')

    await settle()
    release()
  })

  /*
   * Two subagents at work: each one's steps and text go to its own card, none into the lead's
   * transcript, where they used to be drawn flat and nameless (#702). Its approval still reaches the
   * prompt stack.
   */
  it('keeps each subagent\'s steps and text apart from the transcript', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]
    const child = (subagentId: string, eventType: string, data: unknown) =>
      stream.emit('delegation_event', { subagentId, eventType, eventData: JSON.stringify(data) })

    stream.emit('subagent_started', { subagentId: 'sa-1', targetAgentName: 'researcher', detail: 'find X', parentToolCallId: 'call-1' })
    stream.emit('subagent_started', { subagentId: 'sa-2', targetAgentName: 'writer', detail: 'draft Y', parentToolCallId: 'call-2' })
    child('sa-1', 'turn_started', { turnNumber: 1 })
    child('sa-1', 'text_delta', { text: 'Searching first.' })
    child('sa-1', 'tool_started', { toolCallId: 't1', toolName: 'webSearch', argsJson: '{"q":"X"}' })
    child('sa-2', 'tool_started', { toolCallId: 't2', toolName: 'query', argsJson: '{}' })
    child('sa-2', 'tool_completed', { toolCallId: 't2', toolName: 'query' })
    child('sa-2', 'approval_required', { approvalRequestId: 'ap-1', toolName: 'createObject', argsJson: '{}' })

    const state = store.getState()
    expect(state.subagents.find((s) => s.subagentId === 'sa-1')).toMatchObject({ parentToolCallId: 'call-1', task: 'find X' })
    expect(state.subagentLive['sa-1'].steps).toEqual([
      { toolUseId: 't1', name: 'webSearch', summary: 'X', status: 'running' },
    ])
    expect(state.subagentLive['sa-1'].turns).toBe(1)
    expect(state.subagentLive['sa-2'].steps).toEqual([
      { toolUseId: 't2', name: 'query', summary: '', status: 'done' },
    ])
    expect(state.messages.some((m) => m.type === 'tool_call')).toBe(false)
    expect(state.messages.find((m) => m.id === 'ap-1')).toMatchObject({ askedBy: { subagentId: 'sa-2', agent: 'writer' } })

    // The end frame names no agent; the card keeps the name it had.
    stream.emit('subagent_completed', { subagentId: 'sa-1', targetAgentName: '', detail: 'X is 42.' })
    expect(store.getState().subagents.find((s) => s.subagentId === 'sa-1')).toMatchObject({
      targetAgentName: 'researcher',
      status: 'DONE',
      task: 'find X',
    })

    await settle()
    release()
  })

  /*
   * A window that opened the chat while the subagent was at work never saw it start: its card stands
   * on the recorded part. The end frame says which call started it, so that card ends too, and its
   * transcript opens by the run the frame names.
   */
  it('ends the card of a subagent this window did not see start', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('subagent_completed', {
      subagentId: 'sa-1',
      targetAgentName: '',
      detail: 'X is 42.',
      parentToolCallId: 'call-1',
      childRunId: 'run-1',
    })

    const running: SubagentRef = {
      type: 'subagent_ref', id: 'p1', subagentId: 'sa-1', agentName: 'researcher', task: 'find X',
      parentToolCallId: 'call-1', childRunId: '', status: 'RUNNING', summary: '', timestamp: 0,
    }
    const spawn: ChatMessage = {
      type: 'tool_call', id: 'c1', toolUseId: 'call-1', name: 'spawn_subagent', input: {}, status: 'done', depth: 0, timestamp: 0,
    }
    const cards = cardsForTurn([spawn, running], store.getState().subagents, {}, new Set())
    expect(cards).toHaveLength(1)
    expect(cards[0]).toMatchObject({ status: 'DONE', summary: 'X is 42.', task: 'find X', childRunId: 'run-1' })

    await settle()
    release()
  })

  /*
   * The user writes to a subagent from its card: the message goes to that subagent of this chat, the
   * card shows it as sent, and as read once the subagent's frame says it read it.
   */
  it('sends a message to one subagent and marks it read when the subagent has read it', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]
    stream.emit('subagent_started', { subagentId: 'sa-1', targetAgentName: 'researcher', detail: 'find X', parentToolCallId: 'call-1' })

    expect(await store.actions.messageSubagent('sa-1', 'call-1', 'Only 2026.')).toBe('sent')

    expect(subagentMessages).toHaveLength(1)
    expect(subagentMessages[0].url).toContain('/chat/subagents/sa-1/message')
    expect(subagentMessages[0].body).toMatchObject({ parentToolCallId: 'call-1', text: 'Only 2026.' })
    expect(store.getState().subagentLive['sa-1'].notes).toEqual([{ id: 'm1', text: 'Only 2026.', read: false }])

    stream.emit('delegation_event', {
      subagentId: 'sa-1',
      eventType: 'user_message_read',
      eventData: JSON.stringify({ messages: [{ id: 'm1', text: 'Only 2026.' }] }),
    })
    expect(store.getState().subagentLive['sa-1'].notes).toEqual([{ id: 'm1', text: 'Only 2026.', read: true }])

    await settle()
    release()
  })

  /*
   * A synchronous delegation's steps go to its card at the lead's delegate_to_* call; the lead's
   * transcript keeps only the lead's own steps (#703).
   */
  it('keeps a synchronous delegation\'s steps out of the lead\'s transcript', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]
    const before = store.getState().messages.length

    const frame = (eventType: string, data: unknown) =>
      stream.emit('delegation_event', { subagentId: '', delegationToolCallId: 'call-d', eventType, eventData: JSON.stringify(data) })
    frame('tool_started', { toolCallId: 't1', toolName: 'webSearch', argsJson: '{"q":"X"}' })
    frame('tool_completed', { toolCallId: 't1', toolName: 'webSearch', result: 'r', durationMs: 5 })
    frame('run_completed', { runId: 'child', reason: 'COMPLETE' })

    expect(store.getState().messages).toHaveLength(before)
    const live = store.getState().delegationLive['call-d']
    expect(live.steps).toEqual([{ toolUseId: 't1', name: 'webSearch', summary: 'X', status: 'done' }])
    expect(live.ended).toBe('DONE')

    await settle()
    release()
  })

  /* The end frame names the stop reason: a stop at a limit reads as such, not as done (#786). */
  it('ends a subagent stopped at its limit as such', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]
    stream.emit('subagent_started', { subagentId: 'sa-1', targetAgentName: 'researcher', detail: 'find X', parentToolCallId: 'call-1' })
    stream.emit('subagent_started', { subagentId: 'sa-2', targetAgentName: 'researcher', detail: 'find Y', parentToolCallId: 'call-2' })

    stream.emit('subagent_completed', { subagentId: 'sa-1', detail: 'X is 4..', stopReason: 'MAX_TOOL_CALLS' })
    stream.emit('subagent_completed', { subagentId: 'sa-2', detail: 'Y is 7.', stopReason: 'COMPLETE' })

    expect(store.getState().subagents.map((item) => item.status)).toEqual(['LIMIT', 'DONE'])
    await settle()
    release()
  })

  /* A subagent beyond the cap waits; its card says so until its first own step arrives. */
  it('shows a queued subagent as waiting until it starts', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('subagent_started', { subagentId: 'sa-5', targetAgentName: 'researcher', detail: 'find X', queued: true })
    expect(store.getState().subagents.find((s) => s.subagentId === 'sa-5')).toMatchObject({ status: 'QUEUED', task: 'find X' })

    stream.emit('delegation_event', { subagentId: 'sa-5', eventType: 'turn_started', eventData: '{"turnNumber":1}' })
    expect(store.getState().subagents.find((s) => s.subagentId === 'sa-5')).toMatchObject({
      status: 'RUNNING',
      targetAgentName: 'researcher',
      task: 'find X',
    })

    await settle()
    release()
  })

  /*
   * Subagents reported back while the agent was idle, and the server started a turn for them: the
   * line opens that turn, ahead of its run; when no more are started, the strip says so.
   */
  it('opens a turn started for subagent news with the wake line', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('lead_woken', {
      finished: 1,
      asking: 0,
      subagents: [{ subagentId: 'sa-1', agentName: 'researcher', reason: 'FINISHED', parentToolCallId: 'call-1', question: '' }],
    })
    stream.emit('run_started', { runId: 'r9' })
    stream.emit('turn_started', {})
    stream.emit('message_complete', { contentText: 'Der Subagent hat X gefunden.' })

    const messages = store.getState().messages
    const woken = messages.findIndex((m) => m.type === 'notice' && m.kind === 'lead_woken')
    expect(woken).toBeGreaterThanOrEqual(0)
    expect(woken).toBeLessThan(messages.findIndex((m) => m.type === 'assistant_text'))
    expect(messages[woken]).toMatchObject({ woken: [{ subagentId: 'sa-1', reason: 'FINISHED', parentToolCallId: 'call-1' }] })
    expect(store.getState().busy).toBe(false)

    stream.emit('lead_wakes_exhausted', { wakes: 5 })
    expect(store.getState().notice).toBe(bundle(BOOT.language).leadWakesExhausted)

    await settle()
    release()
  })

  it('drives the run state and the transcript', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('run_started', { runId: 'r1' })
    expect(store.getState().busy).toBe(true)
    expect(store.getState().connection).toBe('live')

    stream.emit('turn_started', {})
    stream.emit('text_delta', { text: 'Teil eins' })
    stream.emit('message_complete', { contentText: 'Teil eins und zwei' })

    const answer = store.getState().messages.find((m) => m.type === 'assistant_text')
    expect(answer).toMatchObject({ text: 'Teil eins und zwei', isStreaming: false })
    expect(store.getState().busy).toBe(false)

    await settle()
    release()
  })

  /**
   * `lastAssistantId` outliving the run.
   *
   * The frames arrive as `text_delta …, run_completed, message_complete`, so the authoritative text
   * lands after the run's end has already cleared the id it streamed into.
   */
  it('applies the authoritative text that arrives after the run ended', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('run_started', { runId: 'r1' })
    stream.emit('turn_started', {})
    stream.emit('text_delta', { text: 'Die Hauptstadt von Portugal ist L' })
    stream.emit('run_completed', {})
    stream.emit('message_complete', { contentText: 'Die Hauptstadt von Portugal ist Lissabon.' })

    const answer = store.getState().messages.find((m) => m.type === 'assistant_text')
    expect(answer).toMatchObject({ text: 'Die Hauptstadt von Portugal ist Lissabon.' })

    await settle()
    release()
  })

  /**
   * The sticky notice surviving exactly one run start.
   *
   * "3 spätere Antworten entfernt" is written by the edit whose send starts the next run; a run
   * start that cleared the strip unconditionally wiped it in the same instant.
   */
  it('keeps a sticky notice over one run start and no further one', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    store.actions.showNotice('3 spätere Antworten entfernt', '', { tone: 'info', sticky: true })
    stream.emit('run_started', { runId: 'r1' })
    expect(store.getState().notice).toBe('3 spätere Antworten entfernt')

    stream.emit('run_started', { runId: 'r2' })
    expect(store.getState().notice).toBe('')

    await settle()
    release()
  })

  /**
   * A failed run leaving its mark in the transcript (#429).
   *
   * The notice strip is the composer's and the next send clears it; the thread is the record of the
   * turn. Reported only in the strip, a turn that failed before any text left the question standing
   * alone - the same blank thread the ticket is about.
   */
  it('writes a provider error into the transcript, not only into the notice strip', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('run_started', { runId: 'r1' })
    stream.emit('turn_started', {})
    stream.emit('provider_error', { kind: 'OVERLOADED', errorMessage: 'upstream 529' })

    // Recorded the way a re-opened session records it: the provider's own words and the kind. The
    // sentence the user reads is derived from the kind when the thread renders it.
    const error = store.getState().messages.find((m) => m.type === 'error')
    expect(error).toBeDefined()
    expect(error && 'message' in error ? error.message : '').toBe('upstream 529')
    expect(error && 'code' in error ? error.code : '').toBe('OVERLOADED')
    expect(store.getState().notice).not.toBe('')
    expect(store.getState().busy).toBe(false)

    await settle()
    release()
  })

  /**
   * A run that waits for a rate-limited model says so while it waits (#678), and stops saying it
   * once the model answers. Without the line the chat sat frozen for as long as the wait.
   */
  it('says that the agent waits for a busy model, until the model answers', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('run_started', { runId: 'r1' })
    stream.emit('turn_started', {})
    stream.emit('retry_scheduled', { kind: 'RATE_LIMITED', attempt: 3, waitMs: 20000 })

    expect(store.getState().notice).toContain('20 s')
    expect(store.getState().noticeTone).toBe('info')
    expect(store.getState().busy).toBe(true)

    stream.emit('text_delta', { text: 'Here', end: 4 })

    expect(store.getState().notice).toBe('')

    await settle()
    release()
  })

  /**
   * A run that lost its model continued somewhere else, and the thread says so where the answer
   * is. A notice, not an error: nothing failed for the user (#344).
   */
  it('writes a model switch into the transcript as a notice', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('run_started', { runId: 'r1' })
    stream.emit('turn_started', {})
    stream.emit('model_switched', {
      fromModel: 'claude-sonnet-5',
      toModel: 'gpt-5',
      reason: 'OVERLOADED: upstream 529',
    })

    const notice = store.getState().messages.find((m) => m.type === 'notice')
    expect(notice).toBeDefined()
    // The server's own line, character for character, so the notice reads the same live as it
    // does after a reload.
    expect(notice && 'detail' in notice ? notice.detail : '').toBe(
      'claude-sonnet-5 -> gpt-5: OVERLOADED: upstream 529',
    )
    expect(store.getState().messages.some((m) => m.type === 'error')).toBe(false)

    await settle()
    release()
  })

  /**
   * The turn wrote something and none of it was kept (#410). Nobody else tells the user: the model
   * is told in a tool result and may still answer "done", and the answer is all the chat shows.
   */
  it('writes a rolled-back transaction into the transcript as a notice', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('run_started', { runId: 'r1' })
    stream.emit('turn_started', {})
    stream.emit('tx_began', {})
    stream.emit('tx_rollback', { reason: 'Pre-commit security check failed' })

    const notices = store.getState().messages.filter((m) => m.type === 'notice')
    expect(notices).toHaveLength(1)
    // The server's own line, character for character - what the recorded transaction part carries,
    // so the notice reads the same live as after a reload.
    expect(notices[0] && 'detail' in notices[0] ? notices[0].detail : '').toBe(
      'Pre-commit security check failed',
    )
    expect(notices[0] && 'kind' in notices[0] ? notices[0].kind : '').toBe('transaction_rolled_back')
    // The run itself did not fail; an error card would say it did.
    expect(store.getState().messages.some((m) => m.type === 'error')).toBe(false)

    await settle()
    release()
  })

  /** A committed turn is the normal course of things and says nothing. */
  it('writes no notice for a transaction that committed', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('run_started', { runId: 'r1' })
    stream.emit('turn_started', {})
    stream.emit('tx_began', {})
    stream.emit('tx_committed', { revisionNumber: 42 })

    expect(store.getState().messages.some((m) => m.type === 'notice')).toBe(false)

    await settle()
    release()
  })

  /**
   * A run the user stopped is not a failure (#549).
   *
   * The frames in the order the server sends them for a cancel mid-answer: what was streamed, the
   * turn's end, `run_completed` with `ABORTED`, and the authoritative text of what was streamed.
   * The server sends no `provider_error` for it any more; the stop says so once, as a notice under
   * the partial answer, and not a second time in the composer's strip.
   */
  it('writes a cancel into the transcript as one notice, not as an error', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    const stream = streams[0]

    stream.emit('run_started', { runId: 'r1' })
    stream.emit('turn_started', {})
    stream.emit('text_delta', { text: 'Eins, zwei, ' })
    stream.emit('turn_completed', {})
    stream.emit('run_completed', { reason: 'ABORTED' })
    stream.emit('message_complete', { contentText: 'Eins, zwei, ' })

    const messages = store.getState().messages
    expect(messages.some((m) => m.type === 'error')).toBe(false)
    const notices = messages.filter((m) => m.type === 'notice')
    expect(notices).toHaveLength(1)
    expect(notices[0]).toMatchObject({ kind: 'cancelled', detail: '' })
    // Under what was streamed before the stop, which stays.
    const answer = messages.findIndex((m) => m.type === 'assistant_text' && m.text === 'Eins, zwei, ')
    expect(answer).toBeGreaterThanOrEqual(0)
    expect(messages.indexOf(notices[0])).toBeGreaterThan(answer)
    expect(store.getState().notice).toBe('')
    expect(store.getState().busy).toBe(false)

    await settle()
    release()
  })
})

describe('the layout preferences', () => {
  /**
   * E.4: exactly one writer, and one write per change.
   *
   * Opening the rail on a section is three changes at once - the pane closes, the rail opens, the
   * badges count as seen. React batched those into the single effect run that produced one `PUT`;
   * the store has to batch them too, or several elements on one chat would hammer one
   * `PersonalConfiguration` key.
   */
  it('writes once for a handler that changes three of them', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    // Booting writes nothing: the bootstrap IS what the server holds.
    expect(preferenceWrites).toHaveLength(0)

    store.actions.showSection('progress', 4)
    await settle()

    expect(preferenceWrites).toHaveLength(1)
    expect(preferenceWrites[0]).toEqual({
      railOpen: true,
      paneWidth: 0,
      badgeSeen: 4,
      // Opening the rail on a section expands that section, and that is a decision too.
      sections: { progress: true },
    })

    release()
  })

  /**
   * A reload must not persist what the page started with.
   *
   * The engine re-renders the control tree the window already holds, so before #423 a reloaded page
   * booted from the first render's layout and its own bootstrap write made that stale value the
   * stored one - the user's choice was overwritten by the page that was supposed to restore it.
   */
  it('does not write back the layout it booted with', async () => {
    const store = chatStore('alpha', { ...BOOT, railOpen: true, paneWidth: 480, badgeSeen: 3 })
    const release = store.subscribe(() => undefined)
    store.attach({ ...BOOT, railOpen: true, paneWidth: 480, badgeSeen: 3 })
    await settle()

    expect(preferenceWrites).toHaveLength(0)
    expect(store.getState().sideOpen).toBe(true)

    release()
  })

  it('does not write for a session that is not signed in', async () => {
    const store = chatStore('alpha', { ...BOOT, authenticated: false })
    const release = store.subscribe(() => undefined)
    store.attach({ ...BOOT, authenticated: false })
    await settle()

    expect(preferenceWrites).toHaveLength(0)
    // Nor does such a session open a stream: an EventSource against a 401 reconnects forever.
    expect(streams).toHaveLength(0)

    release()
  })
})

describe('a mounted rail', () => {
  /**
   * The transcript's badge button is derived from this flag, so the flag is what decides whether a
   * page without a `<chat-rail/>` shows a button that toggles nothing.
   */
  it('is not announced until a rail registers, and is withdrawn when it leaves', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    expect(store.getState().railMounted).toBe(false)

    const unregister = store.actions.registerRail()
    expect(store.getState().railMounted).toBe(true)

    unregister()
    expect(store.getState().railMounted).toBe(false)

    release()
  })

  /** A rail that mounts after the conversation must still flip the flag, so subscribers re-render. */
  it('notifies the subscribers already listening', () => {
    const store = chatStore('alpha', BOOT)
    let notifications = 0
    const release = store.subscribe(() => {
      notifications += 1
    })

    const unregister = store.actions.registerRail()
    expect(notifications).toBe(1)
    expect(store.getState().railMounted).toBe(true)

    unregister()
    release()
  })

  /** Counted, not flagged: a remount during development leaves the flag set once, not stuck off. */
  it('stays announced while a second rail is still mounted', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    const first = store.actions.registerRail()
    const second = store.actions.registerRail()
    first()
    expect(store.getState().railMounted).toBe(true)

    second()
    expect(store.getState().railMounted).toBe(false)

    release()
  })

  /**
   * The chat quiescing between the two halves must not leave the count below zero.
   *
   * A store whose last element left is put back as declared and can be handed new subscribers, and
   * the rail's own unregister still runs somewhere in that teardown. A count that went negative
   * there would answer "no rail" to the next rail that mounts.
   */
  it('survives a quiesce between a rail registering and unregistering', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    const unregister = store.actions.registerRail()

    release()
    unregister()

    const again = store.subscribe(() => undefined)
    store.actions.registerRail()
    expect(store.getState().railMounted).toBe(true)

    again()
  })

  /**
   * The badge the transcript's toggle shows is the rail's sum, and opening the rail records it.
   *
   * Passing 0 - which is what the toggle did before the rail published a total - reset `badgeSeen`
   * in the user's preferences on every open, so the next unseen count was measured against nothing.
   */
  it('publishes the badge total the toggle shows and records', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.actions.registerRail()

    store.actions.publishBadgeTotal(3)
    expect(store.getState().badgeTotal).toBe(3)
    expect(unseenBadge(store.getState().badgeTotal, store.getState().badgeSeen)).toBe(3)

    store.actions.toggleSide(store.getState().badgeTotal)
    expect(store.getState().sideOpen).toBe(true)
    expect(store.getState().badgeSeen).toBe(3)
    expect(unseenBadge(store.getState().badgeTotal, store.getState().badgeSeen)).toBe(0)

    release()
  })

  /**
   * The dock asks this before it shows itself at all.
   *
   * A dock offering a second view of the conversation the page already shows would give one draft
   * channel two composers and one remembered layout two rails. Its own nested conversation must not
   * be what makes it hide, which is what `insideDock` says.
   */
  it('counts the conversations mounted outside a dock, and only those', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    expect(store.getState().conversationsOutsideDock).toBe(0)

    const inDock = store.actions.registerConversation(true)
    expect(store.getState().conversationsOutsideDock).toBe(0)

    const onPage = store.actions.registerConversation(false)
    expect(store.getState().conversationsOutsideDock).toBe(1)

    onPage()
    expect(store.getState().conversationsOutsideDock).toBe(0)
    // Idempotent, like the rail's: a second release must not count below zero.
    onPage()
    inDock()
    expect(store.getState().conversationsOutsideDock).toBe(0)

    release()
  })

  /**
   * A `<chat-dock/>` is the user looking without the user opening the rail.
   *
   * The dock's launcher carries the same unseen count the transcript's rail button does, so opening
   * it has to move the same watermark - but only that: it does not open the rail, and `toggleSide`
   * would. To the total as it stands, never to zero, or the next thing a section reports would be
   * measured against nothing.
   */
  it('records what has been seen without touching the rail', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.actions.registerRail()
    store.actions.publishBadgeTotal(4)

    store.actions.markBadgesSeen(store.getState().badgeTotal)
    expect(store.getState().badgeSeen).toBe(4)
    expect(unseenBadge(store.getState().badgeTotal, store.getState().badgeSeen)).toBe(0)
    expect(store.getState().sideOpen).toBe(false)
    expect(store.getState().railDecided).toBe(false)

    release()
  })

  /** Nothing to count once the rail is gone: a stale number on the button would open nothing. */
  it('drops the badge total when the last rail leaves', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    const unregister = store.actions.registerRail()
    store.actions.publishBadgeTotal(2)
    unregister()

    expect(store.getState().badgeTotal).toBe(0)
    release()
  })

  /**
   * The rail states its own default, because a rail nobody has ever opened would otherwise show a
   * standalone page nothing at all. Only the user's own toggle overrules it - and then for good.
   */
  it('applies the configured open default until the user decides', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    expect(store.getState().sideOpen).toBe(false)

    store.actions.applyRailDefault(true)
    expect(store.getState().sideOpen).toBe(true)

    // The user's own toggle wins: a remount, or a second rail, re-states the default and is ignored.
    store.actions.toggleSide(0)
    expect(store.getState().sideOpen).toBe(false)
    store.actions.applyRailDefault(true)
    expect(store.getState().sideOpen).toBe(false)

    release()
  })

  /** A rail the user closed stays closed on the next page load, configured default or not. */
  it('lets the stored rail state overrule the configured default', () => {
    const store = chatStore('alpha', { ...BOOT, railOpen: false, railOpenDefault: true })
    const release = store.subscribe(() => undefined)

    expect(store.getState().sideOpen).toBe(false)
    store.actions.applyRailDefault(true)
    expect(store.getState().sideOpen).toBe(false)

    release()
  })

  /**
   * Collapsing is remembered per section name.
   *
   * A section the user never touched follows its configured `open` flag, so adding a section to a
   * rail gives it its configured state even for a user who arranged the others long ago.
   */
  it('lets a stored section choice overrule the configured default, per name', () => {
    const store = chatStore('alpha', {
      ...BOOT,
      collapsedSections: ['context'],
      sectionChoices: { context: true, progress: false },
    })
    const release = store.subscribe(() => undefined)

    // context: configured collapsed, expanded by the user. progress: configured open, collapsed.
    expect(store.getState().collapsedSections).toEqual(['progress'])

    // A rail re-stating its defaults changes nothing the user decided, and adds what is new.
    store.actions.applyCollapsedDefaults(['context', 'sources'])
    expect(new Set(store.getState().collapsedSections)).toEqual(new Set(['sources', 'progress']))

    release()
  })

  /** Toggling a section is a decision, and decisions are what gets stored. */
  it('records a section toggle as the user choice', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    store.actions.toggleSection('progress')
    await settle()

    expect(store.getState().collapsedSections).toContain('progress')
    expect(preferenceWrites).toHaveLength(1)
    expect(preferenceWrites[0]).toEqual({
      paneWidth: 0,
      badgeSeen: 0,
      sections: { progress: false },
    })

    release()
  })

  /**
   * The preference stores whole pixels and the server refuses a fraction, which a splitter's
   * pointer produces (#588): what the store writes is the rounded width.
   */
  it('stores a pane width as whole pixels', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    store.actions.setPaneWidth(422.70001220703125)
    await settle()

    expect(store.getState().paneWidth).toBe(423)
    expect(preferenceWrites).toHaveLength(1)
    expect(preferenceWrites[0]).toMatchObject({ paneWidth: 423 })

    release()
  })

  /** An unregister that runs twice must not push the count below zero. */
  it('ignores a repeated unregister', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    const unregister = store.actions.registerRail()
    unregister()
    unregister()
    expect(store.getState().railMounted).toBe(false)

    store.actions.registerRail()
    expect(store.getState().railMounted).toBe(true)

    release()
  })
})

describe('the bootstrap', () => {
  it('is what the control put into its state', () => {
    expect(
      readBootstrap({
        windowId: 'w9',
        userName: 'Sandra',
        welcomeName: 'Sandra',
        language: 'de-DE',
        traceMode: 'detailed',
        railOpen: true,
        paneWidth: 480,
        badgeSeen: 3,
        sectionChoices: { progress: false },
      }),
    ).toEqual({
      windowId: 'w9',
      authenticated: true,
      userName: 'Sandra',
      welcomeName: 'Sandra',
      language: 'de',
      traceMode: 'detailed',
      layoutKey: '',
      railOpen: true,
      railOpenDefault: false,
      paneWidth: 480,
      paneWidthDefault: 0,
      badgeSeen: 3,
      sectionChoices: { progress: false },
      collapsedSections: undefined,
    })
  })

  it('falls back to a readable chat when the state says nothing', () => {
    expect(readBootstrap({})).toEqual({
      windowId: '',
      authenticated: true,
      userName: 'User',
      welcomeName: '',
      language: 'en',
      traceMode: 'summary',
      layoutKey: '',
      railOpen: null,
      railOpenDefault: false,
      paneWidth: 0,
      paneWidthDefault: 0,
      badgeSeen: 0,
      sectionChoices: {},
      collapsedSections: undefined,
    })
  })

  /** The element's configured default is what a user who never chose starts with. */
  it('seeds the chat with the configured trace mode and the stored layout', () => {
    const store = chatStore('alpha', {
      ...BOOT,
      traceMode: 'detailed',
      railOpen: true,
      paneWidth: 480,
    })
    const release = store.subscribe(() => undefined)

    expect(store.getState().traceMode).toBe('detailed')
    expect(store.getState().sideOpen).toBe(true)
    expect(store.getState().paneWidth).toBe(480)

    release()
  })
})

/**
 * What a reload in mid-run gets back.
 *
 * `GET /chat/sessions/{id}` brings the transcript, but the stream replays nothing: before this the
 * page came back with an empty Fortschritt list, an idle composer and no sign of the approval the
 * agent was blocked on, while the run carried on server-side.
 */
describe('recovering a run on boot', () => {
  /** What `/chat/updates` and `/chat/interactions` answer in the test at hand. */
  let runState: { processing: boolean; tasksJson: string }
  let waiting: Record<string, unknown>[]
  /** Held to make the feed answer late, so a frame can arrive while it is in flight. */
  let feedGate: Promise<void>

  beforeEach(() => {
    runState = { processing: false, tasksJson: '' }
    waiting = []
    feedGate = Promise.resolve()
    const globals = globalThis as unknown as Record<string, unknown>
    const inner = globals.fetch as (url: string, init?: unknown) => Promise<unknown>
    globals.fetch = vi.fn(async (url: string, init?: { method?: string; body?: string }) => {
      if (url.includes('/chat/updates')) {
        return { ok: true, status: 200, json: async () => runState }
      }
      if (url.includes('/chat/interactions')) {
        await feedGate
        return { ok: true, status: 200, json: async () => ({ interactions: waiting }) }
      }
      return inner(url, init)
    })
  })

  it('brings back the progress list and the busy composer', async () => {
    runState = {
      processing: true,
      tasksJson: JSON.stringify({
        tasks: [{ id: '1', subject: 'Rechnungen lesen', status: 'in_progress' }],
      }),
    }
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    expect(store.getState().busy).toBe(true)
    expect(store.getState().tasks.map((task) => task.subject)).toEqual(['Rechnungen lesen'])

    release()
  })

  it('puts the open approval back in front of the user', async () => {
    waiting = [
      {
        requestId: 'r1',
        type: 'approval',
        header: 'writeInvoice',
        prompt: 'Rechnung schreiben?',
        payload: '{"id":7}',
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const approvals = store.getState().messages.filter((m) => m.type === 'approval_request')
    expect(approvals).toHaveLength(1)
    expect(approvals[0]).toMatchObject({
      approvalRequestId: 'r1',
      toolName: 'writeInvoice',
      argsJson: '{"id":7}',
      outcome: 'pending',
    })

    release()
  })

  /**
   * Two reasons not to rebuild a card: it belongs to another chat, or its deadline has passed - the
   * agent has stopped waiting for it, so a card offering that decision leads nowhere. Both hold for
   * a question exactly as for an approval.
   */
  it('leaves a foreign and an expired request alone', async () => {
    waiting = [
      { requestId: 'r2', type: 'approval', header: 't', sessionId: 's2', remainingMs: 40_000 },
      { requestId: 'r3', type: 'approval', header: 't', sessionId: 's1', remainingMs: 0 },
      { requestId: 'r4', type: 'question', header: 't', sessionId: 's2', remainingMs: 40_000 },
      { requestId: 'r4b', type: 'choice', header: 't', sessionId: 's1', remainingMs: 0 },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')
    expect(store.getState().messages.filter((m) => m.type === 'approval_request')).toHaveLength(0)
    expect(store.getState().messages.filter((m) => m.type === 'interaction_request')).toHaveLength(0)

    release()
  })

  /**
   * The question the agent asked comes back with the shape it was asked in (#466).
   *
   * `multiSelect` is persisted nowhere; the feed reads it off the run that is still parked on the
   * question. A card that turned it into a radio group would take a different answer from the one
   * the agent asked for.
   */
  it('recovers a multi-select question as multi-select', async () => {
    waiting = [
      {
        requestId: 'q1',
        type: 'choice',
        header: 'Rechnungen',
        prompt: 'Welche soll ich verschicken?',
        payload: '["RE-1","RE-2"]',
        options: [{ label: 'RE-1' }, { label: 'RE-2', description: 'Vom Montag' }],
        multiSelect: true,
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const cards = store.getState().messages.filter((m) => m.type === 'interaction_request')
    expect(cards).toHaveLength(1)
    expect(cards[0]).toMatchObject({
      requestId: 'q1',
      question: 'Welche soll ich verschicken?',
      header: 'Rechnungen',
      interactionType: 'choice',
      multiSelect: true,
      answered: false,
      options: [{ label: 'RE-1' }, { label: 'RE-2', description: 'Vom Montag' }],
    })

    release()
  })

  /** A single-select question stays single-select; the shape is read, not assumed. */
  it('leaves a single-select question single-select', async () => {
    waiting = [
      {
        requestId: 'q2',
        type: 'choice',
        header: 'Weiter?',
        prompt: 'Soll ich fortfahren?',
        options: [{ label: 'Ja' }, { label: 'Nein' }],
        multiSelect: false,
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const card = store
      .getState()
      .messages.find((m) => m.type === 'interaction_request' && m.requestId === 'q2')
    expect(card && card.type === 'interaction_request' ? card.multiSelect : 'missing').toBe(false)

    release()
  })

  /** A free-text question comes back as a text field, not as an empty choice. */
  it('recovers a free-text question as a text question', async () => {
    waiting = [
      {
        requestId: 'q3',
        type: 'question',
        header: 'Kundennummer',
        prompt: 'Wie lautet die Kundennummer?',
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const card = store
      .getState()
      .messages.find((m) => m.type === 'interaction_request' && m.requestId === 'q3')
    expect(card).toMatchObject({ interactionType: 'text', options: undefined, multiSelect: false })

    release()
  })

  /** The stream and the feed both report the same question; the user must see one card. */
  it('does not double a question the stream delivered while the feed was in flight', async () => {
    let openFeed: () => void = () => undefined
    feedGate = new Promise<void>((resolve) => {
      openFeed = resolve
    })
    waiting = [
      {
        requestId: 'q4',
        type: 'choice',
        header: 'Weiter?',
        options: [{ label: 'Ja' }],
        multiSelect: true,
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    const selecting = store.actions.selectSession('s1')
    await settle()
    streams[0].emit('user_interaction', {
      requestId: 'q4',
      question: 'Soll ich fortfahren?',
      type: 'choice',
      options: [{ label: 'Ja' }],
      multiSelect: true,
    })
    openFeed()
    await selecting

    expect(store.getState().messages.filter((m) => m.type === 'interaction_request')).toHaveLength(1)

    release()
  })

  /** The stream and the feed both report the same approval; the user must see one card. */
  it('does not double a card the stream delivered while the feed was in flight', async () => {
    let openFeed: () => void = () => undefined
    feedGate = new Promise<void>((resolve) => {
      openFeed = resolve
    })
    waiting = [
      {
        requestId: 'r5',
        type: 'approval',
        header: 'writeInvoice',
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    const selecting = store.actions.selectSession('s1')
    await settle()
    streams[0].emit('approval_required', {
      approvalRequestId: 'r5',
      toolName: 'writeInvoice',
      argsJson: '{}',
    })
    openFeed()
    await selecting

    expect(store.getState().messages.filter((m) => m.type === 'approval_request')).toHaveLength(1)

    release()
  })

  /** The recovered card is phrased from the tool's mode, as the live one is (#393). */
  it('recovers an approval with the mode its sentence is phrased from', async () => {
    waiting = [
      {
        requestId: 'r8',
        type: 'approval',
        header: 'changeData',
        toolMode: 'DATA_WRITE',
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const card = store
      .getState()
      .messages.find((m) => m.type === 'approval_request' && m.approvalRequestId === 'r8')
    expect(card && card.type === 'approval_request' ? card.toolMode : 'missing').toBe('DATA_WRITE')

    release()
  })

  /**
   * Another window of this account answered the card this one is showing (#393).
   *
   * A window that only listed the request has no run of its own to hear `approval_resolved` from;
   * `interaction_resolved` is what stops it from showing a card nobody is waiting on.
   */
  it('takes back a card another window answered', async () => {
    waiting = [
      {
        requestId: 'r7',
        type: 'approval',
        header: 'writeInvoice',
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')
    const shown = store
      .getState()
      .messages.find((m) => m.type === 'approval_request' && m.approvalRequestId === 'r7')
    expect(shown).toBeDefined()

    streams[0].emit('interaction_resolved', { requestId: 'r7', outcome: 'REJECTED:Zu teuer.' })
    await settle()

    const card = store
      .getState()
      .messages.find((m) => m.type === 'approval_request' && m.approvalRequestId === 'r7')
    expect(card && card.type === 'approval_request' ? card.outcome : 'missing').toBe('REJECTED')

    release()
  })

  /** A chat with nothing in flight comes back exactly as it did before. */
  it('says nothing about a chat whose run has ended', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    expect(store.getState().busy).toBe(false)
    expect(store.getState().tasks).toEqual([])
    expect(store.getState().messages).toEqual([])

    release()
  })
})

/**
 * What the four turn-setting channels write into the store.
 *
 * Attaching a set of servers at once is the one action the pickers did not already have: the MCP
 * toggles change one name at a time, while a `mcp-servers` channel states the whole attachment. It
 * counts as an override of what the agent declares, exactly as a toggle does - otherwise the next
 * send would silently drop it and run on the agent's own servers.
 */
describe('the turn settings', () => {
  /**
   * An application with two agents: the entry point declares one server, and there is a second of
   * each to move to.
   */
  function withCapabilities(): void {
    const globals = globalThis as unknown as Record<string, unknown>
    const inner = globals.fetch as (url: string, init?: unknown) => Promise<unknown>
    globals.fetch = vi.fn(async (url: string, init?: { method?: string; body?: string }) => {
      if (url.includes('/chat/capabilities')) {
        return {
          ok: true,
          status: 200,
          json: async () => ({
            entryPointAgent: 'Albert',
            agents: [
              { name: 'Albert', model: 'm1', mcpServers: ['files'] },
              { name: 'Berta', model: 'm2', mcpServers: [] },
            ],
            models: [{ id: 'm1' }, { id: 'm2' }],
            mcpServers: [{ name: 'files' }, { name: 'issues' }],
          }),
        }
      }
      return inner(url, init)
    })
  }

  it('attaches exactly the servers it is given, and marks the attachment as chosen', async () => {
    withCapabilities()
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    expect(store.getState().mcp).toEqual(['files'])
    expect(store.getState().mcpDirty).toBe(false)

    store.actions.selectMcpServers(['files', 'issues'])
    expect(store.getState().mcp).toEqual(['files', 'issues'])
    expect(store.getState().mcpDirty).toBe(true)

    store.actions.selectMcpServers([])
    expect(store.getState().mcp).toEqual([])
    expect(store.getState().mcpDirty).toBe(true)

    release()
  })

  /**
   * An agent chosen before the catalogue arrived survives it, and is then given its model.
   *
   * The `agent` channel can name one in the control state the element boots from, which is a whole
   * round trip earlier than `GET /chat/capabilities` answers. Applying the entry point regardless
   * would silently overrule a host that pre-selected an agent for its page.
   */
  it('keeps an agent chosen before the catalogue arrived, and gives it its model', async () => {
    withCapabilities()
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    // Not the entry point, so applying the entry point regardless would be visible here.
    store.actions.selectAgent('Berta')
    store.attach(BOOT)
    await settle()

    expect(store.getState().agent).toBe('Berta')
    expect(store.getState().model).toBe('m2')

    release()
  })

  /**
   * Picking an agent brings that agent's model and servers with it - which is why a host writing
   * both channels has the conversation apply the agent first.
   */
  it('resets the model and the attachment to what the agent itself uses', async () => {
    withCapabilities()
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    store.actions.selectModel('m2')
    store.actions.selectMcpServers(['issues'])
    expect(store.getState().model).toBe('m2')

    store.actions.selectAgent('Albert')
    expect(store.getState().model).toBe('m1')
    expect(store.getState().mcp).toEqual(['files'])
    expect(store.getState().mcpDirty).toBe(false)

    release()
  })
})

/**
 * The frames of somebody else's turn.
 *
 * Every `/chat/*` call is keyed by the window alone, so both stores of a window with two chat ids
 * attach to one server-side tracker and see one stream. A frame naming a session that is not the
 * open one is the other conversation's.
 */
describe('eventBelongsToChat', () => {
  it('drops a run-scoped frame that names another session', () => {
    expect(eventBelongsToChat('text_delta', JSON.stringify({ sessionId: '77', text: 'x' }), '42')).toBe(false)
    expect(eventBelongsToChat('run_started', JSON.stringify({ sessionId: '77' }), '42')).toBe(false)
    expect(eventBelongsToChat('message_complete', JSON.stringify({ sessionId: '42' }), '42')).toBe(true)
  })

  /**
   * Inert where the server says nothing: most frames carry no session id today, and a guard that
   * dropped those would leave every transcript empty. It tightens by itself once they do.
   */
  it('delivers a frame that names no session, and anything unparsable', () => {
    expect(eventBelongsToChat('text_delta', JSON.stringify({ text: 'x' }), '42')).toBe(true)
    expect(eventBelongsToChat('text_delta', 'not json', '42')).toBe(true)
    expect(eventBelongsToChat('text_delta', JSON.stringify({ sessionId: '' }), '42')).toBe(true)
  })

  /** What is addressed to the window rather than to a turn is never filtered. */
  it('always delivers the frames that belong to the window', () => {
    for (const name of ['connected', 'heartbeat', 'approval_required', 'user_interaction']) {
      expect(eventBelongsToChat(name, JSON.stringify({ sessionId: '77' }), '42')).toBe(true)
    }
  })

  /** A store with no session open yet must not swallow the run that is creating one. */
  it('drops a named frame while no session is open, and keeps an unnamed one', () => {
    expect(eventBelongsToChat('text_delta', JSON.stringify({ sessionId: '77' }), null)).toBe(false)
    expect(eventBelongsToChat('text_delta', JSON.stringify({ text: 'x' }), null)).toBe(true)
  })
})

/**
 * How a host reaches the composer: through the store, never through the DOM.
 *
 * The composer binds itself once it is mounted, and unbinds when it goes. What the `draft` and the
 * `reference` channels do is exactly these two calls, so the store is where "a write replaces the
 * text" and "a write inserts a token" can be held without a browser.
 */
/**
 * Which chat this window brought into being (#508).
 *
 * A host that stamps a new conversation with what its page is about needs that difference, and only
 * the browser has it: the first send is what creates a chat, while selecting one and restoring one
 * after a reload both open a chat that was started somewhere else.
 */
describe('the chat this window created', () => {
  it('records the chat the first send brought into being', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    expect(store.getState().createdSessionId).toBeNull()

    await store.actions.send('allererste Frage', [])

    expect(store.getState().sessionId).toBe('s1')
    expect(store.getState().createdSessionId).toBe('s1')

    await settle()
    release()
  })

  it('records nothing for a chat that was only opened', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    await store.actions.selectSession('s1')

    expect(store.getState().sessionId).toBe('s1')
    expect(store.getState().createdSessionId).toBeNull()

    await settle()
    release()
  })
})

describe('the composer seam', () => {
  it('replaces the draft, and the composer is what focuses and moves the caret', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    const drafts: string[] = []
    store.actions.bindComposer({
      insertReference: () => undefined,
      setDraft: (text) => drafts.push(text),
      focus: () => undefined,
    })

    store.actions.setDraft('Bitte erkläre die Rechnung R-2024-014.')
    store.actions.setDraft('')

    expect(drafts).toEqual(['Bitte erkläre die Rechnung R-2024-014.', ''])

    release()
  })

  it('inserts a reference token, as picking it from the "@" menu does', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    const tokens: string[] = []
    store.actions.bindComposer({
      insertReference: (token) => tokens.push(token.label),
      setDraft: () => undefined,
      focus: () => undefined,
    })

    store.actions.insertReference({
      kind: 'Invoice',
      id: 'aaa-bbb',
      label: 'Hotelrechnung Berlin',
      text: 'Hotelrechnung Berlin (1.240,00 €)',
    })

    expect(tokens).toEqual(['Hotelrechnung Berlin'])

    release()
  })

  it('does nothing at all while no composer is mounted', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.actions.bindComposer(null)

    expect(() => store.actions.setDraft('Bitte erkläre die Rechnung.')).not.toThrow()

    release()
  })
})

/**
 * Which conversation a request is for, beside which window it comes from.
 *
 * A window can show more than one - a page's chat beside a `<chat-dock>`'s, two `<agent-chat>`
 * elements - and the server holds an active session, a run and an event stream for each. It used to
 * hold one per window, so the second conversation to send took the first one's stream and the first
 * waited for an answer that never came. Every call therefore names its conversation.
 */
describe('every request names its conversation', () => {
  it('puts the chat id on the stream, the capabilities, the sessions and the layout', async () => {
    const store = chatStore('belege', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    store.actions.toggleSide(0)
    await settle()

    expect(streams[0].url).toContain('conversation=belege')
    for (const url of requested) {
      expect(url).toContain('conversation=belege')
    }
    expect(requested.some((url) => url.includes('/chat/capabilities'))).toBe(true)
    expect(requested.some((url) => url.includes('/chat/preferences'))).toBe(true)

    release()
  })

  /**
   * The layout key and the conversation are two different keys on the preferences call.
   *
   * `chatId` is where the layout is stored - the empty one for an `<agent-chat>` - while
   * `conversation` is which chat of the window is asking, which is how the servlet resolves the
   * request at all.
   */
  it('keeps the layout key apart from the conversation', async () => {
    const boot = { ...BOOT, layoutKey: '' }
    const store = chatStore('v42', boot)
    const release = store.subscribe(() => undefined)
    store.attach(boot)
    await settle()
    store.actions.toggleSide(0)
    await settle()

    const layout = requested.find((url) => url.includes('/chat/preferences'))
    expect(layout).toContain('chatId=&')
    expect(layout).toContain('conversation=v42')

    release()
  })

  it('gives two conversations of one window two different addresses', async () => {
    const page = chatStore('page', BOOT)
    const dock = chatStore('dock', BOOT)
    const releasePage = page.subscribe(() => undefined)
    const releaseDock = dock.subscribe(() => undefined)
    page.attach(BOOT)
    dock.attach(BOOT)
    await settle()

    expect(streams).toHaveLength(2)
    expect(streams.map((stream) => stream.url.includes('conversation=page'))).toContain(true)
    expect(streams.map((stream) => stream.url.includes('conversation=dock'))).toContain(true)

    releasePage()
    releaseDock()
  })
})

/**
 * The stop button names the chat it is stopping (#465).
 *
 * The run's state lives in the window that started it, and a second tab or a reloaded page is a
 * different window to the server. Without the chat in the body the server has nothing but the
 * asking window to look in, and the stop of every other window did nothing at all.
 */
describe('stopping a run', () => {
  it('names the chat, so a window that did not start the run can stop it', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    await store.actions.send('Mach etwas Langes.', [])
    const chat = sentBodies[0].sessionId
    expect(typeof chat).toBe('string')
    expect(chat).not.toBe('')

    store.actions.stop()
    await settle()

    expect(cancelBodies).toEqual([{ sessionId: chat }])

    release()
  })
})

/**
 * A window that opens a conversation while a run of it is still going (#512).
 *
 * The transcript it is handed now contains the open turn - the user's message, what the agent has
 * said so far, the approval it is parked on - because the server reads it off the run rather than
 * out of the database, which does not have it until the run ends. This is what the store has to
 * make of it: one card, in its place under that turn, wearing the shape the waiting run knows.
 */
describe('a window opened while the run is in flight', () => {
  /** What `GET /chat/sessions/{id}` hands back in the test at hand. */
  let turnsJson: string
  let runState: { processing: boolean; tasksJson: string }
  let waiting: Record<string, unknown>[]
  /** Every `GET /chat/sessions/{id}` the store made, so a reload can be counted. */
  let loads: number
  /** Where the snapshot ends in the run's text; `undefined` for a chat with no run in flight. */
  let inFlight: { streamedChars: number } | undefined
  /** Holds the answer to `GET /chat/sessions/{id}` back until the test lets it go. */
  let gate: Promise<void> | null

  /** The in-flight turn as the server serialises it: user text, partial answer, pending approval. */
  const IN_FLIGHT = JSON.stringify([
    {
      id: 't1',
      turnNumber: 1,
      timestamp: 1000,
      userParts: [{ id: 'u1', type: 'text', timestamp: 1000, textData: { text: 'Bitte ändern' } }],
      assistantParts: [
        { id: 'a1', type: 'text', timestamp: 1100, textData: { text: 'Dafür brauche ich eine Freigabe.' } },
        {
          id: 'r1',
          type: 'approval',
          timestamp: 1200,
          approvalData: {
            toolCallId: 'tc1',
            toolName: 'editText',
            argsJson: '{"objectId":"Invoice:1"}',
            displayReason: 'Modifies shared application data.',
            decision: '',
          },
        },
      ],
    },
  ])

  beforeEach(() => {
    turnsJson = ''
    runState = { processing: true, tasksJson: '' }
    waiting = []
    loads = 0
    inFlight = undefined
    gate = null
    const globals = globalThis as unknown as Record<string, unknown>
    const inner = globals.fetch as (url: string, init?: unknown) => Promise<unknown>
    globals.fetch = vi.fn(async (url: string, init?: { method?: string; body?: string }) => {
      if (url.includes('/chat/updates')) {
        return { ok: true, status: 200, json: async () => runState }
      }
      if (url.includes('/chat/interactions')) {
        return { ok: true, status: 200, json: async () => ({ interactions: waiting }) }
      }
      if (/\/chat\/sessions\/[^/?]/.test(url)) {
        loads++
        // Read now, not when the test lets the answer go: the snapshot is what it was when asked.
        const answer = { id: 's1', turnsJson, inFlight }
        if (gate) await gate
        return { ok: true, status: 200, json: async () => answer }
      }
      return inner(url, init)
    })
  })

  /**
   * The whole of the defect in one assertion: the question the approval is about stands above it.
   * Before the fix the transcript stopped at the previous turn and the card floated at the end of
   * whatever was there, which is how a decision came to sit under an unrelated turn.
   */
  it('renders the open turn, with the approval inside it', async () => {
    turnsJson = IN_FLIGHT
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const kinds = store.getState().messages.map((m) => m.type)
    expect(kinds.indexOf('user')).toBe(0)
    expect(kinds.indexOf('assistant_text')).toBeGreaterThan(0)
    expect(kinds.indexOf('approval_request')).toBeGreaterThan(kinds.indexOf('assistant_text'))
    expect(store.getState().busy).toBe(true)

    release()
  })

  /** A turn that is still being written carries no copy/rate/regenerate row. */
  it('shows the last answer of a running turn as still streaming', async () => {
    turnsJson = IN_FLIGHT
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const answer = store.getState().messages.find((m) => m.type === 'assistant_text')
    expect(answer).toMatchObject({ isStreaming: true })

    release()
  })

  /**
   * The pending feed completes the restored card rather than appending a second one: only the
   * waiting run knows the tool's mode and how long is left, and only the transcript knows where the
   * card belongs.
   */
  it('completes the restored card instead of adding one at the end', async () => {
    turnsJson = IN_FLIGHT
    waiting = [
      {
        requestId: 'r1',
        type: 'approval',
        header: 'editText',
        prompt: 'Modifies shared application data.',
        payload: '{"objectId":"Invoice:1"}',
        toolMode: 'DATA_WRITE',
        sessionId: 's1',
        remainingMs: 40_000,
      },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const approvals = store.getState().messages.filter((m) => m.type === 'approval_request')
    expect(approvals).toHaveLength(1)
    expect(approvals[0]).toMatchObject({
      approvalRequestId: 'r1',
      toolMode: 'DATA_WRITE',
      remainingMs: 40_000,
      outcome: 'pending',
    })
    // Still inside the turn it was asked in, not appended after everything.
    const messages = store.getState().messages
    expect(messages[messages.length - 1].type).toBe('approval_request')
    expect(messages.filter((m) => m.type === 'user')).toHaveLength(1)

    release()
  })

  /**
   * A recorded approval says in the reader's language what the agent wanted to do.
   *
   * The reason the security context wrote is one English sentence for everybody, so the history
   * printed "Modifies shared application data." into a German transcript. The tool's mode is what
   * the live card phrases its own sentence from, and it is now recorded with the approval (#512).
   */
  it('carries the recorded tool mode of an approval', async () => {
    turnsJson = JSON.stringify([
      {
        id: 't1',
        turnNumber: 1,
        timestamp: 1000,
        userParts: [{ id: 'u1', type: 'text', timestamp: 1000, textData: { text: 'Bitte ändern' } }],
        assistantParts: [
          {
            id: 'r1',
            type: 'approval',
            timestamp: 1200,
            approvalData: {
              toolCallId: 'tc1',
              toolName: 'editText',
              argsJson: '{}',
              displayReason: 'Modifies shared application data.',
              mode: 'DATA_WRITE',
              decision: 'APPROVED_FOR_SESSION',
            },
          },
        ],
      },
    ])
    runState = { processing: false, tasksJson: '' }
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')

    const approval = store.getState().messages.find((m) => m.type === 'approval_request')
    expect(approval).toMatchObject({ toolMode: 'DATA_WRITE', outcome: 'APPROVED_FOR_SESSION' })

    release()
  })

  /** A turn the agent is writing, as the snapshot of it reads: `Ich pruefe das ` so far. */
  const WRITING = JSON.stringify([
    {
      id: 't1',
      turnNumber: 1,
      timestamp: 1000,
      userParts: [{ id: 'u1', type: 'text', timestamp: 1000, textData: { text: 'Bitte pruefen' } }],
      assistantParts: [
        { id: 'streaming-t1', type: 'text', timestamp: 1100, textData: { text: 'Ich pruefe das ' } },
      ],
    },
  ])

  /**
   * The snapshot and the stream travel on different connections, so a window joining mid-run gets
   * deltas that are already in its snapshot, deltas that arrive before it, and one that straddles
   * its end. Each piece of text is shown exactly once (#544).
   */
  it('joins the stream where the snapshot ends, without a gap or an overlap', async () => {
    turnsJson = WRITING
    inFlight = { streamedChars: 15 }
    let release = () => undefined as void
    gate = new Promise((resolve) => (release = resolve))
    const store = chatStore('alpha', BOOT)
    const unsubscribe = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    const opened = store.actions.selectSession('s1')
    await settle()
    // Before the snapshot is here: one delta it already holds, and one it does not.
    streams[0].emit('text_delta', { text: 'das ', end: 15, sessionId: 's1' })
    streams[0].emit('text_delta', { text: 'ge', end: 17, sessionId: 's1' })
    release()
    await opened
    // After it: one that overlaps what was placed, and one that is wholly new.
    streams[0].emit('text_delta', { text: 'gerade', end: 21, sessionId: 's1' })
    streams[0].emit('text_delta', { text: '!', end: 22, sessionId: 's1' })

    const answers = store.getState().messages.filter((m) => m.type === 'assistant_text')
    expect(answers).toHaveLength(1)
    expect(answers[0]).toMatchObject({ text: 'Ich pruefe das gerade!', isStreaming: true })
    // Still working, not "worked for 100 ms" as the snapshot's last part would have it.
    const meta = store.getState().messages.find((m) => m.type === 'run_meta')
    expect(meta).toMatchObject({ type: 'run_meta' })
    expect(meta?.type === 'run_meta' && meta.durationMs).toBeUndefined()

    unsubscribe()
  })

  /** The text of the chat being left is not written into the chat being opened. */
  it('drops a delta of another chat that arrives while this one opens', async () => {
    turnsJson = WRITING
    inFlight = { streamedChars: 15 }
    let release = () => undefined as void
    gate = new Promise((resolve) => (release = resolve))
    const store = chatStore('alpha', BOOT)
    const unsubscribe = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    const opened = store.actions.selectSession('s1')
    await settle()
    streams[0].emit('text_delta', { text: 'aus dem alten Chat', end: 40, sessionId: 's0' })
    release()
    await opened

    const answers = store.getState().messages.filter((m) => m.type === 'assistant_text')
    expect(answers[0]).toMatchObject({ text: 'Ich pruefe das ' })

    unsubscribe()
  })

  /** A snapshot that ends on a step: the next text is a paragraph of its own, after the step. */
  it('opens a new paragraph for text after a step', async () => {
    turnsJson = JSON.stringify([
      {
        id: 't1',
        turnNumber: 1,
        timestamp: 1000,
        userParts: [{ id: 'u1', type: 'text', timestamp: 1000, textData: { text: 'Bitte pruefen' } }],
        assistantParts: [
          { id: 'a1', type: 'text', timestamp: 1100, textData: { text: 'Ich sehe nach.' } },
          {
            id: 'c1',
            type: 'tool_call',
            timestamp: 1200,
            toolCallData: { toolCallId: 'tc1', toolName: 'search', argsJson: '{}' },
          },
        ],
      },
    ])
    inFlight = { streamedChars: 14 }
    const store = chatStore('alpha', BOOT)
    const unsubscribe = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')
    streams[0].emit('text_delta', { text: 'Gefunden.', end: 23, sessionId: 's1' })

    const messages = store.getState().messages
    const answers = messages.filter((m) => m.type === 'assistant_text')
    expect(answers.map((m) => m.type === 'assistant_text' && m.text)).toEqual(['Ich sehe nach.', 'Gefunden.'])
    expect(messages[messages.length - 1]).toMatchObject({ type: 'assistant_text', text: 'Gefunden.' })

    unsubscribe()
  })

  /**
   * A window that joined mid-run ends with the recorded turn: the answer at once, and the turn read
   * back, so that its steps are the recorded ones too.
   */
  it('reads the turn back when a run it joined mid-way completes', async () => {
    turnsJson = WRITING
    inFlight = { streamedChars: 15 }
    const store = chatStore('alpha', BOOT)
    const unsubscribe = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')
    const loadsAfterSelect = loads
    streams[0].emit('text_delta', { text: 'gerade', end: 21, sessionId: 's1' })
    streams[0].emit('message_complete', { contentText: 'Ich pruefe das gerade.' })
    await settle()

    expect(loads).toBe(loadsAfterSelect + 1)
    expect(store.getState().busy).toBe(false)

    unsubscribe()
  })

  /**
   * The completion of a run this window did not start: there is no message of ours to write the
   * answer into, so the conversation is read back and the finished turn replaces the snapshot.
   */
  it('reads the conversation back when a completion arrives with no run of its own', async () => {
    turnsJson = IN_FLIGHT
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.selectSession('s1')
    const loadsAfterSelect = loads

    turnsJson = JSON.stringify([
      {
        id: 't1',
        turnNumber: 1,
        timestamp: 1000,
        userParts: [{ id: 'u1', type: 'text', timestamp: 1000, textData: { text: 'Bitte ändern' } }],
        assistantParts: [
          { id: 'a2', type: 'text', timestamp: 1400, textData: { text: 'Erledigt.' } },
        ],
      },
    ])
    streams[0].emit('message_complete', { contentText: 'Erledigt.' })
    await settle()

    expect(loads).toBe(loadsAfterSelect + 1)
    expect(store.getState().busy).toBe(false)
    const answers = store.getState().messages.filter((m) => m.type === 'assistant_text')
    expect(answers).toHaveLength(1)
    expect(answers[0]).toMatchObject({ text: 'Erledigt.' })

    release()
  })
})

/**
 * A page whose window the server no longer knows (issue #574).
 *
 * A login or logout in another tab of the same browser, or an application restart, replaces the
 * session behind an open page. Its chat calls then reach a session that never rendered the window,
 * and the server answers `409 stale_window`. The chat used to take that for "the agent is busy" on
 * a send - dropping what the user typed - and to re-open a refused stream once a second for as long
 * as the page stayed open. Loading the page again is what renders the window in the session the
 * browser holds now; these pin that the chat does that once, and keeps the user's text across it.
 */
describe('a window the server no longer knows', () => {
  let storage: Map<string, string>
  let reloads: number

  /** The server's answer for a window it cannot resolve (`ChatServlet#sendStaleWindow`). */
  function staleAnswer() {
    const answer = {
      ok: false,
      status: 409,
      json: async () => ({ status: 'error', error: 'stale_window' }),
      clone: () => answer,
    }
    return answer
  }

  /** Answers the calls whose URL contains `path` as a stale window, everything else as before. */
  function staleOn(path: string): void {
    const globals = globalThis as unknown as Record<string, unknown>
    const before = globals.fetch as (url: string, init?: unknown) => Promise<unknown>
    globals.fetch = vi.fn(async (url: string, init?: unknown) => {
      if (url.includes(path)) {
        requested.push(url)
        return staleAnswer()
      }
      return before(url, init)
    })
  }

  beforeEach(() => {
    storage = new Map()
    reloads = 0
    const win = (globalThis as unknown as { window: Record<string, unknown> }).window
    win.sessionStorage = {
      getItem: (key: string) => storage.get(key) ?? null,
      setItem: (key: string, value: string) => storage.set(key, value),
      removeItem: (key: string) => storage.delete(key),
    }
    win.location = {
      pathname: '/app/view/chat',
      reload: () => {
        reloads++
      },
    }
    vi.spyOn(console, 'warn').mockImplementation(() => undefined)
    vi.spyOn(console, 'error').mockImplementation(() => undefined)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('reloads the page on a refused send and gives the reloaded composer the text back', async () => {
    staleOn('/chat/send')
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    const staleDrafts: string[] = []
    const staleComposer = {
      insertReference: () => undefined,
      setDraft: (text: string) => staleDrafts.push(text),
      focus: () => undefined,
    }
    store.actions.bindComposer(staleComposer)
    store.actions.dispatch({ type: 'ADD_USER', id: 'u0', text: 'erste Frage' })

    const sent = await store.actions.send('Hallo nach dem Kontowechsel', [])
    // The refusing page renders once more before the reload takes it away, and binds its composer
    // again as it does on every render: the text is for the reloaded page, not for this one.
    store.actions.bindComposer(null)
    store.actions.bindComposer(staleComposer)
    expect(staleDrafts).toEqual([])

    expect(sent).toBe(false)
    expect(reloads).toBe(1)
    // Not the "agent is still working" hint a 409 used to be read as: nothing is running.
    expect(store.getState().notice).toBe('')
    expect(store.getState().busy).toBe(false)
    expect(store.getState().messages).toHaveLength(1)

    await settle()
    release()

    // The reloaded page: a fresh store for the same chat, and its composer mounts.
    const reloaded = chatStore('alpha', BOOT)
    const releaseReloaded = reloaded.subscribe(() => undefined)
    const drafts: string[] = []
    reloaded.actions.bindComposer({
      insertReference: () => undefined,
      setDraft: (text) => drafts.push(text),
      focus: () => undefined,
    })
    expect(drafts).toEqual(['Hallo nach dem Kontowechsel'])

    // Once: a composer that mounts again later does not get it a second time.
    reloaded.actions.bindComposer({
      insertReference: () => undefined,
      setDraft: (text) => drafts.push(text),
      focus: () => undefined,
    })
    expect(drafts).toHaveLength(1)

    await settle()
    releaseReloaded()
  })

  it('keeps the text of the send that creates a chat, too', async () => {
    staleOn('/chat/sessions')
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)

    const sent = await store.actions.send('Neue Frage', [])

    expect(sent).toBe(false)
    expect(reloads).toBe(1)
    expect(sentTexts).toEqual([])
    expect([...storage.values()]).toContain('Neue Frage')

    await settle()
    release()
  })

  it('does not reload again right after a reload, and says why instead', async () => {
    storage.set('tlReactStaleUiReloadTime', String(Date.now() - 2_000))
    staleOn('/chat/send')
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.actions.dispatch({ type: 'ADD_USER', id: 'u0', text: 'erste Frage' })

    const sent = await store.actions.send('noch einmal', [])

    expect(sent).toBe(false)
    expect(reloads).toBe(0)
    expect(store.getState().notice).toBe(bundle('de').staleWindow)

    await settle()
    release()
  })

  it('stops re-opening a stream the server refuses for an unknown window', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    expect(streams).toHaveLength(1)

    staleOn('/chat/updates')
    streams[0].readyState = FakeEventSource.CLOSED
    streams[0].onerror?.()
    await new Promise((resolve) => setTimeout(resolve, 1_200))

    expect(streams).toHaveLength(1)
    expect(reloads).toBe(1)

    release()
  })

  it('re-opens a stream that closed for any other reason, as before', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    streams[0].readyState = FakeEventSource.CLOSED
    streams[0].onerror?.()
    await new Promise((resolve) => setTimeout(resolve, 1_200))

    expect(streams).toHaveLength(2)
    expect(reloads).toBe(0)

    release()
  })
})

/**
 * Which chat a window shows when the choice changes while a transcript is still on its way (#576).
 *
 * A host's `input` channel and the tab's remembered chat both open chats, and an opening that
 * completed after a later one put its chat back on screen. The write-back then reported it, the
 * channel asked for the other one again, and the window switched between two conversations every
 * few milliseconds without end.
 */
describe('choosing a chat while another one loads', () => {
  /** The transcript loads that have not been answered yet, by session id. */
  let pending: Map<string, () => void>
  /** What `GET /chat/sessions` lists. */
  let listed: { id: string; title: string }[]

  beforeEach(() => {
    pending = new Map()
    listed = []
    const globals = globalThis as unknown as Record<string, unknown>
    const inner = globals.fetch as (url: string, init?: unknown) => Promise<unknown>
    globals.fetch = vi.fn(async (url: string, init?: { method?: string; body?: string }) => {
      const load = url.match(/\/chat\/sessions\/([^/?]+)/)
      if (load && !init?.method) {
        const id = load[1]
        await new Promise<void>((resolve) => pending.set(id, resolve))
        const turnsJson = JSON.stringify([
          {
            id: 't-' + id,
            turnNumber: 1,
            timestamp: 1000,
            userParts: [{ id: 'u-' + id, type: 'text', timestamp: 1000, textData: { text: 'Frage in ' + id } }],
            assistantParts: [],
          },
        ])
        return { ok: true, status: 200, json: async () => ({ id, turnsJson }) }
      }
      if (url.includes('/chat/sessions')) {
        return { ok: true, status: 200, json: async () => ({ sessions: listed }) }
      }
      return inner(url, init)
    })
  })

  afterEach(() => {
    delete (globalThis as unknown as { window: Record<string, unknown> }).window.sessionStorage
  })

  const userTexts = (store: ReturnType<typeof chatStore>) =>
    store.getState().messages.filter((m) => m.type === 'user').map((m) => (m as { text?: string }).text)

  it('shows the latest choice even when an earlier transcript arrives after it', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    const earlier = store.actions.selectSession('previous')
    await settle()
    const later = store.actions.selectSession('current')
    await settle()
    pending.get('current')!()
    await later
    pending.get('previous')!()
    await earlier
    await settle()

    expect(store.getState().sessionId).toBe('current')
    expect(userTexts(store)).toEqual(['Frage in current'])

    // "New chat" is a choice too: a transcript still on its way does not undo it.
    const late = store.actions.selectSession('previous')
    await settle()
    store.actions.newChat()
    pending.get('previous')!()
    await late
    await settle()

    expect(store.getState().sessionId).toBeNull()
    expect(userTexts(store)).toEqual([])

    release()
  })

  it('does not reopen the remembered chat over one the window was already given', async () => {
    const window = (globalThis as unknown as { window: Record<string, unknown> }).window
    window.sessionStorage = { getItem: () => 'previous', setItem: () => undefined, removeItem: () => undefined }
    listed = [
      { id: 'previous', title: 'Vorher' },
      { id: 'current', title: 'Jetzt' },
    ]
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    // The host's channel names the page's conversation as the element mounts, before the session
    // list - which is what the restore waits for - has come back.
    const chosen = store.actions.selectSession('current')
    store.attach(BOOT)
    await settle()
    pending.get('current')!()
    await chosen
    await settle()

    expect(pending.has('previous')).toBe(false)
    expect(store.getState().sessionId).toBe('current')

    release()
  })
})

/**
 * The auto-approve switch is a setting of the chat (#658): the server stores it with the chat the
 * switch names, and the switch shows what the server runs the opened chat with.
 */
describe('the auto-approve switch', () => {
  /** The body of every `POST /chat/approval-mode`, in order. */
  let modeBodies: Record<string, unknown>[]
  /** What `GET /chat/sessions/{id}` says about the chat's mode. */
  let storedMode: string

  beforeEach(() => {
    modeBodies = []
    storedMode = 'AUTO_APPROVE'
    const globals = globalThis as unknown as Record<string, unknown>
    const inner = globals.fetch as (url: string, init?: unknown) => Promise<unknown>
    globals.fetch = vi.fn(async (url: string, init?: { method?: string; body?: string }) => {
      if (url.includes('/chat/approval-mode')) {
        modeBodies.push(JSON.parse(init?.body ?? '{}'))
        return { ok: true, status: 200, json: async () => ({ status: 'ok' }) }
      }
      if (/\/chat\/sessions\/[^/?]/.test(url)) {
        return { ok: true, status: 200, json: async () => ({ id: 's1', turnsJson: '', approvalMode: storedMode }) }
      }
      return inner(url, init)
    })
  })

  it('shows the mode the opened chat is run with, not the one the page loaded with', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    expect(store.getState().autoApprove).toBe(false)

    await store.actions.selectSession('s1')

    expect(store.getState().autoApprove).toBe(true)
    release()
  })

  it('names the open chat, so the server stores the mode with it', async () => {
    storedMode = 'MANUAL'
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()
    await store.actions.selectSession('s1')

    await store.actions.toggleAutoApprove()

    expect(modeBodies).toEqual([{ mode: 'AUTO_APPROVE', sessionId: 's1' }])
    release()
  })

  it('names no chat for a conversation that has none yet', async () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    store.attach(BOOT)
    await settle()

    await store.actions.toggleAutoApprove()

    expect(modeBodies).toEqual([{ mode: 'AUTO_APPROVE' }])
    release()
  })
})
