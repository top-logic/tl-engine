export type TraceMode = 'none' | 'summary' | 'detailed'

export type ToolCallStatus = 'pending' | 'running' | 'done' | 'error'

export type ApprovalOutcome =
  | 'pending'
  | 'APPROVED'
  | 'APPROVED_FOR_SESSION'
  | 'REJECTED'
  | 'TIMED_OUT'
  /** The server restarted while the approval was open; nobody can answer it any more (#598). */
  | 'INTERRUPTED'

export interface UserMessage {
  type: 'user'
  id: string
  text: string
  timestamp: number
  /** Whether this message replaced an earlier wording of itself, shown as a marker on the bubble. */
  edited?: boolean
}

export interface AssistantText {
  type: 'assistant_text'
  id: string
  text: string
  isStreaming: boolean
  timestamp: number
  /**
   * Set once the server has sent the finished answer. The deltas are a live preview of that text,
   * so one that arrives afterwards - the stream and the completion travel the same queue but a
   * consumer may still be behind - must not be appended to it.
   */
  authoritative?: boolean
  /** Structured citations the server kept for this answer; absent when the turn cited nothing. */
  citations?: Citation[]
}

/**
 * One source a `[n]` marker in an answer points to.
 *
 * Mirrors {@code CitationDto} on the wire. The server keeps a citation only when a tool of the run
 * read the source it points at.
 */
export interface Citation {
  index: number
  toolName: string
  toolCallId: string
  /** What kind of source this is, as the application declared it, e.g. `web` or `invoice`. */
  kind: string
  /**
   * What to call that kind in front of a reader, in the reader's language.
   *
   * Empty when the host declared no label for the kind; {@link kind} is shown instead.
   */
  kindLabel?: string
  id: string
  label: string
  url: string
  /** The sentence of the source the fact rests on; empty when the model named none. */
  passage?: string
  /** A window of the source around {@link passage}, shown in the marker's popover. */
  excerpt?: string
  /** Where the copy of a web page kept when it was read can be opened; empty without one. */
  archiveUrl?: string
  /** When that copy was taken, ISO-8601. */
  archivedAt?: string
}

export interface ToolCall {
  type: 'tool_call'
  id: string
  toolUseId: string
  name: string
  input: unknown
  status: ToolCallStatus
  durationMs?: number
  depth: number
  timestamp: number
}

export interface ToolResult {
  type: 'tool_result'
  id: string
  toolUseId: string
  output: string
  isError: boolean
  timestamp: number
}

export interface ThinkingBlock {
  type: 'thinking'
  id: string
  content: string
  timestamp: number
}

export interface ErrorMessage {
  type: 'error'
  id: string
  code?: string
  message: string
  timestamp: number
}

/**
 * What a notice is about.
 *
 * `model_switched` - the run lost the model it was configured with and continued on another one.
 * `transaction_rolled_back` - the data changes this turn made were discarded instead of saved.
 * `cancelled` - the user stopped the run.
 * `interrupted` - the server restarted before the run finished, and nobody can continue it (#598).
 */
export type NoticeKind = 'model_switched' | 'transaction_rolled_back' | 'cancelled' | 'interrupted'

/**
 * Something that happened around the conversation rather than in it.
 *
 * Not an {@link ErrorMessage} - the run itself did not fail - and not an assistant message either,
 * because the agent did not say it. A rolled-back turn is the sharper case: the model may well write
 * "done" over writes that were thrown away, so the thread has to say what really happened.
 *
 * The sentence the user reads is the renderer's, from `kind`, so that a notice reads the same live
 * as after a reload (#429) and in the reader's language (#549).
 */
export interface NoticeMessage {
  type: 'notice'
  id: string
  /** What the notice is about. */
  kind: NoticeKind
  /**
   * The server's line under the sentence: `from -> to: reason` for a model switch, the rollback
   * reason for a rollback, empty for a cancel, which has nothing to add to "stopped".
   */
  detail: string
  timestamp: number
}

/**
 * The background subagent that asks, when it is not the agent the user talks to.
 *
 * A subagent works on after its lead's turn has ended, so its question can arrive while the chat
 * looks idle; the card names it, or the user answers a worker believing the main agent asked.
 */
export interface Asker {
  /** The subagent's id within the chat, e.g. `sa-2`. */
  subagentId: string
  /** The agent the subagent runs; may be empty when only the id is known. */
  agent: string
}

