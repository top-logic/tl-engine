/**
 * Leaving the page: the one way the client navigates away from the page it displays.
 *
 * <p>While a navigation away is in flight, the page is still alive: its effects still run, its
 * event stream reconnects, a field still reports its value. Such a request reaches the server after
 * the navigation has changed what the server holds - a login replaces the session on the request
 * that loads the next page - and is answered as one of a page the server no longer knows. Reacting
 * to that answer with a reload of its own cancels the navigation that is under way, and the reload
 * then arrives with the state from before it: after a login, the user would be logged out.</p>
 *
 * <p>So the page knows when it is leaving ({@link isPageLeaving}). {@link leavePage} marks it,
 * tells the parts that talk to the server ({@link onPageLeave}: the event stream closes) and
 * navigates; a navigation the browser starts by itself (a link, the reload button) marks it on
 * {@code pagehide}. From then on the command channel sends nothing, and neither a stale answer nor a
 * lost event stream reloads the page.</p>
 *
 * <p>A navigation the browser starts by itself announces itself earlier, on {@code beforeunload},
 * and the browser may abort the event stream right then - long before {@code pagehide}. That is
 * only a hint ({@link mayBeNavigating}): a page may cancel {@code beforeunload} to ask about unsaved
 * changes, and the user may stay. So it does not stop the commands; the event stream merely does
 * not take a close within {@link NAVIGATION_START_GRACE_MS} after it for a lost connection, and
 * looks again once that time has passed ({@link afterNavigationGrace}).</p>
 *
 * <p>When the server does not know the page any more for a reason of its own - the session was
 * replaced in another tab, the server restarted - {@link reloadForStaleUi} loads it again, guarded
 * against loops, and through {@link reentryUrl}: the address without the name of the window, which
 * the server may have discarded, so the view application's bootstrap page names the window again.</p>
 */

/**
 * Session-storage key holding the time (ms since the epoch) of the last reload for a page the
 * server no longer knew. Shared by everything that reloads the page for that reason, so that two
 * parts of a page do not reload it in turns.
 */
export const STALE_UI_RELOAD_TIME_KEY = 'tlReactStaleUiReloadTime';

/** Minimum time between two reloads for a page the server no longer knew. */
export const STALE_UI_RELOAD_GUARD_MS = 10_000;

/** The path segment under which the view application is served. */
const VIEW_SEGMENT = '/view/';

/** A window name as the view application's bootstrap page creates it. */
const WINDOW_NAME_PATTERN = /^v[0-9a-f]+$/i;

/**
 * How long after {@code beforeunload} the page counts as possibly navigating away: long enough for
 * the next page to replace this one on an ordinary server, short enough that a page whose user
 * decided to stay is back to normal soon.
 */
export const NAVIGATION_START_GRACE_MS = 3_000;

let _leaving = false;

/** The time of the last {@code beforeunload}, `0` while there was none. */
let _navigationStart = 0;

const _leaveListeners = new Set<() => void>();

/** Whether the page is navigating away and must not talk to the server any more. */
export function isPageLeaving(): boolean {
  return _leaving;
}

/**
 * Whether a navigation the browser started may be under way: {@code beforeunload} fired within
 * {@link NAVIGATION_START_GRACE_MS}, and the page has not left (yet). A hint only - the user may
 * have decided to stay - so nothing but reactions to a lost connection may depend on it.
 */
export function mayBeNavigating(): boolean {
  return !_leaving && _navigationStart > 0 && Date.now() - _navigationStart < NAVIGATION_START_GRACE_MS;
}

/**
 * Calls the given function once {@link mayBeNavigating} no longer holds, unless the page has left by
 * then: for a page that is still there after the navigation it announced, because the navigation
 * was cancelled or keeps the page (a download).
 */
export function afterNavigationGrace(callback: () => void): void {
  const wait = _navigationStart + NAVIGATION_START_GRACE_MS - Date.now();
  setTimeout(() => {
    if (_leaving) return;
    if (mayBeNavigating()) {
      // Another navigation was announced meanwhile.
      afterNavigationGrace(callback);
      return;
    }
    callback();
  }, Math.max(wait, 0));
}

/**
 * Registers what is to stop when the page starts leaving, e.g. the event stream.
 *
 * @returns a function removing the listener again.
 */
export function onPageLeave(listener: () => void): () => void {
  _leaveListeners.add(listener);
  return () => _leaveListeners.delete(listener);
}

/**
 * Leaves the page: loads the address given, or the displayed page again where none is given.
 *
 * <p>The page stops talking to the server at once, so the address must name a page that replaces
 * this one - not a download, which leaves the page standing.</p>
 *
 * @param url the address to load, absolute or relative to the address of the page; absent or
 *        empty loads the page again.
 */
