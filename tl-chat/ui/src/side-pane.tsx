import { React } from 'tl-react-bridge'
import type { PaneTab } from './extensions'
import type { bundle } from './i18n'

const { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } = React

type Labels = ReturnType<typeof bundle>

const CONTENT_FLOOR = 360
const DEFAULT_PANE = 420
/** The thread column never goes below this, so the composer always has its own room. */
const STAGE_FLOOR = 320
/** Width of the icon strip between thread and pane, i.e. `.side-strip`'s 3.5rem. */
const STRIP_WIDTH = 56
/** Below 1280, not at it: 1280 itself is still wide enough for a column beside the thread. */
const SHEET_MQ = '(max-width: 1279.98px)'
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
  children,
}: {
  title: string
  backLabel?: string
  tabs?: PaneTab[]
  width: number
  onWidth: (width: number) => void
  onBack: () => void
  labels: Labels
  children: React.ReactNode
}) {
  const pane = useRef<HTMLDivElement>(null)
  const dragging = useRef(false)
  const [sheet, setSheet] = useState(() =>
    typeof window !== 'undefined' ? window.matchMedia(SHEET_MQ).matches : false,
  )
  /**
   * How wide the workspace is right now.
   *
   * A remembered pane width belongs to the window it was dragged in. Reopened in a narrower one it
   * would push the thread - and with it the composer - off the stage, so the stored value is a
   * wish that is granted only as far as this window allows.
   */
  const [workspaceWidth, setWorkspaceWidth] = useState(0)

  useLayoutEffect(() => {
    const workspace = pane.current?.parentElement
    if (!workspace) return undefined
    setWorkspaceWidth(Math.round(workspace.getBoundingClientRect().width))
    if (typeof ResizeObserver === 'undefined') return undefined
    const observer = new ResizeObserver(([entry]) => setWorkspaceWidth(Math.round(entry.contentRect.width)))
    observer.observe(workspace)
    return () => observer.disconnect()
  }, [])

  const maxWidth = useMemo(
    () =>
      workspaceWidth > 0
        ? Math.max(CONTENT_FLOOR, workspaceWidth - STAGE_FLOOR - STRIP_WIDTH)
        : Number.MAX_SAFE_INTEGER,
    [workspaceWidth],
  )
  const paneWidth = Math.min(width > 0 ? width : DEFAULT_PANE, maxWidth)

  useEffect(() => {
    const mq = window.matchMedia(SHEET_MQ)
    const onChange = () => setSheet(mq.matches)
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
      const workspace = pane.current?.parentElement
      if (!workspace) return
      const box = workspace.getBoundingClientRect()
      const next = Math.max(CONTENT_FLOOR, box.right - event.clientX)
      onWidth(Math.min(next, maxWidth))
    },
    [maxWidth, onWidth, sheet],
  )

  const onPointerUp = useCallback(() => {
    dragging.current = false
  }, [])

  const resize = useCallback(
    (by: number) => {
      const current = pane.current?.getBoundingClientRect().width ?? paneWidth
      onWidth(Math.min(maxWidth, Math.max(CONTENT_FLOOR, current + by)))
    },
    [maxWidth, onWidth, paneWidth],
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
        onWidth(maxWidth)
      } else if (event.key === 'End') {
        event.preventDefault()
        onWidth(CONTENT_FLOOR)
      }
    },
    [maxWidth, onWidth, resize, sheet],
  )

  return (
    <div
      ref={pane}
      className={`side-pane${sheet ? ' is-sheet' : ''}`}
      style={sheet ? undefined : { width: paneWidth, minWidth: CONTENT_FLOOR }}
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
      <header className="side-pane-bar">
        <button type="button" className="side-back" onClick={onBack}>
          ‹ {backLabel || labels.sideBack}
        </button>
        <h2 className="side-pane-title" title={title}>
          {title}
        </h2>
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
      </header>
      <div className="side-pane-body">{children}</div>
    </div>
  )
}

export const PANE_CONTENT_FLOOR = CONTENT_FLOOR
