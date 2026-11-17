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
import { React, register, registerPart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLDataList, { ARG_ID, ARG_X, ARG_Y, CMD_ROW_MENU, CMD_SELECT } from './TLDataList';
import ButtonPart from './button/ButtonPart';
import DisclosureTogglePart from './button/DisclosureTogglePart';
import MenuTriggerPart from './button/MenuTriggerPart';
import TextInputPart from './form/TextInputPart';
import AvatarPart from './AvatarPart';
import BadgePart from './chip/BadgePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** A command the list sent: its name and its arguments. */
type Sent = { command: string; args: Record<string, unknown> };

const LIST_ID = 'list';

/** The stand-in for a command button of the list, a child control of its own. */
const CommandStub: React.FC<TLCellProps> = ({ controlId }) => (
  <button type="button" id={controlId} className="tl-button">New</button>
);

/** The observers the list created, so that a test can say "the host was resized". */
const observers: FakeResizeObserver[] = [];

class FakeResizeObserver {
  constructor(private readonly callback: () => void) {
    observers.push(this);
  }
  observe(): void {}
  unobserve(): void {}
  disconnect(): void {
    observers.splice(observers.indexOf(this), 1);
  }
  fire(): void {
    this.callback();
  }
}

let sent: Sent[] = [];

/** The width the host reports, in px (at a 16 px rem, 44 rem are 704 px). */
let hostWidth = 1100;

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  registerPart('DisclosureToggle', DisclosureTogglePart);
  registerPart('MenuTrigger', MenuTriggerPart);
  registerPart('TextInput', TextInputPart);
  registerPart('Avatar', AvatarPart);
  registerPart('Badge', BadgePart);
  register('TestCommand', CommandStub);
  register('TLDataList', TLDataList);
});

afterEach(async () => {
  act(() => unmount(LIST_ID));
  findMountPoint(LIST_ID)?.remove();
  vi.unstubAllGlobals();
  observers.length = 0;
  await settle();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Lets the command channel hand the queued commands to fetch, and the translations arrive. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** The state of a list of three rows in two groups, as the server writes it. */
function listState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    search: false,
    collapsible: false,
    title: 'Contacts',
    emptyText: 'Nobody yet',
    selectedId: '',
    rows: [
      { id: 'r1', group: 'A', lines: [{ text: 'Anna' }, { text: 'anna@example.com', emphasis: 'secondary' }], cssClass: 'is-done' },
      { id: 'r2', group: 'A', lines: [{ text: 'Arne' }, { text: '', emphasis: 'secondary' }] },
      { id: 'r3', group: 'B', lines: [{ text: 'Berta' }, { text: 'berta@example.com', emphasis: 'secondary' }] },
    ],
    ...overrides,
  };
}

/** Mounts the list into a host of the width {@link hostWidth}, and records the commands it sends. */
async function mountList(overrides: Record<string, unknown> = {}, width = 1100): Promise<void> {
  sent = [];
  hostWidth = width;
  vi.stubGlobal('ResizeObserver', FakeResizeObserver);
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === LIST_ID) {
      sent.push({ command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, LIST_ID);
  Object.defineProperty(element, 'clientWidth', { get: () => hostWidth });
  document.body.appendChild(element);
  act(() => mount(LIST_ID, 'TLDataList', listState(overrides), 'w', '/ctx'));
  await settle();
}

/** The host becomes this wide, and the observer says so. */
function resize(width: number): void {
  hostWidth = width;
  act(() => observers.forEach((observer) => observer.fire()));
}

function list(): HTMLElement {
  return document.getElementById(LIST_ID)!;
}

/** The toggle of the strip or column, not the one in an open drawer. */
function toggle(): HTMLButtonElement {
  return list().querySelector<HTMLButtonElement>(':scope > .tl-list__header > .tl-toggle')!;
}

function drawer(): HTMLElement | null {
  return list().querySelector<HTMLElement>('aside.tl-drawer');
}

function scrim(): HTMLElement | null {
  return list().querySelector<HTMLElement>('.tl-drawer__scrim');
}

function mains(root: ParentNode = list()): HTMLButtonElement[] {
  return [...root.querySelectorAll<HTMLButtonElement>('.tl-list__main')];
}

function triggers(): HTMLButtonElement[] {
  return [...list().querySelectorAll<HTMLButtonElement>('.tl-list__item > button[aria-haspopup="menu"]')];
}

function click(element: Element): void {
  act(() => (element as HTMLElement).click());
}

function key(element: Element, name: string): KeyboardEvent {
  const event = new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true });
  act(() => {
    element.dispatchEvent(event);
  });
  return event;
}

