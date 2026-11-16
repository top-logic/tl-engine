import { isPageLeaving, React, useTLCommand, useTLDataUrl, useTLUpload } from 'tl-react-bridge'
import { ALL_FEATURES, readFeatures, sameFeatures, type ConversationFeatures } from './features'
import type { ReactNode } from 'react'
import {
  autoTitle,
  cancelPromptImprovement,
  cancelRun,
  cancelSubagent,
  completedStatus,
  delegationEnd,
  sendSubagentMessage,
  type SubagentMessageStatus,
  BusyError,
  deleteSession,
  editSend as editSendTurn,
  ForbiddenError,
  RefusedError,
  loadSession,
  loadSubagentTranscript,
  openSession,
  FRAME_PROMPT_IMPROVED,
  FRAME_RUN_WAITING,
  rateAnswer,
  readCapabilities,
  readDraftAttachments,
  readPromptImproved,
  readSessions,
  regenerate as regenerateTurn,
  removeAttachment,
  respond,
  sendMessage,
  setApprovalMode,
  startPromptImprovement,
  STATE_DRAFT_ATTACHMENTS,
  readRunState,
  readRunWaiting,
  turnsJsonToMessages,
  wokenList,
  uploadAttachments,
  type ChatUpload,
  type InteractionAnswer,
  type PromptImproved,
  type RunState,
  type RunWaiting,
  type SendRequest,
  type SessionDetail,
  type TurnAnswer,
  type TurnSettings,
} from './api'
import { selectAttachments } from './attachments'
import {
  CMD_REFRESH_RUN_STATE,
  openChatStream,
  type ChatCommand,
  type ChatStream,
  type StreamFrame,
} from './chat-stream'
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
import { argsSummary } from './plain-text'
import { chatReducer, type ChatAction } from './reducer'
import { composeOutboundText } from './send-prefix'
import { keepUnsent, takeUnsent } from './unsent'
import { citedSources, latestAnswerSources, type CitedSource } from './sources'
import {
  EMPTY_RUN,
  createRunStore,
  readActiveSession,
  readEffort,
  readTraceMode,
  runStore,
  type RunStore,
  writeActiveSession,
  writeEffort,
  writeTraceMode,
} from './store'
import {
  emptyLive,
  isEnded,
  keepLast,
  MAX_CHILD_STEPS,
  promptTokens,
  type AgentOption,
  type ApprovalOutcome,
  type Asker,
  type Capabilities,
  type ChatMessage,
  type Citation,
  type ConnectionState,
  type ContextComposition,
  type DraftAttachment,
  type MessageFeedback,
  type ModelOption,
  type NoticeTone,
  type Rating,
  type RunSnapshot,
  type SessionSummary,
  type SubagentActivity,
  type SubagentBudget,
  type SubagentLive,
  type SubagentStatus,
  type SubagentStatusEvent,
  type TokenUsage,
  type ToolCallStatus,
  type TraceMode,
  type WokenSubagent,
} from './types'
import { randomId } from './ids'

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

/** A frame's payload as the shape its handler reads, or `fallback` when the server sent no object. */
function frameData<T>(data: unknown, fallback: T): T {
  return data !== null && typeof data === 'object' ? (data as T) : fallback
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

/**
 * The decision an approval card shows for an answer another window gave, or `null` when the answer
 * is not an approval decision at all - a question's answer is the user's own text.
 */
function approvalOutcome(raw: string | undefined): ApprovalOutcome | null {
  if (raw === 'APPROVED' || raw === 'APPROVED_FOR_SESSION' || raw === 'TIMED_OUT') return raw
  if (raw?.startsWith('REJECTED')) return 'REJECTED'
  return null
}

function parseTraceMode(value: unknown): TraceMode {
  if (value === 'none' || value === 'summary' || value === 'detailed') return value
  // "overview" is the legacy name a stored preference may still carry - it maps to summary.
  if (value === 'overview') return 'summary'
  return 'summary'
}

/**
 * Who asks, when a background subagent does: the id the server sent, with the agent's name taken
 * from the server or, failing that, from the subagent list the chat already holds.
 */
export function asker(
  subagentId: string | undefined,
  agent: string | undefined,
  subagents: SubagentActivity[],
): Asker | undefined {
  if (!subagentId) return undefined
  const known = subagents.find((s) => s.subagentId === subagentId)
  return { subagentId, agent: agent || known?.targetAgentName || '' }
}

/** How much of a running subagent's text its card keeps - the end, the newest. */
const MAX_CHILD_TEXT = 400

/**
 * One live event of a background subagent, applied to what its card shows.
 *
 * Returns the same object when the event changes nothing the card shows, so the caller can skip the
 * update.
 */
export function applyChildEvent(
  current: SubagentLive | undefined,
  eventType: string,
  data: {
    toolCallId?: string
    toolName?: string
    argsJson?: string
    text?: string
    turnNumber?: number
    turnUsage?: { inputTokens?: number; outputTokens?: number }
    messages?: { id?: string; text?: string }[]
    totalCost?: number
    currency?: string
  },
): SubagentLive {
  const live: SubagentLive = current ?? emptyLive()
  const setStatus = (status: ToolCallStatus): SubagentLive =>
    data.toolCallId
      ? {
          ...live,
          steps: live.steps.map((step) => (step.toolUseId === data.toolCallId ? { ...step, status } : step)),
        }
      : live
  switch (eventType) {
    case 'turn_started':
      return { ...live, turns: Math.max(live.turns, data.turnNumber ?? live.turns + 1) }
    case 'run_completed':
      // What the run cost, when it was priced; the panel sums it (#783).
      return data.currency ? { ...live, cost: data.totalCost ?? 0, currency: data.currency } : live
    case 'turn_completed':
      return {
        ...live,
        tokens: live.tokens + (data.turnUsage?.inputTokens ?? 0) + (data.turnUsage?.outputTokens ?? 0),
      }
    case 'text_delta': {
      if (!data.text) return live
      const text = live.text + data.text
      return { ...live, text: text.length > MAX_CHILD_TEXT ? text.slice(text.length - MAX_CHILD_TEXT) : text }
    }
    case 'tool_started': {
      if (!data.toolCallId) return live
      const summary = argsSummary(data.argsJson)
      const steps = [
        ...live.steps,
        {
          toolUseId: data.toolCallId,
          name: data.toolName ?? 'tool',
          summary,
          status: 'running' as ToolCallStatus,
        },
      ]
      // A new step starts a new thought: the text the card shows is what the child wrote since.
      return { ...live, text: '', steps: keepLast(steps, MAX_CHILD_STEPS) }
    }
    case 'user_message_read':
      return readNotes(live, data.messages ?? [])
    case 'tool_completed':
      return setStatus('done')
    case 'tool_failed':
    case 'tool_denied':
      return setStatus('error')
    default:
      return live
  }
}

/** The most messages to one subagent a card keeps. */
const MAX_NOTES = 20

/** A message the user wrote to a subagent, added to what its card shows as sent and not yet read. */
export function addNote(current: SubagentLive | undefined, id: string, text: string): SubagentLive {
  const live: SubagentLive = current ?? emptyLive()
  const notes = [...(live.notes ?? []).filter((note) => note.id !== id), { id, text, read: false }]
  return { ...live, notes: keepLast(notes, MAX_NOTES) }
}

/**
 * The subagent has read these messages. One another window of the chat sent is added as read, so
 * every window shows what the user wrote to it.
 */
function readNotes(live: SubagentLive, read: { id?: string; text?: string }[]): SubagentLive {
  const notes = [...(live.notes ?? [])]
  for (const message of read) {
    if (!message.id) continue
    const index = notes.findIndex((note) => note.id === message.id)
    if (index >= 0) notes[index] = { ...notes[index], read: true }
    else notes.push({ id: message.id, text: message.text ?? '', read: true })
  }
  return { ...live, notes: keepLast(notes, MAX_NOTES) }
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
    // `||`, not `??`: the end frames carry an empty name, which must not replace the known one.
    // Empty while unknown - an end frame names no agent - so that what a card knows from the
    // recorded part is not replaced by a placeholder.
    targetAgentName: event.targetAgentName || previous?.targetAgentName || '',
    status,
    detail: event.detail ?? previous?.detail ?? '',
    totalTokens: event.totalTokens ?? previous?.totalTokens ?? 0,
    durationMs: event.durationMs ?? previous?.durationMs ?? 0,
    // A running child's live log answers too, so its transcript opens from the start. A queued one
    // has no run yet, and a cancelled one may never have had one.
    hasTranscript: (status !== 'CANCELLED' && status !== 'QUEUED') || previous?.hasTranscript === true,
    parentToolCallId: event.parentToolCallId || previous?.parentToolCallId,
    // The detail of a start event is the task; later events carry the result there instead.
    task: previous?.task ?? (isEnded(status) ? undefined : event.detail),
    childRunId: event.childRunId || previous?.childRunId,
  }
  if (index < 0) return [...current, next]
  return current.map((item, itemIndex) => (itemIndex === index ? next : item))
}

/**
 * What a control knows about the chat before the browser has asked the server anything.
 *
 * It travels with the control state (every chat control's `putState`), which is why there is no
 * `GET` for the preferences - see `preferences.ts`.
 */
export interface ChatBootstrap {
  /** False in a window nobody is signed in to; the chat controls refuse every command it sends. */
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
  /** The person's last position of the auto-approve switch; a new chat starts with it. */
  autoApprove: boolean
  /**
   * Section ids that start collapsed. Omitted keeps today's App default ({@code context} closed).
   * Set by {@code <chat-rail/>} from each section's {@code open} flag (D3).
   */
  collapsedSections?: string[]
  /**
   * The agents, models and MCP servers the account may choose from, as the element's control state
   * carries them (read with `readCapabilities`); absent from every element but a conversation.
   */
  capabilities?: unknown
  /** The account's chats, as the element's control state carries them (read with `readSessions`). */
  sessions?: unknown
  /** The files attached to the message, as the control state carries them (`readDraftAttachments`). */
  draftAttachments?: unknown
}

