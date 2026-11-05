import type { CommandResponse } from 'tl-react-bridge'
import type { ChatCommand } from './chat-stream'
import type { ConversationFeatures } from './features'
import { argsSummary } from './plain-text'
import { draftToText, parseDraft, referenceSummary } from './reference'
import { keepLast, MAX_CHILD_STEPS, subagentStatus } from './types'
import type {
  ApprovalOutcome,
  Capabilities,
  ChatMessage,
  ContextComposition,
  DraftAttachment,
  InteractionOption,
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

/**
 * The agents, models and MCP servers the account may choose from, as the `<chat-conversation>`
 * control states them (`ChatConversationControl#STATE_CAPABILITIES`); `undefined` when the state
 * carries none, which every other chat element's does.
 */
export function readCapabilities(data: unknown): Capabilities | undefined {
  if (data == null || typeof data !== 'object') return undefined
  const raw = data as {
    entryPointAgent?: string
    agents?: Capabilities['agents']
    models?: Capabilities['models']
    mcpServers?: Capabilities['mcpServers']
    promptImproverAvailable?: boolean
  }
  return {
    entryPointAgent: raw.entryPointAgent,
    agents: raw.agents ?? [],
    models: raw.models ?? [],
    mcpServers: raw.mcpServers ?? [],
    promptImproverAvailable: raw.promptImproverAvailable !== false,
  }
}

/**
 * The account's chats, as the `<chat-conversation>` control states them (`SessionListWatch#SESSIONS`)
 * with every render and whenever they change; `undefined` when the state carries none.
 */
export function readSessions(data: unknown): SessionSummary[] | undefined {
  if (!Array.isArray(data)) return undefined
  return data
    .filter((item): item is Partial<SessionSummary> => item != null && typeof item === 'object')
    .map((item) => ({
      id: item.id ?? '',
      title: item.title ?? '',
      ...(item.lastUpdated ? { lastUpdated: item.lastUpdated } : {}),
    }))
}

/** Command that opens a chat in the window's conversation. Must match `ChatControl#CMD_SELECT_SESSION`. */
export const CMD_SELECT_SESSION = 'selectSession'

/** Command that deletes a chat of the account. Must match `ChatControl#CMD_DELETE_SESSION`. */
export const CMD_DELETE_SESSION = 'deleteSession'

/** Argument naming the chat. Must match `ChatControl#ARG_SESSION_ID`. */
export const ARG_SESSION_ID = 'sessionId'

/**
 * Field of the open's answer: whether the chat was opened. Must match `SessionOpenReply.opened`
 * (chat `messages.proto`).
 */
export const FIELD_OPENED = 'opened'

/**
 * Field of the open's answer: the chat's run state. Must match `SessionOpenReply.runState` (chat
 * `messages.proto`).
 */
export const FIELD_RUN_STATE = 'runState'

/**
 * Field of the delete's answer: whether the chat was deleted. Must match
 * `SessionDeleteReply.deleted` (chat `messages.proto`).
 */
export const FIELD_DELETED = 'deleted'

/** First part of the data key of a chat's transcript. Must match `ChatControl#DATA_SESSION`. */
export const DATA_SESSION = 'session'

/** First part of the data key of a subagent's transcript. Must match `ChatControl#DATA_SUBAGENT`. */
export const DATA_SUBAGENT = 'subagent'

/** Separator of the parts of a data key. Must match `ChatControl#DATA_KEY_SEPARATOR`. */
export const DATA_KEY_SEPARATOR = '/'

/** The data key of the given kind for the given ids; every id is URL-encoded. */
export function dataKey(kind: string, ...ids: string[]): string {
  return [kind, ...ids.map((id) => encodeURIComponent(id))].join(DATA_KEY_SEPARATOR)
}

/**
 * Opens a chat in the window's conversation: from now on its runs, approval mode, ratings and effort
 * are the conversation's.
 *
 * @returns the chat's run state, applied once its transcript is on screen.
 * @throws when the chat was not opened - it is not this account's, or does not exist.
 */
export async function openSession(command: ChatCommand, id: string): Promise<RunState | undefined> {
  const answer = await command(CMD_SELECT_SESSION, { [ARG_SESSION_ID]: id })
  if (answer?.[FIELD_OPENED] !== true) throw new Error(`chat ${id} was not opened`)
  const run = answer[FIELD_RUN_STATE]
  return run == null ? undefined : readRunState(run)
}

/** Deletes a chat of the account, with everything recorded in it. */
export async function deleteSession(command: ChatCommand, id: string): Promise<void> {
  const answer = await command(CMD_DELETE_SESSION, { [ARG_SESSION_ID]: id })
  if (answer?.[FIELD_DELETED] !== true) throw new Error(`chat ${id} was not deleted`)
}

/**
 * Reads one data item of a chat control (`ChatControl#getDownloadData`) from the bridge's data URL
 * of the element (`useTLDataUrl`).
 */
async function readData<T>(dataUrl: string, key: string): Promise<T> {
  if (!dataUrl) throw new Error('no chat element to read from')
  const res = await fetch(`${dataUrl}&key=${encodeURIComponent(key)}`, { credentials: 'include' })
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)
  return (await res.json()) as T
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
  /** The recorded runs, by the last turn each of them wrote. */
  runs?: RecordedRun[]
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
  /** Which functions beyond sending this conversation supports; read with `readFeatures`. */
  features?: Partial<ConversationFeatures>
}

