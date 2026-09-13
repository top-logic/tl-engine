import { React } from 'tl-react-bridge'
import type { ChatMessage, SubagentActivity } from './types'

const { useEffect, useMemo, useState } = React

function formatDuration(ms: number): string {
  if (ms <= 0) return ''
  if (ms < 1000) return `${ms}ms`
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`
  return `${Math.floor(ms / 60_000)}m ${Math.round((ms % 60_000) / 1000)}s`
}

function formatTokens(tokens: number): string {
  if (tokens < 1000) return tokens ? String(tokens) : ''
  return `${(tokens / 1000).toFixed(tokens < 10_000 ? 1 : 0)}k tokens`
}

function transcriptLine(message: ChatMessage): string | null {
  switch (message.type) {
    case 'user':
      return `You: ${message.text}`
    case 'assistant_text':
      return `Agent: ${message.text}`
    case 'tool_call':
      return `Tool: ${message.name}`
    case 'error':
      return `Error: ${message.message}`
    default:
      return null
  }
}

export const SubagentActivityPanel = React.memo(function SubagentActivityPanel({
  subagents,
  onCancel,
  onStopAll,
  onLoadTranscript,
}: {
  subagents: SubagentActivity[]
  onCancel: (id: string) => Promise<void>
  onStopAll: () => Promise<void>
  onLoadTranscript: (id: string) => Promise<ChatMessage[]>
}) {
  const [open, setOpen] = useState(false)
  const [expanded, setExpanded] = useState<Set<string>>(() => new Set())
  const [transcripts, setTranscripts] = useState<Record<string, ChatMessage[]>>({})
  const [loadingTranscript, setLoadingTranscript] = useState<string | null>(null)
  const [error, setError] = useState('')

  const activeCount = useMemo(
    () => subagents.filter((item) => item.status === 'RUNNING' || item.status === 'QUEUED').length,
    [subagents],
  )
  const ordered = useMemo(
    () =>
      [...subagents].sort((left, right) => {
        const leftActive = left.status === 'RUNNING' || left.status === 'QUEUED'
        const rightActive = right.status === 'RUNNING' || right.status === 'QUEUED'
        return Number(rightActive) - Number(leftActive)
      }),
    [subagents],
  )

  useEffect(() => {
    if (!open) return
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [open])

  if (subagents.length === 0) return null

  const toggleDetails = (id: string) => {
    setExpanded((current) => {
      const next = new Set(current)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  const loadTranscript = async (id: string) => {
    if (transcripts[id]) return
    setLoadingTranscript(id)
    setError('')
    try {
      const messages = await onLoadTranscript(id)
      setTranscripts((current) => ({ ...current, [id]: messages }))
      setExpanded((current) => new Set(current).add(id))
    } catch {
      setError('The subagent transcript could not be loaded.')
    } finally {
      setLoadingTranscript(null)
    }
  }

  return (
    <div className="subagent-activity">
      <button
        type="button"
        className="overlay-chip working-chip"
        aria-expanded={open}
        aria-controls="subagent-activity-panel"
        onClick={() => setOpen((value) => !value)}
      >
        <span className={`status-spinner${activeCount ? ' is-active' : ''}`} aria-hidden="true" />
        {activeCount ? `Working · ${activeCount}` : `Tasks · ${subagents.length}`}
        <span aria-hidden="true">{open ? '⌃' : '⌄'}</span>
      </button>

      <span className="sr-only" aria-live="polite">
        {activeCount ? `${activeCount} subagents working` : 'All subagents finished'}
      </span>

      {open ? (
        <section id="subagent-activity-panel" className="activity-panel" aria-label="Subagent activity">
          <header className="activity-header">
            <strong>{activeCount ? 'Working' : 'Tasks'}</strong>
            <div>
              {activeCount ? (
                <button
                  type="button"
                  className="text-button"
                  onClick={() => {
                    setError('')
                    void onStopAll().catch(() => setError('The subagents could not be stopped.'))
                  }}
                >
                  Stop all
                </button>
              ) : null}
              <button type="button" className="icon-button" aria-label="Close activity" onClick={() => setOpen(false)}>
                ×
              </button>
            </div>
          </header>

          <div className="activity-list">
            {ordered.map((item) => {
              const isActive = item.status === 'RUNNING' || item.status === 'QUEUED'
              const isExpanded = expanded.has(item.subagentId)
              const transcript = transcripts[item.subagentId]
              return (
                <article className={`activity-item status-${item.status.toLocaleLowerCase()}`} key={item.subagentId}>
                  <div className="activity-summary">
                    <span className={`activity-status${isActive ? ' is-active' : ''}`} aria-hidden="true" />
                    <button
                      type="button"
                      className="activity-disclosure"
                      aria-expanded={isExpanded}
                      onClick={() => toggleDetails(item.subagentId)}
                    >
                      <strong>{item.detail || item.targetAgentName}</strong>
                      <span>
                        {item.targetAgentName}
                        {item.durationMs ? ` · ${formatDuration(item.durationMs)}` : ''}
                        {item.totalTokens ? ` · ${formatTokens(item.totalTokens)}` : ''}
                      </span>
                    </button>
                    {isActive ? (
                      <button
                        type="button"
                        className="text-button"
                        onClick={() => void onCancel(item.subagentId).catch(() => setError('The subagent could not be stopped.'))}
                      >
                        Stop
                      </button>
                    ) : null}
                  </div>

                  {isExpanded ? (
                    <div className="activity-detail">
                      <p>{item.detail || 'No details available.'}</p>
                      {item.hasTranscript ? (
                        <button
                          type="button"
                          className="text-button"
                          disabled={loadingTranscript === item.subagentId}
                          onClick={() => void loadTranscript(item.subagentId)}
                        >
                          {loadingTranscript === item.subagentId ? 'Loading…' : 'View transcript'}
                        </button>
                      ) : null}
                      {transcript ? (
                        <div className="activity-transcript">
                          {transcript.map(transcriptLine).filter(Boolean).map((line, index) => (
                            <p key={`${item.subagentId}-${index}`}>{line}</p>
                          ))}
                        </div>
                      ) : null}
                    </div>
                  ) : null}
                </article>
              )
            })}
          </div>
          {error ? <p className="activity-error" role="alert">{error}</p> : null}
        </section>
      ) : null}
    </div>
  )
})
