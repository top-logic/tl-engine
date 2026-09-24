import type {
  ApprovalOutcome,
  ApprovalRequest,
  AssistantText,
  ChatMessage,
  Citation,
  ErrorMessage,
  InteractionOption,
  InteractionRequest,
  NoticeKind,
  NoticeMessage,
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
  | { type: 'ADD_USER'; id: string; text: string; edited?: boolean }
  | { type: 'REMOVE_MESSAGE'; id: string }
  | { type: 'REMOVE_FROM_USER'; id: string }
  | { type: 'ADD_ASSISTANT'; id: string; text: string; isStreaming: boolean }
  | { type: 'APPEND_TEXT'; id: string; delta: string }
  | { type: 'SET_TEXT'; id: string; text: string; citations?: Citation[] }
  | { type: 'FINALIZE_TEXT'; id: string }
  | { type: 'RESUME_TEXT'; id: string }
  | { type: 'ADD_TOOL'; toolUseId: string; name: string; input: unknown; depth?: number }
  | { type: 'UPDATE_TOOL'; toolUseId: string; status: ToolCallStatus; durationMs?: number }
  | { type: 'ADD_TOOL_RESULT'; toolUseId: string; output: string; isError?: boolean }
  | { type: 'APPEND_THINKING'; delta: string }
  | { type: 'ADD_ERROR'; message: string; code?: string }
  | { type: 'ADD_NOTICE'; kind: NoticeKind; detail: string }
  | {
      type: 'ADD_APPROVAL'
      approvalRequestId: string
      toolCallId: string
      toolName: string
      argsJson: string
      displayReason: string
      toolMode?: string
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
      const msg: UserMessage = {
        type: 'user',
        id: action.id,
        text: action.text,
        timestamp: Date.now(),
        edited: action.edited,
      }
      return [...messages, msg]
    }
    case 'REMOVE_MESSAGE':
      return messages.filter((m) => m.id !== action.id)
    case 'REMOVE_FROM_USER': {
      // Everything after a user message was said in reply to it, so it goes too.
      const index = messages.findIndex((m) => m.type === 'user' && m.id === action.id)
      return index < 0 ? messages : messages.slice(0, index)
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
        m.type === 'assistant_text' && m.id === action.id && !m.authoritative
          ? { ...m, text: m.text + action.delta }
          : m,
      )
    case 'SET_TEXT':
      // The authoritative answer, from message_complete. It replaces whatever the deltas built -
      // they are a preview of it - and closes the message to further deltas, so a fragment that
      // arrives late cannot append itself to the finished text.
      return messages.map((m) =>
        m.type === 'assistant_text' && m.id === action.id
          ? { ...m, text: action.text, authoritative: true, citations: action.citations }
          : m,
      )
    case 'FINALIZE_TEXT':
      return messages.map((m) =>
        m.type === 'assistant_text' && m.id === action.id ? { ...m, isStreaming: false } : m,
      )
    case 'RESUME_TEXT':
      // The inverse, for a turn that is restored while it is still being written (#512): what came
      // out of the transcript is a finished answer as far as the reducer is concerned, and a
      // finished answer carries a copy/rate/regenerate row. Offering "regenerate" for a turn the
      // agent has not finished is the one thing that row must not do.
      return messages.map((m) =>
        m.type === 'assistant_text' && m.id === action.id ? { ...m, isStreaming: true } : m,
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
    case 'ADD_NOTICE': {
      const msg: NoticeMessage = {
        type: 'notice',
        id: genId(),
        kind: action.kind,
        detail: action.detail,
        timestamp: Date.now(),
      }
      return [...messages, msg]
    }
    case 'ADD_APPROVAL': {
      // A card the transcript already carries is completed, not repeated. A restored conversation
      // brings the pending approval with it, in its place under the turn that asked for it, and
      // what only the waiting run knows - the tool's mode, how long is left to answer - arrives
      // afterwards from `/chat/interactions` (#512). Appending it a second time would put the same
      // question twice on screen, at the bottom and out of its turn.
      const shown = messages.find(
        (m): m is ApprovalRequest =>
          m.type === 'approval_request' && m.approvalRequestId === action.approvalRequestId,
      )
      if (shown) {
        const completed: ApprovalRequest = {
          ...shown,
          toolCallId: shown.toolCallId || action.toolCallId,
          toolName: shown.toolName || action.toolName,
          argsJson: shown.argsJson || action.argsJson,
          displayReason: shown.displayReason || action.displayReason,
          toolMode: shown.toolMode ?? action.toolMode,
          remainingMs: action.remainingMs,
          receivedAt: Date.now(),
        }
        return messages.map((m) => (m === shown ? completed : m))
      }
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
          toolMode: action.toolMode,
          outcome: 'pending',
          timestamp: Date.now(),
          remainingMs: action.remainingMs,
          receivedAt: Date.now(),
        },
      ]
    }
    case 'RESOLVE_APPROVAL':
      return messages.map((m) =>
        m.type === 'approval_request' && m.approvalRequestId === action.approvalRequestId
          ? { ...m, outcome: action.outcome }
          : m,
      )
    case 'ADD_INTERACTION': {
      // Completed rather than repeated, for the reason ADD_APPROVAL gives: a restored in-flight
      // turn already carries the question, and this is what fills in the shape it was asked in.
      const asked = messages.find(
        (m): m is InteractionRequest =>
          m.type === 'interaction_request' && m.requestId === action.requestId,
      )
      if (asked) {
        const completed: InteractionRequest = {
          ...asked,
          interactionType: action.interactionType ?? asked.interactionType,
          options: action.options ?? asked.options,
          multiSelect: action.multiSelect ?? asked.multiSelect,
          remainingMs: action.remainingMs,
          receivedAt: Date.now(),
        }
        return messages.map((m) => (m === asked ? completed : m))
      }
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