/** Types the given text into the search field, as the user does. */
function type(input: HTMLInputElement, text: string): void {
  const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!;
  act(() => {
    setter.call(input, text);
    input.dispatchEvent(new Event('input', { bubbles: true }));
  });
}

describe('the rows', () => {
  it('stand in their groups in order, with the lines of each row', async () => {
    await mountList();
    expect([...list().querySelectorAll('.tl-list__subheader')].map((h) => h.textContent)).toEqual(['A', 'B']);
    expect(mains().map((b) => b.querySelector('.tl-list__primary')!.textContent)).toEqual(['Anna', 'Arne', 'Berta']);
    expect(mains()[0].querySelector('.tl-list__secondary')!.textContent).toBe('anna@example.com');
    const group = list().querySelector('.tl-list__group')!;
    expect(group.getAttribute('aria-labelledby')).toBe(list().querySelector('.tl-list__subheader')!.id);
  });

  it('draw no secondary line that is empty for the row', async () => {
    await mountList();
    expect(mains()[1].querySelectorAll('.tl-list__secondary')).toHaveLength(0);
  });

  it('carry the computed classes of the row on its element', async () => {
    await mountList();
    expect(list().querySelector('.tl-list__item')!.className).toBe('tl-list__item is-done');
  });

  it('mark the selected row as current', async () => {
    await mountList({ selectedId: 'r2' });
    expect(mains().map((b) => b.getAttribute('aria-current'))).toEqual([null, 'true', null]);
  });

  it('say the configured empty text when there are none', async () => {
    await mountList({ rows: [] });
    expect(list().querySelector('.tl-list__message')!.textContent).toBe('Nobody yet');
  });

  it('send the selection of a clicked row', async () => {
    await mountList();
    click(mains()[2]);
    await settle();
    expect(sent).toEqual([{ command: CMD_SELECT, args: { [ARG_ID]: 'r3' } }]);
  });
});

describe('the search', () => {
  it('filters the rows by the text of their lines and says when nothing matches', async () => {
    await mountList({ search: true });
    const input = list().querySelector<HTMLInputElement>('.tl-list__search input')!;
    type(input, 'BERTA@');
    expect(mains().map((b) => b.title)).toEqual(['Berta']);
    type(input, 'zzz');
    expect(list().querySelector('.tl-list__message')!.textContent).toBe('No matching entries.');
  });
});

describe('the commands of the list', () => {
  it('are the child controls the server put into the state', async () => {
    await mountList({ buttons: [{ controlId: 'b1', module: 'TestCommand', state: {} }] });
    expect(list().querySelector('.tl-list__commands #b1')).not.toBeNull();
  });
});

describe('the trigger of the row commands', () => {
  it('is absent until the server says there is a menu', async () => {
    await mountList({ rowActions: 'always' });
    expect(triggers()).toHaveLength(0);
  });

  it('names its row and is revealed on hover', async () => {
    await mountList({ hasRowMenu: true, rowActions: 'hover' });
    expect(triggers().map((t) => t.getAttribute('aria-label'))).toEqual(['Actions: Anna', 'Actions: Arne', 'Actions: Berta']);
    expect(triggers()[0].classList.contains('tl-reveal-on-hover')).toBe(true);
  });

  it('stands always in the always mode', async () => {
    await mountList({ hasRowMenu: true, rowActions: 'always' });
    expect(triggers()).toHaveLength(3);
    expect(triggers()[0].classList.contains('tl-reveal-on-hover')).toBe(false);
  });

  it('is absent in the none mode', async () => {
    await mountList({ hasRowMenu: true, rowActions: 'none' });
    expect(triggers()).toHaveLength(0);
  });

  it('asks for the menu of its row at the point of the click', async () => {
    await mountList({ hasRowMenu: true });
    act(() => {
      triggers()[1].dispatchEvent(new MouseEvent('click', { bubbles: true, clientX: 12.4, clientY: 30 }));
    });
    await settle();
    expect(sent).toEqual([{ command: CMD_ROW_MENU, args: { [ARG_ID]: 'r2', [ARG_X]: 12, [ARG_Y]: 30 } }]);
  });
});

