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
 * Every call goes through {@link chatFetch}, which turns the answer into one guarded reload, so a
 * send is not taken for "the agent is busy" and the typed message waits for the reloaded page.
 */

import { isPageLeaving, reloadForStaleUi } from 'tl-react-bridge'

/** Where a message the reload interrupted waits for the reloaded page, per chat. */
const UNSENT_KEY = 'tl-chat-unsent'

/** A `/chat/*` call refused because the server does not know this page's window. */
export class StaleWindowError extends Error {
  /**
   * @param reloading whether the page is being replaced - loaded again, or left for another page;
   *        `false` when the guard refused the reload, and the user has to be told instead
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
 * The bridge's {@link reloadForStaleUi} does it: one guard for the chat and the page's own React
 * commands, which the server answers `stale-ui` for the same reason, keeps the two from reloading
 * each other in turns. A page that is leaving already ({@link isPageLeaving}) is not reloaded: the
 * answer belongs to a request the navigation overtook, and the next page takes over.
 *
 * @returns whether the page is being replaced - reloaded now, or on its way to another page.
 *          `false` means it was reloaded within the guard interval and is still unknown, so another
 *          reload would only repeat itself.
 */
export function reloadStaleWindow(): boolean {
  return isPageLeaving() || reloadForStaleUi()
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
