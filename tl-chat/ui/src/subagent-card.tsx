import { React } from 'tl-react-bridge'
import type { SubagentMessageStatus } from './api'
import { formatTokens } from './context-usage'
import { plainText } from './plain-text'
import type { bundle } from './i18n'
import { transcriptLine } from './subagents'
import type {
  ChatMessage,
  DelegationRef,
  SubagentActivity,
  SubagentLive,
  SubagentNote,
  SubagentRef,
  SubagentStatus,
  SubagentStep,
} from './types'

const { useEffect, useRef, useState } = React

type Labels = ReturnType<typeof bundle>

/** What a card shows: the state of a running job, the record of an ended one, or both. */
export interface SubagentCardModel {
  subagentId: string
  agentName: string
  task: string
  /**
   * `WAITING` while the subagent waits for the user's answer or approval, `WAITING_LEAD` while it
   * waits for its lead's reply (`ask_lead`).
   */
  status: SubagentStatus | 'WAITING' | 'WAITING_LEAD'
  /** How it ended - its report or its error - once it has. */
  summary: string
  live?: SubagentLive
  totalTokens: number
  durationMs: number
  /** The run its recorded part names, for a subagent the server holds no job for any more. */
  childRunId?: string
  hasTranscript: boolean
  /** The lead's call that started it; with the id, what a line pointing at the card finds it by. */
  parentToolCallId?: string
  /**
   * `delegation` for a synchronous `delegate_to_*` call: the lead waits for it, so it has no inbox
   * to write to and is stopped with the lead.
   */
  kind?: 'delegation'
}

/**
 * The subagents a turn started, in the order of the calls that started them.
 *
 * A job the server holds is matched to the turn by the id of the tool call that started it; a
 * recorded part stands in for one the server no longer holds - after a restart, or for a chat this
 * window has not held since. A part and a job are the same subagent only when both the subagent id
 * and the starting call agree, because subagent ids start again at `sa-1` in a new server life.
 *
 * A synchronous delegation gets a card too, at its `delegate_to_*` call: from its live events while
 * this window watches it, from its recorded part after a reload.
 *
 * @param waiting The ids of the subagents a question or an approval is open for.
 * @param delegationLive What each synchronous delegation did live, by its `delegate_to_*` call.
 */
export function cardsForTurn(
  events: ChatMessage[],
  subagents: SubagentActivity[],
  live: Record<string, SubagentLive>,
  waiting: Set<string>,
  delegationLive: Record<string, SubagentLive> = {},
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
        agentName: job.targetAgentName || ref?.agentName || '',
        task: job.task || ref?.task || '',
        status: runningState(job.status, waiting.has(job.subagentId), live[job.subagentId]),
        summary: ended ? job.detail : '',
        live: live[job.subagentId],
        totalTokens: job.totalTokens,
        durationMs: job.durationMs,
        childRunId: ref?.childRunId || job.childRunId || undefined,
        hasTranscript: job.hasTranscript,
        parentToolCallId: parent,
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
        parentToolCallId: ref.parentToolCallId,
      },
    })
  }
  for (const { order, ...card } of delegationCards(events, delegationLive)) {
    cards.push({ order: callOrder.get(card.parentToolCallId ?? '') ?? order, card })
  }
  return cards.sort((left, right) => left.order - right.order).map((item) => item.card)
}

/** The tool a subagent asks its lead with; while its call runs, the subagent waits for the reply. */
const ASK_LEAD = 'ask_lead'

/** What a running subagent does: work, or wait - for the user, or for its lead. */
function runningState(status: SubagentStatus, waitsForUser: boolean, live: SubagentLive | undefined): SubagentCardModel['status'] {
  if (status !== 'RUNNING') return status
  if (waitsForUser) return 'WAITING'
  const current = live?.steps[live.steps.length - 1]
  return current?.name === ASK_LEAD && current.status === 'running' ? 'WAITING_LEAD' : status
}

