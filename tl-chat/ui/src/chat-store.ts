import { React } from 'tl-react-bridge'
import type { ReactNode } from 'react'
import {
  autoTitle,
  cancelRun,
  cancelSubagent,
  createSession,
  deleteSession,
  fetchCapabilities,
  fetchPendingInteractions,
  fetchRunSnapshot,
  ForbiddenError,
  listSessions,
  listSubagents,
  loadSession,
  loadSubagentTranscript,
  openEventStream,
  regenerate as regenerateTurn,
  respond,
  sendFeedback,
  sendMessage,
  setApprovalMode,
  truncate,
  turnsJsonToMessages,
} from './api'
import type { ComposerApi, PaneOpenOptions } from './extensions'
import type { ReferenceToken } from './reference'
import { bundle, providerErrorText, toLanguage, type Language } from './i18n'
import { effortLevelsOf, modelsFor } from './models'
import {
  collapsedSections,
  configuredPaneWidth,
  parsePreferences,
  sameLayout,
  savePreferences,
  type ChatPreferences,
} from './preferences'
import { parseTasksJson, type AgentTask } from './progress-model'
import { chatReducer, type ChatAction } from './reducer'
import { composeOutboundText } from './send-prefix'
import { citedSources, type CitedSource } from './sources'
import {
  EMPTY_RUN,
  readActiveSession,
  readEffort,
  readTraceMode,
  runStore,
  writeActiveSession,
  writeEffort,
  writeTraceMode,
} from './store'
import {
  promptTokens,
  type AgentOption,
  type ApprovalOutcome,
  type Attachment,
  type Capabilities,
  type ChatMessage,
  type Citation,
  type ConnectionState,
  type ContextComposition,
  type MessageFeedback,
  type ModelOption,
  type NoticeTone,
  type Rating,
  type SessionSummary,
  type SubagentActivity,
  type SubagentStatus,
  type SubagentStatusEvent,
  type TokenUsage,
  type TraceMode,
} from './types'

const { useCallback, useEffect, useMemo, useSyncExternalStore } = React

/**
 * Without a frame for this long the stream counts as stale and is re-opened.
 *
 * The server sends a heartbeat every 15s when nothing else happens, so this allows two missed ones
 * before the user is told anything - a single slow flush must not flash a warning.
 */
const STALE_AFTER_MS = 40_000

/**
 * How long a fresh connection may stay silent before the user is told it is not answering.
 *
 * The server sends `connected` as its first frame, so a live stream clears this well inside it.
 */
const CONNECT_TIMEOUT_MS = 5_000

