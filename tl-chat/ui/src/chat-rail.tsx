import { React, TLChild, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { bundle } from './i18n'
import {
  collapsedFromSections,
  paneIsOpen,
  railBadgeTotal,
  readRailSections,
  readSectionBadges,
  type RailSectionState,
} from './rail-model'
import { SidePane } from './side-pane'
import { ThemeIcon } from './theme-icon'

const { useCallback, useEffect, useMemo } = React

/**
 * `<chat-rail/>`: the side rail beside a conversation, as a shell whose content is configured.
 *
 * It renders the frame - the strip, the section headers, the pane's chrome - and nothing else: each
 * section's body is the view element the application put there, rendered as a child control.
 *
 * Every child stays mounted. A collapsed section and an unopened pane are hidden, not dropped, so a
 * section keeps reporting its badge while it is out of sight (the badge is the whole point of a
 * closed rail) and a reopened pane shows its object as it is now rather than as it was.
 */
export function TLChatRail({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat, actions } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])

  const sections = useMemo(() => readRailSections(state), [state])
  const badges = useMemo(() => readSectionBadges(state), [state])
  const strip = state.strip !== false
  const openDefault = state.open !== false
  const paneMode = state.paneMode === 'document' ? 'document' : 'card'
  const paneTitle = typeof state.paneTitle === 'string' ? state.paneTitle : ''
  const paneControl = state.pane ?? null

  // Tell the chat a rail is on the page, so `<chat-conversation/>` knows its badge button has
  // something to open. Unregisters on unmount.
  useEffect(() => actions.registerRail(), [actions])

  const collapsedSeed = useMemo(() => collapsedFromSections(sections), [sections])
  useEffect(() => {
    if (!collapsedSeed) return
    actions.seedCollapsedOnce(collapsedSeed)
  }, [actions, collapsedSeed])

  useEffect(() => actions.seedRailOpenOnce(openDefault), [actions, openDefault])

  const badgeTotal = useMemo(() => railBadgeTotal(sections, badges), [sections, badges])
  useEffect(() => actions.publishBadgeTotal(badgeTotal), [actions, badgeTotal])

  const collapsedIds = useMemo(() => new Set(chat.collapsedSections), [chat.collapsedSections])
  /** The pane the `input` channel opens. */
  const objectPane = state.paneOpen === true && paneControl != null
  /** The chat's own transient pane, which a citation renderer still opens with `openInPane`. */
  const hostPane = paneIsOpen(chat.sideOpen, chat.paneContent)
  const paneOpen = objectPane || hostPane
  const sectionsVisible = chat.sideOpen && !paneOpen

  const onStrip = useCallback(
    (section: RailSectionState) => {
      // A second click on what is already showing closes the rail: with no header of its own, the
      // strip is where a rail says "that is enough for now".
      if (chat.sideOpen && !paneOpen && !collapsedIds.has(section.name)) {
        actions.toggleSide(badgeTotal)
        return
      }
      actions.showSection(section.name, badgeTotal)
    },
    [actions, badgeTotal, chat.sideOpen, collapsedIds, paneOpen],
  )

  const closeObjectPane = useCallback(() => {
    void command('closePane')
  }, [command])

  if (!chat.authenticated) {
    return (
      <div id={controlId} className="tl-chat-rail is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  return (
    <div id={controlId} className="tl-chat-rail">
      {strip ? (
        <nav className="side-strip" aria-label={labels.sideRail}>
          {sections.map((section) => (
            <button
              type="button"
              key={section.name}
              className={`side-strip-item${
                sectionsVisible && !collapsedIds.has(section.name) ? ' is-active' : ''
              }`}
              title={section.label}
              aria-label={section.label}
              onClick={() => onStrip(section)}
            >
              <SectionIcon icon={section.icon} />
              {badges[section.name] ? (
                <span className="side-strip-badge">{badges[section.name]}</span>
              ) : null}
            </button>
          ))}
        </nav>
      ) : null}

      <aside
        className={`rail-column${sectionsVisible ? '' : ' is-hidden'}`}
        aria-label={labels.sideRail}
        aria-hidden={!sectionsVisible}
      >
        <div className="side-rail">
          {sections.map((section) => {
            const collapsed = collapsedIds.has(section.name)
            const count = badges[section.name] ?? 0
            return (
              <section
                key={section.name}
                className={`side-section${collapsed ? ' is-collapsed' : ''}`}
              >
                <button
                  type="button"
                  className="side-section-head"
                  aria-expanded={!collapsed}
                  onClick={() => actions.toggleSection(section.name)}
                >
                  <span className={`side-caret${collapsed ? '' : ' is-open'}`} aria-hidden="true" />
                  <span className="side-section-title">{section.label}</span>
                  {count > 0 ? <span className="side-section-badge">{count}</span> : null}
                </button>
                <div className={`side-section-body${collapsed ? ' is-hidden' : ''}`}>
                  <TLChild control={section.content} />
                </div>
              </section>
            )
          })}
          {sections.length === 0 ? <p className="side-empty">{labels.railEmpty}</p> : null}
        </div>
      </aside>

      {paneControl != null ? (
        <SidePane
          title={paneTitle}
          width={chat.paneWidth}
          onWidth={actions.setPaneWidth}
          onBack={closeObjectPane}
          labels={labels}
          mode={paneMode}
          hidden={!objectPane}
          placement="rail"
        >
          <TLChild control={paneControl} />
        </SidePane>
      ) : null}

      {hostPane ? (
        <SidePane
          title={chat.paneOptions?.title ?? ''}
          backLabel={chat.paneOptions?.backLabel}
          tabs={chat.paneOptions?.tabs}
          width={chat.paneWidth}
          onWidth={actions.setPaneWidth}
          onBack={actions.closePane}
          labels={labels}
          mode={chat.paneOptions?.mode ?? paneMode}
          version={chat.paneOptions?.version}
          actions={chat.paneOptions?.actions}
          placement="rail"
        >
          {chat.paneContent}
        </SidePane>
      ) : null}
    </div>
  )
}

/** A section without an icon of its own gets a panel glyph, never a letter. */
function SectionIcon({ icon }: { icon: string }) {
  if (icon) return <ThemeIcon encoded={icon} />
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <rect x="2.5" y="3" width="11" height="10" rx="1.4" />
      <path d="M2.5 6.4h11" />
    </svg>
  )
}
