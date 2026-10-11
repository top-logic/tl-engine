import { argsSummary } from './plain-text'
import { draftToText, parseDraft, referenceSummary } from './reference'
import { chatFetch } from './stale-window'
import type {
  ApprovalOutcome,
  Attachment,
  Capabilities,
  ChatMessage,
  ContextComposition,
  ContextFact,
  ImprovePromptResponse,
  InteractionOption,
  SendPayload,
  SessionSummary,
  MessageFeedback,
  Rating,
  SubagentActivity,
  SubagentBudget,
  SubagentStatus,
  SubagentStep,
  TokenUsage,
  ToolCallStatus,
  WokenSubagent,
} from './types'

export function apiBase(): string {
  const path = window.location.pathname
  for (const marker of ['/view/', '/servlet/']) {
    const idx = path.indexOf(marker)
    if (idx >= 0) return path.substring(0, idx)
  }
  return ''
}

/**
 * Where a `/chat/*` call goes: the browser window, and which conversation of it.
 *
 * A window can show more than one - a page's chat beside a `<chat-dock>`'s, two `<agent-chat>`
 * elements - and each has its own active session, its own run and its own event stream on the
 * server. A bare window id is the window's unnamed conversation, which is what a host calling
 * these functions with a window id alone gets (the classic `chat-client.js` was one until #618).
 *
 * The parameter is `conversation` rather than `chatId` because `PUT /chat/preferences` already
 * spells a `chatId`, and that one is the key the *layout* is stored under - the same string for
 * every element of a chat, and the empty one for an `<agent-chat>`, whose conversation is its own.
 */
export type ChatAddress = string | { windowId: string; chatId: string }

function withWindow(path: string, at: ChatAddress): string {
  const sep = path.includes('?') ? '&' : '?'
  const windowId = typeof at === 'string' ? at : at.windowId
  const chatId = typeof at === 'string' ? '' : at.chatId
  const conversation = chatId ? `&conversation=${encodeURIComponent(chatId)}` : ''
  return `${apiBase()}${path}${sep}windowId=${encodeURIComponent(windowId)}${conversation}`
}

async function readJson<T>(res: Response): Promise<T> {
  if (res.status === 403) {
    throw await forbidden(res)
  }
  if (res.status === 429) {
    throw await refused(res)
  }
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return (await res.json()) as T
}

/**
 * A request the account may not make, with the reason the server wrote for this user.
 *
 * An error of its own, because the caller has something to show rather than something to report: the
 * reason is a finished sentence in the user's language, not a status to translate.
 */
export class ForbiddenError extends Error {
  constructor(public readonly reason: string) {
    super(reason || 'forbidden')
    this.name = 'ForbiddenError'
  }
}

/**
 * A request this installation has no room for right now, with its own sentence.
 *
 * A `ForbiddenError`, so that every caller which already shows a refusal as information shows this
 * one the same way - what differs is only the cause, and the server's sentence says it. Its own
 * class so that a caller which wants to offer "try again" can tell the two apart.
 */
export class RefusedError extends ForbiddenError {
  constructor(reason: string) {
    super(reason)
    this.name = 'RefusedError'
  }
}

/** Reads the reason out of a 403, falling back to nothing rather than to the status line. */
async function forbidden(res: Response): Promise<ForbiddenError> {
  try {
    const data = (await res.json()) as { reason?: string }
    return new ForbiddenError(data.reason ?? '')
  } catch {
    return new ForbiddenError('')
  }
}

/** Reads the reason out of a 429, the same way. */
async function refused(res: Response): Promise<RefusedError> {
  try {
    const data = (await res.json()) as { reason?: string }
    return new RefusedError(data.reason ?? '')
  } catch {
    return new RefusedError('')
  }
}

export async function fetchCapabilities(at: ChatAddress): Promise<Capabilities> {
  const data = await readJson<{
    entryPointAgent?: string
    agents?: Capabilities['agents']
    models?: Capabilities['models']
    mcpServers?: Capabilities['mcpServers']
    promptImproverAvailable?: boolean
  }>(await chatFetch(withWindow('/chat/capabilities', at), { credentials: 'include' }))
  return {
    entryPointAgent: data.entryPointAgent,
    agents: data.agents ?? [],
    models: data.models ?? [],
    mcpServers: data.mcpServers ?? [],
    promptImproverAvailable: data.promptImproverAvailable !== false,
  }
}

