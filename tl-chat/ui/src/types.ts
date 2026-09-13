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
}

export interface TokenUsage {
  inputTokens?: number
  outputTokens?: number
  thinkingTokens?: number
  cacheReadTokens?: number
  cacheWriteTokens?: number
}

export interface RunSnapshot {
  busy: boolean
  usedTokens: number
  contextWindow: number
  model: string
  lastUsage?: TokenUsage
}
