// A click on the entry of the page that is shown already: in the drawer of a small window it must reach the server,
// which closes the drawer; in the rail of a wide window it is a request without an effect and is not sent.
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
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLSidebar from './TLSidebar';
import DisclosureTogglePart from './button/DisclosureTogglePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const SIDEBAR_ID = 'sidebar';

/** The commands the sidebar sent: their names and arguments. */
let sent: Array<{ command: string, args: Record<string, unknown> }> = [];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('DisclosureToggle', DisclosureTogglePart);
  register('TLSidebar', TLSidebar);
});

afterEach(async () => {
  act(() => unmount(SIDEBAR_ID));
  findMountPoint(SIDEBAR_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Lets the command channel hand the queued commands to fetch. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** A sidebar with two entries that shows the page of "home", in a window of the given display class. */
async function mountSidebar({ compact, drawerOpen }: { compact: boolean, drawerOpen: boolean }): Promise<void> {
  vi.stubGlobal('matchMedia', (query: string) => ({
    matches: compact, media: query, addEventListener: () => {}, removeEventListener: () => {},
  }));
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === SIDEBAR_ID) {
      sent.push({ command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, SIDEBAR_ID);
  document.body.appendChild(element);
  act(() => mount(SIDEBAR_ID, 'TLSidebar', {
    items: [
      { id: 'home', type: 'nav', label: 'Home' },
      { id: 'reports', type: 'nav', label: 'Reports' },
    ],
    activeItemId: 'home',
    collapsed: false,
    drawerOpen,
  }, 'w', '/ctx'));
  await settle();
}

/** The entry of the page that is shown. */
function activeEntry(): HTMLButtonElement {
  return document.querySelector<HTMLButtonElement>(`#${SIDEBAR_ID} .tl-sidebar__item[aria-current="page"]`)!;
}

describe('a click on the active entry', () => {
  it('in the open drawer of a small window selects it, so that the server closes the drawer', async () => {
    await mountSidebar({ compact: true, drawerOpen: true });
    expect(activeEntry().closest('aside.tl-drawer')).not.toBeNull();

    act(() => activeEntry().click());
    await settle();

    expect(sent).toEqual([{ command: 'selectItem', args: { itemId: 'home' } }]);
  });

  it('in the rail of a wide window sends nothing', async () => {
    await mountSidebar({ compact: false, drawerOpen: false });

    act(() => activeEntry().click());
    await settle();

    expect(sent).toEqual([]);
  });
});
