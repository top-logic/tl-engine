import { React } from 'tl-react-bridge'
import { formatTokens } from './context-usage'
import type { bundle } from './i18n'
import type { ChatMessage, SubagentActivity } from './types'

const { useEffect, useMemo, useState } = React

type Labels = ReturnType<typeof bundle>

function formatDuration(ms: number): string {
  if (ms <= 0) return ''
  if (ms < 1000) return `${ms}ms`
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`
  return `${Math.floor(ms / 60_000)}m ${Math.round((ms % 60_000) / 1000)}s`
}

export function transcriptLine(message: ChatMessage, labels: Labels): string | null {
  switch (message.type) {
    case 'user':
      return `${labels.you}: ${message.text}`
    case 'assistant_text':
      return `${labels.agentLabel}: ${message.text}`
    case 'tool_call':
      return `${labels.tool}: ${message.name}`
    case 'error':
      return `${labels.error}: ${message.message}`
    default:
      return null
  }
}

export const SubagentActivityPanel = React.memo(function SubagentActivityPanel({
  subagents,
  labels,
  onCancel,
  onStopAll,
  onLoadTranscript,
}: {
  subagents: SubagentActivity[]
  labels: Labels
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
      setError(labels.transcriptFailed)
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
        {activeCount ? `${labels.working_plural} · ${activeCount}` : `${labels.tasks} · ${subagents.length}`}
        <span aria-hidden="true">{open ? '⌃' : '⌄'}</span>
      </button>

      <span className="sr-only" aria-live="polite">
        {activeCount ? `${activeCount} × ${labels.working_plural}` : ''}
      </span>

      {open ? (
        <section id="subagent-activity-panel" className="activity-panel" aria-label={labels.tasks}>
          <header className="activity-header">
            <strong>{activeCount ? labels.working_plural : labels.tasks}</strong>
            <div>
              {activeCount ? (
                <button
                  type="button"
                  className="text-button"
                  onClick={() => {
                    setError('')
                    void onStopAll().catch(() => setError(labels.subagentsStopFailed))
                  }}
                >
                  {labels.stopAll}
                </button>
              ) : null}
              <button type="button" className="icon-button" aria-label={labels.closeActivity} onClick={() => setOpen(false)}>
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
                        {item.totalTokens ? ` · ${formatTokens(item.totalTokens)} ${labels.tokens}` : ''}
                      </span>
                    </button>
                    {isActive ? (
                      <button
                        type="button"
                        className="text-button"
                        onClick={() => void onCancel(item.subagentId).catch(() => setError(labels.subagentStopFailed))}
                      >
                        {labels.stop}
                      </button>
                    ) : null}
                  </div>

                  {isExpanded ? (
                    <div className="activity-detail">
                      <p>{item.detail || labels.noDetails}</p>
                      {item.hasTranscript ? (
                        <button
                          type="button"
                          className="text-button"
                          disabled={loadingTranscript === item.subagentId}
                          onClick={() => void loadTranscript(item.subagentId)}
                        >
                          {loadingTranscript === item.subagentId ? labels.loading : labels.viewTranscript}
                        </button>
                      ) : null}
                      {transcript ? (
                        <div className="activity-transcript">
                          {transcript.map((message) => transcriptLine(message, labels)).filter(Boolean).map((line, index) => (
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
