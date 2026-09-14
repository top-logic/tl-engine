import { React, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import {
  autoTitle,
  cancelRun,
  cancelSubagent,
  createSession,
  deleteSession,
  fetchCapabilities,
  ForbiddenError,
  listSubagents,
  listSessions,
  loadSession,
  loadSubagentTranscript,
  openEventStream,
  regenerate,
  respond,
  sendFeedback,
  sendMessage,
  setApprovalMode,
  truncate,
  turnsJsonToMessages,
} from './api'
import { bundle, providerErrorText, toLanguage, type Language } from './i18n'
import { effortLevelsOf, modelsFor } from './models'
import { SessionRail } from './chrome'
import { Composer } from './composer'
import type { ChatExtensions, ComposerChip, PaneOpenOptions, RailSection } from './extensions'
import { hostRailSections } from './extensions'
import { parsePreferences, savePreferences } from './preferences'
import { ProgressSection } from './progress'
import { progressBadge, unseenBadge } from './progress-model'
import { RailToggle, SideRail, SideStrip, useCollapsedSections } from './rail'
import { chatReducer } from './reducer'
import { composeOutboundText } from './send-prefix'
import { SidePane } from './side-pane'
import {
  EMPTY_RUN,
  readActiveSession,
  readEffort,
  readTraceMode,
  runStore,
  writeActiveSession,
  writeEffort,
  writeTraceMode,
} from './store'
import { Thread } from './thread'
import {
  promptTokens,
  type AgentOption,
  type ApprovalOutcome,
  type ApprovalRequest,
  type Attachment,
  type Capabilities,
  type ChatMessage,
  type ConnectionState,
  type ContextComposition,
  type InteractionRequest,
  type MessageFeedback,
  type NoticeTone,
  type Rating,
  type SessionSummary,
  type SubagentActivity,
  type SubagentStatus,
  type SubagentStatusEvent,
  type TokenUsage,
  type TraceMode,
  type Citation,
} from './types'

const { useCallback, useEffect, useMemo, useReducer, useRef, useState } = React

/**
 * Without a frame for this long the stream counts as stale and is re-opened.
 *
 * The server sends a heartbeat every 15s when nothing else happens, so this allows two missed ones
 * before the user is told anything - a single slow flush must not flash a warning.
 */
const STALE_AFTER_MS = 40_000

/**
 * How long a fresh connection may stay silent before the user is told it is not answering.
 *
 * The server sends `connected` as its first frame, so a live stream clears this well inside it.
 */
const CONNECT_TIMEOUT_MS = 5_000

function parseJson<T>(raw: string, fallback: T): T {
  try {
    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

function parseTraceMode(value: unknown): TraceMode {
  return value === 'none' || value === 'detailed' || value === 'overview' ? value : 'overview'
}

function mergeSubagent(
  current: SubagentActivity[],
  event: SubagentStatusEvent,
  status: SubagentStatus,
): SubagentActivity[] {
  const index = current.findIndex((item) => item.subagentId === event.subagentId)
  const previous = index >= 0 ? current[index] : undefined
  const next: SubagentActivity = {
    subagentId: event.subagentId,
    targetAgentName: event.targetAgentName ?? previous?.targetAgentName ?? 'Subagent',
    status,
    detail: event.detail ?? previous?.detail ?? '',
    totalTokens: event.totalTokens ?? previous?.totalTokens ?? 0,
    durationMs: event.durationMs ?? previous?.durationMs ?? 0,
    hasTranscript: status === 'DONE' || previous?.hasTranscript === true,
  }
  if (index < 0) return [...current, next]
  return current.map((item, itemIndex) => (itemIndex === index ? next : item))
}

export function App({ controlId, extensions }: TLCellProps & { extensions?: ChatExtensions }) {
  const state = useTLState()
  const windowId = (state.windowId as string) || ''
  /** False in a session that is not signed in; the servlet refuses every call such a session makes. */
  const authenticated = state.authenticated !== false
  const userName = (state.userName as string) || 'User'
  const welcomeName = (state.welcomeName as string) || ''
  const configuredTraceMode = parseTraceMode(state.traceMode)
  const promptImprovementEnabled = state.promptImprovementEnabled === true
  // The chat speaks the language the rest of the application speaks: the user's own locale.
  const language: Language = toLanguage(state.language)
  const labels = useMemo(() => bundle(language), [language])
  const prefs = useMemo(() => parsePreferences(state), [state])
  // The stored choice wins over the configured default: the element's trace-mode is what a new user
  // starts with, not a setting that overrules what this user picked.
  const [traceMode, setTraceModeState] = useState<TraceMode>(() => readTraceMode() ?? configuredTraceMode)

  const [caps, setCaps] = useState<Capabilities>({ agents: [], models: [], mcpServers: [] })
  const [sessions, setSessions] = useState<SessionSummary[]>([])
  const [sessionsLoading, setSessionsLoading] = useState(true)
  const [sessionsError, setSessionsError] = useState('')
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [agent, setAgent] = useState('')
  const [model, setModel] = useState('')
  const [mcp, setMcp] = useState<string[]>([])
  const [mcpDirty, setMcpDirty] = useState(false)
  const [effort, setEffortState] = useState<string>(() => readEffort() ?? '')
  const [feedback, setFeedback] = useState<MessageFeedback[]>([])
  const [regenerating, setRegenerating] = useState(false)
  const [busy, setBusy] = useState(false)
  const [railOpen, setRailOpen] = useState(false)
  const [railCollapsed, setRailCollapsed] = useState(false)
  const [sideOpen, setSideOpen] = useState(() => prefs.railOpen)
  const [paneWidth, setPaneWidth] = useState(() => prefs.paneWidth)
  const [badgeSeen, setBadgeSeen] = useState(() => prefs.badgeSeen)
  const [paneContent, setPaneContent] = useState<React.ReactNode>(null)
  const [paneOptions, setPaneOptions] = useState<PaneOpenOptions | null>(null)
  const [hostChips, setHostChips] = useState<ComposerChip[]>(() => extensions?.composerAddons?.chips ?? [])
  const collapsedSections = useCollapsedSections(['context'])
  const [subagents, setSubagents] = useState<SubagentActivity[]>([])
  const [autoApprove, setAutoApprove] = useState(false)
  const [connection, setConnection] = useState<ConnectionState>('connecting')
  const [notice, setNotice] = useState('')
  /** What the provider itself said, kept out of the strip but reachable as its tooltip. */
  const [noticeDetail, setNoticeDetail] = useState('')
  /**
   * Whether the strip reports a failure or merely says what happened.
   *
   * "Abgebrochen." and "der Chat ist beschäftigt" are outcomes of what the user just did; wearing
   * the same warning accent as a provider failure made every line read as something going wrong.
   */
  const [noticeTone, setNoticeTone] = useState<NoticeTone>('warn')
  /** Whether the line now on screen must survive the next run start. */
  const noticeSticky = useRef(false)

  /**
   * Shows one line above the composer, and what stands behind it.
   *
   * Text and detail are set together so a later notice never inherits the tooltip of an earlier
   * one.
   */
  const showNotice = (text: string, detail = '', options: { tone?: NoticeTone; sticky?: boolean } = {}) => {
    setNotice(text)
    setNoticeDetail(detail)
    setNoticeTone(options.tone ?? 'warn')
    noticeSticky.current = text !== '' && options.sticky === true
  }

  /**
   * Takes the strip down as a run begins, unless what it says has not been readable yet.
   *
   * A run start used to clear it unconditionally, which is how "3 spätere Antworten entfernt" was
   * wiped in the same instant it was written: the send that follows an edit is what starts the run
   * the line was meant to survive. It survives exactly that one start, and then stays until the
   * user dismisses it, sends again, or a further run begins.
   */
  const clearNoticeOnRunStart = () => {
    if (noticeSticky.current) {
      noticeSticky.current = false
      return
    }
    showNotice('')
  }
  const [messages, dispatch] = useReducer(chatReducer, [])
  const assistantId = useRef<string | null>(null)
  /**
   * The assistant message this run streamed into, still reachable after the run has ended.
   *
   * The frames arrive as `text_delta …, turn_completed, run_completed, message_complete`, so by the
   * time the authoritative text lands, `assistantId` has already been cleared by the run's end.
   * Applying it needs an id that outlives that.
   */
  const lastAssistantId = useRef<string | null>(null)
  const sessionIdRef = useRef<string | null>(null)
  const messagesRef = useRef(messages)
  const agentRef = useRef(agent)
  const modelRef = useRef(model)
  const mcpRef = useRef(mcp)
  const mcpDirtyRef = useRef(mcpDirty)
  const effortRef = useRef(effort)
  const autoApproveRef = useRef(false)
  const runIdRef = useRef<string | null>(null)
  const chipsRef = useRef<ComposerChip[]>([])
  /** Set when the run ended because the user stopped it, so the provider error it causes is muted. */
  const cancelledRef = useRef(false)
  sessionIdRef.current = sessionId
  messagesRef.current = messages
  agentRef.current = agent
  modelRef.current = model
  mcpRef.current = mcp
  mcpDirtyRef.current = mcpDirty
  effortRef.current = effort
  autoApproveRef.current = autoApprove

  const chips = useMemo(() => {
    const fromHost = extensions?.composerAddons?.chips ?? []
    const extra = hostChips.filter((chip) => !fromHost.some((item) => item.id === chip.id))
    return [...fromHost, ...extra]
  }, [extensions?.composerAddons?.chips, hostChips])
  chipsRef.current = chips

  useEffect(() => {
    if (!authenticated || !windowId) return
    void savePreferences(windowId, { railOpen: sideOpen, paneWidth, badgeSeen })
  }, [authenticated, windowId, sideOpen, paneWidth, badgeSeen])

  const setTraceMode = useCallback((mode: TraceMode) => {
    setTraceModeState(mode)
    writeTraceMode(mode)
  }, [])

  useEffect(() => {
    if (!railOpen) return
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      setRailOpen(false)
      window.setTimeout(() => {
        document.querySelector<HTMLButtonElement>(`#${CSS.escape(controlId)} .rail-toggle`)?.focus()
      })
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [controlId, railOpen])

  const applyAgent = useCallback((name: string, agents: AgentOption[]) => {
    const found = agents.find((a) => a.name === name)
    setAgent(name)
    setMcp(found?.mcpServers ?? [])
    setMcpDirty(false)
    if (found?.model) setModel(found.model)
  }, [])

  useEffect(() => {
    // Nothing to ask for in a session the servlet answers with 401.
    if (!authenticated) return
    let cancelled = false
    ;(async () => {
      try {
        const [nextCaps, nextSessions] = await Promise.all([
          fetchCapabilities(windowId),
          listSessions(windowId),
        ])
        if (cancelled) return
        setCaps(nextCaps)
        setSessions(nextSessions)
        setSessionsError('')
        const initial =
          nextCaps.agents.find((a) => a.name === nextCaps.entryPointAgent)?.name ??
          nextCaps.agents[0]?.name ??
          ''
        if (initial) applyAgent(initial, nextCaps.agents)
        else if (nextCaps.models[0]) setModel(nextCaps.models[0].id)
      } catch (err) {
        console.error('[tl-chat-ui] capabilities', err)
        if (!cancelled) setSessionsError(labels.chatsLoadFailed)
      } finally {
        if (!cancelled) setSessionsLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [authenticated, windowId, applyAgent])

  // The ring must be readable before the first run, and must follow a model change at once, so the
  // window comes from the model catalogue until a run reports the one it really used.
  useEffect(() => {
    const known = caps.models.find((item) => item.id === model)
    if (!known?.contextWindow) return
    runStore.set((snapshot) => ({
      ...snapshot,
      model: model || snapshot.model,
      contextWindow: known.contextWindow ?? snapshot.contextWindow,
    }))
  }, [caps.models, model])

  // Narrowed to the agent the user has selected: the payload lists what any agent they may run
  // accepts, and offering a model that only another agent accepts is offering a refused turn.
  const offeredModels = useMemo(
    () => modelsFor(caps.models, caps.agents.find((item) => item.name === agent)?.model),
    [caps.models, caps.agents, agent],
  )

  // A model that cannot be asked to think harder must not carry a level: the provider would reject
  // it. Switching to such a model drops the choice; switching back leaves the user to pick again.
  const effortLevels = useMemo(() => effortLevelsOf(caps.models, model), [caps.models, model])
  useEffect(() => {
    setEffortState((current) => (current && !effortLevels.includes(current) ? '' : current))
  }, [effortLevels])
  useEffect(() => {
    runStore.set((snapshot) => ({ ...snapshot, effort }))
  }, [effort])

  const setEffort = useCallback((level: string) => {
    setEffortState(level)
    writeEffort(level || null)
  }, [])

  useEffect(() => {
    // An EventSource against a 401 reconnects for as long as the page is open; a session that is
    // not signed in opens none.
    if (!authenticated) return
    let closed = false
    let source: EventSource | null = null
    let staleTimer = 0
    let reopenTimer = 0
    let connectTimer = 0

    const finish = (error?: string, detail?: string, errorTone: NoticeTone = 'warn') => {
      if (assistantId.current) {
        dispatch({ type: 'FINALIZE_TEXT', id: assistantId.current })
        assistantId.current = null
      }
      if (runIdRef.current) {
        dispatch({ type: 'END_RUN', runId: runIdRef.current })
        runIdRef.current = null
      }
      if (error) showNotice(error, detail ?? '', { tone: errorTone })
      setBusy(false)
      runStore.set((s) => ({ ...s, busy: false }))
    }

    /**
     * A frame arrived: the stream is alive, and the stale clock starts again.
     *
     * Only ever called from the `open` handler and from real frames. Calling it because a
     * connection was *attempted* is what made the reconnect state lie: the chip appeared when the
     * stale timer fired and vanished a second later when the next attempt was made, whether or not
     * anything answered.
     */
    const markAlive = () => {
      setConnection('live')
      window.clearTimeout(connectTimer)
      window.clearTimeout(staleTimer)
      staleTimer = window.setTimeout(() => {
        setConnection('reconnecting')
        reopen()
      }, STALE_AFTER_MS)
    }

    const reopen = () => {
      if (closed) return
      source?.close()
      window.clearTimeout(reopenTimer)
      reopenTimer = window.setTimeout(connect, 1000)
    }

    const connect = () => {
      if (closed) return
      const es = openEventStream(windowId)
      source = es
      const on = (name: string, fn: (e: MessageEvent) => void) =>
        es.addEventListener(name, ((event: MessageEvent) => {
          markAlive()
          fn(event)
        }) as EventListener)

      const setTokens = (windowSize?: number, modelName?: string, usage?: TokenUsage) => {
        runStore.set((s) => ({
          ...s,
          contextWindow: windowSize || s.contextWindow,
          model: modelName || s.model,
          lastUsage: usage ?? s.lastUsage,
          measuredTokens: usage ? promptTokens(usage) || s.measuredTokens : s.measuredTokens,
        }))
      }

      es.addEventListener('open', markAlive)
      on('connected', () => undefined)
      on('heartbeat', () => undefined)

      on('run_started', (e) => {
        const data = parseJson<{ runId?: string; usedTokens?: number; contextWindow?: number; model?: string }>(
          e.data,
          {},
        )
        setBusy(true)
        clearNoticeOnRunStart()
        cancelledRef.current = false
        runIdRef.current = data.runId ?? crypto.randomUUID()
        dispatch({ type: 'START_RUN', runId: runIdRef.current })
        runStore.set((s) => ({
          ...s,
          busy: true,
          estimatedTokens: data.usedTokens ?? s.estimatedTokens,
          contextWindow: data.contextWindow || s.contextWindow,
          model: data.model || s.model,
        }))
      })
      on('turn_started', (e) => {
        const data = parseJson<{ usedTokens?: number; contextWindow?: number }>(e.data, {})
        runStore.set((s) => ({
          ...s,
          estimatedTokens: data.usedTokens ?? s.estimatedTokens,
          contextWindow: data.contextWindow || s.contextWindow,
        }))
        if (assistantId.current) dispatch({ type: 'FINALIZE_TEXT', id: assistantId.current })
        assistantId.current = crypto.randomUUID()
        lastAssistantId.current = assistantId.current
        dispatch({ type: 'ADD_ASSISTANT', id: assistantId.current, text: '', isStreaming: true })
      })
      on('context_composition', (e) => {
        const data = parseJson<Partial<ContextComposition>>(e.data, {})
        runStore.set((s) => ({
          ...s,
          composition: {
            systemPromptTokens: data.systemPromptTokens ?? 0,
            toolDefinitionTokens: data.toolDefinitionTokens ?? 0,
            messageTokens: data.messageTokens ?? 0,
            toolResultTokens: data.toolResultTokens ?? 0,
            toolCount: data.toolCount ?? 0,
          },
        }))
      })
      on('text_delta', (e) => {
        if (!assistantId.current) return
        const data = parseJson<{ text?: string }>(e.data, {})
        if (data.text) dispatch({ type: 'APPEND_TEXT', id: assistantId.current, delta: data.text })
      })
      on('thinking_delta', (e) => {
        const data = parseJson<{ text?: string }>(e.data, {})
        if (data.text) dispatch({ type: 'APPEND_THINKING', delta: data.text })
      })
      on('tool_started', (e) => {
        const data = parseJson<{ toolCallId?: string; toolName?: string; argsJson?: string }>(e.data, {})
        if (!data.toolCallId) return
        dispatch({
          type: 'ADD_TOOL',
          toolUseId: data.toolCallId,
          name: data.toolName ?? 'tool',
          input: parseToolInput(data.argsJson),
        })
      })
      on('tool_completed', (e) => {
        const data = parseJson<{ toolCallId?: string; result?: string; durationMs?: number }>(e.data, {})
        if (!data.toolCallId) return
        dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'done', durationMs: data.durationMs })
        dispatch({ type: 'ADD_TOOL_RESULT', toolUseId: data.toolCallId, output: data.result ?? '' })
      })
      on('tool_failed', (e) => {
        const data = parseJson<{ toolCallId?: string; errorMessage?: string; durationMs?: number }>(e.data, {})
        if (!data.toolCallId) return
        dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'error', durationMs: data.durationMs })
        dispatch({
          type: 'ADD_TOOL_RESULT',
          toolUseId: data.toolCallId,
          output: data.errorMessage ?? 'failed',
          isError: true,
        })
      })
      on('tool_denied', (e) => {
        const data = parseJson<{ toolCallId?: string; reason?: string }>(e.data, {})
        if (!data.toolCallId) return
        dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'error' })
        dispatch({
          type: 'ADD_TOOL_RESULT',
          toolUseId: data.toolCallId,
          output: data.reason ?? 'denied',
          isError: true,
        })
      })
      on('approval_mode', (e) => {
        const data = parseJson<{ mode?: string }>(e.data, {})
        setAutoApprove(data.mode === 'AUTO_APPROVE')
      })
      on('approval_required', (e) => {
        const data = parseJson<{
          approvalRequestId?: string
          toolCallId?: string
          toolName?: string
          argsJson?: string
          displayReason?: string
          toolMode?: string
          remainingMs?: number
        }>(e.data, {})
        if (!data.approvalRequestId) return
        if (autoApproveRef.current) {
          void respond(windowId, {
            requestId: data.approvalRequestId,
            response: 'APPROVED',
            toolName: data.toolName,
            argsJson: data.argsJson,
          })
          return
        }
        dispatch({
          type: 'ADD_APPROVAL',
          approvalRequestId: data.approvalRequestId,
          toolCallId: data.toolCallId ?? '',
          toolName: data.toolName ?? 'tool',
          argsJson: data.argsJson ?? '',
          displayReason: data.displayReason ?? '',
          toolMode: data.toolMode,
          remainingMs: data.remainingMs,
        })
      })
      on('approval_resolved', (e) => {
        const data = parseJson<{ approvalRequestId?: string; outcome?: string }>(e.data, {})
        if (!data.approvalRequestId) return
        dispatch({
          type: 'RESOLVE_APPROVAL',
          approvalRequestId: data.approvalRequestId,
          outcome: (data.outcome as ApprovalOutcome) ?? 'APPROVED',
        })
      })
      on('user_interaction', (e) => {
        const data = parseJson<{
          requestId?: string
          question?: string
          header?: string
          type?: 'text' | 'choice'
          options?: { label: string; description?: string }[]
          multiSelect?: boolean
          remainingMs?: number
        }>(e.data, {})
        if (!data.requestId) return
        dispatch({
          type: 'ADD_INTERACTION',
          requestId: data.requestId,
          question: data.question ?? '',
          header: data.header,
          interactionType: data.type,
          options: data.options,
          multiSelect: data.multiSelect,
          remainingMs: data.remainingMs,
        })
      })
      on('delegation_event', (e) => {
        const wrap = parseJson<{ eventType?: string; eventData?: string }>(e.data, {})
        const data = parseJson<{
          toolCallId?: string
          toolName?: string
          argsJson?: string
          result?: string
          errorMessage?: string
          durationMs?: number
          reason?: string
          approvalRequestId?: string
          displayReason?: string
          toolMode?: string
          remainingMs?: number
        }>(wrap.eventData ?? '', {})
        switch (wrap.eventType) {
          case 'tool_started': {
            if (!data.toolCallId) break
            dispatch({
              type: 'ADD_TOOL',
              toolUseId: data.toolCallId,
              name: data.toolName ?? 'tool',
              input: parseToolInput(data.argsJson),
              depth: 1,
            })
            break
          }
          case 'tool_completed':
            if (!data.toolCallId) break
            dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'done', durationMs: data.durationMs })
            dispatch({ type: 'ADD_TOOL_RESULT', toolUseId: data.toolCallId, output: data.result ?? '' })
            break
          case 'tool_failed':
            if (!data.toolCallId) break
            dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'error', durationMs: data.durationMs })
            dispatch({
              type: 'ADD_TOOL_RESULT',
              toolUseId: data.toolCallId,
              output: data.errorMessage ?? 'failed',
              isError: true,
            })
            break
          case 'tool_denied':
            if (!data.toolCallId) break
            dispatch({ type: 'UPDATE_TOOL', toolUseId: data.toolCallId, status: 'error' })
            dispatch({
              type: 'ADD_TOOL_RESULT',
              toolUseId: data.toolCallId,
              output: data.reason ?? 'denied',
              isError: true,
            })
            break
          case 'approval_required':
            if (!data.approvalRequestId) break
            if (autoApproveRef.current) {
              void respond(windowId, {
                requestId: data.approvalRequestId,
                response: 'APPROVED',
                toolName: data.toolName,
                argsJson: data.argsJson,
              })
              break
            }
            dispatch({
              type: 'ADD_APPROVAL',
              approvalRequestId: data.approvalRequestId,
              toolCallId: data.toolCallId ?? '',
              toolName: data.toolName ?? 'tool',
              argsJson: data.argsJson ?? '',
              displayReason: data.displayReason ?? '',
              toolMode: data.toolMode,
              remainingMs: data.remainingMs,
            })
            break
          default:
            break
        }
      })
      on('subagent_started', (e) => {
        const data = parseJson<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) setSubagents((items) => mergeSubagent(items, data, 'RUNNING'))
      })
      on('subagent_completed', (e) => {
        const data = parseJson<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) setSubagents((items) => mergeSubagent(items, data, 'DONE'))
      })
      on('subagent_failed', (e) => {
        const data = parseJson<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) setSubagents((items) => mergeSubagent(items, data, 'FAILED'))
      })
      on('subagent_cancelled', (e) => {
        const data = parseJson<SubagentStatusEvent>(e.data, { subagentId: '' })
        if (data.subagentId) setSubagents((items) => mergeSubagent(items, data, 'CANCELLED'))
      })
      on('compaction_started', (e) => {
        const data = parseJson<{ contextWindow?: number }>(e.data, {})
        // The measured figure describes a conversation that no longer exists; the next turn
        // reports the compacted one, and until then the ring falls back to the estimate.
        runStore.set((s) => ({ ...s, measuredTokens: 0, contextWindow: data.contextWindow || s.contextWindow }))
      })
      on('turn_completed', (e) => {
        const data = parseJson<{ turnUsage?: TokenUsage; contextWindow?: number }>(e.data, {})
        if (data.turnUsage) setTokens(data.contextWindow, undefined, data.turnUsage)
      })
      on('run_completed', (e) => {
        const data = parseJson<{ reason?: string }>(e.data, {})
        // A run the user stopped is not a failure: it ends quietly, in their language, and the
        // provider's own "Stream cancelled." that follows is swallowed below.
        if (data.reason === 'ABORTED') {
          cancelledRef.current = true
          finish(bundle(languageRef.current).cancelled, '', 'info')
        } else {
          finish()
        }
      })
      on('message_complete', (e) => {
        // The authoritative answer. The deltas were a live preview of it; applying it here is what
        // keeps the last fragment from being lost when it arrives after this event.
        const data = parseJson<{ contentText?: string; citations?: Citation[] }>(e.data, {})
        const target = assistantId.current ?? lastAssistantId.current
        if (target && data.contentText) {
          dispatch({ type: 'SET_TEXT', id: target, text: data.contentText, citations: data.citations })
        }
        finish()
      })
      on('provider_error', (e) => {
        if (cancelledRef.current) {
          // The cancel already said what happened; the provider's report of the closed stream adds
          // nothing and would say it as an error.
          cancelledRef.current = false
          return
        }
        const data = parseJson<{ errorMessage?: string; kind?: string }>(e.data, {})
        finish(providerErrorText(data.kind, languageRef.current), data.errorMessage ?? '')
      })

      es.onerror = () => {
        if (es.readyState === EventSource.CLOSED) {
          setConnection('reconnecting')
          reopen()
        }
      }

      // Not alive until something answers: if no frame arrives within this, say so and keep saying
      // it until one does.
      window.clearTimeout(connectTimer)
      connectTimer = window.setTimeout(() => setConnection('reconnecting'), CONNECT_TIMEOUT_MS)
    }

    connect()
    return () => {
      closed = true
      window.clearTimeout(staleTimer)
      window.clearTimeout(reopenTimer)
      window.clearTimeout(connectTimer)
      source?.close()
    }
  }, [authenticated, windowId])

  const languageRef = useRef(language)
  languageRef.current = language

  const refreshSessions = useCallback(async () => {
    try {
      setSessions(await listSessions(windowId))
      setSessionsError('')
    } catch {
      setSessionsError(labels.chatsRefreshFailed)
    }
  }, [labels, windowId])

  /**
   * Takes back a chat this send had just created, when the send produced nothing to keep.
   *
   * <p>
   * The chat comes into being before the request, because it is what the turn is written into. A
   * refused send leaves it empty, and an empty chat in the rail is a row the user has to notice and
   * tidy away - for a refusal they did not cause and cannot act on. Only a chat this very send
   * created is taken back: an existing conversation is never touched.
   * </p>
   */
  const discardEmptyChat = useCallback(
    async (createdHere: boolean, id: string) => {
      if (!createdHere || !id) return
      try {
        await deleteSession(windowId, id)
      } catch {
        // Best effort: an undeleted empty chat is untidy, not broken.
      }
      setSessionId('')
      sessionIdRef.current = ''
      dispatch({ type: 'RESET' })
      await refreshSessions()
    },
    [refreshSessions, windowId],
  )

  /**
   * The chat this window had open before it was reloaded, read once, before anything can overwrite
   * it.
   *
   * The effect below persists every change of the open chat, and on the first render that would be
   * the initial `null` - so the remembered id has to be taken during render, not in an effect.
   */
  const remembered = useRef<string | null>(readActiveSession())

  /** Whether the remembered chat has been dealt with; restoring happens once per mount. */
  const restoredSession = useRef(false)

  useEffect(() => {
    writeActiveSession(sessionId)
  }, [sessionId])

  const refreshSubagents = useCallback(async () => {
    try {
      setSubagents(await listSubagents(windowId))
    } catch {
      setSubagents([])
    }
  }, [windowId])

  const onSelectSession = useCallback(
    async (id: string) => {
      assistantId.current = null
      runIdRef.current = null
      setBusy(false)
      setRailOpen(false)
      setSubagents([])
      showNotice('')
      setFeedback([])
      // The model does not change when the chat does, so its window survives the reset; only what
      // the previous conversation measured is dropped.
      runStore.set((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
      const detail = await loadSession(windowId, id)
      setSessionId(id)
      dispatch({ type: 'REPLACE', messages: turnsJsonToMessages(detail.turnsJson) })
      // A re-opened session shows what its last request really cost, not a fresh estimate.
      runStore.set((snapshot) => ({
        ...snapshot,
        model: detail.lastModel || snapshot.model,
        contextWindow: detail.contextWindow || snapshot.contextWindow,
        lastUsage: detail.lastPromptUsage ?? snapshot.lastUsage,
        measuredTokens: promptTokens(detail.lastPromptUsage),
        composition: detail.lastComposition ?? undefined,
      }))
      setFeedback(detail.feedback ?? [])
      if (detail.lastEffort) setEffortState(detail.lastEffort)
      await refreshSubagents()
    },
    [windowId, refreshSubagents],
  )

  /**
   * Re-opens the chat this window had before it was reloaded.
   *
   * A reload used to land on an empty "new chat" with the conversation only findable in the rail,
   * which reads like data loss. It runs once, and only for a chat that is still in this user's
   * list - a chat deleted meanwhile, or one remembered from another account, is forgotten instead
   * of asked for.
   */
  useEffect(() => {
    if (!authenticated || sessionsLoading || restoredSession.current) return
    restoredSession.current = true
    const id = remembered.current
    if (!id) return
    if (!sessions.some((item) => item.id === id)) {
      writeActiveSession(null)
      return
    }
    void onSelectSession(id)
  }, [authenticated, sessionsLoading, sessions, onSelectSession])

  /**
   * Starts a new chat.
   *
   * Nothing is created here. A chat with no message in it has no title anyone could give it and
   * nothing to show in the rail, so the session is created by the first send, which is also the
   * first moment a title is knowable. That makes "new chat" while an empty one is already open a
   * no-op for free, and leaves no empty rows behind.
   */
  const onNew = useCallback(() => {
    setSessionId(null)
    setRailOpen(false)
    setSubagents([])
    showNotice('')
    setFeedback([])
    dispatch({ type: 'RESET' })
    assistantId.current = null
    runIdRef.current = null
    setBusy(false)
    runStore.set((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
  }, [])

  const onDelete = useCallback(
    async (id: string) => {
      await deleteSession(windowId, id)
      if (sessionIdRef.current === id) {
        setSessionId(null)
        setSubagents([])
        dispatch({ type: 'RESET' })
        runStore.set((s) => ({ ...EMPTY_RUN, model: s.model, contextWindow: s.contextWindow }))
      }
      await refreshSessions()
    },
    [windowId, refreshSessions],
  )

  /**
   * Sends a turn.
   *
   * @returns whether the text left the composer. A refused send keeps the draft where the user can
   *          still see and re-send it, which is why this is not fire-and-forget.
   */
  const onSend = useCallback(
    async (text: string, attachments: Attachment[], edited?: boolean): Promise<boolean> => {
      let active = sessionIdRef.current
      // Whether this send is what created the chat. A send that is then refused has produced nothing
      // to keep, and an empty chat left in the rail is a row the user has to tidy away themselves.
      let createdHere = false
      if (!active) {
        // The first send is what brings a chat into being, titled after what was said.
        const created = await createSession(windowId, autoTitle(text || labels.attachmentOnly))
        await loadSession(windowId, created.id)
        active = created.id
        createdHere = true
        setSessionId(active)
        await refreshSessions()
      }
      showNotice('')
      // Appended before the request, not after it: the server starts streaming the answer the
      // moment it accepts the message, and an await here would let the first assistant event
      // reach the reducer first - on a new chat the answer then appeared above the question.
      // A refused send takes the message back out again.
      const messageId = crypto.randomUUID()
      dispatch({ type: 'ADD_USER', id: messageId, text: text || labels.attachmentOnly, edited })
      setBusy(true)
      runStore.set((s) => ({ ...s, busy: true }))
      const outbound = composeOutboundText(
        text,
        extensions?.contextHook?.contextTag?.({ messages: messagesRef.current }),
        chipsRef.current,
      )
      try {
        await sendMessage(windowId, {
          text: outbound,
          sessionId: active,
          agent: agentRef.current,
          model: modelRef.current,
          mcpOverride: mcpDirtyRef.current,
          mcpServers: mcpRef.current,
          attachments,
          effort: effortRef.current || undefined,
        })
      } catch (err) {
        dispatch({ type: 'REMOVE_MESSAGE', id: messageId })
        setBusy(false)
        runStore.set((s) => ({ ...s, busy: false }))
        // A refusal is not a failure: the server wrote a sentence for this user saying what they may
        // not use, and it reads as information rather than as something that went wrong.
        if (err instanceof ForbiddenError) {
          await discardEmptyChat(createdHere, active)
          showNotice(err.reason || labels.notAllowed, '', { tone: 'info', sticky: true })
          return false
        }
        const busyRefusal = err instanceof Error && err.message === 'busy'
        await discardEmptyChat(createdHere && !busyRefusal, active)
        showNotice(busyRefusal ? labels.busyHint : labels.sendFailed, '', {
          tone: busyRefusal ? 'info' : 'warn',
        })
        return false
      }
      return true
    },
    [labels, windowId, refreshSessions, extensions],
  )

  const onStop = useCallback(() => {
    void cancelRun(windowId)
  }, [windowId])

  const onToggleMcp = useCallback((name: string) => {
    setMcpDirty(true)
    setMcp((prev) => (prev.includes(name) ? prev.filter((x) => x !== name) : [...prev, name]))
  }, [])

  const onToggleAutoApprove = useCallback(async () => {
    const next = !autoApproveRef.current
    setAutoApprove(next)
    try {
      await setApprovalMode(windowId, next ? 'AUTO_APPROVE' : 'MANUAL')
    } catch {
      setAutoApprove(!next)
      return
    }
    if (!next) return
    for (const message of messagesRef.current) {
      if (message.type !== 'approval_request' || message.outcome !== 'pending') continue
      void respond(windowId, {
        requestId: message.approvalRequestId,
        response: 'APPROVED',
        toolName: message.toolName,
        argsJson: message.argsJson,
      })
      dispatch({
        type: 'RESOLVE_APPROVAL',
        approvalRequestId: message.approvalRequestId,
        outcome: 'APPROVED',
      })
    }
  }, [windowId])

  const onAgent = useCallback(
    (name: string) => {
      applyAgent(name, caps.agents)
    },
    [applyAgent, caps.agents],
  )

  const onCancelSubagent = useCallback(
    async (id: string) => {
      await cancelSubagent(windowId, id)
      setSubagents((items) =>
        items.map((item) => (item.subagentId === id ? { ...item, status: 'CANCELLED' } : item)),
      )
    },
    [windowId],
  )

  const onStopSubagents = useCallback(async () => {
    const active = subagents.filter((item) => item.status === 'RUNNING' || item.status === 'QUEUED')
    await Promise.all(active.map((item) => cancelSubagent(windowId, item.subagentId)))
    setSubagents((items) =>
      items.map((item) =>
        item.status === 'RUNNING' || item.status === 'QUEUED' ? { ...item, status: 'CANCELLED' } : item,
      ),
    )
  }, [subagents, windowId])

  const onLoadSubagentTranscript = useCallback(
    (id: string): Promise<ChatMessage[]> => loadSubagentTranscript(windowId, id),
    [windowId],
  )

  const sessionTitle = useMemo(
    () => sessions.find((session) => session.id === sessionId)?.title || labels.newChat,
    [labels, sessionId, sessions],
  )

  const pendingApprovals = useMemo(
    () =>
      messages.filter(
        (message): message is ApprovalRequest =>
          message.type === 'approval_request' && message.outcome === 'pending',
      ),
    [messages],
  )
  const pendingQuestions = useMemo(
    () =>
      messages.filter(
        (message): message is InteractionRequest =>
          message.type === 'interaction_request' && !message.answered,
      ),
    [messages],
  )

  /**
   * Sends an edited message in place of the one the user changed.
   *
   * Nothing happens until this point: the bubble is edited in the thread, and only a send drops the
   * turn and everything after it - here and on the server - and starts the conversation again from
   * the new wording. A cancelled edit leaves the conversation exactly as it was.
   */
  const onEditSend = useCallback(
    async (messageId: string, turnIndex: number, text: string): Promise<boolean> => {
      const active = sessionIdRef.current
      if (!active) return false
      showNotice('')
      let removed = 0
      try {
        const result = await truncate(windowId, active, turnIndex)
        if (result.status === 'busy') {
          showNotice(labels.busyHint, '', { tone: 'info' })
          return false
        }
        removed = result.removedTurns ?? 0
      } catch {
        showNotice(labels.sendFailed)
        return false
      }
      dispatch({ type: 'REMOVE_FROM_USER', id: messageId })
      setFeedback((current) => current.filter((entry) => entry.turnIndex < turnIndex))
      runStore.set((snapshot) => ({ ...snapshot, measuredTokens: 0 }))
      const sent = await onSend(text, [], true)
      // Dropping the edited turn is what editing always does; saying so only helps when answers the
      // user could still see went with it.
      // Sticky: the send that follows an edit is what starts the run, and a run start used to
      // clear the strip in the same instant this line was written into it.
      if (sent && removed > 1) {
        showNotice(labels.laterTurnsRemoved(removed - 1), '', { tone: 'info', sticky: true })
      }
      return sent
    },
    [labels, onSend, windowId],
  )

  /**
   * Answers the last message again.
   *
   * The old answer is not kept beside the new one: the user asked for that turn to be done
   * differently. The server drops it and re-sends the same message; what streams in takes its place.
   */
  const onRegenerate = useCallback(
    async (messageId: string, turnIndex: number) => {
      const active = sessionIdRef.current
      if (!active || regenerating) return
      showNotice('')
      setRegenerating(true)
      try {
        const result = await regenerate(windowId, {
          sessionId: active,
          agent: agentRef.current,
          model: modelRef.current,
          mcpOverride: mcpDirtyRef.current,
          mcpServers: mcpRef.current,
          effort: effortRef.current || undefined,
        })
        if (result.status === 'busy') {
          showNotice(labels.busyHint, '', { tone: 'info' })
          return
        }
        if (result.status !== 'ok') {
          showNotice(labels.sendFailed)
          return
        }
        // The turn is gone server side; drop it here too and let the run rebuild it.
        dispatch({ type: 'REMOVE_FROM_USER', id: messageId })
        setFeedback((current) => current.filter((entry) => entry.turnIndex !== turnIndex))
        dispatch({ type: 'ADD_USER', id: crypto.randomUUID(), text: result.text ?? '' })
        setBusy(true)
        runStore.set((snapshot) => ({ ...snapshot, busy: true, measuredTokens: 0 }))
      } catch (err) {
        if (err instanceof ForbiddenError) {
          showNotice(err.reason || labels.notAllowed, '', { tone: 'info', sticky: true })
        } else {
          showNotice(labels.sendFailed)
        }
      } finally {
        setRegenerating(false)
      }
    },
    [labels, regenerating, windowId],
  )

  /** Records what the user thought of an answer, or withdraws an earlier rating. */
  const onRate = useCallback(
    async (turnIndex: number, rating: Rating, note?: string) => {
      const active = sessionIdRef.current
      if (!active) return
      setFeedback((current) => {
        const others = current.filter((entry) => entry.turnIndex !== turnIndex)
        return rating ? [...others, { turnIndex, rating, note }] : others
      })
      try {
        await sendFeedback(windowId, active, turnIndex, rating, note)
      } catch {
        // The rating is a convenience, not the conversation: a failed write is not worth an error
        // banner, but it must not look recorded either.
        setFeedback((current) => current.filter((entry) => entry.turnIndex !== turnIndex))
      }
    },
    [windowId],
  )

  const progressSection: RailSection = useMemo(
    () => ({
      id: 'progress',
      title: labels.progressTitle,
      contributor: labels.progressContributor,
      badge: () => progressBadge(messages),
      render: () => <ProgressSection messages={messages} labels={labels} />,
    }),
    [labels, messages],
  )

  const railSections = useMemo(
    () => [progressSection, ...hostRailSections(extensions)],
    [progressSection, extensions],
  )

  const badgeTotal = useMemo(
    () => railSections.reduce((sum, section) => sum + (section.badge?.() ?? 0), 0),
    [railSections],
  )
  const badge = unseenBadge(badgeTotal, badgeSeen)

  const openInPane = useCallback((content: React.ReactNode, options: PaneOpenOptions) => {
    setPaneContent(content)
    setPaneOptions(options)
    setSideOpen(true)
  }, [])

  const addComposerChip = useCallback((chip: ComposerChip) => {
    setHostChips((current) => (current.some((item) => item.id === chip.id) ? current : [...current, chip]))
  }, [])

  const railCtx = useMemo(
    () => ({
      messages,
      busy,
      openInPane,
      addComposerChip,
    }),
    [messages, busy, openInPane, addComposerChip],
  )

  const toggleSide = useCallback(() => {
    setSideOpen((open) => {
      if (open) {
        setPaneContent(null)
        setPaneOptions(null)
        return false
      }
      setBadgeSeen(badgeTotal)
      return true
    })
  }, [badgeTotal])

  if (!authenticated) {
    return (
      <div id={controlId} className="tl-cockpit is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  const paneOpen = sideOpen && paneContent != null
  const railVisible = sideOpen && !paneOpen

  return (
    <div id={controlId} className={`tl-cockpit${railCollapsed ? ' is-rail-collapsed' : ''}${sideOpen ? ' has-side' : ''}${paneOpen ? ' has-pane' : ''}`}>
      <SessionRail
        labels={labels}
        sessions={sessions}
        activeId={sessionId}
        open={railOpen}
        collapsed={railCollapsed}
        loading={sessionsLoading}
        error={sessionsError}
        onNew={onNew}
        onSelect={onSelectSession}
        onDelete={onDelete}
        onClose={() => setRailOpen(false)}
        onToggleCollapsed={() => setRailCollapsed((value) => !value)}
      />
      <div className="workspace">
        <div className={`stage${messages.length === 0 ? ' is-empty' : ''}`}>
          <Thread
            messages={messages}
            traceMode={traceMode}
            welcomeName={welcomeName || userName}
            busy={busy}
            regenerating={regenerating}
            feedback={feedback}
            labels={labels}
            language={language}
            effort={effort}
            onEditSend={onEditSend}
            onRegenerate={onRegenerate}
            onRate={onRate}
            onOpenRail={() => setRailOpen(true)}
            railCollapsed={railCollapsed}
            onToggleRail={() => setRailCollapsed((value) => !value)}
            title={sessionTitle}
            headerCaption={extensions?.contextHook?.header?.({ messages })}
            headerEnd={<RailToggle open={sideOpen} badge={badge} labels={labels} onToggle={toggleSide} />}
            renderAnnotation={extensions?.messageAnnotations?.render}
            openInPane={openInPane}
            extraActions={extensions?.messageActions}
          />
          <Composer
            windowId={windowId}
            agents={caps.agents}
            models={offeredModels}
            agent={agent}
            model={model}
            onAgent={onAgent}
            onModel={setModel}
            mcpServers={caps.mcpServers}
            selectedMcp={mcp}
            onToggleMcp={onToggleMcp}
            traceMode={traceMode}
            onTraceMode={setTraceMode}
            effort={effort}
            effortLevels={effortLevels}
            onEffort={setEffort}
            labels={labels}
            autoApprove={autoApprove}
            onToggleAutoApprove={() => void onToggleAutoApprove()}
            pendingApprovals={pendingApprovals}
            pendingQuestions={pendingQuestions}
            dispatch={dispatch}
            promptImprovementEnabled={promptImprovementEnabled && caps.promptImproverAvailable !== false}
            subagents={subagents}
            onCancelSubagent={onCancelSubagent}
            onStopSubagents={onStopSubagents}
            onLoadSubagentTranscript={onLoadSubagentTranscript}
            busy={busy}
            connection={connection}
            notice={notice}
            noticeDetail={noticeDetail}
            noticeTone={noticeTone}
            onDismissNotice={() => showNotice('')}
            onSend={onSend}
            onStop={onStop}
            hostChips={chips}
            onRemoveChip={(id) => setHostChips((current) => current.filter((chip) => chip.id !== id))}
            extraMenuItems={extensions?.composerAddons?.extraMenuItems}
          />
        </div>
        {paneOpen ? (
          <SideStrip
            sections={railSections}
            activeId={paneOptions?.sectionId}
            onSelect={() => {
              setPaneContent(null)
              setPaneOptions(null)
            }}
            labels={labels}
          />
        ) : null}
        {railVisible ? (
          <aside className="side-column" aria-label={labels.sideRail}>
            <SideRail
              sections={railSections}
              ctx={railCtx}
              collapsedIds={collapsedSections.collapsedIds}
              onToggleSection={collapsedSections.onToggleSection}
              labels={labels}
            />
          </aside>
        ) : null}
        {paneOpen ? (
          <SidePane
            title={paneOptions?.title ?? ''}
            backLabel={paneOptions?.backLabel}
            tabs={paneOptions?.tabs}
            width={paneWidth}
            onWidth={setPaneWidth}
            onBack={() => {
              setPaneContent(null)
              setPaneOptions(null)
            }}
            labels={labels}
          >
            {paneContent}
          </SidePane>
        ) : null}
      </div>
    </div>
  )
}

/** Tool arguments arrive as a JSON string; a tool that sends something else keeps it as text. */
function parseToolInput(argsJson: string | undefined): unknown {
  if (argsJson == null) return ''
  try {
    return JSON.parse(argsJson)
  } catch {
    return argsJson
  }
}