/**
 * The subagents a question or an approval is open for - what they wait for the user to answer.
 *
 * @param messages The open requests, or a transcript holding them.
 */
export function subagentsWaitingForUser(messages: ChatMessage[]): Set<string> {
  const ids = new Set<string>()
  for (const message of messages) {
    const open =
      (message.type === 'interaction_request' && !message.answered) ||
      (message.type === 'approval_request' && message.outcome === 'pending')
    if (open && message.askedBy) ids.add(message.askedBy.subagentId)
  }
  return ids
}

/** The prefix of the tool a lead delegates to an agent with, synchronously. */
const DELEGATE_PREFIX = 'delegate_to_'

/**
 * The synchronous delegations of a turn: the recorded part where there is one, completed by what the
 * window saw live; a delegation this window watches before its part arrives from its live events
 * and its call alone.
 */
function delegationCards(
  events: ChatMessage[],
  delegationLive: Record<string, SubagentLive>,
): (SubagentCardModel & { order: number })[] {
  const refs = events.filter((event): event is DelegationRef => event.type === 'delegation_ref')
  const result: (SubagentCardModel & { order: number })[] = []
  const drawn = new Set<string>()
  for (const ref of refs) {
    const call = ref.parentToolCallId
    const watched = call ? delegationLive[call] : undefined
    if (call) drawn.add(call)
    result.push({
      order: events.indexOf(ref),
      subagentId: '',
      kind: 'delegation',
      agentName: ref.agentName,
      task: ref.task,
      status: ref.status === 'RUNNING' && watched?.ended ? watched.ended : ref.status,
      summary: ref.summary,
      // The recorded steps, unless this window saw more of them live.
      live: watched && watched.steps.length >= ref.steps.length
        ? watched
        : { steps: ref.steps, text: '', turns: ref.turns, tokens: ref.totalTokens, startedAt: ref.timestamp },
      totalTokens: ref.totalTokens,
      durationMs: ref.durationMs,
      childRunId: ref.childRunId || undefined,
      hasTranscript: Boolean(ref.childRunId),
      parentToolCallId: call,
    })
  }
  events.forEach((event, index) => {
    if (event.type !== 'tool_call' || drawn.has(event.toolUseId)) return
    const watched = delegationLive[event.toolUseId]
    if (!watched && !event.name.startsWith(DELEGATE_PREFIX)) return
    drawn.add(event.toolUseId)
    const ended: SubagentStatus | undefined =
      watched?.ended ?? (event.status === 'done' ? 'DONE' : event.status === 'error' ? 'FAILED' : undefined)
    const input = event.input as { task?: unknown } | null
    result.push({
      order: index,
      subagentId: '',
      kind: 'delegation',
      agentName: event.name.startsWith(DELEGATE_PREFIX) ? event.name.slice(DELEGATE_PREFIX.length) : event.name,
      task: typeof input?.task === 'string' ? input.task : '',
      status: ended ?? 'RUNNING',
      summary: ended ? watched?.text ?? '' : '',
      live: watched,
      totalTokens: 0,
      durationMs: event.durationMs ?? 0,
      hasTranscript: false,
      parentToolCallId: event.toolUseId,
    })
  })
  return result
}

