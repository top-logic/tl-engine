import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

/** The address the server is checked with. */
const CHECK_URL = '/ctx/react-api/poll?windowName=v1a2b&peek=true';

/** The address the page is entered again through, see `reentryUrl()`. */
const REENTRY_URL = '/ctx/view/property/42?tab=2';

const realLocation = window.location;

/** The navigation methods of the stand-in for {@code window.location}. */
let nav: { reload: ReturnType<typeof vi.fn>; assign: ReturnType<typeof vi.fn>; replace: ReturnType<typeof vi.fn> };

/** What the server answers to the next request, until a test changes it. */
let server: () => Promise<Response>;

/** The answer of the application for a window it knows. */
function known(): Promise<Response> {
  return Promise.resolve(new Response('{"last":3}', { status: 200, headers: { 'Content-Type': 'application/json' } }));
}

/** The answer of the application for a session it does not know. */
function stale(): Promise<Response> {
  return Promise.resolve(new Response(JSON.stringify({ success: false, errorCode: 'stale-ui', error: 'No session.' }),
    { status: 401 }));
}

/** The answer of a servlet container whose application is stopped. */
function notFound(): Promise<Response> {
  return Promise.resolve(new Response('<html><body>404 Not Found</body></html>', { status: 404 }));
}

/** A server that does not answer at all. */
function down(): Promise<Response> {
  return Promise.reject(new TypeError('Failed to fetch'));
}

/** A proxy asking for its own authentication. */
function proxyRefusal(): Promise<Response> {
  return Promise.resolve(new Response('<html><body>Unauthorized</body></html>', { status: 401 }));
}

/** Fresh module state per test, with the server checked at {@link CHECK_URL}. */
async function load() {
  vi.resetModules();
  const pageLeave = await import('./page-leave');
  const channel = await import('./command-channel');
  const connection = await import('./connection');
  connection.setCheckUrl(CHECK_URL);
  return { ...pageLeave, ...channel, ...connection };
}

/** Whether the page navigated in any way. */
function navigated(): boolean {
  return nav.reload.mock.calls.length + nav.assign.mock.calls.length + nav.replace.mock.calls.length > 0;
}