/** Reads an opened chat as the window shows it ({@link openSession} first). */
export async function loadSession(dataUrl: string, id: string): Promise<SessionDetail> {
  return readData<SessionDetail>(dataUrl, dataKey(DATA_SESSION, id))
}

/**
 * Command that sends a message; one without a chat creates it. Must match `ChatControl#CMD_SEND`.
 */
export const CMD_SEND = 'send'

/**
 * Command that drops a turn and sends the edited message in its place. Must match
 * `ChatControl#CMD_EDIT_SEND`.
 */
export const CMD_EDIT_SEND = 'editSend'

/** Command that answers the last message again. Must match `ChatControl#CMD_REGENERATE`. */
export const CMD_REGENERATE = 'regenerate'

/** Command that takes a file off the message. Must match `ChatControl#CMD_REMOVE_ATTACHMENT`. */
export const CMD_REMOVE_ATTACHMENT = 'removeAttachment'

/** Argument: the message. Must match `ChatControl#ARG_TEXT`. */
export const ARG_TEXT = 'text'

/** Argument: the title of a chat the send creates. Must match `ChatControl#ARG_TITLE`. */
export const ARG_TITLE = 'title'

/** Argument: the agent of the turn. Must match `ChatControl#ARG_AGENT`. */
export const ARG_AGENT = 'agent'

/** Argument: the model of the turn. Must match `ChatControl#ARG_MODEL`. */
export const ARG_MODEL = 'model'

/** Argument: whether the MCP servers replace the agent's own. Must match `ChatControl#ARG_MCP_OVERRIDE`. */
export const ARG_MCP_OVERRIDE = 'mcpOverride'

/** Argument: the MCP servers of the turn. Must match `ChatControl#ARG_MCP_SERVERS`. */
export const ARG_MCP_SERVERS = 'mcpServers'

/** Argument: how hard the model may think. Must match `ChatControl#ARG_EFFORT`. */
export const ARG_EFFORT = 'effort'

/** Argument: the auto-approve switch at the send. Must match `ChatControl#ARG_APPROVAL_MODE`. */
export const ARG_APPROVAL_MODE = 'approvalMode'

/** Argument: the first turn an edit drops. Must match `ChatControl#ARG_FROM_TURN`. */
export const ARG_FROM_TURN = 'fromTurn'

/** Argument: the id of an attached file. Must match `ChatControl#ARG_ATTACHMENT_ID`. */
export const ARG_ATTACHMENT_ID = 'attachmentId'

/**
 * Field of the answer: what became of the request. Must match `ChatSendReply.status` (chat
 * `messages.proto`).
 */
export const FIELD_STATUS = 'status'

/** Field of the answer: why it was refused. Must match `ChatSendReply.reason` (chat `messages.proto`). */
export const FIELD_REASON = 'reason'

/**
 * Field of the answer: the chat the message went to. Must match `ChatSendReply.sessionId` (chat
 * `messages.proto`).
 */
export const FIELD_SESSION_ID = 'sessionId'

