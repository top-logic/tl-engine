import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

/** The answer the server gives to a command for a page it no longer knows. */
const STALE_ANSWER = { status: 409, body: JSON.stringify({ errorCode: 'stale-ui' }) };

/** A command as a control sends it. */
const PAYLOAD = { controlId: 'c1', command: 'click', windowName: 'v1a2b', arguments: {} };

const COMMAND_URL = '/ctx/react-api/command';

const realLocation = window.location;

/** The navigation methods of the stand-in for {@code window.location}. */
let nav: { reload: ReturnType<typeof vi.fn>; assign: ReturnType<typeof vi.fn>; replace: ReturnType<typeof vi.fn> };

/** Lets the page stand at the given address, with navigation methods that only record calls. */
function standAt(url: string): void {
  const parsed = new URL(url, 'http://localhost');
  nav = { reload: vi.fn(), assign: vi.fn(), replace: vi.fn() };
  Object.defineProperty(window, 'location', {
    configurable: true,
    value: { pathname: parsed.pathname, search: parsed.search, hash: parsed.hash, href: parsed.href, ...nav },
  });
}

/** An answer of the command endpoint, to be released by the test. */
function deferredAnswer(): { promise: Promise<Response>; answer: (status: number, body: string) => void } {
  let answer!: (status: number, body: string) => void;
  const promise = new Promise<Response>((resolve) => {
    answer = (status, body) => resolve(new Response(body, { status }));
  });
  return { promise, answer };
}

/** The page transition listeners the modules loaded by the current test put on the window. */
const transitionListeners: Array<[string, EventListenerOrEventListenerObject]> = [];

/**
 * Fresh module state per test: the leaving state of one test must not leak into the next, nor may
 * the window listeners of an earlier test's module instance react to this test's events.
 */
async function load() {
  vi.resetModules();
  const add = window.addEventListener.bind(window);
  const spy = vi.spyOn(window, 'addEventListener').mockImplementation(
    (type: string, listener: EventListenerOrEventListenerObject, options?: boolean | AddEventListenerOptions) => {
      if (type === 'pagehide' || type === 'pageshow') transitionListeners.push([type, listener]);
      add(type, listener, options);
    });
  try {
    const pageLeave = await import('./page-leave');
    const channel = await import('./command-channel');
    return { ...pageLeave, ...channel };
  } finally {
    spy.mockRestore();
  }
}

beforeEach(() => {
  window.sessionStorage.clear();
  standAt('/ctx/view/v1a2b/property/42?tab=2#section');
});

afterEach(() => {
  for (const [type, listener] of transitionListeners.splice(0)) {
    window.removeEventListener(type, listener);
  }
  Object.defineProperty(window, 'location', { configurable: true, value: realLocation });
  vi.unstubAllGlobals();
});

describe('a page that is leaving', () => {
  it('sends no command', async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal('fetch', fetchMock);
    const { enqueueCommand, leavePage, isPageLeaving } = await load();

    leavePage();

    expect(isPageLeaving()).toBe(true);
    expect(nav.reload).toHaveBeenCalledTimes(1);
    await expect(enqueueCommand(COMMAND_URL, PAYLOAD)).resolves.toBeUndefined();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it('drops the commands still waiting in the channel', async () => {
    const first = deferredAnswer();
    const fetchMock = vi.fn().mockReturnValueOnce(first.promise);
    vi.stubGlobal('fetch', fetchMock);
    const { enqueueCommand, leavePage } = await load();

    const sent = enqueueCommand(COMMAND_URL, PAYLOAD);
    const waiting = enqueueCommand(COMMAND_URL, PAYLOAD);
    await vi.waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));

    leavePage('/ctx/login');
    first.answer(200, '{"success":true}');

    await expect(sent).resolves.toEqual({ success: true });
    await expect(waiting).resolves.toBeUndefined();
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(nav.assign).toHaveBeenCalledWith('/ctx/login');
  });

  it('does not reload for a stale answer to a command the navigation overtook', async () => {
    const pending = deferredAnswer();
    vi.stubGlobal('fetch', vi.fn().mockReturnValueOnce(pending.promise));
    const { enqueueCommand, leavePage } = await load();

    const sent = enqueueCommand(COMMAND_URL, PAYLOAD);
    await Promise.resolve();
    leavePage();
    pending.answer(STALE_ANSWER.status, STALE_ANSWER.body);

    await expect(sent).resolves.toBeUndefined();
    expect(nav.reload).toHaveBeenCalledTimes(1);
    expect(nav.replace).not.toHaveBeenCalled();
    expect(nav.assign).not.toHaveBeenCalled();
  });

  it('is marked by a navigation the browser starts itself', async () => {
    const { isPageLeaving } = await load();
    expect(isPageLeaving()).toBe(false);

    window.dispatchEvent(new PageTransitionEvent('pagehide', { persisted: false }));

    expect(isPageLeaving()).toBe(true);
  });

  it('loads itself again when the browser shows it from its back-forward cache', async () => {
    const { isPageLeaving } = await load();
    window.dispatchEvent(new PageTransitionEvent('pagehide', { persisted: true }));

    window.dispatchEvent(new PageTransitionEvent('pageshow', { persisted: true }));

    expect(nav.reload).toHaveBeenCalledTimes(1);
    expect(isPageLeaving()).toBe(true);
  });

  it('is not reloaded when shown by an ordinary load', async () => {
    const { isPageLeaving } = await load();

    window.dispatchEvent(new PageTransitionEvent('pageshow', { persisted: false }));

    expect(nav.reload).not.toHaveBeenCalled();
    expect(isPageLeaving()).toBe(false);
  });

  it('starts no second navigation', async () => {
    const { leavePage } = await load();

    leavePage('/ctx/first');
    leavePage('/ctx/second');
    leavePage();

    expect(nav.assign).toHaveBeenCalledTimes(1);
    expect(nav.assign).toHaveBeenCalledWith('/ctx/first');
    expect(nav.reload).not.toHaveBeenCalled();
  });

  it('tells its listeners once', async () => {
    const { leavePage, onPageLeave } = await load();
    const listener = vi.fn();
    onPageLeave(listener);

    leavePage();
    window.dispatchEvent(new PageTransitionEvent('pagehide', { persisted: false }));

    expect(listener).toHaveBeenCalledTimes(1);
  });
});