describe('a collapsible list in a wide host', () => {
  it('collapses to its strip and expands again', async () => {
    await mountList({ collapsible: true });
    expect(list().getAttribute('data-tl-state')).toBeNull();
    click(toggle());
    expect(list().getAttribute('data-tl-state')).toBe('collapsed');
    expect(toggle().getAttribute('aria-expanded')).toBe('false');
    expect(mains()).toHaveLength(3);
    click(toggle());
    expect(list().getAttribute('data-tl-state')).toBeNull();
  });

  it('starts collapsed when the element says so', async () => {
    await mountList({ collapsible: true, collapsed: true });
    expect(list().getAttribute('data-tl-state')).toBe('collapsed');
  });

  it('needs collapsible to start collapsed', async () => {
    await mountList({ collapsible: false, collapsed: true });
    expect(list().getAttribute('data-tl-state')).toBeNull();
    expect(toggle()).toBeNull();
  });

  it('is never a drawer with drawer-below 0', async () => {
    await mountList({ collapsible: true, drawerBelow: 0 }, 320);
    expect(list().getAttribute('data-tl-state')).toBeNull();
    expect(observers).toHaveLength(0);
  });
});

describe('a collapsible list in a narrow host', () => {
  it('is a strip without rows, its toggle offering to open it', async () => {
    await mountList({ collapsible: true, drawerBelow: 44 }, 560);
    expect(list().getAttribute('data-tl-state')).toBe('collapsed');
    expect(toggle().getAttribute('aria-expanded')).toBe('false');
    expect(toggle().getAttribute('aria-label')).toBe('Expand list');
    expect(mains()).toHaveLength(0);
    expect(drawer()).toBeNull();
  });

  it('switches at the configured width, measured in rem', async () => {
    await mountList({ collapsible: true, drawerBelow: 44 }, 704);
    expect(list().getAttribute('data-tl-state')).toBeNull();
    resize(703);
    expect(list().getAttribute('data-tl-state')).toBe('collapsed');
  });

  it('opens as a modal drawer with a scrim, the focus on the selected row', async () => {
    await mountList({ collapsible: true, drawerBelow: 44, selectedId: 'r3' }, 560);
    click(toggle());
    const aside = drawer()!;
    expect(aside.className).toBe('tl-drawer tl-drawer--start tl-drawer--narrow tl-drawer--container');
    expect(aside.getAttribute('role')).toBe('dialog');
    expect(aside.getAttribute('aria-modal')).toBe('true');
    expect(scrim()).not.toBeNull();
    expect(document.activeElement?.getAttribute('title')).toBe('Berta');
  });

  it('closes when a row is picked, still picks it, and hands the focus back to the toggle', async () => {
    await mountList({ collapsible: true, drawerBelow: 44 }, 560);
    click(toggle());
    expect(document.activeElement?.getAttribute('title')).toBe('Anna');
    click(mains(drawer()!)[1]);
    await settle();
    expect(drawer()).toBeNull();
    expect(document.activeElement).toBe(toggle());
    expect(sent).toEqual([{ command: CMD_SELECT, args: { [ARG_ID]: 'r2' } }]);
  });

  it('closes on the scrim, on its own toggle and on a command', async () => {
    await mountList({ collapsible: true, drawerBelow: 44, buttons: [{ controlId: 'b1', module: 'TestCommand', state: {} }] }, 560);
    click(toggle());
    click(scrim()!);
    expect(drawer()).toBeNull();

    click(toggle());
    click(drawer()!.querySelector('.tl-toggle')!);
    expect(drawer()).toBeNull();

    click(toggle());
    click(drawer()!.querySelector('#b1')!);
    expect(drawer()).toBeNull();
  });

  it('closes on Escape, but leaves Escape to a search field with text in it', async () => {
    await mountList({ collapsible: true, drawerBelow: 44, search: true }, 560);
    click(toggle());
    const input = drawer()!.querySelector<HTMLInputElement>('input')!;
    type(input, 'ann');
    act(() => input.focus());
    key(input, 'Escape');
    expect(drawer()).not.toBeNull();

    type(input, '');
    const event = key(input, 'Escape');
    expect(event.defaultPrevented).toBe(true);
    expect(drawer()).toBeNull();
    expect(document.activeElement).toBe(toggle());
  });

  it('keeps the user’s column choice across the drawer and back', async () => {
    await mountList({ collapsible: true, drawerBelow: 44 }, 1100);
    click(toggle());
    expect(list().getAttribute('data-tl-state')).toBe('collapsed');
    resize(560);
    click(toggle());
    expect(drawer()).not.toBeNull();
    resize(1100);
    expect(drawer()).toBeNull();
    expect(list().getAttribute('data-tl-state')).toBe('collapsed');
  });
});

