/** How far one agent task has got. */
export type ProgressStatus = 'done' | 'running' | 'waiting' | 'error'

/** One row of the Fortschritt checklist. */
export interface ProgressStep {
  id: string
  label: string
  status: ProgressStatus
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

/**
 * The steps of the current (or last) agent run, derived from the task store.
 *
 * Subjects are the labels. While a task is in progress, {@code activeForm} is preferred when the
 * agent set one.
 */
export function progressSteps(tasks: AgentTask[]): ProgressStep[] {
  return tasks.map((task) => ({
    id: task.id,
    label: stepLabel(task),
    status: stepStatus(task.status),
  }))
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

/** Count that feeds the Leiste badge: tasks still pending or in progress. */
export function progressBadge(tasks: AgentTask[]): number {
  return progressSteps(tasks).filter((step) => step.status === 'running' || step.status === 'waiting').length
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
