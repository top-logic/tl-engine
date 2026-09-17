import { React, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { bundle } from './i18n'

const { useCallback, useEffect, useMemo, useRef } = React

/**
 * Headless binding between the shared chat store and a `selection` channel, for a page whose session
 * list is a generic `<grouped-table>` rather than the built-in `<chat-sessions>`.
 *
 * It supplies the two things a channel write cannot: a "new chat" button that resets the live
 * conversation (`newChat()`), and a write-back of the store's own session id onto the channel when
 * the store creates a session on first send or restores one on reload, so the list highlights the
 * active row. Selecting a row is already handled by `<chat-thread>`/`<chat-composer>` reading the
 * same channel.
 */
export function TLChatSessionBinding({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat, actions } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  const showNewChat = state.newChatButton !== false

  // Highlight sync: publish the store's session id whenever it changes on its own (first send,
  // restore). Re-publishing the same id is skipped; re-publishing a value the channel already
  // carries is a harmless no-op on the Java side.
  const published = useRef<string | null>(null)
  useEffect(() => {
    const id = chat.sessionId ?? ''
    if (published.current === id) return
    published.current = id
    void command('selectSession', { id })
  }, [chat.sessionId, command])

  const onNew = useCallback(() => {
    actions.newChat()
    published.current = ''
    void command('selectSession', { id: '' })
  }, [actions, command])

  // The session list's "new" button pulses a create signal; each pulse starts a fresh conversation.
  // lastTick is seeded with the initial value on mount, so the first real pulse already fires.
  const tick = state.newChatTick
  const lastTick = useRef<unknown>(tick)
  useEffect(() => {
    if (tick === lastTick.current) return
    lastTick.current = tick
    onNew()
  }, [tick, onNew])

  if (!showNewChat) {
    return <div id={controlId} className="tl-chat-session-binding" hidden />
  }
  return (
    <div id={controlId} className="tl-chat-session-binding">
      <button type="button" className="tl-chat-new-chat" onClick={onNew}>
        {labels.newChat}
      </button>
    </div>
  )
}
