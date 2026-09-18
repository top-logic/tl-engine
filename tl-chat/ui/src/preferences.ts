import { apiBase } from './api'

export interface ChatPreferences {
  /** Whether the right rail is open. Collapsed by default. */
  railOpen: boolean
  /** Width of the artefact pane in pixels; 0 means the default. */
  paneWidth: number
  /** Sum of section badges last stored when the rail was opened. */
  badgeSeen: number
}

const DEFAULTS: ChatPreferences = { railOpen: false, paneWidth: 0, badgeSeen: 0 }

/** Whether two layouts say the same thing, so an unchanged one is not written back. */
export function sameLayout(left: ChatPreferences, right: ChatPreferences): boolean {
  return (
    left.railOpen === right.railOpen &&
    left.paneWidth === right.paneWidth &&
    left.badgeSeen === right.badgeSeen
  )
}

function path(windowId: string): string {
  const sep = '/chat/preferences'
  return `${apiBase()}${sep}?windowId=${encodeURIComponent(windowId)}`
}

/**
 * Reads the per-user chat layout preferences out of the control state.
 *
 * The bootstrap comes with the control (`ChatViewBinding.writeLayoutState`), so there is no `GET`
 * to make: the values are already in the first render. The control writes them again on every
 * render, so the values a reloaded page starts from are the ones stored now, not the ones the
 * window was first rendered with.
 *
 * `paneWidthDefault` is the width `<chat-rail pane-width>` configures. It applies only while the
 * user has not dragged the splitter themselves.
 */
export function parsePreferences(state: Record<string, unknown>): ChatPreferences {
  const stored = typeof state.paneWidth === 'number' && state.paneWidth > 0 ? state.paneWidth : 0
  const configured =
    typeof state.paneWidthDefault === 'number' && state.paneWidthDefault > 0
      ? state.paneWidthDefault
      : 0
  return {
    railOpen: state.railOpen === true,
    paneWidth: stored > 0 ? stored : configured,
    badgeSeen: typeof state.badgeSeen === 'number' && state.badgeSeen >= 0 ? state.badgeSeen : 0,
  }
}

/** Writes the layout preferences through PersonalConfiguration (see /chat/preferences). */
export async function savePreferences(windowId: string, prefs: ChatPreferences): Promise<void> {
  try {
    await fetch(path(windowId), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(prefs),
    })
  } catch {
    // A failed write leaves the in-memory choice; the next session restores the last stored one.
  }
}

export const defaultPreferences = DEFAULTS