/**
 * Field of the answer: what that chat supports. Must match `ChatSendReply.features` (chat
 * `messages.proto`).
 */
export const FIELD_FEATURES = 'features'

/**
 * Field of the answer: how many turns went. Must match `ChatSendReply.removedTurns` (chat
 * `messages.proto`).
 */
export const FIELD_REMOVED_TURNS = 'removedTurns'

/** Field of the answer: the message sent again. Must match `ChatSendReply.text` (chat `messages.proto`). */
export const FIELD_TEXT = 'text'

/** Status: the message went. Must match `ChatControl#STATUS_PROCESSING`. */
export const STATUS_PROCESSING = 'processing'

/** Status: the chat is answering something else. Must match `ChatControl#STATUS_BUSY`. */
export const STATUS_BUSY = 'busy'

/** Status: the account may not make the request. Must match `ChatControl#STATUS_FORBIDDEN`. */
export const STATUS_FORBIDDEN = 'forbidden'

/** Status: no room for the run right now. Must match `ChatControl#STATUS_REJECTED`. */
export const STATUS_REJECTED = 'rejected'

/** State key of the files attached to the message. Must match `ChatControl#STATE_DRAFT_ATTACHMENTS`. */
export const STATE_DRAFT_ATTACHMENTS = 'draftAttachments'

/** Name of the multipart part of an uploaded file. Must match `UploadSupport#FILE_PART`. */
const UPLOAD_FILE_PART = 'file'

/** A send the chat refused because it is answering something else. */
export class BusyError extends Error {
  constructor() {
    super(STATUS_BUSY)
    this.name = 'BusyError'
  }
}

/** The settings of a turn, as the composer shows them at the send. */
export interface TurnSettings {
  agent?: string
  model?: string
  mcpOverride: boolean
  mcpServers: string[]
  /** How hard the model may think for this turn; omitted keeps the agent's assignment. */
  effort?: string
}

/** What a send names besides its settings. */
export interface SendRequest extends TurnSettings {
  text: string
  /** The chat to send into; empty creates one. */
  sessionId: string
  /** The title of the chat the send creates. */
  title?: string
  /** The position of the auto-approve switch at the send; the turn runs with it. */
  approvalMode?: 'AUTO_APPROVE' | 'MANUAL'
}

/** What a message that went answers. */
export interface TurnAnswer {
  /** The chat the message went to; for a send that created it, the new chat. */
  sessionId: string
  /** Which functions beyond sending that chat supports. */
  features?: Partial<ConversationFeatures>
  /** How many turns an edit or a regenerate dropped. */
  removedTurns: number
  /** The message a regenerate sent again. */
  text: string
}

function settingsArgs(settings: TurnSettings): Record<string, unknown> {
  return {
    [ARG_AGENT]: settings.agent,
    [ARG_MODEL]: settings.model,
    [ARG_MCP_OVERRIDE]: settings.mcpOverride,
    [ARG_MCP_SERVERS]: settings.mcpServers,
    [ARG_EFFORT]: settings.effort,
  }
}

/**
 * Sends a turn command and reads its answer.
 *
 * @throws ForbiddenError for a request the account may not make, with the server's sentence;
 *         RefusedError when there is no room for the run right now; BusyError while the chat answers
 *         something else; an Error for anything else that did not send the message.
 */
async function turn(command: ChatCommand, name: string, args: Record<string, unknown>): Promise<TurnAnswer> {
  const answer = await command(name, args)
  const status = answer?.[FIELD_STATUS]
  const reason = typeof answer?.[FIELD_REASON] === 'string' ? (answer[FIELD_REASON] as string) : ''
  if (status === STATUS_FORBIDDEN) throw new ForbiddenError(reason)
  if (status === STATUS_REJECTED) throw new RefusedError(reason)
  if (status === STATUS_BUSY) throw new BusyError()
  if (status !== STATUS_PROCESSING) throw new Error(typeof status === 'string' ? status : `${name} failed`)
  const sessionId = answer?.[FIELD_SESSION_ID]
  const removed = answer?.[FIELD_REMOVED_TURNS]
  const text = answer?.[FIELD_TEXT]
  return {
    sessionId: typeof sessionId === 'string' ? sessionId : '',
    features: (answer?.[FIELD_FEATURES] ?? undefined) as Partial<ConversationFeatures> | undefined,
    removedTurns: typeof removed === 'number' ? removed : 0,
    text: typeof text === 'string' ? text : '',
  }
}

