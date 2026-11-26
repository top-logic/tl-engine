import { act } from 'react';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** The delay the tests' page names, in milliseconds. */
const DELAY = 500;

/** A command as a control sends it. */
const PAYLOAD = { controlId: 'c1', command: 'click', windowName: 'v1a2b', arguments: {} };

const COMMAND_URL = '/ctx/react-api/command';

/** The attribute of the body naming the delay; spelled out to check the wire name. */
const DELAY_ATTR = 'data-busy-delay';

/** The address the server is checked with while the page is disconnected. */
const CHECK_URL = '/ctx/react-api/poll?windowName=v1a2b&peek=true';

/** The translations the server gives. */
let translations: Record<string, string> = {};

/** The answers of the commands sent so far, in order, to be released by the test. */
let answers: Array<() => void> = [];

/** The network errors of the commands sent so far, in order, to be raised by the test. */
let failures: Array<() => void> = [];

/**
 * Lets every command wait for its answer until the test releases it. A check of the server stays
 * unanswered, translations are answered at once.
 */
function holdAnswers(): void {
  answers = [];
  failures = [];
  vi.stubGlobal('fetch', vi.fn((url: string) => {
    if (url.includes('react-api/i18n')) {
      return Promise.resolve(new Response(JSON.stringify(translations), { status: 200 }));
    }
    if (url === CHECK_URL) {
      return new Promise<Response>(() => {});
    }
    return new Promise<Response>((resolve, reject) => {
      answers.push(() => resolve(new Response('{"success":true}', { status: 200 })));
      failures.push(() => reject(new TypeError('Failed to fetch')));
    });
  }));
}

/** Lets the promise reactions run that are due, so that a released answer reaches the channel. */
async function settle(): Promise<void> {
  for (let i = 0; i < 10; i++) {
    await Promise.resolve();
  }
}

/** Releases the answer of the command sent as the given one and waits until it has been processed. */
async function answer(index: number): Promise<void> {
  // The chain sends a command only once its predecessor is answered: wait until it has been sent.
  await settle();
  answers[index]();
  await settle();
}

/** Lets the command sent as the given one fail without an answer and waits until it has been processed. */
async function fail(index: number): Promise<void> {
  await settle();
  failures[index]();
  await settle();
}

/** Fresh module state per test: busy and channel state must not leak from one test into the next. */
async function load() {
  vi.resetModules();
  const busy = await import('./busy');
  const channel = await import('./command-channel');
  const connection = await import('./connection');
  connection.setCheckUrl(CHECK_URL);
  return { ...busy, ...channel, ...connection };
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
});

beforeEach(() => {
  vi.useFakeTimers();
  translations = {};
  holdAnswers();
  vi.spyOn(console, 'warn').mockImplementation(() => {});
  document.body.setAttribute(DELAY_ATTR, String(DELAY));
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
  document.body.removeAttribute(DELAY_ATTR);
});

describe('the busy lock', () => {
  it('reads the delay from the attribute named by BUSY_DELAY_ATTR', async () => {
    const { BUSY_DELAY_ATTR } = await load();
    expect(BUSY_DELAY_ATTR).toBe(DELAY_ATTR);
  });

  it('does not lock the page while a command is answered within the delay', async () => {
    const { enqueueCommand, isBusyLocked } = await load();
    void enqueueCommand(COMMAND_URL, PAYLOAD);

    vi.advanceTimersByTime(DELAY - 1);
    expect(isBusyLocked()).toBe(false);

    await answer(0);
    vi.advanceTimersByTime(DELAY);
    expect(isBusyLocked()).toBe(false);
  });

  it('locks the page once a command takes longer than the delay', async () => {
    const { enqueueCommand, isBusyLocked, subscribeBusy } = await load();
    const listener = vi.fn();
    subscribeBusy(listener);
    void enqueueCommand(COMMAND_URL, PAYLOAD);

    vi.advanceTimersByTime(DELAY);
    expect(isBusyLocked()).toBe(true);
    expect(listener).toHaveBeenCalledTimes(1);
  });

  it('lifts the lock only when the chain has drained', async () => {
    const { enqueueCommand, isBusyLocked } = await load();
    void enqueueCommand(COMMAND_URL, PAYLOAD);
    void enqueueCommand(COMMAND_URL, PAYLOAD);
    vi.advanceTimersByTime(DELAY);
    expect(isBusyLocked()).toBe(true);

    await answer(0);
    expect(isBusyLocked()).toBe(true);

    await answer(1);
    expect(isBusyLocked()).toBe(false);
  });

  it('starts the delay anew for the next command after the chain has drained', async () => {
    const { enqueueCommand, isBusyLocked } = await load();
    void enqueueCommand(COMMAND_URL, PAYLOAD);
    vi.advanceTimersByTime(DELAY - 100);
    await answer(0);

    void enqueueCommand(COMMAND_URL, PAYLOAD);
    vi.advanceTimersByTime(DELAY - 1);
    expect(isBusyLocked()).toBe(false);
    vi.advanceTimersByTime(1);
    expect(isBusyLocked()).toBe(true);
  });

  it('never locks the page for a background command', async () => {
    const { enqueueCommand, isBusyLocked, BACKGROUND_COMMAND } = await load();
    void enqueueCommand(COMMAND_URL, PAYLOAD, BACKGROUND_COMMAND);

    vi.advanceTimersByTime(10 * DELAY);
    expect(isBusyLocked()).toBe(false);
  });

  it('counts a command queued behind a background command from the moment it is enqueued', async () => {
    const { enqueueCommand, isBusyLocked } = await load();
    void enqueueCommand(COMMAND_URL, PAYLOAD, { background: true });
    void enqueueCommand(COMMAND_URL, PAYLOAD);

    vi.advanceTimersByTime(DELAY);
    expect(isBusyLocked()).toBe(true);

    await answer(0);
    expect(isBusyLocked()).toBe(true);
    await answer(1);
    expect(isBusyLocked()).toBe(false);
  });

  it('never locks a page that names no delay', async () => {
    document.body.removeAttribute(DELAY_ATTR);
    const { enqueueCommand, isBusyLocked } = await load();
    void enqueueCommand(COMMAND_URL, PAYLOAD);

    vi.advanceTimersByTime(60_000);
    expect(isBusyLocked()).toBe(false);
  });
});

