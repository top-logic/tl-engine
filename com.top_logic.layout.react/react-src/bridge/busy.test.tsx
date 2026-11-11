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

/** The answers of the commands sent so far, in order, to be released by the test. */
let answers: Array<() => void> = [];

/** Lets every command wait for its answer until the test releases it. */
function holdAnswers(): void {
  answers = [];
  vi.stubGlobal('fetch', vi.fn(() => new Promise<Response>((resolve) => {
    answers.push(() => resolve(new Response('{"success":true}', { status: 200 })));
  })));
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

/** Fresh module state per test: busy and channel state must not leak from one test into the next. */
async function load() {
  vi.resetModules();
  const busy = await import('./busy');
  const channel = await import('./command-channel');
  return { ...busy, ...channel };
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
});

beforeEach(() => {
  vi.useFakeTimers();
  holdAnswers();
  document.body.setAttribute(DELAY_ATTR, String(DELAY));
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
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
});