export function leavePage(url?: string): void {
  if (url) {
    leave(() => window.location.assign(url));
  } else {
    leave(() => window.location.reload());
  }
}

/**
 * Loads the page again after the server answered that it no longer knows it, unless that was just
 * done for the same reason.
 *
 * <p>The page is loaded through {@link reentryUrl}. Nothing happens while the page is leaving
 * anyway: the answer then belongs to a request the navigation overtook.</p>
 *
 * @param options.waitForGuard what happens within {@link STALE_UI_RELOAD_GUARD_MS} after the last
 *        such reload: `false` (the default) does not reload, so a page the server still does not
 *        know after being loaded again stops there; `true` reloads once the interval has passed,
 *        for a server that is expected to come back (a restart).
 * @returns whether the page is being or will be loaded again.
 */
export function reloadForStaleUi(options?: { waitForGuard?: boolean }): boolean {
  if (_leaving) return false;
  const stored = readStorage(STALE_UI_RELOAD_TIME_KEY);
  if (stored === undefined) {
    // Without storage there is no guard, and a page that cannot remember its last reload must not
    // risk a loop: it stays as it is.
    console.error('[TLReact] The server no longer knows this page; no storage to guard a reload, not reloading.');
    return false;
  }
  const last = Number(stored) || 0;
  const elapsed = Date.now() - last;
  if (elapsed < STALE_UI_RELOAD_GUARD_MS) {
    if (!options?.waitForGuard) {
      console.error('[TLReact] The server still does not know this page right after a reload; not reloading again.');
      return false;
    }
    const wait = STALE_UI_RELOAD_GUARD_MS - elapsed;
    console.warn('[TLReact] The server no longer knows this page. Reloading in ' + wait + ' ms.');
    setTimeout(() => {
      if (!_leaving) reloadStale();
    }, wait);
    return true;
  }
  console.warn('[TLReact] The server no longer knows this page (session replaced or window discarded). Reloading.');
  reloadStale();
  return true;
}

/**
 * The address under which the displayed page is entered again: the current address without the
 * name of the window and without a fragment.
 */
export function reentryUrl(): string {
  return withoutWindowName(window.location.pathname) + window.location.search;
}

/**
 * The given path of the view application with the window name segment removed.
 *
 * <p>Server path: /contextPath/view/v1a2b3c/property/42, result: /contextPath/view/property/42.
 * A path that names no window is returned as it is.</p>
 */
export function withoutWindowName(path: string): string {
  const viewIdx = path.indexOf(VIEW_SEGMENT);
  if (viewIdx < 0) return path;

  const start = viewIdx + VIEW_SEGMENT.length;
  const afterView = path.substring(start);
  const firstSlash = afterView.indexOf('/');
  // The window name is the whole remainder when no route follows it.
  const firstSegment = firstSlash >= 0 ? afterView.substring(0, firstSlash) : afterView;
  if (!WINDOW_NAME_PATTERN.test(firstSegment)) return path;

  const route = firstSlash >= 0 ? afterView.substring(firstSlash + 1) : '';
  return path.substring(0, start) + route;
}

// Not beforeunload: a page may cancel that one (unsaved changes), and would then stay leaving.
window.addEventListener('pagehide', markLeaving);

window.addEventListener('beforeunload', () => {
  _navigationStart = Date.now();
});

// A page the browser kept in its back-forward cache and shows again stopped talking to the server
// when it left, and the server detached its window's tree on the unload report: it loads itself
// again, which renders the window's tree anew.
window.addEventListener('pageshow', (event: PageTransitionEvent) => {
  if (event.persisted && _leaving) {
    _leaving = false;
    leavePage();
  }
});

function reloadStale(): void {
  writeStorage(STALE_UI_RELOAD_TIME_KEY, String(Date.now()));
  const url = reentryUrl();
  leave(() => window.location.replace(url));
}

/** Starts a navigation away, unless one is under way already: the first one is not cancelled. */
function leave(navigate: () => void): void {
  if (_leaving) return;
  markLeaving();
  navigate();
}

function markLeaving(): void {
  if (_leaving) return;
  _leaving = true;
  for (const listener of Array.from(_leaveListeners)) {
    try {
      listener();
    } catch (e) {
      console.error('[TLReact] Failed to stop for leaving the page:', e);
    }
  }
}

/** A value of the tab's session storage, `null` where there is none, `undefined` without storage. */
function readStorage(key: string): string | null | undefined {
  try {
    return window.sessionStorage.getItem(key);
  } catch {
    return undefined;
  }
}

/** Writes to the tab's session storage, where there is one. */
function writeStorage(key: string, value: string): void {
  try {
    window.sessionStorage.setItem(key, value);
  } catch {
    // Storage refused the write: the next reload is not guarded.
  }
}