describe('the lock of a disconnected page', () => {
  it('locks the page once it has been disconnected for DISCONNECTED_LOCK_DELAY_MS', async () => {
    const { reportUnreachable, isBusyLocked, isDisconnectedLocked, DISCONNECTED_LOCK_DELAY_MS } = await load();
    expect(DISCONNECTED_LOCK_DELAY_MS).toBe(2_000);

    reportUnreachable();
    vi.advanceTimersByTime(DISCONNECTED_LOCK_DELAY_MS - 1);
    expect(isBusyLocked()).toBe(false);

    vi.advanceTimersByTime(1);
    expect(isBusyLocked()).toBe(true);
    expect(isDisconnectedLocked()).toBe(true);
  });

  it('does not lock the page for a shorter interruption', async () => {
    const { reportUnreachable, reportReachable, isBusyLocked, DISCONNECTED_LOCK_DELAY_MS } = await load();

    reportUnreachable();
    vi.advanceTimersByTime(DISCONNECTED_LOCK_DELAY_MS - 1);
    reportReachable();
    vi.advanceTimersByTime(10 * DISCONNECTED_LOCK_DELAY_MS);

    expect(isBusyLocked()).toBe(false);
  });

  it('lifts the lock when the page is connected again', async () => {
    const { reportUnreachable, reportReachable, isBusyLocked, DISCONNECTED_LOCK_DELAY_MS } = await load();
    reportUnreachable();
    vi.advanceTimersByTime(DISCONNECTED_LOCK_DELAY_MS);

    reportReachable();

    expect(isBusyLocked()).toBe(false);
  });

  it('lifts the lock only when neither a command nor the disconnection holds it', async () => {
    const { enqueueCommand, reportUnreachable, reportReachable, isBusyLocked, DISCONNECTED_LOCK_DELAY_MS } = await load();
    void enqueueCommand(COMMAND_URL, PAYLOAD);
    reportUnreachable();
    vi.advanceTimersByTime(DISCONNECTED_LOCK_DELAY_MS);
    expect(isBusyLocked()).toBe(true);

    // The command ends without an answer: the server still does not answer.
    await fail(0);
    expect(isBusyLocked()).toBe(true);

    reportReachable();
    expect(isBusyLocked()).toBe(false);
  });

  it('keeps the lock of a command when the page is connected again', async () => {
    const { enqueueCommand, reportUnreachable, reportReachable, isBusyLocked, DISCONNECTED_LOCK_DELAY_MS } = await load();
    void enqueueCommand(COMMAND_URL, PAYLOAD);
    reportUnreachable();
    vi.advanceTimersByTime(DISCONNECTED_LOCK_DELAY_MS);

    reportReachable();
    expect(isBusyLocked()).toBe(true);

    await answer(0);
    expect(isBusyLocked()).toBe(false);
  });

  it('never locks a page that names no delay', async () => {
    document.body.removeAttribute(DELAY_ATTR);
    const { reportUnreachable, isBusyLocked } = await load();

    reportUnreachable();
    vi.advanceTimersByTime(60_000);

    expect(isBusyLocked()).toBe(false);
  });
});

