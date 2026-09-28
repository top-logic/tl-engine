/**
 * What the chat does when the server no longer knows the window it is in.
 *
 * Every `/chat/*` call names its browser window, and the server resolves that name within the
 * session the request arrives with. When the session behind an open page has been replaced - a
 * login or logout in another tab of the same browser, an application restart - the page still
 * shows the old window while its requests reach a session that never rendered it. The server then
 * answers `409 {"error":"stale_window"}` (`ChatServlet#sendStaleWindow`), and the only way back is
 * to load the page again: that renders the window, and its chat, in the session the browser holds
 * now.
 *
 * Nothing read that answer before issue #574. A send was taken for "the agent is busy" and the
 * typed message vanished, and a closed event stream was re-opened once a second for as long as the
 * page stayed open. Now every call goes through {@link chatFetch}, which turns the answer into one
 * guarded reload.
 */

/**
 * Where the time of the last such reload is kept.
 *
 * The engine's own key (`STALE_UI_RELOAD_TIME_KEY` in the bridge's `command-channel.ts`), on
 * purpose: the page is reloaded for the same reason when one of its React commands is answered
 * `stale-ui`, and one guard for both is what keeps the chat and the page from reloading each
 * other in turns.
 */
const RELOAD_TIME_KEY = 'tlReactStaleUiReloadTime'

/**
 * How long after a reload a second one is refused.
 *
 * Long enough that a page which is still unknown right after being loaded again stops there
 * instead of loading itself in a loop; the same value as the engine's guard.
 */
const RELOAD_GUARD_MS = 10_000

/** Where a message the reload interrupted waits for the reloaded page, per chat. */
const UNSENT_KEY = 'tl-chat-unsent'

/** A `/chat/*` call refused because the server does not know this page's window. */
export class StaleWindowError extends Error {
  /**
   * @param reloading whether the page is being loaded again; `false` when the guard refused the
   *        reload, and the user has to be told instead
   */
  constructor(public readonly reloading: boolean) {
    super('stale_window')
    this.name = 'StaleWindowError'
  }
}

/** Whether an answer is the server's "this window is unknown" refusal. */
export async function isStaleWindow(res: Response): Promise<boolean> {
  if (res.status !== 409) return false
  try {
    const data = (await res.clone().json()) as { error?: unknown }
    return data.error === 'stale_window'
  } catch {
    return false
  }
}

/**
 * Loads the page again, unless that was just done.
 *
 * @returns whether the page is being reloaded. `false` means it was reloaded within the guard
 *          interval and is still unknown, so another reload would only repeat itself.
 */
export function reloadStaleWindow(): boolean {
  const now = Date.now()
  try {
    const last = Number(window.sessionStorage.getItem(RELOAD_TIME_KEY) ?? '0')
    if (now - last < RELOAD_GUARD_MS) {
      console.error('[tl-chat] The server still does not know this window right after a reload; not reloading again.')
      return false
    }
    window.sessionStorage.setItem(RELOAD_TIME_KEY, String(now))
  } catch {
    // Without storage there is no guard, and a page that cannot remember its last reload must not
    // risk a loop: it stays as it is.
    return false
  }
  console.warn('[tl-chat] The server no longer knows this window (session replaced). Reloading.')
  window.location.reload()
  return true
}

/**
 * `fetch` for a `/chat/*` call.
 *
 * A `stale_window` answer reloads the page and is thrown as a {@link StaleWindowError}, so no caller
 * mistakes it for an answer about the conversation. Every other answer is handed back unchanged.
 */
export async function chatFetch(url: string, init?: RequestInit): Promise<Response> {
  const res = await fetch(url, init)
  if (await isStaleWindow(res)) {
    throw new StaleWindowError(reloadStaleWindow())
  }
  return res
}

function unsentKey(chatId: string): string {
  return `${UNSENT_KEY}:${chatId.trim()}`
}

/**
 * Keeps a message the user sent into a stale window, for the reloaded page to put back.
 *
 * The send is refused and the page reloads under the user's hands; what they typed is the one
 * thing of theirs that would otherwise be lost with it.
 */
export function keepUnsent(chatId: string, text: string): void {
  if (!text.trim()) return
  try {
    window.sessionStorage.setItem(unsentKey(chatId), text)
  } catch {
    /* the message is lost with the page, as it was before */
  }
}

/** The message a reload interrupted in this chat, taken out of storage; `null` when there is none. */
export function takeUnsent(chatId: string): string | null {
  try {
    const key = unsentKey(chatId)
    const text = window.sessionStorage.getItem(key)
    if (text === null) return null
    window.sessionStorage.removeItem(key)
    return text
  } catch {
    return null
  }
}
