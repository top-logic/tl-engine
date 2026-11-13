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
import { React, register, registerPart, useFieldLabelProps, useTLState, TLChild, FormLayoutContext, TOOLTIP_ATTR } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLFormField from './TLFormField';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const FIELD_ID = 'field';

const INPUT_CONTROL_ID = 'input';

const INPUT_ID = 'input-element';

const ERROR = 'Not a valid address.';

const WARNING_1 = 'Unusual domain.';

const WARNING_2 = 'Address not verified.';

/** The explanation of the field, as the server sends it for the label and as help text. */
const EXPLANATION = 'The address replies are sent to.';

/** The name of the help and info icon for assistive technology, the fallback of its translation. */
const HELP_NAME = 'Help';

/** The component name of a form showing values only, around the field it holds. */
const READ_ONLY_FORM = 'TestReadOnlyForm';

/** A form layout showing values only. */
const READ_ONLY_LAYOUT = { readOnly: true, resolvedLabelPosition: 'side', insideForm: true } as const;

/** A form showing values only, around the control in its state's `field`. */
const TestReadOnlyForm: React.FC<TLCellProps> = () => {
  const state = useTLState<{ field: unknown }>();
  return (
    <FormLayoutContext.Provider value={READ_ONLY_LAYOUT}>
      <TLChild control={state.field as never} />
    </FormLayoutContext.Provider>
  );
};

/** An input naming itself by the label of the field around it, as the field controls do. */
const TestInput: React.FC<TLCellProps> = ({ controlId }) => {
  const labelProps = useFieldLabelProps(controlId, INPUT_ID);
  return <input id={INPUT_ID} {...labelProps} />;
};

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('TLFormField', TLFormField);
  register('TestInput', TestInput);
  register(READ_ONLY_FORM, TestReadOnlyForm);
  registerPart('Button', ButtonPart);
  // The field fetches the labels of its buttons; the server answers with none.
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
});

afterEach(async () => {
  act(() => unmount(FIELD_ID));
  document.getElementById(FIELD_ID)?.remove();
  await settle();
});

afterAll(async () => {
  // The bridge entry adds a tooltip host to the page. It goes while the test environment exists, so that the DOM
  // observer of the bridge does not see its removal by the teardown of the environment.
  document.body.replaceChildren();
  await settle();
});

/** Lets the DOM observer of the bridge and the translations the field asked for settle. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** Mounts a field labelled "E-Mail" around the test input, with the given state. */
async function mountField(state: Record<string, unknown>): Promise<void> {
  const element = document.createElement('div');
  element.id = FIELD_ID;
  document.body.appendChild(element);
  act(() => mount(FIELD_ID, 'TLFormField', {
    label: 'E-Mail',
    field: { controlId: INPUT_CONTROL_ID, module: 'TestInput', state: {} },
    ...state,
  }, 'w', '/ctx'));
  await settle();
}

/** Mounts a field labelled "E-Mail" around the test input in a form showing values only. */
async function mountReadOnlyField(state: Record<string, unknown>): Promise<void> {
  const element = document.createElement('div');
  element.id = FIELD_ID;
  document.body.appendChild(element);
  act(() => mount(FIELD_ID, READ_ONLY_FORM, {
    field: {
      controlId: 'readOnlyField', module: 'TLFormField', state: {
        label: 'E-Mail',
        field: { controlId: INPUT_CONTROL_ID, module: 'TestInput', state: {} },
        ...state,
      },
    },
  }, 'w', '/ctx'));
  await settle();
}

/** The info icon standing in for a hidden label, `null` without. */
function info(): HTMLElement | null {
  return root().querySelector<HTMLElement>('.tl-form-field__info > .tl-form-field__help');
}

function helpText(): HTMLElement | null {
  return root().querySelector<HTMLElement>('.tl-form-field__help-text');
}

/** The root element the field renders, inside the element it is mounted to. */
function root(): HTMLElement {
  return document.querySelector<HTMLElement>('.tl-form-field')!;
}

function input(): HTMLInputElement {
  return document.getElementById(INPUT_ID) as HTMLInputElement;
}

function marker(): HTMLButtonElement | null {
  return root().querySelector<HTMLButtonElement>('.tl-form-field__marker');
}

