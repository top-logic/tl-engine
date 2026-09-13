import { React } from 'tl-react-bridge'
import { cancelPromptImprovement, fileToAttachment, improvePrompt } from './api'
import { runStore } from './store'
import { PromptStack } from './prompts'
import { SubagentActivityPanel } from './subagents'
import type { ChatAction } from './reducer'
import type {
  AgentOption,
  ApprovalRequest,
  Attachment,
  ChatMessage,
  InteractionRequest,
  McpServerOption,
  ModelOption,
  SubagentActivity,
  TraceMode,
} from './types'

const { useEffect, useRef, useState, useSyncExternalStore } = React

type MenuPane = 'root' | 'agent' | 'model' | 'mcp' | 'trace'

const TRACE_OPTIONS: { id: TraceMode; label: string }[] = [
  { id: 'none', label: 'None' },
  { id: 'overview', label: 'Overview' },
  { id: 'detailed', label: 'Detailed' },
]

function formatTokens(n: number): string {
  if (!n) return '0'
  if (n < 1000) return String(n)
  if (n < 100_000) return `${(n / 1000).toFixed(1)}K`
  return `${Math.round(n / 1000)}K`
}

function usageRows(usage?: { inputTokens?: number; outputTokens?: number; thinkingTokens?: number; cacheReadTokens?: number; cacheWriteTokens?: number }) {
  if (!usage) return []
  return [
    ['Input', usage.inputTokens],
    ['Output', usage.outputTokens],
    ['Thinking', usage.thinkingTokens],
    ['Cache read', usage.cacheReadTokens],
    ['Cache write', usage.cacheWriteTokens],
  ].filter((row): row is [string, number] => typeof row[1] === 'number' && row[1] > 0)
}

const SendButton = React.memo(function SendButton({
  disabled,
  busy,
  onSend,
  onStop,
}: {
  disabled: boolean
  busy: boolean
  onSend: () => void
  onStop: () => void
}) {
  return (
    <button
      type="button"
      className={`send-button${busy ? ' is-busy' : ''}`}
      disabled={!busy && disabled}
      onClick={busy ? onStop : onSend}
      aria-label={busy ? 'Stop' : 'Send'}
      title={busy ? 'Stop' : 'Send'}
    >
      {busy ? <span className="gauge-stop" /> : <span className="gauge-arrow" />}
    </button>
  )
})

const ContextUsage = React.memo(function ContextUsage() {
  const snap = useSyncExternalStore(runStore.subscribe, runStore.get, runStore.get)
  const [open, setOpen] = useState(false)
  const wrap = useRef<HTMLDivElement>(null)
  const windowSize = snap.contextWindow || 0
  const used = snap.usedTokens || 0
  const remaining = Math.max(0, windowSize - used)
  const ratio = windowSize > 0 ? Math.min(1, used / windowSize) : 0
  const percent = Math.round(ratio * 100)
  const tone = ratio > 0.9 ? 'ember' : ratio > 0.7 ? 'warn' : 'ok'
  const r = 14
  const c = 2 * Math.PI * r
  const dash = c * ratio
  const rows = usageRows(snap.lastUsage)

  useEffect(() => {
    if (!open) return
    const onDoc = (event: MouseEvent) => {
      if (!wrap.current?.contains(event.target as Node)) setOpen(false)
    }
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onDoc)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDoc)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  if (!windowSize) return null

  return (
    <div className="context-wrap" ref={wrap}>
      <button
        type="button"
        className={`context-gauge tone-${tone}${open ? ' is-open' : ''}`}
        aria-expanded={open}
        aria-haspopup="dialog"
        aria-label={`Context usage, ${percent} percent full`}
        title={`${formatTokens(used)} / ${formatTokens(windowSize)} used`}
        onClick={() => setOpen((value) => !value)}
      >
        <svg viewBox="0 0 36 36" aria-hidden="true">
          <circle className="gauge-track" cx="18" cy="18" r={r} />
          <circle
            className="gauge-arc"
            cx="18"
            cy="18"
            r={r}
            strokeDasharray={`${dash} ${c}`}
            transform="rotate(-90 18 18)"
          />
        </svg>
      </button>
      {open ? (
        <div className="context-popover" role="dialog" aria-label="Context usage">
          <div className="context-popover-head">
            <strong>Context Usage</strong>
            <span>{formatTokens(used)} / {formatTokens(windowSize)} tokens</span>
          </div>
          <div className="context-popover-meta">
            <span>{percent}% Full</span>
            <span>~{formatTokens(used)} / {formatTokens(windowSize)}</span>
          </div>
          <div className="context-bar" aria-hidden="true">
            <i className={`tone-${tone}`} style={{ width: `${Math.max(2, percent)}%` }} />
          </div>
          <dl className="context-rows">
            <div>
              <dt>Used</dt>
              <dd>{formatTokens(used)}</dd>
            </div>
            <div>
              <dt>Remaining</dt>
              <dd>{formatTokens(remaining)}</dd>
            </div>
            {snap.model ? (
              <div>
                <dt>Model</dt>
                <dd>{snap.model}</dd>
              </div>
            ) : null}
            {rows.map(([label, value]) => (
              <div key={label}>
                <dt>{label}</dt>
                <dd>{formatTokens(value)}</dd>
              </div>
            ))}
          </dl>
        </div>
      ) : null}
    </div>
  )
})

