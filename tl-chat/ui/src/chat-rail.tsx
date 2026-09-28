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

const { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } = React

/** What Tab moves between inside an open drawer. */
const FOCUSABLE = [
  'button:not([disabled]):not([tabindex="-1"])',
  'input:not([disabled])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  'a[href]',
  '[tabindex="0"]',
  '[contenteditable="true"]',
].join(', ')

/** The root font size in px, which is what the `drawer-below` rem value is measured in. */
function remInPx(): number {
  return parseFloat(getComputedStyle(document.documentElement).fontSize) || 16
}

/** Whether an element takes up room on the page, i.e. is not inside something `display: none`. */
function isShown(element: HTMLElement): boolean {
  return element.getClientRects().length > 0
}

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
  // Two pane children: the <pane> shows one object, the <list-pane> shows several. A rail that
  // configured only one of them uses it for whatever stands on the channel.
  //
  // Both stay mounted and only one is shown, which is the same rule the sections follow: a control
  // the browser drops loses the row source it was observing, and the one that comes back is the one
  // that was computed when the pane was still empty.
  const singleControl = state.pane ?? null
  const listControl = state.listPane ?? null
  const showList = state.paneMany === true ? listControl != null : singleControl == null && listControl != null
  const paneControl = showList ? listControl : singleControl

  // Tell the chat a rail is on the page, so `<chat-conversation/>` knows its badge button has
  // something to open. Unregisters on unmount.
  useEffect(() => actions.registerRail(), [actions])

  // What this rail configures. The store applies it per section name and only where the user has
  // decided nothing, so a user's own collapse survives a reload and a new tab (#423).
  const collapsedDefaults = useMemo(() => collapsedFromSections(sections), [sections])
  useEffect(() => {
    if (!collapsedDefaults) return
    actions.applyCollapsedDefaults(collapsedDefaults)
  }, [actions, collapsedDefaults])

  useEffect(() => actions.applyRailDefault(openDefault), [actions, openDefault])

  const badgeTotal = useMemo(() => railBadgeTotal(sections, badges), [sections, badges])
  useEffect(() => actions.publishBadgeTotal(badgeTotal), [actions, badgeTotal])

  /*
   * A drawer where the host has no room for the rail's column (#565).
   *
   * The rail shares its row with the conversation, whose #511 floor does not give way, and usually
   * with a chat list. Opened as a column, a section took 18.75 rem and the pane more beside all of
   * them, so below the width they need together the row overflowed into the page's scroller - at a
   * phone's width the column opened entirely off-screen. Below `drawerBelow` (rem, measured on the
   * host) the rail therefore keeps only its strip in the flow, and a section or the pane opens over
   * the conversation, like a `<data-list>` does below its own `drawer-below` (#548).
   *
   * Whether the drawer is open lives in the store rather than here, because the conversation's rail
   * button opens and closes it too; the user's column choice is left alone while it is a drawer.
   *
   * Measured before paint, so a narrow page does not first lay out the column it cannot hold.
   */
  const drawerBelow = typeof state.drawerBelow === 'number' ? state.drawerBelow : 0
  const root = useRef<HTMLDivElement | null>(null)
  const [drawer, setDrawer] = useState(false)
  /** Where the open drawer ends: the host's inline end less the strip, in px. */
  const [drawerEnd, setDrawerEnd] = useState(0)

  /*
   * The drawer is placed against the host, so it has to be told how far from the host's inline end
   * the strip it opens beside begins. Measured when the host is resized and again whenever the
   * drawer opens: while a host narrows, its other columns turn into drawers in the same frame, and
   * a measurement taken before they have is one of a row that still overflows.
   */
  const measureEnd = useCallback(() => {
    const rail = root.current
    const host = rail?.parentElement
    if (!rail || !host) return
    const box = host.getBoundingClientRect()
    const railBox = rail.getBoundingClientRect()
    const rtl = getComputedStyle(host).direction === 'rtl'
    const end = rtl
      ? railBox.right - (box.left + host.clientLeft)
      : box.left + host.clientLeft + host.clientWidth - railBox.left
    setDrawerEnd(Math.max(0, Math.round(end)))
  }, [])

  useLayoutEffect(() => {
    const host = root.current?.parentElement
    if (drawerBelow <= 0 || !host) {
      setDrawer(false)
      return
    }
    const measure = () => {
      setDrawer(host.clientWidth < drawerBelow * remInPx())
      measureEnd()
    }
    measure()
    const observer = new ResizeObserver(measure)
    observer.observe(host)
    return () => observer.disconnect()
  }, [drawerBelow, measureEnd])

  // This rail's own key in the store's set of drawers, so another rail's leaving cannot clear it.
  const token = useRef({})
  useEffect(() => {
    const rail = token.current
    actions.setRailDrawer(rail, drawer)
    return () => actions.setRailDrawer(rail, false)
  }, [actions, drawer])

  const collapsedIds = useMemo(() => new Set(chat.collapsedSections), [chat.collapsedSections])
  /** Whether the rail is open: its drawer while it is one, else the user's column choice. */
  const railOpen = drawer ? chat.railDrawer && chat.railDrawerOpen : chat.sideOpen
  /** The pane the `input` channel opens. */
  const objectPane = state.paneOpen === true && paneControl != null
  /** The chat's own transient pane, which a citation renderer still opens with `openInPane`. */
  const hostPane = paneIsOpen(railOpen, chat.paneContent)
  const paneOpen = objectPane || hostPane
  const sectionsVisible = railOpen && !paneOpen
  /** Whether the drawer shows something over the conversation. The object pane opens it too. */
  const drawerOpen = drawer && (railOpen || objectPane)

  const closeObjectPane = useCallback(() => {
    void command('closePane')
  }, [command])

  /** Closes an open drawer: the section column, and the pane it shows. */
  const closeDrawer = useCallback(() => {
    if (objectPane) closeObjectPane()
    actions.closeSide()
  }, [actions, closeObjectPane, objectPane])

  const onStrip = useCallback(
    (section: RailSectionState) => {
      // A second click on what is already showing closes the rail: with no header of its own, the
      // strip is where a rail says "that is enough for now".
      if (railOpen && !paneOpen && !collapsedIds.has(section.name)) {
        actions.toggleSide(badgeTotal)
        return
      }
      actions.showSection(section.name, badgeTotal)
    },
    [actions, badgeTotal, railOpen, collapsedIds, paneOpen],
  )

  /*
   * Focus follows the drawer: in, to the first thing in it, when it opens; back to what opened it -
   * a strip icon or the conversation's rail button - when it closes, if focus was inside it then.
   *
   * And in again whenever what the drawer shows changes under the focus: a row picked in a section
   * opens the pane and hides the row that had the focus, the pane's "back" hides itself, and a focus
   * that fell to the page would take Escape and Tab out of the drawer with it.
   */
  const opener = useRef<HTMLElement | null>(null)
  const wasOpen = useRef(false)
  useLayoutEffect(() => {
    const rail = root.current
    if (!rail) return
    const active = document.activeElement
    if (drawerOpen) {
      const opening = !wasOpen.current
      wasOpen.current = true
      if (opening) {
        measureEnd()
        opener.current = active instanceof HTMLElement && active !== document.body ? active : null
      }
      const panel = rail.querySelector<HTMLElement>(
        '.rail-column:not(.is-hidden), .side-pane:not([hidden])',
      )
      const kept =
        !opening && active instanceof HTMLElement && rail.contains(active) && isShown(active)
      if (kept) return
      const first = [...(panel?.querySelectorAll<HTMLElement>(FOCUSABLE) ?? [])].find(isShown)
      first?.focus()
      return
    }
    if (!wasOpen.current) return
    wasOpen.current = false
    const back = opener.current
    opener.current = null
    const lost = !active || active === document.body || rail.contains(active)
    if (back && back.isConnected && lost) back.focus()
  }, [drawerOpen, paneOpen, measureEnd])

  /*
   * Keys while the drawer is open. Escape closes it - unless it is clearing a search field that has
   * text in it, which is what Escape does there first. Tab wraps inside the rail, because the scrim
   * says the conversation behind it is out of reach for now.
   */
  const onDrawerKey = (event: React.KeyboardEvent<HTMLDivElement>) => {
    if (!drawerOpen) return
    if (event.key === 'Escape') {
      const target = event.target as HTMLInputElement
      if (target.type === 'search' && target.value) return
      event.preventDefault()
      closeDrawer()
      return
    }
    if (event.key !== 'Tab' || !root.current) return
    const focusable = [...root.current.querySelectorAll<HTMLElement>(FOCUSABLE)].filter(isShown)
    if (focusable.length === 0) return
    const first = focusable[0]
    const last = focusable[focusable.length - 1]
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault()
      last.focus()
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault()
      first.focus()
    }
  }

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
    <div
      id={controlId}
      ref={root}
      className={`tl-chat-rail${drawer ? ' is-drawer' : ''}${drawerOpen ? ' is-drawer-open' : ''}`}
      style={drawer ? ({ '--rail-drawer-end': `${drawerEnd}px` } as React.CSSProperties) : undefined}
      onKeyDown={onDrawerKey}
    >
      {drawerOpen ? (
        // A pointer's way out. Keyboard and screen reader users have the strip and Escape.
        <div className="rail-drawer-scrim" aria-hidden="true" onClick={closeDrawer} />
      ) : null}
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
          overlay={drawer}
        >
          {singleControl != null ? (
            <div className={showList ? 'is-hidden' : undefined}>
              <TLChild control={singleControl} />
            </div>
          ) : null}
          {listControl != null ? (
            <div className={showList ? undefined : 'is-hidden'}>
              <TLChild control={listControl} />
            </div>
          ) : null}
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
          overlay={drawer}
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