describe('a page the server no longer knows', () => {
  it('is entered again through the address without the window name', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(STALE_ANSWER.body, { status: STALE_ANSWER.status })));
    const { enqueueCommand, isPageLeaving } = await load();

    await enqueueCommand(COMMAND_URL, PAYLOAD);

    expect(nav.replace).toHaveBeenCalledWith('/ctx/view/property/42?tab=2');
    expect(nav.reload).not.toHaveBeenCalled();
    expect(isPageLeaving()).toBe(true);
  });

  it('is not reloaded again right after such a reload', async () => {
    window.sessionStorage.setItem('tlReactStaleUiReloadTime', String(Date.now()));
    const fetchMock = vi.fn().mockResolvedValue(new Response(STALE_ANSWER.body, { status: STALE_ANSWER.status }));
    vi.stubGlobal('fetch', fetchMock);
    const { enqueueCommand, isPageLeaving } = await load();

    await enqueueCommand(COMMAND_URL, PAYLOAD);

    expect(nav.replace).not.toHaveBeenCalled();
    expect(isPageLeaving()).toBe(false);
  });

  it('shares its guard with every other part of the page', async () => {
    const { reloadForStaleUi, STALE_UI_RELOAD_TIME_KEY } = await load();

    expect(reloadForStaleUi()).toBe(true);

    expect(STALE_UI_RELOAD_TIME_KEY).toBe('tlReactStaleUiReloadTime');
    expect(Number(window.sessionStorage.getItem(STALE_UI_RELOAD_TIME_KEY))).toBeGreaterThan(0);
  });

  it('waits for the guard where asked to', async () => {
    vi.useFakeTimers();
    try {
      window.sessionStorage.setItem('tlReactStaleUiReloadTime', String(Date.now()));
      const { reloadForStaleUi } = await load();

      expect(reloadForStaleUi({ waitForGuard: true })).toBe(true);
      expect(nav.replace).not.toHaveBeenCalled();

      vi.advanceTimersByTime(10_000);

      expect(nav.replace).toHaveBeenCalledWith('/ctx/view/property/42?tab=2');
    } finally {
      vi.useRealTimers();
    }
  });
});

describe('the re-entry address', () => {
  it('drops the window name and the fragment', async () => {
    const { reentryUrl } = await load();
    expect(reentryUrl()).toBe('/ctx/view/property/42?tab=2');
  });

  it('keeps a path that names no window', async () => {
    const { withoutWindowName } = await load();
    expect(withoutWindowName('/ctx/view/property/42')).toBe('/ctx/view/property/42');
    expect(withoutWindowName('/ctx/servlet/LayoutServlet')).toBe('/ctx/servlet/LayoutServlet');
  });

  it('is the view application itself for a window without route', async () => {
    const { withoutWindowName } = await load();
    expect(withoutWindowName('/ctx/view/v0a1b2c3d')).toBe('/ctx/view/');
    expect(withoutWindowName('/ctx/view/v0a1b2c3d/')).toBe('/ctx/view/');
  });
});
