// The badge of a tab, an avatar and a navigation entry: drawn through the part Badge, in its color
// role, as a dot, and as part of the accessible name of what it stands at.

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
import TLTabBar from './TLTabBar';
import TLAvatar from './TLAvatar';
import TLSidebar from './TLSidebar';
import TLBottomBar from './TLBottomBar';
import AvatarPart from './AvatarPart';
import BadgePart from './chip/BadgePart';
import DisclosureTogglePart from './button/DisclosureTogglePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const ROOT_ID = 'c';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Badge', BadgePart);
  registerPart('Avatar', AvatarPart);
  registerPart('DisclosureToggle', DisclosureTogglePart);
  register('TLTabBar', TLTabBar);
  register('TLAvatar', TLAvatar);
  register('TLSidebar', TLSidebar);
  register('TLBottomBar', TLBottomBar);
});

afterEach(() => {
  act(() => unmount(ROOT_ID));
  findMountPoint(ROOT_ID)?.remove();
});

afterAll(() => {
  document.body.replaceChildren();
});

function draw(module: string, state: Record<string, unknown>): HTMLElement {
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, ROOT_ID);
  document.body.appendChild(element);
  act(() => mount(ROOT_ID, module, state, 'w', '/ctx'));
  return element;
}

function badgeIn(element: Element): HTMLElement {
  return element.querySelector('.tl-badge') as HTMLElement;
}

describe('TLTabBar with a badge', () => {
  function tab(root: HTMLElement, label: string): HTMLElement {
    return Array.from(root.querySelectorAll<HTMLElement>('[role="tab"]')).find(t => t.textContent!.startsWith(label))!;
  }

  it('draws the count after the label of the tab, read with the label', () => {
    const root = draw('TLTabBar', {
      tabs: [{ id: 'a', label: 'Open', badge: { content: '7', role: 'info' } }, { id: 'b', label: 'Done' }],
      activeTabId: 'a',
    });

    const open = tab(root, 'Open');
    expect(badgeIn(open).textContent).toBe('7');
    expect(badgeIn(open).getAttribute('data-tl-role')).toBe('info');
    expect(badgeIn(open).getAttribute('role')).toBeNull();
    expect(open.textContent).toBe('Open7');
    expect(badgeIn(tab(root, 'Done'))).toBeNull();
  });

  it('draws a dot as an image named by the label of the badge', () => {
    const root = draw('TLTabBar', {
      tabs: [{ id: 'a', label: 'Open', badge: { dot: true, label: 'New entries' } }],
      activeTabId: 'a',
    });

    const dot = badgeIn(tab(root, 'Open'));
    expect(dot.classList).toContain('tl-badge--dot');
    expect(dot.getAttribute('role')).toBe('img');
    expect(dot.getAttribute('aria-label')).toBe('New entries');
  });
});

describe('TLAvatar with a badge', () => {
  it('puts the badge over the corner of the avatar as an image named by its content', () => {
    const root = draw('TLAvatar', { name: 'Anna Meier', badge: { content: '3', role: 'error' } });

    const avatar = root.querySelector('.tl-avatar')!;
    expect(avatar.parentElement!.classList).toContain('tl-icon-badge');
    const badge = badgeIn(root);
    expect(badge.parentElement!.classList).toContain('tl-icon-badge__count');
    expect(badge.getAttribute('data-tl-role')).toBe('error');
    expect(badge.getAttribute('role')).toBe('img');
    expect(badge.getAttribute('aria-label')).toBe('3');
  });

  it('names the badge by its label where it has one', () => {
    const root = draw('TLAvatar', { name: 'Anna Meier', badge: { dot: true, role: 'success', label: 'Online' } });

    const badge = badgeIn(root);
    expect(badge.classList).toContain('tl-badge--dot');
    expect(badge.getAttribute('aria-label')).toBe('Online');
  });

  it('draws the avatar alone without a badge', () => {
    const root = draw('TLAvatar', { name: 'Anna Meier' });

    expect(root.querySelector('.tl-icon-badge')).toBeNull();
    expect(badgeIn(root)).toBeNull();
  });
});

describe('TLBottomBar with a badge', () => {
  it('puts the badge over the corner of the icon of an item and names the item by label and badge', () => {
    const root = draw('TLBottomBar', {
      items: [
        { id: 'inbox', label: 'Inbox', icon: 'css:bi bi-inbox', badge: { content: '3', role: 'error' } },
        { id: 'home', label: 'Home', icon: 'css:bi bi-house' },
      ],
      activeItemId: 'home',
    });

    const [inbox, home] = Array.from(root.querySelectorAll<HTMLElement>('.tl-bottom-bar__item'));
    const badge = badgeIn(inbox);
    expect(badge.parentElement!.classList).toContain('tl-icon-badge__count');
    expect(badge.getAttribute('data-tl-role')).toBe('error');
    expect(inbox.getAttribute('aria-label')).toBe('Inbox, 3');
    expect(badgeIn(home)).toBeNull();
    expect(home.hasAttribute('aria-label')).toBe(false);
  });
});

describe('TLSidebar with a badge', () => {
  const ITEMS = [
    { id: 'inbox', type: 'nav', label: 'Inbox', icon: 'css:bi bi-inbox', badge: { content: '4', role: 'warning' } },
    { id: 'news', type: 'nav', label: 'News', icon: 'css:bi bi-bell', badge: { dot: true, label: 'New entries' } },
    { id: 'sync', type: 'command', label: 'Sync', icon: 'css:bi bi-arrow-repeat', badge: { content: '2', role: 'error' } },
  ];

  function entry(root: HTMLElement, id: number): HTMLElement {
    return root.querySelectorAll<HTMLElement>('.tl-sidebar__item')[id];
  }

  it('draws the count after the label of an entry of the open rail, in the role of the badge', () => {
    const root = draw('TLSidebar', { items: ITEMS, activeItemId: 'inbox', collapsed: false });

    const inbox = entry(root, 0);
    expect(badgeIn(inbox).textContent).toBe('4');
    expect(badgeIn(inbox).getAttribute('data-tl-role')).toBe('warning');
    expect(inbox.textContent).toContain('Inbox4');
    const dot = badgeIn(entry(root, 1));
    expect(dot.classList).toContain('tl-badge--dot');
    expect(dot.getAttribute('aria-label')).toBe('New entries');
  });

  it('draws the badge of a command item like the one of a navigation item', () => {
    const open = draw('TLSidebar', { items: ITEMS, activeItemId: 'inbox', collapsed: false });
    const sync = entry(open, 2);
    expect(badgeIn(sync).textContent).toBe('2');
    expect(badgeIn(sync).getAttribute('data-tl-role')).toBe('error');
    expect(sync.textContent).toContain('Sync2');
    act(() => unmount(ROOT_ID));
    findMountPoint(ROOT_ID)?.remove();

    const folded = draw('TLSidebar', { items: ITEMS, activeItemId: 'inbox', collapsed: true });
    expect(entry(folded, 2).getAttribute('aria-label')).toBe('Sync, 2');
    expect(badgeIn(entry(folded, 2)).parentElement!.classList).toContain('tl-icon-badge__count');
  });

  it('names an entry of the folded rail by its label and its badge', () => {
    const root = draw('TLSidebar', { items: ITEMS, activeItemId: 'inbox', collapsed: true });

    expect(entry(root, 0).getAttribute('aria-label')).toBe('Inbox, 4');
    expect(entry(root, 1).getAttribute('aria-label')).toBe('News, New entries');
    expect(badgeIn(entry(root, 0)).parentElement!.classList).toContain('tl-icon-badge__count');
  });
});
