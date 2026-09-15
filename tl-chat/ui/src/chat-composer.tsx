import { React, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import type { ApprovalRequest, Attachment, InteractionRequest } from './types'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { Composer, plusMenuVisible } from './composer'
import { bundle } from './i18n'

const { useCallback, useEffect, useMemo } = React

export interface ComposerControls {
  autoApproveSwitch: boolean
  enterHint: boolean
  agentPicker: boolean
  modelPicker: boolean
  thinking: boolean
  mcp: boolean
  attachmentsEnabled: boolean
  tracePicker: boolean
  agentSlash: boolean
}

/** Reads the composer flags a {@code <chat-composer/>} control put into its state. */
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
    agentSlash: state.agentSlash === true,
  }
}

/**
 * The prompt field as its own view element.
 *
 * It joins the chat through {@code chat-id}. A configured {@code input} channel selects the
 * session; without one the store is the source. Every picker can be switched off; sending still
 * works.
 */
export function TLChatComposer({ controlId }: TLCellProps) {
  const state = useTLState()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat, actions } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  const controls = useMemo(() => readComposerControls(state), [state])
  const selected = typeof state.selected === 'string' ? state.selected : ''
  const placeholder = typeof state.placeholder === 'string' ? state.placeholder : ''
  const promptImprovementEnabled = state.promptImprovementEnabled === true

  useEffect(() => {
    if (!selected || selected === chat.sessionId) return
    void actions.selectSession(selected)
  }, [actions, chat.sessionId, selected])

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

  const onSend = useCallback(
    (text: string, attachments: Attachment[]): Promise<boolean> => actions.send(text, attachments),
    [actions],
  )

  if (!chat.authenticated) {
    return (
      <div id={controlId} className="tl-chat-composer is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  return (
    <div id={controlId} className="tl-chat-composer">
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
        {...controls}
      />
    </div>
  )
}

export { plusMenuVisible }