function popover(): HTMLElement | null {
  return document.querySelector<HTMLElement>('.tl-form-field__messages');
}

/** The texts of the elements describing the input, in the order of its aria-describedby. */
function descriptions(): string[] {
  const ids = (input().getAttribute('aria-describedby') ?? '').split(' ').filter(Boolean);
  return ids.map(id => document.getElementById(id)?.textContent ?? `<missing ${id}>`);
}

describe('a compact form field', () => {
  it('shows no marker and no message without error or warning', async () => {
    await mountField({ compact: true });

    expect(root().classList.contains('tl-form-field--compact')).toBe(true);
    expect(marker()).toBeNull();
    expect(root().querySelector('.tl-form-field__message')).toBeNull();
    expect(root().querySelector('.tl-form-field__label')).toBeNull();
  });

  it('names its input by a visually hidden label', async () => {
    await mountField({ compact: true, labelPosition: 'top' });

    expect(root().querySelector('.tl-form-field__label')).toBeNull();
    const label = root().querySelector('label')!;
    expect(label.classList.contains('tl-visually-hidden')).toBe(true);
    expect(input().getAttribute('aria-labelledby')).toBe(label.id);
  });

  it('shows an error as marker instead of a message line, still describing the input', async () => {
    await mountField({ compact: true, error: ERROR, warnings: [WARNING_1] });

    expect(root().querySelector('.tl-form-field__message')).toBeNull();
    const button = marker()!;
    expect(button.getAttribute('data-tl-state')).toBe('error');
    expect(button.getAttribute('aria-label')).toBe('Show error');
    expect(button.getAttribute('data-tooltip')).toBe(`text:${ERROR}`);
    expect(button.getAttribute('aria-expanded')).toBe('false');
    expect(popover()).toBeNull();
    // The error displaces the warnings.
    expect(descriptions()).toEqual([ERROR]);
    const message = root().querySelector('[role="alert"]')!;
    expect(message.classList.contains('tl-visually-hidden')).toBe(true);
  });

  it('opens the messages in a popover on a press of the marker, and closes it with Escape', async () => {
    await mountField({ compact: true, error: ERROR });

    act(() => marker()!.click());
    expect(marker()!.getAttribute('aria-expanded')).toBe('true');
    expect(popover()!.textContent).toBe(ERROR);
    expect(marker()!.getAttribute('aria-controls')).toBe(popover()!.id);
    expect(popover()!.classList.contains('tl-popover')).toBe(true);

    act(() => {
      document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true }));
    });
    expect(popover()).toBeNull();
    expect(marker()!.getAttribute('aria-expanded')).toBe('false');
  });

  it('closes the popover on a second press of the marker', async () => {
    await mountField({ compact: true, error: ERROR });

    act(() => marker()!.click());
    expect(popover()).not.toBeNull();
    act(() => marker()!.click());
    expect(popover()).toBeNull();
  });

  it('shows warnings as warning marker, each warning in the popover and in the description', async () => {
    await mountField({ compact: true, warnings: [WARNING_1, WARNING_2] });

    const button = marker()!;
    expect(button.getAttribute('data-tl-state')).toBe('warning');
    expect(button.getAttribute('aria-label')).toBe('Show warnings');
    expect(descriptions()).toEqual([WARNING_1, WARNING_2]);

    act(() => button.click());
    const lines = [...popover()!.querySelectorAll('.tl-form-field__message')].map(line => line.textContent);
    expect(lines).toEqual([WARNING_1, WARNING_2]);
  });

  it('shows no help, no required mark and no help button', async () => {
    await mountField({ compact: true, required: true, helpText: 'Some help', dirty: true });

    expect(root().querySelector('.tl-form-field__help')).toBeNull();
    expect(root().querySelector('.tl-form-field__help-text')).toBeNull();
    expect(root().querySelector('.tl-form-field__required')).toBeNull();
    expect(root().querySelector('.tl-form-field__dirty')).toBeNull();
  });
});

