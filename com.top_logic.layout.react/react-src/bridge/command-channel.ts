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
 *
 * <p>A page that is leaving (see {@link isPageLeaving}) sends no command any more: the server it
 * would reach may already hold the state the navigation brings, and an answer to such a command
 * says nothing about the page that comes next.</p>
 */

import { isPageLeaving, leavePage, reloadForStaleUi } from './page-leave';
import { expectEvents } from './sse-client';

/** Command name for transmitting a form-field value to the server. */
export const CMD_VALUE_CHANGED = 'valueChanged';

/**
 * Command name for reporting a form-field value the user has finished entering. Must match
 * {@code ReactFormFieldControl#SUBMIT_COMMAND}.
 */
export const CMD_SUBMIT = 'submit';

/**
 * Command name for reporting an edit the user has finished in a typed field. Must match
 * {@code ReactFormFieldControl#COMMIT_COMMAND}.
 */
export const CMD_COMMIT = 'commit';

/**
 * Command name for reporting a file the client refused to upload because it exceeds the configured
 * size limit. Must match {@code ReactServlet#CMD_UPLOAD_REJECTED}.
 */
export const CMD_UPLOAD_REJECTED = 'uploadRejected';

/**
 * Machine-readable error code from the server: the server-side UI state for this page no longer
 * exists (the session was replaced underneath the open page by a login or logout, or the server
 * was restarted). Must match {@code ReactServlet#ERROR_CODE_STALE_UI}.
 */
const ERROR_CODE_STALE_UI = 'stale-ui';

/**
 * Header of a command's answer: the number of the last event the server wrote for the window.
 * Must match {@code ReactServlet#LAST_EVENT_HEADER}.
 */
const LAST_EVENT_HEADER = 'X-TL-Last-Event';

/**
 * Parsed JSON body a command is answered with.
 *
 * <p>Every answer carries {@code success}; a command that reports back beyond that adds its own
 * fields, which the caller reads by the name both sides agree on.</p>
 */
export type CommandResponse = Record<string, unknown>;

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
let _chain: Promise<unknown> = Promise.resolve();

/**
 * Flush callbacks of fields currently holding a debounced value that has not been sent yet.
 * Each callback sends the field's pending value (through this channel) and deregisters itself.
 */
const _pendingFlushes = new Set<() => Promise<void>>();

/**
 * Sends one command POST, reporting failures to the console without throwing.
 *
 * @returns the answer of a successful command, or {@code undefined} when the command failed, was
 *          answered with something other than a JSON object, or was not sent because the page is
 *          leaving.
 */
async function post(url: string, payload: CommandPayload): Promise<CommandResponse | undefined> {
  if (isPageLeaving()) return undefined;
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const body = await resp.text();
    const lastEvent = resp.headers.get(LAST_EVENT_HEADER);
    if (lastEvent) expectEvents(Number(lastEvent));
    if (!resp.ok) {
      if (isStaleUiError(body)) {
        // The server no longer knows this page - e.g. the session was replaced by a login or
        // logout in another tab, or the server was restarted while the page stayed open. Without
        // loading the page again, every further interaction would be rejected.
        reloadForStaleUi();
        return undefined;
      }
      console.error('[TLReact] Command failed:', resp.status, body);
      return undefined;
    }
    return parseResponse(body);
  } catch (e) {
    console.error('[TLReact] Command error:', e);
    return undefined;
  }
}

/** The answer of a command as a JSON object, or {@code undefined} if it is not one. */
function parseResponse(body: string): CommandResponse | undefined {
  try {
    const parsed: unknown = JSON.parse(body);
    return typeof parsed === 'object' && parsed !== null ? (parsed as CommandResponse) : undefined;
  } catch {
    return undefined;
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
 * Options of a command sent through {@link enqueueCommand}.
 */
export interface CommandOptions {
  /**
   * Whether the pending debounced field values are sent before the command; absent: `true`. A
   * command that reads nothing of the form but its own arguments - a query for the suggestions to
   * the text typed, which carries that text - passes `false`, so that it does not send a value a
   * field holds back until it is left.
   */
  flushFields?: boolean;
}

/**
 * Enqueues a command into the strict FIFO chain.
 *
 * <p>For an action command (anything but {@link CMD_VALUE_CHANGED}), all pending debounced field
 * values are flushed first, unless {@link CommandOptions.flushFields} says otherwise. The flushes
 * enqueue their {@link CMD_VALUE_CHANGED} sends synchronously, so they precede the action in the
 * chain and are processed by the server before the action runs.</p>
 *
 * @returns a promise resolving with this command's answer once it has arrived, or with
 *          {@code undefined} when the command failed or the page started leaving before its turn
 *          came; it never rejects (transport failures are logged).
 */
export function enqueueCommand(
  url: string,
  payload: CommandPayload,
  options?: CommandOptions,
): Promise<CommandResponse | undefined> {
  if (payload.command !== CMD_VALUE_CHANGED && options?.flushFields !== false) {
    void flushPendingFieldValues();
  }
  const result = _chain.then(() => post(url, payload));
  _chain = result;
  return result;
}

/**
 * Leaves the page (see {@link leavePage}) once every command sent so far has been answered.
 *
 * <p>A navigation the server asks for through its event stream may be the outcome of a command
 * whose answer is still on its way, and that answer may carry a cookie the next page needs - the
 * changed session ID of a login. Leaving the page before would cancel the answer and load the next
 * page without the cookie. Commands enqueued after this call are not waited for.</p>
 *
 * @param url the address to load, as for {@link leavePage}.
 */
export function leavePageWhenAnswered(url?: string): void {
  void _chain.then(() => leavePage(url));
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
