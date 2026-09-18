import { React } from 'tl-react-bridge'
import type { PaneAction, PaneMode, PaneTab } from './extensions'
import type { bundle } from './i18n'

const { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } = React

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
/** Below 1280, not at it: 1280 itself is still wide enough for a column beside the thread. */
export const SHEET_MQ = '(max-width: 1279.98px)'
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
  labels,
  mode,
  version,
  actions,
  hidden,
  placement = 'workspace',
  children,
}: {
  title: string
  backLabel?: string
  tabs?: PaneTab[]
  width: number
  onWidth: (width: number) => void
  onBack: () => void
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
  children: React.ReactNode
}) {
  const pane = useRef<HTMLDivElement>(null)
  const dragging = useRef(false)
  const [narrow, setNarrow] = useState(() =>
    typeof window !== 'undefined' ? window.matchMedia(SHEET_MQ).matches : false,
  )
  const sheet = placement === 'workspace' && narrow
  /**
   * How wide the workspace is right now.
   *
   * A remembered pane width belongs to the window it was dragged in. Reopened in a narrower one it
   * would push the thread - and with it the composer - off the stage, so the stored value is a
   * wish that is granted only as far as this window allows.
   */
  const [workspaceWidth, setWorkspaceWidth] = useState(0)

  /** The element whose width the pane may spend; see `placement`. */
  const boundsOf = useCallback(() => {
    const parent = pane.current?.parentElement ?? null
    return placement === 'workspace' ? parent : parent?.parentElement ?? null
  }, [placement])

  useLayoutEffect(() => {
    const workspace = boundsOf()
    if (!workspace) return undefined
    setWorkspaceWidth(Math.round(workspace.getBoundingClientRect().width))
    if (typeof ResizeObserver === 'undefined') return undefined
    const observer = new ResizeObserver(([entry]) => setWorkspaceWidth(Math.round(entry.contentRect.width)))
    observer.observe(workspace)
    return () => observer.disconnect()
  }, [boundsOf])

  const maxWidth = useMemo(
    () =>
      workspaceWidth > 0
        ? Math.max(CONTENT_FLOOR, workspaceWidth - STAGE_FLOOR - STRIP_WIDTH)
        : Number.MAX_SAFE_INTEGER,
    [workspaceWidth],
  )
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
  const paneWidth = Math.min(wanted, maxWidth)

  const setWidth = useCallback(
    (next: number) => {
      draggedIn.current = mode ?? 'card'
      onWidth(next)
    },
    [mode, onWidth],
  )

  useEffect(() => {
    const mq = window.matchMedia(SHEET_MQ)
    const onChange = () => setNarrow(mq.matches)
    mq.addEventListener('change', onChange)
    return () => mq.removeEventListener('change', onChange)
  }, [])

  const onPointerDown = useCallback(
    (event: React.PointerEvent<HTMLDivElement>) => {
      if (sheet) return
      dragging.current = true
      event.currentTarget.setPointerCapture(event.pointerId)
    },
    [sheet],
  )

  const onPointerMove = useCallback(
    (event: React.PointerEvent<HTMLDivElement>) => {
      if (!dragging.current || sheet) return
      const workspace = boundsOf()
      if (!workspace) return
      const box = workspace.getBoundingClientRect()
      const next = Math.max(CONTENT_FLOOR, box.right - event.clientX)
      setWidth(Math.min(next, maxWidth))
    },
    [boundsOf, maxWidth, setWidth, sheet],
  )

  const onPointerUp = useCallback(() => {
    dragging.current = false
  }, [])

  const resize = useCallback(
    (by: number) => {
      const current = pane.current?.getBoundingClientRect().width ?? paneWidth
      setWidth(Math.min(maxWidth, Math.max(CONTENT_FLOOR, current + by)))
    },
    [maxWidth, paneWidth, setWidth],
  )

  // A splitter that only answers the mouse is a splitter half the users cannot move.
  const onKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLDivElement>) => {
      if (sheet) return
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
        setWidth(maxWidth)
      } else if (event.key === 'End') {
        event.preventDefault()
        setWidth(CONTENT_FLOOR)
      }
    },
    [maxWidth, resize, setWidth, sheet],
  )

  return (
    <div
      ref={pane}
      className={`side-pane${sheet ? ' is-sheet' : ''}${mode === 'document' ? ' is-document' : ''}`}
      style={sheet ? undefined : { width: paneWidth, minWidth: CONTENT_FLOOR }}
      hidden={hidden}
    >
      {sheet ? null : (
        <div
          className="side-splitter"
          role="separator"
          tabIndex={0}
          aria-orientation="vertical"
          aria-label={labels.sideSplitter}
          aria-valuenow={Math.round(paneWidth)}
          aria-valuemin={CONTENT_FLOOR}
          aria-valuemax={workspaceWidth > 0 ? Math.round(maxWidth) : undefined}
          onKeyDown={onKeyDown}
          onPointerDown={onPointerDown}
          onPointerMove={onPointerMove}
          onPointerUp={onPointerUp}
        />
      )}
      {/*
        One slim row the host fills: where it came from, what it is called, which version, what can
        be done with it, and the way out. Everything below it belongs to the host.
      */}
      <header className={`side-pane-bar${mode === 'document' ? ' is-document' : ''}`}>
        <button type="button" className="side-back" onClick={onBack}>
          ‹ {backLabel || labels.sideBack}
        </button>
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
        <button type="button" className="side-pane-close" aria-label={labels.paneClose} onClick={onBack}>
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="m4 4 8 8M12 4l-8 8" />
          </svg>
        </button>
      </header>
      <div className="side-pane-body">{children}</div>
    </div>
  )
}

export const PANE_CONTENT_FLOOR = CONTENT_FLOOR