/**
 * Sends a message, with the files attached to it on the server; a request without a chat creates
 * one, titled `title`, and answers with its id.
 */
export function sendMessage(command: ChatCommand, request: SendRequest): Promise<TurnAnswer> {
  return turn(command, CMD_SEND, {
    [ARG_TEXT]: request.text,
    [ARG_SESSION_ID]: request.sessionId,
    [ARG_TITLE]: request.title,
    [ARG_APPROVAL_MODE]: request.approvalMode,
    ...settingsArgs(request),
  })
}

/**
 * Drops turn `fromTurn` and everything after it and sends the edited message in its place, in one
 * command: nothing is dropped from an edit that is refused.
 */
export function editSend(
  command: ChatCommand,
  fromTurn: number,
  request: SendRequest,
): Promise<TurnAnswer> {
  return turn(command, CMD_EDIT_SEND, {
    [ARG_TEXT]: request.text,
    [ARG_SESSION_ID]: request.sessionId,
    [ARG_FROM_TURN]: fromTurn,
    [ARG_APPROVAL_MODE]: request.approvalMode,
    ...settingsArgs(request),
  })
}

/**
 * Asks for the last answer again.
 *
 * The previous turn is removed server side before the same message is sent once more, so the new
 * answer takes the old one's place instead of following it.
 */
export function regenerate(command: ChatCommand, sessionId: string, settings: TurnSettings): Promise<TurnAnswer> {
  return turn(command, CMD_REGENERATE, { [ARG_SESSION_ID]: sessionId, ...settingsArgs(settings) })
}

/** Takes a file off the message being written. */
export async function removeAttachment(command: ChatCommand, id: string): Promise<void> {
  await command(CMD_REMOVE_ATTACHMENT, { [ARG_ATTACHMENT_ID]: id })
}

/** An upload to a chat control (`useTLUpload`). */
export type ChatUpload = (formData: FormData) => Promise<CommandResponse | undefined>

/**
 * Uploads files to be attached to the message being written; they then stand in the control state
 * ({@link readDraftAttachments}).
 *
 * @returns why files were not attached, one sentence each; empty when all of them were.
 * @throws when the upload did not reach the server.
 */
export async function uploadAttachments(upload: ChatUpload, files: File[]): Promise<string[]> {
  if (files.length === 0) return []
  const form = new FormData()
  for (const file of files) form.append(UPLOAD_FILE_PART, file, file.name)
  const answer = await upload(form)
  if (!answer) throw new Error('upload failed')
  const reasons = answer[FIELD_REASON]
  return Array.isArray(reasons) ? reasons.filter((reason): reason is string => typeof reason === 'string') : []
}

/**
 * The files attached to the message being written, as a chat control states them; `undefined` when
 * the state carries none.
 */
export function readDraftAttachments(data: unknown): DraftAttachment[] | undefined {
  if (!Array.isArray(data)) return undefined
  return data
    .filter((item): item is Record<string, unknown> => item != null && typeof item === 'object')
    .map((item) => ({
      id: String(item.id ?? ''),
      fileName: String(item.fileName ?? ''),
      mimeType: String(item.mimeType ?? ''),
      size: Number(item.size ?? 0),
    }))
    .filter((item) => item.id !== '')
}

/** One human-in-the-loop request waiting for this user, as the `run_state` frame lists it. */
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
 * The run of the open chat as the server sees it now: the `run_state` frame, which the server sends
 * when the stream is attached and again on `refreshRunState` (`RunStateFrame` on the server).
 *
 * The stream replays nothing, so this is what lets a reloaded page say that the agent is still
 * working, show the progress it has made, put a waiting approval or question back in front of the
 * user instead of a composer that is locked for no visible reason, and list the background
 * subagents with what they spend from.
 */
export interface RunState {
  /** The chat this state is about; empty while the window shows no saved chat. */
  sessionId: string
  /** Whether the chat's agent is still working, in this window or another one of the account. */
  processing: boolean
  /** The run's task list, in the format the `task_update` frame carries; empty when there is none. */
  tasksJson: string
  /** The approvals and questions of this chat waiting for this user. */
  interactions: PendingInteraction[]
  subagents: SubagentActivity[]
  /** The budget the subagents share; absent when no subagent's run is bounded. */
  budget?: SubagentBudget
}