export async function listSessions(at: ChatAddress): Promise<SessionSummary[]> {
  const data = await readJson<{ sessions?: SessionSummary[] }>(
    await chatFetch(withWindow('/chat/sessions', at), { credentials: 'include' }),
  )
  return data.sessions ?? []
}

export async function createSession(at: ChatAddress, title: string): Promise<SessionSummary> {
  return readJson<SessionSummary>(
    await chatFetch(withWindow('/chat/sessions', at), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ title }),
    }),
  )
}

export interface SessionDetail {
  id: string
  title: string
  turnsJson?: string
  /**
   * Provider usage of the last agent-loop iteration the session ran, so re-opening it restores a
   * measured context figure. Absent for a session that never ran a turn, and for one whose
   * transcript predates the field.
   */
  lastPromptUsage?: TokenUsage
  /** Context window of the model that ran the last turn; zero when that model is unknown here. */
  contextWindow?: number
  lastModel?: string
  /** Answer ratings recorded in this session, so a reopened chat shows them again. */
  feedback?: MessageFeedback[]
  lastEffort?: string
  /** The chat's approval mode as the server holds it; the auto-approve switch shows this. */
  approvalMode?: 'AUTO_APPROVE' | 'MANUAL' | ''
  /** What the last request's prompt was made of, so the popover keeps its breakdown after a reload. */
  lastComposition?: ContextComposition
  /**
   * The run this conversation was in the middle of when it was read, and how far that run's text
   * had got, in the count every `text_delta` carries as `end`. Absent when no run was in flight.
   */
  inFlight?: { streamedChars?: number }
}

export async function loadSession(at: ChatAddress, id: string): Promise<SessionDetail> {
  return readJson<SessionDetail>(
    await chatFetch(withWindow(`/chat/sessions/${encodeURIComponent(id)}`, at), {
      credentials: 'include',
    }),
  )
}

export async function deleteSession(at: ChatAddress, id: string): Promise<void> {
  const res = await chatFetch(withWindow(`/chat/sessions/${encodeURIComponent(id)}`, at), {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!res.ok) throw new Error(`delete failed (${res.status})`)
}

export async function sendMessage(at: ChatAddress, payload: SendPayload): Promise<{ taskId?: string; status: string }> {
  const res = await chatFetch(withWindow('/chat/send', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(payload),
  })
  if (res.status === 403) {
    throw await forbidden(res)
  }
  if (res.status === 429) {
    throw await refused(res)
  }
  const data = (await res.json()) as { taskId?: string; status?: string }
  if (res.status === 409 || data.status === 'busy') {
    throw new Error('busy')
  }
  if (!res.ok || data.status === 'error') {
    throw new Error(data.status ?? `send failed (${res.status})`)
  }
  return { taskId: data.taskId, status: data.status ?? 'processing' }
}

/**
 * Asks for the last answer again.
 *
 * The previous turn is removed server side before the same message is sent once more, so the new
 * answer takes the old one's place instead of following it.
 */
export async function regenerate(
  at: ChatAddress,
  payload: {
    sessionId: string
    agent?: string
    model?: string
    mcpOverride: boolean
    mcpServers: string[]
    effort?: string
    facts?: ContextFact[]
  },
): Promise<TruncateResult> {
  return readJson<TruncateResult>(
    await chatFetch(withWindow('/chat/regenerate', at), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(payload),
    }),
  )
}

/** Drops a turn and everything after it, and hands back the user text of the dropped turn. */
export async function truncate(
  at: ChatAddress,
  sessionId: string,
  fromTurn: number,
): Promise<TruncateResult> {
  return readJson<TruncateResult>(
    await chatFetch(withWindow('/chat/truncate', at), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ sessionId, fromTurn }),
    }),
  )
}

export interface TruncateResult {
  status: 'ok' | 'busy' | 'rejected' | 'error'
  removedTurns?: number
  text?: string
  taskId?: string
}

