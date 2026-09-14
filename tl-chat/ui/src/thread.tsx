import { React } from 'tl-react-bridge'
import type { CitationRender, MessageAction, PaneOpenOptions } from './extensions'
import { Markdown } from './markdown'
import { bundle, formatTimestamp, type Language } from './i18n'
import { effortLabel } from './models'
import type {
  ApprovalRequest,
  AssistantText,
  ChatMessage,
  Citation,
  InteractionRequest,
  MessageFeedback,
  Rating,
  RunMeta,
  ToolCall,
  ToolResult,
  TraceMode,
  UserMessage,
} from './types'

const { useEffect, useMemo, useRef, useState } = React

function formatDuration(ms?: number): string {
  if (ms == null) return ''
  if (ms < 1000) return `${ms}ms`
  if (ms < 60_000) return `${Math.round(ms / 100) / 10}s`
  const m = Math.floor(ms / 60_000)
  const s = Math.round((ms % 60_000) / 1000)
  return s ? `${m}m ${s}s` : `${m}m`
}

function pretty(value: unknown): string {
  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      return value
    }
  }
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}

function skillIdFrom(call: ToolCall): string | null {
  if (!/^load[_-]?skill$/i.test(call.name)) return null
  let input: unknown = call.input
  if (typeof input === 'string') {
    const raw: string = input
    try {
      input = JSON.parse(raw)
    } catch {
      return raw.trim() || null
    }
  }
  if (input && typeof input === 'object') {
    const record = input as Record<string, unknown>
    const value = record.skillId ?? record.skill_id ?? record.skill ?? record.id
    return typeof value === 'string' && value.trim() ? value.trim() : null
  }
  return null
}

type Labels = ReturnType<typeof bundle>

interface RunTurn {
  id: string
  user?: UserMessage
  events: ChatMessage[]
  /**
   * Position of the turn in the conversation, which is how the server addresses it for editing,
   * regenerating and rating. Counted over user messages, so it matches the persisted turn order.
   */
  index: number
}

function groupTurns(messages: ChatMessage[]): RunTurn[] {
  const turns: RunTurn[] = []
  let index = -1
  let current: RunTurn = { id: 'run-0', events: [], index: -1 }

  const flush = () => {
    if (current.user || current.events.length) turns.push(current)
  }

  for (const message of messages) {
    if (message.type === 'user') {
      flush()
      index += 1
      current = { id: `run-${message.id}`, user: message, events: [], index }
      continue
    }
    if (message.type === 'tool_result') continue
    current.events.push(message)
  }
  flush()
  return turns
}

function lastAnswer(events: ChatMessage[]): AssistantText | null {
  for (let i = events.length - 1; i >= 0; i -= 1) {
    const item = events[i]
    if (item.type === 'assistant_text') return item
  }
  return null
}

function toolsOf(events: ChatMessage[]): ToolCall[] {
  return events.filter((item): item is ToolCall => item.type === 'tool_call')
}

function runMetaOf(events: ChatMessage[]): RunMeta | null {
  for (const item of events) {
    if (item.type === 'run_meta') return item
  }
  return null
}

function traceEvents(events: ChatMessage[]): ChatMessage[] {
  const answer = lastAnswer(events)
  return events.filter((item) => {
    if (item.type === 'run_meta') return false
    if (answer && item === answer) return false
    if (item.type === 'assistant_text' && !item.text.trim()) return false
    return true
  })
}

/**
 * One line naming what the run actually did.
 *
 * The tool names are the ones the agent really called - no invented categories such as "edited" or
 * "searched", which would need a mapping that is wrong the moment a tool is added. Sorted by how
 * often each was used, so the line opens with the work that dominated the run.
 */
