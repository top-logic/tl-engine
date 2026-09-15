import type { RailSection } from './extensions'

/** XML names of the three framework sections, in the order an unconfigured rail shows them. */
export const DEFAULT_SECTION_NAMES = ['fortschritt', 'kontext', 'quellen'] as const

const FRAMEWORK_IDS: Record<string, string> = {
  fortschritt: 'progress',
  kontext: 'context',
  quellen: 'sources',
  progress: 'progress',
  context: 'context',
  sources: 'sources',
}

export interface ConfiguredSection {
  name: string
  id: string
  open: boolean
  badge: boolean
}

/** Browser id for an XML section name. */
export function internalSectionId(name: string): string {
  const trimmed = name.trim()
  return FRAMEWORK_IDS[trimmed] ?? trimmed
}

/** Reads the section list a {@code <chat-rail/>} control put into its state. */
export function readRailSections(state: Record<string, unknown>): ConfiguredSection[] {
  const raw = state.sections
  if (!Array.isArray(raw)) return []
  const rows: ConfiguredSection[] = []
  for (const entry of raw) {
    if (!entry || typeof entry !== 'object') continue
    const row = entry as Record<string, unknown>
    const name = typeof row.name === 'string' ? row.name.trim() : ''
    if (!name) continue
    const id = typeof row.id === 'string' && row.id.trim() ? row.id.trim() : internalSectionId(name)
    rows.push({
      name,
      id,
      open: row.open === true,
      badge: row.badge !== false,
    })
  }
  return rows
}

/**
 * Resolves the configured names against the framework sections and the host registry.
 *
 * An empty list is today's order: Fortschritt, registered host sections, Kontext, Quellen.
 * An unknown name is dropped; it does not throw.
 */
export function resolveRailSections(
  configured: ConfiguredSection[],
  framework: { progress: RailSection; context: RailSection; sources: RailSection },
  registered: RailSection[],
): { sections: RailSection[]; dropped: string[] } {
  const dropped: string[] = []
  const byId = new Map<string, RailSection>()
  byId.set(framework.progress.id, framework.progress)
  byId.set(framework.context.id, framework.context)
  byId.set(framework.sources.id, framework.sources)
  for (const section of registered) byId.set(section.id, section)

  if (configured.length === 0) {
    return {
      sections: [framework.progress, ...registered, framework.context, framework.sources],
      dropped,
    }
  }

  const sections: RailSection[] = []
  for (const row of configured) {
    const found = byId.get(row.id) ?? byId.get(row.name)
    if (!found) {
      dropped.push(row.name)
      continue
    }
    sections.push(row.badge ? found : { ...found, badge: undefined })
  }
  return { sections, dropped }
}

/**
 * Section ids that start collapsed. {@code undefined} leaves the store's App default
 * ({@code context} closed).
 */
export function collapsedFromConfig(configured: ConfiguredSection[]): string[] | undefined {
  if (configured.length === 0) return undefined
  return configured.filter((row) => !row.open).map((row) => row.id)
}

/** Whether the pane is showing host content. */
export function paneIsOpen(sideOpen: boolean, paneContent: unknown): boolean {
  return Boolean(sideOpen && paneContent != null)
}
