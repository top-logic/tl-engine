import { apiBase } from './api'

/**
 * What the user decided about a chat's layout - not what the page currently shows.
 *
 * A fact the user never touched is absent, and the element's configured default applies to it:
 * `<chat-rail open="true">` opens the rail for a user who has never closed it and leaves it closed
 * for one who has (#423). Same for a `<section open>` and for `<chat-rail pane-width>`.
 */
export interface ChatPreferences {
  /** Whether the right rail is open, or `null` while the user has not decided. */
  railOpen: boolean | null
  /** Width the user dragged the artefact pane to; 0 while they never did. */
  paneWidth: number
  /** Sum of section badges last stored when the rail was opened. */
  badgeSeen: number
  /** Per section name, whether the user left it open. A section never toggled is absent. */
  sections: Record<string, boolean>
}

/** Whether two layouts say the same thing, so an unchanged one is not written back. */
export function sameLayout(left: ChatPreferences, right: ChatPreferences): boolean {
  if (
    left.railOpen !== right.railOpen ||
    left.paneWidth !== right.paneWidth ||
    left.badgeSeen !== right.badgeSeen
  ) {
    return false
  }
  const names = new Set([...Object.keys(left.sections), ...Object.keys(right.sections)])
  for (const name of names) {
    if (left.sections[name] !== right.sections[name]) return false
  }
  return true
}

function path(windowId: string, layoutKey: string): string {
  const sep = '/chat/preferences'
  return `${apiBase()}${sep}?windowId=${encodeURIComponent(windowId)}&chatId=${encodeURIComponent(layoutKey)}`
}

/**
 * Reads the stored layout out of the control state.
 *
 * The bootstrap comes with the control (`ChatViewBinding.writeLayoutState`), so there is no `GET`
 * to make: the values are already in the first render. The control writes them again on every
 * render, so a reloaded page starts from the layout stored now rather than the one the window was
 * first rendered with.
 */
export function parsePreferences(state: Record<string, unknown>): ChatPreferences {
  const sections: Record<string, boolean> = {}
  const raw = state.sectionChoices
  if (raw && typeof raw === 'object') {
    for (const [name, open] of Object.entries(raw as Record<string, unknown>)) {
      if (typeof open === 'boolean') sections[name] = open
    }
  }
  return {
    railOpen: typeof state.railOpen === 'boolean' ? state.railOpen : null,
    paneWidth: typeof state.paneWidth === 'number' && state.paneWidth > 0 ? state.paneWidth : 0,
    badgeSeen: typeof state.badgeSeen === 'number' && state.badgeSeen >= 0 ? state.badgeSeen : 0,
    sections,
  }
}

/** The width an element configures for its pane; applies only until the user drags the splitter. */
export function configuredPaneWidth(state: Record<string, unknown>): number {
  return typeof state.paneWidthDefault === 'number' && state.paneWidthDefault > 0
    ? state.paneWidthDefault
    : 0
}

/**
 * The sections that are collapsed, given the configured defaults and what the user decided.
 *
 * A section the user toggled follows their choice; every other one follows its configuration. That
 * order is per section name, so adding a section to a rail gives it its configured state even for a
 * user who has long since arranged the others.
 */
export function collapsedSections(
  defaults: string[],
  choices: Record<string, boolean>,
): string[] {
  const collapsed = defaults.filter((name) => choices[name] !== true)
  for (const [name, open] of Object.entries(choices)) {
    if (!open && !collapsed.includes(name)) collapsed.push(name)
  }
  return collapsed
}

/** Writes the layout through PersonalConfiguration (see /chat/preferences). */
export async function savePreferences(
  windowId: string,
  layoutKey: string,
  prefs: ChatPreferences,
): Promise<void> {
  const body: Record<string, unknown> = {
    paneWidth: prefs.paneWidth,
    badgeSeen: prefs.badgeSeen,
    sections: prefs.sections,
  }
  // Omitted rather than false: absent is what "the user has not decided" means, and false would
  // freeze the configured default out for good.
  if (prefs.railOpen !== null) body.railOpen = prefs.railOpen
  try {
    await fetch(path(windowId, layoutKey), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(body),
    })
  } catch {
    // A failed write leaves the in-memory choice; the next session restores the last stored one.
  }
}
