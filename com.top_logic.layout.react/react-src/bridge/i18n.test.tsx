// The address of the text request: it is built from the context path of the page, also when the
// busy overlay asks for its text before the first control is mounted on a page at a deep address.

import { act } from 'react';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** The context path of the application. */
const CONTEXT_PATH = '/ctx';

/** A deep address of a page below the context path. */
const DEEP_PAGE = CONTEXT_PATH + '/view/a/b';

/** The path that the text requests must go to. */
const I18N_PATH = CONTEXT_PATH + '/react-api/i18n';

/** The addresses of the requests sent so far, resolved against the page as the browser does. */
let requested: URL[] = [];

/** Lets the batched request of the texts go out. */
async function settle(): Promise<void> {
  for (let i = 0; i < 10; i++) {
    await Promise.resolve();
  }
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
});

beforeEach(() => {
  vi.resetModules();
  requested = [];
  vi.stubGlobal('fetch', vi.fn((url: string) => {
    requested.push(new URL(url, window.location.href));
    return Promise.resolve(new Response('{}', { status: 200 }));
  }));
  window.history.replaceState(null, '', DEEP_PAGE);
});

afterEach(() => {
  vi.unstubAllGlobals();
  delete document.body.dataset.contextPath;
  document.body.innerHTML = '';
  window.history.replaceState(null, '', '/');
});

describe('the text request', () => {
  it('goes to the context path when the busy overlay asks before the first mount', async () => {
    document.body.dataset.contextPath = CONTEXT_PATH;
    const { initBusyOverlay } = await import('./busy-overlay');

    act(() => initBusyOverlay());
    await settle();

    expect(requested.map(url => url.pathname)).toEqual([I18N_PATH]);
  });

  it('goes to the base set by the mount on a page without a context path', async () => {
    const { setI18NApiBase } = await import('./i18n');
    setI18NApiBase(CONTEXT_PATH + '/');
    const { initBusyOverlay } = await import('./busy-overlay');

    act(() => initBusyOverlay());
    await settle();

    expect(requested.map(url => url.pathname)).toEqual([I18N_PATH]);
  });
});
