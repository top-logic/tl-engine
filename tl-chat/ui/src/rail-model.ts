/**
 * What a `<chat-rail/>` control puts into its state, as the browser reads it.
 *
 * The rail is a shell: the frame - which sections there are, what they are called, their icons and
 * their current counts - is this state, and everything inside a section is the child element's own
 * control. Nothing here knows what a section shows.
 */

/** One configured section, as the control serialized it. */
export interface RailSectionState {
  /** Technical name; the key under which the user's collapse is remembered. */
  name: string
  /** Already localised title. */
  label: string
  /** Encoded ThemeImage of the strip icon, e.g. `css:bi bi-list-check`; empty for none. */
  icon: string
  /** Whether the section starts expanded. */
  open: boolean
  /** The child control descriptor this section renders. */
  content: unknown
}

/** Reads the section list a `<chat-rail/>` control put into its state. */
export function readRailSections(state: Record<string, unknown>): RailSectionState[] {
  const raw = state.sections
  if (!Array.isArray(raw)) return []
  const rows: RailSectionState[] = []
  for (const entry of raw) {
    if (!entry || typeof entry !== 'object') continue
    const row = entry as Record<string, unknown>
    const name = typeof row.name === 'string' ? row.name.trim() : ''
    if (!name) continue
    rows.push({
      name,
      label: typeof row.label === 'string' && row.label ? row.label : name,
      icon: typeof row.icon === 'string' ? row.icon : '',
      open: row.open === true,
      content: row.content ?? null,
    })
  }
  return rows
}

/** Reads the current badge number of each section, keyed by section name. */
export function readSectionBadges(state: Record<string, unknown>): Record<string, number> {
  const raw = state.sectionBadges
  if (!raw || typeof raw !== 'object') return {}
  const badges: Record<string, number> = {}
  for (const [name, value] of Object.entries(raw as Record<string, unknown>)) {
    const count = typeof value === 'number' ? value : Number(value)
    if (Number.isFinite(count) && count > 0) badges[name] = Math.round(count)
  }
  return badges
}

/**
 * The sections that start collapsed, or `undefined` when there are none to seed.
 *
 * Applied once per conversation; from then on the user's own toggles decide.
 */
export function collapsedFromSections(sections: RailSectionState[]): string[] | undefined {
  if (sections.length === 0) return undefined
  return sections.filter((section) => !section.open).map((section) => section.name)
}

/**
 * The sum the transcript's rail button counts down from.
 *
 * A section with no badge channel contributes nothing: it has no notion of "new", and counting its
 * rows as news would put a number on the button that never goes away.
 */
export function railBadgeTotal(
  sections: RailSectionState[],
  badges: Record<string, number>,
): number {
  return sections.reduce((sum, section) => sum + (badges[section.name] ?? 0), 0)
}

/** Whether the chat's own transient pane (`openInPane`) is showing something. */
export function paneIsOpen(sideOpen: boolean, paneContent: unknown): boolean {
  return Boolean(sideOpen && paneContent != null)
}

/** How a step of the pane's history shows its objects. */
export type PaneStepKind = 'table' | 'chart' | 'form'

/** One step of the pane's history, as `PresentationPane` serialized it. */
export interface PaneStep {
  /** Unique within the rail's life; what keeps a step's mounted control where it is. */
  id: number
  kind: PaneStepKind
  /** Already localised: the object's label, the agent's title, or the type and the count. */
  title: string
  /** The child control descriptor of the step. */
  control: unknown
}

/**
 * Reads the pane's history, bottom first. The last step is the one shown; an empty history is a
 * closed pane.
 */
export function readPaneSteps(state: Record<string, unknown>): PaneStep[] {
  const raw = state.paneSteps
  if (!Array.isArray(raw)) return []
  const steps: PaneStep[] = []
  for (const entry of raw) {
    if (!entry || typeof entry !== 'object') continue
    const row = entry as Record<string, unknown>
    if (typeof row.id !== 'number' || row.control == null) continue
    const kind = row.kind === 'chart' || row.kind === 'form' ? row.kind : 'table'
    steps.push({
      id: row.id,
      kind,
      title: typeof row.title === 'string' ? row.title : '',
      control: row.control,
    })
  }
  return steps
}

/** The strip icon of a pane step, as an encoded ThemeImage. */
export function paneStepIcon(kind: PaneStepKind): string {
  switch (kind) {
    case 'chart':
      return 'css:bi bi-bar-chart-line'
    case 'form':
      return 'css:bi bi-card-text'
    default:
      return 'css:bi bi-table'
  }
}
