import { React, rootClassName, useTLCommand, useTLState } from 'tl-react-bridge'
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
 * scrolls to its answer; a row with a URL opens it in a new tab.
 *
 * The badge is what the **newest answer** cited, which the rail turns into "how many since you last
 * looked". Not the length of the list: that only grows, so the badge stayed on the icon for the rest
 * of the conversation and said nothing about the answer in front of the reader (#495). An answer
 * that cited nothing carries no badge.
 */
export function TLChatSources({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])

  useBadgeChannel(command, state.badge === true, chat.latestSources.length)

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
    <div id={controlId} className={rootClassName(state, 'tl-chat-section')}>
      <SourcesSection
        sources={chat.sources}
        labels={labels}
        onOpen={openSource}
        onGoTo={goToAnswer}
      />
    </div>
  )
}