/** Records, changes or withdraws what the user thought of one answer. */
export async function sendFeedback(
  at: ChatAddress,
  sessionId: string,
  turnIndex: number,
  rating: Rating,
  note?: string,
): Promise<void> {
  const res = await chatFetch(withWindow('/chat/feedback', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ sessionId, turnIndex, rating: rating ?? '', note: note ?? '' }),
  })
  if (!res.ok) throw new Error(`feedback failed (${res.status})`)
}

/** What a run that is still in flight looks like from outside the stream. */
export interface RunSnapshot {
  /** Whether the open session's agent is still working. */
  processing: boolean
  /** The run's task list, in the format the `task_update` frame carries; empty when there is none. */
  tasksJson: string
}

/**
 * The open session's run, as the server sees it now.
 *
 * The SSE stream replays nothing, so a page that was reloaded in mid-run learns from here that the
 * agent is still working and what it has got through so far.
 */
export async function fetchRunSnapshot(at: ChatAddress): Promise<RunSnapshot> {
  const data = await readJson<{ processing?: boolean; tasksJson?: string }>(
    await chatFetch(withWindow('/chat/updates', at), { credentials: 'include' }),
  )
  return { processing: data.processing === true, tasksJson: data.tasksJson ?? '' }
}

/** One human-in-the-loop request waiting for this user, as `/chat/interactions` reports it. */
export interface PendingInteraction {
  requestId: string
  /** `approval`, `question` or `choice`. */
  type: string
  /** Short label; for an approval the tool's name. */
  header: string
  /** For an approval the display reason. */
  prompt: string
  /** For an approval the tool call's argument JSON. */
  payload: string
  /**
   * For an approval still being waited on, the tool's mode - what the card's sentence is phrased
   * from, so a recovered card reads in the user's language as the live one does. Empty otherwise.
   */
  toolMode: string
  /**
   * For a question of type `choice`, whether it takes more than one answer. Read off the waiting
   * run, like `toolMode`: a rebuilt card that turned a multi-select question into a single-select
   * one would ask something the agent did not (#466).
   */
  multiSelect: boolean
  /** For a question of type `choice`, the options to offer, in the order the agent gave them. */
  options: InteractionOption[]
  sessionId: string
  /** How long the requester is still waiting; zero or less means nobody is listening any more. */
  remainingMs: number
  /** The background subagent that asks, read off the store it waits in; absent or empty otherwise. */
  askedBySubagentId?: string
  /** The agent the asking subagent runs. */
  askedByAgent?: string
}

/**
 * Every approval and question waiting for this user, across their chats.
 *
 * Neither has a server poll of its own that a new window hears - the stream re-emits a pending
 * question only into the window whose run it is - so this is what lets a reloaded page put the card
 * back in front of the user instead of a composer that is locked for no visible reason.
 */
export async function fetchPendingInteractions(at: ChatAddress): Promise<PendingInteraction[]> {
  const data = await readJson<{ interactions?: Partial<PendingInteraction>[] }>(
    await chatFetch(withWindow('/chat/interactions', at), { credentials: 'include' }),
  )
  return (data.interactions ?? []).map((item) => ({
    requestId: item.requestId ?? '',
    type: item.type ?? '',
    header: item.header ?? '',
    prompt: item.prompt ?? '',
    payload: item.payload ?? '',
    toolMode: item.toolMode ?? '',
    multiSelect: item.multiSelect === true,
    options: item.options ?? [],
    sessionId: item.sessionId ?? '',
    remainingMs: typeof item.remainingMs === 'number' ? item.remainingMs : 0,
  }))
}

/**
 * Stops the run of the given chat.
 *
 * The chat is named rather than left to the server, so the stop reaches the run wherever it is:
 * the run's state hangs on the window that started it, and a second tab or a reloaded page is a
 * different window (issue #465). A stop without a chat still works and stops whatever this window
 * is running.
 */
export async function cancelRun(at: ChatAddress, sessionId?: string | null): Promise<void> {
  await chatFetch(withWindow('/chat/cancel', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ sessionId: sessionId ?? '' }),
  })
}

