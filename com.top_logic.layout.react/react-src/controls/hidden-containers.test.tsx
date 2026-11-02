import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

/**
 * The SSE listeners subscribed by the bridge, by control ID: delivering an event to a control calls
 * its listeners like the SSE client does with a patch of the server.
 */
const sse = vi.hoisted(() => {
  const listeners = new Map<string, Set<(data: Record<string, unknown>) => void>>();
  return {
    listeners,
    deliver(controlId: string, data: Record<string, unknown>): void {
      for (const listener of listeners.get(controlId) ?? []) {
        listener(data);
      }
    },
  };
});

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: (controlId: string, listener: (data: Record<string, unknown>) => void) => {
    let set = sse.listeners.get(controlId);
    if (!set) {
      set = new Set();
      sse.listeners.set(controlId, set);
    }
    set.add(listener);
  },
  unsubscribe: (controlId: string, listener: (data: Record<string, unknown>) => void) => {
    const set = sse.listeners.get(controlId);
    set?.delete(listener);
    if (set?.size === 0) {
      sse.listeners.delete(controlId);
    }
  },
}));

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { register } from 'tl-react-bridge';
import TLStack from './TLStack';
import TLDeckPane from './TLDeckPane';
import TLGrid from './TLGrid';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const ROOT_ID = 'root';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('TLStack', TLStack);
  register('TLDeckPane', TLDeckPane);
  register('TLGrid', TLGrid);
});

afterEach(async () => {
  act(() => unmount(ROOT_ID));
  document.getElementById(ROOT_ID)?.remove();
  await settle();
});

afterAll(async () => {
  // The bridge entry adds a tooltip host to the page. It goes while the test environment exists, so that the DOM
  // observer of the bridge does not see its removal by the teardown of the environment.
  document.body.replaceChildren();
  await settle();
});

/** Lets the DOM observer of the bridge see what changed. */
async function settle(): Promise<void> {
  for (let round = 0; round < 5; round++) {
    await new Promise<void>(resolve => setTimeout(resolve, 0));
  }
}

/** Mounts a column holding the given child controls. */
function mountColumn(children: Record<string, unknown>[]): void {
  const element = document.createElement('div');
  element.id = ROOT_ID;
  document.body.appendChild(element);
  act(() => mount(ROOT_ID, 'TLStack', { direction: 'column', children }, 'w', '/ctx'));
}

function child(controlId: string, module: string, state: Record<string, unknown>): Record<string, unknown> {
  return { controlId, module, state };
}

function display(controlId: string): string {
  return document.getElementById(controlId)!.style.display;
}

describe('a hidden container', () => {
  it('is not displayed, so that it takes no place - and no gap - in its container', () => {
    mountColumn([
      child('stack', 'TLStack', { hidden: true, children: [] }),
      child('deck', 'TLDeckPane', { hidden: true, activeChild: null }),
      child('grid', 'TLGrid', { hidden: true, children: [] }),
      child('shown', 'TLStack', { children: [] }),
    ]);

    expect(display('stack')).toBe('none');
    expect(display('deck')).toBe('none');
    expect(display('grid')).toBe('none');
    expect(display('shown')).toBe('');
  });

  it('is displayed again when the server shows it', () => {
    mountColumn([
      child('deck', 'TLDeckPane', { hidden: true, activeChild: null }),
      child('stack', 'TLStack', { hidden: true, maxWidth: '10rem', children: [] }),
    ]);

    act(() => {
      sse.deliver('deck', { hidden: false });
      sse.deliver('stack', { hidden: false });
    });

    expect(display('deck')).toBe('');
    expect(display('stack')).toBe('');
    expect(document.getElementById('stack')!.style.maxWidth).toBe('10rem');
  });
});
