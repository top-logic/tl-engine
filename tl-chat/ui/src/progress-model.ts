import { toolLabel } from './models'
import type { ChatMessage, ToolCall, ToolCallStatus } from './types'

/** How far one step of the current run has got. */
export type ProgressStatus = 'done' | 'running' | 'waiting' | 'error'

/** One row of the Fortschritt checklist. */
export interface ProgressStep {
  id: string
  toolUseId: string
  name: string
  label: string
  status: ProgressStatus
}

/**
 * The steps of the current (or last) agent run, derived from tool and approval events.
 *
 * The list is the tools that ran after the last user message. A pending approval for the same
 * tool call is "waiting"; a completed call is "done"; a running call is "running".
 */
export function progressSteps(messages: ChatMessage[]): ProgressStep[] {
  let from = 0
  for (let i = messages.length - 1; i >= 0; i--) {
    if (messages[i].type === 'user') {
      from = i + 1
      break
    }
  }
  const slice = messages.slice(from)
  const waiting = new Set(
    slice
      .filter((item) => item.type === 'approval_request' && item.outcome === 'pending')
      .map((item) => (item.type === 'approval_request' ? item.toolCallId : '')),
  )
  const tools = slice.filter((item): item is ToolCall => item.type === 'tool_call')
  return tools.map((tool) => ({
    id: tool.id,
    toolUseId: tool.toolUseId,
    name: tool.name,
    label: toolLabel(tool.name),
    status: stepStatus(tool.status, waiting.has(tool.toolUseId)),
  }))
}

function stepStatus(status: ToolCallStatus, waitingForApproval: boolean): ProgressStatus {
  if (waitingForApproval) return 'waiting'
  if (status === 'done') return 'done'
  if (status === 'error') return 'error'
  return 'running'
}

/** Count that feeds the Leiste badge: steps still running or waiting for approval. */
export function progressBadge(messages: ChatMessage[]): number {
  return progressSteps(messages).filter((step) => step.status === 'running' || step.status === 'waiting').length
}

/**
 * Unseen count shown on the Leiste toggle.
 *
 * Each section reports its current badge; the toggle shows the sum minus the total last stored
 * when the rail was opened. Never negative.
 */
export function unseenBadge(currentTotal: number, lastSeen: number): number {
  return Math.max(0, currentTotal - lastSeen)
}