describe('a form field that is not compact', () => {
  it('shows its error as message line below the input, without marker', async () => {
    await mountField({ labelPosition: 'top', error: ERROR });

    expect(root().classList.contains('tl-form-field--compact')).toBe(false);
    expect(root().classList.contains('tl-form-field--top')).toBe(true);
    expect(marker()).toBeNull();
    const message = root().querySelector('.tl-form-field__message[role="alert"]')!;
    expect(message.textContent).toBe(ERROR);
    expect(descriptions()).toEqual([ERROR]);
    expect(root().querySelector('.tl-form-field__label-text')!.textContent).toBe('E-Mail');
  });

  it('shows each warning as message line', async () => {
    await mountField({ labelPosition: 'top', warnings: [WARNING_1, WARNING_2] });

    const lines = [...root().querySelectorAll('.tl-form-field__message[aria-live="polite"]')].map(l => l.textContent);
    expect(lines).toEqual([WARNING_1, WARNING_2]);
    expect(marker()).toBeNull();
  });
});

describe('the explanation of a form field', () => {
  it('shows the explanation, not the name of the help icon, as tooltip of the help icon at a visible label', async () => {
    await mountField({ helpText: EXPLANATION, tooltipText: EXPLANATION });

    const help = root().querySelector<HTMLElement>('.tl-form-field__label > .tl-form-field__help')!;
    expect(help.tagName).toBe('BUTTON');
    expect(help.getAttribute(TOOLTIP_ATTR)).toBe(`text:${EXPLANATION}`);
    expect(help.getAttribute('aria-label')).toBe(HELP_NAME);
    expect(info()).toBeNull();
  });

  it('shows the rich explanation the server holds as tooltip of the help icon', async () => {
    await mountField({ helpText: EXPLANATION, tooltipText: EXPLANATION, hasTooltip: true });

    const help = root().querySelector<HTMLElement>('.tl-form-field__label > .tl-form-field__help')!;
    expect(help.getAttribute(TOOLTIP_ATTR)).toBe('key:tooltip');
  });

  it('shows an info icon beside the input of a hidden label, disclosing the help text in edit mode', async () => {
    await mountField({ labelPosition: 'hidden', helpText: EXPLANATION, tooltipText: EXPLANATION });

    expect(root().querySelector('.tl-form-field__label')).toBeNull();
    expect(root().querySelector('.tl-form-field__input')!.classList.contains('tl-form-field__input--explained'))
      .toBe(true);
    const icon = info()!;
    expect(icon.tagName).toBe('BUTTON');
    expect(icon.getAttribute(TOOLTIP_ATTR)).toBe(`text:${EXPLANATION}`);
    expect(icon.getAttribute('aria-label')).toBe(HELP_NAME);
    expect(icon.getAttribute('aria-expanded')).toBe('false');
    expect(icon.getAttribute('aria-controls')).toBe(helpText()!.id);
    expect(helpText()!.hidden).toBe(true);

    act(() => icon.click());

    expect(helpText()!.hidden).toBe(false);
    expect(helpText()!.textContent).toBe(EXPLANATION);
    expect(info()!.getAttribute('aria-expanded')).toBe('true');
    expect(descriptions()).toEqual([EXPLANATION]);
  });

  it('shows the info icon of a hidden label in view mode as image carrying the explanation', async () => {
    await mountReadOnlyField({ labelPosition: 'hidden', helpText: EXPLANATION, tooltipText: EXPLANATION });

    const icon = info()!;
    expect(icon.tagName).toBe('SPAN');
    expect(icon.getAttribute('role')).toBe('img');
    expect(icon.getAttribute(TOOLTIP_ATTR)).toBe(`text:${EXPLANATION}`);
    expect(icon.getAttribute('aria-label')).toBe(HELP_NAME);
    expect(root().querySelector('button.tl-form-field__help')).toBeNull();
    expect(helpText()).toBeNull();
  });

  it('shows no info icon for a hidden label without explanation', async () => {
    await mountField({ labelPosition: 'hidden' });

    expect(root().querySelector('.tl-form-field__info')).toBeNull();
    expect(root().querySelector('.tl-form-field__help')).toBeNull();
    expect(root().querySelector('.tl-form-field__input')!.classList.contains('tl-form-field__input--explained'))
      .toBe(false);
  });

  it('shows no info icon in a compact field', async () => {
    await mountField({ compact: true, helpText: EXPLANATION, tooltipText: EXPLANATION });

    expect(root().querySelector('.tl-form-field__info')).toBeNull();
  });
});
