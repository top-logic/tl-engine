import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

/** Session-storage key of a tab that polls, see `POLLING_KEY` in `sse-client.ts`. */
const POLLING_KEY = 'tl-react-sse-polling';

/** The stand-in event streams the client opened, newest last. */
let sources: FakeEventSource[];

/** An event stream that only records how it is used; a test drives its callbacks. */
class FakeEventSource {
  static readonly CONNECTING = 0;
  static readonly OPEN = 1;
  static readonly CLOSED = 2;

  readyState = FakeEventSource.CONNECTING;
  onmessage: ((event: MessageEvent) => void) | null = null;
  onerror: (() => void) | null = null;

  constructor(readonly url: string) {
    sources.push(this);
  }

  close(): void {
    this.readyState = FakeEventSource.CLOSED;
  }

  /** The browser aborts the stream, as it does when a navigation starts. */
  abort(): void {
    this.readyState = FakeEventSource.CLOSED;
    this.onerror?.();
  }
}

/** The page transition listeners the modules of the current test put on the window. */
const windowListeners: Array<[string, EventListenerOrEventListenerObject]> = [];

/** Fresh module state per test, with the window listeners of the loaded modules recorded. */
async function load() {
  vi.resetModules();
  const add = window.addEventListener.bind(window);
  const spy = vi.spyOn(window, 'addEventListener').mockImplementation(
    (type: string, listener: EventListenerOrEventListenerObject, options?: boolean | AddEventListenerOptions) => {
      windowListeners.push([type, listener]);
      add(type, listener, options);
    });
  try {
    const client = await import('./sse-client');
    const pageLeave = await import('./page-leave');
    return { ...client, ...pageLeave };
  } finally {
    spy.mockRestore();
  }
}

/** A message of the stream, so that the first-message check passes. */
function heartbeat(source: FakeEventSource): void {
  source.readyState = FakeEventSource.OPEN;
  source.onmessage?.(new MessageEvent('message', { data: JSON.stringify(['Heartbeat', {}]), lastEventId: '1' }));
}

beforeEach(() => {
  sources = [];
  window.sessionStorage.clear();
  vi.useFakeTimers();
  vi.stubGlobal('EventSource', FakeEventSource);
  vi.stubGlobal('fetch', vi.fn(() => new Promise(() => {})));
  vi.spyOn(console, 'warn').mockImplementation(() => {});
  vi.spyOn(console, 'log').mockImplementation(() => {});
});

