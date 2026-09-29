/**
 * The single client-side channel for control commands.
 *
 * <p>Every command POST to the server goes through one strict FIFO chain: a command is sent only
 * after the previous command's response has arrived. Two interactions can therefore never overtake
 * each other on the wire — a field value flushed right before a button click is guaranteed to be
 * applied server-side before the click command executes.</p>
 *
 * <p>The channel also tracks form fields that hold a debounced, not-yet-transmitted value (see
 * {@link registerPendingFlush}). Enqueueing an action command (any command except
 * {@link CMD_VALUE_CHANGED} itself) first flushes those pending values into the chain, so the
 * action always operates on the complete form state — e.g. submitting a login dialog with Enter
 * while the password field still holds its latest keystrokes in the debounce window.</p>
 */

/** Command name for transmitting a form-field value to the server. */
export const CMD_VALUE_CHANGED = 'valueChanged';

/**
 * Machine-readable error code from the server: the server-side UI state for this page no longer
 * exists (the session was replaced underneath the open page by a login or logout, or the server
 * was restarted). Must match {@code ReactServlet#ERROR_CODE_STALE_UI}.
 */
const ERROR_CODE_STALE_UI = 'stale-ui';

/** Session-storage key holding the time of the last stale-UI reload, guarding against loops. */
const STALE_UI_RELOAD_TIME_KEY = 'tlReactStaleUiReloadTime';

/** Minimum time between two stale-UI reloads. */
const STALE_UI_RELOAD_GUARD_MS = 10_000;

/** JSON payload of a control command POST. */
export interface CommandPayload {
  controlId: string;
  command: string;
  windowName: string;
  arguments: Record<string, unknown>;
}

/**
 * Tail of the strict FIFO command chain. Each enqueued command awaits its predecessor's
 * response before being sent; {@link post} never rejects, so the chain never breaks.
 */
let _chain: Promise<void> = Promise.resolve();

/**
 * Flush callbacks of fields currently holding a debounced value that has not been sent yet.
 * Each callback sends the field's pending value (through this channel) and deregisters itself.
 */
const _pendingFlushes = new Set<() => Promise<void>>();

/** Sends one command POST, reporting failures to the console without throwing. */
async function post(url: string, payload: CommandPayload): Promise<void> {
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!resp.ok) {
      const body = await resp.text();
      if (isStaleUiError(body)) {
        reloadOnStaleUi();
        return;
      }
      console.error('[TLReact] Command failed:', resp.status, body);
    }
  } catch (e) {
    console.error('[TLReact] Command error:', e);
  }
}

/** Whether an error response body carries the {@link ERROR_CODE_STALE_UI} code. */
function isStaleUiError(body: string): boolean {
  try {
    return (JSON.parse(body) as { errorCode?: string }).errorCode === ERROR_CODE_STALE_UI;
  } catch {
    return false;
  }
}

/**
 * Re-bootstraps the page after the server rejected a command because it no longer knows this
 * page's UI state — e.g. the session was replaced by a login or logout in another tab, or the
 * server was restarted while the page stayed open. Without the reload, every further interaction
 * would be rejected and the page would appear frozen.
 *
 * <p>Guarded so a stale answer arriving right after a reload does not cause a reload loop.</p>
 */
function reloadOnStaleUi(): void {
  const lastReload = Number(window.sessionStorage.getItem(STALE_UI_RELOAD_TIME_KEY) ?? '0');
  if (Date.now() - lastReload < STALE_UI_RELOAD_GUARD_MS) {
    console.error('[TLReact] Server-side UI state still missing right after a reload; not reloading again.');
    return;
  }
  window.sessionStorage.setItem(STALE_UI_RELOAD_TIME_KEY, String(Date.now()));
  console.warn('[TLReact] Server no longer knows this page (session replaced or window discarded). Reloading.');
  window.location.reload();
}

/**
 * Enqueues a command into the strict FIFO chain.
 *
 * <p>For an action command (anything but {@link CMD_VALUE_CHANGED}), all pending debounced field
 * values are flushed first. The flushes enqueue their {@link CMD_VALUE_CHANGED} sends
 * synchronously, so they precede the action in the chain and are processed by the server before
 * the action runs.</p>
 *
 * @returns a promise resolving when this command's server response has arrived; it never
 *          rejects (transport failures are logged).
 */
export function enqueueCommand(url: string, payload: CommandPayload): Promise<void> {
  if (payload.command !== CMD_VALUE_CHANGED) {
    void flushPendingFieldValues();
  }
  const result = _chain.then(() => post(url, payload));
  _chain = result;
  return result;
}

/**
 * Registers a field's flush callback while the field holds a debounced, not-yet-sent value.
 *
 * <p>The callback must be identity-stable for the lifetime of the field and must deregister
 * itself (via {@link unregisterPendingFlush}) when it runs, so a flush is never invoked
 * twice for the same pending value.</p>
 */
export function registerPendingFlush(flush: () => Promise<void>): void {
  _pendingFlushes.add(flush);
}

/** Removes a flush callback registered via {@link registerPendingFlush}. */
export function unregisterPendingFlush(flush: () => Promise<void>): void {
  _pendingFlushes.delete(flush);
}

/**
 * Sends every pending debounced field value now.
 *
 * <p>The sends are enqueued synchronously during this call; the returned promise resolves when
 * all of them have been answered by the server. Callers that only need ordering relative to a
 * command they enqueue afterwards need not await it — the FIFO chain already guarantees that.</p>
 */
export function flushPendingFieldValues(): Promise<void> {
  if (_pendingFlushes.size === 0) {
    return Promise.resolve();
  }
  const flushes = Array.from(_pendingFlushes);
  return Promise.all(flushes.map((flush) => flush())).then(() => undefined);
}
