import { React } from 'tl-react-bridge'
import { Markdown } from './markdown'
import type {
  ApprovalRequest,
  AssistantText,
  ChatMessage,
  InteractionRequest,
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

interface RunTurn {
  id: string
  user?: UserMessage
  events: ChatMessage[]
}

function groupTurns(messages: ChatMessage[]): RunTurn[] {
  const turns: RunTurn[] = []
  let current: RunTurn = { id: 'run-0', events: [] }

  const flush = () => {
    if (current.user || current.events.length) turns.push(current)
  }

  for (const message of messages) {
    if (message.type === 'user') {
      flush()
      current = { id: `run-${message.id}`, user: message, events: [] }
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
function overviewLine(tools: ToolCall[]): string {
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
  const calls = `${tools.length} tool call${tools.length === 1 ? '' : 's'}`
  const body = `${calls}: ${named.join(', ')}`
  return skills.length ? `${body} · skill ${skills.join(', ')}` : body
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

function UserTurn({ text, onResend }: { text: string; onResend?: (text: string) => void }) {
  return (
    <div className="turn turn-user">
      <div className="bubble-user">
        <div className="bubble-user-text">{text}</div>
        {onResend ? (
          <span className="bubble-user-actions">
            <IconButton
              label="Edit and send again"
              hint="Edit and send again"
              onClick={() => onResend(text)}
            >
              {EditIcon}
            </IconButton>
          </span>
        ) : null}
      </div>
    </div>
  )
}

/**
 * Copy, a rating and a retry under a finished answer.
 *
 * The rating has no store behind it yet - there is no feedback endpoint in `ChatServlet` - so it
 * only records the click for this view and says so on hover. TODO(#390 follow-up): persist it once
 * the chat API accepts an answer rating.
 */
function AnswerFooter({
  text,
  onRetry,
}: {
  text: string
  onRetry?: () => void
}) {
  const [copied, setCopied] = useState(false)
  const [rating, setRating] = useState<'up' | 'down' | null>(null)
  if (!text.trim()) return null
  return (
    <div className="answer-footer">
      <IconButton
        label={copied ? 'Copied' : 'Copy answer'}
        hint={copied ? 'Copied' : 'Copy answer'}
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
        label="Good answer"
        hint="Good answer (not stored yet)"
        active={rating === 'up'}
        onClick={() => setRating((value) => (value === 'up' ? null : 'up'))}
      >
        {ThumbUpIcon}
      </IconButton>
      <IconButton
        label="Bad answer"
        hint="Bad answer (not stored yet)"
        active={rating === 'down'}
        onClick={() => setRating((value) => (value === 'down' ? null : 'down'))}
      >
        {ThumbDownIcon}
      </IconButton>
      {onRetry ? (
        <IconButton label="Ask again" hint="Ask again" onClick={onRetry}>
          {RetryIcon}
        </IconButton>
      ) : null}
    </div>
  )
}

function AssistantAnswer({
  text,
  streaming,
  onRetry,
}: {
  text: string
  streaming: boolean
  onRetry?: () => void
}) {
  if (!text && !streaming) return null
  return (
    <div className="turn turn-agent">
      <div className={`answer${streaming ? ' is-streaming' : ''}`}>
        <Markdown text={text} streaming={streaming} />
      </div>
      {streaming ? null : <AnswerFooter text={text} onRetry={onRetry} />}
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
}: {
  label: string
  text: string
  danger?: boolean
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
          {more ? 'Show less' : `Show ${text.length - limit} more characters`}
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
}: {
  item: ChatMessage
  results: Map<string, ToolResult>
}) {
  switch (item.type) {
    case 'thinking':
      return (
        <TraceEntry title="Thinking" preview={item.content.replace(/\s+/g, ' ').slice(0, 80)}>
          <TraceBlock label="Thinking" text={item.content} />
        </TraceEntry>
      )
    case 'tool_call': {
      const result = results.get(item.toolUseId)
      return (
        <TraceEntry
          title={item.name}
          meta={item.status === 'running' ? 'running' : formatDuration(item.durationMs)}
          danger={item.status === 'error' || result?.isError}
        >
          <TraceBlock label="Arguments" text={pretty(item.input)} />
          {result ? (
            <TraceBlock
              label={result.isError ? 'Error' : 'Result'}
              text={result.output}
              danger={result.isError}
            />
          ) : item.status === 'running' ? (
            <TraceBlock label="Result" text="Running…" />
          ) : null}
        </TraceEntry>
      )
    }
    case 'assistant_text':
      return (
        <TraceEntry title="Output" preview={item.text.replace(/\s+/g, ' ').slice(0, 80)}>
          <div className="trace-block">
            <div className="trace-block-label">Output</div>
            <div className="trace-block-md">
              <Markdown text={item.text} />
            </div>
          </div>
        </TraceEntry>
      )
    case 'interaction_request':
      return (
        <TraceEntry
          title="Question"
          meta={item.answered ? 'answered' : 'waiting'}
          preview={interactionPreview(item)}
        >
          <TraceBlock label="Question" text={item.question} />
        </TraceEntry>
      )
    case 'approval_request':
      return (
        <TraceEntry
          title="Approval"
          meta={item.outcome === 'pending' ? 'waiting' : item.outcome.replace(/_/g, ' ').toLowerCase()}
          preview={approvalPreview(item)}
        >
          <TraceBlock label="Tool" text={item.toolName} />
          {item.displayReason ? <TraceBlock label="Reason" text={item.displayReason} /> : null}
        </TraceEntry>
      )
    case 'error':
      return (
        <TraceEntry title="Error" danger preview={item.message.replace(/\s+/g, ' ').slice(0, 80)}>
          <TraceBlock label="Error" text={item.message} danger />
        </TraceEntry>
      )
    default:
      return null
  }
}

function DetailedTrace({
  events,
  results,
}: {
  events: ChatMessage[]
  results: Map<string, ToolResult>
}) {
  const [open, setOpen] = useState(false)
  const toolCount = events.filter((item) => item.type === 'tool_call').length
  const label = toolCount
    ? `${toolCount} tool${toolCount === 1 ? '' : 's'} used`
    : `${events.length} step${events.length === 1 ? '' : 's'}`

  return (
    <div className="trace-panel">
      <button type="button" className="trace-panel-head" onClick={() => setOpen((value) => !value)}>
        <span>Trace</span>
        <span className="trace-panel-summary">{label}</span>
        <span className={`trace-chevron${open ? ' is-open' : ''}`} aria-hidden="true" />
      </button>
      {open ? (
        <div className="trace-panel-body">
          {events.map((item) => (
            <TraceEvent key={item.id} item={item} results={results} />
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
}: {
  meta: RunMeta | null
  summary: string
  busy: boolean
}) {
  const worked = meta?.durationMs != null ? `Worked for ${formatDuration(meta.durationMs)}` : ''
  const label = worked || (meta && busy ? 'Working…' : '')
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
  onRetry,
}: {
  turn: RunTurn
  results: Map<string, ToolResult>
  traceMode: TraceMode
  busy: boolean
  onRetry?: () => void
}) {
  const answer = lastAnswer(turn.events)
  const events = traceEvents(turn.events)
  const tools = toolsOf(turn.events)
  const meta = runMetaOf(turn.events)
  const summary = traceMode === 'overview' ? overviewLine(tools) : ''

  return (
    <div className="agent-run">
      <RunMetaLine meta={meta} summary={summary} busy={busy} />
      {traceMode === 'detailed' && events.length > 0 ? (
        <DetailedTrace events={events} results={results} />
      ) : null}
      {answer ? (
        <AssistantAnswer text={answer.text} streaming={answer.isStreaming} onRetry={onRetry} />
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
  onResend,
  onRetry,
  onOpenRail,
  railCollapsed,
  onToggleRail,
  title,
}: {
  messages: ChatMessage[]
  traceMode: TraceMode
  welcomeName: string
  busy?: boolean
  onResend?: (text: string) => void
  onRetry?: (text: string) => void
  onOpenRail: () => void
  railCollapsed: boolean
  onToggleRail: () => void
  title: string
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
          aria-label={railCollapsed ? 'Show chats' : 'Hide chats'}
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
        <h1>{title || 'New chat'}</h1>
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
              <h2>{welcomeName ? `Hello ${welcomeName}.` : 'Hello.'}</h2>
              <p>What can I help you with?</p>
            </div>
          ) : (
            turns.map((turn) => (
              <div key={turn.id} className="run-block">
                {turn.user ? <UserTurn text={turn.user.text} onResend={onResend} /> : null}
                <AgentRun
                  turn={turn}
                  results={results}
                  traceMode={traceMode}
                  busy={busy === true}
                  onRetry={turn.user && onRetry ? () => onRetry(turn.user!.text) : undefined}
                />
              </div>
            ))
          )}
        </div>
      </div>
      {showJump ? (
        <button type="button" className="jump-latest" onClick={jumpToLatest} aria-label="Jump to latest">
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="M8 3v9M4.5 8.5 8 12l3.5-3.5" />
          </svg>
        </button>
      ) : null}
      <span className="sr-only" aria-live="polite">
        {busy ? 'Generating a response' : ''}
      </span>
    </div>
  )
})
