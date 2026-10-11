import { React } from 'tl-react-bridge'
import { formatTokens } from './context-usage'
import { plainText } from './plain-text'
import type { bundle } from './i18n'
import { isEnded, type ChatMessage, type SubagentActivity, type SubagentBudget, type SubagentLive } from './types'

const { useEffect, useMemo, useState } = React

type Labels = ReturnType<typeof bundle>

/** A run's duration, as both the activity panel and a subagent's card show it. */
export function formatDuration(ms: number): string {
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

/**
 * What the subagents of the chat spent together, for the panel's header (#783): tokens - live while
 * they work -, money where their runs were priced, and the share of the budget they spend from.
 * Empty while there is nothing to sum.
 */
export function subagentCosts(
  subagents: SubagentActivity[],
  live: Record<string, SubagentLive>,
  budget: SubagentBudget | undefined,
  labels: Labels,
  language: string,
): string {
  let tokens = 0
  let money = 0
  let currency = ''
  for (const item of subagents) {
    const watched = live[item.subagentId]
    tokens += item.totalTokens || watched?.tokens || 0
    const cost = item.cost ?? watched?.cost
    const costCurrency = item.currency || watched?.currency || ''
    if (cost !== undefined && costCurrency) {
      money += cost
      currency = currency || costCurrency
    }
  }
  const parts: string[] = []
  if (tokens > 0) parts.push(`${formatTokens(tokens)} ${labels.tokens}`)
  if (currency && money > 0) parts.push(formatMoney(money, currency, language))
  const share =
    budget && budget.money > 0 && currency && budget.currency === currency
      ? money / budget.money
      : budget && budget.tokens > 0
        ? tokens / budget.tokens
        : undefined
  if (share !== undefined && (tokens > 0 || money > 0)) parts.push(labels.subagentsBudgetShare(percent(share)))
  return parts.join(' · ')
}

function formatMoney(amount: number, currency: string, language: string): string {
  try {
    // A short run costs less than a cent: then four places, otherwise the currency's own.
    return new Intl.NumberFormat(language, {
      style: 'currency',
      currency,
      maximumFractionDigits: amount < 0.1 ? 4 : 2,
    }).format(amount)
  } catch {
    return `${amount.toFixed(4)} ${currency}`
  }
}

function percent(share: number): string {
  const value = share * 100
  if (value > 0 && value < 1) return '< 1'
  return String(Math.round(value))
}

export const SubagentActivityPanel = React.memo(function SubagentActivityPanel({
  subagents,
  labels,
  onCancel,
  onStopAll,
  onLoadTranscript,
  waiting,
  live = {},
  budget,
  language = 'de',
  onOpen,
}: {
  subagents: SubagentActivity[]
  labels: Labels
  onCancel: (id: string, parentToolCallId?: string) => Promise<void>
  onStopAll: () => Promise<void>
  onLoadTranscript: (id: string) => Promise<ChatMessage[]>
  /** The subagents whose question or approval is open; listed first. */
  waiting?: Set<string>
  /** What each subagent did live - its tokens and cost while the list does not know them yet. */
  live?: Record<string, SubagentLive>
  /** The budget the subagents spend from, as the last list said. */
  budget?: SubagentBudget
  language?: string
  /** Called when the panel opens: the list is read anew for the costs and the budget. */
  onOpen?: () => void
}) {
  const [open, setOpen] = useState(false)
  const [expanded, setExpanded] = useState<Set<string>>(() => new Set())
  const [transcripts, setTranscripts] = useState<Record<string, ChatMessage[]>>({})
  const [loadingTranscript, setLoadingTranscript] = useState<string | null>(null)
  const [error, setError] = useState('')

  const activeCount = useMemo(
    () => subagents.filter((item) => !isEnded(item.status)).length,
    [subagents],
  )
  // What needs the user first, then what works, then what has yet to start, then what has ended (#778).
  const ordered = useMemo(() => {
    const rank = (item: SubagentActivity) =>
      item.status === 'RUNNING' && waiting?.has(item.subagentId) ? 0
        : item.status === 'RUNNING' ? 1
          : item.status === 'QUEUED' ? 2
            : 3
    return [...subagents].sort((left, right) => rank(left) - rank(right))
  }, [subagents, waiting])

  useEffect(() => {
    if (!open) return
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [open])

  if (subagents.length === 0) return null
  const costs = subagentCosts(subagents, live, budget, labels, language)

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
        onClick={() =>
          setOpen((value) => {
            if (!value) onOpen?.()
            return !value
          })
        }
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
            <div className="activity-title">
              <strong>{activeCount ? labels.working_plural : labels.tasks}</strong>
              {costs ? <span className="activity-cost">{costs}</span> : null}
            </div>
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
              const isActive = !isEnded(item.status)
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
                      <strong>{plainText(item.detail) || item.targetAgentName}</strong>
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
                        onClick={() =>
                          void onCancel(item.subagentId, item.parentToolCallId).catch(() => setError(labels.subagentStopFailed))
                        }
                      >
                        {labels.stop}
                      </button>
                    ) : null}
                  </div>

                  {isExpanded ? (
                    <div className="activity-detail">
                      <p>{plainText(item.detail) || labels.noDetails}</p>
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
