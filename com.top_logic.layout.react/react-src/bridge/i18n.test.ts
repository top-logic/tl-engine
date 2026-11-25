import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** The key the tests translate. */
const KEY = 'js.connection.reconnecting';

/** The text shown as long as no translation has arrived. */
const FALLBACK = 'Connection to the server lost.';

/** The translation the server gives. */
const TRANSLATION = 'Verbindung zum Server unterbrochen.';

/** The fetch function answering every i18n request with {@link TRANSLATION}. */
let fetchMock: ReturnType<typeof vi.fn>;

/** Lets the promise reactions run that are due. */
async function settle(): Promise<void> {
  for (let i = 0; i < 10; i++) {
    await Promise.resolve();
  }
}

/**
 * Fresh i18n state per test, and a component showing the translation of {@link KEY} rendered into
 * the document.
 */
async function renderText() {
  vi.resetModules();
  const React = await import('react');
  const { createRoot } = await import('react-dom/client');
  const i18n = await import('./i18n');
  const { setMountContext } = await import('./page-context');

  function Text() {
    const t = i18n.useI18N({ [KEY]: FALLBACK });
    return React.createElement('span', { className: 'text' }, t[KEY]);
  }

  const container = document.createElement('div');
  document.body.appendChild(container);
  const root = createRoot(container);
  const act = React.act;
  return {
    ...i18n,
    setMountContext,
    act,
    /** Renders the component. Called after the setup of the case. */
    render: () => act(() => root.render(React.createElement(Text))),
    text: () => container.querySelector('.text')?.textContent,
  };
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
});

beforeEach(() => {
  fetchMock = vi.fn(() => Promise.resolve(new Response(JSON.stringify({ [KEY]: TRANSLATION }), { status: 200 })));
  vi.stubGlobal('fetch', fetchMock);
});

afterEach(() => {
  vi.unstubAllGlobals();
  document.body.replaceChildren();
});

describe('the i18n service', () => {
  it('sends no request before the context path is known on a page without context attributes and shows the fallback', async () => {
    const { render, text, act } = await renderText();
    render();
    await act(settle);

    expect(fetchMock).not.toHaveBeenCalled();
    expect(text()).toBe(FALLBACK);
  });

  it('sends the waiting keys once the context path is known, with the window name of the same mount', async () => {
    const { render, text, act, setMountContext } = await renderText();
    render();
    await act(settle);

    await act(async () => {
      setMountContext('/ctx', 'v1');
      await settle();
    });

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenCalledWith(
      '/ctx/react-api/i18n?keys=' + encodeURIComponent(KEY) + '&windowName=v1');
    expect(text()).toBe(TRANSLATION);
  });

  it('sends a request in the next microtask once the context path is known', async () => {
    const { render, text, act, setMountContext } = await renderText();
    setMountContext('/ctx', 'v1');
    await settle();
    expect(fetchMock).not.toHaveBeenCalled();

    render();
    expect(fetchMock).not.toHaveBeenCalled();
    expect(text()).toBe(FALLBACK);

    await act(settle);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenCalledWith(
      '/ctx/react-api/i18n?keys=' + encodeURIComponent(KEY) + '&windowName=v1');
    expect(text()).toBe(TRANSLATION);
  });
});
