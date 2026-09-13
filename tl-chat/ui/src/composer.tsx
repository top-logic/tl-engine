import { React } from 'tl-react-bridge'
import { cancelPromptImprovement, improvePrompt } from './api'
import { ACCEPT_ATTRIBUTE, collectAttachments, dragHasFiles } from './attachments'
import { ContextUsage } from './context-usage'
import { PromptStack } from './prompts'
import { SubagentActivityPanel } from './subagents'
import type { ChatAction } from './reducer'
import type {
  AgentOption,
  ApprovalRequest,
  Attachment,
  ChatMessage,
  ConnectionState,
  InteractionRequest,
  McpServerOption,
  ModelOption,
  SubagentActivity,
  TraceMode,
} from './types'

const { useEffect, useRef, useState } = React

type MenuPane = 'root' | 'agent' | 'model' | 'mcp' | 'trace'

const TRACE_OPTIONS: { id: TraceMode; label: string; description: string }[] = [
  { id: 'none', label: 'None', description: 'Only the answer' },
  { id: 'overview', label: 'Overview', description: 'One line naming the tools that ran' },
  { id: 'detailed', label: 'Detailed', description: 'Every step with its arguments and result' },
]

const PANE_TITLES: Record<Exclude<MenuPane, 'root'>, string> = {
  agent: 'Agent',
  model: 'Model',
  mcp: 'MCP servers',
  trace: 'Trace level',
}

const Icon = {
  plus: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M8 3.5v9M3.5 8h9" />
    </svg>
  ),
  paperclip: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M12.5 7.5 7.8 12.2a2.9 2.9 0 0 1-4.1-4.1l5-5a1.9 1.9 0 1 1 2.7 2.7l-5 5a.9.9 0 1 1-1.3-1.3l4.6-4.6" />
    </svg>
  ),
  agent: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <rect x="3" y="5.5" width="10" height="7.5" rx="2" />
      <path d="M8 2.5v3M5.8 9h.01M10.2 9h.01" />
    </svg>
  ),
  model: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M8 2.2 13.4 5.4v5.2L8 13.8 2.6 10.6V5.4z" />
    </svg>
  ),
  mcp: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M6.5 9.5 9.5 6.5" />
      <path d="M9 4.6 10.4 3.2a2.6 2.6 0 0 1 3.7 3.7L12.7 8.3" />
      <path d="M7 11.4 5.6 12.8a2.6 2.6 0 0 1-3.7-3.7L3.3 7.7" />
    </svg>
  ),
  trace: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M3 4.5h10M3 8h7M3 11.5h4" />
    </svg>
  ),
  back: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M9.5 3.5 5 8l4.5 4.5" />
    </svg>
  ),
  chevron: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="m6.5 3.5 4.5 4.5-4.5 4.5" />
    </svg>
  ),
  caret: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="m4.5 6.5 3.5 3.5 3.5-3.5" />
    </svg>
  ),
  spark: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M8 2.5 9.3 6 12.8 7.3 9.3 8.6 8 12.1 6.7 8.6 3.2 7.3 6.7 6z" />
    </svg>
  ),
  close: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="m4 4 8 8M12 4l-8 8" />
    </svg>
  ),
  undo: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M3 8a5 5 0 1 0 1.6-3.7" />
      <path d="M2.7 2v3h3" />
    </svg>
  ),
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
      aria-label={busy ? 'Stop the run' : 'Send'}
      title={busy ? 'Stop the run' : 'Send'}
    >
      {busy ? (
        <span className="send-stop" />
      ) : (
        <svg viewBox="0 0 16 16" aria-hidden="true" className="send-arrow">
          <path d="M8 12.5v-9M4.2 7.3 8 3.5l3.8 3.8" />
        </svg>
      )}
    </button>
  )
})