beforeEach(() => {
  window.sessionStorage.clear();
  const parsed = new URL('/ctx/view/v1a2b/property/42?tab=2#section', 'http://localhost');
  nav = { reload: vi.fn(), assign: vi.fn(), replace: vi.fn() };
  Object.defineProperty(window, 'location', {
    configurable: true,
    value: { pathname: parsed.pathname, search: parsed.search, hash: parsed.hash, href: parsed.href, ...nav },
  });
  vi.useFakeTimers();
  server = known;
  vi.stubGlobal('fetch', vi.fn(() => server()));
  vi.spyOn(console, 'warn').mockImplementation(() => {});
  vi.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(() => {
  Object.defineProperty(window, 'location', { configurable: true, value: realLocation });
  vi.useRealTimers();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

describe('a reload the server orders', () => {
  it('loads the page again when the application answers', async () => {
    const { leavePageWhenAnswered, isConnected } = await load();

    leavePageWhenAnswered();
    await vi.advanceTimersByTimeAsync(0);

    expect(fetch).toHaveBeenCalledWith(CHECK_URL, expect.anything());
    expect(nav.reload).toHaveBeenCalledTimes(1);
    expect(isConnected()).toBe(true);
  });

  it('does not navigate to the error page of a stopped application, and disconnects', async () => {
    server = notFound;
    const { leavePageWhenAnswered, isConnected, isPageLeaving } = await load();

    leavePageWhenAnswered();
    await vi.advanceTimersByTimeAsync(0);

    expect(navigated()).toBe(false);
    expect(isPageLeaving()).toBe(false);
    expect(isConnected()).toBe(false);
  });

  it('does not navigate while the server does not answer at all', async () => {
    server = down;
    const { leavePageWhenAnswered, isConnected } = await load();

    leavePageWhenAnswered();
    await vi.advanceTimersByTimeAsync(0);

    expect(navigated()).toBe(false);
    expect(isConnected()).toBe(false);
  });

  it('enters the page again when the application comes back without the session', async () => {
    server = notFound;
    const { leavePageWhenAnswered, MIN_RETRY_BACKOFF_MS } = await load();
    leavePageWhenAnswered();
    await vi.advanceTimersByTimeAsync(0);

    server = stale;
    await vi.advanceTimersByTimeAsync(MIN_RETRY_BACKOFF_MS);

    expect(nav.replace).toHaveBeenCalledWith(REENTRY_URL);
    expect(nav.reload).not.toHaveBeenCalled();
  });

  it('connects again without navigation when the application comes back with the window', async () => {
    server = down;
    const { leavePageWhenAnswered, isConnected, subscribeConnection, MIN_RETRY_BACKOFF_MS } = await load();
    const listener = vi.fn();
    subscribeConnection(listener);
    leavePageWhenAnswered();
    await vi.advanceTimersByTimeAsync(0);
    expect(listener).toHaveBeenCalledTimes(1);

    server = known;
    await vi.advanceTimersByTimeAsync(MIN_RETRY_BACKOFF_MS);

    expect(isConnected()).toBe(true);
    expect(listener).toHaveBeenCalledTimes(2);
    expect(navigated()).toBe(false);
  });

  it('does not take the refusal of a proxy for the answer of the application', async () => {
    server = down;
    const { leavePageWhenAnswered, isConnected, MAX_RETRY_BACKOFF_MS } = await load();
    leavePageWhenAnswered();
    await vi.advanceTimersByTimeAsync(0);

    server = proxyRefusal;
    await vi.advanceTimersByTimeAsync(4 * MAX_RETRY_BACKOFF_MS);

    expect(navigated()).toBe(false);
    expect(isConnected()).toBe(false);
  });
});

describe('the reload of a page the server no longer knows', () => {
  it('does not navigate while the application does not answer', async () => {
    server = notFound;
    const { reloadForStaleUi, isConnected, STALE_UI_RELOAD_TIME_KEY } = await load();

    expect(reloadForStaleUi()).toBe(true);
    await vi.advanceTimersByTimeAsync(0);

    expect(navigated()).toBe(false);
    expect(isConnected()).toBe(false);
    // The guard counts reloads that happened.
    expect(window.sessionStorage.getItem(STALE_UI_RELOAD_TIME_KEY)).toBeNull();
  });

  it('enters the page again once the application answers', async () => {
    server = stale;
    const { reloadForStaleUi } = await load();

    reloadForStaleUi();
    await vi.advanceTimersByTimeAsync(0);

    expect(nav.replace).toHaveBeenCalledWith(REENTRY_URL);
  });
});

describe('the checks of a disconnected page', () => {
  it('wait longer each time, up to the limit', async () => {
    server = down;
    const { reportUnreachable, MIN_RETRY_BACKOFF_MS, MAX_RETRY_BACKOFF_MS } = await load();

    reportUnreachable();
    await vi.advanceTimersByTimeAsync(MIN_RETRY_BACKOFF_MS - 1);
    expect(fetch).toHaveBeenCalledTimes(0);
    await vi.advanceTimersByTimeAsync(1);
    expect(fetch).toHaveBeenCalledTimes(1);
    await vi.advanceTimersByTimeAsync(2 * MIN_RETRY_BACKOFF_MS);
    expect(fetch).toHaveBeenCalledTimes(2);
    await vi.advanceTimersByTimeAsync(4 * MIN_RETRY_BACKOFF_MS);
    expect(fetch).toHaveBeenCalledTimes(3);
    await vi.advanceTimersByTimeAsync(8 * MIN_RETRY_BACKOFF_MS);
    expect(fetch).toHaveBeenCalledTimes(4);
    await vi.advanceTimersByTimeAsync(MAX_RETRY_BACKOFF_MS);
    expect(fetch).toHaveBeenCalledTimes(5);
    await vi.advanceTimersByTimeAsync(MAX_RETRY_BACKOFF_MS);
    expect(fetch).toHaveBeenCalledTimes(6);
  });

  it('run once, however often the server is reported unreachable', async () => {
    server = down;
    const { reportUnreachable, MIN_RETRY_BACKOFF_MS } = await load();

    reportUnreachable();
    reportUnreachable();
    reportUnreachable();
    await vi.advanceTimersByTimeAsync(MIN_RETRY_BACKOFF_MS);

    expect(fetch).toHaveBeenCalledTimes(1);
  });

  it('share the check of a reload asked for meanwhile', async () => {
    let answer!: (response: Response) => void;
    server = () => new Promise<Response>((resolve) => {
      answer = resolve;
    });
    const { leavePageWhenAnswered, reloadForStaleUi } = await load();

    leavePageWhenAnswered();
    await vi.advanceTimersByTimeAsync(0);
    reloadForStaleUi();
    await vi.advanceTimersByTimeAsync(0);
    expect(fetch).toHaveBeenCalledTimes(1);

    answer(new Response('<html></html>', { status: 503 }));
    await vi.advanceTimersByTimeAsync(0);

    expect(navigated()).toBe(false);
  });

  it('stop when the application is reported to answer', async () => {
    server = down;
    const { reportUnreachable, reportReachable, isConnected, MIN_RETRY_BACKOFF_MS } = await load();

    reportUnreachable();
    reportReachable();
    await vi.advanceTimersByTimeAsync(10 * MIN_RETRY_BACKOFF_MS);

    expect(isConnected()).toBe(true);
    expect(fetch).not.toHaveBeenCalled();
  });
});

describe('a command without an answer', () => {
  it('disconnects the page', async () => {
    server = down;
    const { enqueueCommand, isConnected } = await load();

    await enqueueCommand('/ctx/react-api/command', { controlId: 'c1', command: 'click', windowName: 'v1a2b', arguments: {} });

    expect(isConnected()).toBe(false);
  });
});
