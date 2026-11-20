/**
 * The busy overlay: what the page shows while it is locked by commands taking longer than its delay
 * or by a server that does not answer (see {@link isBusyLocked}).
 *
 * <p>While the page is locked, an overlay covers the whole page, the pointer shows that the page is
 * working ({@code cursor: progress}), the overlay shows the part `Spinner` at its centre, and the
 * page's {@code <body>} is marked {@code aria-busy}. The presses and the keyboard input of the page
 * are dropped before any handler of the page sees them ({@link INPUT_EVENTS}): a press neither
 * closes an open popup nor takes the focus away from the field holding it. The focus stays where it
 * is, so the user continues where they left off once the lock is lifted.</p>
 *
 * <p>While the server does not answer ({@link isDisconnectedLocked}), the overlay says so below the
 * spinner ({@link RECONNECTING_KEY}), announced to assistive technology as a status. The text is
 * loaded while the server still answers: once it does not, it cannot be asked for its
 * translation.</p>
 *
 * <p>The overlay is a React root of its own, wrapped like every root of the bridge (see
 * {@link wrapRoot}), so a component library drawing the spinner finds its context.</p>
 */
import React, { useRef, useSyncExternalStore } from 'react';
import { createRoot } from 'react-dom/client';
import { isBusyLocked, isDisconnectedLocked, subscribeBusy } from './busy';
import { isConnected, subscribeConnection } from './connection';
import { useI18N } from './i18n';
import { usePart } from './parts';
import { wrapRoot } from './root-wrapper';

/** ID of the element the overlay is rendered into. */
const HOST_ID = 'tl-busy-host';

/** Class of the overlay covering the page while it is locked. */
const LOCK_CLASS = 'tl-busy-lock';

/** Class of the box holding the spinner at the centre of the overlay. */
const INDICATOR_CLASS = 'tl-busy-lock__indicator';

/** Class of the message below the spinner while the server does not answer. */
const MESSAGE_CLASS = 'tl-busy-lock__message';

/** I18N key of the message shown while the server does not answer. */
export const RECONNECTING_KEY = 'js.connection.reconnecting';

/** Texts of the overlay; the server translates them (I18NConstants). */
const I18N_KEYS = {
  [RECONNECTING_KEY]: 'Connection to the server lost. Reconnecting\u2026',
};

/** Attribute marking the page's {@code <body>} as busy for assistive technology. */
const ARIA_BUSY_ATTR = 'aria-busy';

/**
 * The input events dropped while the page is locked: those starting a press or a keystroke. The
 * events ending one pass, so that a gesture begun before the lock (a drag) still ends.
 */
const INPUT_EVENTS = ['keydown', 'keypress', 'pointerdown', 'mousedown', 'click', 'dblclick', 'contextmenu'] as const;

/** The element the overlay is rendered into, once {@link initBusyOverlay} ran. */
let _host: HTMLDivElement | null = null;

/** The overlay with the spinner, drawn while the page is locked, and the given message below it. */
const BusyLock: React.FC<{ message?: string }> = ({ message }) => {
  const Spinner = usePart('Spinner');
  return (
    <div className={LOCK_CLASS}>
      <div className={INDICATOR_CLASS}>
        <Spinner />
        {message && (
          <div className={MESSAGE_CLASS} role="status" aria-live="polite">
            {message}
          </div>
        )}
      </div>
    </div>
  );
};

/**
 * The message telling that the server does not answer, in the translation the server gave while it
 * still answered.
 */
function useReconnectingMessage(): string {
  const text = useI18N(I18N_KEYS)[RECONNECTING_KEY];
  const connected = useSyncExternalStore(subscribeConnection, isConnected);
  const known = useRef(text);
  if (connected) known.current = text;
  return known.current;
}

/** Draws {@link BusyLock} while the page is locked, nothing otherwise. */
const BusyOverlay: React.FC = () => {
  const locked = useSyncExternalStore(subscribeBusy, isBusyLocked);
  const disconnected = useSyncExternalStore(subscribeBusy, isDisconnectedLocked);
  const message = useReconnectingMessage();
  return locked ? <BusyLock message={disconnected ? message : undefined} /> : null;
};

/** Drops an input event of the page while it is locked, before any handler of the page sees it. */
function dropInput(event: Event): void {
  if (!isBusyLocked()) return;
  event.preventDefault();
  event.stopImmediatePropagation();
}

/** Marks the page's body as busy while the page is locked. */
function markBusy(): void {
  if (isBusyLocked()) {
    document.body.setAttribute(ARIA_BUSY_ATTR, 'true');
  } else {
    document.body.removeAttribute(ARIA_BUSY_ATTR);
  }
}

/**
 * Installs the busy overlay of the page (idempotent): its root at the end of the page's body, and
 * the listeners dropping the input of the page while it is locked.
 */
export function initBusyOverlay(): void {
  if (_host) return;
  _host = document.createElement('div');
  _host.id = HOST_ID;
  document.body.appendChild(_host);
  createRoot(_host).render(wrapRoot(<BusyOverlay />));

  for (const type of INPUT_EVENTS) {
    window.addEventListener(type, dropInput, true);
  }
  subscribeBusy(markBusy);
}
