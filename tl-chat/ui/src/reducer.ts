import type {
  ApprovalOutcome,
  AssistantText,
  ChatMessage,
  ErrorMessage,
  InteractionOption,
  InteractionRequest,
  RunMeta,
  ThinkingBlock,
  ToolCall,
  ToolCallStatus,
  ToolResult,
  UserMessage,
} from './types'

function genId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

export type ChatAction =
  | { type: 'ADD_USER'; text: string }
  | { type: 'ADD_ASSISTANT'; id: string; text: string; isStreaming: boolean }
  | { type: 'APPEND_TEXT'; id: string; delta: string }
  | { type: 'FINALIZE_TEXT'; id: string }
  | { type: 'ADD_TOOL'; toolUseId: string; name: string; input: unknown; depth?: number }
  | { type: 'UPDATE_TOOL'; toolUseId: string; status: ToolCallStatus; durationMs?: number }
  | { type: 'ADD_TOOL_RESULT'; toolUseId: string; output: string; isError?: boolean }
  | { type: 'APPEND_THINKING'; delta: string }
  | { type: 'ADD_ERROR'; message: string; code?: string }
  | {
      type: 'ADD_APPROVAL'
      approvalRequestId: string
      toolCallId: string
      toolName: string
      argsJson: string
      displayReason: string
      remainingMs?: number
    }
  | { type: 'RESOLVE_APPROVAL'; approvalRequestId: string; outcome: ApprovalOutcome }
  | {
      type: 'ADD_INTERACTION'
      requestId: string
      question: string
      header?: string
      interactionType?: 'text' | 'choice'
      options?: InteractionOption[]
      multiSelect?: boolean
      remainingMs?: number
    }
  | { type: 'RESOLVE_INTERACTION'; requestId: string }
  | { type: 'START_RUN'; runId: string }
  | { type: 'END_RUN'; runId: string }
  | { type: 'REPLACE'; messages: ChatMessage[] }
  | { type: 'RESET' }

export function chatReducer(messages: ChatMessage[], action: ChatAction): ChatMessage[] {
  switch (action.type) {
    case 'ADD_USER': {
      const msg: UserMessage = { type: 'user', id: genId(), text: action.text, timestamp: Date.now() }
      return [...messages, msg]
    }
    case 'ADD_ASSISTANT': {
      const msg: AssistantText = {
        type: 'assistant_text',
        id: action.id,
        text: action.text,
        isStreaming: action.isStreaming,
        timestamp: Date.now(),
      }
      return [...messages, msg]
    }
    case 'APPEND_TEXT':
      return messages.map((m) =>
        m.type === 'assistant_text' && m.id === action.id ? { ...m, text: m.text + action.delta } : m,
      )
    case 'FINALIZE_TEXT':
      return messages.map((m) =>
        m.type === 'assistant_text' && m.id === action.id ? { ...m, isStreaming: false } : m,
      )
    case 'ADD_TOOL': {
      const msg: ToolCall = {
        type: 'tool_call',
        id: genId(),
        toolUseId: action.toolUseId,
        name: action.name,
        input: action.input,
        status: 'running',
        depth: action.depth ?? 0,
        timestamp: Date.now(),
      }
      return [...messages, msg]
    }
    case 'UPDATE_TOOL':
      return messages.map((m) =>
        m.type === 'tool_call' && m.toolUseId === action.toolUseId
          ? { ...m, status: action.status, durationMs: action.durationMs }
          : m,
      )
    case 'ADD_TOOL_RESULT': {
      const msg: ToolResult = {
        type: 'tool_result',
        id: genId(),
        toolUseId: action.toolUseId,
        output: action.output,
        isError: action.isError ?? false,
        timestamp: Date.now(),
      }
      return [...messages, msg]
    }
    case 'APPEND_THINKING': {
      const last = messages[messages.length - 1]
      if (last?.type === 'thinking') {
        return [...messages.slice(0, -1), { ...last, content: last.content + action.delta }]
      }
      const msg: ThinkingBlock = {
        type: 'thinking',
        id: genId(),
        content: action.delta,
        timestamp: Date.now(),
      }
      return [...messages, msg]
    }
    case 'ADD_ERROR': {
      const msg: ErrorMessage = {
        type: 'error',
        id: genId(),
        message: action.message,
        code: action.code,
        timestamp: Date.now(),
      }
      return [...messages, msg]
    }
    case 'ADD_APPROVAL':
      return [
        ...messages,
        {
          type: 'approval_request',
          id: action.approvalRequestId,
          approvalRequestId: action.approvalRequestId,
          toolCallId: action.toolCallId,
          toolName: action.toolName,
          argsJson: action.argsJson,
          displayReason: action.displayReason,
          outcome: 'pending',
          timestamp: Date.now(),
          remainingMs: action.remainingMs,
          receivedAt: Date.now(),
        },
      ]
    case 'RESOLVE_APPROVAL':
      return messages.map((m) =>
        m.type === 'approval_request' && m.approvalRequestId === action.approvalRequestId
          ? { ...m, outcome: action.outcome }
          : m,
      )
    case 'ADD_INTERACTION': {
      const msg: InteractionRequest = {
        type: 'interaction_request',
        id: action.requestId,
        requestId: action.requestId,
        question: action.question,
        header: action.header,
        answered: false,
        timestamp: Date.now(),
        interactionType: action.interactionType,
        options: action.options,
        multiSelect: action.multiSelect,
        remainingMs: action.remainingMs,
        receivedAt: Date.now(),
      }
      return [...messages, msg]
    }
    case 'RESOLVE_INTERACTION':
      return messages.map((m) =>
        m.type === 'interaction_request' && m.requestId === action.requestId ? { ...m, answered: true } : m,
      )
    case 'START_RUN': {
      if (messages.some((m) => m.type === 'run_meta' && m.runId === action.runId)) {
        return messages
      }
      const now = Date.now()
      const msg: RunMeta = {
        type: 'run_meta',
        id: `run-meta-${action.runId}`,
        runId: action.runId,
        startedAt: now,
        timestamp: now,
      }
      return [...messages, msg]
    }
    case 'END_RUN':
      return messages.map((m) =>
        m.type === 'run_meta' && m.runId === action.runId && m.durationMs == null
          ? { ...m, durationMs: Date.now() - m.startedAt }
          : m,
      )
    case 'REPLACE':
      return action.messages
    case 'RESET':
      return []
  }
}