export async function respond(at: ChatAddress, body: Record<string, unknown>): Promise<void> {
  const res = await chatFetch(withWindow('/chat/respond', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(`respond failed (${res.status})`)
}

export function openEventStream(at: ChatAddress): EventSource {
  return new EventSource(withWindow('/chat/events', at), { withCredentials: true })
}

export async function improvePrompt(
  at: ChatAddress,
  text: string,
  requestId: string,
  signal?: AbortSignal,
): Promise<ImprovePromptResponse> {
  return readJson<ImprovePromptResponse>(
    await chatFetch(withWindow('/chat/improve-prompt', at), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ draft: text, requestId }),
      signal,
    }),
  )
}

/**
 * Turns "auto-approve" on or off. The mode is a setting of the chat: with a `sessionId` the server
 * stores it with that chat at once and tells the account's other windows showing it (#658). A
 * conversation that has no chat yet sends none, and its first send stores the mode with the chat.
 */
export async function setApprovalMode(
  at: ChatAddress,
  mode: 'AUTO_APPROVE' | 'MANUAL',
  sessionId: string | null,
): Promise<void> {
  const res = await chatFetch(withWindow('/chat/approval-mode', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(sessionId ? { mode, sessionId } : { mode }),
  })
  if (!res.ok) throw new Error(`approval-mode failed (${res.status})`)
}

export async function cancelPromptImprovement(at: ChatAddress, requestId: string): Promise<void> {
  await chatFetch(withWindow('/chat/improve-prompt/cancel', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ requestId }),
  })
}

/** The subagents of this window's chat, and the budget they spend from when the run has one. */
export async function listSubagents(at: ChatAddress): Promise<{ subagents: SubagentActivity[]; budget?: SubagentBudget }> {
  const data = await readJson<{ subagents?: SubagentActivity[]; budget?: SubagentBudget }>(
    await chatFetch(withWindow('/chat/subagents', at), { credentials: 'include' }),
  )
  // The job ends DONE whatever stopped its run; a stop at a limit reads as such (#786).
  const subagents = (data.subagents ?? []).map((item) =>
    item.status === 'DONE' && isLimitStop(item.stopReason) ? { ...item, status: 'LIMIT' as const } : item,
  )
  return { subagents, budget: data.budget }
}

/**
 * Stops one background subagent of the given chat, whichever window of the account started it.
 *
 * @param parentToolCallId The lead's call that started it, which tells it from a namesake.
 */
export async function cancelSubagent(
  at: ChatAddress,
  id: string,
  sessionId: string | null,
  parentToolCallId?: string,
): Promise<void> {
  const query = `?sessionId=${encodeURIComponent(sessionId ?? '')}&parentToolCallId=${encodeURIComponent(parentToolCallId ?? '')}`
  const res = await chatFetch(withWindow(`/chat/subagents/${encodeURIComponent(id)}${query}`, at), {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!res.ok) throw new Error(`cancel subagent failed (${res.status})`)
}

/**
 * What the server answered a message the user wrote to a subagent: `sent` - it reads it at the start
 * of its next turn; `ended` - it has ended and reads no more; `unknown` - no such subagent in this
 * account's chat; `empty` - there was no text.
 */
export type SubagentMessageStatus = 'sent' | 'ended' | 'unknown' | 'empty'

/**
 * Writes to one running background subagent, from its card.
 *
 * @param sessionId The chat whose lead started it.
 * @param parentToolCallId The lead's call that started it, which tells it from a namesake.
 */
export async function sendSubagentMessage(
  at: ChatAddress,
  id: string,
  sessionId: string | null,
  parentToolCallId: string,
  text: string,
): Promise<{ status: SubagentMessageStatus; messageId: string }> {
  const res = await chatFetch(withWindow(`/chat/subagents/${encodeURIComponent(id)}/message`, at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ sessionId: sessionId ?? '', parentToolCallId, text }),
  })
  // The refusals carry their reason in the same shape as the success.
  if (!res.ok && res.status !== 400 && res.status !== 404 && res.status !== 409) {
    throw new Error(`message to subagent failed (${res.status})`)
  }
  const data = (await res.json()) as { status?: string; messageId?: string }
  const known: SubagentMessageStatus[] = ['sent', 'ended', 'unknown', 'empty']
  const status = known.find((item) => item === data.status)
  if (!status) throw new Error(`message to subagent failed (${res.status})`)
  return { status, messageId: data.messageId ?? '' }
}

