/**
 * Manages browser windows opened via server SSE events.
 *
 * Listens for WindowOpenEvent, WindowCloseEvent, WindowFocusEvent from the SSE stream.
 * Only acts on events where targetWindowId matches the current page's window name.
 */

/** Registry of windows opened from this page. */
const openWindows: Map<string, Window> = new Map();

/** Interval ID for polling closed windows. */
let pollIntervalId: ReturnType<typeof setInterval> | null = null;

/** The current page's window name. */
function getMyWindowId(): string {
  return document.body.dataset.windowName ?? '';
}

/** The context path for URL construction. */
function getContextPath(): string {
  return document.body.dataset.contextPath ?? '';
}

/** Convert WindowOpenEvent options to window.open() features string. */
function buildFeatureString(event: WindowOpenEventData): string {
  const parts: string[] = [];
  if (event.width > 0) parts.push(`width=${event.width}`);
  if (event.height > 0) parts.push(`height=${event.height}`);
  parts.push(`resizable=${event.resizable ? 'yes' : 'no'}`);
  return parts.join(',');
}

/** Start polling for user-closed windows if not already running. */
function ensurePolling(): void {
  if (pollIntervalId !== null) return;
  pollIntervalId = setInterval(() => {
    for (const [windowId, ref] of openWindows) {
      if (ref.closed) {
        openWindows.delete(windowId);
        notifyWindowClosed(windowId);
      }
    }
    if (openWindows.size === 0 && pollIntervalId !== null) {
      clearInterval(pollIntervalId);
      pollIntervalId = null;
    }
  }, 2000);
}

/** Notify server that a window was closed by the user. */
function notifyWindowClosed(windowId: string): void {
  const contextPath = getContextPath();
  const myWindowId = getMyWindowId();
  fetch(`${contextPath}/react-api/command`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      controlId: '',
      command: 'windowClosed',
      windowName: myWindowId,
      arguments: { windowId },
    }),
  }).catch(() => {
    // Best-effort notification. Server will clean up via heartbeat timeout if this fails.
  });
}

// Data types matching msgbuf-generated event shapes.
export interface WindowOpenEventData {
  targetWindowId: string;
  windowId: string;
  width: number;
  height: number;
  title: string;
  resizable: boolean;
}

export interface WindowCloseEventData {
  targetWindowId: string;
  windowId: string;
}

export interface WindowFocusEventData {
  targetWindowId: string;
  windowId: string;
}

/** Handle a WindowOpenEvent from SSE. */
export function handleWindowOpen(event: WindowOpenEventData): void {
  const contextPath = getContextPath();
  const url = `${contextPath}/view/${event.windowId}/`;
  const ref = window.open(url, event.windowId, buildFeatureString(event));

  if (ref) {
    openWindows.set(event.windowId, ref);
    ensurePolling();
  } else {
    // Popup was blocked.
    notifyWindowBlocked(event.windowId);
  }
}

/** Handle a WindowCloseEvent from SSE. */
export function handleWindowClose(event: WindowCloseEventData): void {
  const ref = openWindows.get(event.windowId);
  if (ref) {
    ref.close();
    openWindows.delete(event.windowId);
  }
}

/** Handle a WindowFocusEvent from SSE. */
export function handleWindowFocus(event: WindowFocusEventData): void {
  const ref = openWindows.get(event.windowId);
  if (ref && !ref.closed) {
    ref.focus();
  }
}

/** Notify server that a popup was blocked. */
function notifyWindowBlocked(windowId: string): void {
  const contextPath = getContextPath();
  const myWindowId = getMyWindowId();
  fetch(`${contextPath}/react-api/command`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      controlId: '',
      command: 'windowBlocked',
      windowName: myWindowId,
      arguments: { windowId },
    }),
  }).catch(() => {
    // Best-effort.
  });
}

/**
 * Send a self-close notification on page unload.
 * Handles the case where the opener has closed and nobody is polling this window.
 */
export function initSelfCloseNotification(): void {
  window.addEventListener('beforeunload', () => {
    const contextPath = getContextPath();
    const myWindowId = getMyWindowId();
    if (!myWindowId) return;

    // Use sendBeacon for reliability during unload.
    // Wrap in Blob with application/json Content-Type since sendBeacon with
    // a plain string sends text/plain, which may not be parsed correctly.
    const payload = JSON.stringify({
      controlId: '',
      command: 'windowClosed',
      windowName: myWindowId,
      arguments: { windowId: myWindowId },
    });
    const blob = new Blob([payload], { type: 'application/json' });
    navigator.sendBeacon(`${contextPath}/react-api/command`, blob);
  });
}
