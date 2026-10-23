import { React } from 'tl-react-bridge'
import type { PaneAction, PaneMode, PaneTab } from './extensions'
import type { bundle } from './i18n'

const { useCallback, useLayoutEffect, useMemo, useRef, useState } = React

type Labels = ReturnType<typeof bundle>

const CONTENT_FLOOR = 360
const DEFAULT_PANE = 420
/**
 * The thread column never goes below this, so the composer always has its own room.
 *
 * 240 rather than 320 since the composer became two rows: the text has a whole row to itself, and
 * the controls beneath it need `+`, the model name and the send button, which fit in this. The
 * floor used to be set by a single row that spent 275 px on chrome before the first letter.
 */
const STAGE_FLOOR = 240
/** A document takes about two thirds of the workspace, the thread the rest. */
const DOCUMENT_SHARE = 2 / 3
/** Width of the icon strip between thread and pane, i.e. `.side-strip`'s 3.5rem. */
const STRIP_WIDTH = 56
/**
 * Below 1280, not at it: 1280 itself is still wide enough for a column beside the thread.
 *
 * Of the chat's own width, not the window's - see the `chat-shell` container query in `styles.css`,
 * which carries the same number for the CSS half of the same decision (Issue #427). `App` measures
 * it once and hands the answer down, so the two halves cannot disagree.
 */
export const SHEET_MAX = 1279.98
/** One arrow-key press; Page/Home/End move further. */
const KEY_STEP = 16
const KEY_PAGE = 64

