/**
 * Whether the page is connected to the application on the server: the connection guard.
 *
 * <p>The event stream, the polls and the commands report whether the application answered
 * ({@link reportReachable}) or not ({@link reportUnreachable}). Once the application is not
 * reachable, the page counts as disconnected ({@link isConnected}) and checks the server in growing
 * intervals, from {@link MIN_RETRY_BACKOFF_MS} to {@link MAX_RETRY_BACKOFF_MS}, by asking for the
 * last event of its window ({@code react-api/poll?peek=true}, see {@link setCheckUrl}). At most one
 * such check runs at a time. When the application answers again:</p>
 *
 * <ul>
 * <li>with the number of the last event, it still knows the window (an interrupted network): the
 * page is connected again, and its event stream reconnects with the full state, see
 * {@link subscribeConnection}.</li>
 * <li>with {@link ERROR_CODE_STALE_UI}, the session or the window is gone (a restart): the page is
 * entered again, see {@link reloadForStaleUi}.</li>
 * </ul>
 *
 * <p>Only those two count as an answer of the application ({@link classifyAnswer}). Anything else -
 * a network error, a 404 of the servlet container whose application is stopped, a 502 or 503 of a
 * proxy, a page of HTML - means that the application is not reachable.</p>
 *
 * <p>Loading the page again without an address of its own - the reload the server orders, and the
 * reload of a page the server no longer knows - goes through {@link reloadWhenReachable}: the page
 * is only left when the application answers, so that a server shutting down does not leave the
 * browser on the error page of the servlet container.</p>
 */

import { isPageLeaving, mayBeNavigating, reloadForStaleUi } from './page-leave';

/**
 * Machine-readable error code from the server: the server-side UI state for this page no longer
 * exists (the session was replaced underneath the open page by a login or logout, or the server
 * was restarted). Must match {@code ReactServlet#ERROR_CODE_STALE_UI}.
 */
export const ERROR_CODE_STALE_UI = 'stale-ui';

/**
 * The first wait before the server is asked again after it was not reachable, by a check or by a
 * poll.
 */
export const MIN_RETRY_BACKOFF_MS = 1_000;

/** The longest wait between two requests to a server that is not reachable, checks or polls. */
export const MAX_RETRY_BACKOFF_MS = 10_000;

/** What an answer of the server says about the application. */
export type Answer =
  /** The application answered and knows the window. */
  | 'ok'
  /** The application answered that it no longer knows the session or the window. */
  | 'stale'
  /** The answer did not come from the application, or none came. */
  | 'unreachable';

/** Whether the application answered the last time it was asked. */
let _connected = true;

/** The address the server is checked with, empty while the page has none. */
let _checkUrl = '';

/** The check running right now, shared by everybody asking meanwhile. */
let _check: Promise<Answer> | null = null;

/** Whether the checks of a disconnected page run. */
let _recovering = false;

/** Called when the page is disconnected or connected again. */
const _listeners = new Set<() => void>();

/**
 * Sets the address the server is checked with: the address of a poll of the page's window that only
 * asks for the number of the last event ({@code peek=true}).
 */
export function setCheckUrl(url: string): void {
  _checkUrl = url;
}

/** Whether the application answered the last time it was asked. */
export function isConnected(): boolean {
  return _connected;
}

/**
 * Calls the given listener whenever the page is disconnected or connected again, until the
 * returned function is called.
 */
export function subscribeConnection(listener: () => void): () => void {
  _listeners.add(listener);
  return () => {
    _listeners.delete(listener);
  };
}

/** Reports that the application answered: a disconnected page is connected again. */
export function reportReachable(): void {
  setConnected(true);
}

/**
 * Reports that the application did not answer: the page is disconnected and checks the server
 * until it answers again. Not while the page is leaving or a navigation may be starting, and not
 * on a page without an address to check the server with ({@link setCheckUrl}).
 */
