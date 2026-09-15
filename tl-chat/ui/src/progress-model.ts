/**
 * How far one agent task has got.
 *
 * `waiting` is "not started yet"; `approval` is "started and now waiting for the user to allow a
 * tool call". The two look different in the rail because they mean different things to the reader:
 * the second one is waiting for *them*.
 */
export type ProgressStatus = 'done' | 'running' | 'approval' | 'waiting' | 'error'

/** One row of the Fortschritt checklist. */
export interface ProgressStep {
  id: string
  label: string
  status: ProgressStatus
  /** Caption on the right of the row, e.g. the tool an approval is open for. */
  note?: string
}

/**
 * A row from the agent's task store (`createTask` / `updateTask`), as serialized in
 * {@code tasksJson}.
 */
export interface AgentTask {
  id: string
  subject?: string
  description?: string
  activeForm?: string
  status?: string
}

/**
 * Reads the task list out of a {@code task_update} payload.
 *
 * The server sends the whole store; deleted rows are dropped here so they never reach the list.
 */
export function parseTasksJson(raw: string | undefined): AgentTask[] {
  if (!raw) return []
  try {
    const data = JSON.parse(raw) as { tasks?: AgentTask[] }
    if (!Array.isArray(data.tasks)) return []
    return data.tasks.filter((task) => (task.status ?? '') !== 'deleted')
  } catch {
    return []
  }
}

/** A tool call the run is waiting for the user to approve. */
export interface PendingApproval {
  id: string
  toolName: string
}

/**
 * The steps of the current (or last) agent run, derived from the task store.
 *
 * Subjects are the labels. While a task is in progress, {@code activeForm} is preferred when the
 * agent set one.
 *
 * An open approval belongs to whatever the agent is doing right now, so it is shown on the running
 * step. A run that opened an approval before it wrote any task still has something to report, so
 * that case becomes a step of its own rather than "no tasks yet".
 */
export function progressSteps(tasks: AgentTask[], approvals: PendingApproval[] = []): ProgressStep[] {
  const waitingFor = approvals.map((approval) => approval.toolName).filter(Boolean).join(', ')
  const steps: ProgressStep[] = tasks.map((task) => ({
    id: task.id,
    label: stepLabel(task),
    status: stepStatus(task.status),
  }))
  if (approvals.length === 0) return steps
  const running = steps.findIndex((step) => step.status === 'running')
  if (running >= 0) {
    steps[running] = { ...steps[running], status: 'approval', note: waitingFor }
    return steps
  }
  return [...steps, { id: 'approval', label: waitingFor, status: 'approval' }]
}

function stepLabel(task: AgentTask): string {
  const active = task.activeForm?.trim() ?? ''
  if (task.status === 'in_progress' && active) return active
  const subject = task.subject?.trim() ?? ''
  return subject || task.id
}

function stepStatus(status: string | undefined): ProgressStatus {
  if (status === 'completed') return 'done'
  if (status === 'pending') return 'waiting'
  return 'running'
}

/** Count that feeds the Leiste badge: steps still pending, in progress or awaiting approval. */
export function progressBadge(tasks: AgentTask[], approvals: PendingApproval[] = []): number {
  return progressSteps(tasks, approvals).filter(
    (step) => step.status === 'running' || step.status === 'waiting' || step.status === 'approval',
  ).length
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
