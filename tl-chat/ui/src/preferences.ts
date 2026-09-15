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

function path(windowId: string): string {
  const sep = '/chat/preferences'
  return `${apiBase()}${sep}?windowId=${encodeURIComponent(windowId)}`
}

/**
 * Reads the per-user chat layout preferences out of the control state.
 *
 * The bootstrap comes with the control (`AgentChatControl.putState`), so there is no `GET` to make:
 * the values are already in the first render.
 */
export function parsePreferences(state: Record<string, unknown>): ChatPreferences {
  return {
    railOpen: state.railOpen === true,
    paneWidth: typeof state.paneWidth === 'number' && state.paneWidth > 0 ? state.paneWidth : 0,
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
