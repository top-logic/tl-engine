import { React } from 'tl-react-bridge'
import type { RailRenderContext, RailSection, SidePaneState } from './extensions'
import type { bundle } from './i18n'

const { useState } = React

type Labels = ReturnType<typeof bundle>

export function SideRail({
  sections,
  ctx,
  collapsedIds,
  onToggleSection,
  labels,
}: {
  sections: RailSection[]
  ctx: RailRenderContext
  collapsedIds: Set<string>
  onToggleSection: (id: string) => void
  labels: Labels
}) {
  return (
    <div className="side-rail">
      {sections.map((section) => {
        const collapsed = collapsedIds.has(section.id)
        const count = section.count?.() ?? section.badge?.() ?? 0
        return (
          <section
            key={section.id}
            className={`side-section${section.accent ? ' is-accent' : ' is-framework'}${collapsed ? ' is-collapsed' : ''}`}
          >
            <button
              type="button"
              className="side-section-head"
              aria-expanded={!collapsed}
              onClick={() => onToggleSection(section.id)}
            >
              <span className={`side-caret${collapsed ? '' : ' is-open'}`} aria-hidden="true" />
              <span className="side-section-title">{section.title}</span>
              {count > 0 ? <span className="side-section-badge">{count}</span> : null}
              {section.contributor ? (
                <span className="side-section-by">{section.contributor}</span>
              ) : null}
            </button>
            {collapsed ? null : <div className="side-section-body">{section.render(ctx)}</div>}
          </section>
        )
      })}
      {sections.length === 0 ? <p className="side-empty">{labels.railEmpty}</p> : null}
    </div>
  )
}

export function SideStrip({
  sections,
  activeId,
  onSelect,
  labels,
}: {
  sections: RailSection[]
  activeId?: string
  onSelect: (id: string) => void
  labels: Labels
}) {
  return (
    <nav className="side-strip" aria-label={labels.sideRail}>
      {sections.map((section) => (
        <button
          type="button"
          key={section.id}
          className={`side-strip-item${section.id === activeId ? ' is-active' : ''}${section.accent ? ' is-accent' : ''}`}
          title={section.title}
          aria-label={section.title}
          onClick={() => onSelect(section.id)}
        >
          {section.icon ?? DefaultSectionIcon}
        </button>
      ))}
    </nav>
  )
}

/** Shown for a section that brought no icon of its own: a panel, not a letter. */
const DefaultSectionIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <rect x="2.5" y="3" width="11" height="10" rx="1.4" />
    <path d="M2.5 6.4h11" />
  </svg>
)

export function RailToggle({
  open,
  badge,
  labels,
  onToggle,
}: {
  open: boolean
  badge: number
  labels: Labels
  onToggle: () => void
}) {
  return (
    <button
      type="button"
      className="icon-button side-toggle"
      aria-label={labels.sideRail}
      aria-pressed={open}
      title={labels.sideRail}
      onClick={onToggle}
    >
      <svg viewBox="0 0 16 16" aria-hidden="true">
        <rect x="2" y="3" width="12" height="10" rx="1.6" />
        <path d="M10.2 3v10" />
      </svg>
      {/*
        Only while the rail is closed: "N new since you last looked" says nothing next to an open
        rail that is showing exactly those N things.
      */}
      {!open && badge > 0 ? (
        <span className="side-toggle-badge" aria-label={labels.sideRailBadge(badge)}>
          {badge}
        </span>
      ) : null}
    </button>
  )
}

export function useCollapsedSections(initial: string[]): {
  collapsedIds: Set<string>
  onToggleSection: (id: string) => void
  collapse: (id: string) => void
  expand: (id: string) => void
} {
  const [collapsed, setCollapsed] = useState<string[]>(initial)
  return {
    collapsedIds: new Set(collapsed),
    onToggleSection: (id: string) => {
      setCollapsed((current) => (current.includes(id) ? current.filter((item) => item !== id) : [...current, id]))
    },
    collapse: (id: string) => {
      setCollapsed((current) => (current.includes(id) ? current : [...current, id]))
    },
    expand: (id: string) => {
      setCollapsed((current) => current.filter((item) => item !== id))
    },
  }
}

export type { SidePaneState }
