import { React } from 'tl-react-bridge'
import type { PaneTab } from './extensions'
import type { bundle } from './i18n'

const { useCallback, useEffect, useRef, useState } = React

type Labels = ReturnType<typeof bundle>

const CONTENT_FLOOR = 360
const DEFAULT_PANE = 420
const SHEET_MQ = '(max-width: 1280px)'

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
      const stageMin = 20 * 16
      const max = Math.max(CONTENT_FLOOR, box.width - stageMin)
      onWidth(Math.min(next, max))
    },
    [onWidth, sheet],
  )

  const onPointerUp = useCallback(() => {
    dragging.current = false
  }, [])

  const paneWidth = width > 0 ? width : DEFAULT_PANE

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
          aria-orientation="vertical"
          aria-label={labels.sideSplitter}
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
