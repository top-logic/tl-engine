import { React, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { useBadgeChannel } from './chat-section'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { bundle } from './i18n'
import { SourcesSection, type CitedSource } from './sources'

const { useCallback, useMemo } = React

/**
 * `<chat-sources/>`: everything this conversation has cited, as a rail section of its own.
 *
 * Session-wide, so a page cited in three answers is one row with three marker numbers. A marker
 * scrolls to its answer; a row with a URL opens it in a new tab. The badge is how many sources
 * there are, which the rail turns into "how many since you last looked".
 */
export function TLChatSources({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])

  useBadgeChannel(command, state.badge === true, chat.sources.length)

  const openSource = useCallback((source: CitedSource) => {
    const citation = source.citation
    const href = citation.url || (/^https?:\/\//i.test(citation.id) ? citation.id : '')
    if (href) window.open(href, '_blank', 'noopener,noreferrer')
  }, [])

  const goToAnswer = useCallback((messageId: string) => {
    const el = document.querySelector(`[data-message-id="${CSS.escape(messageId)}"]`)
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el?.classList.add('is-pointed')
    window.setTimeout(() => el?.classList.remove('is-pointed'), 1600)
  }, [])

  return (
    <div id={controlId} className="tl-chat-section">
      <SourcesSection
        sources={chat.sources}
        labels={labels}
        onOpen={openSource}
        onGoTo={goToAnswer}
      />
    </div>
  )
}
