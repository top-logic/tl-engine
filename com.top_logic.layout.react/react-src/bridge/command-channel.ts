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
      console.error('[TLReact] Command failed:', resp.status, await resp.text());
    }
  } catch (e) {
    console.error('[TLReact] Command error:', e);
  }
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
