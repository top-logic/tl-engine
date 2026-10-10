import { React } from 'tl-react-bridge'
import { formatTokens } from './context-usage'
import type { bundle } from './i18n'
import { transcriptLine } from './subagents'
import type { ChatMessage, SubagentActivity, SubagentLive, SubagentRef, SubagentStatus, SubagentStep } from './types'

const { useState } = React

type Labels = ReturnType<typeof bundle>

/** What a card shows: the state of a running job, the record of an ended one, or both. */
export interface SubagentCardModel {
  subagentId: string
  agentName: string
  task: string
  /** `WAITING` while the subagent waits for the user's answer or approval. */
  status: SubagentStatus | 'WAITING'
  /** How it ended - its report or its error - once it has. */
  summary: string
  live?: SubagentLive
  totalTokens: number
  durationMs: number
  /** The run its recorded part names, for a subagent the server holds no job for any more. */
  childRunId?: string
  hasTranscript: boolean
}

/**
 * The subagents a turn started, in the order of the calls that started them.
 *
 * A job the server holds is matched to the turn by the id of the tool call that started it; a
 * recorded part stands in for one the server no longer holds - after a restart, or for a chat this
 * window has not held since. A part and a job are the same subagent only when both the subagent id
 * and the starting call agree, because subagent ids start again at `sa-1` in a new server life.
 *
 * @param waiting The ids of the subagents a question or an approval is open for.
 */
export function cardsForTurn(
  events: ChatMessage[],
  subagents: SubagentActivity[],
  live: Record<string, SubagentLive>,
  waiting: Set<string>,
): SubagentCardModel[] {
  const callOrder = new Map<string, number>()
  events.forEach((event, index) => {
    if (event.type === 'tool_call' && !callOrder.has(event.toolUseId)) callOrder.set(event.toolUseId, index)
  })
  const refs = events.filter((event): event is SubagentRef => event.type === 'subagent_ref')
  const cards: { order: number; card: SubagentCardModel }[] = []
  for (const job of subagents) {
    const parent = job.parentToolCallId
    if (!parent || !callOrder.has(parent)) continue
    const ref = refs.find((item) => item.subagentId === job.subagentId && item.parentToolCallId === parent)
    const ended = job.status === 'DONE' || job.status === 'FAILED' || job.status === 'CANCELLED'
    cards.push({
      order: callOrder.get(parent) ?? 0,
      card: {
        subagentId: job.subagentId,
        agentName: job.targetAgentName,
        task: job.task || ref?.task || '',
        status: job.status === 'RUNNING' && waiting.has(job.subagentId) ? 'WAITING' : job.status,
        summary: ended ? job.detail : '',
        live: live[job.subagentId],
        totalTokens: job.totalTokens,
        durationMs: job.durationMs,
        childRunId: ref?.childRunId || undefined,
        hasTranscript: job.hasTranscript,
      },
    })
  }
  for (const ref of refs) {
    const held = subagents.some((job) => job.subagentId === ref.subagentId && job.parentToolCallId === ref.parentToolCallId)
    if (held) continue
    cards.push({
      order: callOrder.get(ref.parentToolCallId) ?? events.indexOf(ref),
      card: {
        subagentId: ref.subagentId,
        agentName: ref.agentName,
        task: ref.task,
        status: ref.status,
        summary: ref.summary,
        totalTokens: 0,
        durationMs: 0,
        childRunId: ref.childRunId || undefined,
        hasTranscript: Boolean(ref.childRunId),
      },
    })
  }
  return cards.sort((left, right) => left.order - right.order).map((item) => item.card)
}

function statusLabel(status: SubagentCardModel['status'], labels: Labels): string {
  switch (status) {
    case 'QUEUED':
      return labels.subagentQueued
    case 'RUNNING':
      return labels.subagentRunning
    case 'WAITING':
      return labels.subagentWaiting
    case 'DONE':
      return labels.subagentDone
    case 'FAILED':
      return labels.subagentFailed
    case 'CANCELLED':
      return labels.subagentCancelled
  }
}