/** Reads the bootstrap a chat element pushed into its control state. */
export function readBootstrap(state: Record<string, unknown>): ChatBootstrap {
  const prefs = parsePreferences(state)
  return {
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
    autoApprove: state.approvalMode === 'AUTO_APPROVE',
    collapsedSections: Array.isArray(state.collapsedSections)
      ? state.collapsedSections.map((id) => String(id))
      : undefined,
    // As the state holds them: the store reads them only when the server sent new ones.
    capabilities: state.capabilities,
    sessions: state.sessions,
    draftAttachments: state[STATE_DRAFT_ATTACHMENTS],
  }
}

/**
 * The SSE frames that belong to one turn of one conversation.
 *
 * Everything else a stream carries - the connect, the heartbeat, an approval or a question the
 * *user* has to answer - is addressed to the window rather than to a turn, and is delivered
 * whatever conversation is open.
 */
const RUN_SCOPED_EVENTS = new Set([
  'run_started',
  'run_completed',
  'turn_started',
  'turn_completed',
  'context_composition',
  'text_delta',
  'thinking_delta',
  'tool_started',
  'tool_completed',
  'tool_failed',
  'tool_denied',
  'task_update',
  'compaction_started',
  'message_complete',
  'provider_error',
  'model_switched',
  'retry_scheduled',
  FRAME_RUN_WAITING,
  'tx_rollback',
  'delegation_event',
])

/**
 * Whether a stream frame belongs to the conversation this store is showing.
 *
 * A stream belongs to one conversation: every command goes to a chat control of its conversation,
 * and the server holds a `TaskProgressTracker` - one active session, one processing state - for
 * each of them. So a frame arriving here is already this conversation's, and what is left for this
 * guard is the frame that names a session other than the one the store has open: a run of this
 * conversation the user has since moved away from.
 *
 * A frame that names no session at all is delivered: most of them name none, so this is a guard
 * that tightens by itself as the server learns to stamp them.
 */
export function eventBelongsToChat(name: string, raw: unknown, sessionId: string | null): boolean {
  if (!RUN_SCOPED_EVENTS.has(name)) return true
  const named = frameData<{ sessionId?: unknown }>(raw, {}).sessionId
  if (typeof named !== 'string' || !named) return true
  return named === sessionId
}

/**
 * Which store a chat element joins.
 *
 * `chat-id` is what lets separate elements find each other. An element that hands down none falls
 * back to the control's own id, so two chats in one window stay two chats - `CopilotRegistry` in
 * Consulting keeps a *list* of live panels, and a fixed key would silently merge their
 * transcripts.
 */
export function chatIdOf(state: Record<string, unknown>, controlId: string): string {
  const configured = typeof state.chatId === 'string' ? state.chatId.trim() : ''
  return configured || controlId
}

/** Everything one chat shows, as one immutable snapshot. */
export interface ChatState {
  // Bootstrap.
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

  /**
   * Which functions beyond sending the open conversation supports, as the server stated it with the
   * conversation or the last send; everything for a new chat.
   */
  features: ConversationFeatures

  // Transcript.
  messages: ChatMessage[]
  feedback: MessageFeedback[]
  /** Every source this chat has cited, newest run included. */
  sources: CitedSource[]
  /**
   * What the newest answer cited - the count the Quellen badge reports.
   *
   * A badge over {@link sources} could only grow, so it sat on the icon for the rest of the chat
   * and said nothing about the answer in front of the reader (#495).
   */
  latestSources: CitedSource[]

  // Run.
  busy: boolean
  regenerating: boolean
  subagents: SubagentActivity[]
  /** The budget the subagents spend from, as the last list said; absent while the run has none. */
  subagentBudget?: SubagentBudget
  /** What each running subagent has done so far, by subagent id - what its card shows live. */
  subagentLive: Record<string, SubagentLive>
  /**
   * What each synchronous delegation has done so far, by the lead's `delegate_to_*` call - what its
   * card shows live.
   */
  delegationLive: Record<string, SubagentLive>
  tasks: AgentTask[]
  connection: ConnectionState
  notice: string
  /** What the provider itself said, kept out of the strip but reachable as its tooltip. */
  noticeDetail: string
  /**
   * Whether the strip reports a failure or merely says what happened.
   *
   * "Der Chat ist beschäftigt" is an outcome of what the user just did; wearing the same warning
   * accent as a provider failure made every line read as something going wrong.
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
   * Whether the mounted rail is a drawer: its host is too narrow for a column beside the
   * conversation, so it keeps only its strip in the flow and opens over the conversation (#565).
   *
   * Published by the rail, which is the only element that measures its host. While it is set, the
   * actions that open and close the rail open and close {@link railDrawerOpen} instead of
   * {@link sideOpen}: the user's column choice is a statement about a wide page, and it is what the
   * rail shows again once the host is wide again.
   */
  railDrawer: boolean
  /** Whether the rail's drawer is open. Never stored: every narrow page load starts closed. */
  railDrawerOpen: boolean

  /**
   * How many `<chat-conversation/>` elements of this chat are mounted **outside** a
   * `<chat-dock/>`.
   *
   * What a dock asks before it shows itself. Not a transport question any more - a window carries
   * as many conversations as it shows - but a question about *this* one: two composers on one draft
   * channel and two rails on one remembered layout are the same conversation shown twice. The
   * page's conversation wins; it is the one the user navigated to.
   */
  conversationsOutsideDock: number