/** The payload of a `run_state` frame, with a default for every field the server left out. */
export function readRunState(data: unknown): RunState {
  const raw = (data ?? {}) as {
    sessionId?: string
    processing?: boolean
    tasksJson?: string
    interactions?: Partial<PendingInteraction>[]
    subagents?: SubagentActivity[]
    budget?: SubagentBudget
  }
  return {
    sessionId: raw.sessionId ?? '',
    processing: raw.processing === true,
    tasksJson: raw.tasksJson ?? '',
    interactions: (raw.interactions ?? []).map((item) => ({
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
      askedBySubagentId: item.askedBySubagentId ?? '',
      askedByAgent: item.askedByAgent ?? '',
    })),
    // The job ends DONE whatever stopped its run; a stop at a limit reads as such (#786).
    subagents: (raw.subagents ?? []).map((item) =>
      item.status === 'DONE' ? { ...item, status: completedStatus(item.stopReason) } : item,
    ),
    budget: raw.budget ?? undefined,
  }
}

/** Command that stops the run of a chat. Must match `ChatControl#CMD_CANCEL`. */
export const CMD_CANCEL = 'cancel'

/** Command that answers an approval or a question. Must match `ChatControl#CMD_RESPOND`. */
export const CMD_RESPOND = 'respond'

/** Command that switches auto-approve. Must match `ChatControl#CMD_SET_APPROVAL_MODE`. */
export const CMD_SET_APPROVAL_MODE = 'setApprovalMode'

/** Command that rates an answer. Must match `ChatControl#CMD_RATE`. */
export const CMD_RATE = 'rate'

/** Command that starts improving a draft. Must match `ChatControl#CMD_IMPROVE_PROMPT`. */
export const CMD_IMPROVE_PROMPT = 'improvePrompt'

/** Command that cancels an improvement. Must match `ChatControl#CMD_CANCEL_PROMPT_IMPROVEMENT`. */
export const CMD_CANCEL_PROMPT_IMPROVEMENT = 'cancelPromptImprovement'

/** Command that writes to a subagent. Must match `ChatControl#CMD_MESSAGE_SUBAGENT`. */
export const CMD_MESSAGE_SUBAGENT = 'messageSubagent'

/** Command that stops a subagent. Must match `ChatControl#CMD_CANCEL_SUBAGENT`. */
export const CMD_CANCEL_SUBAGENT = 'cancelSubagent'

/** Argument: the id of an approval, a question or an improvement. Must match `ChatControl#ARG_REQUEST_ID`. */
export const ARG_REQUEST_ID = 'requestId'

/** Argument: the decision or the answer. Must match `ChatControl#ARG_RESPONSE`. */
export const ARG_RESPONSE = 'response'

/** Argument: the tool an approval is about. Must match `ChatControl#ARG_TOOL_NAME`. */
export const ARG_TOOL_NAME = 'toolName'

/** Argument: why a tool call was denied. Must match `ChatControl#ARG_COMMENT`. */
export const ARG_COMMENT = 'comment'

/** Argument: what the user typed for a choice question. Must match `ChatControl#ARG_OWN_WORDS`. */
export const ARG_OWN_WORDS = 'ownWords'

/** Argument: an approval mode. Must match `ChatControl#ARG_MODE`. */
export const ARG_MODE = 'mode'

/** Argument: the turn whose answer is rated. Must match `ChatControl#ARG_TURN_INDEX`. */
export const ARG_TURN_INDEX = 'turnIndex'

/** Argument: the rating. Must match `ChatControl#ARG_RATING`. */
export const ARG_RATING = 'rating'

/** Argument: the note of a rating. Must match `ChatControl#ARG_NOTE`. */
export const ARG_NOTE = 'note'

/** Argument: the draft to improve. Must match `ChatControl#ARG_DRAFT`. */
export const ARG_DRAFT = 'draft'

/** Argument: the subagent's id. Must match `ChatControl#ARG_SUBAGENT_ID`. */
export const ARG_SUBAGENT_ID = 'subagentId'

