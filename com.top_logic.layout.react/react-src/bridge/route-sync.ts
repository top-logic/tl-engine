/**
 * Client-side route synchronization.
 * - Receives RouteChangeEvent from server: updates browser URL (pushState/replaceState)
 * - Receives RouteVetoEvent from server: corrects URL after vetoed navigation
 * - Listens for popstate (browser back/forward): sends navigateToRoute to server
 * - On initial load, hides window name from URL via replaceState
 */

export interface RouteChangeEventData {
  url: string;
  replace: boolean;
}

export interface RouteVetoEventData {
  currentUrl: string;
}

let _initialized = false;

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

  // Browser Back/Forward: send navigateToRoute to server.
  window.addEventListener('popstate', () => {
    const routePath = extractRoutePath();
    const query = window.location.search;
    sendRouteCommand(routePath + query);
  });

  // On initial load, hide window name from visible URL.
  hideWindowNameFromUrl();
}

/**
 * Handle RouteChangeEvent from server.
 */
export function handleRouteChangeEvent(event: RouteChangeEventData): void {
  const basePath = getViewBasePath();
  const fullUrl = basePath + event.url;
  if (event.replace) {
    history.replaceState(null, '', fullUrl);
  } else {
    history.pushState(null, '', fullUrl);
  }
}

/**
 * Handle RouteVetoEvent from server.
 */
export function handleRouteVetoEvent(event: RouteVetoEventData): void {
  const basePath = getViewBasePath();
  history.replaceState(null, '', basePath + event.currentUrl);
}

/**
 * Send navigateToRoute command to the server.
 * Uses the same POST endpoint and format as window-manager.ts commands.
 */
function sendRouteCommand(url: string): void {
  const contextPath = getContextPath();
  const windowName = getWindowName();
  fetch(`${contextPath}/react-api/command`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      controlId: '',
      command: 'navigateToRoute',
      windowName,
      arguments: { url },
    }),
  }).catch((e) => {
    console.error('[TLReact] navigateToRoute error:', e);
  });
}

/**
 * Hide the window name segment from the visible URL.
 * Server URL: /contextPath/view/v1a2b3c/property/42
 * Visible URL: /contextPath/view/property/42
 */
function hideWindowNameFromUrl(): void {
  const path = window.location.pathname;
  const viewIdx = path.indexOf('/view/');
  if (viewIdx < 0) return;

  const afterView = path.substring(viewIdx + 6); // everything after "/view/"
  const firstSlash = afterView.indexOf('/');
  if (firstSlash > 0) {
    const firstSegment = afterView.substring(0, firstSlash);
    // Window names start with 'v' followed by hex characters.
    if (firstSegment.match(/^v[0-9a-f]+$/i)) {
      const cleanPath = path.substring(0, viewIdx + 6) + afterView.substring(firstSlash + 1);
      history.replaceState(null, '', cleanPath + window.location.search);
    }
  }
}

/**
 * Extract the route path from the current URL (without base path).
 * After hideWindowNameFromUrl, the visible URL has no window name segment.
 */
function extractRoutePath(): string {
  const path = window.location.pathname;
  const basePath = getViewBasePath();
  return path.substring(basePath.length);
}

/**
 * Get the base path for views (up to and including /view/).
 * After hideWindowNameFromUrl, the visible URL is /contextPath/view/routeSegments...
 */
function getViewBasePath(): string {
  const path = window.location.pathname;
  const viewIdx = path.indexOf('/view/');
  if (viewIdx >= 0) {
    return path.substring(0, viewIdx + 6); // includes trailing /
  }
  return '/';
}