export function reportUnreachable(): void {
  // A navigation aborts the requests of the page; that says nothing about the server. A page
  // without an address to check the server with could not find out that it answers again.
  if (isPageLeaving() || mayBeNavigating() || !_checkUrl) return;
  setConnected(false);
  void recover();
}

/**
 * Loads the page again through the given navigation once the application answers.
 *
 * <p>The server is asked first. When the application answers - whether it knows the window or not
 * - the page navigates. Otherwise the page is disconnected, the navigation is dropped, and the
 * checks decide: the window still known - the page simply continues -, or gone - the page is
 * entered again.</p>
 *
 * <p>A page without an address to check the server with ({@link setCheckUrl}) navigates at
 * once.</p>
 */
export function reloadWhenReachable(navigate: () => void): void {
  if (isPageLeaving()) return;
  if (!_checkUrl) {
    navigate();
    return;
  }
  void check().then((answer) => {
    if (isPageLeaving()) return;
    if (answer === 'unreachable') {
      console.warn('[TLReact] The server does not answer; not loading the page again before it does.');
      reportUnreachable();
      return;
    }
    navigate();
  });
}

/**
 * What the given answer of a request to the application says: {@code 'ok'} for a success with a
 * JSON body, {@code 'stale'} for a {@code 401} or {@code 410} carrying {@link ERROR_CODE_STALE_UI},
 * {@code 'unreachable'} for anything else.
 */
export function classifyAnswer(status: number, body: string): Answer {
  if (status >= 200 && status < 300) {
    return parseObject(body) !== undefined ? 'ok' : 'unreachable';
  }
  if ((status === 401 || status === 410) && readErrorCode(body) === ERROR_CODE_STALE_UI) {
    return 'stale';
  }
  return 'unreachable';
}

/**
 * The machine-readable error code of an error response body (e.g. {@link ERROR_CODE_STALE_UI}), or
 * {@code undefined} if it carries none.
 */
export function readErrorCode(body: string): string | undefined {
  const errorCode = parseObject(body)?.errorCode;
  return typeof errorCode === 'string' ? errorCode : undefined;
}

function parseObject(body: string): Record<string, unknown> | undefined {
  try {
    const parsed: unknown = JSON.parse(body);
    return typeof parsed === 'object' && parsed !== null ? (parsed as Record<string, unknown>) : undefined;
  } catch {
    return undefined;
  }
}

function setConnected(connected: boolean): void {
  if (_connected === connected) return;
  _connected = connected;
  if (connected) {
    console.warn('[TLReact] The server answers again.');
  } else {
    console.warn('[TLReact] The server does not answer; checking it until it does.');
  }
  for (const listener of [..._listeners]) {
    try {
      listener();
    } catch (e) {
      console.error('[TLReact] Failed to react to the connection state:', e);
    }
  }
}

/** Asks the server, joining a check that runs already. */
function check(): Promise<Answer> {
  if (!_check) {
    _check = ask().finally(() => {
      _check = null;
    });
  }
  return _check;
}

async function ask(): Promise<Answer> {
  try {
    const response = await fetch(_checkUrl, { cache: 'no-store' });
    return classifyAnswer(response.status, await response.text());
  } catch {
    return 'unreachable';
  }
}

/** Checks the server in growing intervals while the page is disconnected. */
async function recover(): Promise<void> {
  if (_recovering || !_checkUrl) return;
  _recovering = true;
  try {
    let backoff = 0;
    while (!_connected && !isPageLeaving()) {
      backoff = Math.min(backoff ? backoff * 2 : MIN_RETRY_BACKOFF_MS, MAX_RETRY_BACKOFF_MS);
      await new Promise((resolve) => setTimeout(resolve, backoff));
      if (_connected || isPageLeaving()) return;
      const answer = await check();
      if (answer === 'ok') {
        setConnected(true);
        return;
      }
      if (answer === 'stale') {
        // The page is left from here; it stays disconnected until then.
        reloadForStaleUi({ waitForGuard: true });
        return;
      }
    }
  } finally {
    _recovering = false;
  }
}