/** Argument: the lead's call that started a subagent. Must match `ChatControl#ARG_PARENT_TOOL_CALL_ID`. */
export const ARG_PARENT_TOOL_CALL_ID = 'parentToolCallId'

/**
 * Field of the answer: whether something was stopped. Must match `CancelReply.cancelled` (chat
 * `messages.proto`).
 */
export const FIELD_CANCELLED = 'cancelled'

/**
 * Field of the answer: the id of a message to a subagent. Must match
 * `SubagentMessageReply.messageId` (chat `messages.proto`).
 */
export const FIELD_MESSAGE_ID = 'messageId'

/** Status: done. Must match `ChatControl#STATUS_OK`. */
export const STATUS_OK = 'ok'

/** Status: no run of the account waits for the request. Must match `ChatControl#STATUS_IGNORED`. */
export const STATUS_IGNORED = 'ignored'

/** Status: the improvement runs. Must match `ChatControl#STATUS_STARTED`. */
export const STATUS_STARTED = 'started'

/**
 * Name of the frame with the result of an improvement. Must match `ChatSSEHandler#PROMPT_IMPROVED`.
 */
export const FRAME_PROMPT_IMPROVED = 'prompt_improved'

/** The position of the auto-approve switch. Must match the `ApprovalMode` names. */
export type ApprovalModeName = 'AUTO_APPROVE' | 'MANUAL'

/** The status of an answer, or `undefined` when the command did not reach the server. */
function statusOf(answer: CommandResponse | undefined): unknown {
  return answer?.[FIELD_STATUS]
}

/**
 * Stops the run of the given chat.
 *
 * The chat is named rather than left to the server, so the stop reaches the run wherever it is:
 * the run's state hangs on the window that started it, and a second tab or a reloaded page is a
 * different window (issue #465). A stop without a chat stops whatever this conversation is running.
 */
export async function cancelRun(command: ChatCommand, sessionId?: string | null): Promise<void> {
  await command(CMD_CANCEL, { [ARG_SESSION_ID]: sessionId ?? '' })
}

/** The user's answer to an approval or a question. */
export interface InteractionAnswer {
  requestId: string
  /** `APPROVED`, `APPROVED_FOR_SESSION`, `REJECTED`, or the answer to a question. */
  response: string
  /** The tool an approval is about; an `APPROVED_FOR_SESSION` approves it for the chat. */
  toolName?: string
  /** Why a tool call was denied. */
  comment?: string
  /** What the user typed for a choice question, beside the options picked. */
  ownWords?: string
}

/**
 * Answers an approval or a question a run of the account waits for, whichever window started it.
 * An answer nothing waits for any more is no failure.
 *
 * @throws when the answer did not reach the server.
 */
export async function respond(command: ChatCommand, answer: InteractionAnswer): Promise<void> {
  const reply = await command(CMD_RESPOND, {
    [ARG_REQUEST_ID]: answer.requestId,
    [ARG_RESPONSE]: answer.response,
    [ARG_TOOL_NAME]: answer.toolName,
    [ARG_COMMENT]: answer.comment,
    [ARG_OWN_WORDS]: answer.ownWords,
  })
  const status = statusOf(reply)
  if (status !== STATUS_OK && status !== STATUS_IGNORED) throw new Error('respond failed')
}

/**
 * Starts improving a draft; the result arrives as a {@link FRAME_PROMPT_IMPROVED} frame naming
 * `requestId`.
 *
 * @throws RefusedError when there is no room for the run right now, with the server's sentence;
 *         an Error when the improvement was not started.
 */
export async function startPromptImprovement(command: ChatCommand, draft: string, requestId: string): Promise<void> {
  const answer = await command(CMD_IMPROVE_PROMPT, { [ARG_DRAFT]: draft, [ARG_REQUEST_ID]: requestId })
  const status = statusOf(answer)
  if (status === STATUS_STARTED) return
  const reason = typeof answer?.[FIELD_REASON] === 'string' ? (answer[FIELD_REASON] as string) : ''
  if (status === STATUS_REJECTED) throw new RefusedError(reason)
  if (status === STATUS_FORBIDDEN) throw new ForbiddenError(reason)
  throw new Error('improve prompt failed')
}

