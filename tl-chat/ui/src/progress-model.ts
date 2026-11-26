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
