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
import { HeaderBar, SessionRail } from './chrome'
import { Composer } from './composer'
import { chatReducer } from './reducer'
import { runStore } from './store'
import { Thread } from './thread'
import type {
  AgentOption,
  ApprovalOutcome,
  ApprovalRequest,
  Attachment,
  Capabilities,
  ChatMessage,
  InteractionRequest,
  SessionSummary,
  SubagentActivity,
  SubagentStatus,
  SubagentStatusEvent,
  TokenUsage,
  TraceMode,
} from './types'

const { useCallback, useEffect, useMemo, useReducer, useRef, useState } = React

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
  const [traceMode, setTraceMode] = useState<TraceMode>(configuredTraceMode)

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
  const [messages, dispatch] = useReducer(chatReducer, [])
  const assistantId = useRef<string | null>(null)
  const sessionIdRef = useRef<string | null>(null)
  const messagesRef = useRef(messages)
  const agentRef = useRef(agent)
  const modelRef = useRef(model)
  const mcpRef = useRef(mcp)
  const mcpDirtyRef = useRef(mcpDirty)
  const autoApproveRef = useRef(false)
  sessionIdRef.current = sessionId
  messagesRef.current = messages
  agentRef.current = agent
  modelRef.current = model
  mcpRef.current = mcp
  mcpDirtyRef.current = mcpDirty
  autoApproveRef.current = autoApprove

  useEffect(() => {
    setTraceMode(configuredTraceMode)
  }, [configuredTraceMode])

  useEffect(() => {
    if (!railOpen) return
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      setRailOpen(false)
      window.setTimeout(() => {
        document.querySelector<HTMLButtonElement>(`#${CSS.escape(controlId)} .header-menu`)?.focus()
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

  useEffect(() => {
    const es = openEventStream(windowId)
    const on = (name: string, fn: (e: MessageEvent) => void) =>
      es.addEventListener(name, fn as EventListener)

    const finish = (error?: string) => {
      if (assistantId.current) {
        dispatch({ type: 'FINALIZE_TEXT', id: assistantId.current })
        assistantId.current = null
      }
      if (error) dispatch({ type: 'ADD_ERROR', message: error })
      setBusy(false)
      runStore.set((s) => ({ ...s, busy: false }))
    }

    const setTokens = (used?: number, windowSize?: number, modelName?: string, usage?: TokenUsage) => {
      runStore.set((s) => ({
        ...s,
        usedTokens: used ?? s.usedTokens,
        contextWindow: windowSize || s.contextWindow,
        model: modelName || s.model,
        lastUsage: usage ?? s.lastUsage,
      }))
    }

    on('run_started', (e) => {
      const data = parseJson<{ usedTokens?: number; contextWindow?: number; model?: string }>(e.data, {})
      setBusy(true)
      runStore.set((s) => ({
        ...s,
        busy: true,
        usedTokens: data.usedTokens ?? s.usedTokens,
        contextWindow: data.contextWindow || s.contextWindow,
        model: data.model || s.model,
      }))
    })
    on('turn_started', (e) => {
      const data = parseJson<{ usedTokens?: number; contextWindow?: number }>(e.data, {})
      setTokens(data.usedTokens, data.contextWindow)
      if (assistantId.current) dispatch({ type: 'FINALIZE_TEXT', id: assistantId.current })
      assistantId.current = crypto.randomUUID()
      dispatch({ type: 'ADD_ASSISTANT', id: assistantId.current, text: '', isStreaming: true })
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
      let input: unknown = data.argsJson
      try {
        input = JSON.parse(data.argsJson ?? '')
      } catch {
        /* keep */
      }
      dispatch({ type: 'ADD_TOOL', toolUseId: data.toolCallId, name: data.toolName ?? 'tool', input })
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
      }>(wrap.eventData ?? '', {})
      switch (wrap.eventType) {
        case 'tool_started': {
          if (!data.toolCallId) break
          let input: unknown = data.argsJson
          try {
            input = JSON.parse(data.argsJson ?? '')
          } catch {
            /* keep */
          }
          dispatch({
            type: 'ADD_TOOL',
            toolUseId: data.toolCallId,
            name: data.toolName ?? 'tool',
            input,
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
          })
          break
        case 'run_started':
        case 'turn_started': {
          const tokens = parseJson<{ usedTokens?: number; contextWindow?: number; model?: string }>(
            wrap.eventData ?? '',
            {},
          )
          setTokens(tokens.usedTokens, tokens.contextWindow, tokens.model)
          break
        }
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
      const data = parseJson<{ tokensBefore?: number; contextWindow?: number }>(e.data, {})
      setTokens(data.tokensBefore, data.contextWindow)
    })
    on('turn_completed', (e) => {
      const data = parseJson<{ turnUsage?: TokenUsage }>(e.data, {})
      if (data.turnUsage) setTokens(undefined, undefined, undefined, data.turnUsage)
    })
    on('run_completed', (e) => {
      const data = parseJson<{ totalUsage?: TokenUsage }>(e.data, {})
      if (data.totalUsage) setTokens(undefined, undefined, undefined, data.totalUsage)
      finish()
    })
    on('message_complete', () => finish())
    on('provider_error', (e) => {
      const data = parseJson<{ errorMessage?: string }>(e.data, {})
      finish(data.errorMessage ?? 'The model returned an error.')
    })
    es.onerror = () => {
      if (es.readyState === EventSource.CLOSED) finish('Lost the event stream.')
    }

    return () => {
      es.close()
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
      setBusy(false)
      setRailOpen(false)
      setSubagents([])
      runStore.set((s) => ({ ...s, busy: false }))
      const detail = await loadSession(windowId, id)
      setSessionId(id)
      dispatch({ type: 'REPLACE', messages: turnsJsonToMessages(detail.turnsJson) })
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
    dispatch({ type: 'RESET' })
    assistantId.current = null
    setBusy(false)
    await refreshSessions()
  }, [windowId, refreshSessions])

  const onDelete = useCallback(
    async (id: string) => {
      await deleteSession(windowId, id)
      if (sessionIdRef.current === id) {
        setSessionId(null)
        setSubagents([])
        dispatch({ type: 'RESET' })
      }
      await refreshSessions()
    },
    [windowId, refreshSessions],
  )

  const onSend = useCallback(
    async (text: string, attachments: Attachment[]) => {
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
      dispatch({ type: 'ADD_USER', text: text || '(attachment)' })
      setBusy(true)
      runStore.set((s) => ({ ...s, busy: true }))
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
        setBusy(false)
        runStore.set((s) => ({ ...s, busy: false }))
        dispatch({
          type: 'ADD_ERROR',
          message:
            err instanceof Error && err.message === 'busy'
              ? 'A run is already in progress.'
              : 'Send failed.',
        })
      }
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

  return (
    <div id={controlId} className="tl-cockpit">
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
        <HeaderBar title={sessionTitle} onOpenSessions={() => setRailOpen(true)} onNew={onNew} />
        <Thread
          messages={messages}
          traceMode={traceMode}
          welcomeName={welcomeName}
          userName={userName}
          agentName={agent}
          busy={busy}
          onStarter={(text) => onSend(text, [])}
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
          onSend={onSend}
          onStop={onStop}
        />
      </div>
    </div>
  )
}