  /**
   * The files attached to the message being written, as the chat controls state them: the files
   * wait on the server, which the send takes them from.
   */
  draftAttachments: DraftAttachment[]

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

/**
 * What belongs to one conversation and to no other: its subagents, its tasks, the user's feedback
 * on its answers and the notice strip about it.
 *
 * Opening, starting and deleting a chat all empty this at once. Each used to list the fields itself,
 * and the lists drifted apart - a deleted chat's notice stayed up, and no switch dropped the previous
 * run's subagent budget.
 */
const EMPTY_CONVERSATION: Pick<
  ChatState,
  | 'subagents'
  | 'subagentBudget'
  | 'subagentLive'
  | 'delegationLive'
  | 'tasks'
  | 'feedback'
  | 'notice'
  | 'noticeDetail'
  | 'noticeTone'
  | 'features'
> = {
  subagents: [],
  subagentBudget: undefined,
  subagentLive: {},
  delegationLive: {},
  tasks: [],
  feedback: [],
  notice: '',
  noticeDetail: '',
  noticeTone: 'warn',
  features: ALL_FEATURES,
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
  /**
   * Opens a chat. Asking for the chat the window already shows, or is already opening, changes
   * nothing; any other chat supersedes whatever was being opened, so the latest choice is the one
   * the window ends up showing.
   */
  selectSession(id: string): Promise<void>
  newChat(): void
  deleteSession(id: string): Promise<void>
  /** Sends a turn, with the files attached to it ({@link ChatState.draftAttachments}). */
  send(text: string, options?: SendOptions): Promise<boolean>
  editSend(messageId: string, turnIndex: number, text: string, options?: SendOptions): Promise<boolean>
  regenerate(messageId: string, turnIndex: number): Promise<void>
  rate(turnIndex: number, rating: Rating, note?: string): Promise<void>
  stop(): void
  /**
   * Attaches files to the message being written: those the server would refuse are left out, the
   * others are uploaded.
   *
   * @returns why files were not attached, ready to show; empty when all of them were.
   */
  attach(files: File[]): Promise<string>
  /** Takes a file off the message being written. */
  removeAttachment(id: string): void
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
  /**
   * Answers an approval or a question a run of the account waits for.
   *
   * @throws when the answer did not reach the server.
   */
  respond(answer: InteractionAnswer): Promise<void>
  /**
   * Improves the draft with the conversation's prompt improver: starts the improvement and settles
   * with its result, which the server sends on the stream when the model is done.
   *
   * @throws when the improvement was not started; a `RefusedError` carries the server's sentence.
   */
  improvePrompt(draft: string, requestId: string): Promise<PromptImproved>
  /** Cancels an improvement; its promise settles at once as `cancelled`. */
  cancelPromptImprovement(requestId: string): void
  /** Stops one background subagent; the starting call tells it from a namesake of another window. */
  cancelSubagent(id: string, parentToolCallId?: string): Promise<void>
  /**
   * Writes to one running background subagent from its card; on `sent` the card shows the message
   * as sent until the subagent reads it.
   */
  messageSubagent(id: string, parentToolCallId: string, text: string): Promise<SubagentMessageStatus>
  stopSubagents(): Promise<void>
  /** Reads the subagents of the chat anew - their costs, and the budget they spend from. */
  refreshSubagents(): Promise<void>
  loadSubagentTranscript(id: string, runId?: string): Promise<ChatMessage[]>
  openPane(content: ReactNode, options: PaneOpenOptions): void
  closePane(): void
  /** Closes whatever the side is showing - what a click beside a sheet means. */
  closeSide(): void
  toggleSide(badgeTotal: number): void
  /** Opens the rail on one section, as a click on its strip icon does. */
  showSection(id: string, badgeTotal: number): void
  /**
   * Publishes whether one mounted rail is a drawer; see {@link ChatState.railDrawer}.
   *
   * Per rail, because a rail that mounts and leaves again - a dock's, for one commit, on a page that
   * shows the conversation itself - must not take the page rail's answer with it.
   */
  setRailDrawer(rail: object, drawer: boolean): void
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
  /**
   * Announces a mounted `<chat-conversation/>`; the returned function unregisters it again.
   *
   * `insideDock` is what the element's control read from its view context, so a dock's own
   * conversation does not count against the dock itself. Counted rather than flagged, for the same
   * reason {@link registerRail} counts.
   */
  registerConversation(insideDock: boolean): () => void
  /**
   * Says that a host's `input` channel decides which chat this window shows, an empty channel
   * included: the tab's remembered chat is then not restored. It named the chat this tab had on the
   * previous page, which the channel of this page may not want - it showed until the host refused
   * it (#577).
   */
  followChannel(): void
  bindComposer(api: ComposerApi | null): void
  insertReference(token: ReferenceToken): void
  /** Replaces the composer's draft, focuses it and puts the caret at the end. */
  setDraft(text: string): void
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

/**
 * The chat list with `id` titled as the server just answered for it.
 *
 * Opening a chat reads its transcript, and that answer carries the title as the server has it now.
 * The header reads the title from the list, so a list read before a rename would otherwise go on
 * naming the chat by its old title every time it is opened (#596). The same list comes back when
 * nothing differs, so an unchanged title costs no render.
 */
export function withTitle(sessions: SessionSummary[], id: string, title: string | undefined): SessionSummary[] {
  if (!title) return sessions
  const index = sessions.findIndex((session) => session.id === id)
  if (index < 0 || sessions[index].title === title) return sessions
  const next = sessions.slice()
  next[index] = { ...sessions[index], title }
  return next
}

/** The list without the given chat. */
function withoutSession(sessions: SessionSummary[], id: string): SessionSummary[] {
  return sessions.some((session) => session.id === id) ? sessions.filter((session) => session.id !== id) : sessions
}

/** The list with the given chat first, unless it is in it already. */
function withSession(sessions: SessionSummary[], created: SessionSummary): SessionSummary[] {
  return sessions.some((session) => session.id === created.id) ? sessions : [created, ...sessions]
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

/** How the store reaches the server through one of its elements: the element's control's command and data URL. */
interface ElementLink {
  command: ChatCommand
  dataUrl: string
  upload?: ChatUpload
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

  /**
   * How the chat reaches the server through its elements: the command function and the data URL of
   * each element subscribed now, the latest last.
   *
   * The stream, the run state, opening a chat and its transcript are commands and data of a chat
   * control on the server, and every chat element is one, so whichever is mounted can carry them -
   * the server keeps the stream for as long as any of them is displayed, not only the one it was
   * attached through.
   */
  private readonly links: ElementLink[] = []

  /** The link of the element that left last, for the detach its leaving causes. */
  private lastLink: ElementLink | null = null

  /** How many files are on their way to the server now. */
  private attaching = 0

  /** Settles once the files on their way to the server have arrived. */
  private uploads: Promise<void> = Promise.resolve()

  /** The improvements started and not yet answered, by request id: what settles each one's promise. */
  private readonly improvements = new Map<string, (result: PromptImproved) => void>()

  /** Whether the server's capabilities have arrived, which is when the first agent is chosen. */
  private capsTaken = false

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

  /**
   * How far into the running run's text this window's transcript reaches, in the count the
   * server's `text_delta` frames carry as `end`; `null` when this window has seen the run from its
   * start and every delta is new to it.
   *
   * A window that opens a conversation mid-run is shown a snapshot of the turn, and receives the
   * deltas on the stream, a different connection - the first of them may already be in the
   * snapshot, and some may arrive before it. The position is what tells them apart (#544).
   */
  private streamMark: number | null = null

  /**
   * Whether this window joined the running run mid-way, from a snapshot, rather than starting it
   * or seeing it start. Such a window reads the finished turn back when the run ends: its steps
   * came partly from the snapshot and partly from the stream, and the recorded turn is the one
   * version of them that is whole.
   */
  private watching = false

  /**
   * The chat being opened while its transcript is on its way, with the deltas that arrive before
   * it: they cannot be placed until the snapshot says where it ends.
   */
  private opening: { sessionId: string; held: { text: string; end: number }[] } | null = null

  /** Whether the line now on screen must survive the next run start. */
  private noticeSticky = false
  /** Whether the strip says that the run is waiting for a busy model, see `retry_scheduled`. */
  private retryNotice = false

  /** Whether the strip says that the send waits for a free worker, see {@link FRAME_RUN_WAITING}. */
  private waitingNotice = false

  /**
   * The chat this window had open before it was reloaded, read before anything can overwrite it.
   *
   * The store persists every change of the open chat, and the first of those would be the initial
   * `null` - so the remembered id has to be taken when the store is built, not once it runs.
   */
  private readonly remembered: string | null

  /** Whether the remembered chat has been dealt with; restoring happens once per store. */
  private restored = false

  /**
   * Counts the choices of which chat this window shows - every `selectSession` and `newChat`.
   *
   * Opening a chat waits for its transcript, and the choice may have changed by the time it
   * arrives. Only the latest choice may take effect: an earlier one that completed late put its
   * chat back on screen, the write-back reported it onto the `input` channel, and the channel then
   * asked for the chat that had just been left - two openings chasing each other, the transcript
   * switching between two conversations every few milliseconds without end (#576).
   */
  private choice = 0

  /**
   * The person's last position of the auto-approve switch, in whichever chat they set it.
   *
   * A new chat starts with it. A chat that exists shows the mode stored with it instead, so opening
   * one does not change this.
   */
  private approvalChoice: boolean

  /**
   * This chat's run snapshot: what the context ring reads.
   *
   * One per chat rather than the module-level {@link runStore}, which a window holding two
   * conversations would have both of them writing - and the ring beside either composer would then
   * show whichever ran last. The module store is still mirrored for the single-chat host seam.
   */
  readonly runs: RunStore = createRunStore()

  /** The width the element configures, kept apart from the width the user dragged to. */
  private readonly paneWidthDefault: number

  /** The `chat-id` the layout is stored under; the store's own key may be a control id. */
  private readonly layoutKey: string

  /** How many {@code <chat-rail/>} elements on this `chat-id` are mounted. */
  private rails = 0

  /** The mounted rails that are drawers; {@link ChatState.railDrawer} is whether there is one. */
  private readonly drawerRails = new Set<object>()

  /** How many `<chat-conversation/>` elements on this `chat-id` are mounted outside a dock. */
  private conversations = 0

  /**
   * The layout the server holds, so an unchanged layout is not written back.
   *
   * The bootstrap is the stored layout; a `PUT` of it would be a no-op at best and, when the page
   * booted from a stale control state, would overwrite the user's real choice.
   */
  private storedLayout: ChatPreferences

  /**
   * Writes this chat's run snapshot, and mirrors it onto the module-level {@link runStore}.
   *
   * The mirror is the host seam (`index.ts` exports `runStore`) and nothing in this bundle renders
   * from it; a window with two chats leaves the last writer standing there, which is what that
   * seam has always meant.
   */
  private setRun = (next: RunSnapshot | ((prev: RunSnapshot) => RunSnapshot)): void => {
    this.runs.set(next)
    runStore.set(this.runs.get())
  }

  /** The patch that opens the rail: its drawer while it is one, else the stored column choice. */
  private opened(): Partial<ChatState> {
    return this.state.railDrawer ? { railDrawerOpen: true } : { sideOpen: true, railDecided: true }
  }

  /** The patch that closes the rail; see {@link opened}. */
  private closed(): Partial<ChatState> {
    return this.state.railDrawer ? { railDrawerOpen: false } : { sideOpen: false, railDecided: true }
  }

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

  /**
   * Whether a composer has been bound to this store yet.
   *
   * A message the page was reloaded over before it went is given back to the first composer of the reloaded
   * page, and to no later one: the element binds its composer on every render, and the page that
   * refused the send renders again before the reload takes it away - it would take the message back
   * out of storage and lose it with itself.
   */
  private composerBound = false

  private readonly offeredModelsOf = memo(
    (models: ModelOption[], agents: AgentOption[], agent: string): ModelOption[] =>
      modelsFor(models, agents.find((item) => item.name === agent)?.model),
  )

  private readonly effortLevelsOf = memo((models: ModelOption[], model: string): string[] =>
    effortLevelsOf(models, model),
  )

  private readonly sourcesOf = memo((messages: ChatMessage[]): CitedSource[] => citedSources(messages))

  private readonly latestSourcesOf = memo((messages: ChatMessage[]): CitedSource[] =>
    latestAnswerSources(messages),
  )

  readonly actions: ChatActions

  private readonly chatId: string

  constructor(chatId: string, boot: ChatBootstrap) {
    this.chatId = chatId
    this.remembered = readActiveSession(chatId)
    this.approvalChoice = boot.autoApprove
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
      autoApprove: boot.autoApprove,
      // The stored choice wins over the configured default: the element's trace-mode is what a new
      // user starts with, not a setting that overrules what this user picked.
      traceMode: readTraceMode() ?? boot.traceMode,
      messages: [],
      sources: [],
      latestSources: [],
      busy: false,
      regenerating: false,
      ...EMPTY_CONVERSATION,
      connection: 'connecting',
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
      railDrawer: false,
      railDrawerOpen: false,
      conversationsOutsideDock: 0,
      draftAttachments: [],
      badgeTotal: 0,
    })
    this.effects = this.declareEffects()
    this.actions = this.declareActions()
    this.takeLists(boot.capabilities, boot.sessions)
    this.takeAttachments(boot.draftAttachments)
  }

  getState = (): ChatState => this.snapshot

  /**
   * Joins an element: `listener` hears every change, `command`, `dataUrl` and `upload` are how the
   * store may reach the server through that element's control.
   */
  subscribe = (listener: () => void, command?: ChatCommand, dataUrl = '', upload?: ChatUpload): (() => void) => {
    this.listeners.add(listener)
    const link: ElementLink | null = command ? { command, dataUrl, upload } : null
    if (link) this.links.push(link)
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
      if (link) {
        const index = this.links.lastIndexOf(link)
        if (index >= 0) this.links.splice(index, 1)
        this.lastLink = link
      }
      this.refs -= 1
      if (this.refs <= 0) this.quiesce()
    }
  }

  /**
   * Sends a command to a chat control of this conversation, through the element that joined last;
   * answered with `undefined` while no element has brought a command function.
   */
  private readonly command: ChatCommand = (name, args, options) => {
    const link = this.link()
    return link ? link.command(name, args, options) : Promise.resolve(undefined)
  }

  /**
   * Uploads to a chat control of this conversation, through the same element {@link command} uses;
   * answered with `undefined` while no element has brought an upload function.
   */
  private readonly upload: ChatUpload = (formData) => {
    const upload = this.link()?.upload
    return upload ? upload(formData) : Promise.resolve(undefined)
  }

