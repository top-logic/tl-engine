import { React, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import {
  autoTitle,
  cancelRun,
  cancelSubagent,
  createSession,
  deleteSession,
  fetchCapabilities,
  listSubagents,
  listSessions,
  loadSession,
  loadSubagentTranscript,
  openEventStream,
  renameSession,
  respond,
  sendMessage,
  setApprovalMode,
  turnsJsonToMessages,
} from './api'
import { SessionRail } from './chrome'
import { Composer } from './composer'
import { chatReducer } from './reducer'
import { EMPTY_RUN, readTraceMode, runStore, writeTraceMode } from './store'
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
  type SessionSummary,
  type SubagentActivity,
  type SubagentStatus,
  type SubagentStatusEvent,
  type TokenUsage,
  type TraceMode,
} from './types'

const { useCallback, useEffect, useMemo, useReducer, useRef, useState } = React

/**
 * Without a frame for this long the stream counts as stale and is re-opened.
 *
 * The server sends a heartbeat every 15s when nothing else happens, so this allows two missed ones
 * before the user is told anything - a single slow flush must not flash a warning.
 */
const STALE_AFTER_MS = 40_000

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

export function App({ controlId }: TLCellProps) {
  const state = useTLState()
  const windowId = (state.windowId as string) || ''
  const userName = (state.userName as string) || 'User'
  const welcomeName = (state.welcomeName as string) || ''
  const configuredTraceMode = parseTraceMode(state.traceMode)
  const promptImprovementEnabled = state.promptImprovementEnabled === true
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
  const [busy, setBusy] = useState(false)
  const [railOpen, setRailOpen] = useState(false)
  const [railCollapsed, setRailCollapsed] = useState(false)
  const [subagents, setSubagents] = useState<SubagentActivity[]>([])
  const [autoApprove, setAutoApprove] = useState(false)
  const [connection, setConnection] = useState<ConnectionState>('connecting')
  const [notice, setNotice] = useState('')
  const [messages, dispatch] = useReducer(chatReducer, [])
  const assistantId = useRef<string | null>(null)
  const sessionIdRef = useRef<string | null>(null)
  const messagesRef = useRef(messages)
  const agentRef = useRef(agent)
  const modelRef = useRef(model)
  const mcpRef = useRef(mcp)
  const mcpDirtyRef = useRef(mcpDirty)
  const autoApproveRef = useRef(false)
  const runIdRef = useRef<string | null>(null)
  sessionIdRef.current = sessionId
  messagesRef.current = messages
  agentRef.current = agent
  modelRef.current = model
  mcpRef.current = mcp
  mcpDirtyRef.current = mcpDirty
  autoApproveRef.current = autoApprove

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
        if (!cancelled) setSessionsError('Chats could not be loaded.')
      } finally {
        if (!cancelled) setSessionsLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [windowId, applyAgent])

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

  useEffect(() => {
    let closed = false
    let source: EventSource | null = null
    let staleTimer = 0
    let reopenTimer = 0

    const finish = (error?: string) => {
      if (assistantId.current) {
        dispatch({ type: 'FINALIZE_TEXT', id: assistantId.current })
        assistantId.current = null
      }
      if (runIdRef.current) {
        dispatch({ type: 'END_RUN', runId: runIdRef.current })
        runIdRef.current = null
      }
      if (error) setNotice(error)
      setBusy(false)
      runStore.set((s) => ({ ...s, busy: false }))
    }

    /** Restarts the stale timer; every frame from the server counts, heartbeats included. */
    const markAlive = () => {
      setConnection('live')
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
        setNotice('')
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
      on('run_completed', () => finish())
      on('message_complete', () => finish())
      on('provider_error', (e) => {
        const data = parseJson<{ errorMessage?: string; kind?: string }>(e.data, {})
        finish(data.errorMessage ?? 'The model returned an error.')
      })

      es.onerror = () => {
        if (es.readyState === EventSource.CLOSED) {
          setConnection('reconnecting')
          reopen()
        }
      }
      markAlive()
    }

    connect()
    return () => {
      closed = true
      window.clearTimeout(staleTimer)
      window.clearTimeout(reopenTimer)
      source?.close()
    }
  }, [windowId])

  const refreshSessions = useCallback(async () => {
    try {
      setSessions(await listSessions(windowId))
      setSessionsError('')
    } catch {
      setSessionsError('Chats could not be refreshed.')
    }
  }, [windowId])

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
      setNotice('')
      runStore.set({ ...EMPTY_RUN, model: modelRef.current })
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
      }))
      await refreshSubagents()
    },
    [windowId, refreshSubagents],
  )

  const onNew = useCallback(async () => {
    const created = await createSession(windowId, 'New chat')
    await loadSession(windowId, created.id)
    setSessionId(created.id)
    setRailOpen(false)
    setSubagents([])
    setNotice('')
    dispatch({ type: 'RESET' })
    assistantId.current = null
    runIdRef.current = null
    setBusy(false)
    runStore.set({ ...EMPTY_RUN, model: modelRef.current })
    await refreshSessions()
  }, [windowId, refreshSessions])

  const onDelete = useCallback(
    async (id: string) => {
      await deleteSession(windowId, id)
      if (sessionIdRef.current === id) {
        setSessionId(null)
        setSubagents([])
        dispatch({ type: 'RESET' })
        runStore.set({ ...EMPTY_RUN, model: modelRef.current })
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
    async (text: string, attachments: Attachment[]): Promise<boolean> => {
      let active = sessionIdRef.current
      if (!active) {
        const created = await createSession(windowId, autoTitle(text))
        await loadSession(windowId, created.id)
        active = created.id
        setSessionId(active)
        await refreshSessions()
      } else if (messagesRef.current.length === 0 && text) {
        try {
          await renameSession(windowId, active, autoTitle(text))
          await refreshSessions()
        } catch {
          /* title is best-effort */
        }
      }
      setNotice('')
      try {
        await sendMessage(windowId, {
          text,
          sessionId: active,
          agent: agentRef.current,
          model: modelRef.current,
          mcpOverride: mcpDirtyRef.current,
          mcpServers: mcpRef.current,
          attachments,
        })
      } catch (err) {
        setNotice(
          err instanceof Error && err.message === 'busy'
            ? 'The agent is still working on the previous message. Your text is kept - send it again when the run has finished.'
            : 'The message could not be sent.',
        )
        return false
      }
      dispatch({ type: 'ADD_USER', text: text || '(attachment)' })
      setBusy(true)
      runStore.set((s) => ({ ...s, busy: true }))
      return true
    },
    [windowId, refreshSessions],
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
    () => sessions.find((session) => session.id === sessionId)?.title || 'New chat',
    [sessionId, sessions],
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

  /** Puts a user message back into the composer so it can be corrected and sent again. */
  const [resendDraft, setResendDraft] = useState<{ text: string; nonce: number } | null>(null)
  const onResend = useCallback((text: string) => {
    setResendDraft({ text, nonce: Date.now() })
  }, [])
  const onRetry = useCallback(
    (text: string) => {
      void onSend(text, [])
    },
    [onSend],
  )

  return (
    <div id={controlId} className={`tl-cockpit${railCollapsed ? ' is-rail-collapsed' : ''}`}>
      <SessionRail
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
      <div className="stage">
        <Thread
          messages={messages}
          traceMode={traceMode}
          welcomeName={welcomeName || userName}
          busy={busy}
          onResend={onResend}
          onRetry={onRetry}
          onOpenRail={() => setRailOpen(true)}
          railCollapsed={railCollapsed}
          onToggleRail={() => setRailCollapsed((value) => !value)}
          title={sessionTitle}
        />
        <Composer
          windowId={windowId}
          agents={caps.agents}
          models={caps.models}
          agent={agent}
          model={model}
          onAgent={onAgent}
          onModel={setModel}
          mcpServers={caps.mcpServers}
          selectedMcp={mcp}
          onToggleMcp={onToggleMcp}
          traceMode={traceMode}
          onTraceMode={setTraceMode}
          autoApprove={autoApprove}
          onToggleAutoApprove={() => void onToggleAutoApprove()}
          pendingApprovals={pendingApprovals}
          pendingQuestions={pendingQuestions}
          dispatch={dispatch}
          promptImprovementEnabled={promptImprovementEnabled}
          subagents={subagents}
          onCancelSubagent={onCancelSubagent}
          onStopSubagents={onStopSubagents}
          onLoadSubagentTranscript={onLoadSubagentTranscript}
          busy={busy}
          connection={connection}
          notice={notice}
          onDismissNotice={() => setNotice('')}
          resendDraft={resendDraft}
          onSend={onSend}
          onStop={onStop}
        />
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
