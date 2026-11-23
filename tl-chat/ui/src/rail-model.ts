/**
 * What a `<chat-rail/>` control puts into its state, as the browser reads it.
 *
 * The rail is a shell: the frame - which sections there are, what they are called, their icons and
 * their current badges - is this state, and everything inside a section is the child element's own
 * control. Nothing here knows what a section shows.
 */

import type { BadgeJson } from 'tl-react-bridge'
import { unseenBadge } from './progress-model'

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

/** The color roles of a badge, the most urgent first. */
const ROLE_URGENCY: readonly BadgeJson.Role[] = ['error', 'warning', 'info', 'success', 'brand']

/** The largest count a badge shows with its digits; the server's `BadgeBinding.MAX_COUNT`. */
export const MAX_BADGE_COUNT = 99

/** What a badge shows for a count above {@link MAX_BADGE_COUNT}. */
export const BADGE_OVERFLOW = `${MAX_BADGE_COUNT}+`

/** State key of the badges of the sections; the server's `ChatRailControl.SECTION_BADGES`. */
export const SECTION_BADGES = 'sectionBadges'

/** Reads the badge each section currently shows, keyed by section name; a section without one is left out. */
export function readSectionBadges(state: Record<string, unknown>): Record<string, Partial<BadgeJson>> {
  const raw = state[SECTION_BADGES]
  if (!raw || typeof raw !== 'object') return {}
  const badges: Record<string, Partial<BadgeJson>> = {}
  for (const [name, value] of Object.entries(raw as Record<string, unknown>)) {
    if (!value || typeof value !== 'object') continue
    const row = value as Record<string, unknown>
    const badge: Partial<BadgeJson> = {}
    if (row.dot === true) badge.dot = true
    else if (typeof row.content === 'string' && row.content) badge.content = row.content
    else continue
    if (typeof row.role === 'string' && ROLE_URGENCY.includes(row.role as BadgeJson.Role)) {
      badge.role = row.role as BadgeJson.Role
    }
    if (typeof row.label === 'string' && row.label) badge.label = row.label
    badges[name] = badge
  }
  return badges
}

/**
 * What a badge counts: the number it shows, more than {@link MAX_BADGE_COUNT} for the overflow, and
 * 0 for a dot or a text.
 */
export function badgeCount(badge: Partial<BadgeJson> | undefined): number {
  if (!badge || badge.dot === true || typeof badge.content !== 'string') return 0
  if (badge.content === BADGE_OVERFLOW) return MAX_BADGE_COUNT + 1
  if (!/^\d+$/.test(badge.content)) return 0
  return Number(badge.content)
}

/** What a count shows on a badge: its digits, or {@link BADGE_OVERFLOW} above {@link MAX_BADGE_COUNT}. */
export function countContent(count: number): string {
  return count > MAX_BADGE_COUNT ? BADGE_OVERFLOW : String(count)
}

/** What one section's badge contributes to the transcript's rail button. */
export interface SectionCount {
  /** What the badge counts, see {@link badgeCount}; 0 for a dot or a text. */
  count: number
  /** The color role of the badge. */
  role: BadgeJson.Role
  /** Whether the badge is a dot. */
  dot: boolean
}

/**
 * What the rail's sections show for the transcript's rail button: per section name the
 * {@link SectionCount} of its badge, a section without a badge left out.
 */
export type RailBadge = Readonly<Record<string, SectionCount>>

/** The rail badge of a rail whose sections show nothing. */
export const NO_RAIL_BADGE: RailBadge = Object.freeze({})

/**
 * Per section name, the count of its badge the user has seen; a section absent has had nothing
 * seen.
 */
export type SeenCounts = Readonly<Record<string, number>>

/** The more urgent of two roles; an absent role is the brand's. */
function moreUrgent(a: BadgeJson.Role | undefined, b: BadgeJson.Role | undefined): BadgeJson.Role {
  const ra = ROLE_URGENCY.indexOf(a ?? 'brand')
  const rb = ROLE_URGENCY.indexOf(b ?? 'brand')
  return ROLE_URGENCY[Math.min(ra, rb)]
}

/**
 * Collects the badges of the sections into the {@link RailBadge} of the transcript's rail button.
 *
 * A section with no badge channel contributes nothing: it has no notion of "new", and counting its
 * rows as news would put a number on the button that never goes away.
 */
export function railBadge(
  sections: RailSectionState[],
  badges: Record<string, Partial<BadgeJson>>,
): RailBadge {
  const result: Record<string, SectionCount> = {}
  for (const section of sections) {
    const badge = badges[section.name]
    if (!badge) continue
    result[section.name] = {
      count: badgeCount(badge),
      role: badge.role ?? 'brand',
      dot: badge.dot === true,
    }
  }
  return result
}

/** Whether two rail badges say the same. */
export function sameRailBadge(left: RailBadge, right: RailBadge): boolean {
  const names = Object.keys(left)
  if (names.length !== Object.keys(right).length) return false
  return names.every((name) => {
    const a = left[name]
    const b = right[name]
    return b !== undefined && a.count === b.count && a.role === b.role && a.dot === b.dot
  })
}

/**
 * What the user sees of the given rail: per section the count of its badge, a section counting
 * nothing left out.
 */
export function seenCounts(rail: RailBadge): SeenCounts {
  const result: Record<string, number> = {}
  for (const [name, section] of Object.entries(rail)) {
    if (section.count > 0) result[name] = section.count
  }
  return result
}

/** Whether two sets of seen counts say the same; a section absent has seen 0. */
export function sameSeenCounts(left: SeenCounts, right: SeenCounts): boolean {
  const names = new Set([...Object.keys(left), ...Object.keys(right)])
  for (const name of names) {
    if ((left[name] ?? 0) !== (right[name] ?? 0)) return false
  }
  return true
}

/**
 * The badge of the transcript's rail button: what is new since the user last looked.
 *
 * Per section, the count above what was seen of it; the button shows the sum of these, in the most
 * urgent role among the sections with something new. A section showing {@link BADGE_OVERFLOW} has an
 * unknown real count: when it counts above what was seen of it, the button shows
 * {@link BADGE_OVERFLOW} as well; seen at {@link BADGE_OVERFLOW}, it adds nothing. When nothing is
 * new by count, the button shows a dot where a section shows one, in the most urgent role among those;
 * a dot stays until its section clears it. Else no badge.
 *
 * @param seen What was seen of each section, see {@link seenCounts}.
 * @param label The name a screen reader gives the badge.
 */
export function unseenRailBadge(
  rail: RailBadge,
  seen: SeenCounts,
  label: string,
): Partial<BadgeJson> | null {
  let unseen = 0
  let overflow = false
  let role: BadgeJson.Role | undefined
  let dotRole: BadgeJson.Role | undefined
  for (const [name, section] of Object.entries(rail)) {
    if (section.dot) {
      dotRole = moreUrgent(dotRole, section.role)
      continue
    }
    const fresh = unseenBadge(section.count, seen[name] ?? 0)
    if (fresh <= 0) continue
    unseen += fresh
    role = moreUrgent(role, section.role)
    if (section.count > MAX_BADGE_COUNT) overflow = true
  }
  if (unseen > 0) {
    return withRole({ content: overflow ? BADGE_OVERFLOW : countContent(unseen), label }, role)
  }
  if (dotRole) return withRole({ dot: true, label }, dotRole)
  return null
}

/** The badge with the given role, left out for the brand's, which is a badge's default. */
function withRole(badge: Partial<BadgeJson>, role: BadgeJson.Role | undefined): Partial<BadgeJson> {
  if (role && role !== 'brand') badge.role = role
  return badge
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
