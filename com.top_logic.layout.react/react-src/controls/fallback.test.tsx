import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// The inputs show the value a field inherits while it holds none of its own, each in the way its
// element allows, and the field frame offers to remove an entered value so that the inherited one
// applies again (ticket #30131).

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLFormField from './TLFormField';
import TLDatePicker from './TLDatePicker';
import TLSlider from './TLSlider';
import TLCheckbox from './TLCheckbox';
import ButtonPart from './button/ButtonPart';
import CheckboxPart from './form/CheckboxPart';
import FieldValuePart from './form/FieldValuePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'fallback';

/** The input in the field frame: an empty date field. */
const INPUT = { controlId: 'fallback-input', module: 'TLDatePicker', state: { value: null } };

/** The command of the field frame removing the entered value. */
const CMD_RESET = 'resetValue';

/** The commands sent by the mounted control. */
let sent: string[] = [];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  registerPart('Checkbox', CheckboxPart);
  registerPart('FieldValue', FieldValuePart);
  register('TLFormField', TLFormField);
  register('TLDatePicker', TLDatePicker);
  register('TLSlider', TLSlider);
  register('TLCheckbox', TLCheckbox);
});

afterEach(async () => {
  act(() => unmount(CONTROL_ID));
  document.getElementById(CONTROL_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts the given component with the given state and records the commands it sends. */
function mountControl(module: string, state: Record<string, unknown>): HTMLElement {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === CONTROL_ID) {
      sent.push(body.command);
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.id = CONTROL_ID;
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, module, state, 'w', '/ctx'));
  return element;
}

/** Waits until the command channel has handed all queued commands to fetch. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

describe('field frame', () => {
  it('offers to reset an entered value and sends the reset command', async () => {
    const root = mountControl('TLFormField', { label: 'Status', field: INPUT, resetLabel: 'Reset to inherited value' });

    const button = root.querySelector<HTMLButtonElement>('.tl-form-field__reset');
    expect(button).not.toBeNull();
    expect(button!.getAttribute('aria-label') ?? button!.textContent).toContain('Reset to inherited value');
    expect(root.querySelector('.tl-form-field__input--resettable')).not.toBeNull();

    act(() => button!.click());
    await settle();

    expect(sent).toEqual([CMD_RESET]);
  });

  it('offers no reset without an entered value', () => {
    const root = mountControl('TLFormField', { label: 'Status', field: INPUT });

    expect(root.querySelector('.tl-form-field__reset')).toBeNull();
    expect(root.querySelector('.tl-form-field__input--resettable')).toBeNull();
  });
});

describe('date picker', () => {
  it('shows the placeholder as hint while empty', () => {
    const root = mountControl('TLDatePicker', { value: null, placeholder: '01.06.2026' });

    const hint = root.querySelector('.tl-field-hint');
    expect(hint?.textContent).toBe('01.06.2026');
    expect(root.querySelector('input')!.getAttribute('aria-describedby')).toBe(hint!.id);
  });

  it('shows no hint for a value', () => {
    const root = mountControl('TLDatePicker', { value: '2026-06-01', placeholder: '01.06.2026' });

    expect(root.querySelector('.tl-field-hint')).toBeNull();
  });
});

describe('slider', () => {
  it('puts the handle of an empty field at the inherited value', () => {
    const root = mountControl('TLSlider', { value: null, fallbackValue: 40, placeholder: '40 %' });

    const slider = root.querySelector('.tl-slider')!;
    expect(slider.getAttribute('data-tl-state')).toBe('fallback');
    expect(root.querySelector<HTMLInputElement>('input[type="range"]')!.value).toBe('40');
    expect(root.querySelector('.tl-slider__text')!.textContent).toBe('40 %');
  });

  it('shows its own value over the inherited one', () => {
    const root = mountControl('TLSlider', { value: 70, valueLabel: '70 %', fallbackValue: 40, placeholder: '40 %' });

    expect(root.querySelector('.tl-slider')!.getAttribute('data-tl-state')).toBeNull();
    expect(root.querySelector<HTMLInputElement>('input[type="range"]')!.value).toBe('70');
    expect(root.querySelector('.tl-slider__text')!.textContent).toBe('70 %');
  });
});

describe('checkbox', () => {
  it('describes the inherited value while it holds none', () => {
    const root = mountControl('TLCheckbox', { value: null, triState: true, placeholder: 'Inherited: Yes' });

    const box = root.querySelector('input[type="checkbox"]')!;
    expect(box.getAttribute('aria-description')).toBe('Inherited: Yes');
  });

  it('describes nothing for a value of its own', () => {
    const root = mountControl('TLCheckbox', { value: true, triState: true, placeholder: 'Inherited: Yes' });

    const box = root.querySelector('input[type="checkbox"]')!;
    expect(box.getAttribute('aria-description')).toBeNull();
  });
});