/**
 * The transcript of one subagent: its live log while it runs, its recorded run afterwards.
 *
 * @param runId The run its recorded part names, for a subagent the server holds no job for any
 *   more - an earlier server life, a chat this window no longer holds.
 */
export async function loadSubagentTranscript(at: ChatAddress, id: string, runId?: string): Promise<ChatMessage[]> {
  const query = runId ? `?runId=${encodeURIComponent(runId)}` : ''
  const detail = await readJson<SessionDetail>(
    await chatFetch(withWindow(`/chat/subagents/${encodeURIComponent(id)}/transcript${query}`, at), {
      credentials: 'include',
    }),
  )
  return turnsJsonToMessages(detail.turnsJson)
}

export function fileToAttachment(file: File): Promise<Attachment> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onerror = () => reject(reader.error)
    reader.onload = () => {
      const result = String(reader.result ?? '')
      const comma = result.indexOf(',')
      const base64Data = comma >= 0 ? result.slice(comma + 1) : result
      resolve({
        fileName: file.name,
        mimeType: file.type || 'application/octet-stream',
        base64Data,
        size: file.size,
      })
    }
    reader.readAsDataURL(file)
  })
}

interface PersistedPart {
  id: string
  type: string
  timestamp: number
  textData?: { text: string; citations?: import('./types').Citation[] }
  thinkingData?: { text: string }
  toolCallData?: {
    toolName: string
    argsJson: string
    status: string
    result?: string
    errorMessage?: string
    denyReason?: string
    durationMs?: number
  }
  approvalData?: {
    toolCallId: string
    toolName: string
    argsJson: string
    displayReason: string
    /** The `ToolMode` the approval was asked for; empty for a turn recorded before #512. */
    mode?: string
    decision: string
  }
  interactionData?: {
    interactionType?: 'text' | 'choice'
    question: string
    header?: string
    options?: string[]
    multiSelect?: boolean
    userResponse?: string
  }
  errorData?: { kind?: string; errorMessage: string }
  transactionData?: { type?: string; revisionNumber?: number; reason?: string }
  systemData?: { eventType?: string; details?: string }
  delegationData?: {
    toAgent?: string
    inputSummary?: string
    resultSummary?: string
    childSessionId?: string
    durationMs?: number
    childParts?: PersistedPart[]
    turnsUsed?: number
    totalUsage?: { inputTokens?: number; outputTokens?: number }
    stopReason?: string
    parentToolCallId?: string
  }
  subagentData?: {
    subagentId?: string
    agentName?: string
    task?: string
    parentToolCallId?: string
    childRunId?: string
    status?: string
    summary?: string
  }
}

interface PersistedTurn {
  id: string
  turnNumber: number
  timestamp: number
  userParts: PersistedPart[]
  assistantParts: PersistedPart[]
}

function mapApprovalDecision(decision: string | undefined): ApprovalOutcome {
  switch (decision) {
    case 'APPROVED':
      return 'APPROVED'
    case 'APPROVED_FOR_SESSION':
      return 'APPROVED_FOR_SESSION'
    case 'DENIED':
      return 'REJECTED'
    case 'TIMED_OUT':
      return 'TIMED_OUT'
    case 'INTERRUPTED':
      return 'INTERRUPTED'
    default:
      return 'pending'
  }
}

function mapToolStatus(status: string | undefined): { status: ToolCallStatus; isError: boolean } {
  switch (status) {
    case 'RUNNING':
      return { status: 'running', isError: false }
    case 'FAILED':
    case 'DENIED':
      return { status: 'error', isError: true }
    default:
      return { status: 'done', isError: false }
  }
}

/**
 * Whether a run with the given stop reason stopped in order at one of its limits (`StopReason.isLimit`
 * on the server): it has an answer, but maybe not the whole one (#786).
 */
export function isLimitStop(reason: string | undefined): boolean {
  return reason === 'MAX_TURNS' || reason === 'MAX_TOOL_CALLS' || reason === 'BUDGET_EXHAUSTED' || reason === 'OUTPUT_LIMIT'
}