export interface ApprovalRequest {
  type: 'approval_request'
  id: string
  approvalRequestId: string
  toolCallId: string
  toolName: string
  argsJson: string
  displayReason: string
  /**
   * What kind of change the tool makes (`DATA_WRITE`, `STRUCTURAL`, …). Why approval is being
   * asked, in a form the UI can phrase in the user's language; `displayReason` is the server's
   * English wording of the same thing.
   */
  toolMode?: string
  outcome: ApprovalOutcome
  timestamp: number
  /**
   * Milliseconds the user had left when the event arrived, and the local clock reading at that
   * moment. Both are needed: the deadline is the server's, the countdown is the browser's.
   */
  remainingMs?: number
  receivedAt?: number
  askedBy?: Asker
}

export interface InteractionOption {
  label: string
  description?: string
}

export interface InteractionRequest {
  type: 'interaction_request'
  id: string
  requestId: string
  question: string
  header?: string
  answered: boolean
  timestamp: number
  interactionType?: 'text' | 'choice'
  options?: InteractionOption[]
  multiSelect?: boolean
  /** @see ApprovalRequest.remainingMs */
  remainingMs?: number
  receivedAt?: number
  askedBy?: Asker
}

/**
 * How long one run took, carried in the transcript so the meta line above an answer can say it.
 *
 * A marker rather than a field on the answer: a run can produce several answers, or none, and it
 * has a start before it has any.
 */
export interface RunMeta {
  type: 'run_meta'
  id: string
  runId: string
  startedAt: number
  durationMs?: number
  timestamp: number
}

export type ChatMessage =
  | UserMessage
  | AssistantText
  | ToolCall
  | ToolResult
  | ThinkingBlock
  | ErrorMessage
  | NoticeMessage
  | ApprovalRequest
  | InteractionRequest
  | RunMeta
  | SubagentRef

export interface AgentOption {
  /** The identifier: what a request names and what a persisted session holds. */
  name: string
  /**
   * What to call this agent in front of the user, in their language.
   *
   * Absent for an agent that has no display name; it is then shown by its identifier.
   */
  label?: string
  /**
   * One localised line saying what this agent is for.
   *
   * Absent for an agent that has none - its delegation description is written for a model and is
   * never shown to a person, so the picker then offers the name alone.
   */
  summary?: string
  mcpServers?: string[]
  model?: string
}

/** Whether a notice above the composer reports a failure or merely says what happened. */
export type NoticeTone = 'warn' | 'info'

export interface ModelOption {
  id: string
  /**
   * Whether the account may choose this model whatever agent it runs.
   *
   * False for a model that is listed only because an agent the account may run is assigned it - that
   * one is accepted with *that* agent and refused with another, so a menu has to narrow by the agent
   * the user has selected.
   */
  grantedToRole?: boolean
  provider?: string
  contextWindow?: number
  maxOutput?: number
  /**
   * How hard this model may be asked to think, in increasing order; empty when it offers no such
   * setting. A UI must check this before offering the choice: the providers reject a level their
   * model does not know.
   */
  effortLevels?: string[]
  /** The level in force when none is chosen. */
  defaultEffort?: string
}

/** What the user thought of an answer; `null` means they said nothing. */
export type Rating = 'up' | 'down' | null

/** One rating, addressed by the turn whose answer it belongs to. */
export interface MessageFeedback {
  turnIndex: number
  rating: 'up' | 'down'
  note?: string
  recordedAt?: string
}

export interface McpServerOption {
  name: string
  status?: string
}

export interface Capabilities {
  entryPointAgent?: string
  agents: AgentOption[]
  models: ModelOption[]
  mcpServers: McpServerOption[]
  /**
   * Whether the prompt improver would run for this account.
   *
   * False when a role forbids the model it runs on: it is outside the agent allowlist, but what it
   * runs on is still decided, so its button would offer a refusal. Absent means yes, so a host that
   * does not send the field keeps the button.
   */
  promptImproverAvailable?: boolean
}

export type SubagentStatus = 'QUEUED' | 'RUNNING' | 'DONE' | 'FAILED' | 'CANCELLED'

export interface SubagentActivity {
  subagentId: string
  targetAgentName: string
  status: SubagentStatus
  detail: string
  totalTokens: number
  durationMs: number
  hasTranscript: boolean
  /** The id of the lead's tool call that started it - where its card is drawn. */
  parentToolCallId?: string
  /** The task brief it was given. */
  task?: string
}

/** One tool call of a running subagent, as its card lists it. */
export interface SubagentStep {
  toolUseId: string
  name: string
  /** The start of the call's arguments. */
  summary: string
  status: ToolCallStatus
}

