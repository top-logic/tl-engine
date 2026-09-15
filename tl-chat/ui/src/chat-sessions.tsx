import { React, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { SessionRail } from './chrome'
import { bundle } from './i18n'

const { useCallback, useEffect, useMemo, useState } = React

export interface SessionsConfig {
  search: boolean
  newChat: boolean
  canDelete: boolean
  grouping: 'by-date' | 'flat'
  collapsible: boolean
}

/** Reads the list flags a {@code <chat-sessions/>} control put into its state. */
export function readSessionsConfig(state: Record<string, unknown>): SessionsConfig {
  return {
    search: state.search !== false,
    newChat: state.newChat !== false,
    canDelete: state.delete !== false,
    grouping: state.grouping === 'flat' ? 'flat' : 'by-date',
    collapsible: state.collapsible !== false,
  }
}

/**
 * The session list as its own view element.
 *
 * It joins the chat through {@code chat-id}, so an {@code App} (or a thread) on the same id sees
 * the same sessions. A configured {@code selection} channel is a second, optional path: the
 * control writes the {@code AgentSession} object for a host table or form, and a value written
 * there selects the row here.
 */
export function TLChatSessions({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat, actions } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  const config = useMemo(() => readSessionsConfig(state), [state])
  const [collapsed, setCollapsed] = useState(false)
  const selected = typeof state.selected === 'string' ? state.selected : ''

  useEffect(() => {
    if (!selected || selected === chat.sessionId) return
    void actions.selectSession(selected)
  }, [actions, chat.sessionId, selected])

  const publish = useCallback(
    (id: string) => {
      void command('selectSession', { id })
    },
    [command],
  )

  const onSelect = useCallback(
    (id: string) => {
      void actions.selectSession(id)
      publish(id)
    },
    [actions, publish],
  )

  const onNew = useCallback(() => {
    actions.newChat()
    publish('')
  }, [actions, publish])

  if (!chat.authenticated) {
    return (
      <div id={controlId} className="tl-chat-sessions is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  return (
    <div id={controlId} className="tl-chat-sessions">
      <SessionRail
        labels={labels}
        sessions={chat.sessions}
        activeId={chat.sessionId}
        open
        collapsed={config.collapsible && collapsed}
        loading={chat.sessionsLoading}
        error={chat.sessionsError}
        onNew={onNew}
        onSelect={onSelect}
        onDelete={actions.deleteSession}
        onClose={() => undefined}
        onToggleCollapsed={() => setCollapsed((value) => !value)}
        search={config.search}
        newChat={config.newChat}
        canDelete={config.canDelete}
        grouping={config.grouping}
        collapsible={config.collapsible}
      />
    </div>
  )
}
