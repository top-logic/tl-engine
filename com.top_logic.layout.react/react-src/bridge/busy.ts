/**
 * Whether the page is busy with a command or waits for the server to answer again: the state of
 * the busy lock.
 *
 * <p>The command channel ({@link enqueueCommand}) reports every command the user waits for when it
 * is enqueued ({@link busyCommandEnqueued}) and when its answer has arrived
 * ({@link busyCommandAnswered}). Once commands have been outstanding without a break for longer
 * than the delay the page names in the attribute {@link BUSY_DELAY_ATTR} of its {@code <body>}, the
 * page is locked ({@link isBusyLocked}): the busy overlay covers it and takes its input. The lock
 * is lifted as soon as no such command is outstanding any more. A command answered within the delay
 * shows nothing at all.</p>
 *
 * <p>The page is locked as well once it has been disconnected from the server (see
 * {@link isConnected}) for longer than {@link DISCONNECTED_LOCK_DELAY_MS}, until it is connected
 * again ({@link isDisconnectedLocked}): the input would only get lost. A short interruption shows
 * nothing. The lock is lifted only when neither a command nor the disconnection holds it.</p>
 *
 * <p>A page without the attribute is never locked: React controls embedded in a page that shows a
 * busy state of its own.</p>
 */

import { isConnected, subscribeConnection } from './connection';

/**
 * Attribute of the page's {@code <body>} naming the delay, in milliseconds, after which a command
 * the user waits for locks the page. Must match {@code ViewServlet#BUSY_DELAY_ATTR}.
 */
export const BUSY_DELAY_ATTR = 'data-busy-delay';

/**
 * How long the page has to be disconnected from the server before it is locked, so that a short
 * interruption does not flicker.
 */
export const DISCONNECTED_LOCK_DELAY_MS = 2_000;

/** The number of commands outstanding that the user waits for. */
let _outstanding = 0;

/** The timer locking the page once the delay has passed, while commands are outstanding. */
let _timer: ReturnType<typeof setTimeout> | null = null;

/** Whether commands the user waits for lock the page. */
let _commandLocked = false;

/** The timer locking the page once it has been disconnected for {@link DISCONNECTED_LOCK_DELAY_MS}. */
let _disconnectTimer: ReturnType<typeof setTimeout> | null = null;

/** Whether the disconnection from the server locks the page. */
let _disconnectedLocked = false;

/** Called when the page is locked or unlocked. */
const _listeners = new Set<() => void>();

/**
 * The delay of the page in milliseconds, read from {@link BUSY_DELAY_ATTR} each time a busy period
 * starts; {@code null} when the page names none, and is never locked.
 */
function busyDelay(): number | null {
  const value = document.body?.getAttribute(BUSY_DELAY_ATTR);
  if (value == null || value.trim() === '') return null;
  const delay = Number(value);
  return Number.isFinite(delay) && delay >= 0 ? delay : null;
}

function setCommandLocked(locked: boolean): void {
  if (_commandLocked === locked) return;
  _commandLocked = locked;
  notify();
}

function setDisconnectedLocked(locked: boolean): void {
  if (_disconnectedLocked === locked) return;
  _disconnectedLocked = locked;
  notify();
}

function notify(): void {
  for (const listener of [..._listeners]) {
    listener();
  }
}

/** Starts or ends the lock for a disconnection, as the connection state changes. */
function onConnectionChange(): void {
  if (isConnected()) {
    if (_disconnectTimer !== null) {
      clearTimeout(_disconnectTimer);
      _disconnectTimer = null;
    }
    setDisconnectedLocked(false);
    return;
  }
  if (_disconnectTimer !== null || _disconnectedLocked) return;
  if (busyDelay() === null) return;
  _disconnectTimer = setTimeout(() => {
    _disconnectTimer = null;
    setDisconnectedLocked(true);
  }, DISCONNECTED_LOCK_DELAY_MS);
}

subscribeConnection(onConnectionChange);

/**
 * Reports a command the user waits for as enqueued. The first one outstanding starts the delay.
 */
export function busyCommandEnqueued(): void {
  _outstanding++;
  if (_outstanding > 1) return;
  const delay = busyDelay();
  if (delay === null) return;
  _timer = setTimeout(() => {
    _timer = null;
    setCommandLocked(true);
  }, delay);
}

/**
 * Reports the answer of a command reported by {@link busyCommandEnqueued} as arrived. The last one
 * outstanding lifts the lock.
 */
export function busyCommandAnswered(): void {
  if (_outstanding === 0) return;
  _outstanding--;
  if (_outstanding > 0) return;
  if (_timer !== null) {
    clearTimeout(_timer);
    _timer = null;
  }
  setCommandLocked(false);
}

/**
 * Whether the page is locked, because commands the user waits for take longer than the delay, or
 * because the server does not answer.
 */
export function isBusyLocked(): boolean {
  return _commandLocked || _disconnectedLocked;
}

/** Whether the page is locked because the server has not answered for a while. */
export function isDisconnectedLocked(): boolean {
  return _disconnectedLocked;
}

/**
 * Calls the given listener whenever the page is locked or unlocked, until the returned function is
 * called.
 */
export function subscribeBusy(listener: () => void): () => void {
  _listeners.add(listener);
  return () => {
    _listeners.delete(listener);
  };
}
