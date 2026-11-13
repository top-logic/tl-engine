import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// A text - in a single line or in a text area -, a password and a number are emptied by deleting
// what is typed: their inputs offer the clear button only where the field asks for it
// (state.clearable).

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { CMD_VALUE_CHANGED, register, registerPart } from 'tl-react-bridge';
import TLTextInput from './TLTextInput';
import TLNumberInput from './TLNumberInput';
import TLPasswordInput from './TLPasswordInput';
import ButtonPart from './button/ButtonPart';
import TextInputPart from './form/TextInputPart';
import FieldValuePart from './form/FieldValuePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'field';

/** The component name of the text field. */
const TEXT = 'TLTextInput';

/** The component name of the number field. */
const NUMBER = 'TLNumberInput';

/** The component name of the password field. */
const PASSWORD = 'TLPasswordInput';

/** The argument of {@link CMD_VALUE_CHANGED} holding the value. */
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
  registerPart('Button', ButtonPart);
  registerPart('TextInput', TextInputPart);
  registerPart('FieldValue', FieldValuePart);
  register(TEXT, TLTextInput);
  register(NUMBER, TLNumberInput);
  register(PASSWORD, TLPasswordInput);
});

afterEach(async () => {
  act(() => unmount(CONTROL_ID));
  document.getElementById(CONTROL_ID)?.remove();
  await settle();
  vi.unstubAllGlobals();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts the given field component with the given state and returns its input or text area. */
function mountField(component: string, state: Record<string, unknown>): HTMLInputElement | HTMLTextAreaElement {
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
  act(() => mount(CONTROL_ID, component, { editable: true, ...state }, 'w', '/ctx'));
  return element.querySelector<HTMLInputElement | HTMLTextAreaElement>('input, textarea')!;
}


/** Waits until the command channel has handed all queued commands to fetch. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** Mounts the given field and checks that it shows its input without a clear button. */
async function expectNoClearButton(component: string, state: Record<string, unknown>): Promise<void> {
  const input = mountField(component, state);
  await settle();
  expect(input.isConnected).toBe(true);
  expect(clearButton()).toBeNull();
}

/** The clear button of the mounted field, or `null` where it offers none. */
function clearButton(): HTMLButtonElement | null {
  return document.getElementById(CONTROL_ID)!.querySelector<HTMLButtonElement>('button');
}

describe('the clear button of a number field', () => {
  it('sends no value at once and hands the focus back to the input', async () => {
    const input = mountField(NUMBER, { value: '12,50', clearable: true, sendValueOnBlur: true });

    act(() => clearButton()!.click());
    await settle();

    expect(sent).toEqual([{ command: CMD_VALUE_CHANGED, arguments: { [ARG_VALUE]: null } }]);
    expect(input.value).toBe('');
    expect(document.activeElement).toBe(input);
    expect(clearButton()).toBeNull();
  });

  it('is not offered where the field does not ask for it, also when it is optional', async () => {
    await expectNoClearButton(NUMBER, { value: '12,50', mandatory: false });
  });

  it('is not offered for an empty field', async () => {
    await expectNoClearButton(NUMBER, { value: '', clearable: true });
  });

  it('is not offered for a disabled field', async () => {
    await expectNoClearButton(NUMBER, { value: '12,50', clearable: true, editable: false, disabled: true });
  });

  it('is not offered for a read-only field', async () => {
    mountField(NUMBER, { value: '12,50', clearable: true, editable: false });
    await settle();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('12,50');
    expect(clearButton()).toBeNull();
  });
});

describe('the clear button of a text field', () => {
  it('sends the empty text at once', async () => {
    const input = mountField(TEXT, { value: 'abc', clearable: true });

    act(() => clearButton()!.click());
    await settle();

    expect(sent).toEqual([{ command: CMD_VALUE_CHANGED, arguments: { [ARG_VALUE]: '' } }]);
    expect(input.value).toBe('');
  });

  it('is not offered where the field does not ask for it, also when it is optional', async () => {
    await expectNoClearButton(TEXT, { value: 'abc', mandatory: false });
  });

  it('is not offered for an empty field', async () => {
    await expectNoClearButton(TEXT, { value: '', clearable: true });
  });

  it('is not offered for a disabled field', async () => {
    await expectNoClearButton(TEXT, { value: 'abc', clearable: true, editable: false, disabled: true });
  });
});

describe('the clear button of a text area', () => {
  it('sends the empty text at once and hands the focus back to the text area', async () => {
    const area = mountField(TEXT, { value: 'eins\nzwei', multiline: true, rows: 3, clearable: true });
    expect(area.tagName).toBe('TEXTAREA');

    act(() => clearButton()!.click());
    await settle();

    expect(sent).toEqual([{ command: CMD_VALUE_CHANGED, arguments: { [ARG_VALUE]: '' } }]);
    expect(area.value).toBe('');
    expect(document.activeElement).toBe(area);
    expect(clearButton()).toBeNull();
  });

  it('is not offered where the field does not ask for it', async () => {
    await expectNoClearButton(TEXT, { value: 'eins', multiline: true });
  });

  it('is not offered for an empty field', async () => {
    await expectNoClearButton(TEXT, { value: '', multiline: true, clearable: true });
  });

  it('is not offered for a read-only field', async () => {
    mountField(TEXT, { value: 'eins', multiline: true, clearable: true, editable: false });
    await settle();
    expect(document.getElementById(CONTROL_ID)!.textContent).toContain('eins');
    expect(clearButton()).toBeNull();
  });
});

describe('the clear button of a password field', () => {
  it('sends the empty password at once and hands the focus back to the input', async () => {
    const input = mountField(PASSWORD, { value: 'geheim', clearable: true });
    expect(input.type).toBe('password');

    act(() => clearButton()!.click());
    await settle();

    expect(sent).toEqual([{ command: CMD_VALUE_CHANGED, arguments: { [ARG_VALUE]: '' } }]);
    expect(input.value).toBe('');
    expect(document.activeElement).toBe(input);
    expect(clearButton()).toBeNull();
  });

  it('is not offered where the field does not ask for it', async () => {
    await expectNoClearButton(PASSWORD, { value: 'geheim' });
  });

  it('is not offered for an empty field', async () => {
    await expectNoClearButton(PASSWORD, { value: '', clearable: true });
  });

  it('is not offered for a disabled field', async () => {
    await expectNoClearButton(PASSWORD, { value: 'geheim', clearable: true, editable: false, disabled: true });
  });

  it('is not offered for a read-only field', async () => {
    mountField(PASSWORD, { value: 'geheim', clearable: true, editable: false });
    await settle();
    expect(document.getElementById(CONTROL_ID)!.querySelector('input')).toBeNull();
    expect(clearButton()).toBeNull();
  });
});