afterEach(() => {
  for (const [type, listener] of windowListeners.splice(0)) {
    window.removeEventListener(type, listener);
  }
  vi.useRealTimers();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

describe('an event stream the browser aborts for a navigation', () => {
  it('does not make the tab poll', async () => {
    const { connect, isPageLeaving } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    heartbeat(sources[0]);

    window.dispatchEvent(new Event('beforeunload'));
    sources[0].abort();
    vi.advanceTimersByTime(1_000);

    expect(window.sessionStorage.getItem(POLLING_KEY)).toBeNull();
    expect(fetch).not.toHaveBeenCalled();
    expect(isPageLeaving()).toBe(false);
  });

  it('is opened again when the page is still there after the navigation it announced', async () => {
    const { connect, NAVIGATION_START_GRACE_MS } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    heartbeat(sources[0]);

    window.dispatchEvent(new Event('beforeunload'));
    sources[0].abort();
    vi.advanceTimersByTime(NAVIGATION_START_GRACE_MS);

    expect(sources).toHaveLength(2);
    expect(sources[1].url).toBe('/ctx/react-api/events?windowName=v1a2b');
    expect(window.sessionStorage.getItem(POLLING_KEY)).toBeNull();
  });

  it('stays closed when the page is left', async () => {
    const { connect, NAVIGATION_START_GRACE_MS } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    heartbeat(sources[0]);

    window.dispatchEvent(new Event('beforeunload'));
    sources[0].abort();
    window.dispatchEvent(new PageTransitionEvent('pagehide', { persisted: false }));
    vi.advanceTimersByTime(NAVIGATION_START_GRACE_MS);

    expect(sources).toHaveLength(1);
    expect(window.sessionStorage.getItem(POLLING_KEY)).toBeNull();
  });
});

describe('an event stream that fails while a navigation may be starting', () => {
  it('is closed rather than left to reconnect, and opened again afterwards', async () => {
    const { connect, NAVIGATION_START_GRACE_MS } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    heartbeat(sources[0]);

    window.dispatchEvent(new Event('beforeunload'));
    // The server ended the stream: the browser would reconnect it by itself.
    sources[0].readyState = FakeEventSource.CONNECTING;
    sources[0].onerror?.();

    expect(sources[0].readyState).toBe(FakeEventSource.CLOSED);
    expect(sources).toHaveLength(1);

    vi.advanceTimersByTime(NAVIGATION_START_GRACE_MS);

    expect(sources).toHaveLength(2);
    expect(window.sessionStorage.getItem(POLLING_KEY)).toBeNull();
  });
});

describe('a tab that polls', () => {
  /** A poll answer with no events. */
  function emptyAnswer(): Response {
    return new Response(JSON.stringify({ last: 0, token: 't', events: [] }), { status: 200 });
  }

  it('reads no events while a navigation may be starting', async () => {
    window.sessionStorage.setItem(POLLING_KEY, '1');
    let release: () => void = () => {};
    const fetchMock = vi.fn(() => new Promise<Response>((resolve) => {
      release = () => resolve(emptyAnswer());
    }));
    vi.stubGlobal('fetch', fetchMock);
    const { connect, NAVIGATION_START_GRACE_MS } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    await vi.advanceTimersByTimeAsync(0);
    expect(fetchMock).toHaveBeenCalledTimes(1);

    window.dispatchEvent(new Event('beforeunload'));
    // The server ends the waiting poll when it renders the next page.
    release();
    await vi.advanceTimersByTimeAsync(NAVIGATION_START_GRACE_MS - 1);

    expect(fetchMock).toHaveBeenCalledTimes(1);

    await vi.advanceTimersByTimeAsync(1);

    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});

describe('an event stream refused without a navigation', () => {
  it('makes the tab poll', async () => {
    const { connect } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    heartbeat(sources[0]);

    sources[0].abort();

    expect(window.sessionStorage.getItem(POLLING_KEY)).toBe('1');
    expect(fetch).toHaveBeenCalled();
  });
});

/** A heartbeat of the stream carrying the given number, as the server's first heartbeat does. */
function numbered(source: FakeEventSource, id: string): void {
  source.readyState = FakeEventSource.OPEN;
  source.onmessage?.(new MessageEvent('message', { data: JSON.stringify(['Heartbeat', {}]), lastEventId: id }));
}

describe('a page loaded again while the numbering of its window continues', () => {
  it('takes the number its stream starts from, so that a command naming it makes it not poll', async () => {
    const { connect, expectEvents } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    numbered(sources[0], '120');

    expectEvents(120);
    await vi.advanceTimersByTimeAsync(5_000);

    expect(window.sessionStorage.getItem(POLLING_KEY)).toBeNull();
    expect(fetch).not.toHaveBeenCalled();
  });

  it('does not step back when the number of the stream start arrives after a later event', async () => {
    const { connect, expectEvents } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    // An event flushed to the new stream before its first heartbeat.
    numbered(sources[0], '121');
    numbered(sources[0], '120');

    expectEvents(121);
    await vi.advanceTimersByTimeAsync(5_000);

    expect(window.sessionStorage.getItem(POLLING_KEY)).toBeNull();
  });

  it('polls when the events a command names do not arrive', async () => {
    const { connect, expectEvents } = await load();
    connect('/ctx/react-api/events?windowName=v1a2b');
    numbered(sources[0], '');

    expectEvents(120);
    await vi.advanceTimersByTimeAsync(5_000);

    expect(window.sessionStorage.getItem(POLLING_KEY)).toBe('1');
  });
});

describe('a cancelled beforeunload', () => {
  it('does not stop the commands', async () => {
    const { mayBeNavigating, isPageLeaving } = await load();
    const { enqueueCommand } = await import('./command-channel');
    vi.stubGlobal('fetch', vi.fn(async () => new Response('{"success":true}', { status: 200 })));

    window.dispatchEvent(new Event('beforeunload'));

    expect(mayBeNavigating()).toBe(true);
    expect(isPageLeaving()).toBe(false);
    await expect(enqueueCommand('/ctx/react-api/command', {
      controlId: 'c1', command: 'click', windowName: 'v1a2b', arguments: {},
    })).resolves.toEqual({ success: true });
  });

  it('is only a hint for a while', async () => {
    const { mayBeNavigating, NAVIGATION_START_GRACE_MS } = await load();

    window.dispatchEvent(new Event('beforeunload'));
    vi.advanceTimersByTime(NAVIGATION_START_GRACE_MS);

    expect(mayBeNavigating()).toBe(false);
  });
});
