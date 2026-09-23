/**
 * Client-side route synchronization: keeps the browser history and the server-side display on the
 * same page.
 *
 * <p>A navigation on the server arrives as a RouteChangeEvent and is written to the history as a
 * new entry or as a replacement of the current one. A move the browser makes by itself (the back
 * and forward buttons, an address the user types) arrives as a {@code popstate} and is reported to
 * the server with the {@link CMD_NAVIGATE_TO_ROUTE} command; the display may refuse it, e.g. over
 * a form holding unsaved changes, and the command's answer says so.</p>
 *
 * <p>To answer a refusal the browser is taken back to the entry the display belongs to, which
 * requires knowing where that entry is: every entry this module writes therefore carries its
 * position in {@code history.state} under {@link NAV_STATE_KEY}, stamped with the id of the page
 * load that wrote it. {@link _displayPos} is the position of the entry the display belongs to, so
 * a refused move is undone by going back the distance between the two - a move within the history,
 * which leaves every entry as it is. An entry from another page load carries no position this load
 * can use, and is corrected by writing the display's address into it.</p>
 *
 * <p>The refused move is remembered, and a RouteResumeEvent - sent once the user has said what is
 * to become of the unsaved changes - makes it again, in the direction the user pressed. The move
 * is reported like any other, so the display takes up the URL it leads to and the entry the user
 * came from stays where it is, reachable by the button that leads back to it.</p>
 */

import { enqueueCommand, type CommandResponse } from './command-channel';

export interface RouteChangeEventData {
  url: string;
  replace: boolean;
}

export interface RouteResumeEventData {
  url: string;
}

/**
 * Name of the command reporting a URL the browser moved to by itself. Must match
 * {@code ReactServlet#CMD_NAVIGATE_TO_ROUTE}.
 */
const CMD_NAVIGATE_TO_ROUTE = 'navigateToRoute';

/**
 * Name of the {@link CMD_NAVIGATE_TO_ROUTE} argument holding the URL to adopt. Must match
 * {@code ReactServlet#ARG_URL}.
 */
const ARG_URL = 'url';

/**
 * Name of the {@link CMD_NAVIGATE_TO_ROUTE} answer field that is set when the display does not
 * take up the URL. Must match {@code ReactServlet#FIELD_REFUSED}.
 */
const FIELD_REFUSED = 'refused';

/**
 * Name of the {@link CMD_NAVIGATE_TO_ROUTE} answer field holding the URL of the page the display
 * is left on. Must match {@code ReactServlet#FIELD_CURRENT_URL}.
 */
const FIELD_CURRENT_URL = 'currentUrl';

/** Key under which a history entry carries its {@link NavState}. */
const NAV_STATE_KEY = 'tlNav';

/** The bookkeeping a history entry written by this module carries. */
interface NavState {
  /** The page load that wrote the entry, identifying the positions it can be compared with. */
  load: string;
  /** The position of the entry within the entries written by that page load. */
  pos: number;
}

let _initialized = false;

/**
 * Identifies the current page load, so that an entry left in the history by an earlier load of
 * this window is recognized as one whose position says nothing about the current entries.
 */
const _loadId = `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 10)}`;

/** The position of the history entry the display belongs to. */
let _displayPos = 0;

/**
 * The address to restore in the entry a {@code history.go} is currently travelling to, set while
 * that move is in flight and read by the {@code popstate} it causes.
 */
let _restoredUrl: string | null = null;

/** A move the display refused, kept until it is resumed or a move of the user's replaces it. */
interface RefusedMove {
  /** The URL the move leads to. */
  url: string;
  /**
   * The distance from the display's entry to the one the move leads to, or {@code null} for a move
   * to an entry whose place in this load's history is unknown.
   */
  delta: number | null;
}

/** The move the display refused last, or {@code null} if none is waiting to be resumed. */
let _refusedMove: RefusedMove | null = null;

/** The current page's window name (read from body data attribute). */
function getWindowName(): string {
  return document.body.dataset.windowName ?? '';
}