  /** The data URL of a chat control of this conversation, of the same element {@link command} uses. */
  private dataUrl(): string {
    return this.link()?.dataUrl ?? ''
  }

  private link(): ElementLink | null {
    return this.links[this.links.length - 1] ?? this.lastLink
  }

  /**
   * Takes the capabilities and the chat list an element's control state carries, when it carries
   * them: the server writes them with every render of a `<chat-conversation>` and the list again
   * whenever the account's chats change.
   *
   * The first capabilities choose the agent: one already chosen wins over the entry point - the
   * `agent` channel may have named one before they arrived, and so may the user. Re-applying it
   * against the catalogue is what gives it its model and its servers, which it could not have had
   * until now. Later ones - a render in another language - leave the turn settings alone.
   */
  takeLists = (capabilities: unknown, sessions: unknown): void => {
    const caps = readCapabilities(capabilities)
    if (caps && JSON.stringify(caps) !== JSON.stringify(this.state.caps)) {
      this.patch({ caps })
      if (!this.capsTaken) {
        this.capsTaken = true
        const chosen = caps.agents.find((a) => a.name === this.state.agent)?.name
        const initial =
          chosen ?? caps.agents.find((a) => a.name === caps.entryPointAgent)?.name ?? caps.agents[0]?.name ?? ''
        if (initial) this.applyAgent(initial, caps.agents)
        else if (caps.models[0]) this.patch({ model: caps.models[0].id })
      }
    }
    const list = readSessions(sessions)
    if (list) this.patch({ sessions: list, sessionsLoading: false })
  }

  /** Takes the files attached to the message, when an element's control state brings new ones. */
  takeAttachments = (raw: unknown): void => {
    const files = readDraftAttachments(raw)
    if (files && JSON.stringify(files) !== JSON.stringify(this.state.draftAttachments)) {
      this.patch({ draftAttachments: files })
    }
  }

