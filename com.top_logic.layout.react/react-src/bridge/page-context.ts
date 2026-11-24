/**
 * The context of the page for requests to the server: the context path of the application and the
 * name of the window that the page shows.
 *
 * The server writes both into the body of the page (attributes data-context-path and
 * data-window-name), so they are known before the first control is mounted. The body is read at the
 * time of each request: a request that is sent before the first mount() (for example the text
 * request of the busy overlay) gets the same values as all later requests.
 *
 * A page without these body attributes (another host of the bridge, a test page) uses the values
 * of the last mount() call instead, see {@link setMountContext}. On such a page, the context is not
 * known before the first mount() that gives a context path, see {@link isContextKnown}.
 */

/** The context path given to the last mount() that gave one, e.g. "/demo"; null before. */
let _mountContextPath: string | null = null;

/** The window name given to the last mount(). */
let _mountWindowName = '';

/** The functions to call after each {@link setMountContext}, see {@link onMountContext}. */
const _mountContextListeners: Array<() => void> = [];

/**
 * Keeps the context of a mount() call for a page whose body does not name its context.
 *
 * @param contextPath The context path given to mount(); undefined keeps the value of an earlier
 *        call.
 * @param windowName The window name given to mount().
 */
export function setMountContext(contextPath: string | undefined, windowName: string): void {
  if (contextPath !== undefined) {
    _mountContextPath = contextPath;
  }
  _mountWindowName = windowName;
  for (const listener of _mountContextListeners) {
    listener();
  }
}

/**
 * Registers a function that each {@link setMountContext} calls after it has kept the new context.
 *
 * A module that holds requests back while the context is not known sends them from this function.
 * The registration keeps this module free of imports of its users.
 */
export function onMountContext(listener: () => void): void {
  _mountContextListeners.push(listener);
}

/**
 * Whether the context path is known: the body of the page names it, or a mount() gave one.
 *
 * Without it, a request would go to an address relative to the page and not to the application.
 */
export function isContextKnown(): boolean {
  return document.body.dataset.contextPath !== undefined || _mountContextPath !== null;
}

/**
 * The context path of the application, e.g. "/demo"; empty for an application at the root, and
 * empty while the context is not known.
 */
export function getContextPath(): string {
  return document.body.dataset.contextPath ?? _mountContextPath ?? '';
}

/** The base URL of the server API: the context path with a trailing slash, e.g. "/demo/". */
export function getApiBase(): string {
  return getContextPath() + '/';
}

/** The name of the window that the page shows. */
export function getWindowName(): string {
  return document.body.dataset.windowName ?? _mountWindowName;
}
