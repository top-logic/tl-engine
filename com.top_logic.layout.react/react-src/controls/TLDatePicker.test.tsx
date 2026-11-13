import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// A date typed segment by segment is held back until the field is left or Enter is pressed, a
// date chosen in the picker is sent at once, and an incomplete input never clears a stored value
// (ticket #30197).

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { CMD_COMMIT, CMD_VALUE_CHANGED, register, registerPart } from 'tl-react-bridge';
import TLDatePicker from './TLDatePicker';
import FieldValuePart from './form/FieldValuePart';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'date';

/** The argument of {@link CMD_VALUE_CHANGED} holding the ISO form of the value. */
const ARG_VALUE = 'value';

/** A command the mounted control sent, with its arguments. */
interface Sent {
  command: string;
  arguments: Record<string, unknown>;
}

/** The commands sent by the mounted control. */
let sent: Sent[] = [];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('FieldValue', FieldValuePart);
  registerPart('Button', ButtonPart);
  register('TLDatePicker', TLDatePicker);
});

afterEach(async () => {
  act(() => unmount(CONTROL_ID));
  document.getElementById(CONTROL_ID)?.remove();
  // A value still held back is sent on unmount, still to the stubbed fetch.
  await settle();
  vi.unstubAllGlobals();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts a date field with the given state and returns its input. */
function mountDate(state: Record<string, unknown>): HTMLInputElement {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === CONTROL_ID) {
      sent.push({ command: body.command, arguments: body.arguments ?? {} });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.id = CONTROL_ID;
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TLDatePicker', { sendValueOnBlur: true, ...state }, 'w', '/ctx'));
  return element.querySelector('input')!;
}

/** Waits until the command channel has handed all queued commands to fetch. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** Sets the value of the input as the browser does and reports the change to React. */
function change(input: HTMLInputElement, value: string): void {
  const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!;
  act(() => {
    setter.call(input, value);
    input.dispatchEvent(new Event('input', { bubbles: true }));
  });
}

/** Presses the given key in the input: a segment key changes the input to the given value. */
function press(input: HTMLInputElement, key: string, value?: string): void {
  act(() => {
    input.dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true }));
  });
  if (value !== undefined) {
    change(input, value);
  }
  act(() => {
    input.dispatchEvent(new KeyboardEvent('keyup', { key, bubbles: true }));
  });
}

/** Lets the input report incomplete segments, as the browser does for a date half typed. */
function markIncomplete(input: HTMLInputElement): void {
  Object.defineProperty(input, 'validity', { configurable: true, value: { badInput: true } });
}

function focus(input: HTMLInputElement): void {
  act(() => input.focus());
}

function blur(input: HTMLInputElement): void {
  act(() => input.blur());
}

function changedTo(value: string | null): Sent {
  return { command: CMD_VALUE_CHANGED, arguments: { [ARG_VALUE]: value } };
}

/** Types the date 01.03.2027 segment by segment, passing through the year 0002. */
function typeDate(input: HTMLInputElement): void {
  press(input, '0');
  press(input, '1');
  press(input, '0');
  press(input, '3');
  press(input, '2', '0002-03-01');
  press(input, '0', '0020-03-01');
  press(input, '2', '0202-03-01');
  press(input, '7', '2027-03-01');
}