  /**
   * Takes the bootstrap a mounting element brought, and runs whatever that started.
   *
   * Idempotent: a second element on the same `chat-id` brings the same values, so nothing changes
   * and no effect re-runs. Values that really differ - a sign-in, a language switch - are
   * what the effect dependencies below react to, exactly as the component's did.
   */
  attach = (boot: ChatBootstrap): void => {
    this.patch({
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
    const latestSources = this.latestSourcesOf(base.messages)
    const sessionTitle =
      base.sessions.find((session) => session.id === base.sessionId)?.title ||
      bundle(base.language).newChat
    return { ...base, offeredModels, effortLevels, sources, latestSources, sessionTitle }
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
   * the rail, mark the badges seen, close the pane - must produce the one `saveLayout` command
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
          s.railDecided,
          s.sideOpen,
          s.paneWidth,
          s.badgeSeen,
          s.sectionChoices,
        ],
        run: () => {
          if (!this.state.authenticated) return
          const next = this.layout
          // What the page booted with is what the server already holds. Writing it back would make
          // every reload store the layout the page happened to start with, which is how the stored
          // choice used to be lost (#423).
          if (sameLayout(next, this.storedLayout)) return
          this.storedLayout = next
          void savePreferences(this.command, this.layoutKey, next)
        },
      },
      {
        deps: (s) => [s.sessionId],
        run: () => writeActiveSession(this.chatId, this.state.sessionId),
      },
      {
        deps: (s) => [s.authenticated],
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
          this.setRun((snapshot) => ({
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
        run: () => this.setRun((snapshot) => ({ ...snapshot, effort: this.state.effort })),
      },
      {
        deps: (s) => [s.authenticated, s.sessionsLoading, s.sessions],
        run: () => this.restoreRememberedSession(),
      },
    ]
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
    // A chat was already chosen for this window. The remembered one would only compete with it, and
    // a host that takes a reported chat as its own would take this tab's stale one back from the
    // chat the page has moved on to (#576).
    if (this.choice > 0) return
    if (!sessions.some((item) => item.id === id)) {
      writeActiveSession(this.chatId, null)
      return
    }
    void this.actions.selectSession(id)
  }

  // --- the stream --------------------------------------------------------------------------

  /**
   * The chat's one stream, opened by the first element that joined this chat and closed by
   * the last one to leave.
   */
  private connect(): (() => void) | void {
    // The window's stream reconnects by itself, and its queue keeps what it could not write. What
    // the engine cannot bring back is the attach: the server ends it when the page counts as left,
    // or forgets the window. A stream that stays silent past STALE_AFTER_MS - the server sends a
    // heartbeat every 15 s - is therefore attached again, and so is one the server failed to
    // start. A session that is not signed in attaches none.
    if (!this.state.authenticated) return
    let closed = false
    let source: ChatStream | null = null
    let staleTimer = 0
    let reopenTimer = 0
    let connectTimer = 0

    const finish = (error?: string, detail?: string) => {
      this.clearRetryNotice()
      if (this.assistantId) {
        this.dispatch({ type: 'FINALIZE_TEXT', id: this.assistantId })
        this.assistantId = null
      }
      if (this.runId) {
        this.dispatch({ type: 'END_RUN', runId: this.runId })
        this.runId = null
      }
      if (error) this.showNotice(error, detail ?? '', { tone: 'warn' })
      this.patch({ busy: false })
      this.setRun((s) => ({ ...s, busy: false }))
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
      const es = openChatStream(this.command)
      source = es
      const on = (name: string, fn: (e: StreamFrame) => void) =>
        es.addEventListener(name, (event: StreamFrame) => {
          markAlive()
          // A frame naming a session other than the open one is a run this conversation has since
          // moved away from - see `eventBelongsToChat`. While a chat is being opened, that chat is
          // the open one: its run's frames are the ones that matter, and the old chat's are not.
          if (!eventBelongsToChat(name, event.data, this.opening?.sessionId ?? this.state.sessionId)) return
          fn(event)
        })

      const setTokens = (windowSize?: number, modelName?: string, usage?: TokenUsage) => {
        this.setRun((s) => ({
          ...s,
          contextWindow: windowSize || s.contextWindow,
          model: modelName || s.model,
          lastUsage: usage ?? s.lastUsage,
          measuredTokens: usage ? promptTokens(usage) || s.measuredTokens : s.measuredTokens,
        }))
      }

      es.addEventListener('open', markAlive)
      on('connected', () => undefined)
      on('run_state', (e) => this.applyRunState(readRunState(e.data)))
      on('heartbeat', () => undefined)

      on('run_started', (e) => {
        const data = frameData<{ runId?: string; usedTokens?: number; contextWindow?: number; model?: string }>(
          e.data,
          {},
        )
        this.patch({ busy: true })
        this.clearWaitingNotice()
        this.clearNoticeOnRunStart()
        // A run this window sees start is one it sees whole.
        this.streamMark = null
        this.watching = false
        this.runId = data.runId ?? randomId()
        this.dispatch({ type: 'START_RUN', runId: this.runId })
        this.setRun((s) => ({
          ...s,
          busy: true,
          estimatedTokens: data.usedTokens ?? s.estimatedTokens,
          contextWindow: data.contextWindow || s.contextWindow,
          model: data.model || s.model,
        }))
      })
      on('turn_started', (e) => {
        const data = frameData<{ usedTokens?: number; contextWindow?: number }>(e.data, {})
        this.setRun((s) => ({
          ...s,
          estimatedTokens: data.usedTokens ?? s.estimatedTokens,
          contextWindow: data.contextWindow || s.contextWindow,
        }))
        if (this.assistantId) this.dispatch({ type: 'FINALIZE_TEXT', id: this.assistantId })
        this.assistantId = randomId()
        this.lastAssistantId = this.assistantId
        this.dispatch({ type: 'ADD_ASSISTANT', id: this.assistantId, text: '', isStreaming: true })
      })
      on('context_composition', (e) => {
        const data = frameData<Partial<ContextComposition>>(e.data, {})
        this.setRun((s) => ({
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
      on(FRAME_RUN_WAITING, (e) => this.applyRunWaiting(readRunWaiting(e.data)))
      on('retry_scheduled', (e) => {
        // The model refused the call and the run waits before it asks again - for a busy key that
        // can be minutes (#678). Said in the strip, not in the transcript: it is a state of the
        // moment, gone as soon as the model answers, and a line per attempt would bury the turn.
        const data = frameData<{ kind?: string; waitMs?: number }>(e.data, {})
        const seconds = Math.max(1, Math.round((data.waitMs ?? 0) / 1000))
        const labels = this.labels
        this.showNotice(
          data.kind === 'OVERLOADED' ? labels.retryOverloaded(seconds) : labels.retryRateLimited(seconds),
          '',
          { tone: 'info' },
        )
        this.retryNotice = true
      })
      on('text_delta', (e) => {
        this.clearRetryNotice()
        const data = frameData<{ text?: string; end?: number }>(e.data, {})
        if (data.text) this.appendDelta(data.text, data.end ?? 0)
      })
      on('thinking_delta', (e) => {
        this.clearRetryNotice()
        const data = frameData<{ text?: string }>(e.data, {})
        if (data.text) this.dispatch({ type: 'APPEND_THINKING', delta: data.text })
      })
      on('tool_started', (e) => {
        this.clearRetryNotice()
        const data = frameData<{ toolCallId?: string; toolName?: string; argsJson?: string }>(e.data, {})
        if (!data.toolCallId) return
        this.dispatch({
          type: 'ADD_TOOL',
          toolUseId: data.toolCallId,
          name: data.toolName ?? 'tool',
          input: parseToolInput(data.argsJson),
        })
      })
      on('tool_completed', (e) => {
        const data = frameData<{ toolCallId?: string; result?: string; durationMs?: number }>(e.data, {})
        if (!data.toolCallId) return
        this.dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'done', durationMs: data.durationMs })
        this.dispatch({ type: 'ADD_TOOL_RESULT', toolUseId: data.toolCallId, output: data.result ?? '' })
      })
      on('tool_failed', (e) => {
        const data = frameData<{ toolCallId?: string; errorMessage?: string; durationMs?: number }>(e.data, {})
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
        const data = frameData<{ toolCallId?: string; reason?: string }>(e.data, {})
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
        const data = frameData<{ tasksJson?: string }>(e.data, {})
        this.patch({ tasks: parseTasksJson(data.tasksJson) })
      })
      on('approval_mode', (e) => {
        // The frame names the mode of the chat the server holds open for this window. A new chat
        // that has no chat on the server yet is not that chat: it keeps the person's choice.
        if (!this.state.sessionId) return
        const data = frameData<{ mode?: string }>(e.data, {})
        this.patch({ autoApprove: data.mode === 'AUTO_APPROVE' })
      })
      on(FRAME_PROMPT_IMPROVED, (e) => {
        const result = readPromptImproved(e.data)
        if (!result) return
        const done = this.improvements.get(result.requestId)
        if (!done) return
        this.improvements.delete(result.requestId)
        done(result)
      })
      on('approval_required', (e) => {
        const data = frameData<{
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
          void respond(this.command, {
            requestId: data.approvalRequestId,
            response: 'APPROVED',
            toolName: data.toolName,
          }).catch(() => undefined)
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
        const data = frameData<{ approvalRequestId?: string; outcome?: string }>(e.data, {})
        if (!data.approvalRequestId) return
        this.dispatch({
          type: 'RESOLVE_APPROVAL',
          approvalRequestId: data.approvalRequestId,
          outcome: (data.outcome as ApprovalOutcome) ?? 'APPROVED',
        })
      })
      on('interaction_resolved', (e) => {
        // Another window of this account answered something this one may be showing. The window
        // whose run it is hears the same news as `approval_resolved` from the run itself; this
        // frame is what reaches a window that only listed the request (a second tab, a reloaded
        // page). Both message kinds are addressed by the same id, so one of the two takes effect.
        const data = frameData<{ requestId?: string; outcome?: string }>(e.data, {})
        if (!data.requestId) return
        const outcome = approvalOutcome(data.outcome)
        if (outcome) {
          this.dispatch({ type: 'RESOLVE_APPROVAL', approvalRequestId: data.requestId, outcome })
        }
        this.dispatch({ type: 'RESOLVE_INTERACTION', requestId: data.requestId })
      })
      on('user_interaction', (e) => {
        const data = frameData<{
          requestId?: string
          question?: string
          header?: string
          type?: 'text' | 'choice'
          options?: { label: string; description?: string }[]
          multiSelect?: boolean
          remainingMs?: number
          askedBySubagentId?: string
          askedByAgent?: string
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
          askedBy: asker(data.askedBySubagentId, data.askedByAgent, this.state.subagents),
        })
      })
      on('delegation_event', (e) => {
        const wrap = frameData<{
          eventType?: string
          eventData?: string
          subagentId?: string
          delegationToolCallId?: string
        }>(e.data, {})
        // A background subagent's own steps go to its card, never into the lead's transcript: mixed
        // into it they were drawn flat, nameless, and among the steps of every other child.
        // Its approvals still reach the prompt stack below, where the user answers them.
        if (wrap.subagentId && wrap.eventType !== 'approval_required' && wrap.eventType !== 'approval_resolved') {
          const id = wrap.subagentId
          const live = applyChildEvent(this.state.subagentLive[id], wrap.eventType ?? '', parseJson(wrap.eventData ?? '', {}))
          if (live !== this.state.subagentLive[id]) {
            this.patch({ subagentLive: { ...this.state.subagentLive, [id]: live } })
          }
          return
        }
        // A synchronous delegation's steps go to its card at the lead's delegate_to_* call, never into
        // the lead's transcript: drawn there they were counted as the lead's own steps (#703).
        const prompt = wrap.eventType === 'approval_required' || wrap.eventType === 'approval_resolved'
        if (!wrap.subagentId && wrap.delegationToolCallId && !prompt) {
          const call = wrap.delegationToolCallId
          const eventData = parseJson<Parameters<typeof applyChildEvent>[2] & { reason?: string }>(
            wrap.eventData ?? '',
            {},
          )
          let live = applyChildEvent(this.state.delegationLive[call], wrap.eventType ?? '', eventData)
          if (wrap.eventType === 'run_completed') live = { ...live, ended: delegationEnd(eventData.reason) }
          if (live !== this.state.delegationLive[call]) {
            this.patch({ delegationLive: { ...this.state.delegationLive, [call]: live } })
          }
          return
        }
        const data = parseJson<{
          toolCallId?: string
          toolName?: string
          argsJson?: string
          approvalRequestId?: string
          displayReason?: string
          toolMode?: string
          remainingMs?: number
        }>(wrap.eventData ?? '', {})
        switch (wrap.eventType) {
          case 'approval_required':
            if (!data.approvalRequestId) break
            if (this.state.autoApprove) {
              void respond(this.command, {
                requestId: data.approvalRequestId,
                response: 'APPROVED',
                toolName: data.toolName,
              }).catch(() => undefined)
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
              // A background subagent asks for this approval, not the agent the user talks to.
              askedBy: asker(wrap.subagentId, undefined, this.state.subagents),
            })
            break
          default:
            break
        }
      })
      on('subagent_started', (e) => {
        const data = frameData<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) {
          this.patch({ subagents: mergeSubagent(this.state.subagents, data, data.queued ? 'QUEUED' : 'RUNNING') })
        }
      })
      on('subagent_running', (e) => {
        // A queued subagent got its slot. Told by the server, not read off its first step: that may be
        // an approval prompt, which never reaches the card, and a window may receive no step at all.
        const data = frameData<SubagentStatusEvent>(e.data, { subagentId: '' })
        const known = this.state.subagents.find((item) => item.subagentId === data.subagentId)
        // An end that arrived first stays: the frames of one flush are not ordered across subagents.
        if (data.subagentId && !(known && isEnded(known.status))) {
          this.patch({ subagents: mergeSubagent(this.state.subagents, data, 'RUNNING') })
        }
      })
      on('lead_woken', (e) => {
        // Ahead of the turn's run_started: the line opens the turn in place of the user's bubble.
        const data = frameData<{ subagents?: WokenSubagent[] }>(e.data, {})
        this.dispatch({ type: 'ADD_NOTICE', kind: 'lead_woken', detail: '', woken: wokenList(data.subagents) })
      })
      on('lead_wakes_exhausted', () => {
        this.showNotice(this.labels.leadWakesExhausted, '', { tone: 'info' })
      })
      on('subagent_completed', (e) => {
        const data = frameData<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) {
          this.patch({ subagents: mergeSubagent(this.state.subagents, data, completedStatus(data.stopReason)) })
        }
      })
      on('subagent_failed', (e) => {
        const data = frameData<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) this.patch({ subagents: mergeSubagent(this.state.subagents, data, 'FAILED') })
      })
      on('subagent_cancelled', (e) => {
        const data = frameData<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) this.patch({ subagents: mergeSubagent(this.state.subagents, data, 'CANCELLED') })
      })
      on('compaction_started', (e) => {
        const data = frameData<{ contextWindow?: number }>(e.data, {})
        // The measured figure describes a conversation that no longer exists; the next turn
        // reports the compacted one, and until then the ring falls back to the estimate.
        this.setRun((s) => ({ ...s, measuredTokens: 0, contextWindow: data.contextWindow || s.contextWindow }))
      })
      on('turn_completed', (e) => {
        const data = frameData<{ turnUsage?: TokenUsage; contextWindow?: number }>(e.data, {})
        if (data.turnUsage) setTokens(data.contextWindow, undefined, data.turnUsage)
      })
      on('run_completed', (e) => {
        const data = frameData<{ reason?: string; totalCost?: number; currency?: string }>(e.data, {})
        // What the run cost, when it reported it - whoever answered: the framework's agent or a
        // backend.
        if (this.runId && data.currency && data.totalCost !== undefined) {
          this.dispatch({ type: 'SET_RUN_COST', runId: this.runId, cost: data.totalCost, currency: data.currency })
        }
        // A run the user stopped is not a failure, and the server does not report it as one
        // (#549). It says so once, in the transcript where the answer stops - the same line the
        // server records for a reload or another window - and not a second time in the strip.
        if (data.reason === 'ABORTED') {
          this.dispatch({ type: 'ADD_NOTICE', kind: 'cancelled', detail: '' })
        }
        finish()
      })
      on('message_complete', (e) => {
        // The authoritative answer. The deltas were a live preview of it; applying it here is what
        // keeps the last fragment from being lost when it arrives after this event.
        const data = frameData<{ contentText?: string; citations?: Citation[] }>(e.data, {})
        const target = this.assistantId ?? this.lastAssistantId
        if (target && data.contentText) {
          this.dispatch({ type: 'SET_TEXT', id: target, text: data.contentText, citations: data.citations })
        }
        if (this.watching) {
          // This window joined the run mid-way (#544): the answer is right already, and reading the
          // turn back makes its steps the recorded ones too.
          this.watching = false
          this.streamMark = null
          void this.reloadTranscript()
        } else if (!target || !data.contentText) {
          // No message of ours to write it into: this window did not start the run, it only has the
          // conversation open, and what it is showing is the snapshot it was restored with (#512).
          // The turn is recorded before this frame is sent, so reading the conversation back is what
          // puts the finished turn on screen - answer, steps and the approval under it - rather than
          // leaving a run that has ended still looking like it is working.
          void this.reloadTranscript()
        }
        finish()
      })
      on('provider_error', (e) => {
        const data = frameData<{ errorMessage?: string; kind?: string }>(e.data, {})
        const detail = data.errorMessage ?? ''
        const text = providerErrorText(data.kind, this.state.language, detail)
        // Also into the transcript, not only into the composer's notice strip: a run that ended
        // without an answer has to say so where the answer would have been, and the strip is
        // cleared by the next send (#429). A turn that failed before any text would otherwise
        // leave the question standing alone, which is the blank thread all over again.
        //
        // Recorded exactly as a re-opened session records it - the provider's own words as the
        // message, its kind as the code - so the card reads the same live as after a reload. The
        // sentence the user reads is the renderer's, from the kind.
        this.dispatch({ type: 'ADD_ERROR', message: detail, code: data.kind })
        finish(text, detail.trim() === text ? '' : detail)
      })

      on('tx_rollback', (e) => {
        // The turn wrote something and none of it was kept. Nobody else says so: the model is told
        // in a tool result and may still answer "done", and the answer is all the user sees. So a
        // notice in this turn, under its steps - not an error card, because the run itself went on,
        // and not the composer's strip, because the next send wipes that and this is part of what
        // happened here (#429).
        //
        // The reason is the server's own line, verbatim, which is what the recorded transaction
        // part carries too - so the notice reads the same live as after a reload.
        const data = frameData<{ reason?: string }>(e.data, {})
        this.dispatch({
          type: 'ADD_NOTICE',
          kind: 'transaction_rolled_back',
          detail: data.reason ?? '',
        })
      })

      on('model_switched', (e) => {
        // A notice, not an error: the run did not fail, it continued on another model, and the
        // answer under this line comes from that one. Written into the transcript for the same
        // reason the provider error is (#429) - it is part of what happened in this turn, and the
        // composer's strip is wiped by the next send. Composed in exactly the wording the server
        // persists, so the line reads the same live as after a reload.
        const data = frameData<{ fromModel?: string; toModel?: string; reason?: string }>(e.data, {})
        this.dispatch({
          type: 'ADD_NOTICE',
          kind: 'model_switched',
          detail: `${data.fromModel ?? ''} -> ${data.toModel ?? ''}: ${data.reason ?? ''}`,
        })
      })

      es.onerror = () => {
        // The one refusal a new stream cannot cure is that of a window the server does not know:
        // the bridge is loading the page again by then, and asking again would only repeat it,
        // once a second, for as long as the page is open. Any other failure - a server that is
        // restarting answers nothing useful yet - is tried again, and the window's own stream
        // reloads the page once the server is back.
        if (isPageLeaving()) return
        this.patch({ connection: 'reconnecting' })
        reopen()
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
    // Whatever the strip says now, it is no longer the waiting line; applyRunWaiting marks it again.
    this.waitingNotice = false
  }

  /** Takes the waiting line down: the model answered, or the run is over. */
  private clearRetryNotice(): void {
    if (!this.retryNotice) return
    this.retryNotice = false
    this.showNotice('')
  }

  /**
   * Says in the strip that the send waits for a free worker, at which place, or takes that line down
   * once it no longer waits.
   *
   * A line of the strip, not of the transcript: it is a state of the moment, and the run that ends
   * the wait starts its turn right after.
   */
  private applyRunWaiting(waiting: RunWaiting): void {
    if (waiting.position > 0 && waiting.text) {
      this.showNotice(waiting.text, '', { tone: 'info' })
      this.waitingNotice = true
    } else {
      this.clearWaitingNotice()
    }
  }

  /** Takes the waiting line down: the run started, or it was stopped while it waited. */
  private clearWaitingNotice(): void {
    if (!this.waitingNotice) return
    this.waitingNotice = false
    this.showNotice('')
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

  /**
   * Adds a piece of the answer to the message being written.
   *
   * `end` is where the piece ends in the run's text; zero when the server does not say. A piece
   * that ends inside what this window already shows is cut to what is new, and dropped when nothing
   * is (#544).
   */
  private appendDelta(text: string, end: number): void {
    if (this.opening) {
      this.opening.held.push({ text, end })
      return
    }
    let piece = text
    if (this.streamMark !== null && end > 0) {
      if (end <= this.streamMark) return
      const start = end - text.length
      if (start < this.streamMark) piece = text.slice(this.streamMark - start)
      this.streamMark = end
    }
    if (!this.assistantId) {
      // A window that saw the run start has a message from `turn_started`. One that joined it from
      // a snapshot taken between a step and the next piece of text has none to write into yet.
      if (!this.watching) return
      this.assistantId = randomId()
      this.lastAssistantId = this.assistantId
      this.dispatch({ type: 'ADD_ASSISTANT', id: this.assistantId, text: '', isStreaming: true })
    }
    this.dispatch({ type: 'APPEND_TEXT', id: this.assistantId, delta: piece })
  }

  /**
   * Continues the run a snapshot was taken of, from where the snapshot ends (#544).
   *
   * The snapshot's last paragraph is the one the agent is still writing, so the stream goes on in it
   * - unless the turn's last entry is a step, in which case the next text is a new paragraph. The
   * deltas that arrived while the snapshot was on its way are placed now that its end is known.
   */
  private joinRun(inFlight: SessionDetail['inFlight']): void {
    const held = this.opening?.held ?? []
    this.opening = null
    if (!inFlight) return
    this.watching = true
    this.streamMark = inFlight.streamedChars ?? 0
    // The running turn is everything after the last user message. Its "worked for" is not known
    // yet, so it says it is working, as it does in the window that started it.
    const messages = this.state.messages
    const question = messages.map((m) => m.type).lastIndexOf('user')
    const meta = messages.slice(question + 1).find((m) => m.type === 'run_meta')
    if (meta?.type === 'run_meta') {
      this.runId = meta.runId
      this.dispatch({ type: 'RESUME_RUN', runId: meta.runId })
    }
    const last = messages[messages.length - 1]
    if (last?.type === 'assistant_text') {
      this.assistantId = last.id
      this.lastAssistantId = last.id
      this.dispatch({ type: 'RESUME_TEXT', id: last.id })
    }
    for (const delta of held) this.appendDelta(delta.text, delta.end)
  }

  /**
   * Puts the run this chat may still be in back on screen.
   *
   * The transcript travels with the session, but the stream replays nothing: after a reload the
   * progress list is empty, the composer is idle and the open card - an approval, or a question the
   * agent asked - is gone, while the agent is still parked on it. The `run_state` frame says whether
   * the run is working, what it has got through, what is waiting for this user and which subagents
   * the run has; the server sends it when the stream is attached and again on
   * {@link CMD_REFRESH_RUN_STATE}, and answers the open of a chat with it ({@link openSession}). A
   * state of another chat than the one on screen - the window has moved on, or is still opening
   * one - is not this chat's.
   */
  private applyRunState(run: RunState): void {
    const sessionId = this.state.sessionId
    if (!run.sessionId || run.sessionId !== sessionId || this.opening) return
    // First, so that a request a subagent asks names the subagent the list knows.
    this.patch({ subagents: run.subagents, subagentBudget: run.budget })
    this.patch({ busy: run.processing, tasks: parseTasksJson(run.tasksJson) })
    if (run.waiting) this.applyRunWaiting(run.waiting)
    if (run.processing) {
      // The last answer of a restored transcript is the paragraph the agent is still writing, not
      // a finished one, so it is shown as streaming and carries no answer actions (#512).
      const open = [...this.state.messages].reverse().find((m) => m.type === 'assistant_text')
      if (open) this.dispatch({ type: 'RESUME_TEXT', id: open.id })
    }
    for (const item of run.interactions) {
      // A request past its deadline can no longer be answered: the agent has stopped waiting for
      // it, and a card nobody is listening to would lock the composer behind a decision that leads
      // nowhere.
      if (item.remainingMs <= 0) continue
      if (item.type === 'approval') {
        // Dispatched even when the restored transcript already carries the card: the reducer
        // completes that one in its place under the turn that asked for it, rather than appending
        // a second (#512). What it adds is what only the waiting run knows - the tool's mode, and
        // how long is left to answer.
        this.dispatch({
          type: 'ADD_APPROVAL',
          approvalRequestId: item.requestId,
          toolCallId: '',
          toolName: item.header,
          argsJson: item.payload,
          displayReason: item.prompt,
          toolMode: item.toolMode,
          remainingMs: item.remainingMs,
          askedBy: asker(item.askedBySubagentId, item.askedByAgent, this.state.subagents),
        })
        continue
      }
      if (item.type !== 'question' && item.type !== 'choice') continue
      // The card is rebuilt with the shape the agent asked in, not with a default: a multi-select
      // question drawn as a radio group would take a different answer from the one it asked for
      // (#466). The shape comes off the waiting run, so an item that reaches here has one.
      this.dispatch({
        type: 'ADD_INTERACTION',
        requestId: item.requestId,
        question: item.prompt,
        header: item.header,
        interactionType: item.type === 'choice' ? 'choice' : 'text',
        options: item.options.length > 0 ? item.options : undefined,
        multiSelect: item.multiSelect,
        remainingMs: item.remainingMs,
        askedBy: asker(item.askedBySubagentId, item.askedByAgent, this.state.subagents),
      })
    }
  }

  /**
   * Reads the open conversation back from the server, keeping everything else as it is.
   *
   * A window that is only watching a conversation has no stream of its own to build the turn from:
   * the run's events go to the window that started it. When such a run ends, the recorded turn is
   * the whole of what this window knows, so it is fetched once rather than left as the snapshot the
   * window was restored with. Best effort - a failure leaves the transcript standing, which is what
   * a window that never asked would have shown.
   */
  private async reloadTranscript(): Promise<void> {
    const sessionId = this.state.sessionId
    if (!sessionId) return
    try {
      const detail = await loadSession(this.dataUrl(), sessionId)
      // The user may have moved to another chat while this was in flight.
      if (this.state.sessionId !== sessionId) return
      this.dispatch({ type: 'REPLACE', messages: turnsJsonToMessages(detail.turnsJson, detail.runs) })
    } catch {
      // Best effort: what is on screen is a turn of this conversation either way.
    }
  }

  /**
   * Asks for the conversation's `run_state` frame again, which brings the subagents with their costs
   * and budget: the live `subagent_*` frames keep their status current, but not what they cost.
   * The answer arrives on the stream ({@link applyRunState}).
   */
  async refreshSubagents(): Promise<void> {
    await this.command(CMD_REFRESH_RUN_STATE)
  }

  /** The settings of a turn, as the composer shows them now. */
  private get turnSettings(): TurnSettings {
    return {
      agent: this.state.agent,
      model: this.state.model,
      mcpOverride: this.state.mcpDirty,
      mcpServers: this.state.mcp,
      effort: this.state.effort || undefined,
    }
  }

  /**
   * Sends a turn: a new message, or with `edit` the edited message in place of the turn the user
   * changed.
   *
   * A send in a conversation that has no chat yet is what brings the chat into being, titled after
   * what was said; the server creates it with the send, answers with its id, and takes it back
   * again when the message could not be sent. A new message carries the files attached to it,
   * once those on their way have arrived; an edited one carries none.
   *
   * @returns whether the text left the composer.
   */
  private async startTurn(
    text: string,
    options: SendOptions,
    edit: { messageId: string; turnIndex: number } | null,
  ): Promise<boolean> {
    const labels = this.labels
    const active = this.state.sessionId
    if (edit && !active) return false
    if (!edit) await this.uploads
    this.showNotice('')
    // Composed before the dispatch below, because the host's prefix describes the conversation
    // this message is being added to, not the one it is already in. `App` got that for free: it
    // read a ref assigned during render, which a `useReducer` dispatch in the same handler
    // cannot have updated. This store's dispatch does update its state at once, so reading it
    // afterwards would hand the host the outgoing message - and a host that counts turns would
    // put the wrong number into every prompt.
    const outbound = composeOutboundText(text, options.contextTag?.({ messages: this.state.messages }))
    const sent = edit ? [] : this.state.draftAttachments
    // What an edit takes away, to put back when the server refuses it; and whether a run was
    // already going, which a refused send must not end in the composer.
    const before = this.state.messages
    const wasBusy = this.state.busy
    const feedback = this.state.feedback
    if (edit) {
      this.dispatch({ type: 'REMOVE_FROM_USER', id: edit.messageId })
      this.patch({ feedback: feedback.filter((entry) => entry.turnIndex < edit.turnIndex) })
      this.setRun((snapshot) => ({ ...snapshot, measuredTokens: 0 }))
    }
    // Appended before the request, not after it: the server starts streaming the answer the
    // moment it accepts the message, and an await here would let the first assistant event
    // reach the reducer first - on a new chat the answer then appeared above the question.
    // A refused send takes the message back out again.
    const messageId = randomId()
    this.dispatch({
      type: 'ADD_USER',
      id: messageId,
      text: text || labels.attachmentOnly,
      edited: options.edited,
    })
    this.patch({ busy: true })
    this.setRun((s) => ({ ...s, busy: true }))
    const request: SendRequest = {
      text: outbound,
      sessionId: active ?? '',
      title: active ? undefined : autoTitle(text || labels.attachmentOnly),
      // The turn runs with what the switch shows now, not with what reached the server; a chat the
      // send creates stores it.
      approvalMode: this.state.autoApprove ? 'AUTO_APPROVE' : 'MANUAL',
      ...this.turnSettings,
    }
    // Kept for the reloaded page, should the page be reloaded under the user's hands because the
    // server no longer knows it: the command then never answers here.
    keepUnsent(this.chatId, text)
    let answer: TurnAnswer
    try {
      answer = edit
        ? await editSendTurn(this.command, edit.turnIndex, request)
        : await sendMessage(this.command, request)
    } catch (err) {
      if (!isPageLeaving()) takeUnsent(this.chatId)
      if (edit) {
        this.dispatch({ type: 'REPLACE', messages: before })
        this.patch({ feedback })
      } else {
        this.dispatch({ type: 'REMOVE_MESSAGE', id: messageId })
      }
      this.patch({ busy: wasBusy })
      this.setRun((s) => ({ ...s, busy: wasBusy }))
      // A refusal is not a failure: the server wrote a sentence for this user saying what they
      // may not use, and it reads as information rather than as something that went wrong.
      // A RefusedError says the same thing about a ceiling that will pass, so it is not sticky -
      // the answer is to send again in a moment, and a strip the user has to dismiss first is in
      // the way of exactly that.
      if (err instanceof ForbiddenError) {
        this.showNotice(err.reason || labels.notAllowed, '', {
          tone: 'info',
          sticky: !(err instanceof RefusedError),
        })
      } else if (err instanceof BusyError) {
        this.showNotice(labels.busyHint, '', { tone: 'info' })
      } else {
        this.showNotice(labels.sendFailed, '', { tone: 'warn' })
      }
      return false
    }
    takeUnsent(this.chatId)
    const sessionId = answer.sessionId || active
    if (!active && sessionId) {
      // The chat this send created: in the list at once, titled after what was said; the server's
      // list follows with the command's model events.
      this.patch({
        sessionId,
        sessions: withSession(this.state.sessions, { id: sessionId, title: request.title ?? '' }),
      })
    }
    // The send is where a new chat learns who answers it.
    if (answer.features && this.state.sessionId === sessionId) {
      const features = readFeatures(answer.features)
      if (!sameFeatures(features, this.state.features)) this.patch({ features })
    }
    if (sent.length > 0) {
      // The server took them off with the send; until its state says so, the composer must not
      // show them either.
      const gone = new Set(sent.map((file) => file.id))
      this.patch({ draftAttachments: this.state.draftAttachments.filter((file) => !gone.has(file.id)) })
    }
    // Dropping the edited turn is what editing always does; saying so only helps when answers
    // the user could still see went with it. Sticky: the run start that follows used to clear the
    // strip in the same instant this line was written into it.
    if (edit && answer.removedTurns > 1) {
      this.showNotice(labels.laterTurnsRemoved(answer.removedTurns - 1), '', { tone: 'info', sticky: true })
    }
    return true
  }

  private declareActions(): ChatActions {
    return {
      dispatch: this.dispatch,
      showNotice: this.showNotice,

      selectSession: async (id: string) => {
        // The chat the latest choice leads to: the one being opened, or else the one on screen.
        // Asking for it again must not start a second opening, which could only land late (#577).
        if (id === (this.opening?.sessionId ?? this.state.sessionId)) return
        const choice = ++this.choice
        this.assistantId = null
        this.runId = null
        this.streamMark = null
        this.watching = false
        this.patch({ busy: false, ...EMPTY_CONVERSATION })
        this.noticeSticky = false
        // The model does not change when the chat does, so its window survives the reset; only what
        // the previous conversation measured is dropped.
        this.setRun((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
        this.opening = { sessionId: id, held: [] }
        let detail: SessionDetail
        let runState: RunState | undefined
        try {
          // The chat becomes the conversation's on the server first: the transcript is read from it.
          runState = await openSession(this.command, id)
          detail = await loadSession(this.dataUrl(), id)
        } catch (error) {
          if (choice === this.choice) this.opening = null
          throw error
        }
        // Another chat was chosen while this one loaded: that choice is what the window shows.
        if (choice !== this.choice) return
        this.patch({
          sessionId: id,
          sessions: withTitle(this.state.sessions, id, detail.title),
          messages: turnsJsonToMessages(detail.turnsJson, detail.runs),
          feedback: detail.feedback ?? [],
          features: readFeatures(detail.features),
          ...(detail.lastEffort ? { effort: detail.lastEffort } : {}),
          // The switch shows the mode the server now runs this chat with, not the one the
          // previous chat or the page load left behind (#658); a chat that stores none is
          // manual (#808).
          autoApprove: detail.approvalMode === 'AUTO_APPROVE',
        })
        this.joinRun(detail.inFlight)
        // A re-opened session shows what its last request really cost, not a fresh estimate.
        this.setRun((snapshot) => ({
          ...snapshot,
          model: detail.lastModel || snapshot.model,
          contextWindow: detail.contextWindow || snapshot.contextWindow,
          lastUsage: detail.lastPromptUsage ?? snapshot.lastUsage,
          measuredTokens: promptTokens(detail.lastPromptUsage),
          composition: detail.lastComposition ?? undefined,
        }))
        // The run of the chat now open, as the server answered the open: applied now that the
        // transcript it belongs to is on screen.
        if (runState && choice === this.choice) this.applyRunState(runState)
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
        this.choice += 1
        this.opening = null
        this.assistantId = null
        this.runId = null
        this.noticeSticky = false
        this.patch({
          sessionId: null,
          // A new chat starts with the person's last choice, never with a reset: a person who has
          // switched auto-approve on is not asked again because they started another chat.
          autoApprove: this.approvalChoice,
          ...EMPTY_CONVERSATION,
          messages: chatReducer(this.snapshot.messages, { type: 'RESET' }),
          busy: false,
        })
        this.setRun((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
      },

      deleteSession: async (id: string) => {
        await deleteSession(this.command, id)
        // The server sends the list without it; until then the rail must not show it either.
        this.patch({ sessions: withoutSession(this.state.sessions, id) })
        if (this.state.sessionId === id) {
          this.noticeSticky = false
          this.patch({
            sessionId: null,
            autoApprove: this.approvalChoice,
            ...EMPTY_CONVERSATION,
            messages: chatReducer(this.snapshot.messages, { type: 'RESET' }),
          })
          this.setRun((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
        }
      },

      /**
       * Sends a turn.
       *
       * @returns whether the text left the composer. A refused send keeps the draft where the user
       *          can still see and re-send it, which is why this is not fire-and-forget.
       */
      send: (text: string, options: SendOptions = {}) => this.startTurn(text, options, null),

      /**
       * Sends an edited message in place of the one the user changed.
       *
       * Nothing happens until this point: the bubble is edited in the thread, and only a send drops
       * the turn and everything after it - here and on the server, in the one command that sends the
       * new wording, so nothing can come between the two. A cancelled edit leaves the conversation
       * exactly as it was, and so does an edit the server refuses.
       */
      editSend: (messageId: string, turnIndex: number, text: string, options: SendOptions = {}) =>
        this.startTurn(text, { ...options, edited: true }, { messageId, turnIndex }),

      /**
       * Answers the last message again.
       *
       * The old answer is not kept beside the new one: the user asked for that turn to be done
       * differently. The server drops it and re-sends the same message; what streams in takes its
       * place.
       */
      regenerate: async (messageId: string, turnIndex: number) => {
        const labels = this.labels
        const active = this.state.sessionId
        if (!active || this.state.regenerating) return
        this.showNotice('')
        this.patch({ regenerating: true })
        try {
          const result = await regenerateTurn(this.command, active, this.turnSettings)
          // The turn is gone server side; drop it here too and let the run rebuild it.
          this.dispatch({ type: 'REMOVE_FROM_USER', id: messageId })
          this.patch({ feedback: this.state.feedback.filter((entry) => entry.turnIndex !== turnIndex) })
          this.dispatch({ type: 'ADD_USER', id: randomId(), text: result.text })
          this.patch({ busy: true })
          this.setRun((snapshot) => ({ ...snapshot, busy: true, measuredTokens: 0 }))
        } catch (err) {
          if (err instanceof BusyError) {
            this.showNotice(labels.busyHint, '', { tone: 'info' })
          } else if (err instanceof ForbiddenError) {
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
        const active = this.state.sessionId
        if (!active) return
        const others = this.state.feedback.filter((entry) => entry.turnIndex !== turnIndex)
        this.patch({ feedback: rating ? [...others, { turnIndex, rating, note }] : others })
        try {
          await rateAnswer(this.command, active, turnIndex, rating, note)
        } catch {
          // The rating is a convenience, not the conversation: a failed write is not worth an error
          // banner, but it must not look recorded either.
          this.patch({ feedback: this.state.feedback.filter((entry) => entry.turnIndex !== turnIndex) })
        }
      },

      stop: () => {
        // Named, so that the stop reaches the run even when this is not the window that started it.
        void cancelRun(this.command, this.state.sessionId)
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

      attach: async (files: File[]) => {
        const labels = this.labels
        const { usable, problems } = selectAttachments(
          files,
          this.state.draftAttachments.length + this.attaching,
          labels,
        )
        if (usable.length > 0) {
          this.attaching += usable.length
          const upload = uploadAttachments(this.upload, usable)
            .catch(() => [labels.attachFailed])
            .finally(() => {
              this.attaching -= usable.length
            })
          // A send waits for what is on its way, so the file goes with the message it was meant for.
          const before = this.uploads
          this.uploads = Promise.all([before, upload]).then(() => undefined)
          problems.push(...(await upload))
        }
        return problems.join(' · ')
      },

      removeAttachment: (id: string) => {
        this.patch({ draftAttachments: this.state.draftAttachments.filter((file) => file.id !== id) })
        void removeAttachment(this.command, id)
      },

      selectTraceMode: (mode: TraceMode) => {
        this.patch({ traceMode: mode })
        writeTraceMode(mode)
      },

      toggleAutoApprove: async () => {
        const next = !this.state.autoApprove
        const previous = this.approvalChoice
        this.patch({ autoApprove: next })
        this.approvalChoice = next
        // The server stores the switch as the person's choice, and with the open chat. A
        // conversation that has no chat yet names the mode with the send that creates the chat.
        try {
          await setApprovalMode(this.command, next ? 'AUTO_APPROVE' : 'MANUAL', this.state.sessionId)
        } catch {
          this.patch({ autoApprove: !next })
          this.approvalChoice = previous
          return
        }
        if (!next) return
        for (const message of this.state.messages) {
          if (message.type !== 'approval_request' || message.outcome !== 'pending') continue
          void respond(this.command, {
            requestId: message.approvalRequestId,
            response: 'APPROVED',
            toolName: message.toolName,
          }).catch(() => undefined)
          this.dispatch({
            type: 'RESOLVE_APPROVAL',
            approvalRequestId: message.approvalRequestId,
            outcome: 'APPROVED',
          })
        }
      },

      respond: (answer: InteractionAnswer) => respond(this.command, answer),

      improvePrompt: (draft: string, requestId: string) =>
        new Promise<PromptImproved>((resolve, reject) => {
          // Before the command goes: its result may come on the stream before the answer does.
          this.improvements.set(requestId, resolve)
          startPromptImprovement(this.command, draft, requestId).catch((err: unknown) => {
            this.improvements.delete(requestId)
            reject(err)
          })
        }),

      cancelPromptImprovement: (requestId: string) => {
        const done = this.improvements.get(requestId)
        this.improvements.delete(requestId)
        done?.({ requestId, status: 'cancelled', improvedText: '', reason: '' })
        void cancelPromptImprovement(this.command, requestId)
      },

      cancelSubagent: async (id: string, parentToolCallId?: string) => {
        await cancelSubagent(this.command, id, this.state.sessionId, parentToolCallId)
        this.patch({
          subagents: this.state.subagents.map((item) =>
            item.subagentId === id ? { ...item, status: 'CANCELLED' } : item,
          ),
        })
      },

      messageSubagent: async (id: string, parentToolCallId: string, text: string) => {
        const answer = await sendSubagentMessage(this.command, id, this.state.sessionId, parentToolCallId, text)
        if (answer.status === 'sent') {
          this.patch({
            subagentLive: { ...this.state.subagentLive, [id]: addNote(this.state.subagentLive[id], answer.messageId, text) },
          })
        }
        return answer.status
      },

      refreshSubagents: () => this.refreshSubagents(),

      stopSubagents: async () => {
        const active = this.state.subagents.filter(
          (item) => !isEnded(item.status),
        )
        const sessionId = this.state.sessionId
        await Promise.all(active.map((item) => cancelSubagent(this.command, item.subagentId, sessionId, item.parentToolCallId)))
        this.patch({
          subagents: this.state.subagents.map((item) =>
            isEnded(item.status) ? item : { ...item, status: 'CANCELLED' },
          ),
        })
      },

      loadSubagentTranscript: (id: string, runId?: string) => loadSubagentTranscript(this.dataUrl(), id, runId),

      openPane: (content: ReactNode, options: PaneOpenOptions) => {
        // Opening a pane opens the side, and that is the user opening it: a reload shows what they
        // were looking at rather than the element's default.
        this.patch({ paneContent: content, paneOptions: options, ...this.opened() })
      },

      closePane: () => this.patch({ paneContent: null, paneOptions: null }),

      closeSide: () => this.patch({ paneContent: null, paneOptions: null, ...this.closed() }),

      toggleSide: (badgeTotal: number) => {
        const open = this.state.railDrawer ? this.state.railDrawerOpen : this.state.sideOpen
        if (open) {
          this.patch({ paneContent: null, paneOptions: null, ...this.closed() })
          return
        }
        this.patch({ ...this.opened(), badgeSeen: badgeTotal })
      },

      showSection: (id: string, badgeTotal: number) => {
        this.patch({
          paneContent: null,
          paneOptions: null,
          ...this.opened(),
          badgeSeen: badgeTotal,
          collapsedSections: this.state.collapsedSections.filter((item) => item !== id),
          sectionChoices: { ...this.state.sectionChoices, [id]: true },
        })
      },

      setRailDrawer: (rail: object, drawer: boolean) => {
        if (drawer) this.drawerRails.add(rail)
        else this.drawerRails.delete(rail)
        const next = this.drawerRails.size > 0
        if (next === this.state.railDrawer) return
        // Either way the drawer starts closed: a host that grows wide again takes the rail back as
        // the column the user chose, and one that narrows does not open over the conversation on
        // its own.
        this.patch({ railDrawer: next, railDrawerOpen: false })
      },

      setPaneWidth: (requested: number) => {
        // Whole pixels, because that is what the preference stores and the server accepts (#588).
        const width = Math.round(requested)
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
        // unchanged watermark would put a `saveLayout` on every open of the dock.
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

      followChannel: () => {
        this.restored = true
      },

      registerConversation: (insideDock: boolean) => {
        if (insideDock) return () => undefined
        this.conversations += 1
        this.patch({ conversationsOutsideDock: this.conversations })
        let released = false
        return () => {
          if (released) return
          released = true
          this.conversations = Math.max(0, this.conversations - 1)
          this.patch({ conversationsOutsideDock: this.conversations })
        }
      },

      bindComposer: (api) => {
        this.composerApi = api
        if (!api || this.composerBound) return
        this.composerBound = true
        // A message the last page could not send because its window was gone: the reload is what
        // brought this composer, and the user's text goes back where they left it.
        const unsent = takeUnsent(this.chatId)
        if (unsent) api.setDraft(unsent)
      },

      insertReference: (token) => {
        this.composerApi?.insertReference(token)
      },

      setDraft: (text) => {
        this.composerApi?.setDraft(text)
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
  /** This chat's run snapshot - what the context ring beside its composer reads. */
  runs: RunStore
}

/**
 * Joins the chat with the given id.
 *
 * Every element that passes the same id sees the same conversation, the same run and the same one
 * stream; the first to arrive opens the stream and the last to leave closes it. Must be called by a
 * component the bridge mounted for a chat control (`ChatControl` on the server): the store sends its
 * stream commands through the element's control.
 */
export function useChat(chatId: string, boot: ChatBootstrap): ChatHandle {
  // The bootstrap seeds a store that does not exist yet; an existing one keeps what it has and
  // takes only the fields `attach` below reacts to, so a second element joining changes nothing.
  const store = useMemo(() => chatStore(chatId, boot), [chatId])
  // The element's own control is how the store reaches the server through this element.
  const command = useTLCommand()
  const dataUrl = useTLDataUrl()
  const upload = useTLUpload()
  const subscribe = useCallback(
    (listener: () => void) => store.subscribe(listener, command, dataUrl, upload),
    [store, command, dataUrl, upload],
  )
  const chat = useSyncExternalStore(subscribe, store.getState, store.getState)
  useEffect(
    () => store.attach(boot),
    // The fields, not the object: a host that rebuilds the bootstrap on every render must not
    // re-run what only a sign-in or a language switch really changes.
    [store, boot.authenticated, boot.userName, boot.welcomeName, boot.language],
  )
  // The state's own values: they change only when the server sends new ones.
  useEffect(() => store.takeLists(boot.capabilities, boot.sessions), [store, boot.capabilities, boot.sessions])
  useEffect(() => store.takeAttachments(boot.draftAttachments), [store, boot.draftAttachments])
  return { chat, actions: store.actions, runs: store.runs }
}