function formatSeconds(ms: number): string {
  if (ms <= 0) return ''
  const seconds = Math.round(ms / 1000)
  return seconds < 60 ? `${seconds}s` : `${Math.floor(seconds / 60)}m ${seconds % 60}s`
}

function stepLine(step: SubagentStep): string {
  return step.summary ? `${step.name} ${step.summary}` : step.name
}

/**
 * One background subagent, at the tool call of the turn that started it.
 *
 * Collapsed: who it is, what it was asked, how it stands and its current step. Expanded: its recent
 * steps, the text it is writing, and its transcript on request. Everything the subagent wrote is
 * shown as plain text, never as markup: it is model output that may repeat what the subagent read.
 */
export function SubagentCard({
  card,
  labels,
  onLoadTranscript,
}: {
  card: SubagentCardModel
  labels: Labels
  onLoadTranscript: (id: string, runId?: string) => Promise<ChatMessage[]>
}) {
  const [open, setOpen] = useState(false)
  const [transcript, setTranscript] = useState<ChatMessage[] | null>(null)
  const [loading, setLoading] = useState(false)
  const [failed, setFailed] = useState(false)
  const live = card.live
  const current = live?.steps[live.steps.length - 1]
  const active = card.status === 'RUNNING' || card.status === 'QUEUED' || card.status === 'WAITING'
  const tokens = card.totalTokens || live?.tokens || 0
  const elapsed = card.durationMs || (active && live ? Date.now() - live.startedAt : 0)
  const meta = [
    live?.turns ? labels.subagentTurns(live.turns) : '',
    tokens ? `${formatTokens(tokens)} ${labels.tokens}` : '',
    formatSeconds(elapsed),
  ].filter(Boolean)

  const loadTranscript = async () => {
    setLoading(true)
    setFailed(false)
    try {
      setTranscript(await onLoadTranscript(card.subagentId, card.childRunId))
    } catch {
      setFailed(true)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className={`subagent-card status-${card.status.toLowerCase()}`} aria-label={card.agentName}>
      <button type="button" className="subagent-card-head" aria-expanded={open} onClick={() => setOpen((value) => !value)}>
        <span className={`subagent-chip${active ? ' is-active' : ''}`}>{statusLabel(card.status, labels)}</span>
        <strong className="subagent-card-agent">{card.agentName}</strong>
        <span className="subagent-card-task">{card.task}</span>
        <span className="subagent-card-toggle" aria-hidden="true">{open ? '⌃' : '⌄'}</span>
      </button>
      <div className="subagent-card-line">
        {active && current ? <span className="subagent-card-step">{stepLine(current)}</span> : null}
        {!active && card.summary ? <span className="subagent-card-step">{card.summary}</span> : null}
        {meta.length > 0 ? <span className="subagent-card-meta">{meta.join(' · ')}</span> : null}
      </div>
      {open ? (
        <div className="subagent-card-body">
          {live && live.steps.length > 0 ? (
            <ol className="subagent-card-steps">
              {live.steps.map((step) => (
                <li key={step.toolUseId} className={`is-${step.status}`}>
                  {stepLine(step)}
                </li>
              ))}
            </ol>
          ) : (
            <p className="subagent-card-empty">{active ? labels.subagentNoSteps : labels.noDetails}</p>
          )}
          {live?.text ? <p className="subagent-card-text">{live.text}</p> : null}
          {card.hasTranscript ? (
            <button type="button" className="text-button" disabled={loading} onClick={() => void loadTranscript()}>
              {loading ? labels.loading : labels.viewTranscript}
            </button>
          ) : null}
          {failed ? <p className="activity-error" role="alert">{labels.transcriptFailed}</p> : null}
          {transcript ? (
            <div className="activity-transcript">
              {transcript
                .map((message) => transcriptLine(message, labels))
                .filter(Boolean)
                .map((line, index) => (
                  <p key={`${card.subagentId}-${index}`}>{line}</p>
                ))}
            </div>
          ) : null}
        </div>
      ) : null}
    </section>
  )
}
