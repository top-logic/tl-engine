import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

/** The attribute marking a page shown under another servlet's URL; spelled out to check the wire name. */
const FIXED_URL_ATTR = 'data-fixed-url';

/** The URL of a servlet that forwarded its request to a view. */
const SERVLET_URL = '/ctx/servlet/ticket-link?nr=abc';

/** The URL a page of the view application is loaded under, with the window name. */
const VIEW_URL = '/ctx/view/v0123456789abcdef/tickets?q=x';

/** The same URL as the address bar shows it, without the window name. */
const VISIBLE_VIEW_URL = '/ctx/view/tickets?q=x';

/** Loads the module afresh, for a page loaded under the given URL. */
async function loadPage(url: string, fixedUrl: boolean): Promise<typeof import('./route-sync')> {
  history.replaceState(null, '', url);
  document.body.dataset.contextPath = '/ctx';
  if (fixedUrl) {
    document.body.setAttribute(FIXED_URL_ATTR, 'true');
  }
  vi.resetModules();
  return import('./route-sync');
}

// The module registers its popstate listener with the window for good: the test dispatching a
// popstate comes first, before any page of the view application has registered one.
describe('route-sync', () => {
  let fetchSpy: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    fetchSpy = vi.fn(() => new Promise(() => {}));
    vi.stubGlobal('fetch', fetchSpy);
  });

  afterEach(() => {
    document.body.removeAttribute(FIXED_URL_ATTR);
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('leaves the address bar and the history of a page under a fixed URL unchanged', async () => {
    const routeSync = await loadPage(SERVLET_URL, true);
    const replaceState = vi.spyOn(history, 'replaceState');
    const pushState = vi.spyOn(history, 'pushState');

    routeSync.initRouteSync();
    const arrive = vi.fn();
    routeSync.pushLocalStep(() => {}, arrive);
    window.dispatchEvent(new PopStateEvent('popstate', { state: null }));

    expect(arrive).toHaveBeenCalledOnce();
    expect(replaceState).not.toHaveBeenCalled();
    expect(pushState).not.toHaveBeenCalled();
    expect(fetchSpy).not.toHaveBeenCalled();
    expect(window.location.pathname + window.location.search).toBe(SERVLET_URL);
  });

  it('takes up the entry of a page of the view application', async () => {
    const routeSync = await loadPage(VIEW_URL, false);

    routeSync.initRouteSync();

    expect(window.location.pathname + window.location.search).toBe(VISIBLE_VIEW_URL);
  });
});
