import type {
  ApprovalOutcome,
  Attachment,
  Capabilities,
  ChatMessage,
  ImprovePromptResponse,
  InteractionOption,
  SendPayload,
  SessionSummary,
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

function withWindow(path: string, windowId: string): string {
  const sep = path.includes('?') ? '&' : '?'
  return `${apiBase()}${path}${sep}windowId=${encodeURIComponent(windowId)}`
}

async function readJson<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return (await res.json()) as T
}

export async function fetchCapabilities(windowId: string): Promise<Capabilities> {
  const data = await readJson<{
    entryPointAgent?: string
    agents?: Capabilities['agents']
    models?: Capabilities['models']
    mcpServers?: Capabilities['mcpServers']
  }>(await fetch(withWindow('/chat/capabilities', windowId), { credentials: 'include' }))
  return {
    entryPointAgent: data.entryPointAgent,
    agents: data.agents ?? [],
    models: data.models ?? [],
    mcpServers: data.mcpServers ?? [],
  }
}

export async function listSessions(windowId: string): Promise<SessionSummary[]> {
  const data = await readJson<{ sessions?: SessionSummary[] }>(
    await fetch(withWindow('/chat/sessions', windowId), { credentials: 'include' }),
  )
  return data.sessions ?? []
}

export async function createSession(windowId: string, title: string): Promise<SessionSummary> {
  return readJson<SessionSummary>(
    await fetch(withWindow('/chat/sessions', windowId), {
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
}

export async function loadSession(windowId: string, id: string): Promise<SessionDetail> {
  return readJson<SessionDetail>(
    await fetch(withWindow(`/chat/sessions/${encodeURIComponent(id)}`, windowId), {
      credentials: 'include',
    }),
  )
}

export async function renameSession(windowId: string, id: string, title: string): Promise<void> {
  const res = await fetch(withWindow(`/chat/sessions/${encodeURIComponent(id)}/title`, windowId), {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ title }),
  })
  if (!res.ok) throw new Error(`rename failed (${res.status})`)
}

export async function deleteSession(windowId: string, id: string): Promise<void> {
  const res = await fetch(withWindow(`/chat/sessions/${encodeURIComponent(id)}`, windowId), {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!res.ok) throw new Error(`delete failed (${res.status})`)
}

export async function sendMessage(windowId: string, payload: SendPayload): Promise<{ taskId?: string; status: string }> {
  const res = await fetch(withWindow('/chat/send', windowId), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(payload),
  })
  const data = (await res.json()) as { taskId?: string; status?: string }
  if (res.status === 409 || data.status === 'busy') {
    throw new Error('busy')
  }
  if (!res.ok || data.status === 'error') {
    throw new Error(data.status ?? `send failed (${res.status})`)
  }
  return { taskId: data.taskId, status: data.status ?? 'processing' }
}

export async function cancelRun(windowId: string): Promise<void> {
  await fetch(withWindow('/chat/cancel', windowId), { method: 'POST', credentials: 'include' })
}

export async function respond(windowId: string, body: Record<string, unknown>): Promise<void> {
  const res = await fetch(withWindow('/chat/respond', windowId), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(`respond failed (${res.status})`)
}

export function openEventStream(windowId: string): EventSource {
  return new EventSource(withWindow('/chat/events', windowId), { withCredentials: true })
}

export async function improvePrompt(
  windowId: string,
  text: string,
  requestId: string,
  signal?: AbortSignal,
): Promise<ImprovePromptResponse> {
  return readJson<ImprovePromptResponse>(
    await fetch(withWindow('/chat/improve-prompt', windowId), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ draft: text, requestId }),
      signal,
    }),
  )
}

export async function setApprovalMode(
  windowId: string,
  mode: 'AUTO_APPROVE' | 'MANUAL',
): Promise<void> {
  const res = await fetch(withWindow('/chat/approval-mode', windowId), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ mode }),
  })
  if (!res.ok) throw new Error(`approval-mode failed (${res.status})`)
}

export async function cancelPromptImprovement(windowId: string, requestId: string): Promise<void> {
  await fetch(withWindow('/chat/improve-prompt/cancel', windowId), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ requestId }),
  })
}

export async function listSubagents(windowId: string): Promise<SubagentActivity[]> {
  const data = await readJson<{ subagents?: SubagentActivity[] }>(
    await fetch(withWindow('/chat/subagents', windowId), { credentials: 'include' }),
  )
  return data.subagents ?? []
}

export async function cancelSubagent(windowId: string, id: string): Promise<void> {
  const res = await fetch(withWindow(`/chat/subagents/${encodeURIComponent(id)}`, windowId), {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!res.ok) throw new Error(`cancel subagent failed (${res.status})`)
}

export async function loadSubagentTranscript(windowId: string, id: string): Promise<ChatMessage[]> {
  const detail = await readJson<SessionDetail>(
    await fetch(withWindow(`/chat/subagents/${encodeURIComponent(id)}/transcript`, windowId), {
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
  textData?: { text: string }
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
      }
    }
  }
  return msgs
}

export function autoTitle(text: string): string {
  const trimmed = text.trim().replace(/\s+/g, ' ')
  return trimmed.length > 40 ? `${trimmed.slice(0, 40)}…` : trimmed || 'New chat'
}
