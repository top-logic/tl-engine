import type { ChatCommand } from './chat-stream'

/** Technical command that stores a chat's layout. Must match `ChatControl#CMD_SAVE_LAYOUT`. */
export const CMD_SAVE_LAYOUT = 'saveLayout'

/** Argument: the key the layout is stored under. Must match `ChatControl#ARG_LAYOUT_KEY`. */
export const ARG_LAYOUT_KEY = 'layoutKey'

/** Argument: the layout. Must match `ChatControl#ARG_LAYOUT`. */
export const ARG_LAYOUT = 'layout'

/** Field of the layout: the rail open or not. Must match `ChatPreferencesDto#RAIL_OPEN__PROP`. */
const LAYOUT_RAIL_OPEN = 'railOpen'

/** Field of the layout: the pane width. Must match `ChatPreferencesDto#PANE_WIDTH__PROP`. */
const LAYOUT_PANE_WIDTH = 'paneWidth'

/** Field of the layout: the badges seen. Must match `ChatPreferencesDto#BADGE_SEEN__PROP`. */
const LAYOUT_BADGE_SEEN = 'badgeSeen'

/** Field of the layout: the sections' open flags. Must match `ChatPreferencesDto#SECTIONS__PROP`. */
const LAYOUT_SECTIONS = 'sections'

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

/**
 * Reads the stored layout out of the control state.
 *
 * The bootstrap comes with the control (`ChatViewBinding.writeRenderState`), so there is no `GET`
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

/**
 * Writes the layout through PersonalConfiguration ({@link CMD_SAVE_LAYOUT}).
 *
 * `layoutKey` is where the layout is *stored* - one layout per `chat-id` - which the command names
 * beside the conversation the command is sent from.
 */
export async function savePreferences(
  command: ChatCommand,
  layoutKey: string,
  prefs: ChatPreferences,
): Promise<void> {
  const layout: Record<string, unknown> = {
    [LAYOUT_PANE_WIDTH]: prefs.paneWidth,
    [LAYOUT_BADGE_SEEN]: prefs.badgeSeen,
    [LAYOUT_SECTIONS]: prefs.sections,
  }
  // Omitted rather than false: absent is what "the user has not decided" means, and false would
  // freeze the configured default out for good.
  if (prefs.railOpen !== null) layout[LAYOUT_RAIL_OPEN] = prefs.railOpen
  try {
    await command(CMD_SAVE_LAYOUT, { [ARG_LAYOUT_KEY]: layoutKey, [ARG_LAYOUT]: layout })
  } catch {
    // A failed write leaves the in-memory choice; the next session restores the last stored one.
  }
}