describe('what stands in front of and after the lines', () => {
  it('is an avatar with the initials of the name, or with the picture, or an icon', async () => {
    await mountList({
      rows: [
        { id: 'r1', lines: [{ text: 'Anna Albrecht' }], leading: { name: 'Anna Albrecht' } },
        { id: 'r2', lines: [{ text: 'Arne' }], leading: { name: 'Arne', image: '/images/arne.png' } },
        { id: 'r3', lines: [{ text: 'Office' }], leading: { icon: 'css:bi bi-building' } },
        { id: 'r4', lines: [{ text: 'Nobody' }] },
      ],
    });
    const leading = mains().map((main) => main.querySelector('.tl-list__leading'));
    const initials = leading[0]!.querySelector('.tl-avatar')!;
    expect(initials.textContent).toBe('AA');
    expect(initials.getAttribute('data-tl-role')).toMatch(/^category-[1-8]$/);
    expect(initials.getAttribute('aria-hidden')).toBe('true');
    expect(leading[1]!.querySelector('.tl-avatar img')!.getAttribute('src')).toBe('/images/arne.png');
    expect(leading[2]!.querySelector('.tl-avatar')).toBeNull();
    expect(leading[2]!.querySelector('i')!.className).toContain('bi-building');
    expect(leading[3]).toBeNull();
  });

  it('is metadata text or a badge', async () => {
    await mountList({
      rows: [
        { id: 'r1', lines: [{ text: 'Anna' }], trailing: { text: 'yesterday' } },
        { id: 'r2', lines: [{ text: 'Arne' }], trailing: { text: '3', badge: true } },
      ],
    });
    const trailing = mains().map((main) => main.querySelector('.tl-list__trailing')!);
    expect(trailing[0].textContent).toBe('yesterday');
    expect(trailing[0].querySelector('.tl-badge')).toBeNull();
    expect(trailing[1].querySelector('.tl-badge')!.textContent).toBe('3');
  });
});

describe('a right click on a row', () => {
  function rightClick(element: Element): MouseEvent {
    const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    act(() => {
      element.dispatchEvent(event);
    });
    return event;
  }

  it('selects a row outside the selection and lets the event go on to a menu region', async () => {
    await mountList({ selectedId: 'r1' });
    let reached = false;
    const listener = () => { reached = true; };
    document.body.addEventListener('contextmenu', listener);
    try {
      const event = rightClick(mains()[2]);
      await settle();
      expect(sent).toEqual([{ command: CMD_SELECT, args: { [ARG_ID]: 'r3' } }]);
      expect(event.defaultPrevented).toBe(false);
      expect(reached).toBe(true);
    } finally {
      document.body.removeEventListener('contextmenu', listener);
    }
  });

  it('keeps the selection on the selected row', async () => {
    await mountList({ selectedId: 'r2' });
    rightClick(mains()[1]);
    await settle();
    expect(sent).toEqual([]);
  });
});
