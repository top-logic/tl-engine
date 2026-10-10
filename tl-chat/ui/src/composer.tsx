import { React } from 'tl-react-bridge'
import { cancelPromptImprovement, improvePrompt } from './api'
import type { ChatAddress } from './api'
import { ACCEPT_ATTRIBUTE, collectAttachments, dragHasFiles } from './attachments'
import { ContextUsage, formatTokens } from './context-usage'
import type { RunStore } from './store'
import type { bundle } from './i18n'
import { agentLabel, effortLabel, modelLabel } from './models'
import { ComposerNotice } from './notice'
import type { ComposerApi, ComposerMenuItem, ReferenceCandidate } from './extensions'
import { draftIsEmpty, type ReferenceToken } from './reference'
import { TokenInput, type TokenInputHandle } from './token-input'
import { PromptStack } from './prompts'
import { subagentsWaitingForUser } from './subagent-card'
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
  NoticeTone,
  SubagentActivity,
  TraceMode,
} from './types'

const { useEffect, useImperativeHandle, useMemo, useRef, useState } = React

/** Shown for a reference candidate the host gave no icon of its own. */
const DefaultSourceIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path d="M4 2.5h5.2L12 5.3v8.2H4zM9 2.6v3h3" />
  </svg>
)

type MenuPane = 'root' | 'agent' | 'model' | 'mcp' | 'trace' | 'effort'

type Labels = ReturnType<typeof bundle>

function traceOptions(labels: Labels): { id: TraceMode; label: string; description: string }[] {
  return [
    { id: 'none', label: labels.traceNone, description: labels.traceNoneHint },
    { id: 'summary', label: labels.traceSummary, description: labels.traceSummaryHint },
    { id: 'detailed', label: labels.traceDetailed, description: labels.traceDetailedHint },
  ]
}