/** How a synchronous delegation ended, from its run's stop reason. */
export function delegationEnd(reason: string | undefined): SubagentStatus {
  if (isLimitStop(reason)) return 'LIMIT'
  switch (reason) {
    case 'COMPLETE':
      return 'DONE'
    case 'ABORTED':
      return 'CANCELLED'
    default:
      return 'FAILED'
  }
}

/** The recorded state of a synchronous delegation. */
function delegationStatus(d: NonNullable<PersistedPart['delegationData']>): SubagentStatus {
  if (d.stopReason) return delegationEnd(d.stopReason)
  // Ended by an exception, which records no stop reason - only the error as its summary.
  if (d.resultSummary) return d.resultSummary.startsWith('ERROR') ? 'FAILED' : 'DONE'
  return 'RUNNING'
}

/** The steps of a synchronous delegation, from the child parts its recorded part holds. */
function delegationSteps(parts: PersistedPart[] | undefined): SubagentStep[] {
  const steps: SubagentStep[] = []
  for (const part of parts ?? []) {
    const d = part.toolCallData
    if (part.type !== 'tool_call' || !d) continue
    steps.push({
      toolUseId: part.id,
      name: d.toolName,
      summary: argsSummary(d.argsJson),
      status: mapToolStatus(d.status).status,
    })
  }
  return steps.slice(Math.max(0, steps.length - 20))
}

function subagentStatus(status: string | undefined): SubagentStatus {
  switch (status) {
    case 'QUEUED':
    case 'RUNNING':
    case 'DONE':
    case 'LIMIT':
    case 'FAILED':
    case 'CANCELLED':
      return status
    default:
      return 'RUNNING'
  }
}

/**
 * The subagents a wake names, from the server's frame or the recorded part, keeping only well-formed
 * entries: the text is the server's, but a log of another version must not break the transcript.
 */
export function wokenList(value: unknown): WokenSubagent[] {
  if (!Array.isArray(value)) return []
  const reasons = ['FINISHED', 'LIMIT', 'FAILED', 'CANCELLED', 'ASKS']
  return value
    .filter((item): item is Record<string, unknown> => typeof item === 'object' && item !== null)
    .filter((item) => typeof item.subagentId === 'string' && item.subagentId !== '')
    .map((item) => ({
      subagentId: String(item.subagentId),
      agentName: typeof item.agentName === 'string' ? item.agentName : '',
      reason: (reasons.includes(String(item.reason)) ? item.reason : 'FINISHED') as WokenSubagent['reason'],
      parentToolCallId: typeof item.parentToolCallId === 'string' ? item.parentToolCallId : '',
      question: typeof item.question === 'string' ? item.question : '',
    }))
}

function parseJsonArray(text: string | undefined): unknown {
  if (!text) return []
  try {
    return JSON.parse(text)
  } catch {
    return []
  }
}

/** Whether a recorded turn was started for subagents' news: its first part is their delivery. */
function opensWithSubagentNews(turn: PersistedTurn): boolean {
  const first = (turn.assistantParts ?? [])[0]
  return first?.type === 'system' && first.systemData?.eventType === 'SUBAGENT_RESULT'
}

