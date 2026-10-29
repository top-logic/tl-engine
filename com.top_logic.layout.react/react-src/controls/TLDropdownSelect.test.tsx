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
import { register, registerPart } from 'tl-react-bridge';
import TLDropdownSelect from './TLDropdownSelect';
import ButtonPart from './button/ButtonPart';
import TextInputPart from './form/TextInputPart';
import ProgressBarPart from './progress/ProgressBarPart';
import ChipPart from './chip/ChipPart';
import { ARG_TEXT, CMD_ENTER_TEXT } from './selectOptions';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** A command the select sent: its name and its arguments. */
type Sent = { command: string; args: Record<string, unknown> };

const SELECT_ID = 'select';

/** The command transmitting the chosen options. */
const CMD_VALUE_CHANGED = 'valueChanged';

const ALPHA = { value: 'a', label: 'Alpha' };
const ALPINE = { value: 'p', label: 'Alpine' };
const BETA = { value: 'b', label: 'Beta' };
const OPTIONS = [ALPHA, ALPINE, BETA];

let sent: Sent[] = [];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  // jsdom lays nothing out.
  Element.prototype.scrollIntoView = () => {};
  registerPart('Button', ButtonPart);
  registerPart('TextInput', TextInputPart);
  registerPart('ProgressBar', ProgressBarPart);
  registerPart('Chip', ChipPart);
  register('TLDropdownSelect', TLDropdownSelect);
});

afterEach(async () => {
  act(() => unmount(SELECT_ID));
  document.getElementById(SELECT_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts the select with the given state, its options loaded, and records the commands it sends. */
function mountSelect(state: Record<string, unknown>): void {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === SELECT_ID) {
      sent.push({ command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.id = SELECT_ID;
  document.body.appendChild(element);
  act(() => mount(SELECT_ID, 'TLDropdownSelect',
    { value: [], optionsLoaded: true, options: OPTIONS, ...state }, 'w', '/ctx'));
}

/**
 * Waits until the command channel has handed all queued commands to fetch, and the translations the
 * select asked for have arrived.
 */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

function field(): HTMLElement {
  return document.getElementById(SELECT_ID)!.querySelector<HTMLElement>('[role="combobox"]')
    ?? document.getElementById(SELECT_ID)!;
}

function search(): HTMLInputElement | null {
  return document.querySelector<HTMLInputElement>('.tl-select__search-group input');
}

/** Opens the list with a click on the field. */
function open(): void {
  act(() => field().click());
}

/** Types the given text into the search field, as the user does. */
function type(text: string): void {
  const input = search()!;
  const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!;
  act(() => {
    setter.call(input, text);
    input.dispatchEvent(new Event('input', { bubbles: true }));
  });
}

function key(name: string): void {
  act(() => {
    search()!.dispatchEvent(new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true }));
  });
}

/** The labels of the rows of the open list. */
function rows(): string[] {
  return [...document.querySelectorAll('.tl-select__list [role="option"]')].map(row => row.textContent ?? '');
}

function highlighted(): string | null {
  return document.querySelector('.tl-select__list [data-tl-state="highlighted"]')?.textContent ?? null;
}

function entered(text: string): Sent {
  return { command: CMD_ENTER_TEXT, args: { [ARG_TEXT]: text } };
}

describe('a select accepting free entry', () => {
  it('enters a text naming no option with Enter, closing the list of a single-valued field', async () => {
    mountSelect({ freeEntry: true });
    open();
    type(' Gamma ');
    expect(highlighted()).toBeNull();
    key('Enter');
    await settle();

    expect(sent).toEqual([entered('Gamma')]);
    expect(search()).toBeNull();
  });

  it('enters a text contained in a single option label as text rather than choosing that option', async () => {
    mountSelect({ freeEntry: true });
    open();
    type('Bet');
    expect(rows()).toEqual(['Beta', 'Use "Bet"']);
    expect(highlighted()).toBeNull();
    key('Enter');
    await settle();

    expect(sent).toEqual([entered('Bet')]);
  });

  it('chooses the highlighted option with Enter', async () => {
    mountSelect({ freeEntry: true });
    open();
    type('Alp');
    key('ArrowDown');
    key('ArrowDown');
    expect(highlighted()).toBe('Alpine');
    key('Enter');
    await settle();

    expect(sent).toEqual([{ command: CMD_VALUE_CHANGED, args: { value: ['p'] } }]);
  });

  it('highlights the option a text names exactly and hides the entry taking the text', async () => {
    mountSelect({ freeEntry: true });
    open();
    type('alpha');
    expect(rows()).toEqual(['Alpha']);
    expect(highlighted()).toBe('Alpha');
    key('Enter');
    await settle();

    expect(sent).toEqual([{ command: CMD_VALUE_CHANGED, args: { value: ['a'] } }]);
  });

  it('offers the entry taking the text at the end of the list, reached with the arrow keys and clickable', async () => {
    mountSelect({ freeEntry: true });
    open();
    type('Al');
    expect(rows()).toEqual(['Alpha', 'Alpine', 'Use "Al"']);
    key('ArrowUp');
    expect(highlighted()).toBe('Use "Al"');
    act(() => (document.querySelector('.tl-select__list [data-tl-state="highlighted"]') as HTMLElement).click());
    await settle();

    expect(sent).toEqual([entered('Al')]);
  });

  it('keeps the list of a multi-valued field open for the next entry, the search cleared', async () => {
    mountSelect({ freeEntry: true, multiSelect: true });
    open();
    type('Gamma');
    key('Enter');
    await settle();

    expect(sent).toEqual([entered('Gamma')]);
    expect(search()?.value).toBe('');
  });

  it('asks again for the options the server drops while the list stays open', async () => {
    mountSelect({ freeEntry: true, multiSelect: true });
    open();
    type('Gamma');
    key('Enter');
    act(() => sse.deliver(SELECT_ID, { value: [{ value: 'g', label: 'Gamma' }], optionsLoaded: false }));
    await settle();

    expect(sent).toEqual([entered('Gamma'), { command: 'loadOptions', args: {} }]);
    expect(search()).not.toBeNull();
  });

  it('enters the typed text when the list is left with Tab', async () => {
    mountSelect({ freeEntry: true });
    open();
    type('Gamma');
    key('Tab');
    await settle();

    expect(sent).toEqual([entered('Gamma')]);
  });

  it('enters the typed text on a press outside', async () => {
    mountSelect({ freeEntry: true });
    open();
    type('Gamma');
    act(() => {
      document.body.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, cancelable: true }));
    });
    await settle();

    expect(sent).toEqual([entered('Gamma')]);
    expect(search()).toBeNull();
  });

  it('drops the typed text on Escape', async () => {
    mountSelect({ freeEntry: true });
    open();
    type('Gamma');
    key('Escape');
    await settle();

    expect(sent).toEqual([]);
    expect(search()).toBeNull();
  });

  it('has a search field even where the field asks for none', () => {
    mountSelect({ freeEntry: true, noFilter: true });
    open();

    expect(search()).not.toBeNull();
  });
});

describe('a select without free entry', () => {
  it('neither offers nor enters a typed text', async () => {
    mountSelect({});
    open();
    type('Gamma');
    expect(rows()).toEqual([]);
    key('Enter');
    key('Tab');
    await settle();

    expect(sent).toEqual([]);
  });

  it('highlights the single option the search leaves', async () => {
    mountSelect({});
    open();
    type('Bet');
    expect(highlighted()).toBe('Beta');
    key('Enter');
    await settle();

    expect(sent).toEqual([{ command: CMD_VALUE_CHANGED, args: { value: ['b'] } }]);
  });
});