/** The context path for URL construction (read from body data attribute). */
function getContextPath(): string {
  return document.body.dataset.contextPath ?? '';
}

/**
 * Initialize route synchronization. Called once during bridge initialization.
 */
export function initRouteSync(): void {
  if (_initialized) return;
  _initialized = true;

  window.addEventListener('popstate', (event: PopStateEvent) => {
    if (_restoredUrl !== null) {
      // The move back to the display's own entry has arrived; the browser is where the display is.
      finishRestore();
      return;
    }
    // A move of the user's own supersedes the one that was refused: it is the display's answer to
    // the unsaved changes that is awaited now, not a resume of a move the user has moved on from.
    _refusedMove = null;
    const target = readNavState(event.state);
    void sendRouteCommand(extractRoutePath() + window.location.search, target);
  });

  initHistoryState();
}

/**
 * Handle RouteChangeEvent from server.
 */
export function handleRouteChangeEvent(event: RouteChangeEventData): void {
  const fullUrl = getViewBasePath() + event.url;
  if (event.replace) {
    history.replaceState(navState(_displayPos), '', fullUrl);
  } else {
    // A navigation of the display leaves the entries beyond it behind, the refused move's target
    // among them.
    _refusedMove = null;
    const pos = _displayPos + 1;
    history.pushState(navState(pos), '', fullUrl);
    _displayPos = pos;
  }
}

/**
 * Handle RouteResumeEvent from server: make the refused move again, now that the unsaved changes
 * that stood in its way are resolved.
 *
 * <p>Travelling the remembered distance is what keeps the history as the user left it: the entry
 * the move leads to is the one they pressed for, and the entry they came from stays ahead of it.
 * The {@code popstate} this causes reports the move like any other, which is what takes the
 * display to the URL. A move whose entry this page load does not know - or one the user has
 * meanwhile moved on from - is made as a navigation to the URL instead, which is the best a page
 * can do for an entry it cannot travel to.</p>
 */
export function handleRouteResumeEvent(event: RouteResumeEventData): void {
  const move = _refusedMove;
  _refusedMove = null;
  if (move !== null && move.url === event.url && move.delta !== null && move.delta !== 0) {
    history.go(move.delta);
    return;
  }
  // The entry is written here rather than travelled to, and reported like the move to it would be:
  // the command records it as the display's and answers a refusal of it as it does any other.
  const state = navState(_displayPos + 1);
  history.pushState(state, '', getViewBasePath() + event.url);
  void sendRouteCommand(event.url, state[NAV_STATE_KEY]);
}

/**
 * Report a URL the browser moved to by itself to the server, and act on its answer.
 *
 * <p>Dispatched through the strict FIFO command channel so navigation cannot overtake (or be
 * overtaken by) in-flight control commands. The entry moved to is the one the display belongs to
 * while the command is under way - the display adopting the URL reports the address it arrives at
 * as a replacement, which is written into that entry. A refusal takes both the bookkeeping and
 * the browser back to the entry the display is left on.</p>
 *
 * @param target
 *        The bookkeeping of the history entry the browser moved to, or {@code null} if that entry
 *        does not carry any this page load can use.
 */
async function sendRouteCommand(url: string, target: NavState | null): Promise<void> {
  const contextPath = getContextPath();
  const windowName = getWindowName();
  const displayPosBefore = _displayPos;
  if (target !== null) {
    _displayPos = target.pos;
  }
  const answer: CommandResponse | undefined = await enqueueCommand(`${contextPath}/react-api/command`, {
    controlId: '',
    command: CMD_NAVIGATE_TO_ROUTE,
    windowName,
    arguments: { [ARG_URL]: url },
  });

  if (answer?.[FIELD_REFUSED] === true) {
    _displayPos = displayPosBefore;
    restoreDisplayEntry(url, target, String(answer[FIELD_CURRENT_URL] ?? ''));
  }
}