export function turnsJsonToMessages(turnsJson: string | undefined): ChatMessage[] {
  if (!turnsJson) return []
  let turns: PersistedTurn[]
  try {
    turns = JSON.parse(turnsJson) as PersistedTurn[]
  } catch {
    return []
  }
  const msgs: ChatMessage[] = []
  for (const turn of turns ?? []) {
    const userText = (turn.userParts ?? [])
      .filter((p) => p.type === 'text' && p.textData)
      .map((p) => p.textData!.text)
      .join('\n')
    if (userText) {
      msgs.push({ type: 'user', id: `${turn.id}-u`, text: userText, timestamp: turn.timestamp })
    } else if (opensWithSubagentNews(turn)) {
      // Started by subagents reporting back, not by the user: drawn as the live wake line is.
      const recorded = (turn.assistantParts ?? []).find(
        (part) => part.type === 'system' && part.systemData?.eventType === 'LEAD_WOKEN',
      )
      const woken = wokenList(parseJsonArray(recorded?.systemData?.details))
      msgs.push({
        type: 'notice',
        id: `${turn.id}-w`,
        kind: 'lead_woken',
        detail: '',
        timestamp: turn.timestamp,
        ...(woken.length ? { woken } : {}),
      })
    }
    // "Worked for" on a restored turn is measured the same way as on a live one: from when the turn
    // started to its last recorded part. Turns whose parts carry no later stamp show no duration
    // rather than a fabricated one.
    const lastPartAt = (turn.assistantParts ?? []).reduce(
      (latest, part) => Math.max(latest, part.timestamp ?? 0),
      0,
    )
    if (lastPartAt > turn.timestamp) {
      msgs.push({
        type: 'run_meta',
        id: `${turn.id}-run`,
        runId: turn.id,
        startedAt: turn.timestamp,
        durationMs: lastPartAt - turn.timestamp,
        timestamp: turn.timestamp,
      })
    }
    for (const part of turn.assistantParts ?? []) {
      switch (part.type) {
        case 'text':
          if (part.textData?.text) {
            msgs.push({
              type: 'assistant_text',
              id: part.id,
              text: part.textData.text,
              isStreaming: false,
              timestamp: part.timestamp,
              citations: part.textData.citations,
            })
          }
          break
        case 'thinking':
          if (part.thinkingData?.text) {
            msgs.push({
              type: 'thinking',
              id: part.id,
              content: part.thinkingData.text,
              timestamp: part.timestamp,
            })
          }
          break
        case 'tool_call': {
          const d = part.toolCallData
          if (!d) break
          let input: unknown = d.argsJson
          try {
            input = JSON.parse(d.argsJson)
          } catch {
            /* keep string */
          }
          const { status, isError } = mapToolStatus(d.status)
          msgs.push({
            type: 'tool_call',
            id: `${part.id}-call`,
            toolUseId: part.id,
            name: d.toolName,
            input,
            status,
            durationMs: d.durationMs,
            depth: 0,
            timestamp: part.timestamp,
          })
          if (status !== 'running') {
            msgs.push({
              type: 'tool_result',
              id: `${part.id}-result`,
              toolUseId: part.id,
              // `||`, not `??`: the server writes an absent field as "" (never null), so a failed
              // call has `result: ""` and its error behind it would never be reached.
              output: d.result || d.errorMessage || d.denyReason || '',
              isError,
              timestamp: part.timestamp,
            })
          }
          break
        }
        case 'approval': {
          const d = part.approvalData
          if (!d) break
          msgs.push({
            type: 'approval_request',
            id: part.id,
            approvalRequestId: part.id,
            toolCallId: d.toolCallId,
            toolName: d.toolName,
            argsJson: d.argsJson,
            displayReason: d.displayReason,
            // What lets the recorded entry be phrased in the reader's language, the way the live
            // card is: the reason the security context wrote is English for everyone (#512).
            toolMode: d.mode ? d.mode : undefined,
            outcome: mapApprovalDecision(d.decision),
            timestamp: part.timestamp,
          })
          break
        }
        case 'interaction': {
          const d = part.interactionData
          if (!d) break
          msgs.push({
            type: 'interaction_request',
            id: part.id,
            requestId: part.id,
            question: d.question,
            header: d.header,
            answered: !!d.userResponse,
            timestamp: part.timestamp,
            interactionType: d.interactionType,
            options: d.options?.map((label): InteractionOption => ({ label })),
            multiSelect: d.multiSelect,
          })
          break
        }
        case 'error':
          if (part.errorData) {
            msgs.push({
              type: 'error',
              id: part.id,
              code: part.errorData.kind,
              message: part.errorData.errorMessage,
              timestamp: part.timestamp,
            })
          }
          break
        case 'transaction':
          // Only the rollback. `BEGAN` and `COMMITTED` are the normal course of a turn that wrote
          // something, and the chat has never drawn them; a rollback is the one that changes what
          // the reader may believe about the answer above it, so the restored turn says it too -
          // the live notice would otherwise be gone the moment the page is reloaded (#512).
          if (part.transactionData?.type === 'ROLLED_BACK') {
            msgs.push({
              type: 'notice',
              id: part.id,
              kind: 'transaction_rolled_back',
              detail: part.transactionData.reason ?? '',
              timestamp: part.timestamp,
            })
          }
          break
        case 'subagent': {
          // A background subagent the turn started: its card is drawn from this after a reload. Its
          // steps are not here - they are in its own transcript, found by its run id.
          const d = part.subagentData
          if (!d?.subagentId) break
          msgs.push({
            type: 'subagent_ref',
            id: part.id,
            subagentId: d.subagentId,
            agentName: d.agentName ?? '',
            task: d.task ?? '',
            parentToolCallId: d.parentToolCallId ?? '',
            childRunId: d.childRunId ?? '',
            status: subagentStatus(d.status),
            summary: d.summary ?? '',
            timestamp: part.timestamp,
          })
          break
        }
        case 'delegation': {
          // A synchronous delegation the turn made: its card is drawn from this after a reload, with
          // the steps the part recorded - the lead waited for it, so they are all here.
          const d = part.delegationData
          if (!d) break
          msgs.push({
            type: 'delegation_ref',
            id: part.id,
            agentName: d.toAgent ?? '',
            task: d.inputSummary ?? '',
            parentToolCallId: d.parentToolCallId ?? '',
            childRunId: d.childSessionId ?? '',
            status: delegationStatus(d),
            summary: d.resultSummary ?? '',
            steps: delegationSteps(d.childParts),
            durationMs: d.durationMs ?? 0,
            totalTokens: (d.totalUsage?.inputTokens ?? 0) + (d.totalUsage?.outputTokens ?? 0),
            turns: d.turnsUsed && d.turnsUsed > 0 ? d.turnsUsed : 0,
            timestamp: part.timestamp,
          })
          break
        }
        case 'system':
          // Only the system events the thread draws. The others - an approval mode change, the text
          // a finished subagent was handed - are not the user's business, and an unknown one is
          // silently skipped rather than rendered as a nameless line.
          if (part.systemData?.eventType === 'MODEL_CHANGED' && part.systemData.details) {
            msgs.push({
              type: 'notice',
              id: part.id,
              kind: 'model_switched',
              detail: part.systemData.details,
              timestamp: part.timestamp,
            })
          } else if (part.systemData?.eventType === 'CANCELLED') {
            // The server's details are an English line for the operator; the reader gets the
            // renderer's sentence in their language, exactly as the live cancel draws it (#549).
            msgs.push({ type: 'notice', id: part.id, kind: 'cancelled', detail: '', timestamp: part.timestamp })
          } else if (part.systemData?.eventType === 'USER_MESSAGE' && part.systemData.details) {
            // In a subagent's own transcript: what the user wrote to it from its card. The details are
            // what its model was shown - an opening line, then the messages - and the user wrote the
            // messages only.
            msgs.push({ type: 'user', id: part.id, text: userMessageText(part.systemData.details), timestamp: part.timestamp })
          } else if (part.systemData?.eventType === 'INTERRUPTED') {
            // Written by the server's startup sweep onto a run the restart cut off (#598). There is
            // no live counterpart: the window that watched the run lost its stream with the server.
            msgs.push({ type: 'notice', id: part.id, kind: 'interrupted', detail: '', timestamp: part.timestamp })
          }
          break
      }
    }
  }
  return msgs
}

/** The user's own words of a `USER_MESSAGE` part: everything after the opening line. */
export function userMessageText(details: string): string {
  const start = details.indexOf('\n\n')
  return start < 0 ? details : details.slice(start + 2)
}

/**
 * The title a new chat gets from its first message.
 *
 * Long enough to be recognisable in the thread header, which shows it in full: the rail truncates
 * with an ellipsis of its own, so a title cut short here was cut short everywhere. A chat is only
 * created once something has been said in it, so there is always a message to name it after.
 */
export function autoTitle(text: string): string {
  // What the user read, not what travelled: a reference token names its source in the title rather
  // than showing the `<quote/>` it serialises to.
  const plain = draftToText(parseDraft(text).map((part) =>
    part.type === 'text' ? part : { type: 'text' as const, text: referenceSummary(part.token) },
  ))
  const trimmed = plain.trim().replace(/\s+/g, ' ')
  return trimmed.length > 72 ? `${trimmed.slice(0, 72)}…` : trimmed
}