function MenuRow({
  icon,
  label,
  description,
  value,
  selected,
  more,
  onClick,
}: {
  icon?: React.ReactNode
  label: string
  description?: string
  value?: string
  selected?: boolean
  more?: boolean
  onClick: () => void
}) {
  return (
    <button type="button" className={`plus-row${selected ? ' is-selected' : ''}`} onClick={onClick}>
      {icon ? (
        <span className="plus-ico" aria-hidden="true">
          {icon}
        </span>
      ) : (
        <span className="plus-ico" aria-hidden="true" />
      )}
      <span className="plus-text">
        <strong>{label}</strong>
        {description ? <em>{description}</em> : null}
      </span>
      {value ? <span className="plus-value">{value}</span> : null}
      {more ? (
        <span className="plus-more" aria-hidden="true">
          {Icon.chevron}
        </span>
      ) : null}
    </button>
  )
}

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
  connection,
  notice,
  onDismissNotice,
  resendDraft,
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
  connection: ConnectionState
  notice: string
  onDismissNotice: () => void
  resendDraft: { text: string; nonce: number } | null
  onSend: (text: string, attachments: Attachment[]) => Promise<boolean>
  onStop: () => void
}) {
  const [text, setText] = useState('')
  const [files, setFiles] = useState<Attachment[]>([])
  const [attachError, setAttachError] = useState('')
  const [dropping, setDropping] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)
  const [modelMenuOpen, setModelMenuOpen] = useState(false)
  const [pane, setPane] = useState<MenuPane>('root')
  const [query, setQuery] = useState('')
  const [improveVisible, setImproveVisible] = useState(false)
  const [improving, setImproving] = useState(false)
  const [improveError, setImproveError] = useState('')
  const [undoDraft, setUndoDraft] = useState<string | null>(null)
  const area = useRef<HTMLTextAreaElement>(null)
  const file = useRef<HTMLInputElement>(null)
  const menu = useRef<HTMLDivElement>(null)
  const modelMenu = useRef<HTMLDivElement>(null)
  const surface = useRef<HTMLDivElement>(null)
  const improveId = useRef<string | null>(null)
  const abort = useRef<AbortController | null>(null)

  useEffect(() => {
    const el = area.current
    if (!el) return
    el.style.height = 'auto'
    el.style.height = `${Math.min(el.scrollHeight, 180)}px`
  }, [text])

  // "Edit and send again" on a user message refills the composer; the nonce lets the same text be
  // sent back more than once.
  useEffect(() => {
    if (!resendDraft) return
    setText(resendDraft.text)
    setUndoDraft(null)
    const el = area.current
    if (!el) return
    el.focus()
    el.setSelectionRange(resendDraft.text.length, resendDraft.text.length)
  }, [resendDraft])

  useEffect(() => {
    if (!promptImprovementEnabled || improving || !text.trim()) {
      setImproveVisible(false)
      return
    }
    const timer = window.setTimeout(() => setImproveVisible(true), 700)
    return () => window.clearTimeout(timer)
  }, [improving, promptImprovementEnabled, text])

  useEffect(() => {
    if (!menuOpen && !modelMenuOpen) return
    const onDoc = (event: MouseEvent) => {
      const target = event.target as Node
      if (menuOpen && !menu.current?.contains(target)) {
        setMenuOpen(false)
        setPane('root')
        setQuery('')
      }
      if (modelMenuOpen && !modelMenu.current?.contains(target)) setModelMenuOpen(false)
    }
    const onKey = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      if (modelMenuOpen) {
        setModelMenuOpen(false)
        return
      }
      if (pane !== 'root') setPane('root')
      else setMenuOpen(false)
    }
    document.addEventListener('mousedown', onDoc)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDoc)
      document.removeEventListener('keydown', onKey)
    }
  }, [menuOpen, modelMenuOpen, pane])

  const addFiles = async (incoming: File[]) => {
    const result = await collectAttachments(incoming, files)
    if (result.attachments.length) {
      setFiles((prev) => [...prev, ...result.attachments])
    }
    setAttachError(result.rejected)
    if (result.rejected) {
      window.setTimeout(() => setAttachError(''), 6000)
    }
  }

  /**
   * Files may be dropped anywhere over the chat, not only on the composer.
   *
   * The listeners hang on the cockpit element rather than on the composer, so the thread area is a
   * drop target too - dragging a screenshot onto the conversation is the obvious gesture and
   * dropping it there must not navigate the browser away from the application.
   */
  useEffect(() => {
    const root = surface.current?.closest('.tl-cockpit') as HTMLElement | null
    if (!root) return
    let depth = 0
    const onEnter = (event: DragEvent) => {
      if (!dragHasFiles(event.dataTransfer)) return
      event.preventDefault()
      depth++
      setDropping(true)
    }
    const onOver = (event: DragEvent) => {
      if (!dragHasFiles(event.dataTransfer)) return
      event.preventDefault()
      if (event.dataTransfer) event.dataTransfer.dropEffect = 'copy'
    }
    const onLeave = (event: DragEvent) => {
      if (!dragHasFiles(event.dataTransfer)) return
      depth = Math.max(0, depth - 1)
      if (depth === 0) setDropping(false)
    }
    const onDrop = (event: DragEvent) => {
      if (!dragHasFiles(event.dataTransfer)) return
      event.preventDefault()
      depth = 0
      setDropping(false)
      void addFiles(Array.from(event.dataTransfer?.files ?? []))
      area.current?.focus()
    }
    root.addEventListener('dragenter', onEnter)
    root.addEventListener('dragover', onOver)
    root.addEventListener('dragleave', onLeave)
    root.addEventListener('drop', onDrop)
    return () => {
      root.removeEventListener('dragenter', onEnter)
      root.removeEventListener('dragover', onOver)
      root.removeEventListener('dragleave', onLeave)
      root.removeEventListener('drop', onDrop)
    }
  })

  const q = query.trim().toLocaleLowerCase()
  const agentHits = agents.filter(
    (item) => !q || item.name.toLocaleLowerCase().includes(q) || (item.description ?? '').toLocaleLowerCase().includes(q),
  )
  const modelHits = models.filter(
    (item) => !q || item.id.toLocaleLowerCase().includes(q) || (item.provider ?? '').toLocaleLowerCase().includes(q),
  )
  const mcpHits = mcpServers.filter((item) => !q || item.name.toLocaleLowerCase().includes(q))
  const traceHits = TRACE_OPTIONS.filter(
    (item) => !q || item.label.toLocaleLowerCase().includes(q) || item.description.toLocaleLowerCase().includes(q),
  )
  // With a search term the root pane is a result list across all four sections, as in a command
  // palette: typing "opus" must find the model without first walking into the model pane.
  const searching = q.length > 0

  const submit = () => {
    const trimmed = text.trim()
    if (!trimmed && files.length === 0) return
    const sentFiles = files
    void onSend(trimmed, sentFiles).then((sent) => {
      if (!sent) return
      setText('')
      setFiles([])
      setUndoDraft(null)
      setImproveVisible(false)
    })
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
        setUndoDraft(draft)
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

  const undoImprove = () => {
    if (undoDraft == null) return
    setText(undoDraft)
    setUndoDraft(null)
    area.current?.focus()
  }

  const closeMenu = () => {
    setMenuOpen(false)
    setPane('root')
    setQuery('')
  }

  const modelLabel = model || 'Model'

  return (
    <footer className={`composer${dropping ? ' is-dropping' : ''}`}>
      <div className="composer-overlays">
        <PromptStack
          approvals={pendingApprovals}
          questions={pendingQuestions}
          windowId={windowId}
          dispatch={dispatch}
        />
        <div className="composer-chips">
          <SubagentActivityPanel
            subagents={subagents}
            onCancel={onCancelSubagent}
            onStopAll={onStopSubagents}
            onLoadTranscript={onLoadSubagentTranscript}
          />
          {connection === 'reconnecting' ? (
            <span className="overlay-chip is-muted" role="status">
              <span className="chip-spinner" aria-hidden="true" />
              Reconnecting…
            </span>
          ) : null}
          {promptImprovementEnabled && improving ? (
            <button type="button" className="overlay-chip" onClick={cancelImprove}>
              <span className="chip-spinner" aria-hidden="true" />
              Improving…
              <span>Cancel</span>
            </button>
          ) : null}
          {promptImprovementEnabled && !improving && undoDraft != null ? (
            <button type="button" className="overlay-chip" onClick={undoImprove}>
              {Icon.undo}
              Undo improvement
            </button>
          ) : null}
          {promptImprovementEnabled && !improving && undoDraft == null && improveVisible ? (
            <button type="button" className="overlay-chip is-accent" onClick={() => void runImprove()}>
              {Icon.spark}
              Improve prompt
            </button>
          ) : null}
          {improveError ? (
            <button type="button" className="overlay-chip is-error" onClick={() => void runImprove()}>
              {improveError} · Retry
            </button>
          ) : null}
        </div>
      </div>

      {notice || attachError ? (
        <div className="composer-notice" role="status">
          <span>{notice || attachError}</span>
          <button
            type="button"
            className="icon-button tiny"
            aria-label="Dismiss"
            onClick={() => {
              onDismissNotice()
              setAttachError('')
            }}
          >
            {Icon.close}
          </button>
        </div>
      ) : null}

      <div className={`composer-surface${dropping ? ' is-dropping' : ''}`} ref={surface}>
        {files.length > 0 ? (
          <div className="attach-row">
            {files.map((item) => (
              <span key={`${item.fileName}-${item.size}`} className="attach-chip">
                {Icon.paperclip}
                <span className="attach-name">{item.fileName}</span>
                <button
                  type="button"
                  aria-label={`Remove ${item.fileName}`}
                  onClick={() => setFiles((prev) => prev.filter((fileItem) => fileItem !== item))}
                >
                  {Icon.close}
                </button>
              </span>
            ))}
          </div>
        ) : null}

        <div className="composer-row">
          <input
            ref={file}
            type="file"
            accept={ACCEPT_ATTRIBUTE}
            multiple
            hidden
            onChange={async (event) => {
              const list = Array.from(event.target.files ?? [])
              event.target.value = ''
              closeMenu()
              await addFiles(list)
            }}
          />

          <div className="plus-menu" ref={menu}>
            <button
              type="button"
              className={`plus-trigger${menuOpen ? ' is-open' : ''}`}
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              aria-label="Attach a file or change the agent, model or trace level"
              onClick={() => {
                setMenuOpen((value) => !value)
                setPane('root')
                setQuery('')
              }}
            >
              {Icon.plus}
            </button>
            {menuOpen ? (
              <div className="plus-popover" role="menu">
                <label className="plus-search">
                  <span className="sr-only">Search agents, models and settings</span>
                  <input
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                    placeholder="Search agents, models, settings…"
                    autoFocus
                  />
                </label>

                {pane !== 'root' && !searching ? (
                  <button type="button" className="plus-back" onClick={() => setPane('root')}>
                    {Icon.back}
                    {PANE_TITLES[pane]}
                  </button>
                ) : null}

                {pane === 'root' && !searching ? (
                  <div className="plus-list">
                    <div className="plus-section">
                      <MenuRow
                        icon={Icon.paperclip}
                        label="Upload file"
                        description="Images or PDF, up to 5 files"
                        onClick={() => file.current?.click()}
                      />
                    </div>
                    <div className="plus-section">
                      <MenuRow
                        icon={Icon.agent}
                        label="Agent"
                        description="Who answers and which tools it has"
                        value={agent || 'Default'}
                        more
                        onClick={() => setPane('agent')}
                      />
                      <MenuRow
                        icon={Icon.model}
                        label="Model"
                        description="The model this chat runs on"
                        value={modelLabel}
                        more
                        onClick={() => setPane('model')}
                      />
                    </div>
                    <div className="plus-section">
                      <MenuRow
                        icon={Icon.mcp}
                        label="MCP servers"
                        description="External tool servers for this turn"
                        value={selectedMcp.length ? `${selectedMcp.length} on` : 'None'}
                        more
                        onClick={() => setPane('mcp')}
                      />
                      <MenuRow
                        icon={Icon.trace}
                        label="Trace level"
                        description="How much of the agent's work is shown"
                        value={TRACE_OPTIONS.find((item) => item.id === traceMode)?.label ?? 'Overview'}
                        more
                        onClick={() => setPane('trace')}
                      />
                    </div>
                  </div>
                ) : null}

                {(pane === 'agent' || searching) && agentHits.length ? (
                  <div className="plus-list">
                    {searching ? <div className="plus-heading">Agents</div> : null}
                    {agentHits.map((item) => (
                      <MenuRow
                        key={item.name}
                        label={item.name}
                        description={item.description || undefined}
                        selected={item.name === agent}
                        onClick={() => {
                          onAgent(item.name)
                          closeMenu()
                        }}
                      />
                    ))}
                  </div>
                ) : null}

                {(pane === 'model' || searching) && modelHits.length ? (
                  <div className="plus-list">
                    {searching ? <div className="plus-heading">Models</div> : null}
                    {modelHits.map((item) => (
                      <MenuRow
                        key={item.id}
                        label={item.id}
                        description={item.provider || undefined}
                        value={item.contextWindow ? `${Math.round(item.contextWindow / 1000)}K` : undefined}
                        selected={item.id === model}
                        onClick={() => {
                          onModel(item.id)
                          closeMenu()
                        }}
                      />
                    ))}
                  </div>
                ) : null}

                {(pane === 'mcp' || searching) && mcpHits.length ? (
                  <div className="plus-list">
                    {searching ? <div className="plus-heading">MCP servers</div> : null}
                    {mcpHits.map((item) => {
                      const on = selectedMcp.includes(item.name)
                      return (
                        <MenuRow
                          key={item.name}
                          label={item.name}
                          description={item.status === 'connected' ? 'Connected' : 'Not connected'}
                          value={on ? 'On' : 'Off'}
                          selected={on}
                          onClick={() => onToggleMcp(item.name)}
                        />
                      )
                    })}
                  </div>
                ) : null}

                {(pane === 'trace' || searching) && traceHits.length ? (
                  <div className="plus-list">
                    {searching ? <div className="plus-heading">Trace level</div> : null}
                    {traceHits.map((item) => (
                      <MenuRow
                        key={item.id}
                        label={item.label}
                        description={item.description}
                        selected={item.id === traceMode}
                        onClick={() => {
                          onTraceMode(item.id)
                          closeMenu()
                        }}
                      />
                    ))}
                  </div>
                ) : null}

                {searching && !agentHits.length && !modelHits.length && !mcpHits.length && !traceHits.length ? (
                  <div className="plus-empty">Nothing matches “{query.trim()}”</div>
                ) : null}
              </div>
            ) : null}
          </div>

          <textarea
            ref={area}
            rows={1}
            value={text}
            onChange={(event) => setText(event.target.value)}
            onPaste={(event) => {
              const pasted = Array.from(event.clipboardData?.files ?? [])
              if (!pasted.length) return
              event.preventDefault()
              void addFiles(pasted)
            }}
            onKeyDown={(event) => {
              if (event.key === 'Escape' && improving) {
                event.preventDefault()
                cancelImprove()
                return
              }
              if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault()
                submit()
              }
            }}
            placeholder={busy ? 'Type your next message…' : 'Ask anything…'}
          />

          <div className="composer-actions">
            <div className="model-menu" ref={modelMenu}>
              <button
                type="button"
                className={`model-trigger${modelMenuOpen ? ' is-open' : ''}`}
                aria-haspopup="menu"
                aria-expanded={modelMenuOpen}
                title={`Model: ${modelLabel}`}
                onClick={() => setModelMenuOpen((value) => !value)}
              >
                <span>{modelLabel}</span>
                {Icon.caret}
              </button>
              {modelMenuOpen ? (
                <div className="model-popover" role="menu">
                  {models.length === 0 ? <div className="plus-empty">No models configured</div> : null}
                  {models.map((item) => (
                    <MenuRow
                      key={item.id}
                      label={item.id}
                      description={item.provider || undefined}
                      value={item.contextWindow ? `${Math.round(item.contextWindow / 1000)}K` : undefined}
                      selected={item.id === model}
                      onClick={() => {
                        onModel(item.id)
                        setModelMenuOpen(false)
                      }}
                    />
                  ))}
                </div>
              ) : null}
            </div>
            <ContextUsage />
            <SendButton
              disabled={!text.trim() && files.length === 0}
              busy={busy}
              onSend={submit}
              onStop={onStop}
            />
          </div>
        </div>
      </div>

      <div className="composer-foot">
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
        <span className="composer-hint">Enter sends · Shift+Enter adds a line</span>
      </div>

      {dropping ? (
        <div className="drop-veil" aria-hidden="true">
          <span>Drop images or PDFs to attach</span>
        </div>
      ) : null}
    </footer>
  )
})
