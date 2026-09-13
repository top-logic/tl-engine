import { React } from 'tl-react-bridge'
import { Markdown } from './markdown'
import type {
  ApprovalRequest,
  AssistantText,
  ChatMessage,
  InteractionRequest,
  ToolCall,
  ToolResult,
  TraceMode,
  UserMessage,
} from './types'

const { useEffect, useMemo, useRef, useState } = React

function formatDuration(ms?: number): string {
  if (ms == null) return ''
  if (ms < 1000) return `${ms}ms`
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`
  const m = Math.floor(ms / 60_000)
  const s = Math.round((ms % 60_000) / 1000)
  return `${m}m ${s}s`
}

function formatTime(timestamp: number): string {
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
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
    try {
      input = JSON.parse(input)
    } catch {
      return input.trim() || null
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

function traceEvents(events: ChatMessage[]): ChatMessage[] {
  const answer = lastAnswer(events)
  return events.filter((item) => {
    if (answer && item === answer) return false
    if (item.type === 'assistant_text' && !item.text.trim()) return false
    return true
  })
}

function overviewLine(tools: ToolCall[]): string {
  if (tools.length === 0) return ''
  const names = tools.map((tool) => tool.name)
  const skills = tools.map(skillIdFrom).filter((id): id is string => Boolean(id))
  const uniqueNames = [...new Set(names)]
  const uniqueSkills = [...new Set(skills)]
  const used = `Used ${uniqueNames.join(', ')}`
  return uniqueSkills.length ? `${used} · loaded skill ${uniqueSkills.join(', ')}` : used
}

function UserBubble({ text }: { text: string }) {
  return (
    <div className="turn turn-user">
      <div className="bubble bubble-user">
        <div className="bubble-user-text">{text}</div>
      </div>
    </div>
  )
}

function AnswerFooter({ text, timestamp, streaming }: { text: string; timestamp: number; streaming: boolean }) {
  const [copied, setCopied] = useState(false)
  if (streaming || !text.trim()) return null
  return (
    <div className="answer-footer">
      <button
        type="button"
        className="icon-button copy-button"
        aria-label={copied ? 'Copied' : 'Copy'}
        title={copied ? 'Copied' : 'Copy'}
        onClick={async () => {
          try {
            await navigator.clipboard.writeText(text)
            setCopied(true)
            window.setTimeout(() => setCopied(false), 1500)
          } catch {
            /* clipboard may be blocked */
          }
        }}
      >
        {copied ? (
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="M3.5 8.5 6.5 11.5 12.5 4.5" />
          </svg>
        ) : (
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <rect x="5.5" y="5.5" width="8" height="9" rx="1.2" />
            <path d="M10.5 5.5V3.8A1.3 1.3 0 0 0 9.2 2.5H3.8A1.3 1.3 0 0 0 2.5 3.8v8.4A1.3 1.3 0 0 0 3.8 13.5H5.5" />
          </svg>
        )}
      </button>
      <time dateTime={new Date(timestamp).toISOString()}>{formatTime(timestamp)}</time>
    </div>
  )
}

function AssistantAnswer({
  text,
  streaming,
  timestamp,
}: {
  text: string
  streaming: boolean
  timestamp: number
}) {
  if (!text && !streaming) return null
  return (
    <div className="turn turn-agent">
      <div className={`bubble bubble-agent${streaming ? ' is-streaming' : ''}`}>
        {streaming ? <div className="bubble-stream">{text}</div> : <Markdown text={text} />}
      </div>
      <AnswerFooter text={text} timestamp={timestamp} streaming={streaming} />
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
        <span className={`trace-chevron${open ? ' is-open' : ''}`} aria-hidden="true">
          ▼
        </span>
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
          title="AskUser"
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
        <span className={`trace-chevron${open ? ' is-open' : ''}`} aria-hidden="true">
          ▼
        </span>
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

function AgentRun({
  turn,
  results,
  traceMode,
}: {
  turn: RunTurn
  results: Map<string, ToolResult>
  traceMode: TraceMode
}) {
  const answer = lastAnswer(turn.events)
  const events = traceEvents(turn.events)
  const summary = overviewLine(toolsOf(turn.events))

  return (
    <div className="agent-run">
      {traceMode === 'overview' && summary ? <div className="overview-line">{summary}</div> : null}
      {traceMode === 'detailed' && events.length > 0 ? (
        <DetailedTrace events={events} results={results} />
      ) : null}
      {answer ? (
        <AssistantAnswer text={answer.text} streaming={answer.isStreaming} timestamp={answer.timestamp} />
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
  userName,
  agentName,
  busy,
  onStarter,
}: {
  messages: ChatMessage[]
  traceMode: TraceMode
  welcomeName: string
  userName: string
  agentName?: string
  busy?: boolean
  onStarter?: (text: string) => void
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
        {messages.length === 0 ? (
          <div className="welcome">
            <div className="welcome-mark">{(agentName || 'A').slice(0, 1)}</div>
            <h2>What can I help you work through?</h2>
            <p>
              {welcomeName || userName ? `${welcomeName || userName}, ` : ''}
              ask a question, attach a document, or choose a tool.
            </p>
            {onStarter ? (
              <div className="welcome-starters">
                {['Summarize a document', 'Draft a response', 'Explore this application’s model'].map((starter) => (
                  <button key={starter} type="button" onClick={() => onStarter(starter)}>
                    {starter}
                  </button>
                ))}
              </div>
            ) : null}
          </div>
        ) : (
          turns.map((turn) => (
            <div key={turn.id} className="run-block">
              {turn.user ? <UserBubble text={turn.user.text} /> : null}
              <AgentRun turn={turn} results={results} traceMode={traceMode} />
            </div>
          ))
        )}
      </div>
      {showJump ? (
        <button type="button" className="jump-latest" onClick={jumpToLatest}>
          ↓ Jump to latest
        </button>
      ) : null}
      <span className="sr-only" aria-live="polite">
        {busy ? 'Generating a response' : ''}
      </span>
    </div>
  )
})