function overviewLine(tools: ToolCall[], labels: Labels): string {
  if (tools.length === 0) return ''
  const counts = new Map<string, number>()
  for (const tool of tools) {
    counts.set(tool.name, (counts.get(tool.name) ?? 0) + 1)
  }
  const MAX_NAMED = 4
  const ordered = [...counts.entries()].sort((left, right) => right[1] - left[1])
  const named = ordered
    .slice(0, MAX_NAMED)
    .map(([name, count]) => (count > 1 ? `${name} ×${count}` : name))
  const rest = ordered.length - named.length
  if (rest > 0) named.push(`${rest} more`)

  const skills = [...new Set(tools.map(skillIdFrom).filter((id): id is string => Boolean(id)))]
  const body = `${labels.toolCalls(tools.length)}: ${named.join(', ')}`
  return skills.length ? `${body} · ${labels.skillLabel} ${skills.join(', ')}` : body
}

function IconButton({
  label,
  active,
  hint,
  onClick,
  children,
}: {
  label: string
  active?: boolean
  hint?: string
  onClick: () => void
  children: React.ReactNode
}) {
  return (
    <button
      type="button"
      className={`icon-button tiny${active ? ' is-on' : ''}`}
      aria-label={label}
      aria-pressed={active === undefined ? undefined : active}
      title={hint ?? label}
      onClick={onClick}
    >
      {children}
    </button>
  )
}

const CopyIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <rect x="5.5" y="5.5" width="8" height="9" rx="1.4" />
    <path d="M10.5 5.5V3.8A1.3 1.3 0 0 0 9.2 2.5H3.8A1.3 1.3 0 0 0 2.5 3.8v8.4A1.3 1.3 0 0 0 3.8 13.5H5.5" />
  </svg>
)

const CheckIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path d="M3.5 8.6 6.4 11.5 12.5 4.8" />
  </svg>
)

const ThumbUpIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path d="M5.5 14V7.2l3-4.7a1.2 1.2 0 0 1 2.2.8L10.2 6h2.6a1.2 1.2 0 0 1 1.2 1.4l-.9 5A1.6 1.6 0 0 1 11.6 14z" />
    <path d="M5.5 7.4H3.2A.7.7 0 0 0 2.5 8v5.3c0 .4.3.7.7.7h2.3" />
  </svg>
)

const ThumbDownIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path d="M5.5 2v6.8l3 4.7a1.2 1.2 0 0 0 2.2-.8L10.2 10h2.6a1.2 1.2 0 0 0 1.2-1.4l-.9-5A1.6 1.6 0 0 0 11.6 2z" />
    <path d="M5.5 8.6H3.2a.7.7 0 0 1-.7-.7V2.7c0-.4.3-.7.7-.7h2.3" />
  </svg>
)

const RetryIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path d="M13 8a5 5 0 1 1-1.6-3.7" />
    <path d="M13.3 2v3h-3" />
  </svg>
)

const EditIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path d="M11.2 2.8a1.4 1.4 0 0 1 2 2L6 12l-2.8.8L4 10z" />
  </svg>
)

/**
 * A message the user sent, editable in place.
 *
 * Editing happens in the bubble itself and changes nothing until it is sent: the text is the
 * user's to try out, and a cancelled edit leaves the conversation - including every answer that
 * came after it - exactly as it was. Only a send truncates the conversation and asks again.
 */