export function SidePane({
  title,
  backLabel,
  tabs,
  width,
  onWidth,
  onBack,
  onClose,
  showBack = true,
  onCollapse,
  labels,
  mode,
  version,
  actions,
  hidden,
  placement = 'workspace',
  narrow = false,
  overlay = false,
  bodyClassName,
  children,
}: {
  title: string
  backLabel?: string
  tabs?: PaneTab[]
  width: number
  onWidth: (width: number) => void
  onBack: () => void
  /** What the close cross does; `onBack` when there is no difference between the two. */
  onClose?: () => void
  /**
   * Whether there is a step to go back to. Without one the back link is left out and the cross is
   * the way out - a "back" that closes says one thing and does another.
   */
  showBack?: boolean
  /** Folds the pane away with its content kept; no fold button when absent. */
  onCollapse?: () => void
  labels: Labels
  mode?: PaneMode
  version?: string
  actions?: PaneAction[]
  /**
   * Whether the pane is out of sight.
   *
   * Hidden rather than unmounted, so what it holds keeps its state and its server-side updates
   * while the sections are showing - a pane that is rebuilt on every open shows the object as it
   * was when it was first built.
   */
  hidden?: boolean
  /**
   * Where this pane sits, which decides what bounds it and whether it becomes a sheet.
   *
   * `workspace` (the default) is inside `App`'s workspace grid, which is both the space the pane
   * shares with the thread and the region a sheet spreads over below 1280 px. `rail` is inside a
   * composed `<chat-rail/>`, which is itself sized by its children - measuring it would be circular,
   * so the row the rail sits in is what bounds the pane, and with no grid to spread over and nothing
   * to dim it stays a column at every width.
   */
  placement?: 'workspace' | 'rail'
  /** Whether the chat is below {@link SHEET_MAX}; measured by the host, meaningless for `rail`. */
  narrow?: boolean
  /**
   * Whether a `rail` pane opens over the conversation rather than beside it: the rail is a drawer
   * (#565). It then has no splitter - there is nothing beside it to take the width from - and it
   * never grows past the room the host has left of the strip.
   */
  overlay?: boolean
  /** A class for the body, for a host that lays out what it puts there itself. */
  bodyClassName?: string
  children: React.ReactNode
}) {
  const pane = useRef<HTMLDivElement>(null)
  const dragging = useRef(false)
  const sheet = placement === 'workspace' && narrow
  /** Whether the pane's width is fixed by its surroundings: a sheet, or a rail's drawer. */
  const fixed = sheet || overlay
  /**
   * How wide the workspace is right now.
   *
   * A remembered pane width belongs to the window it was dragged in. Reopened in a narrower one it
   * would push the thread - and with it the composer - off the stage, so the stored value is a
   * wish that is granted only as far as this window allows.
   */
  const [workspaceWidth, setWorkspaceWidth] = useState(0)
  /**
   * How wide a `rail` pane may become in the row it stands in, measured by {@link railRoom}; 0
   * while nobody measured.
   *
   * A fixed allowance does not do for a rail: its row holds whatever the host put beside it - a
   * session list, the conversation with its own floor, gaps - and a pane that took the workspace's
   * `STAGE_FLOOR` pushed the conversation past its floor and the row out of the page (#586).
   */
  const [room, setRoom] = useState(0)
  const measuresRoom = placement === 'rail' && !overlay

  /** The element whose width the pane may spend; see `placement`. */
  const boundsOf = useCallback(() => {
    const parent = pane.current?.parentElement ?? null
    return placement === 'workspace' ? parent : parent?.parentElement ?? null
  }, [placement])

  /** Measures the rail pane's room anew, where there is one to measure; see {@link railRoom}. */
  const measureRoom = useCallback((): number => {
    const measured = measuresRoom && pane.current && !hidden ? railRoom(pane.current) : 0
    if (measured > 0) setRoom(measured)
    return measured
  }, [measuresRoom, hidden])

  useLayoutEffect(() => {
    const workspace = boundsOf()
    if (!workspace) return undefined
    const measure = () => {
      setWorkspaceWidth(Math.round(workspace.getBoundingClientRect().width))
      measureRoom()
    }
    measure()
    if (typeof ResizeObserver === 'undefined') return undefined
    const observer = new ResizeObserver(measure)
    observer.observe(workspace)
    /*
     * A rail's room also changes when something beside it changes width while the host does not -
     * the session list folding into its strip, or opening again. Only the neighbours that do not
     * grow are watched: one that grows only fills what is left, so it changes with every step of
     * the splitter and would measure the row once per step for nothing.
     */
    if (measuresRoom) {
      for (const sibling of Array.from(workspace.children)) {
        if (sibling === pane.current?.parentElement) continue
        if ((parseFloat(getComputedStyle(sibling).flexGrow) || 0) > 0) continue
        observer.observe(sibling)
      }
    }
    return () => observer.disconnect()
  }, [boundsOf, measureRoom, measuresRoom])

  const maxWidth = useMemo(
    () =>
      measuresRoom
        ? room > 0
          ? Math.max(CONTENT_FLOOR, room)
          : Number.MAX_SAFE_INTEGER
        : workspaceWidth > 0
          ? Math.max(CONTENT_FLOOR, workspaceWidth - STAGE_FLOOR - STRIP_WIDTH)
          : Number.MAX_SAFE_INTEGER,
    [measuresRoom, room, workspaceWidth],
  )
  /**
   * The limit an interaction works against: measured afresh for a rail, whose neighbours may have
   * changed since the last measurement without anything telling the pane.
   */
  const currentMax = useCallback(() => {
    const measured = measureRoom()
    return measured > 0 ? Math.max(CONTENT_FLOOR, measured) : maxWidth
  }, [measureRoom, maxWidth])
  /**
   * Which mode the remembered width belongs to.
   *
   * One stored number serves both modes, and a card's 420 px is the wrong size for a document. The
   * width therefore counts only for the mode it was dragged in - starting as a card's, which is
   * what every stored value from before document mode is - and the other mode falls back to its own
   * default until the user drags it there too.
   */
  const draggedIn = useRef<PaneMode>('card')
  const own = (mode ?? 'card') === draggedIn.current
  const wanted =
    width > 0 && own
      ? width
      : mode === 'document' && workspaceWidth > 0
        ? Math.round(workspaceWidth * DOCUMENT_SHARE)
        : DEFAULT_PANE
  /**
   * The width a pointer drag shows while it lasts; null outside a drag.
   *
   * A drag reports a width on every pointer move, and each one the store receives is a preference
   * write. The pane therefore follows the pointer on its own and hands the store the width it came
   * to rest at, once, when the pointer lets go (#588).
   */
  const [dragWidth, setDragWidth] = useState<number | null>(null)
  /** The same width for the pointer's release, which may come before the last move rendered. */
  const dragged = useRef<number | null>(null)
  const paneWidth = Math.min(dragWidth ?? wanted, maxWidth)

  const setWidth = useCallback(
    (next: number) => {
      draggedIn.current = mode ?? 'card'
      // Whole pixels: a pointer and a measured room both come in fractions, and the stored
      // preference is an integer the server refuses a fraction for (#588).
      onWidth(Math.round(next))
    },
    [mode, onWidth],
  )

  const onPointerDown = useCallback(
    (event: React.PointerEvent<HTMLDivElement>) => {
      if (fixed) return
      dragging.current = true
      measureRoom()
      event.currentTarget.setPointerCapture(event.pointerId)
    },
    [fixed, measureRoom],
  )

  const onPointerMove = useCallback(
    (event: React.PointerEvent<HTMLDivElement>) => {
      if (!dragging.current || fixed) return
      const workspace = boundsOf()
      if (!workspace) return
      const box = workspace.getBoundingClientRect()
      const next = Math.max(CONTENT_FLOOR, box.right - event.clientX)
      dragged.current = Math.min(next, maxWidth)
      setDragWidth(dragged.current)
    },
    [boundsOf, maxWidth, fixed],
  )

  /** Ends a drag - let go or cancelled alike - and stores the width it ended at, if it moved. */
  const onPointerUp = useCallback(() => {
    if (!dragging.current) return
    dragging.current = false
    if (dragged.current !== null) setWidth(dragged.current)
    dragged.current = null
    setDragWidth(null)
  }, [setWidth])

  const resize = useCallback(
    (by: number) => {
      const current = pane.current?.getBoundingClientRect().width ?? paneWidth
      setWidth(Math.min(currentMax(), Math.max(CONTENT_FLOOR, current + by)))
    },
    [currentMax, paneWidth, setWidth],
  )

  // A splitter that only answers the mouse is a splitter half the users cannot move.
  const onKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLDivElement>) => {
      if (fixed) return
      const step =
        event.key === 'ArrowLeft'
          ? KEY_STEP
          : event.key === 'ArrowRight'
            ? -KEY_STEP
            : event.key === 'PageUp'
              ? KEY_PAGE
              : event.key === 'PageDown'
                ? -KEY_PAGE
                : 0
      if (step !== 0) {
        event.preventDefault()
        resize(step)
        return
      }
      if (event.key === 'Home') {
        event.preventDefault()
        setWidth(currentMax())
      } else if (event.key === 'End') {
        event.preventDefault()
        setWidth(CONTENT_FLOOR)
      }
    },
    [currentMax, resize, setWidth, fixed],
  )

  return (
    <div
      ref={pane}
      className={`side-pane${sheet ? ' is-sheet' : ''}${mode === 'document' ? ' is-document' : ''}`}
      style={
        sheet
          ? undefined
          : overlay
            ? { width: paneWidth, minWidth: 0 }
            : { width: paneWidth, minWidth: CONTENT_FLOOR }
      }
      hidden={hidden}
    >
      {fixed ? null : (
        <div
          className="side-splitter"
          role="separator"
          tabIndex={0}
          aria-orientation="vertical"
          aria-label={labels.sideSplitter}
          aria-valuenow={Math.round(paneWidth)}
          aria-valuemin={CONTENT_FLOOR}
          aria-valuemax={maxWidth < Number.MAX_SAFE_INTEGER ? Math.round(maxWidth) : undefined}
          onKeyDown={onKeyDown}
          onPointerDown={onPointerDown}
          onPointerMove={onPointerMove}
          onPointerUp={onPointerUp}
          onPointerCancel={onPointerUp}
          onLostPointerCapture={onPointerUp}
        />
      )}
      {/*
        One slim row the host fills: where it came from, what it is called, which version, what can
        be done with it, and the way out. Everything below it belongs to the host.
      */}
      <header className={`side-pane-bar${mode === 'document' ? ' is-document' : ''}`}>
        {showBack ? (
          <button type="button" className="side-back" onClick={onBack}>
            ‹ {backLabel || labels.sideBack}
          </button>
        ) : null}
        <h2 className="side-pane-title" title={title}>
          {title}
        </h2>
        {version ? <span className="side-pane-version">{version}</span> : null}
        {tabs && tabs.length > 0 ? (
          <div className="side-pane-tabs">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                type="button"
                className={`side-pane-tab${tab.active ? ' is-active' : ''}`}
                onClick={() => tab.onSelect?.()}
              >
                {tab.label}
              </button>
            ))}
          </div>
        ) : null}
        {actions && actions.length > 0 ? (
          <div className="side-pane-actions">
            {actions.map((action) => (
              <button
                key={action.id}
                type="button"
                className="side-pane-action"
                onClick={action.onSelect}
                title={action.label}
              >
                {action.icon ?? null}
                <span>{action.label}</span>
              </button>
            ))}
          </div>
        ) : null}
        {onCollapse ? (
          <button
            type="button"
            className="side-pane-collapse"
            aria-label={labels.paneCollapse}
            title={labels.paneCollapse}
            onClick={onCollapse}
          >
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <path d="m3.5 4 4 4-4 4M8.5 4l4 4-4 4" />
            </svg>
          </button>
        ) : null}
        <button
          type="button"
          className="side-pane-close"
          aria-label={labels.paneClose}
          title={labels.paneClose}
          onClick={onClose ?? onBack}
        >
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="m4 4 8 8M12 4l-8 8" />
          </svg>
        </button>
      </header>
      <div className={bodyClassName ? `side-pane-body ${bodyClassName}` : 'side-pane-body'}>{children}</div>
    </div>
  )
}

