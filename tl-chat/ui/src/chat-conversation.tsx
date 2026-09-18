import { React, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import type { ApprovalRequest, Attachment, InteractionRequest } from './types'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { Composer, plusMenuVisible } from './composer'
import type { ComposerApi, ReferenceCandidate } from './extensions'
import { bundle } from './i18n'
import { RailToggle } from './rail'
import {
  registeredAnnotationRenderer,
  registeredReferenceSource,
  resolveMenuItems,
  resolveMessageActions,
} from './registry'
import { Thread } from './thread'

const { useCallback, useEffect, useMemo, useRef } = React

export interface ThreadConfig {
  citationMarkers: boolean
  copy: boolean
  rate: boolean
  regenerate: boolean
  timestamp: boolean
}

/**
 * Reads the transcript flags a {@code <chat-conversation/>} control put into its state.
 *
 * The trace level is *not* here: it is the store's own axis (seeded from the element's {@code trace}
 * and changed live by the composer's trace picker), so the transcript reads {@code chat.traceMode}.
 */
export function readThreadConfig(state: Record<string, unknown>): ThreadConfig {
  return {
    citationMarkers: state.citations !== false,
    copy: state.copy !== false,
    rate: state.rate !== false,
    regenerate: state.regenerate !== false,
    timestamp: state.timestamp !== false,
  }
}

export interface ComposerControls {
  autoApproveSwitch: boolean
  enterHint: boolean
  agentPicker: boolean
  modelPicker: boolean
  thinking: boolean
  mcp: boolean
  attachmentsEnabled: boolean
  tracePicker: boolean
}

/** Reads the composer flags a {@code <chat-conversation/>} control put into its state. */
export function readComposerControls(state: Record<string, unknown>): ComposerControls {
  return {
    autoApproveSwitch: state.autoApprove !== false,
    enterHint: state.enterHint !== false,
    agentPicker: state.agentPicker !== false,
    modelPicker: state.modelPicker !== false,
    thinking: state.thinking !== false,
    mcp: state.mcp !== false,
    attachmentsEnabled: state.attachments !== false,
    tracePicker: state.tracePicker !== false,
  }
}

/**
 * A synchronous {@code @}-source over the candidates a model-bound {@code <mention>} pushed into
 * state. The composer calls it on each keystroke; it filters the pre-pushed set (label or quoted text
 * contains the query). Returns {@code undefined} when no model binding is present, so the caller can
 * fall back to a registered TypeScript source.
 */
export function modelBoundSource(raw: unknown): ((query: string) => ReferenceCandidate[]) | undefined {
  if (!Array.isArray(raw)) return undefined
  const candidates: ReferenceCandidate[] = []
  for (const item of raw) {
    if (!item || typeof item !== 'object') continue
    const row = item as Record<string, unknown>
    const token = row.token as Record<string, unknown> | undefined
    if (!token || typeof token.id !== 'string') continue
    candidates.push({
      id: String(row.id ?? token.id),
      label: String(row.label ?? token.label ?? ''),
      token: {
        kind: typeof token.kind === 'string' ? token.kind : '',
        id: String(token.id),
        label: String(token.label ?? ''),
        text: typeof token.text === 'string' ? token.text : '',
      },
    })
  }
  return (query: string) => {
    const q = query.trim().toLowerCase()
    if (!q) return candidates
    return candidates.filter(
      (candidate) =>
        candidate.label.toLowerCase().includes(q) || candidate.token.text.toLowerCase().includes(q),
    )
  }
}

/**
 * The transcript and the prompt field as one view element.
 *
 * It joins the chat through {@code chat-id}. A configured {@code input} channel selects the
 * session; without one the store is the source. A configured {@code busy} channel is written while
 * the agent works. Every picker can be switched off; sending still works. The pane a source or a
 * host section opens lives in a sibling {@code <chat-rail/>} on the same {@code chat-id}.
 */
export function TLChatConversation({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat, actions } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  const config = useMemo(() => readThreadConfig(state), [state])
  const controls = useMemo(() => readComposerControls(state), [state])
  const selected = typeof state.selected === 'string' ? state.selected : ''
  const hasInput = state.hasInput === true
  const placeholder = typeof state.placeholder === 'string' ? state.placeholder : ''
  const promptImprovementEnabled = state.promptImprovementEnabled === true
  const extraMenuItems = useMemo(
    () => resolveMenuItems(Array.isArray(state.menuItems) ? state.menuItems.map(String) : []),
    [state.menuItems],
  )
  const referenceCandidates = useMemo(
    () =>
      modelBoundSource(state.referenceCandidates) ??
      registeredReferenceSource(typeof state.referenceSource === 'string' ? state.referenceSource : ''),
    [state.referenceCandidates, state.referenceSource],
  )

  // Follow the selection channel. A non-empty value selects that session; clearing it (a transition
  // to empty) means "new chat" - a deselect shows a fresh, empty conversation. The transition guard
  // avoids wiping a session that was restored before the write-back below set the channel.
  const prevSelected = useRef(selected)
  useEffect(() => {
    if (!hasInput) {
      prevSelected.current = selected
      return
    }
    const wasSelected = prevSelected.current
    prevSelected.current = selected
    if (selected) {
      if (selected !== chat.sessionId) void actions.selectSession(selected)
    } else if (wasSelected && chat.sessionId) {
      actions.newChat()
    }
  }, [hasInput, selected, chat.sessionId, actions])

  // Write-back: publish the store's own session onto the channel (two-way), so a session list
  // highlights it after the store creates one on first send or restores one on reload.
  const published = useRef<string | null>(null)
  useEffect(() => {
    if (!hasInput) return
    const id = chat.sessionId ?? ''
    if (published.current === id) return
    published.current = id
    void command('publishSession', { id })
  }, [hasInput, chat.sessionId, command])

  useEffect(() => {
    void command('setBusy', { busy: chat.busy })
  }, [chat.busy, command])

  const pendingApprovals = useMemo(
    () =>
      chat.messages.filter(
        (message): message is ApprovalRequest =>
          message.type === 'approval_request' && message.outcome === 'pending',
      ),
    [chat.messages],
  )
  const pendingQuestions = useMemo(
    () =>
      chat.messages.filter(
        (message): message is InteractionRequest =>
          message.type === 'interaction_request' && !message.answered,
      ),
    [chat.messages],
  )

  const onEditSend = useCallback(
    (messageId: string, turnIndex: number, text: string): Promise<boolean> =>
      actions.editSend(messageId, turnIndex, text),
    [actions],
  )
  const onSend = useCallback(
    (text: string, attachments: Attachment[]): Promise<boolean> => actions.send(text, attachments),
    [actions],
  )

  if (!chat.authenticated) {
    return (
      <div id={controlId} className="tl-chat-conversation is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  return (
    <div id={controlId} className="tl-chat-conversation">
      <div className={`stage${chat.messages.length === 0 ? ' is-empty' : ''}`}>
        <Thread
          messages={chat.messages}
          traceMode={chat.traceMode}
          welcomeName={chat.welcomeName || chat.userName}
          busy={chat.busy}
          regenerating={chat.regenerating}
          feedback={chat.feedback}
          labels={labels}
          language={chat.language}
          effort={chat.effort}
          onEditSend={onEditSend}
          onRegenerate={actions.regenerate}
          onRate={actions.rate}
          onOpenRail={() => undefined}
          railCollapsed={false}
          onToggleRail={() => undefined}
          title={chat.sessionTitle}
          sessionListToggle={false}
          canCopy={config.copy}
          canRate={config.rate}
          canRegenerate={config.regenerate}
          citationMarkers={config.citationMarkers}
          showTimestamp={config.timestamp}
          extraActions={resolveMessageActions(
            Array.isArray(state.hostActions) ? state.hostActions.map(String) : [],
          )}
          renderAnnotation={registeredAnnotationRenderer(
            typeof state.citationRenderer === 'string' ? state.citationRenderer : '',
          )}
          openInPane={actions.openPane}
          headerEnd={
            // Derived, not configured: the badge button appears exactly when a `<chat-rail/>` on
            // this `chat-id` is mounted, because with no rail it would toggle nothing visible.
            chat.railMounted ? (
              <RailToggle
                open={chat.sideOpen}
                badge={0}
                labels={labels}
                onToggle={() => actions.toggleSide(0)}
              />
            ) : null
          }
          onOpenSource={() => undefined}
        />
        <Composer
          windowId={chat.windowId}
          agents={chat.caps.agents}
          models={chat.offeredModels}
          agent={chat.agent}
          model={chat.model}
          onAgent={actions.selectAgent}
          onModel={actions.selectModel}
          mcpServers={chat.caps.mcpServers}
          selectedMcp={chat.mcp}
          onToggleMcp={actions.toggleMcp}
          traceMode={chat.traceMode}
          onTraceMode={actions.selectTraceMode}
          effort={chat.effort}
          effortLevels={chat.effortLevels}
          onEffort={actions.selectEffort}
          labels={labels}
          autoApprove={chat.autoApprove}
          onToggleAutoApprove={() => void actions.toggleAutoApprove()}
          pendingApprovals={pendingApprovals}
          pendingQuestions={pendingQuestions}
          dispatch={actions.dispatch}
          promptImprovementEnabled={promptImprovementEnabled && chat.caps.promptImproverAvailable !== false}
          subagents={chat.subagents}
          onCancelSubagent={actions.cancelSubagent}
          onStopSubagents={actions.stopSubagents}
          onLoadSubagentTranscript={actions.loadSubagentTranscript}
          busy={chat.busy}
          connection={chat.connection}
          notice={chat.notice}
          noticeDetail={chat.noticeDetail}
          noticeTone={chat.noticeTone}
          onDismissNotice={() => actions.showNotice('')}
          onSend={onSend}
          onStop={actions.stop}
          placeholder={placeholder || undefined}
          extraMenuItems={extraMenuItems}
          referenceCandidates={referenceCandidates}
          ref={(api: ComposerApi | null) => actions.bindComposer(api)}
          {...controls}
        />
      </div>
    </div>
  )
}

export { plusMenuVisible }
