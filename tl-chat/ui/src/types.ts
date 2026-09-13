export type TraceMode = 'none' | 'overview' | 'detailed'

export type ToolCallStatus = 'pending' | 'running' | 'done' | 'error'

export type ApprovalOutcome =
  | 'pending'
  | 'APPROVED'
  | 'APPROVED_FOR_SESSION'
  | 'REJECTED'
  | 'TIMED_OUT'

export interface UserMessage {
  type: 'user'
  id: string
  text: string
  timestamp: number
}

export interface AssistantText {
  type: 'assistant_text'
  id: string
  text: string
  isStreaming: boolean
  timestamp: number
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

export interface ApprovalRequest {
  type: 'approval_request'
  id: string
  approvalRequestId: string
  toolCallId: string
  toolName: string
  argsJson: string
  displayReason: string
  outcome: ApprovalOutcome
  timestamp: number
  /**
   * Milliseconds the user had left when the event arrived, and the local clock reading at that
   * moment. Both are needed: the deadline is the server's, the countdown is the browser's.
   */
  remainingMs?: number
  receivedAt?: number
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
  | ApprovalRequest
  | InteractionRequest
  | RunMeta

export interface AgentOption {
  name: string
  description?: string
  mcpServers?: string[]
  model?: string
}

export interface ModelOption {
  id: string
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
}

export interface SubagentStatusEvent {
  subagentId: string
  targetAgentName?: string
  detail?: string
  totalTokens?: number
  durationMs?: number
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
  messageTokens: number
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