function statusLabel(status: SubagentCardModel['status'], labels: Labels, leadName: string): string {
  switch (status) {
    case 'WAITING_LEAD':
      return labels.subagentWaitingLead(leadName || labels.subagentLeadFallback)
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

/** One step: the tool's id as code (#550), then the values it was called with, as plain text. */
function StepLine({ step }: { step: SubagentStep }) {
  return (
    <>
      <code className="tool-id">{step.name}</code>
      {step.summary ? <span className="subagent-step-args"> {step.summary}</span> : null}
    </>
  )
}

/** Re-renders every second while `running`, so that an elapsed time computed in render moves. */
function useSecondTick(running: boolean): void {
  const [, setTick] = useState(0)
  useEffect(() => {
    if (!running) return
    const timer = window.setInterval(() => setTick((tick) => tick + 1), 1000)
    return () => window.clearInterval(timer)
  }, [running])
}

/** Writes to a running subagent from its card; see `ChatActions.messageSubagent`. */
export type SendToSubagent = (id: string, parentToolCallId: string, text: string) => Promise<SubagentMessageStatus>

/**
 * What the user wrote to the subagent, each with its receipt, and - while it works - the field to
 * write more.
 *
 * The subagent reads a message at the start of its next turn, which may be after the tool call it is
 * busy with: the hint says so, and the receipt turns from "sent" to "read" when it has.
 */
function CardMessages({
  card,
  notes,
  active,
  labels,
  onSend,
}: {
  card: SubagentCardModel
  notes: SubagentNote[]
  active: boolean
  labels: Labels
  onSend?: SendToSubagent
}) {
  const [draft, setDraft] = useState('')
  const [sending, setSending] = useState(false)
  const [refusal, setRefusal] = useState('')
  const name = card.agentName ? `${card.subagentId} (${card.agentName})` : card.subagentId
  const canWrite = active && onSend !== undefined
  const hintId = `subagent-hint-${card.parentToolCallId ?? ''}-${card.subagentId}`

  const submit = async () => {
    const text = draft.trim()
    if (!text || !onSend || sending) return
    setSending(true)
    setRefusal('')
    try {
      const status = await onSend(card.subagentId, card.parentToolCallId ?? '', text)
      if (status === 'sent') setDraft('')
      else setRefusal(status === 'empty' ? '' : status === 'ended' || status === 'unknown' ? labels.subagentMessageEnded : labels.subagentMessageFailed)
    } catch {
      setRefusal(labels.subagentMessageFailed)
    } finally {
      setSending(false)
    }
  }

  if (notes.length === 0 && !canWrite) return null
  return (
    <div className="subagent-card-messages">
      {notes.length > 0 ? (
        <ul className="subagent-card-notes" aria-label={labels.subagentMessagesLabel}>
          {notes.map((note) => (
            <li key={note.id} className={note.read ? 'is-read' : 'is-sent'}>
              <span className="subagent-note-text">{note.text}</span>
              <span className="subagent-note-state">{note.read ? labels.subagentMessageRead : labels.subagentMessageSent}</span>
            </li>
          ))}
        </ul>
      ) : null}
      {canWrite ? (
        <form
          className="subagent-card-compose"
          onSubmit={(event) => {
            event.preventDefault()
            void submit()
          }}
        >
          <input
            type="text"
            className="subagent-card-input"
            value={draft}
            maxLength={8000}
            placeholder={labels.subagentMessagePlaceholder}
            aria-label={labels.subagentMessageLabel(name)}
            aria-describedby={hintId}
            onChange={(event) => setDraft(event.target.value)}
          />
          <button type="submit" className="prompt-continue" disabled={sending || !draft.trim()}>
            {labels.send}
          </button>
        </form>
      ) : null}
      {canWrite ? (
        <p className="subagent-card-hint" id={hintId}>
          {labels.subagentMessageHint}
        </p>
      ) : null}
      {refusal ? (
        <p className="subagent-card-refusal" role="alert">
          {refusal}
        </p>
      ) : null}
    </div>
  )
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
  onSendMessage,
  onCancel,
  leadName,
}: {
  card: SubagentCardModel
  labels: Labels
  /** What the agent the user talks to is called - who a subagent in `ask_lead` waits for. */
  leadName?: string
  onLoadTranscript: (id: string, runId?: string) => Promise<ChatMessage[]>
  /** Writes to the subagent; without it the card offers no field. */
  onSendMessage?: SendToSubagent
  /** Stops the subagent; without it the card offers no stop. */
  onCancel?: (id: string, parentToolCallId?: string) => Promise<void>
}) {
  const [open, setOpen] = useState(false)
  const [stopping, setStopping] = useState(false)
  const [stopFailed, setStopFailed] = useState(false)
  const [transcript, setTranscript] = useState<ChatMessage[] | null>(null)
  const [loading, setLoading] = useState(false)
  const [failed, setFailed] = useState(false)
  const live = card.live
  const current = live?.steps[live.steps.length - 1]
  const active =
    card.status === 'RUNNING' || card.status === 'QUEUED' || card.status === 'WAITING' || card.status === 'WAITING_LEAD'
  // A tool call can take a minute with no event; the seconds move on regardless.
  useSecondTick(active && live !== undefined && !card.durationMs)
  const tokens = card.totalTokens || live?.tokens || 0
  const elapsed = card.durationMs || (active && live ? Date.now() - live.startedAt : 0)
  const meta = [
    live?.turns ? labels.subagentTurns(live.turns) : '',
    tokens ? `${formatTokens(tokens)} ${labels.tokens}` : '',
    formatSeconds(elapsed),
  ].filter(Boolean)

  // A synchronous delegation ends with the lead's turn; the main Stop is its stop.
  const canStop = active && card.kind !== 'delegation' && onCancel !== undefined && card.subagentId !== ''
  const stop = async () => {
    if (!onCancel) return
    setStopping(true)
    setStopFailed(false)
    try {
      await onCancel(card.subagentId, card.parentToolCallId)
    } catch {
      setStopFailed(true)
    } finally {
      setStopping(false)
    }
  }

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
    <section
      className={`subagent-card status-${card.status.toLowerCase()}`}
      aria-label={card.agentName}
      data-subagent-id={card.subagentId}
      data-parent-call={card.parentToolCallId ?? ''}
    >
      <div className="subagent-card-top">
        <button type="button" className="subagent-card-head" aria-expanded={open} onClick={() => setOpen((value) => !value)}>
          <span className="subagent-card-title">
            <span className={`subagent-chip${active ? ' is-active' : ''}`}>{statusLabel(card.status, labels, leadName ?? '')}</span>
            <strong className="subagent-card-agent">{card.agentName}</strong>
            {card.kind === 'delegation' ? <span className="subagent-card-kind">{labels.delegationTag}</span> : null}
            <span className="subagent-card-toggle" aria-hidden="true">{open ? '⌃' : '⌄'}</span>
          </span>
          {card.task ? <span className="subagent-card-task">{card.task}</span> : null}
        </button>
        {canStop ? (
          <button
            type="button"
            className="text-button is-danger subagent-card-stop"
            disabled={stopping}
            aria-label={labels.subagentStopOne(card.agentName ? `${card.subagentId} (${card.agentName})` : card.subagentId)}
            onClick={() => void stop()}
          >
            <i className="bi bi-stop-circle subagent-card-stop-icon" aria-hidden="true" />
            <span className="subagent-card-stop-label">{labels.stop}</span>
          </button>
        ) : null}
      </div>
      {stopFailed ? <p className="subagent-card-refusal" role="alert">{labels.subagentStopFailed}</p> : null}
      <div className="subagent-card-line">
        {active && current ? (
          <span className="subagent-card-step">
            <StepLine step={current} />
          </span>
        ) : null}
        {!active && card.summary ? <span className="subagent-card-summary">{plainText(card.summary)}</span> : null}
        {meta.length > 0 ? <span className="subagent-card-meta">{meta.join(' · ')}</span> : null}
      </div>
      {open ? (
        <div className="subagent-card-body">
          {live && live.steps.length > 0 ? (
            <ol className="subagent-card-steps">
              {live.steps.map((step) => (
                <li key={step.toolUseId} className={`is-${step.status}`}>
                  <StepLine step={step} />
                </li>
              ))}
            </ol>
          ) : (
            <p className="subagent-card-empty">{active ? labels.subagentNoSteps : labels.noDetails}</p>
          )}
          {live?.text ? <p className="subagent-card-text">{plainText(live.text)}</p> : null}
          <CardMessages
            card={card}
            notes={live?.notes ?? []}
            active={active}
            labels={labels}
            onSend={card.kind === 'delegation' ? undefined : onSendMessage}
          />
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

/** From how many cards on a turn on the cards get a summary row and the ended ones are folded. */
export const FOLD_ABOVE = 3

/** The event a link pointing at a folded card sends it, so that its list unfolds first. */
export const REVEAL_CARD_EVENT = 'tl-subagent-reveal'

function ended(card: SubagentCardModel): boolean {
  return card.status === 'DONE' || card.status === 'FAILED' || card.status === 'CANCELLED'
}

/** "5 Subagenten: 1 wartet, 2 laufen, 2 fertig" - the states of a turn's cards, each counted once. */
export function cardSummary(cards: SubagentCardModel[], labels: Labels): string {
  const count = (test: (card: SubagentCardModel) => boolean) => cards.filter(test).length
  const parts: [number, (count: number) => string][] = [
    [count((card) => card.status === 'WAITING' || card.status === 'WAITING_LEAD'), labels.subagentsWaitingN],
    [count((card) => card.status === 'RUNNING'), labels.subagentsRunningN],
    [count((card) => card.status === 'QUEUED'), labels.subagentsQueuedN],
    [count((card) => card.status === 'DONE'), labels.subagentsDoneN],
    [count((card) => card.status === 'FAILED'), labels.subagentsFailedN],
    [count((card) => card.status === 'CANCELLED'), labels.subagentsCancelledN],
  ]
  const states = parts.filter(([n]) => n > 0).map(([n, text]) => text(n))
  return `${labels.subagentsCount(cards.length)}: ${states.join(', ')}`
}

/**
 * The cards of one turn. Up to {@link FOLD_ABOVE} they are all shown; with more, a summary row says
 * how they stand and the ended ones are folded behind it - never removed (handover 9.5) - so the
 * ones that still work or wait stand together.
 */
export function SubagentCards({
  cards,
  labels,
  onLoadTranscript,
  onSendMessage,
  onCancel,
  leadName,
}: {
  cards: SubagentCardModel[]
  labels: Labels
  onLoadTranscript: (id: string, runId?: string) => Promise<ChatMessage[]>
  onSendMessage?: SendToSubagent
  onCancel?: (id: string, parentToolCallId?: string) => Promise<void>
  leadName?: string
}) {
  const [folded, setFolded] = useState(true)
  const foldRef = useRef<HTMLDivElement>(null)
  const many = cards.length > FOLD_ABOVE
  const endedCards = many ? cards.filter(ended) : []
  const shown = many ? cards.filter((card) => !ended(card)) : cards

  // A link to a folded card (the woken line names an ended subagent) unfolds the list before it
  // scrolls: the attribute goes at once, so the card can be scrolled to in the same task.
  useEffect(() => {
    const node = foldRef.current
    if (!node) return
    const reveal = () => {
      node.hidden = false
      setFolded(false)
    }
    node.addEventListener(REVEAL_CARD_EVENT, reveal)
    return () => node.removeEventListener(REVEAL_CARD_EVENT, reveal)
  }, [many])

  const render = (card: SubagentCardModel) => (
    <SubagentCard
      key={`${card.parentToolCallId ?? ''}|${card.subagentId}`}
      card={card}
      labels={labels}
      onLoadTranscript={onLoadTranscript}
      onSendMessage={onSendMessage}
      onCancel={onCancel}
      leadName={leadName}
    />
  )

  return (
    <div className="subagent-cards">
      {many ? (
        <div className="subagent-cards-summary">
          <span>{cardSummary(cards, labels)}</span>
          {endedCards.length > 0 ? (
            <button type="button" className="text-button" aria-expanded={!folded} onClick={() => setFolded((value) => !value)}>
              {folded ? labels.subagentsShowEnded(endedCards.length) : labels.subagentsHideEnded}
            </button>
          ) : null}
        </div>
      ) : null}
      {shown.map(render)}
      {endedCards.length > 0 ? (
        <div className="subagent-cards-folded" ref={foldRef} hidden={folded}>
          {endedCards.map(render)}
        </div>
      ) : null}
    </div>
  )
}