/** Cancels the improvement `requestId` names. */
export async function cancelPromptImprovement(command: ChatCommand, requestId: string): Promise<void> {
  await command(CMD_CANCEL_PROMPT_IMPROVEMENT, { [ARG_REQUEST_ID]: requestId })
}

/** The result of an improvement, as its {@link FRAME_PROMPT_IMPROVED} frame states it. */
export interface PromptImproved {
  requestId: string
  /** `completed`, `cancelled`, `stale`, `error` or `forbidden`. */
  status: string
  /** The improved draft; empty unless `completed`. */
  improvedText: string
  /** Why the improvement was refused, a sentence for the user; empty unless it was. */
  reason: string
}

/** Reads a {@link FRAME_PROMPT_IMPROVED} frame. */
export function readPromptImproved(data: unknown): PromptImproved | undefined {
  if (data == null || typeof data !== 'object') return undefined
  const raw = data as Record<string, unknown>
  if (typeof raw.requestId !== 'string' || !raw.requestId) return undefined
  return {
    requestId: raw.requestId,
    status: typeof raw.status === 'string' ? raw.status : '',
    improvedText: typeof raw.improvedText === 'string' ? raw.improvedText : '',
    reason: typeof raw.reason === 'string' ? raw.reason : '',
  }
}

/**
 * Turns "auto-approve" on or off. The server stores the mode as the person's last choice, which a
 * new chat starts with, and with the chat `sessionId` names; it tells the account's other windows
 * showing that chat (#658). A conversation that has no chat yet passes `null` and names the mode with
 * the send that creates the chat (`sendMessage`).
 *
 * @throws when the switch was not stored.
 */
export async function setApprovalMode(
  command: ChatCommand,
  mode: ApprovalModeName,
  sessionId: string | null,
): Promise<void> {
  const answer = await command(
    CMD_SET_APPROVAL_MODE,
    sessionId ? { [ARG_MODE]: mode, [ARG_SESSION_ID]: sessionId } : { [ARG_MODE]: mode },
  )
  if (statusOf(answer) !== STATUS_OK) throw new Error('approval mode not stored')
}

/**
 * Records, changes or withdraws what the user thought of one answer.
 *
 * @throws when the rating was not stored.
 */
export async function rateAnswer(
  command: ChatCommand,
  sessionId: string,
  turnIndex: number,
  rating: Rating,
  note?: string,
): Promise<void> {
  const answer = await command(CMD_RATE, {
    [ARG_SESSION_ID]: sessionId,
    [ARG_TURN_INDEX]: turnIndex,
    [ARG_RATING]: rating ?? '',
    [ARG_NOTE]: note ?? '',
  })
  if (statusOf(answer) !== STATUS_OK) throw new Error('rating not stored')
}

/**
 * Stops one background subagent of the given chat, whichever window of the account started it.
 *
 * @param parentToolCallId The lead's call that started it, which tells it from a namesake.
 * @throws when the stop did not reach the server.
 */
export async function cancelSubagent(
  command: ChatCommand,
  id: string,
  sessionId: string | null,
  parentToolCallId?: string,
): Promise<void> {
  const answer = await command(CMD_CANCEL_SUBAGENT, {
    [ARG_SUBAGENT_ID]: id,
    [ARG_SESSION_ID]: sessionId ?? '',
    [ARG_PARENT_TOOL_CALL_ID]: parentToolCallId ?? '',
  })
  if (!answer) throw new Error('cancel subagent failed')
}

/**
 * What the server answered a message the user wrote to a subagent: `sent` - it reads it at the start
 * of its next turn; `ended` - it has ended and reads no more; `unknown` - no such subagent in this
 * account's chat; `empty` - there was no text. Must match `AccountRuns.SubagentMessageStatus`.
 */
export type SubagentMessageStatus = 'sent' | 'ended' | 'unknown' | 'empty'

const SUBAGENT_MESSAGE_STATUSES: SubagentMessageStatus[] = ['sent', 'ended', 'unknown', 'empty']

/**
 * Writes to one running background subagent, from its card.
 *
 * @param sessionId The chat whose lead started it.
 * @param parentToolCallId The lead's call that started it, which tells it from a namesake.
 * @throws when the message did not reach the server.
 */