export const PANE_CONTENT_FLOOR = CONTENT_FLOOR

/**
 * How wide a `rail` pane may become before its row overflows: the host's content width less the
 * rest of the rail, the gaps, the margins and every neighbour at its own floor (#586).
 *
 * A neighbour's floor is not something a stylesheet states: the conversation's panel has
 * `min-width: auto`, i.e. the conversation's own `min-width` reached through its content. So the
 * row is asked instead. For the length of one synchronous layout the pane is made as wide as the
 * whole host, which overfills the row, so every neighbour that can give way gives way to exactly its
 * floor; the widths are read and the pane's own width is put back before anything is painted.
 *
 * Returns 0 where there is nothing to measure: no host, or a pane that is not laid out.
 */
export function railRoom(pane: HTMLElement): number {
  const rail = pane.parentElement
  const host = rail?.parentElement
  if (!rail || !host) return 0
  const hostStyle = getComputedStyle(host)
  const inner = host.clientWidth - px(hostStyle.paddingLeft) - px(hostStyle.paddingRight)
  if (inner <= 0 || pane.getBoundingClientRect().width <= 0) return 0

  const previous = pane.style.width
  pane.style.width = `${inner}px`
  try {
    const railOwn = rail.getBoundingClientRect().width - pane.getBoundingClientRect().width
    let taken = railOwn + outerMargins(rail)
    // Only a row puts its children beside the rail; anywhere else the rail has the host to itself.
    if (/flex/.test(hostStyle.display) && hostStyle.flexDirection.startsWith('row')) {
      const beside = Array.from(host.children).filter((child) => {
        if (child === rail) return false
        const style = getComputedStyle(child)
        return style.display !== 'none' && style.position !== 'absolute' && style.position !== 'fixed'
      })
      for (const child of beside) {
        taken += child.getBoundingClientRect().width + outerMargins(child)
      }
      taken += (px(hostStyle.columnGap) || 0) * beside.length
    }
    return Math.max(0, Math.floor(inner - taken))
  } finally {
    pane.style.width = previous
  }
}

function outerMargins(element: Element): number {
  const style = getComputedStyle(element)
  return px(style.marginLeft) + px(style.marginRight)
}

function px(value: string): number {
  return parseFloat(value) || 0
}
