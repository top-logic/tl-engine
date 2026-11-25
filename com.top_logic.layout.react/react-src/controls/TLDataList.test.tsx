import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

/** The listeners of the state updates of the mounted controls, by control ID. */
const updates = new Map<string, (patch: Record<string, unknown>) => void>();

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: (controlId: string, listener: (patch: Record<string, unknown>) => void) => updates.set(controlId, listener),
  unsubscribe: (controlId: string) => updates.delete(controlId),
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

/**
 * The stand-in for the content of a template after the lines of a row: a status text and a button
 * of its own.
 */
const TrailingStub: React.FC<TLCellProps> = ({ controlId }) => (
  <span id={controlId}>
    <span className="test-status">Open</span>
    <button type="button" className="test-action">Act</button>
  </span>
);

/** The stand-in for a template showing a status the server updates, e.g. a pill. */
const StatusStub: React.FC<TLCellProps> = ({ controlId, state }) => (
  <span id={controlId} className="test-status">{String(state.text)}</span>
);

let sent: Sent[] = [];

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
  register('TestTrailing', TrailingStub);
  register('TestStatus', StatusStub);
  register('TLDataList', TLDataList);
});

afterEach(async () => {
  act(() => unmount(LIST_ID));
  findMountPoint(LIST_ID)?.remove();
  vi.unstubAllGlobals();
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

/** Mounts the list, and records the commands it sends. */
async function mountList(overrides: Record<string, unknown> = {}): Promise<void> {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === LIST_ID) {
      sent.push({ command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, LIST_ID);
  document.body.appendChild(element);
  act(() => mount(LIST_ID, 'TLDataList', listState(overrides), 'w', '/ctx'));
  await settle();
}

function list(): HTMLElement {
  return document.getElementById(LIST_ID)!;
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

describe('a list', () => {
  it('has no collapse of its own: no toggle, and its rows always shown', async () => {
    await mountList({ title: 'Contacts' });
    expect(list().querySelector('.tl-toggle')).toBeNull();
    expect(list().getAttribute('data-tl-state')).toBeNull();
    expect(mains()).toHaveLength(3);
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
    expect(initials.hasAttribute('role')).toBe(false);
    expect(initials.hasAttribute('aria-label')).toBe(false);
    expect(leading[1]!.querySelector('.tl-avatar img')!.getAttribute('src')).toBe('/images/arne.png');
    expect(leading[1]!.querySelector('.tl-avatar img')!.getAttribute('alt')).toBe('');
    expect(leading[1]!.querySelector('.tl-avatar')!.getAttribute('aria-hidden')).toBe('true');
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

describe('the content of a template after the lines', () => {
  async function mountTemplateList(): Promise<void> {
    await mountList({
      rows: [
        { id: 'r1', lines: [{ text: 'Anna' }], trailing: { text: '', badge: false, control: { controlId: 't1', module: 'TestTrailing', state: {} } } },
        { id: 'r2', lines: [{ text: 'Arne' }], trailing: { text: 'yesterday' } },
      ],
    });
  }

  it('is the child control of the row, beside the button of the entry, not in it', async () => {
    await mountTemplateList();
    const item = list().querySelector('.tl-list__item')!;
    const trailing = item.querySelector(':scope > .tl-list__trailing')!;
    expect(trailing.querySelector('#t1')).not.toBeNull();
    expect(mains()[0].querySelector('#t1')).toBeNull();
    expect(mains()[0].querySelector('.tl-list__trailing')).toBeNull();
    expect(mains()[1].querySelector('.tl-list__trailing')!.textContent).toBe('yesterday');
  });

  it('selects the row on a click on its text', async () => {
    await mountTemplateList();
    click(list().querySelector('#t1 .test-status')!);
    await settle();
    expect(sent).toEqual([{ command: CMD_SELECT, args: { [ARG_ID]: 'r1' } }]);
  });

  it('leaves a click on its button to the button, without selecting the row', async () => {
    await mountTemplateList();
    click(list().querySelector('#t1 .test-action')!);
    await settle();
    expect(sent).toEqual([]);
  });
});

describe('the template of an entry hidden by the search', () => {
  async function mountStatusList(): Promise<HTMLInputElement> {
    await mountList({
      search: true,
      rows: [
        { id: 'r1', lines: [{ text: 'Anna' }], trailing: { text: '', badge: false, control: { controlId: 's1', module: 'TestStatus', state: { text: 'Open' } } } },
        { id: 'r2', lines: [{ text: 'Arne' }] },
      ],
    });
    return list().querySelector<HTMLInputElement>('.tl-list__search input')!;
  }

  function status(): string | null {
    return list().querySelector('#s1')?.textContent ?? null;
  }

  function update(controlId: string, patch: Record<string, unknown>): void {
    act(() => updates.get(controlId)!(patch));
  }

  it('shows the state it received before it was hidden when it is shown again', async () => {
    const search = await mountStatusList();
    update('s1', { text: 'In progress' });
    expect(status()).toBe('In progress');

    type(search, 'arne');
    expect(status()).toBeNull();
    type(search, '');
    expect(status()).toBe('In progress');
  });

  it('shows the state it received while it was hidden when it is shown again', async () => {
    const search = await mountStatusList();
    type(search, 'arne');
    expect(status()).toBeNull();
    update('s1', { text: 'Closed' });

    type(search, '');
    expect(status()).toBe('Closed');
  });

  it('is released when the list no longer describes it', async () => {
    const search = await mountStatusList();
    type(search, 'arne');
    update(LIST_ID, { rows: [{ id: 'r2', lines: [{ text: 'Arne' }] }] });
    expect(updates.has('s1')).toBe(false);
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

describe('the row whose menu was open', () => {
  it('hands the focus to the trigger of its neighbour when it disappears, with the focus ring', async () => {
    await mountList({ hasRowMenu: true });
    const focusCalls: unknown[] = [];
    const arne = triggers()[1];
    const anna = triggers()[0];
    anna.focus = (options?: FocusOptions) => {
      focusCalls.push(options);
      HTMLElement.prototype.focus.call(anna, options);
    };
    click(arne);
    await settle();

    const rows = (listState().rows as Record<string, unknown>[]).filter((row) => row.id !== 'r2');
    act(() => updates.get(LIST_ID)!({ rows }));

    expect(document.activeElement).toBe(anna);
    expect(focusCalls).toEqual([{ focusVisible: true }]);
  });
});