export async function sendSubagentMessage(
  command: ChatCommand,
  id: string,
  sessionId: string | null,
  parentToolCallId: string,
  text: string,
): Promise<{ status: SubagentMessageStatus; messageId: string }> {
  const answer = await command(CMD_MESSAGE_SUBAGENT, {
    [ARG_SUBAGENT_ID]: id,
    [ARG_SESSION_ID]: sessionId ?? '',
    [ARG_PARENT_TOOL_CALL_ID]: parentToolCallId,
    [ARG_TEXT]: text,
  })
  const status = SUBAGENT_MESSAGE_STATUSES.find((item) => item === statusOf(answer))
  if (!status) throw new Error('message to subagent failed')
  const messageId = answer?.[FIELD_MESSAGE_ID]
  return { status, messageId: typeof messageId === 'string' ? messageId : '' }
}

/**
 * The transcript of one subagent: its live log while it runs, its recorded run afterwards.
 *
 * @param runId The run its recorded part names, for a subagent the server holds no job for any
 *   more - an earlier server life, a chat this window no longer holds.
 */
export async function loadSubagentTranscript(dataUrl: string, id: string, runId?: string): Promise<ChatMessage[]> {
  const key = runId ? dataKey(DATA_SUBAGENT, id, runId) : dataKey(DATA_SUBAGENT, id)
  const detail = await readData<SessionDetail>(dataUrl, key)
  return turnsJsonToMessages(detail.turnsJson, detail.runs)
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
  return reason === 'MAX_TURNS' || reason === 'BUDGET_EXHAUSTED' || reason === 'OUTPUT_LIMIT'
}

/** How a subagent whose job completed ended, from its run's stop reason: with its answer, or at a limit. */
export function completedStatus(reason: string | undefined): SubagentStatus {
  return isLimitStop(reason) ? 'LIMIT' : 'DONE'
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
  return keepLast(steps, MAX_CHILD_STEPS)
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

/** The cost fields of a recorded run, none when it reported no cost. */
function costOf(run: RecordedRun | undefined): { cost?: number; currency?: string } {
  return run && run.currency ? { cost: run.cost, currency: run.currency } : {}
}

/** What the history knows about one recorded run, by the last turn it wrote. */
export interface RecordedRun {
  turnId: string
  /** When the run reported its start, in milliseconds since the epoch; zero if not recorded. */
  startedAt?: number
  /** When the run ended, in milliseconds since the epoch; zero while it has not ended. */
  endedAt?: number
  /** The cost in `currency`; absent or zero without a currency. */
  cost?: number
  /** Empty or absent when the run reported no cost. */
  currency?: string
}

/**
 * Start and duration of the run meta line of a restored turn, `null` for none.
 *
 * Measured the way a live run is: from when the run reported its start to its end, both as
 * recorded with the run that ended with this turn - a turn starts with the user's message, which may
 * be well before the process answering it is running. A turn without such a run (one the run did
 * not end with, a transcript older than the recorded times) is measured from its own start to its
 * last recorded part; one whose parts carry no later stamp shows no duration rather than a
 * fabricated one.
 */
function restoredRunSpan(
  turn: PersistedTurn,
  run: RecordedRun | undefined,
): { startedAt: number; durationMs: number } | null {
  if (run && run.startedAt && run.endedAt && run.startedAt >= turn.timestamp && run.endedAt >= run.startedAt) {
    return { startedAt: run.startedAt, durationMs: run.endedAt - run.startedAt }
  }
  const lastPartAt = (turn.assistantParts ?? []).reduce(
    (latest, part) => Math.max(latest, part.timestamp ?? 0),
    0,
  )
  return lastPartAt > turn.timestamp ? { startedAt: turn.timestamp, durationMs: lastPartAt - turn.timestamp } : null
}

export function turnsJsonToMessages(turnsJson: string | undefined, runs?: RecordedRun[]): ChatMessage[] {
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
    const run = runs?.find((item) => item.turnId === turn.id)
    const span = restoredRunSpan(turn, run)
    if (span) {
      msgs.push({
        type: 'run_meta',
        id: `${turn.id}-run`,
        runId: turn.id,
        startedAt: span.startedAt,
        durationMs: span.durationMs,
        ...costOf(run),
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