/**
 * Take the browser back to the entry the display belongs to, after the display refused the URL of
 * the entry it moved to.
 *
 * <p>Going the distance between the two entries leaves both of them as they are, so the entries
 * the move skipped over stay reachable. An entry from another page load has no distance to the
 * display's entry: it is given the display's address, which is the best that can be done for an
 * entry whose place in this load's history is unknown.</p>
 *
 * <p>The move is remembered, so that it can be made again once the user has answered about the
 * unsaved changes that refused it.</p>
 *
 * @param url
 *        The URL the refused move leads to.
 * @param currentUrl
 *        The URL of the page the display is left on, relative to the view base.
 */
function restoreDisplayEntry(url: string, target: NavState | null, currentUrl: string): void {
  const fullUrl = getViewBasePath() + currentUrl;
  if (target === null) {
    _refusedMove = { url, delta: null };
    history.replaceState(navState(_displayPos), '', fullUrl);
    return;
  }
  // The direction the user pressed, which is what resuming the move travels again.
  _refusedMove = { url, delta: target.pos - _displayPos };
  const delta = _displayPos - target.pos;
  if (delta === 0) {
    // The browser is on the display's own entry already; nothing was left behind to come back to.
    return;
  }
  _restoredUrl = fullUrl;
  history.go(delta);
}

/**
 * Complete the move back to the display's entry: that entry holds the address the display composes
 * unless the server has meanwhile composed a different one, in which case it is written into it.
 */
function finishRestore(): void {
  const fullUrl = _restoredUrl;
  _restoredUrl = null;
  if (fullUrl === null) return;
  if (window.location.pathname + window.location.search !== fullUrl) {
    history.replaceState(navState(_displayPos), '', fullUrl);
  }
}

/** The {@code history.state} value marking an entry of this page load at the given position. */
function navState(pos: number): Record<string, NavState> {
  return { [NAV_STATE_KEY]: { load: _loadId, pos } };
}

/**
 * The bookkeeping carried by a history entry, or {@code null} if the entry carries none of this
 * page load's.
 */
function readNavState(state: unknown): NavState | null {
  if (typeof state !== 'object' || state === null) return null;
  const nav = (state as Record<string, unknown>)[NAV_STATE_KEY];
  if (typeof nav !== 'object' || nav === null) return null;
  const candidate = nav as Partial<NavState>;
  if (candidate.load !== _loadId || typeof candidate.pos !== 'number') return null;
  return { load: candidate.load, pos: candidate.pos };
}

/**
 * Take up the entry the page was loaded into as the display's first one, hiding the window name
 * segment from the visible URL.
 *
 * <p>Server URL: /contextPath/view/v1a2b3c/property/42, visible URL:
 * /contextPath/view/property/42. The entry is rewritten in either case, so that it carries the
 * position every further entry is counted from.</p>
 */
function initHistoryState(): void {
  _displayPos = 0;
  history.replaceState(navState(0), '', visiblePath() + window.location.search);
}

/** The current path with the window name segment removed. */
function visiblePath(): string {
  const path = window.location.pathname;
  const viewIdx = path.indexOf('/view/');
  if (viewIdx < 0) return path;

  const afterView = path.substring(viewIdx + 6); // everything after "/view/"
  const firstSlash = afterView.indexOf('/');
  // The window name is the whole remainder when no route follows it.
  const firstSegment = firstSlash >= 0 ? afterView.substring(0, firstSlash) : afterView;
  // Window names start with 'v' followed by hex characters.
  if (!firstSegment.match(/^v[0-9a-f]+$/i)) return path;

  const route = firstSlash >= 0 ? afterView.substring(firstSlash + 1) : '';
  return path.substring(0, viewIdx + 6) + route;
}

/**
 * Extract the route path from the current URL (without base path).
 * The visible URL has no window name segment.
 */
function extractRoutePath(): string {
  const path = window.location.pathname;
  const basePath = getViewBasePath();
  return path.substring(basePath.length);
}

/**
 * Get the base path for views (up to and including /view/).
 * The visible URL is /contextPath/view/routeSegments...
 */
function getViewBasePath(): string {
  const path = window.location.pathname;
  const viewIdx = path.indexOf('/view/');
  if (viewIdx >= 0) {
    return path.substring(0, viewIdx + 6); // includes trailing /
  }
  return '/';
}
