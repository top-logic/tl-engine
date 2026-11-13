import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { React, register } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLWindow from './TLWindow';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const WINDOW_ID = 'window';

/** The stand-in for the content of the window. */
const WindowContent: React.FC<TLCellProps> = ({ controlId }) => <div id={controlId} />;

const CONTENT = { controlId: 'content', module: 'WindowTestContent', state: {} };

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('WindowTestContent', WindowContent);
  register('TLWindow', TLWindow);
});

afterEach(async () => {
  act(() => unmount(WINDOW_ID));
  document.getElementById(WINDOW_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(async () => {
  // The bridge entry adds a tooltip host to the page. It goes while the test environment exists, so that the DOM
  // observer of the bridge does not see its removal by the teardown of the environment.
  document.body.replaceChildren();
  await settle();
});

/** Waits until the DOM observer of the bridge has seen the changes. */
async function settle(): Promise<void> {
  for (let round = 0; round < 5; round++) {
    await new Promise<void>(resolve => setTimeout(resolve, 0));
  }
}

/** Mounts a window with the given state, answering every command it sends. */
function mountWindow(state: Record<string, unknown>): void {
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  const element = document.createElement('div');
  element.id = WINDOW_ID;
  document.body.appendChild(element);
  act(() => mount(WINDOW_ID, 'TLWindow', { title: 'Window', resizable: true, child: CONTENT, ...state }, 'w', '/ctx'));
}

/** The surface of the mounted window. */
function surface(): HTMLElement {
  return document.querySelector<HTMLElement>('.tl-dialog__surface')!;
}

describe('the configured size of a window', () => {
  it('uses a size in pixels as it is', () => {
    mountWindow({ width: '640px', height: '600px' });
    expect(surface().style.width).toBe('640px');
    expect(surface().style.height).toBe('600px');
  });

  it('relates a size in percent to the browser window', () => {
    mountWindow({ width: '80%', height: '75%' });
    expect(surface().style.width).toBe('calc(var(--tl-size-viewport-width) * 0.8)');
    expect(surface().style.height).toBe('calc(var(--tl-size-viewport-height) * 0.75)');
  });

  it('leaves the height to the content without a configured height', () => {
    mountWindow({ width: '500px' });
    expect(surface().style.height).toBe('');
  });
});
