/**
 * The message of a send the page was reloaded over (#574).
 *
 * A send is a command of a chat control, and the bridge's command channel reloads a page whose UI
 * the server no longer knows (a login or logout in another tab of the same browser, an application
 * restart) instead of answering the command. What the user typed is the one thing of theirs that
 * would be lost with the page, so the store keeps it here before it sends, and the reloaded composer
 * puts it back.
 */

/** Where a message the reload interrupted waits for the reloaded page, per chat. */
const UNSENT_KEY = 'tl-chat-unsent'

function unsentKey(chatId: string): string {
  return `${UNSENT_KEY}:${chatId.trim()}`
}

/** Keeps a message the user is sending, for a page reloaded before it went to put back. */
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
