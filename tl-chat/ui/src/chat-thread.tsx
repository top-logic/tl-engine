import { React, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { bundle } from './i18n'
import { RailToggle } from './rail'
import { Thread } from './thread'

const { useCallback, useEffect, useMemo } = React

export interface ThreadConfig {
  traceMode: 'none' | 'overview' | 'detailed'
  runSummary: 'collapsed' | 'expanded' | 'hidden'
  citationMarkers: boolean
  railToggle: boolean
  copy: boolean
  rate: boolean
  regenerate: boolean
}

/** Reads the thread flags a {@code <chat-thread/>} control put into its state. */
export function readThreadConfig(state: Record<string, unknown>): ThreadConfig {
  const runSummary =
    state.runSummary === 'expanded' || state.runSummary === 'hidden' ? state.runSummary : 'collapsed'
  const traceMode =
    state.traceMode === 'none' || state.traceMode === 'detailed' ? state.traceMode : 'overview'
  return {
    traceMode,
    runSummary,
    citationMarkers: state.citationMarkers !== false,
    railToggle: state.railToggle !== false,
    copy: state.copy !== false,
    rate: state.rate !== false,
    regenerate: state.regenerate !== false,
  }
}

/**
 * The session id a channel write should select, or {@code null} when the store already is the
 * source (no {@code input} channel, or the channel matches the open session).
 */
export function threadSessionToSelect(selected: string, storeSessionId: string | null): string | null {
  if (!selected || selected === storeSessionId) return null
  return selected
}

/**
 * The transcript as its own view element.
 *
 * It joins the chat through {@code chat-id}. A configured {@code input} channel selects the
 * session; without one the store is the source. A configured {@code busy} channel is written
 * while the agent works.
 */
export function TLChatThread({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat, actions } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  const config = useMemo(() => readThreadConfig(state), [state])
  const selected = typeof state.selected === 'string' ? state.selected : ''

  useEffect(() => {
    const next = threadSessionToSelect(selected, chat.sessionId)
    if (!next) return
    void actions.selectSession(next)
  }, [actions, chat.sessionId, selected])

  useEffect(() => {
    void command('setBusy', { busy: chat.busy })
  }, [chat.busy, command])

  const onEditSend = useCallback(
    (messageId: string, turnIndex: number, text: string): Promise<boolean> =>
      actions.editSend(messageId, turnIndex, text),
    [actions],
  )

  const onOpenSource = useCallback(() => undefined, [])

  if (!chat.authenticated) {
    return (
      <div id={controlId} className="tl-chat-thread is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  return (
    <div id={controlId} className="tl-chat-thread">
      <Thread
        messages={chat.messages}
        traceMode={config.traceMode}
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
        runSummary={config.runSummary}
        canCopy={config.copy}
        canRate={config.rate}
        canRegenerate={config.regenerate}
        citationMarkers={config.citationMarkers}
        headerEnd={
          config.railToggle ? (
            <RailToggle
              open={chat.sideOpen}
              badge={0}
              labels={labels}
              onToggle={() => actions.toggleSide(0)}
            />
          ) : null
        }
        onOpenSource={onOpenSource}
      />
    </div>
  )
}
