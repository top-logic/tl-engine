import { React, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { useBadgeChannel } from './chat-section'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { bundle } from './i18n'
import { ProgressSection } from './progress'
import { progressBadge } from './progress-model'

const { useMemo } = React

/**
 * `<chat-progress/>`: what the agent is working on, as a rail section of its own.
 *
 * The steps come from the agent's task store. Open approvals are deliberately not here - they are
 * cards above the prompt field, where the user answers them, and a second copy in the rail would
 * only be a second place to look. The badge counts the steps that still want something.
 */
export function TLChatProgress({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])

  useBadgeChannel(command, state.badge === true, progressBadge(chat.tasks))

  return (
    <div id={controlId} className="tl-chat-section">
      <ProgressSection tasks={chat.tasks} labels={labels} />
    </div>
  )
}