/**
 * What a running subagent has done so far, kept from its live events.
 *
 * Kept apart from {@link SubagentActivity}, which the server's list replaces wholesale: a refresh of
 * the list must not wipe the steps a card is showing.
 */
export interface SubagentLive {
  steps: SubagentStep[]
  /** The end of the text the child is writing or wrote last. */
  text: string
  turns: number
  /** Tokens of its finished turns. */
  tokens: number
  /** Local clock reading when the first event arrived, for the elapsed time. */
  startedAt: number
}

/**
 * A background subagent a turn started, as the lead's recorded log keeps it (`Part.SubagentPart`).
 *
 * What rebuilds the subagent's card after a reload, when the server no longer holds the job.
 */
export interface SubagentRef {
  type: 'subagent_ref'
  id: string
  subagentId: string
  agentName: string
  task: string
  parentToolCallId: string
  /** Its run id, by which its recorded transcript is found; empty while unknown. */
  childRunId: string
  status: SubagentStatus
  summary: string
  timestamp: number
}

export interface SubagentStatusEvent {
  subagentId: string
  targetAgentName?: string
  detail?: string
  totalTokens?: number
  durationMs?: number
  /** On start: the id of the lead's tool call that started it. */
  parentToolCallId?: string
}

export interface ImprovePromptResponse {
  requestId: string
  status: string
  improvedText?: string
  text?: string
}

export interface SessionSummary {
  id: string
  title: string
  lastUpdated?: string
}

export interface Attachment {
  fileName: string
  mimeType: string
  base64Data: string
  size: number
}

/**
 * One fact the page states about the situation a message is sent in.
 *
 * Computed on the server from the chat element's `<context>` configuration and pushed into the
 * control state; the browser only carries it back, because the run happens on a background thread
 * long after those channels were read. The message text is never touched by it.
 */
export interface ContextFact {
  name: string
  value: string
  /**
   * Whether the value holds for the whole conversation.
   *
   * Only a stable fact reaches the system prompt, which has to stay byte-identical across the turns
   * of one conversation or the provider's prompt cache misses on every one of them.
   */
  stable: boolean
}

export interface SendPayload {
  text: string
  sessionId: string
  agent?: string
  model?: string
  mcpOverride: boolean
  mcpServers: string[]
  attachments?: Attachment[]
  /** How hard the model may think for this turn; omitted keeps the agent's assignment. */
  effort?: string
  /** What the page states about the situation this message is asked in. */
  facts?: ContextFact[]
}

export interface TokenUsage {
  inputTokens?: number
  outputTokens?: number
  thinkingTokens?: number
  cacheReadTokens?: number
  cacheWriteTokens?: number
}

/**
 * The prompt size the provider actually billed: everything the model read.
 *
 * Cached input is still input - a cache hit changes the price, not the context - so it counts here.
 */
export function promptTokens(usage: TokenUsage | undefined): number {
  if (!usage) return 0
  return (usage.inputTokens ?? 0) + (usage.cacheReadTokens ?? 0) + (usage.cacheWriteTokens ?? 0)
}

/** Tokens the model wrote in the turn: the answer plus any thinking. */
export function answerTokens(usage: TokenUsage | undefined): number {
  if (!usage) return 0
  return (usage.outputTokens ?? 0) + (usage.thinkingTokens ?? 0)
}

/**
 * Estimated split of the prompt, from the agent loop's `context_composition` event.
 *
 * These are the loop's own estimates - the provider reports a single prompt total and never breaks
 * it down - so they are labelled as estimates and never replace the measured total.
 */
export interface ContextComposition {
  systemPromptTokens: number
  toolDefinitionTokens: number
  /** The whole conversation, tool results included. */
  messageTokens: number
  /** The part of `messageTokens` the agent read back from the tools it ran. */
  toolResultTokens: number
  toolCount: number
}

export interface RunSnapshot {
  busy: boolean
  /**
   * Provider-reported prompt size of the most recent request; zero until a turn has completed.
   * This is the exact context the model saw, and takes precedence over `estimatedTokens`.
   */
  measuredTokens: number
  /** The agent loop's estimate, used only until the session has a measurement. */
  estimatedTokens: number
  contextWindow: number
  model: string
  /** Effort level in force for this chat, empty when the model offers none or none was chosen. */
  effort: string
  lastUsage?: TokenUsage
  composition?: ContextComposition
}

/** Health of the SSE connection, derived from heartbeats and stream errors. */
export type ConnectionState = 'connecting' | 'live' | 'reconnecting'