function UserTurn({
  text,
  edited,
  labels,
  editing,
  onStartEdit,
  onCancelEdit,
  onSubmitEdit,
}: {
  text: string
  edited?: boolean
  labels: Labels
  editing: boolean
  onStartEdit?: () => void
  onCancelEdit: () => void
  onSubmitEdit: (text: string) => void
}) {
  const [draft, setDraft] = useState(text)
  const area = useRef<HTMLTextAreaElement>(null)

  // The bubble grows with the text rather than scrolling inside itself: the user is reading their
  // own message, and a two-line window onto it would hide what they are about to change.
  useEffect(() => {
    const el = area.current
    if (!editing || !el) return
    el.style.height = 'auto'
    el.style.height = `${el.scrollHeight}px`
  }, [draft, editing])

  useEffect(() => {
    if (!editing) return
    setDraft(text)
    const el = area.current
    if (!el) return
    el.focus()
    el.setSelectionRange(text.length, text.length)
  }, [editing, text])

  if (editing) {
    const submit = () => {
      const next = draft.trim()
      if (!next) return
      onSubmitEdit(next)
    }
    return (
      <div className="turn turn-user">
        <div className="bubble-user is-editing">
          <textarea
            ref={area}
            className="bubble-user-edit"
            value={draft}
            aria-label={labels.editAndSendAgain}
            rows={1}
            onChange={(event) => setDraft(event.currentTarget.value)}
            onKeyDown={(event) => {
              if (event.key === 'Escape') {
                event.preventDefault()
                onCancelEdit()
              } else if (event.key === 'Enter' && (event.metaKey || event.ctrlKey)) {
                event.preventDefault()
                submit()
              }
            }}
          />
          <div className="bubble-user-edit-actions">
            <button type="button" className="text-button" onClick={onCancelEdit}>
              {labels.cancel}
            </button>
            <button type="button" className="prompt-continue" disabled={!draft.trim()} onClick={submit}>
              {labels.send}
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="turn turn-user">
      <div className="bubble-user">
        <div className="bubble-user-text">{text}</div>
        {edited ? <span className="bubble-user-edited">{labels.editedMarker}</span> : null}
        {onStartEdit ? (
          <span className="bubble-user-actions">
            <IconButton label={labels.editAndSendAgain} hint={labels.editAndSendAgain} onClick={onStartEdit}>
              {EditIcon}
            </IconButton>
          </span>
        ) : null}
      </div>
    </div>
  )
}

/**
 * Copy, a rating, a regenerate and the time, under a finished answer.
 *
 * The rating is written through to the server (`POST /chat/feedback`) and comes back with the
 * session, so it survives a reload and can be read back for evaluation. A thumbs-down opens a small
 * note field: what was wrong is worth far more than that something was.
 */
function AnswerFooter({
  text,
  timestamp,
  labels,
  language,
  rating,
  note,
  regenerating,
  onRate,
  onRegenerate,
  extraActions,
  message,
}: {
  text: string
  timestamp: number
  labels: Labels
  language: Language
  rating: Rating
  note?: string
  regenerating: boolean
  onRate?: (rating: Rating, note?: string) => void
  onRegenerate?: () => void
  extraActions?: MessageAction[]
  message?: ChatMessage
}) {
  const [copied, setCopied] = useState(false)
  const [noteOpen, setNoteOpen] = useState(false)
  const [draft, setDraft] = useState(note ?? '')
  if (!text.trim()) return null

  const rate = (next: Rating) => {
    if (!onRate) return
    const value = rating === next ? null : next
    onRate(value, value === 'down' ? draft || undefined : undefined)
    setNoteOpen(value === 'down')
  }

  return (
    <div className="answer-footer-wrap">
      <div className="answer-footer">
        <IconButton
          label={copied ? labels.copied : labels.copyAnswer}
          hint={copied ? labels.copied : labels.copyAnswer}
          onClick={() => {
            void navigator.clipboard
              .writeText(text)
              .then(() => {
                setCopied(true)
                window.setTimeout(() => setCopied(false), 1500)
              })
              .catch(() => {
                /* the clipboard may be blocked */
              })
          }}
        >
          {copied ? CheckIcon : CopyIcon}
        </IconButton>
        <IconButton
          label={labels.goodAnswer}
          hint={labels.goodAnswer}
          active={rating === 'up'}
          onClick={() => rate('up')}
        >
          {ThumbUpIcon}
        </IconButton>
        <IconButton
          label={labels.badAnswer}
          hint={labels.badAnswer}
          active={rating === 'down'}
          onClick={() => rate('down')}
        >
          {ThumbDownIcon}
        </IconButton>
        {onRegenerate ? (
          <IconButton
            label={regenerating ? labels.regenerating : labels.regenerate}
            hint={regenerating ? labels.regenerating : labels.regenerate}
            onClick={onRegenerate}
          >
            {RetryIcon}
          </IconButton>
        ) : null}
        {extraActions && message
          ? extraActions.map((action) => (
              <IconButton
                key={action.id}
                label={action.label}
                hint={action.label}
                onClick={() => action.action(message)}
              >
                {action.icon ?? action.label.slice(0, 1)}
              </IconButton>
            ))
          : null}
        <time className="answer-time" dateTime={new Date(timestamp).toISOString()}>
          {formatTimestamp(timestamp, language)}
        </time>
      </div>
      {noteOpen && rating === 'down' ? (
        <div className="feedback-note">
          <input
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            placeholder={labels.feedbackNotePlaceholder}
            autoFocus
            onKeyDown={(event) => {
              if (event.key !== 'Enter') return
              event.preventDefault()
              onRate?.('down', draft || undefined)
              setNoteOpen(false)
            }}
          />
          <button
            type="button"
            className="text-button"
            onClick={() => {
              onRate?.('down', draft || undefined)
              setNoteOpen(false)
            }}
          >
            {labels.feedbackNoteSave}
          </button>
          <button type="button" className="text-button" onClick={() => setNoteOpen(false)}>
            {labels.feedbackNoteSkip}
          </button>
        </div>
      ) : null}
    </div>
  )
}

function AssistantAnswer({
  text,
  streaming,
  timestamp,
  labels,
  language,
  rating,
  note,
  regenerating,
  onRate,
  onRegenerate,
  citations,
  message,
  renderAnnotation,
  openInPane,
  extraActions,
}: {
  text: string
  streaming: boolean
  timestamp: number
  labels: Labels
  language: Language
  rating: Rating
  note?: string
  regenerating: boolean
  onRate?: (rating: Rating, note?: string) => void
  onRegenerate?: () => void
  citations?: Citation[]
  message?: ChatMessage
  renderAnnotation?: CitationRender
  openInPane?: (content: React.ReactNode, options: PaneOpenOptions) => void
  extraActions?: MessageAction[]
}) {
  if (!text && !streaming) return null
  return (
    <div className="turn turn-agent">
      <div className={`answer${streaming ? ' is-streaming' : ''}`}>
        <Markdown
          text={text}
          streaming={streaming}
          labels={labels}
          citations={citations}
          message={message}
          renderAnnotation={renderAnnotation}
          openInPane={openInPane}
        />
      </div>
      {streaming ? null : (
        <AnswerFooter
          text={text}
          timestamp={timestamp}
          labels={labels}
          language={language}
          rating={rating}
          note={note}
          regenerating={regenerating}
          onRate={onRate}
          onRegenerate={onRegenerate}
          extraActions={extraActions}
          message={message}
        />
      )}
    </div>
  )
}

function TraceEntry({
  title,
  meta,
  preview,
  children,
  danger,
}: {
  title: string
  meta?: string
  preview?: string
  children?: React.ReactNode
  danger?: boolean
}) {
  const [open, setOpen] = useState(false)
  return (
    <div className={`trace-entry${danger ? ' is-error' : ''}`}>
      <button type="button" className="trace-entry-head" onClick={() => setOpen((value) => !value)}>
        <span className="trace-name">{title}</span>
        {preview && !open ? <span className="trace-preview">{preview}</span> : null}
        {meta ? <span className="trace-meta">{meta}</span> : null}
        <span className={`trace-chevron${open ? ' is-open' : ''}`} aria-hidden="true" />
      </button>
      {open ? <div className="trace-entry-body">{children}</div> : null}
    </div>
  )
}

function TraceBlock({
  label,
  text,
  danger,
  labels,
}: {
  label: string
  text: string
  danger?: boolean
  labels: Labels
}) {
  const [more, setMore] = useState(false)
  const limit = 2000
  const long = text.length > limit
  const shown = !long || more ? text : `${text.slice(0, limit)}\n…`
  if (!text) return null
  return (
    <div className={`trace-block${danger ? ' is-error' : ''}`}>
      <div className="trace-block-label">{label}</div>
      <pre>{shown}</pre>
      {long ? (
        <button type="button" className="trace-more" onClick={() => setMore((value) => !value)}>
          {more ? labels.showLess : labels.showMore(text.length - limit)}
        </button>
      ) : null}
    </div>
  )
}

function interactionPreview(item: InteractionRequest): string {
  return (item.header || item.question).replace(/\s+/g, ' ').slice(0, 80)
}

function approvalPreview(item: ApprovalRequest): string {
  return (item.displayReason || item.toolName).replace(/\s+/g, ' ').slice(0, 80)
}

function TraceEvent({
  item,
  results,
  labels,
}: {
  item: ChatMessage
  results: Map<string, ToolResult>
  labels: Labels
}) {
  switch (item.type) {
    case 'thinking':
      return (
        <TraceEntry title={labels.thinking} preview={item.content.replace(/\s+/g, ' ').slice(0, 80)}>
          <TraceBlock label={labels.thinking} text={item.content} labels={labels} />
        </TraceEntry>
      )
    case 'tool_call': {
      const result = results.get(item.toolUseId)
      return (
        <TraceEntry
          title={item.name}
          meta={item.status === 'running' ? labels.running : formatDuration(item.durationMs)}
          danger={item.status === 'error' || result?.isError}
        >
          <TraceBlock label={labels.argumentsLabel} text={pretty(item.input)} labels={labels} />
          {result ? (
            <TraceBlock
              label={result.isError ? labels.error : labels.result}
              text={result.output}
              danger={result.isError}
              labels={labels}
            />
          ) : item.status === 'running' ? (
            <TraceBlock label={labels.result} text={`${labels.running}…`} labels={labels} />
          ) : null}
        </TraceEntry>
      )
    }
    case 'assistant_text':
      return (
        <TraceEntry title={labels.output} preview={item.text.replace(/\s+/g, ' ').slice(0, 80)}>
          <div className="trace-block">
            <div className="trace-block-label">{labels.output}</div>
            <div className="trace-block-md">
              <Markdown text={item.text} labels={labels} />
            </div>
          </div>
        </TraceEntry>
      )
    case 'interaction_request':
      return (
        <TraceEntry
          title={labels.question}
          meta={item.answered ? labels.answered : labels.waiting}
          preview={interactionPreview(item)}
        >
          <TraceBlock label={labels.question} text={item.question} labels={labels} />
        </TraceEntry>
      )
    case 'approval_request':
      return (
        <TraceEntry
          title={labels.approval}
          meta={item.outcome === 'pending' ? labels.waiting : item.outcome.replace(/_/g, ' ').toLowerCase()}
          preview={approvalPreview(item)}
        >
          <TraceBlock label={labels.tool} text={item.toolName} labels={labels} />
          {item.displayReason ? <TraceBlock label={labels.reason} text={item.displayReason} labels={labels} /> : null}
        </TraceEntry>
      )
    case 'error':
      return (
        <TraceEntry title={labels.error} danger preview={item.message.replace(/\s+/g, ' ').slice(0, 80)}>
          <TraceBlock label={labels.error} text={item.message} danger labels={labels} />
        </TraceEntry>
      )
    default:
      return null
  }
}

function DetailedTrace({
  events,
  results,
  labels,
  effort,
  model,
}: {
  events: ChatMessage[]
  results: Map<string, ToolResult>
  labels: Labels
  effort: string
  model: string
}) {
  const [open, setOpen] = useState(false)
  const toolCount = events.filter((item) => item.type === 'tool_call').length
  const label = toolCount ? labels.toolsUsed(toolCount) : labels.stepsUsed(events.length)

  return (
    <div className="trace-panel">
      <button type="button" className="trace-panel-head" onClick={() => setOpen((value) => !value)}>
        <span>{labels.trace}</span>
        <span className="trace-panel-summary">{label}</span>
        {/* What the turn was actually run with, so a trace can be read without guessing. */}
        {effort ? <span className="trace-effort">{`${labels.effort}: ${effortLabel(effort, labels)}`}</span> : null}
        {model ? <span className="trace-effort">{model}</span> : null}
        <span className={`trace-chevron${open ? ' is-open' : ''}`} aria-hidden="true" />
      </button>
      {open ? (
        <div className="trace-panel-body">
          {events.map((item) => (
            <TraceEvent key={item.id} item={item} results={results} labels={labels} />
          ))}
        </div>
      ) : null}
    </div>
  )
}

/**
 * The quiet line between the question and the answer.
 *
 * Reserved even while the run is still going, so the answer does not jump down the moment the
 * duration becomes known.
 */
function RunMetaLine({
  meta,
  summary,
  busy,
  labels,
}: {
  meta: RunMeta | null
  summary: string
  busy: boolean
  labels: Labels
}) {
  const worked = meta?.durationMs != null ? labels.workedFor(formatDuration(meta.durationMs)) : ''
  const label = worked || (meta && busy ? labels.working : '')
  if (!label && !summary) return null
  return (
    <div className="run-meta">
      {label ? <span className={busy && !worked ? 'is-live' : undefined}>{label}</span> : null}
      {label && summary ? <span className="run-meta-dot" aria-hidden="true" /> : null}
      {summary ? <span className="run-meta-summary">{summary}</span> : null}
    </div>
  )
}

function AgentRun({
  turn,
  results,
  traceMode,
  busy,
  labels,
  language,
  effort,
  rating,
  note,
  regenerating,
  onRate,
  onRegenerate,
  renderAnnotation,
  openInPane,
  extraActions,
}: {
  turn: RunTurn
  results: Map<string, ToolResult>
  traceMode: TraceMode
  busy: boolean
  labels: Labels
  language: Language
  effort: string
  rating: Rating
  note?: string
  regenerating: boolean
  onRate?: (rating: Rating, note?: string) => void
  onRegenerate?: () => void
  renderAnnotation?: CitationRender
  openInPane?: (content: React.ReactNode, options: PaneOpenOptions) => void
  extraActions?: MessageAction[]
}) {
  const answer = lastAnswer(turn.events)
  const events = traceEvents(turn.events)
  const tools = toolsOf(turn.events)
  const meta = runMetaOf(turn.events)
  const summary = traceMode === 'overview' ? overviewLine(tools, labels) : ''

  return (
    <div className="agent-run">
      <RunMetaLine meta={meta} summary={summary} busy={busy} labels={labels} />
      {traceMode === 'detailed' && events.length > 0 ? (
        <DetailedTrace events={events} results={results} labels={labels} effort={effort} model={''} />
      ) : null}
      {answer ? (
        <AssistantAnswer
          text={answer.text}
          streaming={answer.isStreaming}
          timestamp={answer.timestamp}
          labels={labels}
          language={language}
          rating={rating}
          note={note}
          regenerating={regenerating}
          onRate={onRate}
          onRegenerate={onRegenerate}
          citations={answer.citations}
          message={answer}
          renderAnnotation={renderAnnotation}
          openInPane={openInPane}
          extraActions={extraActions}
        />
      ) : null}
      {traceMode !== 'detailed'
        ? turn.events
            .filter((item) => item.type === 'error')
            .map((item) =>
              item.type === 'error' ? (
                <div key={item.id} className="error-card">
                  {item.message}
                </div>
              ) : null,
            )
        : null}
    </div>
  )
}

export const Thread = React.memo(function Thread({
  messages,
  traceMode,
  welcomeName,
  busy,
  regenerating,
  feedback,
  labels,
  language,
  effort,
  onEditSend,
  onRegenerate,
  onRate,
  onOpenRail,
  railCollapsed,
  onToggleRail,
  title,
  headerEnd,
  headerCaption,
  renderAnnotation,
  openInPane,
  extraActions,
}: {
  messages: ChatMessage[]
  traceMode: TraceMode
  welcomeName: string
  busy?: boolean
  regenerating: boolean
  feedback: MessageFeedback[]
  labels: Labels
  language: Language
  effort: string
  onEditSend: (messageId: string, turnIndex: number, text: string) => Promise<boolean>
  onRegenerate: (messageId: string, turnIndex: number) => void
  onRate: (turnIndex: number, rating: Rating, note?: string) => void
  onOpenRail: () => void
  railCollapsed: boolean
  onToggleRail: () => void
  title: string
  headerEnd?: React.ReactNode
  headerCaption?: React.ReactNode
  renderAnnotation?: CitationRender
  openInPane?: (content: React.ReactNode, options: PaneOpenOptions) => void
  extraActions?: MessageAction[]
}) {
  const scroller = useRef<HTMLDivElement>(null)
  const stick = useRef(true)
  const [showJump, setShowJump] = useState(false)
  const results = useMemo(() => {
    const map = new Map<string, ToolResult>()
    for (const message of messages) {
      if (message.type === 'tool_result') map.set(message.toolUseId, message)
    }
    return map
  }, [messages])
  const turns = useMemo(() => groupTurns(messages), [messages])
  const ratings = useMemo(() => {
    const map = new Map<number, MessageFeedback>()
    for (const entry of feedback) map.set(entry.turnIndex, entry)
    return map
  }, [feedback])
  const lastIndex = turns.length - 1
  /** The message being edited in place, if any; editing one closes an edit open on another. */
  const [editingId, setEditingId] = useState<string | null>(null)

  useEffect(() => {
    const el = scroller.current
    if (el && stick.current) el.scrollTop = el.scrollHeight
  }, [messages])

  const jumpToLatest = () => {
    const el = scroller.current
    if (!el) return
    stick.current = true
    setShowJump(false)
    el.scrollTop = el.scrollHeight
  }

  return (
    <div className="thread-shell">
      <div className="thread-bar">
        <button
          type="button"
          className="icon-button rail-toggle"
          aria-label={railCollapsed ? labels.showChats : labels.hideChats}
          aria-expanded={!railCollapsed}
          onClick={() => {
            onToggleRail()
            onOpenRail()
          }}
        >
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <rect x="2" y="3" width="12" height="10" rx="1.6" />
            <path d="M6.2 3v10" />
          </svg>
        </button>
        <h1 title={title || labels.newChat}>{title || labels.newChat}</h1>
        {headerCaption ? <div className="thread-bar-caption">{headerCaption}</div> : null}
        {headerEnd ? <div className="thread-bar-end">{headerEnd}</div> : null}
      </div>
      <div
        className="thread"
        ref={scroller}
        onScroll={(event) => {
          const el = event.currentTarget
          const atBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 80
          stick.current = atBottom
          setShowJump(!atBottom)
        }}
      >
        <div className="thread-column">
          {messages.length === 0 ? (
            <div className="welcome">
              <h2>{labels.greeting(welcomeName)}</h2>
              <p>{labels.howCanIHelp}</p>
            </div>
          ) : (
            turns.map((turn, position) => {
              const user = turn.user
              const entry = ratings.get(turn.index)
              return (
                <div key={turn.id} className="run-block">
                  {user ? (
                    <UserTurn
                      text={user.text}
                      edited={user.edited}
                      labels={labels}
                      editing={editingId === user.id}
                      onStartEdit={busy ? undefined : () => setEditingId(user.id)}
                      onCancelEdit={() => setEditingId(null)}
                      onSubmitEdit={(text) => {
                        setEditingId(null)
                        void onEditSend(user.id, turn.index, text)
                      }}
                    />
                  ) : null}
                  <AgentRun
                    turn={turn}
                    results={results}
                    traceMode={traceMode}
                    busy={busy === true}
                    labels={labels}
                    language={language}
                    effort={effort}
                    rating={entry?.rating ?? null}
                    note={entry?.note}
                    regenerating={regenerating}
                    onRate={turn.index >= 0 ? (value, note) => onRate(turn.index, value, note) : undefined}
                    // Only the last answer can be produced again: regenerating an earlier one would
                    // silently discard everything said after it, which is what editing is for.
                    onRegenerate={
                      user && position === lastIndex && !busy
                        ? () => onRegenerate(user.id, turn.index)
                        : undefined
                    }
                    renderAnnotation={renderAnnotation}
                    openInPane={openInPane}
                    extraActions={extraActions}
                  />
                </div>
              )
            })
          )}
        </div>
      </div>
      {showJump ? (
        <button type="button" className="jump-latest" onClick={jumpToLatest} aria-label={labels.jumpToLatest}>
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="M8 3v9M4.5 8.5 8 12l3.5-3.5" />
          </svg>
        </button>
      ) : null}
      <span className="sr-only" aria-live="polite">
        {busy ? labels.generating : ''}
      </span>
    </div>
  )
})
