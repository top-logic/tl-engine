import { React, TLChild, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { bundle } from './i18n'
import { unseenBadge } from './progress-model'
import { ThemeIcon } from './theme-icon'

const { useCallback, useEffect, useMemo, useRef, useState } = React

/** What a `<chat-dock/>` control puts into its state, as the browser reads it. */
export interface DockState {
  /** Encoded ThemeImage of the launcher glyph; empty for the built-in one. */
  icon: string
  /** The dock's name, already localised; empty for the chat's own wording. */
  label: string
  /** Whether the panel is open on first render. */
  open: boolean
}

/**
 * Reads the frame a `<chat-dock/>` control serialized.
 *
 * `dockOpen` rather than `open`, because `open` is how `<chat-rail/>` states *its* first-load
 * default and the whole bootstrap comes out of one state map (`readBootstrap`).
 */
export function readDockState(state: Record<string, unknown>): DockState {
  return {
    icon: typeof state.icon === 'string' ? state.icon : '',
    label: typeof state.label === 'string' ? state.label.trim() : '',
    open: state.dockOpen === true,
  }
}

/**
 * Whether a key press in the dock closes it.
 *
 * Escape closes an open dock and means nothing to a closed one. Whether this handler ever sees the
 * key is the other half of the rule and belongs to the DOM: a popover inside the panel that handles
 * Escape itself calls `stopPropagation`, so a user who opens the model picker and backs out gets
 * the picker back rather than losing the conversation.
 */
export function closesOnKey(key: string, open: boolean): boolean {
  return open && key === 'Escape'
}

/** What the launcher shows beside its glyph. */
export type LauncherMark = 'busy' | 'badge' | 'none'

/**
 * Which of the two marks the launcher carries.
 *
 * Both belong to the *closed* dock only: beside an open panel showing exactly those N things, "N
 * new since you last looked" says nothing, and a busy composer is already saying it is busy. A run
 * in progress wins over a count, because it is the newer fact and the count will follow it.
 */
export function launcherMark(open: boolean, busy: boolean, badge: number): LauncherMark {
  if (open) return 'none'
  if (busy) return 'busy'
  return badge > 0 ? 'badge' : 'none'
}

/**
 * `<chat-dock/>`: a floating launcher that opens the chat over the page.
 *
 * The dock is a frame. It renders a button in the corner of the window and, over the page, a card
 * holding whatever the application nested inside it - normally one `<chat>` with the session list,
 * the conversation and the rail.
 *
 * **The panel is hidden when closed, never unmounted.** Everything the conversation is - the event
 * stream, the transcript, the open run - lives in the elements inside it, and the store drops a
 * chat when its last element leaves. A dock that unmounted its panel would therefore hand the user
 * a new, empty conversation every time they opened it, and would stop hearing about a run it
 * started.
 *
 * The launcher is what a closed dock still says: the unseen count of the rail's sections, and a dot
 * while the agent works.
 *
 * **A dock steps aside for a page that shows its conversation.** Not for the transport any more -
 * a window carries as many conversations as it shows - but because these two are the *same* one:
 * two composers on one draft channel overwrite each other's text, and two rails on one chat id
 * fight over the one remembered layout. While a `<chat-conversation/>` of this chat id is mounted
 * outside a dock, this element therefore renders nothing at all: no panel, no launcher, and its
 * content is unmounted rather than hidden.
 */
export function TLChatDock({ controlId }: TLCellProps) {
  const state = useTLState()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat, actions } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  const dock = useMemo(() => readDockState(state), [state])
  const content = state.content ?? null

  const [open, setOpen] = useState(dock.open)

  /*
   * The content from the second commit on, not the first.
   *
   * On the first commit this dock cannot know yet whether the page shows its conversation: a
   * `<chat-conversation/>` announces itself from an effect, which runs after that commit. The dock
   * used to mount its content meanwhile and unmount it a moment later - long enough for its rail to
   * state its own `open="false"` over the page rail's `open="true"`, so a fresh account's rail on
   * the chat page started closed (#552). One commit later the answer is in.
   */
  const [settled, setSettled] = useState(false)
  useEffect(() => setSettled(true), [])
  const panelRef = useRef<HTMLDivElement | null>(null)
  const launcherRef = useRef<HTMLButtonElement | null>(null)

  const name = dock.label || labels.dockName
  const badge = unseenBadge(chat.badgeTotal, chat.badgeSeen)
  const mark = launcherMark(open, chat.busy, badge)

  // Opening the dock is the user looking at this conversation, so what was new stops being new -
  // the watermark moves to the total as it stands now, exactly as opening the rail moves it. Only
  // the watermark: the dock does not open the rail, so `toggleSide` would be the wrong half.
  const toggle = useCallback(() => {
    if (!open) actions.markBadgesSeen(chat.badgeTotal)
    setOpen(!open)
  }, [open, actions, chat.badgeTotal])

  // Focus follows the panel: into it when it opens, back to the launcher when it closes. Skipped on
  // the first render, so a page configured `open="true"` does not take the focus from the page.
  const wasOpen = useRef(open)
  useEffect(() => {
    if (open === wasOpen.current) return
    wasOpen.current = open
    if (open) panelRef.current?.focus()
    else launcherRef.current?.focus()
  }, [open])

  const onKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLDivElement>) => {
      // An Escape something inside already answered - the rail's drawer, the list's - closed that,
      // and closing the dock with it would take the user two steps back for one key.
      if (event.defaultPrevented || !closesOnKey(event.key, open)) return
      event.stopPropagation()
      setOpen(false)
    },
    [open],
  )

  const panelId = `${controlId}-panel`

  // A session that is not signed in cannot chat, and the servlet refuses every call it makes. A
  // launcher onto that is an invitation to a dead end, so the dock shows nothing at all - unlike an
  // element that a view gave a place to, which says why it is empty.
  //
  // The page showing this conversation itself is the other such case: there is nothing for a dock
  // to open, and a second view of one conversation is what the window cannot carry.
  if (!chat.authenticated || chat.conversationsOutsideDock > 0) {
    return <div id={controlId} className="tl-chat-dock is-stood-down" />
  }

  return (
    <div
      id={controlId}
      className={`tl-chat-dock${open ? ' is-open' : ''}`}
      onKeyDown={onKeyDown}
    >
      <div
        id={panelId}
        ref={panelRef}
        className="tl-chat-dock__panel"
        role="dialog"
        aria-label={name}
        tabIndex={-1}
        hidden={!open}
      >
        <div className="tl-chat-dock__body">
          {content != null && settled ? <TLChild control={content} /> : null}
        </div>
      </div>

      <button
        type="button"
        ref={launcherRef}
        className="tl-chat-dock__launcher"
        aria-expanded={open}
        aria-controls={panelId}
        aria-label={open ? labels.dockClose(name) : labels.dockOpen(name)}
        title={open ? labels.dockClose(name) : labels.dockOpen(name)}
        onClick={toggle}
      >
        <LauncherIcon icon={dock.icon} open={open} />
        {mark === 'busy' ? (
          <span className="tl-chat-dock__busy" role="status" aria-label={labels.dockBusy} />
        ) : null}
        {mark === 'badge' ? (
          <span className="tl-chat-dock__badge" aria-label={labels.sideRailBadge(badge)}>
            {badge}
          </span>
        ) : null}
      </button>
    </div>
  )
}

/** The configured glyph while the panel is closed, a close cross while it is open. */
function LauncherIcon({ icon, open }: { icon: string; open: boolean }) {
  if (open) {
    return (
      <svg viewBox="0 0 16 16" aria-hidden="true">
        <path d="M4 4l8 8M12 4l-8 8" />
      </svg>
    )
  }
  if (icon) return <ThemeIcon encoded={icon} />
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M13.5 8.2c0 2.6-2.5 4.7-5.5 4.7-.7 0-1.3-.1-1.9-.3L2.5 13.5l1-2.7C2.6 10 2 9.2 2 8.2 2 5.6 4.5 3.5 7.5 3.5S13.5 5.6 13.5 8.2z" />
    </svg>
  )
}