export const Composer = React.memo(function Composer({
  windowId,
  agents,
  models,
  agent,
  model,
  onAgent,
  onModel,
  mcpServers,
  selectedMcp,
  onToggleMcp,
  traceMode,
  onTraceMode,
  autoApprove,
  onToggleAutoApprove,
  pendingApprovals,
  pendingQuestions,
  dispatch,
  promptImprovementEnabled,
  subagents,
  onCancelSubagent,
  onStopSubagents,
  onLoadSubagentTranscript,
  busy,
  onSend,
  onStop,
}: {
  windowId: string
  agents: AgentOption[]
  models: ModelOption[]
  agent: string
  model: string
  onAgent: (name: string) => void
  onModel: (id: string) => void
  mcpServers: McpServerOption[]
  selectedMcp: string[]
  onToggleMcp: (name: string) => void
  traceMode: TraceMode
  onTraceMode: (mode: TraceMode) => void
  autoApprove: boolean
  onToggleAutoApprove: () => void
  pendingApprovals: ApprovalRequest[]
  pendingQuestions: InteractionRequest[]
  dispatch: React.Dispatch<ChatAction>
  promptImprovementEnabled: boolean
  subagents: SubagentActivity[]
  onCancelSubagent: (id: string) => Promise<void>
  onStopSubagents: () => Promise<void>
  onLoadSubagentTranscript: (id: string) => Promise<ChatMessage[]>
  busy: boolean
  onSend: (text: string, attachments: Attachment[]) => void
  onStop: () => void
}) {
  const [text, setText] = useState('')
  const [files, setFiles] = useState<Attachment[]>([])
  const [menuOpen, setMenuOpen] = useState(false)
  const [pane, setPane] = useState<MenuPane>('root')
  const [query, setQuery] = useState('')
  const [improveVisible, setImproveVisible] = useState(false)
  const [improving, setImproving] = useState(false)
  const [improveError, setImproveError] = useState('')
  const area = useRef<HTMLTextAreaElement>(null)
  const file = useRef<HTMLInputElement>(null)
  const menu = useRef<HTMLDivElement>(null)
  const improveId = useRef<string | null>(null)
  const abort = useRef<AbortController | null>(null)

  useEffect(() => {
    const el = area.current
    if (!el) return
    el.style.height = 'auto'
    el.style.height = `${Math.min(el.scrollHeight, 180)}px`
  }, [text])

  useEffect(() => {
    if (!promptImprovementEnabled || busy || improving || !text.trim()) {
      setImproveVisible(false)
      return
    }
    const timer = window.setTimeout(() => setImproveVisible(true), 700)
    return () => window.clearTimeout(timer)
  }, [busy, improving, promptImprovementEnabled, text])

  useEffect(() => {
    if (!menuOpen) return
    const onDoc = (event: MouseEvent) => {
      if (!menu.current?.contains(event.target as Node)) {
        setMenuOpen(false)
        setPane('root')
        setQuery('')
      }
    }
    const onKey = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      if (pane !== 'root') setPane('root')
      else setMenuOpen(false)
    }
    document.addEventListener('mousedown', onDoc)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDoc)
      document.removeEventListener('keydown', onKey)
    }
  }, [menuOpen, pane])

  const q = query.trim().toLocaleLowerCase()
  const agentHits = agents.filter((item) => !q || item.name.toLocaleLowerCase().includes(q))
  const modelHits = models.filter(
    (item) => !q || item.id.toLocaleLowerCase().includes(q) || (item.provider ?? '').toLocaleLowerCase().includes(q),
  )
  const mcpHits = mcpServers.filter((item) => !q || item.name.toLocaleLowerCase().includes(q))

  const submit = () => {
    const trimmed = text.trim()
    if ((!trimmed && files.length === 0) || busy) return
    onSend(trimmed, files)
    setText('')
    setFiles([])
    setImproveVisible(false)
  }

  const runImprove = async () => {
    const draft = text.trim()
    if (!draft || improving) return
    const requestId = crypto.randomUUID()
    improveId.current = requestId
    abort.current?.abort()
    abort.current = new AbortController()
    setImproving(true)
    setImproveError('')
    try {
      const result = await improvePrompt(windowId, draft, requestId, abort.current.signal)
      if (improveId.current !== requestId) return
      if (result.status === 'completed' && (result.improvedText || result.text)) {
        setText(result.improvedText || result.text || draft)
        setImproveVisible(false)
        area.current?.focus()
      } else if (result.status !== 'cancelled' && result.status !== 'stale') {
        setImproveError('The prompt could not be improved.')
      }
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') return
      setImproveError('The prompt could not be improved.')
    } finally {
      if (improveId.current === requestId) setImproving(false)
    }
  }

  const cancelImprove = () => {
    const requestId = improveId.current
    abort.current?.abort()
    if (requestId) void cancelPromptImprovement(windowId, requestId)
    setImproving(false)
    area.current?.focus()
  }

  return (
    <footer className="composer">
      <div className="composer-overlays">
        <PromptStack
          approvals={pendingApprovals}
          questions={pendingQuestions}
          windowId={windowId}
          dispatch={dispatch}
        />
        <SubagentActivityPanel
          subagents={subagents}
          onCancel={onCancelSubagent}
          onStopAll={onStopSubagents}
          onLoadTranscript={onLoadSubagentTranscript}
        />
        {promptImprovementEnabled && (improveVisible || improving || improveError) ? (
          <div className="improve-popup">
            {improving ? (
              <button type="button" className="overlay-chip" onClick={cancelImprove}>
                Improving…
                <span>Cancel</span>
              </button>
            ) : (
              <button type="button" className="overlay-chip" onClick={() => void runImprove()}>
                ✦ Improve prompt
              </button>
            )}
            {improveError ? (
              <button type="button" className="overlay-chip is-error" onClick={() => void runImprove()}>
                {improveError} · Retry
              </button>
            ) : null}
          </div>
        ) : null}
      </div>

      <div className="composer-surface">
        {files.length > 0 ? (
          <div className="attach-row">
            {files.map((item) => (
              <span key={item.fileName} className="attach-chip">
                {item.fileName}
                <button
                  type="button"
                  aria-label={`Remove ${item.fileName}`}
                  onClick={() => setFiles((prev) => prev.filter((fileItem) => fileItem !== item))}
                >
                  ×
                </button>
              </span>
            ))}
          </div>
        ) : null}

        <textarea
          ref={area}
          rows={1}
          value={text}
          onChange={(event) => setText(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter' && !event.shiftKey) {
              event.preventDefault()
              submit()
            }
          }}
          placeholder="Ask Albert…"
          disabled={busy}
        />

        <div className="composer-actions">
          <input
            ref={file}
            type="file"
            accept="image/*,application/pdf"
            multiple
            hidden
            onChange={async (event) => {
              const list = Array.from(event.target.files ?? []).slice(0, 5)
              const next = await Promise.all(list.map(fileToAttachment))
              setFiles((prev) => [...prev, ...next].slice(0, 5))
              event.target.value = ''
              setMenuOpen(false)
            }}
          />

          <div className="plus-menu" ref={menu}>
            <button
              type="button"
              className={`plus-trigger${menuOpen ? ' is-open' : ''}`}
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              aria-label="Add files, change agent, model, or tools"
              onClick={() => {
                setMenuOpen((value) => !value)
                setPane('root')
                setQuery('')
              }}
            >
              +
            </button>
            {menuOpen ? (
              <div className="plus-popover" role="menu">
                <label className="plus-search">
                  <span className="sr-only">Search agents, models, MCPs</span>
                  <input
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                    placeholder="Search agents, models, MCPs…"
                    autoFocus
                  />
                </label>
                {pane !== 'root' ? (
                  <button type="button" className="plus-back" onClick={() => setPane('root')}>
                    ← Back
                  </button>
                ) : null}
                {pane === 'root' ? (
                  <div className="plus-list">
                    <button type="button" className="plus-row" onClick={() => file.current?.click()}>
                      <span className="plus-ico" aria-hidden="true">📎</span>
                      <span>
                        <strong>Files</strong>
                        <em>Add images or PDFs</em>
                      </span>
                    </button>
                    <button type="button" className="plus-row" onClick={() => setPane('agent')}>
                      <span className="plus-ico" aria-hidden="true">A</span>
                      <span>
                        <strong>Agent</strong>
                        <em>{agent || 'Choose an agent'}</em>
                      </span>
                      <span className="plus-more">›</span>
                    </button>
                    <button type="button" className="plus-row" onClick={() => setPane('model')}>
                      <span className="plus-ico" aria-hidden="true">◇</span>
                      <span>
                        <strong>Model</strong>
                        <em>{model || 'Choose a model'}</em>
                      </span>
                      <span className="plus-more">›</span>
                    </button>
                    <button type="button" className="plus-row" onClick={() => setPane('mcp')}>
                      <span className="plus-ico" aria-hidden="true">⛓</span>
                      <span>
                        <strong>MCP</strong>
                        <em>{selectedMcp.length ? `${selectedMcp.length} enabled` : 'No tools selected'}</em>
                      </span>
                      <span className="plus-more">›</span>
                    </button>
                    <button type="button" className="plus-row" onClick={() => setPane('trace')}>
                      <span className="plus-ico" aria-hidden="true">☰</span>
                      <span>
                        <strong>Trace</strong>
                        <em>{TRACE_OPTIONS.find((item) => item.id === traceMode)?.label ?? 'Overview'}</em>
                      </span>
                      <span className="plus-more">›</span>
                    </button>
                  </div>
                ) : null}
                {pane === 'agent' ? (
                  <div className="plus-list">
                    {agentHits.map((item) => (
                      <button
                        key={item.name}
                        type="button"
                        className={`plus-row${item.name === agent ? ' is-selected' : ''}`}
                        onClick={() => {
                          onAgent(item.name)
                          setMenuOpen(false)
                        }}
                      >
                        <span>
                          <strong>{item.name}</strong>
                        </span>
                      </button>
                    ))}
                    {agentHits.length === 0 ? <div className="plus-empty">No matching agents</div> : null}
                  </div>
                ) : null}
                {pane === 'model' ? (
                  <div className="plus-list">
                    {modelHits.map((item) => (
                      <button
                        key={item.id}
                        type="button"
                        className={`plus-row${item.id === model ? ' is-selected' : ''}`}
                        onClick={() => {
                          onModel(item.id)
                          setMenuOpen(false)
                        }}
                      >
                        <span>
                          <strong>{item.id}</strong>
                          {item.provider ? <em>{item.provider}</em> : null}
                        </span>
                      </button>
                    ))}
                    {modelHits.length === 0 ? <div className="plus-empty">No matching models</div> : null}
                  </div>
                ) : null}
                {pane === 'mcp' ? (
                  <div className="plus-list">
                    {mcpHits.map((item) => {
                      const on = selectedMcp.includes(item.name)
                      return (
                        <button
                          key={item.name}
                          type="button"
                          className={`plus-row${on ? ' is-selected' : ''}${item.status === 'disconnected' ? ' is-off' : ''}`}
                          onClick={() => onToggleMcp(item.name)}
                        >
                          <span>
                            <strong>{item.name}</strong>
                            <em>{on ? 'Enabled' : item.status || 'Off'}</em>
                          </span>
                        </button>
                      )
                    })}
                    {mcpHits.length === 0 ? <div className="plus-empty">No matching MCP servers</div> : null}
                  </div>
                ) : null}
                {pane === 'trace' ? (
                  <div className="plus-list">
                    {TRACE_OPTIONS.map((item) => (
                      <button
                        key={item.id}
                        type="button"
                        className={`plus-row${item.id === traceMode ? ' is-selected' : ''}`}
                        onClick={() => {
                          onTraceMode(item.id)
                          setMenuOpen(false)
                        }}
                      >
                        <span>
                          <strong>{item.label}</strong>
                        </span>
                      </button>
                    ))}
                  </div>
                ) : null}
              </div>
            ) : null}
          </div>

          <button
            type="button"
            className={`approve-switch${autoApprove ? ' is-on' : ''}`}
            role="switch"
            aria-checked={autoApprove}
            onClick={onToggleAutoApprove}
          >
            <span className="approve-switch-track" aria-hidden="true">
              <span className="approve-switch-knob" />
            </span>
            Auto-approve
          </button>
          <span className="composer-grow" />
          <SendButton disabled={!text.trim() && files.length === 0} busy={busy} onSend={submit} onStop={onStop} />
          <ContextUsage />
        </div>
      </div>
    </footer>
  )
})
