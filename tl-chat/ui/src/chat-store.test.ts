import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { chatIdOf, chatStore, liveChatCount, readBootstrap, type ChatBootstrap } from './chat-store'
import { unseenBadge } from './progress-model'
import type { ChatMessage } from './types'

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
  railOpen: false,
  paneWidth: 0,
  badgeSeen: 0,
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
  const globals = globalThis as unknown as Record<string, unknown>
  globals.window = {
    location: { pathname: '/app/view/chat' },
    setTimeout: (fn: () => void, ms?: number) => setTimeout(fn, ms) as unknown as number,
    clearTimeout: (id: number) => clearTimeout(id),
  }
  globals.EventSource = FakeEventSource
  globals.fetch = vi.fn(async (url: string, init?: { method?: string; body?: string }) => {
    if (init?.method === 'PUT' && url.includes('/chat/preferences')) {
      preferenceWrites.push(JSON.parse(init.body ?? '{}'))
      return { ok: true, status: 200, json: async () => ({}) }
    }
    if (url.includes('/chat/capabilities')) {
      return { ok: true, status: 200, json: async () => ({ agents: [], models: [], mcpServers: [] }) }
    }
    if (url.includes('/chat/send')) {
      sentTexts.push(JSON.parse(init?.body ?? '{}').text)
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
   * The reason `<agent-chat>` keys its store by the control id rather than by a fixed name.
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
   * {@code TLChatSessions} and {@code App} both call {@code useChat(chatId, boot)}. Two subscribers
   * on one id are those two elements: one session list, one transcript, one stream.
   */
  it('TLChatSessions and App on one chat-id see the same session list', async () => {
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

describe('the stream', () => {
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
    // The bootstrap's own write; what follows is what the click costs.
    const afterBootstrap = preferenceWrites.length
    expect(afterBootstrap).toBe(1)

    store.actions.showSection('progress', 4)
    await settle()

    expect(preferenceWrites).toHaveLength(afterBootstrap + 1)
    expect(preferenceWrites[afterBootstrap]).toEqual({ railOpen: true, paneWidth: 0, badgeSeen: 4 })

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
   * The rail states its own first-load state, because the stored preference defaults to "closed"
   * and a standalone rail would then show a page nothing at all.
   */
  it('applies the configured open default once', () => {
    const store = chatStore('alpha', BOOT)
    const release = store.subscribe(() => undefined)
    expect(store.getState().sideOpen).toBe(false)

    store.actions.seedRailOpenOnce(true)
    expect(store.getState().sideOpen).toBe(true)

    // The user's own toggle wins: a second seed - a remount, or a second rail - leaves it alone.
    store.actions.toggleSide(0)
    expect(store.getState().sideOpen).toBe(false)
    store.actions.seedRailOpenOnce(true)
    expect(store.getState().sideOpen).toBe(false)

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
      }),
    ).toEqual({
      windowId: 'w9',
      authenticated: true,
      userName: 'Sandra',
      welcomeName: 'Sandra',
      language: 'de',
      traceMode: 'detailed',
      railOpen: true,
      paneWidth: 480,
      badgeSeen: 3,
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
      railOpen: false,
      paneWidth: 0,
      badgeSeen: 0,
    })
  })

  /** The element's configured default is what a user who never chose starts with. */
  it('seeds the chat with the configured trace mode and the stored layout', () => {
    const store = chatStore('alpha', { ...BOOT, traceMode: 'detailed', railOpen: true, paneWidth: 480 })
    const release = store.subscribe(() => undefined)

    expect(store.getState().traceMode).toBe('detailed')
    expect(store.getState().sideOpen).toBe(true)
    expect(store.getState().paneWidth).toBe(480)

    release()
  })
})
