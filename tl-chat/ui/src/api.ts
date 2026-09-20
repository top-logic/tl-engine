import { draftToText, parseDraft, referenceSummary } from './reference'
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
  TokenUsage,
  ToolCallStatus,
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
 * server. A bare window id is the window's unnamed conversation, which is what the classic
 * `chat-client.js` addresses and what a host calling these functions with a window id alone still
 * gets.
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
  }>(await fetch(withWindow('/chat/capabilities', at), { credentials: 'include' }))
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
    await fetch(withWindow('/chat/sessions', at), { credentials: 'include' }),
  )
  return data.sessions ?? []
}

export async function createSession(at: ChatAddress, title: string): Promise<SessionSummary> {
  return readJson<SessionSummary>(
    await fetch(withWindow('/chat/sessions', at), {
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
  /** What the last request's prompt was made of, so the popover keeps its breakdown after a reload. */
  lastComposition?: ContextComposition
}

export async function loadSession(at: ChatAddress, id: string): Promise<SessionDetail> {
  return readJson<SessionDetail>(
    await fetch(withWindow(`/chat/sessions/${encodeURIComponent(id)}`, at), {
      credentials: 'include',
    }),
  )
}

export async function deleteSession(at: ChatAddress, id: string): Promise<void> {
  const res = await fetch(withWindow(`/chat/sessions/${encodeURIComponent(id)}`, at), {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!res.ok) throw new Error(`delete failed (${res.status})`)
}

export async function sendMessage(at: ChatAddress, payload: SendPayload): Promise<{ taskId?: string; status: string }> {
  const res = await fetch(withWindow('/chat/send', at), {
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
    await fetch(withWindow('/chat/regenerate', at), {
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
    await fetch(withWindow('/chat/truncate', at), {
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
  const res = await fetch(withWindow('/chat/feedback', at), {
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
    await fetch(withWindow('/chat/updates', at), { credentials: 'include' }),
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
  sessionId: string
  /** How long the requester is still waiting; zero or less means nobody is listening any more. */
  remainingMs: number
}

/**
 * Every approval and question waiting for this user, across their chats.
 *
 * Approvals have no server poll of their own, so this is what lets a reloaded page put the card
 * back in front of the user instead of a composer that is locked for no visible reason.
 */
export async function fetchPendingInteractions(at: ChatAddress): Promise<PendingInteraction[]> {
  const data = await readJson<{ interactions?: Partial<PendingInteraction>[] }>(
    await fetch(withWindow('/chat/interactions', at), { credentials: 'include' }),
  )
  return (data.interactions ?? []).map((item) => ({
    requestId: item.requestId ?? '',
    type: item.type ?? '',
    header: item.header ?? '',
    prompt: item.prompt ?? '',
    payload: item.payload ?? '',
    toolMode: item.toolMode ?? '',
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
  await fetch(withWindow('/chat/cancel', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ sessionId: sessionId ?? '' }),
  })
}

export async function respond(at: ChatAddress, body: Record<string, unknown>): Promise<void> {
  const res = await fetch(withWindow('/chat/respond', at), {
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
    await fetch(withWindow('/chat/improve-prompt', at), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ draft: text, requestId }),
      signal,
    }),
  )
}

export async function setApprovalMode(
  at: ChatAddress,
  mode: 'AUTO_APPROVE' | 'MANUAL',
): Promise<void> {
  const res = await fetch(withWindow('/chat/approval-mode', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ mode }),
  })
  if (!res.ok) throw new Error(`approval-mode failed (${res.status})`)
}

export async function cancelPromptImprovement(at: ChatAddress, requestId: string): Promise<void> {
  await fetch(withWindow('/chat/improve-prompt/cancel', at), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ requestId }),
  })
}

export async function listSubagents(at: ChatAddress): Promise<SubagentActivity[]> {
  const data = await readJson<{ subagents?: SubagentActivity[] }>(
    await fetch(withWindow('/chat/subagents', at), { credentials: 'include' }),
  )
  return data.subagents ?? []
}

export async function cancelSubagent(at: ChatAddress, id: string): Promise<void> {
  const res = await fetch(withWindow(`/chat/subagents/${encodeURIComponent(id)}`, at), {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!res.ok) throw new Error(`cancel subagent failed (${res.status})`)
}

export async function loadSubagentTranscript(at: ChatAddress, id: string): Promise<ChatMessage[]> {
  const detail = await readJson<SessionDetail>(
    await fetch(withWindow(`/chat/subagents/${encodeURIComponent(id)}/transcript`, at), {
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
  systemData?: { eventType?: string; details?: string }
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
              output: d.result ?? d.errorMessage ?? d.denyReason ?? '',
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
        case 'system':
          // Only the one system event the thread draws. The others - a cancellation, an approval
          // mode change, the text a finished subagent was handed - are either shown by something
          // else or are not the user's business, and an unknown one is silently skipped rather
          // than rendered as a nameless line.
          if (part.systemData?.eventType === 'MODEL_CHANGED' && part.systemData.details) {
            msgs.push({
              type: 'notice',
              id: part.id,
              kind: 'model_switched',
              detail: part.systemData.details,
              timestamp: part.timestamp,
            })
          }
          break
      }
    }
  }
  return msgs
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
