import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// A person who types with an input method (for example for Japanese) ends a composition with Enter.
// That Enter is the input method's: it triggers nothing - no commit, no submit, no pick in the list of
// suggestions, no key binding around the field. The next Enter does (ticket #30487).

/** The listeners of the server updates, by control ID, so that a test can send an answer of the server. */
const { serverListeners } = vi.hoisted(() => ({
  serverListeners: new Map<string, (patch: Record<string, unknown>) => void>(),
}));

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: (controlId: string, listener: (patch: Record<string, unknown>) => void) => {
    serverListeners.set(controlId, listener);
  },
  unsubscribe: (controlId: string) => {
    serverListeners.delete(controlId);
  },
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { CMD_COMMIT, CMD_SUBMIT, CMD_VALUE_CHANGED, CMD_SUGGEST, register, registerPart } from 'tl-react-bridge';
import { addBinding, pageScope } from '../bridge/keyboard-dispatcher';
import TLTextInput from './TLTextInput';
import TextInputPart from './form/TextInputPart';
import ListboxPart from './listbox/ListboxPart';
import FieldValuePart from './form/FieldValuePart';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'field';

/** The text composed with the input method. */
const COMPOSED = 'にほん';

/** A suggestion replacing the started term "@K". */
const KITCHEN = { label: 'Kitchen', text: '@"Kitchen"', from: 0, to: 2 };

/** A command the mounted control sent, with its arguments. */
interface Sent {
  command: string;
  arguments: Record<string, unknown>;
}

/** The commands sent by the mounted control. */
let sent: Sent[] = [];

/** Removes the key binding a test added, if any. */
let removeBinding: (() => void) | null = null;

/** How a browser reports the Enter that ends a composition. */
interface CompositionEnter {
  /** Whether the composition ends before the keydown of the Enter is reported. */
  endsFirst: boolean;

  /** The key code of the keydown; 229 is the key processed by an input method. */
  keyCode: number;

  /** The `isComposing` of the keydown. */
  isComposing: boolean;
}

/** The forms of the Enter that ends a composition. */
const COMPOSITION_ENTERS: [string, CompositionEnter][] = [
  ['during the composition, as Chromium and Firefox report it', { endsFirst: false, keyCode: 229, isComposing: true }],
  ['during the composition, with the key code of Enter', { endsFirst: false, keyCode: 13, isComposing: true }],
  ['after the end of the composition, as Safari reports it', { endsFirst: true, keyCode: 229, isComposing: false }],
];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('TextInput', TextInputPart);
  registerPart('Listbox', ListboxPart);
  registerPart('FieldValue', FieldValuePart);
  registerPart('Button', ButtonPart);
  register('TLTextInput', TLTextInput);
});

afterEach(async () => {
  removeBinding?.();
  removeBinding = null;
  act(() => unmount(CONTROL_ID));
  findMountPoint(CONTROL_ID)?.remove();
  await settle();
  vi.unstubAllGlobals();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts a single-line text field with the given state, focuses it and returns its input. */
function mountText(state: Record<string, unknown>): HTMLInputElement {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === CONTROL_ID) {
      sent.push({ command: body.command, arguments: body.arguments ?? {} });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, CONTROL_ID);
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TLTextInput', { editable: true, value: '', ...state }, 'w', '/ctx'));
  const input = element.querySelector('input')!;
  act(() => input.focus());
  return input;
}

/** Waits until the command channel has handed all queued commands to fetch. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** The commands sent, without the value updates. */
function actions(): Sent[] {
  return sent.filter(command => command.command !== CMD_VALUE_CHANGED);
}

/** Starts a composition in the input and shows the given text, composed so far, as the browser does. */
function compose(input: HTMLInputElement, text: string): void {
  const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!;
  act(() => {
    input.dispatchEvent(new CompositionEvent('compositionstart', { bubbles: true, data: '' }));
    setter.call(input, text);
    input.dispatchEvent(new Event('input', { bubbles: true }));
  });
}

/** Ends the composition in the input with Enter, in the form and the order the given browser reports it. */
function endWithEnter(input: HTMLInputElement, enter: CompositionEnter): void {
  const end = () => input.dispatchEvent(new CompositionEvent('compositionend', { bubbles: true, data: input.value }));
  act(() => {
    if (enter.endsFirst) {
      end();
    }
    input.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'Enter', code: 'Enter', keyCode: enter.keyCode, isComposing: enter.isComposing,
      bubbles: true, cancelable: true,
    }));
    if (!enter.endsFirst) {
      end();
    }
  });
}

/** Presses Enter in the input without an input method. */
function pressEnter(input: HTMLInputElement): void {
  act(() => {
    input.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'Enter', code: 'Enter', keyCode: 13, bubbles: true, cancelable: true,
    }));
  });
}

describe.each(COMPOSITION_ENTERS)('the Enter that ends a composition %s', (_form, enter) => {
  it('neither commits nor submits the text; the next Enter does', async () => {
    const input = mountText({ commitEdits: true, submitOnEnter: true });

    compose(input, COMPOSED);
    endWithEnter(input, enter);
    await settle();

    expect(actions()).toEqual([]);
    expect(input.value).toBe(COMPOSED);

    pressEnter(input);
    await settle();

    expect(actions()).toEqual([
      { command: CMD_COMMIT, arguments: {} },
      { command: CMD_SUBMIT, arguments: { value: COMPOSED } },
    ]);
  });

  it('picks no suggestion; the next Enter does', async () => {
    const input = mountText({ offersSuggestions: true });

    compose(input, '@K');
    // The answer of the server to the question the typing asked.
    act(() => serverListeners.get(CONTROL_ID)!({ suggestions: [KITCHEN], suggestionsText: '@K', suggestionsCaret: 2 }));
    const option = document.querySelector<HTMLElement>('[role="option"]')!;
    // The pointer rests on the entry, which makes it the active one.
    act(() => {
      option.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
    });
    expect(option.getAttribute('aria-selected')).toBe('true');

    endWithEnter(input, enter);
    await settle();

    expect(input.value).toBe('@K');
    expect(sent.filter(command => command.command === CMD_SUGGEST).map(command => command.arguments.text))
      .not.toContain(KITCHEN.text);

    pressEnter(input);

    expect(input.value).toBe(KITCHEN.text);
  });

  it('runs no key binding around the field; the next Enter does', () => {
    const input = mountText({});
    // The binding of a command with key="ENTER" on the page around the field.
    const command = vi.fn(() => true);
    removeBinding = addBinding(pageScope(), 'Enter', command);

    compose(input, COMPOSED);
    endWithEnter(input, enter);

    expect(command).not.toHaveBeenCalled();

    pressEnter(input);

    expect(command).toHaveBeenCalledTimes(1);
  });
});