function paneTitle(pane: Exclude<MenuPane, 'root'>, labels: Labels): string {
  switch (pane) {
    case 'agent':
      return labels.agent
    case 'model':
      return labels.model
    case 'mcp':
      return labels.mcpServers
    case 'effort':
      return labels.effort
    case 'trace':
      return labels.traceLevel
  }
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
  effort: (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M8 2.5a5.5 5.5 0 0 1 3 10.1V14H5v-1.4A5.5 5.5 0 0 1 8 2.5z" />
      <path d="M6.5 14h3" />
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
  labels,
  onSend,
  onStop,
}: {
  disabled: boolean
  busy: boolean
  labels: Labels
  onSend: () => void
  onStop: () => void
}) {
  return (
    <button
      type="button"
      className={`send-button${busy ? ' is-busy' : ''}`}
      disabled={!busy && disabled}
      onClick={busy ? onStop : onSend}
      aria-label={busy ? labels.stopRun : labels.send}
      title={busy ? labels.stopRun : labels.send}
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
  // The row is named by its label and value and described by its hint, rather than named by all
  // three run together: a screen reader then reads "Model, GPT 5.6 Luna" and the sentence after it.
  const id = React.useId()
  return (
    <button
      type="button"
      className={`plus-row${selected ? ' is-selected' : ''}`}
      aria-labelledby={value ? `${id}-label ${id}-value` : `${id}-label`}
      aria-describedby={description ? `${id}-hint` : undefined}
      onClick={onClick}
    >
      {icon ? (
        <span className="plus-ico" aria-hidden="true">
          {icon}
        </span>
      ) : (
        <span className="plus-ico" aria-hidden="true" />
      )}
      <strong className="plus-label" id={`${id}-label`}>
        {label}
      </strong>
      {value ? (
        <span className="plus-value" id={`${id}-value`}>
          {value}
        </span>
      ) : null}
      {description ? (
        <span className="plus-hint" id={`${id}-hint`}>
          {description}
        </span>
      ) : null}
      {more ? (
        <span className="plus-more" aria-hidden="true">
          {Icon.chevron}
        </span>
      ) : null}
    </button>
  )
}

/** Whether the plus menu has anything to offer. Used by {@code <chat-conversation/>} when every picker is off. */
export function plusMenuVisible(flags: {
  attachmentsEnabled: boolean
  agentPicker: boolean
  modelPicker: boolean
  thinking: boolean
  mcp: boolean
  tracePicker: boolean
  extraMenuItems?: { length: number }
}): boolean {
  return (
    flags.attachmentsEnabled ||
    flags.agentPicker ||
    flags.modelPicker ||
    flags.thinking ||
    flags.mcp ||
    flags.tracePicker ||
    (flags.extraMenuItems?.length ?? 0) > 0
  )
}

export const Composer = React.memo(
  React.forwardRef(function Composer({
  at,
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
  effort,
  effortLevels,
  onEffort,
  labels,
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
  noticeDetail,
  noticeTone,
  onDismissNotice,
  onSend,
  onStop,
  onDraft,
  extraMenuItems,
  referenceCandidates,
  attachmentsEnabled = true,
  agentPicker = true,
  modelPicker = true,
  thinking = true,
  mcp = true,
  tracePicker = true,
  autoApproveSwitch = true,
  enterHint = true,
  placeholder,
  runs,
}: {
  /** The conversation this composer belongs to - its window and its chat id. */
  at: ChatAddress
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
  /** Effort level in force, empty when none is chosen or the model offers none. */
  effort: string
  /** Levels the selected model offers; empty means the setting is not offered at all. */
  effortLevels: string[]
  onEffort: (level: string) => void
  labels: Labels
  autoApprove: boolean
  onToggleAutoApprove: () => void
  pendingApprovals: ApprovalRequest[]
  pendingQuestions: InteractionRequest[]
  dispatch: React.Dispatch<ChatAction>
  promptImprovementEnabled: boolean
  subagents: SubagentActivity[]
  onCancelSubagent: (id: string, parentToolCallId?: string) => Promise<void>
  onStopSubagents: () => Promise<void>
  onLoadSubagentTranscript: (id: string) => Promise<ChatMessage[]>
  busy: boolean
  connection: ConnectionState
  notice: string
  /** What stands behind the notice technically, shown as its tooltip; empty when there is none. */
  noticeDetail: string
  /** Whether the strip reports a failure or only says what happened. */
  noticeTone: NoticeTone
  onDismissNotice: () => void
  onSend: (text: string, attachments: Attachment[]) => Promise<boolean>
  onStop: () => void
  /**
   * What stands unsent in the field, for a host that follows the draft.
   *
   * `settled` is true for the two moments the host must not learn late: the field losing the focus,
   * and the send. Everything else is a keystroke, which the caller is free to let pause first.
   */
  onDraft?: (text: string, settled: boolean) => void
  extraMenuItems?: ComposerMenuItem[]
  referenceCandidates?: (query: string) => ReferenceCandidate[]
  /** Whether files can be attached. Default on, matching today's composer. */
  attachmentsEnabled?: boolean
  agentPicker?: boolean
  modelPicker?: boolean
  thinking?: boolean
  mcp?: boolean
  tracePicker?: boolean
  autoApproveSwitch?: boolean
  enterHint?: boolean
  /**
   * The host's own invitation for the empty field, replacing both framework variants.
   *
   * Idle only: while the agent works the field keeps the framework's "type your next message",
   * which is about the run rather than about the application (#420).
   */
  placeholder?: string
  /** The run store of the chat this composer belongs to; the context ring reads it. */
  runs?: RunStore
}, handle: React.Ref<ComposerApi>) {
  const [text, setText] = useState('')
  const [files, setFiles] = useState<Attachment[]>([])
  const [attachError, setAttachError] = useState('')
  const [dropping, setDropping] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)
  const [modelMenuOpen, setModelMenuOpen] = useState(false)
  // The compact dropdown beside the send button: effort and model, one level deep each.
  const [modelPane, setModelPane] = useState<'root' | 'effort' | 'model'>('root')
  const [pane, setPane] = useState<MenuPane>('root')
  const [query, setQuery] = useState('')
  /**
   * The draft the "improve" pause has elapsed for, or null while none has.
   *
   * The chip's visibility is *derived* from this and never stored: an effect that writes state on
   * every keystroke is a nested update per keystroke, and React counts fifty of those in a row
   * without an idle frame as "maximum update depth exceeded" - which typing at full speed reaches
   * (#419). Written once by the timer below and by nothing else; a keystroke hides the chip by
   * moving `text` away from it, with no render of its own.
   */
  const [improvePausedFor, setImprovePausedFor] = useState<string | null>(null)
  const [improving, setImproving] = useState(false)
  const [improveError, setImproveError] = useState('')
  const [improveUnchanged, setImproveUnchanged] = useState(false)
  const [undoDraft, setUndoDraft] = useState<string | null>(null)
  /** The "@…" being typed, or null when the picker is closed. */
  const [mention, setMention] = useState<string | null>(null)
  const [mentionIndex, setMentionIndex] = useState(0)
  const area = useRef<TokenInputHandle>(null)
  const file = useRef<HTMLInputElement>(null)
  const menu = useRef<HTMLDivElement>(null)
  const modelMenu = useRef<HTMLDivElement>(null)
  const surface = useRef<HTMLDivElement>(null)
  const improveId = useRef<string | null>(null)
  const abort = useRef<AbortController | null>(null)
  const showPlus = plusMenuVisible({
    attachmentsEnabled,
    agentPicker,
    modelPicker,
    thinking,
    mcp,
    tracePicker,
    extraMenuItems,
  })
  const showModelMenu = modelPicker || thinking

  useEffect(() => {
    if (!promptImprovementEnabled || improving || draftIsEmpty(text)) return
    const timer = window.setTimeout(() => setImprovePausedFor(text), 700)
    return () => window.clearTimeout(timer)
  }, [improving, promptImprovementEnabled, text])

  // The pause is over and it was this sentence the user stopped on. Returning to a sentence the
  // pause already elapsed for - a character typed and taken back again - offers the chip at once
  // rather than waiting another 700 ms, which is the same answer to the same draft.
  const improveVisible = improvePausedFor === text

  useEffect(() => {
    if (!menuOpen && !modelMenuOpen) return
    const onDoc = (event: MouseEvent) => {
      const target = event.target as Node
      if (menuOpen && !menu.current?.contains(target)) {
        setMenuOpen(false)
        setPane('root')
        setQuery('')
      }
      if (modelMenuOpen && !modelMenu.current?.contains(target)) {
        setModelMenuOpen(false)
        setModelPane('root')
      }
    }
    // Escape closes one level at a time, and closing the menu hands the focus back to the button
    // that opened it - otherwise the focus falls to the document and the next Tab starts over.
    const onKey = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      if (modelMenuOpen) {
        if (modelPane !== 'root') {
          setModelPane('root')
        } else {
          setModelMenuOpen(false)
          modelMenu.current?.querySelector<HTMLButtonElement>('.model-trigger')?.focus()
        }
        return
      }
      if (pane !== 'root') {
        setPane('root')
      } else {
        setMenuOpen(false)
        menu.current?.querySelector<HTMLButtonElement>('.plus-trigger')?.focus()
      }
    }
    document.addEventListener('mousedown', onDoc)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDoc)
      document.removeEventListener('keydown', onKey)
    }
  }, [menuOpen, modelMenuOpen, modelPane, pane])

  const addFiles = async (incoming: File[]) => {
    if (!attachmentsEnabled) return
    const result = await collectAttachments(incoming, files, labels)
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
    if (!attachmentsEnabled) return
    const root = surface.current?.closest('.tl-cockpit, .tl-chat-conversation') as HTMLElement | null
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
  // Searched by what is on screen and by the identifier, because both are things a user types: the
  // one they read in the row, and the one they know from the configuration.
  const agentHits = agentPicker
    ? agents.filter(
        (item) =>
          !q ||
          item.name.toLocaleLowerCase().includes(q) ||
          (item.label ?? '').toLocaleLowerCase().includes(q) ||
          (item.summary ?? '').toLocaleLowerCase().includes(q),
      )
    : []
  const modelHits = modelPicker
    ? models.filter(
        (item) => !q || item.id.toLocaleLowerCase().includes(q) || (item.provider ?? '').toLocaleLowerCase().includes(q),
      )
    : []
  const mcpHits = mcp ? mcpServers.filter((item) => !q || item.name.toLocaleLowerCase().includes(q)) : []
  const traces = traceOptions(labels)
  const traceHits = tracePicker
    ? traces.filter(
        (item) => !q || item.label.toLocaleLowerCase().includes(q) || item.description.toLocaleLowerCase().includes(q),
      )
    : []
  const effortHits = thinking ? effortLevels.filter((level) => !q || level.includes(q)) : []
  // With a search term the root pane is a result list across all four sections, as in a command
  // palette: typing "opus" must find the model without first walking into the model pane.
  const searching = q.length > 0

  /** What a host does to the composer: from the `+` menu, from the "@" picker, from its own pane. */
  const composerApi: ComposerApi = useMemo(
    () => ({
      insertReference: (token: ReferenceToken) => area.current?.insertReference(token),
      setDraft: (draft: string) => area.current?.setDraft(draft),
      focus: () => area.current?.focus(),
    }),
    [],
  )
  useImperativeHandle(handle, () => composerApi, [composerApi])

  // Whatever changed the text - a keystroke, an inserted reference, an improved prompt, a host's
  // own write - the host hears the same thing. Blur and send report it again, settled.
  useEffect(() => {
    onDraft?.(text, false)
  }, [text, onDraft])

  const mentionQuery = mention == null ? '' : mention.replace(/^@/, '')
  const candidates = useMemo(
    () => (mention == null ? [] : (referenceCandidates?.(mentionQuery) ?? []).slice(0, 8)),
    [mention, mentionQuery, referenceCandidates],
  )

  const takeCandidate = (candidate: ReferenceCandidate) => {
    // The "@query" the user typed is replaced by the token it stood for.
    area.current?.replaceBefore(mention?.length ?? 0, '')
    area.current?.insertReference(candidate.token)
    setMention(null)
  }

  const submit = () => {
    const trimmed = text.trim()
    if (draftIsEmpty(text) && files.length === 0) return
    // Before the send, not after: what the host reads must be the sentence that is going.
    onDraft?.(text, true)
    const sentFiles = files
    void onSend(trimmed, sentFiles).then((sent) => {
      if (!sent) return
      setText('')
      setFiles([])
      setUndoDraft(null)
      setMention(null)
    })
  }

  const runImprove = async () => {
    const draft = text.trim()
    if (draftIsEmpty(text) || improving) return
    const requestId = crypto.randomUUID()
    improveId.current = requestId
    abort.current?.abort()
    abort.current = new AbortController()
    setImproving(true)
    setImproveError('')
    setImproveUnchanged(false)
    try {
      const result = await improvePrompt(at, draft, requestId, abort.current.signal)
      if (improveId.current !== requestId) return
      const improvedText = result.improvedText || result.text || ''
      if (result.status === 'completed' && improvedText) {
        if (improvedText.trim() === draft) {
          // The server hands the draft back when there is nothing to rewrite - a confirmation, a
          // single word, a prompt already fine. Saying so beats silently replacing text with itself.
          setImproveUnchanged(true)
          window.setTimeout(() => setImproveUnchanged(false), 4000)
        } else {
          setUndoDraft(draft)
          setText(improvedText)
        }
        area.current?.focus()
      } else if (result.status !== 'cancelled' && result.status !== 'stale') {
        setImproveError(labels.improveFailed)
      }
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') return
      setImproveError(labels.improveFailed)
    } finally {
      if (improveId.current === requestId) setImproving(false)
    }
  }

  const cancelImprove = () => {
    const requestId = improveId.current
    abort.current?.abort()
    if (requestId) void cancelPromptImprovement(at, requestId)
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

  // The composer says what a turn will actually run with: the model, and the effort when one is set.
  const modelName = model ? modelLabel(model) : labels.model
  const triggerLabel = effort ? `${modelName} · ${effortLabel(effort, labels)}` : modelName
  const defaultEffort = models.find((item) => item.id === model)?.defaultEffort ?? ''

  // Subagents whose question or approval is open: said before the working chip, because it is the
  // one thing about them that needs the user.
  const waitingSubagents = subagentsWaitingForUser([...pendingApprovals, ...pendingQuestions])
  const waitingForYou = waitingSubagents.size

  return (
    <footer className={`composer${dropping ? ' is-dropping' : ''}`}>
      <div className="composer-overlays">
        <PromptStack
          approvals={pendingApprovals}
          questions={pendingQuestions}
          at={at}
          labels={labels}
          dispatch={dispatch}
        />
        <div className="composer-chips">
          {waitingForYou > 0 ? (
            <span className="overlay-chip is-waiting" role="status">
              <span className="waiting-dot" aria-hidden="true" />
              {labels.subagentsWaitingForYou(waitingForYou)}
            </span>
          ) : null}
          <SubagentActivityPanel
            subagents={subagents}
            waiting={waitingSubagents}
            labels={labels}
            onCancel={onCancelSubagent}
            onStopAll={onStopSubagents}
            onLoadTranscript={onLoadSubagentTranscript}
          />
          {connection === 'reconnecting' ? (
            <span className="overlay-chip is-muted" role="status">
              <span className="chip-spinner" aria-hidden="true" />
              {labels.reconnecting}
            </span>
          ) : null}
          {promptImprovementEnabled && improving ? (
            <button type="button" className="overlay-chip" onClick={cancelImprove}>
              <span className="chip-spinner" aria-hidden="true" />
              {labels.improving}
              <span>{labels.cancel}</span>
            </button>
          ) : null}
          {promptImprovementEnabled && !improving && improveUnchanged ? (
            <span className="overlay-chip is-muted" role="status">
              {labels.improveNoChanges}
            </span>
          ) : null}
          {promptImprovementEnabled && !improving && undoDraft != null ? (
            <button type="button" className="overlay-chip" onClick={undoImprove}>
              {Icon.undo}
              {labels.undoImprovement}
            </button>
          ) : null}
          {promptImprovementEnabled && !improving && !improveUnchanged && undoDraft == null && improveVisible ? (
            <button type="button" className="overlay-chip is-accent" onClick={() => void runImprove()}>
              {Icon.spark}
              {labels.improvePrompt}
            </button>
          ) : null}
          {improveError ? (
            <button type="button" className="overlay-chip is-error" onClick={() => void runImprove()}>
              {improveError} · {labels.retry}
            </button>
          ) : null}
        </div>
      </div>

      {notice || attachError ? (
        <ComposerNotice
          text={notice || attachError}
          detail={notice ? noticeDetail : ''}
          tone={attachError && !notice ? 'warn' : noticeTone}
          labels={labels}
          onDismiss={() => {
            onDismissNotice()
            setAttachError('')
          }}
        />
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
                  aria-label={labels.removeFile(item.fileName)}
                  onClick={() => setFiles((prev) => prev.filter((fileItem) => fileItem !== item))}
                >
                  {Icon.close}
                </button>
              </span>
            ))}
          </div>
        ) : null}

        {/*
          Two rows, as the layout reference has them: the sentence on its own full width, the
          controls beneath. One row made the text column collapse to 45 px beside a document
          pane, which broke German compounds over twenty lines.
        */}
        <div className="composer-input">
          <TokenInput
            ref={area}
            value={text}
            onBlur={() => onDraft?.(text, true)}
            onChange={(next) => {
              setText(next)
              // Reading the word under the caret after the change, so "@" opens the picker on the
              // keystroke that typed it and closes it again when the "@" is deleted.
              const word = area.current?.wordBeforeCaret() ?? ''
              if (word.startsWith('@') && referenceCandidates) {
                setMention(word)
                setMentionIndex(0)
              } else {
                setMention(null)
              }
            }}
            onPasteFiles={(pasted) => void addFiles(pasted)}
            ariaLabel={placeholder || labels.askAnything}
            placeholder={
              // The host's invitation is the *idle* one. "Type your next message" is about the run,
              // not about this application, so a host that replaced it would be inviting the user
              // to start something that is already under way (#420).
              busy
                ? labels.typeNext
                : placeholder || (referenceCandidates ? labels.askAnythingWithAt : labels.askAnything)
            }
            onKeyDown={(event) => {
              if (event.key === 'Escape' && improving) {
                event.preventDefault()
                cancelImprove()
                return
              }
              if (mention != null && candidates.length > 0) {
                if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
                  event.preventDefault()
                  setMentionIndex((current) => {
                    const next = current + (event.key === 'ArrowDown' ? 1 : -1)
                    return (next + candidates.length) % candidates.length
                  })
                  return
                }
                if (event.key === 'Enter' || event.key === 'Tab') {
                  event.preventDefault()
                  takeCandidate(candidates[mentionIndex] ?? candidates[0])
                  return
                }
              }
              if (event.key === 'Escape' && mention != null) {
                event.preventDefault()
                setMention(null)
                return
              }
              if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault()
                submit()
              }
            }}
          />
          {mention != null && candidates.length > 0 ? (
            <div className="mention-popover" role="listbox" aria-label={labels.addSource}>
              {candidates.map((candidate, index) => (
                <button
                  type="button"
                  key={candidate.id}
                  role="option"
                  aria-selected={index === mentionIndex}
                  className={`mention-row${index === mentionIndex ? ' is-active' : ''}`}
                  onMouseEnter={() => setMentionIndex(index)}
                  onMouseDown={(event) => {
                    // mousedown, not click: the editor must not lose its caret before the insert.
                    event.preventDefault()
                    takeCandidate(candidate)
                  }}
                >
                  <span className="mention-icon" aria-hidden="true">
                    {candidate.icon ?? DefaultSourceIcon}
                  </span>
                  <span className="mention-label">{candidate.label}</span>
                  {candidate.hint ? <span className="mention-hint">{candidate.hint}</span> : null}
                </button>
              ))}
            </div>
          ) : null}
        </div>

        <div className="composer-row">
          {attachmentsEnabled ? (
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
          ) : null}

          {showPlus ? (
          <div className="plus-menu" ref={menu}>
            <button
              type="button"
              className={`plus-trigger${menuOpen ? ' is-open' : ''}`}
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              aria-label={labels.plusMenuLabel}
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
                  <span className="sr-only">{labels.searchMenuLabel}</span>
                  <input
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                    placeholder={labels.searchMenu}
                    autoFocus
                  />
                </label>

                {pane !== 'root' && !searching ? (
                  <button type="button" className="plus-back" onClick={() => setPane('root')}>
                    {Icon.back}
                    {paneTitle(pane, labels)}
                  </button>
                ) : null}

                {pane === 'root' && !searching ? (
                  <div className="plus-list">
                    <div className="plus-section">
                      {attachmentsEnabled ? (
                        <MenuRow
                          icon={Icon.paperclip}
                          label={labels.uploadFile}
                          description={labels.uploadFileHint}
                          onClick={() => file.current?.click()}
                        />
                      ) : null}
                      {extraMenuItems?.map((item) => (
                        <MenuRow
                          key={item.id}
                          icon={item.icon}
                          label={item.label}
                          description={item.description}
                          onClick={() => {
                            item.action(composerApi)
                            closeMenu()
                          }}
                        />
                      ))}
                    </div>
                    <div className="plus-section">
                      {agentPicker ? (
                        <MenuRow
                          icon={Icon.agent}
                          label={labels.agent}
                          description={labels.agentHint}
                          value={agent ? agentLabel(agent, agents) : labels.defaultValue}
                          more
                          onClick={() => setPane('agent')}
                        />
                      ) : null}
                      {modelPicker ? (
                        <MenuRow
                          icon={Icon.model}
                          label={labels.model}
                          description={labels.modelHint}
                          value={modelName}
                          more
                          onClick={() => setPane('model')}
                        />
                      ) : null}
                      {thinking && effortLevels.length ? (
                        <MenuRow
                          icon={Icon.effort}
                          label={labels.effort}
                          description={labels.effortHint}
                          value={effort ? effortLabel(effort, labels) : labels.defaultValue}
                          more
                          onClick={() => setPane('effort')}
                        />
                      ) : null}
                    </div>
                    <div className="plus-section">
                      {mcp ? (
                        <MenuRow
                          icon={Icon.mcp}
                          label={labels.mcpServers}
                          description={labels.mcpHint}
                          value={selectedMcp.length ? labels.enabledCount(selectedMcp.length) : labels.none}
                          more
                          onClick={() => setPane('mcp')}
                        />
                      ) : null}
                      {tracePicker ? (
                        <MenuRow
                          icon={Icon.trace}
                          label={labels.traceLevel}
                          description={labels.traceHint}
                          value={traces.find((item) => item.id === traceMode)?.label ?? labels.traceSummary}
                          more
                          onClick={() => setPane('trace')}
                        />
                      ) : null}
                    </div>
                  </div>
                ) : null}

                {(pane === 'agent' || searching) && agentHits.length ? (
                  <div className="plus-list">
                    {searching ? <div className="plus-heading">{labels.agent}</div> : null}
                    {agentHits.map((item) => (
                      <MenuRow
                        key={item.name}
                        label={agentLabel(item.name, agents)}
                        description={item.summary || undefined}
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
                    {searching ? <div className="plus-heading">{labels.model}</div> : null}
                    {modelHits.map((item) => (
                      <MenuRow
                        key={item.id}
                        label={modelLabel(item.id)}
                        description={item.id}
                        value={item.contextWindow ? formatTokens(item.contextWindow) : undefined}
                        selected={item.id === model}
                        onClick={() => {
                          onModel(item.id)
                          closeMenu()
                        }}
                      />
                    ))}
                  </div>
                ) : null}

                {(pane === 'effort' || searching) && effortHits.length ? (
                  <div className="plus-list">
                    {searching ? <div className="plus-heading">{labels.effort}</div> : null}
                    {effortHits.map((level) => (
                      <MenuRow
                        key={level}
                        label={effortLabel(level, labels)}
                        description={level === defaultEffort ? labels.defaultValue : undefined}
                        selected={level === effort}
                        onClick={() => {
                          onEffort(level === effort ? '' : level)
                          closeMenu()
                        }}
                      />
                    ))}
                  </div>
                ) : null}

                {pane === 'mcp' && !mcpServers.length ? (
                  <div className="plus-empty">{labels.noMcpServers}</div>
                ) : null}

                {(pane === 'mcp' || searching) && mcpHits.length ? (
                  <div className="plus-list">
                    {searching ? <div className="plus-heading">{labels.mcpServers}</div> : null}
                    {mcpHits.map((item) => {
                      const on = selectedMcp.includes(item.name)
                      return (
                        <MenuRow
                          key={item.name}
                          label={item.name}
                          description={item.status === 'connected' ? labels.connected : labels.notConnected}
                          value={on ? labels.on : labels.off}
                          selected={on}
                          onClick={() => onToggleMcp(item.name)}
                        />
                      )
                    })}
                  </div>
                ) : null}

                {(pane === 'trace' || searching) && traceHits.length ? (
                  <div className="plus-list">
                    {searching ? <div className="plus-heading">{labels.traceLevel}</div> : null}
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

                {searching &&
                !agentHits.length &&
                !modelHits.length &&
                !mcpHits.length &&
                !traceHits.length &&
                !effortHits.length ? (
                  <div className="plus-empty">{labels.nothingMatches(query.trim())}</div>
                ) : null}
              </div>
            ) : null}
          </div>
          ) : null}

          <div className="composer-actions">
            {showModelMenu ? (
            <div className="model-menu" ref={modelMenu}>
              <button
                type="button"
                className={`model-trigger${modelMenuOpen ? ' is-open' : ''}`}
                aria-haspopup="menu"
                aria-expanded={modelMenuOpen}
                aria-label={`${labels.model}: ${triggerLabel}`}
                title={`${labels.model}: ${triggerLabel}`}
                onClick={() => {
                  setModelMenuOpen((value) => !value)
                  setModelPane('root')
                }}
              >
                <span>{triggerLabel}</span>
                {Icon.caret}
              </button>
              {modelMenuOpen ? (
                <div className="model-popover" role="menu">
                  {modelPane === 'root' ? (
                    <div className="plus-list">
                      {/* Offered only where the provider accepts it - see ModelOptionDto.effortLevels. */}
                      {thinking && effortLevels.length ? (
                        <MenuRow
                          label={labels.effort}
                          value={effort ? effortLabel(effort, labels) : labels.defaultValue}
                          more
                          onClick={() => setModelPane('effort')}
                        />
                      ) : null}
                      {modelPicker ? (
                        <MenuRow
                          label={labels.model}
                          value={modelName}
                          more
                          onClick={() => setModelPane('model')}
                        />
                      ) : null}
                    </div>
                  ) : null}
                  {modelPane !== 'root' ? (
                    <button type="button" className="plus-back" onClick={() => setModelPane('root')}>
                      {Icon.back}
                      {modelPane === 'effort' ? labels.effort : labels.model}
                    </button>
                  ) : null}
                  {modelPane === 'effort' ? (
                    <div className="plus-list">
                      {effortLevels.map((level) => (
                        <MenuRow
                          key={level}
                          label={effortLabel(level, labels)}
                          description={level === defaultEffort ? labels.defaultValue : undefined}
                          selected={level === effort}
                          onClick={() => {
                            onEffort(level === effort ? '' : level)
                            setModelMenuOpen(false)
                          }}
                        />
                      ))}
                    </div>
                  ) : null}
                  {modelPane === 'model' ? (
                    <div className="plus-list">
                      {models.length === 0 ? <div className="plus-empty">{labels.none}</div> : null}
                      {models.map((item) => (
                        <MenuRow
                          key={item.id}
                          label={modelLabel(item.id)}
                          description={item.id}
                          value={item.contextWindow ? formatTokens(item.contextWindow) : undefined}
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
              ) : null}
            </div>
            ) : null}
            <ContextUsage labels={labels} runs={runs} />
            <SendButton
              disabled={!text.trim() && files.length === 0}
              busy={busy}
              labels={labels}
              onSend={submit}
              onStop={onStop}
            />
          </div>
        </div>
      </div>

      <div className="composer-foot">
        {autoApproveSwitch ? (
          <button
            type="button"
            className={`approve-switch${autoApprove ? ' is-on' : ''}`}
            role="switch"
            aria-checked={autoApprove}
            aria-label={labels.autoApprove}
            title={labels.autoApprove}
            onClick={onToggleAutoApprove}
          >
            <span className="approve-switch-track" aria-hidden="true">
              <span className="approve-switch-knob" />
            </span>
            <span className="approve-switch-label">{labels.autoApprove}</span>
          </button>
        ) : null}
        {enterHint ? (
          <span className="composer-hint" title={labels.enterSends}>
            <span className="composer-hint-long">{labels.enterSends}</span>
            <span className="composer-hint-short">{labels.enterSendsShort}</span>
          </span>
        ) : null}
      </div>

      {dropping ? (
        <div className="drop-veil" aria-hidden="true">
          <span>{labels.dropHint}</span>
        </div>
      ) : null}
    </footer>
  )
  }),
)