describe('the busy overlay', () => {
  /** Fresh overlay with the channel it watches, the spinner drawn by a test part. */
  async function loadOverlay() {
    const modules = await load();
    const parts = await import('./parts');
    parts.registerPart('Spinner', (props) => <span className="test-spinner" {...props} />);
    const overlay = await import('./busy-overlay');
    act(() => overlay.initBusyOverlay());
    return modules;
  }

  afterEach(() => {
    document.body.replaceChildren();
  });

  it('covers the page with the spinner, marks it busy and drops the keyboard input while locked', async () => {
    const { enqueueCommand } = await loadOverlay();
    const field = document.createElement('input');
    document.body.appendChild(field);
    const keys = vi.fn();
    field.addEventListener('keydown', keys);

    void enqueueCommand(COMMAND_URL, PAYLOAD);
    act(() => vi.advanceTimersByTime(DELAY));

    expect(document.querySelector('.tl-busy-lock .test-spinner')).not.toBeNull();
    expect(document.body.getAttribute('aria-busy')).toBe('true');

    const press = new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, cancelable: true });
    field.dispatchEvent(press);
    expect(keys).not.toHaveBeenCalled();
    expect(press.defaultPrevented).toBe(true);

    await act(() => answer(0));

    expect(document.querySelector('.tl-busy-lock')).toBeNull();
    expect(document.body.hasAttribute('aria-busy')).toBe(false);
    field.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, cancelable: true }));
    expect(keys).toHaveBeenCalledTimes(1);
  });

  it('shows nothing for a command answered within the delay', async () => {
    const { enqueueCommand } = await loadOverlay();
    void enqueueCommand(COMMAND_URL, PAYLOAD);
    act(() => vi.advanceTimersByTime(DELAY - 1));
    await act(() => answer(0));
    act(() => vi.advanceTimersByTime(DELAY));

    expect(document.querySelector('.tl-busy-lock')).toBeNull();
    expect(document.body.hasAttribute('aria-busy')).toBe(false);
  });

  it('tells that the server does not answer, and drops the input, once disconnected for the delay', async () => {
    const { reportUnreachable, DISCONNECTED_LOCK_DELAY_MS } = await loadOverlay();
    const field = document.createElement('input');
    document.body.appendChild(field);
    const keys = vi.fn();
    field.addEventListener('keydown', keys);

    act(() => reportUnreachable());
    act(() => vi.advanceTimersByTime(DISCONNECTED_LOCK_DELAY_MS - 1));
    expect(document.querySelector('.tl-busy-lock')).toBeNull();

    act(() => vi.advanceTimersByTime(1));

    const message = document.querySelector('.tl-busy-lock .tl-busy-lock__message');
    expect(message?.textContent).toBe('Connection to the server lost. Reconnecting\u2026');
    expect(message?.getAttribute('role')).toBe('status');
    expect(document.querySelector('.tl-busy-lock .test-spinner')).not.toBeNull();
    expect(document.body.getAttribute('aria-busy')).toBe('true');
    const press = new KeyboardEvent('keydown', { key: 'a', bubbles: true, cancelable: true });
    field.dispatchEvent(press);
    expect(keys).not.toHaveBeenCalled();
  });

  it('disappears when the page is connected again', async () => {
    const { reportUnreachable, reportReachable, DISCONNECTED_LOCK_DELAY_MS } = await loadOverlay();
    act(() => reportUnreachable());
    act(() => vi.advanceTimersByTime(DISCONNECTED_LOCK_DELAY_MS));
    expect(document.querySelector('.tl-busy-lock')).not.toBeNull();

    act(() => reportReachable());

    expect(document.querySelector('.tl-busy-lock')).toBeNull();
    expect(document.body.hasAttribute('aria-busy')).toBe(false);
  });

  it('shows the translation the server gave while it still answered', async () => {
    translations = { 'js.connection.reconnecting': 'Verbindung zum Server unterbrochen.' };
    const { reportUnreachable, DISCONNECTED_LOCK_DELAY_MS } = await loadOverlay();
    // The overlay renders before the first mount(), which makes the context path known on a page
    // without context attributes.
    const { setMountContext } = await import('./page-context');
    setMountContext('/ctx', 'v1a2b');
    await act(() => vi.advanceTimersByTimeAsync(0));

    act(() => reportUnreachable());
    act(() => vi.advanceTimersByTime(DISCONNECTED_LOCK_DELAY_MS));

    expect(document.querySelector('.tl-busy-lock__message')?.textContent).toBe('Verbindung zum Server unterbrochen.');
  });

  it('shows no message for a command that takes long', async () => {
    const { enqueueCommand } = await loadOverlay();
    void enqueueCommand(COMMAND_URL, PAYLOAD);
    act(() => vi.advanceTimersByTime(DELAY));

    expect(document.querySelector('.tl-busy-lock')).not.toBeNull();
    expect(document.querySelector('.tl-busy-lock__message')).toBeNull();
  });
});
