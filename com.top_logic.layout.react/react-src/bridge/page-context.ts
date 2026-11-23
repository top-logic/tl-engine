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
 * of the last mount() call instead, see {@link setMountContext}.
 */

/** The context path given to the last mount() that gave one, e.g. "/demo". */
let _mountContextPath = '';

/** The window name given to the last mount(). */
let _mountWindowName = '';

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
}

/** The context path of the application, e.g. "/demo"; empty for an application at the root. */
export function getContextPath(): string {
  return document.body.dataset.contextPath ?? _mountContextPath;
}

/** The base URL of the server API: the context path with a trailing slash, e.g. "/demo/". */
export function getApiBase(): string {
  return getContextPath() + '/';
}

/** The name of the window that the page shows. */
export function getWindowName(): string {
  return document.body.dataset.windowName ?? _mountWindowName;
}