describe('TLDatePicker typing', () => {
  it('sends nothing while a date is typed, and the date when the field is left', async () => {
    const input = mountDate({ value: null });
    focus(input);

    typeDate(input);
    await settle();

    expect(sent).toEqual([]);
    expect(input.value).toBe('2027-03-01');

    blur(input);
    await settle();

    expect(sent).toEqual([changedTo('2027-03-01')]);
  });

  it('sends the typed date on Enter, without commitEdits', async () => {
    const input = mountDate({ value: null });
    focus(input);

    typeDate(input);
    press(input, 'Enter');
    await settle();

    expect(sent).toEqual([changedTo('2027-03-01')]);
  });

  it('commits the typed date once on Enter and blur with commitEdits', async () => {
    const input = mountDate({ value: null, commitEdits: true });
    focus(input);

    typeDate(input);
    press(input, 'Enter');
    blur(input);
    await settle();

    expect(sent).toEqual([changedTo('2027-03-01'), { command: CMD_COMMIT, arguments: {} }]);
  });

  it('sends a date chosen without a key in a segment at once', async () => {
    const input = mountDate({ value: '2026-06-01', commitEdits: true });
    focus(input);

    change(input, '2026-06-15');
    await settle();

    expect(sent).toEqual([changedTo('2026-06-15'), { command: CMD_COMMIT, arguments: {} }]);

    blur(input);
    await settle();

    expect(sent).toHaveLength(2);
  });

  it('holds back the empty value of a segment being cleared', async () => {
    const input = mountDate({ value: '2026-06-01' });
    focus(input);

    markIncomplete(input);
    press(input, 'Backspace', '');
    await settle();

    expect(sent).toEqual([]);
  });

  it('sends nothing for an incomplete input when left and shows the stored value again', async () => {
    const input = mountDate({ value: '2026-06-01' });
    focus(input);

    markIncomplete(input);
    press(input, 'Backspace', '');
    blur(input);
    await settle();

    expect(sent).toEqual([]);
    expect(input.value).toBe('2026-06-01');
  });

  it('sends nothing for an incomplete input on Enter and shows the stored value again', async () => {
    const input = mountDate({ value: '2026-06-01', commitEdits: true });
    focus(input);

    press(input, '2', '0002-06-01');
    markIncomplete(input);
    press(input, 'Backspace', '');
    press(input, 'Enter');
    await settle();

    expect(sent).toEqual([]);
    expect(input.value).toBe('2026-06-01');

    blur(input);
    await settle();

    expect(sent).toEqual([]);
  });

  it('sends no value for a field whose segments were all cleared', async () => {
    const input = mountDate({ value: '2026-06-01' });
    focus(input);

    press(input, 'Backspace', '');
    await settle();

    expect(sent).toEqual([]);

    blur(input);
    await settle();

    expect(sent).toEqual([changedTo(null)]);
  });
});

/** The clear button of the mounted field, or `null` where it offers none. */
function clearButton(): HTMLButtonElement | null {
  return document.getElementById(CONTROL_ID)!.querySelector<HTMLButtonElement>('button');
}

describe('TLDatePicker clear button', () => {
  it('empties an optional field at once and hands the focus back to the input', async () => {
    const input = mountDate({ value: '2026-06-01', editable: true, mandatory: false });

    const button = clearButton();
    expect(button).not.toBeNull();
    act(() => button!.click());
    await settle();

    expect(sent).toEqual([changedTo(null)]);
    expect(input.value).toBe('');
    expect(document.activeElement).toBe(input);
    expect(clearButton()).toBeNull();
  });

  it('commits the cleared value with commitEdits', async () => {
    mountDate({ value: '2026-06-01', editable: true, commitEdits: true });

    act(() => clearButton()!.click());
    await settle();

    expect(sent).toEqual([changedTo(null), { command: CMD_COMMIT, arguments: {} }]);
  });

  it('is offered for a time of day', async () => {
    mountDate({ value: '14:30', inputType: 'time', editable: true });
    await settle();
    expect(clearButton()).not.toBeNull();
  });

  it('is offered for a date with a time of day', async () => {
    mountDate({ value: '2026-06-01T14:30', inputType: 'datetime-local', editable: true });
    await settle();
    expect(clearButton()).not.toBeNull();
  });

  it('is not offered for a mandatory field', async () => {
    mountDate({ value: '2026-06-01', editable: true, mandatory: true });
    await settle();
    expect(clearButton()).toBeNull();
  });

  it('is not offered for an empty field', async () => {
    mountDate({ value: null, editable: true });
    await settle();
    expect(clearButton()).toBeNull();
  });

  it('is not offered for a read-only field', async () => {
    mountDate({ value: '2026-06-01', displayValue: '01.06.2026', editable: false });
    await settle();
    expect(clearButton()).toBeNull();
  });

  it('is not offered for a disabled field', async () => {
    mountDate({ value: '2026-06-01', editable: false, disabled: true });
    await settle();
    expect(clearButton()).toBeNull();
  });
});