function parseJson<T>(raw: string, fallback: T): T {
  try {
    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

/** Tool arguments arrive as a JSON string; a tool that sends something else keeps it as text. */
function parseToolInput(argsJson: string | undefined): unknown {
  if (argsJson == null) return ''
  try {
    return JSON.parse(argsJson)
  } catch {
    return argsJson
  }
}

function parseTraceMode(value: unknown): TraceMode {
  if (value === 'none' || value === 'summary' || value === 'detailed') return value
  // "overview" is the legacy name (and the vocabulary <agent-chat> still emits) - it maps to summary.
  if (value === 'overview') return 'summary'
  return 'summary'
}

function mergeSubagent(
  current: SubagentActivity[],
  event: SubagentStatusEvent,
  status: SubagentStatus,
): SubagentActivity[] {
  const index = current.findIndex((item) => item.subagentId === event.subagentId)
  const previous = index >= 0 ? current[index] : undefined
  const next: SubagentActivity = {
    subagentId: event.subagentId,
    targetAgentName: event.targetAgentName ?? previous?.targetAgentName ?? 'Subagent',
    status,
    detail: event.detail ?? previous?.detail ?? '',
    totalTokens: event.totalTokens ?? previous?.totalTokens ?? 0,
    durationMs: event.durationMs ?? previous?.durationMs ?? 0,
    hasTranscript: status === 'DONE' || previous?.hasTranscript === true,
  }
  if (index < 0) return [...current, next]
  return current.map((item, itemIndex) => (itemIndex === index ? next : item))
}

/**
 * What a control knows about the chat before the browser has asked the server anything.
 *
 * It travels with the control state (`AgentChatControl.putState`), which is why there is no `GET`
 * for the preferences - see `preferences.ts`.
 */
export interface ChatBootstrap {
  windowId: string
  /** False in a session that is not signed in; the servlet refuses every call such a session makes. */
  authenticated: boolean
  userName: string
  welcomeName: string
  language: Language
  /** The element's configured default; the user's own stored pick wins over it. */
  traceMode: TraceMode
  /** The `chat-id` the layout is stored under, which is not the store's own key - see `layoutKey`. */
  layoutKey: string
  /** Whether the user decided the rail is open; `null` leaves {@link railOpenDefault} in force. */
  railOpen: boolean | null
  /** What the element configures, for a user who has never toggled the rail. */
  railOpenDefault: boolean
  /** The width the user dragged the pane to; 0 while they never did. */
  paneWidth: number
  /** The width the element configures, for a user who has never dragged the splitter. */
  paneWidthDefault: number
  badgeSeen: number
  /** Per section name, whether the user left it open. A section never toggled is absent. */
  sectionChoices: Record<string, boolean>
  /**
   * Section ids that start collapsed. Omitted keeps today's App default ({@code context} closed).
   * Set by {@code <chat-rail/>} from each section's {@code open} flag (D3).
   */
  collapsedSections?: string[]
}

/** Reads the bootstrap a chat element pushed into its control state. */
export function readBootstrap(state: Record<string, unknown>): ChatBootstrap {
  const prefs = parsePreferences(state)
  return {
    windowId: (state.windowId as string) || '',
    authenticated: state.authenticated !== false,
    userName: (state.userName as string) || 'User',
    welcomeName: (state.welcomeName as string) || '',
    // The chat speaks the language the rest of the application speaks: the user's own locale.
    language: toLanguage(state.language),
    traceMode: parseTraceMode(state.traceMode),
    layoutKey: typeof state.layoutKey === 'string' ? state.layoutKey : '',
    railOpen: prefs.railOpen,
    // `<chat-rail open>`; every other element leaves the rail closed for a user who never opened it.
    railOpenDefault: state.open === true,
    paneWidth: prefs.paneWidth,
    paneWidthDefault: configuredPaneWidth(state),
    badgeSeen: prefs.badgeSeen,
    sectionChoices: prefs.sections,
    collapsedSections: Array.isArray(state.collapsedSections)
      ? state.collapsedSections.map((id) => String(id))
      : undefined,
  }
}

/**
 * Which store a chat element joins.
 *
 * `chat-id` is what lets separate elements find each other. `<agent-chat>` leaves it empty and
 * falls back to the control's own id, so two chats in one window stay two chats - `CopilotRegistry`
 * in Consulting keeps a *list* of live panels, and a fixed key would silently merge their
 * transcripts.
 */
export function chatIdOf(state: Record<string, unknown>, controlId: string): string {
  const configured = typeof state.chatId === 'string' ? state.chatId.trim() : ''
  return configured || controlId
}

/** Everything one chat shows, as one immutable snapshot. */
export interface ChatState {
  // Bootstrap.
  windowId: string
  authenticated: boolean
  userName: string
  welcomeName: string
  language: Language

  // Capabilities.
  caps: Capabilities
  /**
   * The models the picker may offer, narrowed to the selected agent.
   *
   * The payload lists what any agent this account may run accepts, and offering a model that only
   * another agent accepts is offering a refused turn.
   */
  offeredModels: ModelOption[]
  /** The thinking levels the selected model understands; empty when it understands none. */
  effortLevels: string[]

  // Sessions.
  sessions: SessionSummary[]
  sessionsLoading: boolean
  sessionsError: string
  sessionId: string | null
  sessionTitle: string

  // Turn settings.
  agent: string
  model: string
  mcp: string[]
  mcpDirty: boolean
  effort: string
  autoApprove: boolean
  traceMode: TraceMode

  // Transcript.
  messages: ChatMessage[]
  feedback: MessageFeedback[]
  /** Every source this chat has cited, newest run included. */
  sources: CitedSource[]

  // Run.
  busy: boolean
  regenerating: boolean
  subagents: SubagentActivity[]
  tasks: AgentTask[]
  connection: ConnectionState
  notice: string
  /** What the provider itself said, kept out of the strip but reachable as its tooltip. */
  noticeDetail: string
  /**
   * Whether the strip reports a failure or merely says what happened.
   *
   * "Abgebrochen." and "der Chat ist beschäftigt" are outcomes of what the user just did; wearing
   * the same warning accent as a provider failure made every line read as something going wrong.
   */
  noticeTone: NoticeTone

  /**
   * Whether a `<chat-rail/>` on this `chat-id` is mounted.
   *
   * The transcript's rail badge button is derived from this rather than configured: with no rail on
   * the page the button toggles something nobody can see. The rail registers itself on mount, so a
   * rail that mounts after the conversation makes the button appear then.
   */
  railMounted: boolean

  /**
   * The sum of the mounted rail's section badges.
   *
   * Published by the rail because the sections are its children and nothing else can see them. The
   * transcript's toggle button shows this minus {@link badgeSeen} and hands it back to
   * {@link ChatActions.toggleSide}, so opening the rail records what the user has now seen.
   */
  badgeTotal: number

  // Layout preferences.
  sideOpen: boolean
  /**
   * Whether {@link sideOpen} is the user's own decision.
   *
   * While it is not, the rail follows what the element configured and nothing is stored for it - so
   * a rail configured open still opens in a new tab, and one the user closed stays closed.
   */
  railDecided: boolean
  /** The pane width in force: what the user dragged to, else what the element configured. */
  paneWidth: number
  badgeSeen: number
  paneContent: ReactNode
  paneOptions: PaneOpenOptions | null
  /** The sections showing as collapsed: the configured defaults, overruled per name by the user. */
  collapsedSections: string[]
  /** Per section name, whether the user left it open. A section never toggled is absent. */
  sectionChoices: Record<string, boolean>
}

/** What a send needs from the element that triggered it. */
export interface SendOptions {
  /** Whether the text replaces a message the user changed. */
  edited?: boolean
  /** The host's `<context>` prefix; a function of the transcript, so it is evaluated on send. */
  contextTag?: (arg: { messages: ChatMessage[] }) => string | undefined
}

/** Everything a chat element may do to the chat it joined. */
export interface ChatActions {
  dispatch(action: ChatAction): void
  /**
   * Shows one line above the composer, and what stands behind it.
   *
   * Text and detail are set together so a later notice never inherits the tooltip of an earlier
   * one.
   */
  showNotice(text: string, detail?: string, options?: { tone?: NoticeTone; sticky?: boolean }): void
  selectSession(id: string): Promise<void>
  newChat(): void
  deleteSession(id: string): Promise<void>
  send(text: string, attachments: Attachment[], options?: SendOptions): Promise<boolean>
  editSend(messageId: string, turnIndex: number, text: string, options?: SendOptions): Promise<boolean>
  regenerate(messageId: string, turnIndex: number): Promise<void>
  rate(turnIndex: number, rating: Rating, note?: string): Promise<void>
  stop(): void
  selectAgent(name: string): void
  selectModel(id: string): void
  toggleMcp(name: string): void
  /**
   * Attaches exactly these MCP servers, as a whole set rather than one toggle at a time.
   *
   * What a host writes onto the `mcp-servers` channel: it states the attachment, so it counts as an
   * override of the agent's own declaration the same way a toggle does.
   */
  selectMcpServers(names: string[]): void
  selectEffort(level: string): void
  selectTraceMode(mode: TraceMode): void
  toggleAutoApprove(): Promise<void>
  cancelSubagent(id: string): Promise<void>
  stopSubagents(): Promise<void>
  loadSubagentTranscript(id: string): Promise<ChatMessage[]>
  openPane(content: ReactNode, options: PaneOpenOptions): void
  closePane(): void
  /** Closes whatever the side is showing - what a click beside a sheet means. */
  closeSide(): void
  toggleSide(badgeTotal: number): void
  /** Opens the rail on one section, as a click on its strip icon does. */
  showSection(id: string, badgeTotal: number): void
  setPaneWidth(width: number): void
  toggleSection(id: string): void
  /**
   * States which sections the rail element configures as collapsed.
   *
   * Applied per section name and only where the user has decided nothing, so it is idempotent: a
   * remount re-states the same defaults and leaves every section the user toggled alone.
   */
  applyCollapsedDefaults(ids: string[]): void
  /**
   * States the rail element's configured {@code open} flag.
   *
   * A user who has never toggled the rail gets it; one who has keeps their own choice, in this tab
   * and in the next one. Idempotent, for the same reason as {@link applyCollapsedDefaults}.
   */
  applyRailDefault(open: boolean): void
  /** Publishes the sum of the rail's section badges; see {@link ChatState.badgeTotal}. */
  publishBadgeTotal(total: number): void
  /**
   * Records that the user has now looked at this conversation.
   *
   * The same watermark {@link toggleSide} moves when the rail is opened: what is counted as new is
   * {@link ChatState.badgeTotal} minus {@link ChatState.badgeSeen}, so "seen" is the total as it
   * stands, never zero - a section that reports nothing must not turn into news the next time one
   * does. This is the half of `toggleSide` that a `<chat-dock/>` needs: opening the dock is the
   * user looking, but it is not the user opening the rail.
   */
  markBadgesSeen(badgeTotal: number): void
  /**
   * Announces a mounted rail; the returned function unregisters it again.
   *
   * Counted rather than flagged, because React remounts an effect on the same component in
   * development and two rails on one `chat-id` are not forbidden.
   */
  registerRail(): () => void
  bindComposer(api: ComposerApi | null): void
  insertReference(token: ReferenceToken): void
}

/**
 * One `useEffect` of the component this store replaces.
 *
 * Keeping them as declared dependency lists rather than as calls scattered through the actions is
 * what makes "the same thing happens, at the same moments" reviewable: each entry below is the
 * effect it was lifted from, deps included.
 */
interface StoreEffect {
  deps(state: ChatState): unknown[]
  run(): (() => void) | void
  last?: unknown[]
  cleanup?: () => void
}

function sameDeps(a: unknown[] | undefined, b: unknown[]): boolean {
  if (!a || a.length !== b.length) return false
  return a.every((value, index) => Object.is(value, b[index]))
}

function sameList<T>(a: readonly T[], b: readonly T[]): boolean {
  return a.length === b.length && a.every((value, index) => Object.is(value, b[index]))
}

/** Keeps the previous result while the inputs are unchanged, as `useMemo` does. */
function memo<A extends unknown[], T>(compute: (...deps: A) => T): (...deps: A) => T {
  let last: unknown[] | undefined
  let value: T
  return (...deps: A) => {
    if (!sameDeps(last, deps)) {
      last = deps
      value = compute(...deps)
    }
    return value
  }
}

/**
 * The state of one chat, and the only thing that changes it.
 *
 * Lifted out of `App` so that several view elements can show one conversation: the chat bundle is
 * a single ES module instance in the browser, so a module-scoped store keyed by `chat-id` is one
 * object for every element on the page. Nothing here renders; `paneContent` is the host's node,
 * carried rather than read.
 */
export class ChatStore {
  private listeners = new Set<() => void>()

  private refs = 0

  /** Whether any element is watching. The effects run only while one is. */
  private live = false

  private snapshot: ChatState

  private effects: StoreEffect[]

  private flushing = false

  /** The assistant message the current turn streams into, cleared when the run ends. */
  private assistantId: string | null = null

  /**
   * The assistant message this run streamed into, still reachable after the run has ended.
   *
   * The frames arrive as `text_delta …, turn_completed, run_completed, message_complete`, so by the
   * time the authoritative text lands, `assistantId` has already been cleared by the run's end.
   * Applying it needs an id that outlives that.
   */
  private lastAssistantId: string | null = null

  private runId: string | null = null

  /** Set when the run ended because the user stopped it, so the provider error it causes is muted. */
  private cancelled = false

  /** Whether the line now on screen must survive the next run start. */
  private noticeSticky = false

  /**
   * The chat this window had open before it was reloaded, read before anything can overwrite it.
   *
   * The store persists every change of the open chat, and the first of those would be the initial
   * `null` - so the remembered id has to be taken when the store is built, not once it runs.
   */
  private readonly remembered: string | null

  /** Whether the remembered chat has been dealt with; restoring happens once per store. */
  private restored = false

  /** The width the element configures, kept apart from the width the user dragged to. */
  private readonly paneWidthDefault: number

  /** The `chat-id` the layout is stored under; the store's own key may be a control id. */
  private readonly layoutKey: string

  /** How many {@code <chat-rail/>} elements on this `chat-id` are mounted. */
  private rails = 0

  /**
   * The layout the server holds, so an unchanged layout is not written back.
   *
   * The bootstrap is the stored layout; a `PUT` of it would be a no-op at best and, when the page
   * booted from a stale control state, would overwrite the user's real choice.
   */
  private storedLayout: ChatPreferences

  /** What the user decided per fact, which is what is stored - never what the page happens to show. */
  private get layout(): ChatPreferences {
    const { railDecided, sideOpen, badgeSeen, sectionChoices } = this.state
    return {
      railOpen: railDecided ? sideOpen : null,
      paneWidth: this.paneWidthStored,
      badgeSeen,
      sections: sectionChoices,
    }
  }

  /** The width the user dragged the pane to; 0 while they never did. */
  private paneWidthStored: number

  private composerApi: ComposerApi | null = null

  private readonly offeredModelsOf = memo(
    (models: ModelOption[], agents: AgentOption[], agent: string): ModelOption[] =>
      modelsFor(models, agents.find((item) => item.name === agent)?.model),
  )

  private readonly effortLevelsOf = memo((models: ModelOption[], model: string): string[] =>
    effortLevelsOf(models, model),
  )

  private readonly sourcesOf = memo((messages: ChatMessage[]): CitedSource[] => citedSources(messages))

  readonly actions: ChatActions

  private readonly chatId: string

  constructor(chatId: string, boot: ChatBootstrap) {
    this.chatId = chatId
    this.remembered = readActiveSession()
    this.layoutKey = boot.layoutKey
    this.paneWidthDefault = boot.paneWidthDefault
    this.paneWidthStored = boot.paneWidth
    this.storedLayout = {
      railOpen: boot.railOpen,
      paneWidth: boot.paneWidth,
      badgeSeen: boot.badgeSeen,
      sections: boot.sectionChoices,
    }
    this.snapshot = this.derive({
      windowId: boot.windowId,
      authenticated: boot.authenticated,
      userName: boot.userName,
      welcomeName: boot.welcomeName,
      language: boot.language,
      caps: { agents: [], models: [], mcpServers: [] },
      offeredModels: [],
      effortLevels: [],
      sessions: [],
      sessionsLoading: true,
      sessionsError: '',
      sessionId: null,
      sessionTitle: '',
      agent: '',
      model: '',
      mcp: [],
      mcpDirty: false,
      effort: readEffort() ?? '',
      autoApprove: false,
      // The stored choice wins over the configured default: the element's trace-mode is what a new
      // user starts with, not a setting that overrules what this user picked.
      traceMode: readTraceMode() ?? boot.traceMode,
      messages: [],
      feedback: [],
      sources: [],
      busy: false,
      regenerating: false,
      subagents: [],
      tasks: [],
      connection: 'connecting',
      notice: '',
      noticeDetail: '',
      noticeTone: 'warn',
      // The user's own decision, else what the element configured. The one is stored, the other is
      // not - which is the whole of the precedence rule (#423).
      sideOpen: boot.railOpen ?? boot.railOpenDefault,
      railDecided: boot.railOpen !== null,
      paneWidth: boot.paneWidth > 0 ? boot.paneWidth : boot.paneWidthDefault,
      badgeSeen: boot.badgeSeen,
      paneContent: null,
      paneOptions: null,
      // `App`'s own default is Kontext collapsed; a `<chat-rail/>` states its sections' own flags
      // through `applyCollapsedDefaults` as soon as it mounts.
      collapsedSections: collapsedSections(boot.collapsedSections ?? ['context'], boot.sectionChoices),
      sectionChoices: boot.sectionChoices,
      railMounted: false,
      badgeTotal: 0,
    })
    this.effects = this.declareEffects()
    this.actions = this.declareActions()
  }

  getState = (): ChatState => this.snapshot

  subscribe = (listener: () => void): (() => void) => {
    this.listeners.add(listener)
    this.refs += 1
    if (this.refs === 1) {
      this.live = true
      // Register here, not in the factory: a render React throws away never subscribed, and
      // must not occupy the map. Do not displace a newer store that already owns this id.
      const owner = CHATS.get(this.chatId)
      if (owner == null || owner === this) CHATS.set(this.chatId, this)
      if (BUILDING.get(this.chatId) === this) BUILDING.delete(this.chatId)
    }
    let released = false
    return () => {
      if (released) return
      released = true
      this.listeners.delete(listener)
      this.refs -= 1
      if (this.refs <= 0) this.quiesce()
    }
  }

  /**
   * Takes the bootstrap a mounting element brought, and runs whatever that started.
   *
   * Idempotent: a second element on the same `chat-id` brings the same values, so nothing changes
   * and no effect re-runs. Values that really differ - a window id arriving late, a sign-in - are
   * what the effect dependencies below react to, exactly as the component's did.
   */
  attach = (boot: ChatBootstrap): void => {
    this.patch({
      windowId: boot.windowId,
      authenticated: boot.authenticated,
      userName: boot.userName,
      welcomeName: boot.welcomeName,
      language: boot.language,
    })
    this.schedule()
  }

  // --- state plumbing ----------------------------------------------------------------------

  private derive(base: ChatState): ChatState {
    const offeredModels = this.offeredModelsOf(base.caps.models, base.caps.agents, base.agent)
    const effortLevels = this.effortLevelsOf(base.caps.models, base.model)
    const sources = this.sourcesOf(base.messages)
    const sessionTitle =
      base.sessions.find((session) => session.id === base.sessionId)?.title ||
      bundle(base.language).newChat
    return { ...base, offeredModels, effortLevels, sources, sessionTitle }
  }

  private patch(next: Partial<ChatState>): void {
    const merged = this.derive({ ...this.snapshot, ...next })
    if (Object.keys(merged).every((key) => sameField(merged, this.snapshot, key as keyof ChatState))) {
      return
    }
    this.snapshot = merged
    this.listeners.forEach((listener) => listener())
    this.schedule()
  }

  private get state(): ChatState {
    return this.snapshot
  }

  private get labels() {
    return bundle(this.snapshot.language)
  }

  /**
   * Runs the effects once the synchronous work is over.
   *
   * A microtask rather than a call per setter, because a handler that changes three things - open
   * the rail, mark the badges seen, close the pane - must produce the one `PUT /chat/preferences`
   * React's batching produced, not three.
   */
  private schedule(): void {
    if (this.flushing || !this.live) return
    this.flushing = true
    queueMicrotask(() => {
      this.flushing = false
      if (this.live) this.runEffects()
    })
  }

  private runEffects(): void {
    for (const effect of this.effects) {
      const deps = effect.deps(this.snapshot)
      if (sameDeps(effect.last, deps)) continue
      effect.last = deps
      effect.cleanup?.()
      effect.cleanup = effect.run() ?? undefined
    }
  }

  /**
   * The last element has left: the stream closes and the chat is dropped.
   *
   * Everything is put back as it was declared rather than marked dead, because the same instance
   * can be handed a new subscriber - React remounts an effect on the same component in development
   * - and a chat that came back mute would be worse than one that reconnects.
   */
  private quiesce(): void {
    if (!this.live) return
    this.live = false
    this.composerApi = null
    if (CHATS.get(this.chatId) === this) CHATS.delete(this.chatId)
    if (BUILDING.get(this.chatId) === this) BUILDING.delete(this.chatId)
    for (const effect of this.effects) {
      effect.cleanup?.()
      effect.cleanup = undefined
      effect.last = undefined
    }
    this.listeners.clear()
  }

  // --- effects -----------------------------------------------------------------------------

  private declareEffects(): StoreEffect[] {
    return [
      // The layout preferences, written by the store and by nothing else: one writer per chat, so
      // several elements showing one chat cannot race on the PersonalConfiguration key.
      {
        deps: (s) => [
          s.authenticated,
          s.windowId,
          s.railDecided,
          s.sideOpen,
          s.paneWidth,
          s.badgeSeen,
          s.sectionChoices,
        ],
        run: () => {
          const { authenticated, windowId } = this.state
          if (!authenticated || !windowId) return
          const next = this.layout
          // What the page booted with is what the server already holds. Writing it back would make
          // every reload store the layout the page happened to start with, which is how the stored
          // choice used to be lost (#423).
          if (sameLayout(next, this.storedLayout)) return
          this.storedLayout = next
          void savePreferences(windowId, this.layoutKey, next)
        },
      },
      {
        deps: (s) => [s.sessionId],
        run: () => writeActiveSession(this.state.sessionId),
      },
      {
        deps: (s) => [s.authenticated, s.windowId],
        run: () => this.loadCapabilities(),
      },
      {
        deps: (s) => [s.authenticated, s.windowId],
        run: () => this.connect(),
      },
      // The ring must be readable before the first run, and must follow a model change at once, so
      // the window comes from the model catalogue until a run reports the one it really used.
      {
        deps: (s) => [s.caps.models, s.model],
        run: () => {
          const { caps, model } = this.state
          const known = caps.models.find((item) => item.id === model)
          if (!known?.contextWindow) return
          runStore.set((snapshot) => ({
            ...snapshot,
            model: model || snapshot.model,
            contextWindow: known.contextWindow ?? snapshot.contextWindow,
          }))
        },
      },
      // A model that cannot be asked to think harder must not carry a level: the provider would
      // reject it. Switching to such a model drops the choice; switching back leaves the user to
      // pick again.
      {
        deps: (s) => [s.effortLevels],
        run: () => {
          const { effort, effortLevels } = this.state
          if (effort && !effortLevels.includes(effort)) this.patch({ effort: '' })
        },
      },
      {
        deps: (s) => [s.effort],
        run: () => runStore.set((snapshot) => ({ ...snapshot, effort: this.state.effort })),
      },
      {
        deps: (s) => [s.authenticated, s.sessionsLoading, s.sessions],
        run: () => this.restoreRememberedSession(),
      },
    ]
  }

  private loadCapabilities(): (() => void) | void {
    // Nothing to ask for in a session the servlet answers with 401.
    if (!this.state.authenticated) return
    const windowId = this.state.windowId
    let cancelled = false
    void (async () => {
      try {
        const [nextCaps, nextSessions] = await Promise.all([
          fetchCapabilities(windowId),
          listSessions(windowId),
        ])
        if (cancelled) return
        // An agent already chosen wins over the entry point: the `agent` channel may have named one
        // before this answer arrived, and so may the user. Re-applying it against the fresh catalogue
        // is what gives it its model and its servers, which it could not have had until now.
        const chosen = nextCaps.agents.find((a) => a.name === this.state.agent)?.name
        const initial =
          chosen ??
          nextCaps.agents.find((a) => a.name === nextCaps.entryPointAgent)?.name ??
          nextCaps.agents[0]?.name ??
          ''
        this.patch({ caps: nextCaps, sessions: nextSessions, sessionsError: '' })
        if (initial) this.applyAgent(initial, nextCaps.agents)
        else if (nextCaps.models[0]) this.patch({ model: nextCaps.models[0].id })
      } catch (err) {
        console.error('[tl-chat-ui] capabilities', err)
        if (!cancelled) this.patch({ sessionsError: this.labels.chatsLoadFailed })
      } finally {
        if (!cancelled) this.patch({ sessionsLoading: false })
      }
    })()
    return () => {
      cancelled = true
    }
  }

  /**
   * Re-opens the chat this window had before it was reloaded.
   *
   * A reload used to land on an empty "new chat" with the conversation only findable in the rail,
   * which reads like data loss. It runs once, and only for a chat that is still in this user's
   * list - a chat deleted meanwhile, or one remembered from another account, is forgotten instead
   * of asked for.
   */
  private restoreRememberedSession(): void {
    const { authenticated, sessionsLoading, sessions } = this.state
    if (!authenticated || sessionsLoading || this.restored) return
    this.restored = true
    const id = this.remembered
    if (!id) return
    if (!sessions.some((item) => item.id === id)) {
      writeActiveSession(null)
      return
    }
    void this.actions.selectSession(id)
  }

  // --- the stream --------------------------------------------------------------------------

  /**
   * The chat's one `EventSource`, opened by the first element that joined this chat and closed by
   * the last one to leave.
   */
  private connect(): (() => void) | void {
    // An EventSource against a 401 reconnects for as long as the page is open; a session that is
    // not signed in opens none.
    if (!this.state.authenticated) return
    const windowId = this.state.windowId
    let closed = false
    let source: EventSource | null = null
    let staleTimer = 0
    let reopenTimer = 0
    let connectTimer = 0

    const finish = (error?: string, detail?: string, errorTone: NoticeTone = 'warn') => {
      if (this.assistantId) {
        this.dispatch({ type: 'FINALIZE_TEXT', id: this.assistantId })
        this.assistantId = null
      }
      if (this.runId) {
        this.dispatch({ type: 'END_RUN', runId: this.runId })
        this.runId = null
      }
      if (error) this.showNotice(error, detail ?? '', { tone: errorTone })
      this.patch({ busy: false })
      runStore.set((s) => ({ ...s, busy: false }))
    }

    /**
     * A frame arrived: the stream is alive, and the stale clock starts again.
     *
     * Only ever called from the `open` handler and from real frames. Calling it because a
     * connection was *attempted* is what made the reconnect state lie: the chip appeared when the
     * stale timer fired and vanished a second later when the next attempt was made, whether or not
     * anything answered.
     */
    const markAlive = () => {
      this.patch({ connection: 'live' })
      window.clearTimeout(connectTimer)
      window.clearTimeout(staleTimer)
      staleTimer = window.setTimeout(() => {
        this.patch({ connection: 'reconnecting' })
        reopen()
      }, STALE_AFTER_MS)
    }

    const reopen = () => {
      if (closed) return
      source?.close()
      window.clearTimeout(reopenTimer)
      reopenTimer = window.setTimeout(open, 1000)
    }

    const open = () => {
      if (closed) return
      const es = openEventStream(windowId)
      source = es
      const on = (name: string, fn: (e: MessageEvent) => void) =>
        es.addEventListener(name, ((event: MessageEvent) => {
          markAlive()
          fn(event)
        }) as EventListener)

      const setTokens = (windowSize?: number, modelName?: string, usage?: TokenUsage) => {
        runStore.set((s) => ({
          ...s,
          contextWindow: windowSize || s.contextWindow,
          model: modelName || s.model,
          lastUsage: usage ?? s.lastUsage,
          measuredTokens: usage ? promptTokens(usage) || s.measuredTokens : s.measuredTokens,
        }))
      }

      es.addEventListener('open', markAlive)
      on('connected', () => undefined)
      on('heartbeat', () => undefined)

      on('run_started', (e) => {
        const data = parseJson<{ runId?: string; usedTokens?: number; contextWindow?: number; model?: string }>(
          e.data,
          {},
        )
        this.patch({ busy: true })
        this.clearNoticeOnRunStart()
        this.cancelled = false
        this.runId = data.runId ?? crypto.randomUUID()
        this.dispatch({ type: 'START_RUN', runId: this.runId })
        runStore.set((s) => ({
          ...s,
          busy: true,
          estimatedTokens: data.usedTokens ?? s.estimatedTokens,
          contextWindow: data.contextWindow || s.contextWindow,
          model: data.model || s.model,
        }))
      })
      on('turn_started', (e) => {
        const data = parseJson<{ usedTokens?: number; contextWindow?: number }>(e.data, {})
        runStore.set((s) => ({
          ...s,
          estimatedTokens: data.usedTokens ?? s.estimatedTokens,
          contextWindow: data.contextWindow || s.contextWindow,
        }))
        if (this.assistantId) this.dispatch({ type: 'FINALIZE_TEXT', id: this.assistantId })
        this.assistantId = crypto.randomUUID()
        this.lastAssistantId = this.assistantId
        this.dispatch({ type: 'ADD_ASSISTANT', id: this.assistantId, text: '', isStreaming: true })
      })
      on('context_composition', (e) => {
        const data = parseJson<Partial<ContextComposition>>(e.data, {})
        runStore.set((s) => ({
          ...s,
          composition: {
            systemPromptTokens: data.systemPromptTokens ?? 0,
            toolDefinitionTokens: data.toolDefinitionTokens ?? 0,
            messageTokens: data.messageTokens ?? 0,
            toolResultTokens: data.toolResultTokens ?? 0,
            toolCount: data.toolCount ?? 0,
          },
        }))
      })
      on('text_delta', (e) => {
        if (!this.assistantId) return
        const data = parseJson<{ text?: string }>(e.data, {})
        if (data.text) this.dispatch({ type: 'APPEND_TEXT', id: this.assistantId, delta: data.text })
      })
      on('thinking_delta', (e) => {
        const data = parseJson<{ text?: string }>(e.data, {})
        if (data.text) this.dispatch({ type: 'APPEND_THINKING', delta: data.text })
      })
      on('tool_started', (e) => {
        const data = parseJson<{ toolCallId?: string; toolName?: string; argsJson?: string }>(e.data, {})
        if (!data.toolCallId) return
        this.dispatch({
          type: 'ADD_TOOL',
          toolUseId: data.toolCallId,
          name: data.toolName ?? 'tool',
          input: parseToolInput(data.argsJson),
        })
      })
      on('tool_completed', (e) => {
        const data = parseJson<{ toolCallId?: string; result?: string; durationMs?: number }>(e.data, {})
        if (!data.toolCallId) return
        this.dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'done', durationMs: data.durationMs })
        this.dispatch({ type: 'ADD_TOOL_RESULT', toolUseId: data.toolCallId, output: data.result ?? '' })
      })
      on('tool_failed', (e) => {
        const data = parseJson<{ toolCallId?: string; errorMessage?: string; durationMs?: number }>(e.data, {})
        if (!data.toolCallId) return
        this.dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'error', durationMs: data.durationMs })
        this.dispatch({
          type: 'ADD_TOOL_RESULT',
          toolUseId: data.toolCallId,
          output: data.errorMessage ?? 'failed',
          isError: true,
        })
      })
      on('tool_denied', (e) => {
        const data = parseJson<{ toolCallId?: string; reason?: string }>(e.data, {})
        if (!data.toolCallId) return
        this.dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'error' })
        this.dispatch({
          type: 'ADD_TOOL_RESULT',
          toolUseId: data.toolCallId,
          output: data.reason ?? 'denied',
          isError: true,
        })
      })
      on('task_update', (e) => {
        const data = parseJson<{ tasksJson?: string }>(e.data, {})
        this.patch({ tasks: parseTasksJson(data.tasksJson) })
      })
      on('approval_mode', (e) => {
        const data = parseJson<{ mode?: string }>(e.data, {})
        this.patch({ autoApprove: data.mode === 'AUTO_APPROVE' })
      })
      on('approval_required', (e) => {
        const data = parseJson<{
          approvalRequestId?: string
          toolCallId?: string
          toolName?: string
          argsJson?: string
          displayReason?: string
          toolMode?: string
          remainingMs?: number
        }>(e.data, {})
        if (!data.approvalRequestId) return
        if (this.state.autoApprove) {
          void respond(windowId, {
            requestId: data.approvalRequestId,
            response: 'APPROVED',
            toolName: data.toolName,
            argsJson: data.argsJson,
          })
          return
        }
        this.dispatch({
          type: 'ADD_APPROVAL',
          approvalRequestId: data.approvalRequestId,
          toolCallId: data.toolCallId ?? '',
          toolName: data.toolName ?? 'tool',
          argsJson: data.argsJson ?? '',
          displayReason: data.displayReason ?? '',
          toolMode: data.toolMode,
          remainingMs: data.remainingMs,
        })
      })
      on('approval_resolved', (e) => {
        const data = parseJson<{ approvalRequestId?: string; outcome?: string }>(e.data, {})
        if (!data.approvalRequestId) return
        this.dispatch({
          type: 'RESOLVE_APPROVAL',
          approvalRequestId: data.approvalRequestId,
          outcome: (data.outcome as ApprovalOutcome) ?? 'APPROVED',
        })
      })
      on('user_interaction', (e) => {
        const data = parseJson<{
          requestId?: string
          question?: string
          header?: string
          type?: 'text' | 'choice'
          options?: { label: string; description?: string }[]
          multiSelect?: boolean
          remainingMs?: number
        }>(e.data, {})
        if (!data.requestId) return
        this.dispatch({
          type: 'ADD_INTERACTION',
          requestId: data.requestId,
          question: data.question ?? '',
          header: data.header,
          interactionType: data.type,
          options: data.options,
          multiSelect: data.multiSelect,
          remainingMs: data.remainingMs,
        })
      })
      on('delegation_event', (e) => {
        const wrap = parseJson<{ eventType?: string; eventData?: string }>(e.data, {})
        const data = parseJson<{
          toolCallId?: string
          toolName?: string
          argsJson?: string
          result?: string
          errorMessage?: string
          durationMs?: number
          reason?: string
          approvalRequestId?: string
          displayReason?: string
          toolMode?: string
          remainingMs?: number
        }>(wrap.eventData ?? '', {})
        switch (wrap.eventType) {
          case 'tool_started': {
            if (!data.toolCallId) break
            this.dispatch({
              type: 'ADD_TOOL',
              toolUseId: data.toolCallId,
              name: data.toolName ?? 'tool',
              input: parseToolInput(data.argsJson),
              depth: 1,
            })
            break
          }
          case 'tool_completed':
            if (!data.toolCallId) break
            this.dispatch({
              type: 'UPDATE_TOOL',
              toolUseId: data.toolCallId,
              status: 'done',
              durationMs: data.durationMs,
            })
            this.dispatch({ type: 'ADD_TOOL_RESULT', toolUseId: data.toolCallId, output: data.result ?? '' })
            break
          case 'tool_failed':
            if (!data.toolCallId) break
            this.dispatch({
              type: 'UPDATE_TOOL',
              toolUseId: data.toolCallId,
              status: 'error',
              durationMs: data.durationMs,
            })
            this.dispatch({
              type: 'ADD_TOOL_RESULT',
              toolUseId: data.toolCallId,
              output: data.errorMessage ?? 'failed',
              isError: true,
            })
            break
          case 'tool_denied':
            if (!data.toolCallId) break
            this.dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'error' })
            this.dispatch({
              type: 'ADD_TOOL_RESULT',
              toolUseId: data.toolCallId,
              output: data.reason ?? 'denied',
              isError: true,
            })
            break
          case 'approval_required':
            if (!data.approvalRequestId) break
            if (this.state.autoApprove) {
              void respond(windowId, {
                requestId: data.approvalRequestId,
                response: 'APPROVED',
                toolName: data.toolName,
                argsJson: data.argsJson,
              })
              break
            }
            this.dispatch({
              type: 'ADD_APPROVAL',
              approvalRequestId: data.approvalRequestId,
              toolCallId: data.toolCallId ?? '',
              toolName: data.toolName ?? 'tool',
              argsJson: data.argsJson ?? '',
              displayReason: data.displayReason ?? '',
              toolMode: data.toolMode,
              remainingMs: data.remainingMs,
            })
            break
          default:
            break
        }
      })
      on('subagent_started', (e) => {
        const data = parseJson<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) this.patch({ subagents: mergeSubagent(this.state.subagents, data, 'RUNNING') })
      })
      on('subagent_completed', (e) => {
        const data = parseJson<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) this.patch({ subagents: mergeSubagent(this.state.subagents, data, 'DONE') })
      })
      on('subagent_failed', (e) => {
        const data = parseJson<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) this.patch({ subagents: mergeSubagent(this.state.subagents, data, 'FAILED') })
      })
      on('subagent_cancelled', (e) => {
        const data = parseJson<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) this.patch({ subagents: mergeSubagent(this.state.subagents, data, 'CANCELLED') })
      })
      on('compaction_started', (e) => {
        const data = parseJson<{ contextWindow?: number }>(e.data, {})
        // The measured figure describes a conversation that no longer exists; the next turn
        // reports the compacted one, and until then the ring falls back to the estimate.
        runStore.set((s) => ({ ...s, measuredTokens: 0, contextWindow: data.contextWindow || s.contextWindow }))
      })
      on('turn_completed', (e) => {
        const data = parseJson<{ turnUsage?: TokenUsage; contextWindow?: number }>(e.data, {})
        if (data.turnUsage) setTokens(data.contextWindow, undefined, data.turnUsage)
      })
      on('run_completed', (e) => {
        const data = parseJson<{ reason?: string }>(e.data, {})
        // A run the user stopped is not a failure: it ends quietly, in their language, and the
        // provider's own "Stream cancelled." that follows is swallowed below.
        if (data.reason === 'ABORTED') {
          this.cancelled = true
          finish(this.labels.cancelled, '', 'info')
        } else {
          finish()
        }
      })
      on('message_complete', (e) => {
        // The authoritative answer. The deltas were a live preview of it; applying it here is what
        // keeps the last fragment from being lost when it arrives after this event.
        const data = parseJson<{ contentText?: string; citations?: Citation[] }>(e.data, {})
        const target = this.assistantId ?? this.lastAssistantId
        if (target && data.contentText) {
          this.dispatch({ type: 'SET_TEXT', id: target, text: data.contentText, citations: data.citations })
        }
        finish()
      })
      on('provider_error', (e) => {
        if (this.cancelled) {
          // The cancel already said what happened; the provider's report of the closed stream adds
          // nothing and would say it as an error.
          this.cancelled = false
          return
        }
        const data = parseJson<{ errorMessage?: string; kind?: string }>(e.data, {})
        finish(providerErrorText(data.kind, this.state.language), data.errorMessage ?? '')
      })

      es.onerror = () => {
        if (es.readyState === EventSource.CLOSED) {
          this.patch({ connection: 'reconnecting' })
          reopen()
        }
      }

      // Not alive until something answers: if no frame arrives within this, say so and keep saying
      // it until one does.
      window.clearTimeout(connectTimer)
      connectTimer = window.setTimeout(() => this.patch({ connection: 'reconnecting' }), CONNECT_TIMEOUT_MS)
    }

    open()
    return () => {
      closed = true
      window.clearTimeout(staleTimer)
      window.clearTimeout(reopenTimer)
      window.clearTimeout(connectTimer)
      source?.close()
    }
  }

  // --- actions -----------------------------------------------------------------------------

  private dispatch = (action: ChatAction): void => {
    this.patch({ messages: chatReducer(this.snapshot.messages, action) })
  }

  private showNotice = (
    text: string,
    detail = '',
    options: { tone?: NoticeTone; sticky?: boolean } = {},
  ): void => {
    this.patch({ notice: text, noticeDetail: detail, noticeTone: options.tone ?? 'warn' })
    this.noticeSticky = text !== '' && options.sticky === true
  }

  /**
   * Takes the strip down as a run begins, unless what it says has not been readable yet.
   *
   * A run start used to clear it unconditionally, which is how "3 spätere Antworten entfernt" was
   * wiped in the same instant it was written: the send that follows an edit is what starts the run
   * the line was meant to survive. It survives exactly that one start, and then stays until the
   * user dismisses it, sends again, or a further run begins.
   */
  private clearNoticeOnRunStart(): void {
    if (this.noticeSticky) {
      this.noticeSticky = false
      return
    }
    this.showNotice('')
  }

  private applyAgent(name: string, agents: AgentOption[]): void {
    const found = agents.find((a) => a.name === name)
    this.patch({
      agent: name,
      mcp: found?.mcpServers ?? [],
      mcpDirty: false,
      ...(found?.model ? { model: found.model } : {}),
    })
  }

  private async refreshSessions(): Promise<void> {
    try {
      this.patch({ sessions: await listSessions(this.state.windowId), sessionsError: '' })
    } catch {
      this.patch({ sessionsError: this.labels.chatsRefreshFailed })
    }
  }

  /**
   * Puts the run this chat may still be in back on screen.
   *
   * The transcript travels with the session, but the stream replays nothing: after a reload the
   * progress list is empty, the composer is idle and an open approval card is gone, while the agent
   * is still working. `GET /chat/updates` says whether it is and what it has got through;
   * `GET /chat/interactions` says what is waiting for this user. Both are best effort - a chat that
   * shows only what the transcript says is what a failure here leaves behind, which is what every
   * reload used to look like.
   */
  private async recoverRunState(sessionId: string): Promise<void> {
    const windowId = this.state.windowId
    const [snapshot, pending] = await Promise.all([
      fetchRunSnapshot(windowId).catch(() => null),
      fetchPendingInteractions(windowId).catch(() => []),
    ])
    // The user may have moved to another chat while this was in flight.
    if (this.state.sessionId !== sessionId) return
    if (snapshot) {
      this.patch({ busy: snapshot.processing, tasks: parseTasksJson(snapshot.tasksJson) })
    }
    for (const item of pending) {
      if (item.type !== 'approval' || item.sessionId !== sessionId) continue
      // An approval past its deadline can no longer be answered: the agent has stopped waiting for
      // it, and a card nobody is listening to would lock the composer behind a decision that leads
      // nowhere.
      if (item.remainingMs <= 0) continue
      const known = this.state.messages.some(
        (message) =>
          message.type === 'approval_request' && message.approvalRequestId === item.requestId,
      )
      if (known) continue
      this.dispatch({
        type: 'ADD_APPROVAL',
        approvalRequestId: item.requestId,
        toolCallId: '',
        toolName: item.header,
        argsJson: item.payload,
        displayReason: item.prompt,
        remainingMs: item.remainingMs,
      })
    }
  }

  private async refreshSubagents(): Promise<void> {
    try {
      this.patch({ subagents: await listSubagents(this.state.windowId) })
    } catch {
      this.patch({ subagents: [] })
    }
  }

  /**
   * Takes back a chat this send had just created, when the send produced nothing to keep.
   *
   * The chat comes into being before the request, because it is what the turn is written into. A
   * refused send leaves it empty, and an empty chat in the rail is a row the user has to notice and
   * tidy away - for a refusal they did not cause and cannot act on. Only a chat this very send
   * created is taken back: an existing conversation is never touched.
   */
  private async discardEmptyChat(createdHere: boolean, id: string): Promise<void> {
    if (!createdHere || !id) return
    try {
      await deleteSession(this.state.windowId, id)
    } catch {
      // Best effort: an undeleted empty chat is untidy, not broken.
    }
    this.patch({ sessionId: '' })
    this.dispatch({ type: 'RESET' })
    await this.refreshSessions()
  }

  private declareActions(): ChatActions {
    return {
      dispatch: this.dispatch,
      showNotice: this.showNotice,

      selectSession: async (id: string) => {
        const windowId = this.state.windowId
        this.assistantId = null
        this.runId = null
        this.patch({
          busy: false,
          subagents: [],
          tasks: [],
          feedback: [],
          notice: '',
          noticeDetail: '',
          noticeTone: 'warn',
        })
        this.noticeSticky = false
        // The model does not change when the chat does, so its window survives the reset; only what
        // the previous conversation measured is dropped.
        runStore.set((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
        const detail = await loadSession(windowId, id)
        this.patch({
          sessionId: id,
          messages: turnsJsonToMessages(detail.turnsJson),
          feedback: detail.feedback ?? [],
          ...(detail.lastEffort ? { effort: detail.lastEffort } : {}),
        })
        // A re-opened session shows what its last request really cost, not a fresh estimate.
        runStore.set((snapshot) => ({
          ...snapshot,
          model: detail.lastModel || snapshot.model,
          contextWindow: detail.contextWindow || snapshot.contextWindow,
          lastUsage: detail.lastPromptUsage ?? snapshot.lastUsage,
          measuredTokens: promptTokens(detail.lastPromptUsage),
          composition: detail.lastComposition ?? undefined,
        }))
        await this.refreshSubagents()
        await this.recoverRunState(id)
      },

      /**
       * Starts a new chat.
       *
       * Nothing is created here. A chat with no message in it has no title anyone could give it and
       * nothing to show in the rail, so the session is created by the first send, which is also the
       * first moment a title is knowable. That makes "new chat" while an empty one is already open a
       * no-op for free, and leaves no empty rows behind.
       */
      newChat: () => {
        this.assistantId = null
        this.runId = null
        this.noticeSticky = false
        this.patch({
          sessionId: null,
          subagents: [],
          tasks: [],
          feedback: [],
          notice: '',
          noticeDetail: '',
          noticeTone: 'warn',
          messages: chatReducer(this.snapshot.messages, { type: 'RESET' }),
          busy: false,
        })
        runStore.set((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
      },

      deleteSession: async (id: string) => {
        await deleteSession(this.state.windowId, id)
        if (this.state.sessionId === id) {
          this.patch({
            sessionId: null,
            subagents: [],
            tasks: [],
            messages: chatReducer(this.snapshot.messages, { type: 'RESET' }),
          })
          runStore.set((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
        }
        await this.refreshSessions()
      },

      /**
       * Sends a turn.
       *
       * @returns whether the text left the composer. A refused send keeps the draft where the user
       *          can still see and re-send it, which is why this is not fire-and-forget.
       */
      send: async (text: string, attachments: Attachment[], options: SendOptions = {}) => {
        const windowId = this.state.windowId
        const labels = this.labels
        let active = this.state.sessionId
        // Whether this send is what created the chat. A send that is then refused has produced
        // nothing to keep, and an empty chat left in the rail is a row the user has to tidy away
        // themselves.
        let createdHere = false
        if (!active) {
          // The first send is what brings a chat into being, titled after what was said.
          const created = await createSession(windowId, autoTitle(text || labels.attachmentOnly))
          await loadSession(windowId, created.id)
          active = created.id
          createdHere = true
          this.patch({ sessionId: active })
          await this.refreshSessions()
        }
        this.showNotice('')
        // Composed before the dispatch below, because the host's prefix describes the conversation
        // this message is being added to, not the one it is already in. `App` got that for free: it
        // read a ref assigned during render, which a `useReducer` dispatch in the same handler
        // cannot have updated. This store's dispatch does update its state at once, so reading it
        // afterwards would hand the host the outgoing message - and a host that counts turns would
        // put the wrong number into every prompt.
        const outbound = composeOutboundText(
          text,
          options.contextTag?.({ messages: this.state.messages }),
        )
        // Appended before the request, not after it: the server starts streaming the answer the
        // moment it accepts the message, and an await here would let the first assistant event
        // reach the reducer first - on a new chat the answer then appeared above the question.
        // A refused send takes the message back out again.
        const messageId = crypto.randomUUID()
        this.dispatch({
          type: 'ADD_USER',
          id: messageId,
          text: text || labels.attachmentOnly,
          edited: options.edited,
        })
        this.patch({ busy: true })
        runStore.set((s) => ({ ...s, busy: true }))
        try {
          await sendMessage(windowId, {
            text: outbound,
            sessionId: active,
            agent: this.state.agent,
            model: this.state.model,
            mcpOverride: this.state.mcpDirty,
            mcpServers: this.state.mcp,
            attachments,
            effort: this.state.effort || undefined,
          })
        } catch (err) {
          this.dispatch({ type: 'REMOVE_MESSAGE', id: messageId })
          this.patch({ busy: false })
          runStore.set((s) => ({ ...s, busy: false }))
          // A refusal is not a failure: the server wrote a sentence for this user saying what they
          // may not use, and it reads as information rather than as something that went wrong.
          if (err instanceof ForbiddenError) {
            await this.discardEmptyChat(createdHere, active)
            this.showNotice(err.reason || labels.notAllowed, '', { tone: 'info', sticky: true })
            return false
          }
          const busyRefusal = err instanceof Error && err.message === 'busy'
          await this.discardEmptyChat(createdHere && !busyRefusal, active)
          this.showNotice(busyRefusal ? labels.busyHint : labels.sendFailed, '', {
            tone: busyRefusal ? 'info' : 'warn',
          })
          return false
        }
        return true
      },

      /**
       * Sends an edited message in place of the one the user changed.
       *
       * Nothing happens until this point: the bubble is edited in the thread, and only a send drops
       * the turn and everything after it - here and on the server - and starts the conversation
       * again from the new wording. A cancelled edit leaves the conversation exactly as it was.
       */
      editSend: async (messageId: string, turnIndex: number, text: string, options: SendOptions = {}) => {
        const windowId = this.state.windowId
        const labels = this.labels
        const active = this.state.sessionId
        if (!active) return false
        this.showNotice('')
        let removed = 0
        try {
          const result = await truncate(windowId, active, turnIndex)
          if (result.status === 'busy') {
            this.showNotice(labels.busyHint, '', { tone: 'info' })
            return false
          }
          removed = result.removedTurns ?? 0
        } catch {
          this.showNotice(labels.sendFailed)
          return false
        }
        this.dispatch({ type: 'REMOVE_FROM_USER', id: messageId })
        this.patch({ feedback: this.state.feedback.filter((entry) => entry.turnIndex < turnIndex) })
        runStore.set((snapshot) => ({ ...snapshot, measuredTokens: 0 }))
        const sent = await this.actions.send(text, [], { ...options, edited: true })
        // Dropping the edited turn is what editing always does; saying so only helps when answers
        // the user could still see went with it.
        // Sticky: the send that follows an edit is what starts the run, and a run start used to
        // clear the strip in the same instant this line was written into it.
        if (sent && removed > 1) {
          this.showNotice(labels.laterTurnsRemoved(removed - 1), '', { tone: 'info', sticky: true })
        }
        return sent
      },

      /**
       * Answers the last message again.
       *
       * The old answer is not kept beside the new one: the user asked for that turn to be done
       * differently. The server drops it and re-sends the same message; what streams in takes its
       * place.
       */
      regenerate: async (messageId: string, turnIndex: number) => {
        const windowId = this.state.windowId
        const labels = this.labels
        const active = this.state.sessionId
        if (!active || this.state.regenerating) return
        this.showNotice('')
        this.patch({ regenerating: true })
        try {
          const result = await regenerateTurn(windowId, {
            sessionId: active,
            agent: this.state.agent,
            model: this.state.model,
            mcpOverride: this.state.mcpDirty,
            mcpServers: this.state.mcp,
            effort: this.state.effort || undefined,
          })
          if (result.status === 'busy') {
            this.showNotice(labels.busyHint, '', { tone: 'info' })
            return
          }
          if (result.status !== 'ok') {
            this.showNotice(labels.sendFailed)
            return
          }
          // The turn is gone server side; drop it here too and let the run rebuild it.
          this.dispatch({ type: 'REMOVE_FROM_USER', id: messageId })
          this.patch({ feedback: this.state.feedback.filter((entry) => entry.turnIndex !== turnIndex) })
          this.dispatch({ type: 'ADD_USER', id: crypto.randomUUID(), text: result.text ?? '' })
          this.patch({ busy: true })
          runStore.set((snapshot) => ({ ...snapshot, busy: true, measuredTokens: 0 }))
        } catch (err) {
          if (err instanceof ForbiddenError) {
            this.showNotice(err.reason || labels.notAllowed, '', { tone: 'info', sticky: true })
          } else {
            this.showNotice(labels.sendFailed)
          }
        } finally {
          this.patch({ regenerating: false })
        }
      },

      /** Records what the user thought of an answer, or withdraws an earlier rating. */
      rate: async (turnIndex: number, rating: Rating, note?: string) => {
        const windowId = this.state.windowId
        const active = this.state.sessionId
        if (!active) return
        const others = this.state.feedback.filter((entry) => entry.turnIndex !== turnIndex)
        this.patch({ feedback: rating ? [...others, { turnIndex, rating, note }] : others })
        try {
          await sendFeedback(windowId, active, turnIndex, rating, note)
        } catch {
          // The rating is a convenience, not the conversation: a failed write is not worth an error
          // banner, but it must not look recorded either.
          this.patch({ feedback: this.state.feedback.filter((entry) => entry.turnIndex !== turnIndex) })
        }
      },

      stop: () => {
        void cancelRun(this.state.windowId)
      },

      selectAgent: (name: string) => this.applyAgent(name, this.state.caps.agents),

      selectModel: (id: string) => this.patch({ model: id }),

      selectMcpServers: (names: string[]) => {
        this.patch({ mcpDirty: true, mcp: [...names] })
      },

      toggleMcp: (name: string) => {
        const mcp = this.state.mcp
        this.patch({
          mcpDirty: true,
          mcp: mcp.includes(name) ? mcp.filter((x) => x !== name) : [...mcp, name],
        })
      },

      selectEffort: (level: string) => {
        this.patch({ effort: level })
        writeEffort(level || null)
      },

      selectTraceMode: (mode: TraceMode) => {
        this.patch({ traceMode: mode })
        writeTraceMode(mode)
      },

      toggleAutoApprove: async () => {
        const windowId = this.state.windowId
        const next = !this.state.autoApprove
        this.patch({ autoApprove: next })
        try {
          await setApprovalMode(windowId, next ? 'AUTO_APPROVE' : 'MANUAL')
        } catch {
          this.patch({ autoApprove: !next })
          return
        }
        if (!next) return
        for (const message of this.state.messages) {
          if (message.type !== 'approval_request' || message.outcome !== 'pending') continue
          void respond(windowId, {
            requestId: message.approvalRequestId,
            response: 'APPROVED',
            toolName: message.toolName,
            argsJson: message.argsJson,
          })
          this.dispatch({
            type: 'RESOLVE_APPROVAL',
            approvalRequestId: message.approvalRequestId,
            outcome: 'APPROVED',
          })
        }
      },

      cancelSubagent: async (id: string) => {
        await cancelSubagent(this.state.windowId, id)
        this.patch({
          subagents: this.state.subagents.map((item) =>
            item.subagentId === id ? { ...item, status: 'CANCELLED' } : item,
          ),
        })
      },

      stopSubagents: async () => {
        const windowId = this.state.windowId
        const active = this.state.subagents.filter(
          (item) => item.status === 'RUNNING' || item.status === 'QUEUED',
        )
        await Promise.all(active.map((item) => cancelSubagent(windowId, item.subagentId)))
        this.patch({
          subagents: this.state.subagents.map((item) =>
            item.status === 'RUNNING' || item.status === 'QUEUED' ? { ...item, status: 'CANCELLED' } : item,
          ),
        })
      },

      loadSubagentTranscript: (id: string) => loadSubagentTranscript(this.state.windowId, id),

      openPane: (content: ReactNode, options: PaneOpenOptions) => {
        // Opening a pane opens the side, and that is the user opening it: a reload shows what they
        // were looking at rather than the element's default.
        this.patch({ paneContent: content, paneOptions: options, sideOpen: true, railDecided: true })
      },

      closePane: () => this.patch({ paneContent: null, paneOptions: null }),

      closeSide: () =>
        this.patch({ paneContent: null, paneOptions: null, sideOpen: false, railDecided: true }),

      toggleSide: (badgeTotal: number) => {
        if (this.state.sideOpen) {
          this.patch({ paneContent: null, paneOptions: null, sideOpen: false, railDecided: true })
          return
        }
        this.patch({ sideOpen: true, railDecided: true, badgeSeen: badgeTotal })
      },

      showSection: (id: string, badgeTotal: number) => {
        this.patch({
          paneContent: null,
          paneOptions: null,
          sideOpen: true,
          railDecided: true,
          badgeSeen: badgeTotal,
          collapsedSections: this.state.collapsedSections.filter((item) => item !== id),
          sectionChoices: { ...this.state.sectionChoices, [id]: true },
        })
      },

      setPaneWidth: (width: number) => {
        // Dragging back to nothing is not a width the user chose: the configured one applies again.
        this.paneWidthStored = width > 0 ? width : 0
        this.patch({ paneWidth: width > 0 ? width : this.paneWidthDefault })
      },

      toggleSection: (id: string) => {
        const collapsed = this.state.collapsedSections
        const open = collapsed.includes(id)
        this.patch({
          collapsedSections: open ? collapsed.filter((item) => item !== id) : [...collapsed, id],
          sectionChoices: { ...this.state.sectionChoices, [id]: open },
        })
      },

      applyCollapsedDefaults: (ids: string[]) => {
        this.patch({ collapsedSections: collapsedSections(ids, this.state.sectionChoices) })
      },

      applyRailDefault: (open: boolean) => {
        if (this.state.railDecided) return
        this.patch({ sideOpen: open })
      },

      publishBadgeTotal: (total: number) => {
        const next = Number.isFinite(total) && total > 0 ? Math.round(total) : 0
        if (next === this.state.badgeTotal) return
        this.patch({ badgeTotal: next })
      },

      markBadgesSeen: (badgeTotal: number) => {
        const seen = Number.isFinite(badgeTotal) && badgeTotal > 0 ? Math.round(badgeTotal) : 0
        // Guarded rather than patched blindly: the layout is persisted from a state change, and an
        // unchanged watermark would put a PUT /chat/preferences on every open of the dock.
        if (seen === this.state.badgeSeen) return
        this.patch({ badgeSeen: seen })
      },

      registerRail: () => {
        this.rails += 1
        this.patch({ railMounted: true })
        let released = false
        return () => {
          if (released) return
          released = true
          this.rails = Math.max(0, this.rails - 1)
          const mounted = this.rails > 0
          // The badge belongs to the rail that reported it: with none left there is nothing to
          // count, and a stale total would keep a number on a button that opens nothing.
          this.patch({ railMounted: mounted, badgeTotal: mounted ? this.state.badgeTotal : 0 })
        }
      },

      bindComposer: (api) => {
        this.composerApi = api
      },

      insertReference: (token) => {
        this.composerApi?.insertReference(token)
      },
    }
  }
}

function sameField(next: ChatState, previous: ChatState, key: keyof ChatState): boolean {
  const a: unknown = next[key]
  const b: unknown = previous[key]
  if (Array.isArray(a) && Array.isArray(b)) return sameList(a, b)
  return Object.is(a, b)
}

/**
 * The chats this browser window is showing, one per `chat-id`.
 *
 * Module-scoped on purpose: `tl-chat-ui.js` and `tl-chat-seams.js` are shims over one
 * `tl-chat-shared.js`, so every chat element on the page - whatever bundle entry mounted it - sees
 * this same map. A chat is dropped again when its last element unmounts, so a fresh mount starts as
 * fresh as it does today.
 */
const CHATS = new Map<string, ChatStore>()

/**
 * Stores already built for a chat id whose first element has not subscribed yet.
 *
 * A store is not written into {@link CHATS} until it is subscribed, because a render React throws
 * away would otherwise leave a `live === false` entry there that no `quiesce` ever removes. But
 * every element of one chat is *built* in the same commit, before any of them subscribes - so
 * without this the second element builds a store of its own and keeps it for good: a rail and a
 * transcript on one `chat-id` showing two conversations, with a stream each. Construction is
 * therefore shared here, and `subscribe` moves the entry into {@link CHATS}.
 */
const BUILDING = new Map<string, ChatStore>()

/** The store of a chat, created on first use. Exported for tests; components use {@link useChat}. */
export function chatStore(chatId: string, boot: ChatBootstrap): ChatStore {
  const existing = CHATS.get(chatId) ?? BUILDING.get(chatId)
  if (existing) return existing
  const store = new ChatStore(chatId, boot)
  BUILDING.set(chatId, store)
  return store
}

/** How many chats are currently held. Exported for tests. */
export function liveChatCount(): number {
  return CHATS.size
}

/** What a chat element sees and may do. */
export interface ChatHandle {
  chat: ChatState
  actions: ChatActions
}

/**
 * Joins the chat with the given id.
 *
 * Every element that passes the same id sees the same conversation, the same run and the same one
 * `EventSource`; the first to arrive opens the stream and the last to leave closes it.
 */
export function useChat(chatId: string, boot: ChatBootstrap): ChatHandle {
  // The bootstrap seeds a store that does not exist yet; an existing one keeps what it has and
  // takes only the fields `attach` below reacts to, so a second element joining changes nothing.
  const store = useMemo(() => chatStore(chatId, boot), [chatId])
  const subscribe = useCallback((listener: () => void) => store.subscribe(listener), [store])
  const chat = useSyncExternalStore(subscribe, store.getState, store.getState)
  useEffect(
    () => store.attach(boot),
    // The fields, not the object: a host that rebuilds the bootstrap on every render must not
    // re-run what a sign-in or a late window id is what really changes.
    [store, boot.windowId, boot.authenticated, boot.userName, boot.welcomeName, boot.language],
  )
  return { chat, actions: store.actions }
}
