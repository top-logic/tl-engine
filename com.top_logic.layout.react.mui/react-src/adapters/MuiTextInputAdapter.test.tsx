// The wire contract of TLTextInput, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CMD_SUBMIT, CMD_VALUE_CHANGED, VALUE_DEBOUNCE_MS, fieldLabel } from 'tl-react-bridge';
import type { TextInputStateJson } from 'tl-react-bridge';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import { CONTROL_ID, fakeDebounceTimers, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** The argument of {@link CMD_VALUE_CHANGED} and {@link CMD_SUBMIT} holding the text. */
const ARG_VALUE = 'value';

/** The command a typing field sends when it is left after an edit. */
const CMD_COMMIT = 'commit';

/** The translation key of the label of the clear button; the test server answers it with itself. */
const KEY_CLEAR = 'js.textInput.clear';

/** The ID of the input inside the control. */
const INPUT_ID = CONTROL_ID + '-input';

/** The ID of the form field around the input. */
const FIELD_ID = 'f1';

function mountText(state: Partial<TextInputStateJson>, options?: MountOptions) {
  return mountAdapter(MuiTextInputAdapter, state, options);
}

function changedTo(value: string) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: value }];
}

function input(): HTMLInputElement {
  return screen.getByRole('textbox') as HTMLInputElement;
}

afterEach(() => {
  cleanup();
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('TLTextInput as MUI TextField', () => {
  it('renders a small MUI text field without a label of its own, labelled by the form field', () => {
    const setInputId = vi.fn();
    mountText({ value: 'Hallo', placeholder: 'Name', cssClass: 'my-class' },
      { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, setInputId) });

    const box = input();
    expect(box.id).toBe(INPUT_ID);
    expect(box.value).toBe('Hallo');
    expect(box.placeholder).toBe('Name');
    expect(box.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(setInputId).toHaveBeenCalledWith(INPUT_ID);
    expect(box.closest('.MuiInputBase-sizeSmall')).not.toBeNull();
    expect(document.getElementById(CONTROL_ID)!.querySelector('label')).toBeNull();
    expect(box.closest('.MuiTextField-root')!.classList).toContain('my-class');
  });

  it('sends the typed text after the debounce, not before', async () => {
    fakeDebounceTimers();
    const sent = mountText({ value: '' });

    fireEvent.change(input(), { target: { value: 'abc' } });
    expect(input().value).toBe('abc');
    vi.advanceTimersByTime(VALUE_DEBOUNCE_MS - 1);
    await settle();
    expect(sent).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    await settle();
    expect(sent.mock.calls).toEqual([changedTo('abc')]);
  });

  it('waits the debounce the state names', async () => {
    fakeDebounceTimers();
    const sent = mountText({ value: '', debounceMs: 1000 });

    fireEvent.change(input(), { target: { value: 'abc' } });
    vi.advanceTimersByTime(999);
    await settle();
    expect(sent).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    await settle();
    expect(sent.mock.calls).toEqual([changedTo('abc')]);
  });

  it('sends a value held back at once when it is left, and only once', async () => {
    fakeDebounceTimers();
    const sent = mountText({ value: '' });

    fireEvent.change(input(), { target: { value: 'abc' } });
    fireEvent.blur(input());
    await settle();
    expect(sent.mock.calls).toEqual([changedTo('abc')]);

    vi.advanceTimersByTime(VALUE_DEBOUNCE_MS);
    await settle();
    expect(sent.mock.calls).toEqual([changedTo('abc')]);
  });

  it('holds the value back until it is left with sendValueOnBlur', async () => {
    fakeDebounceTimers();
    const sent = mountText({ value: '', sendValueOnBlur: true });

    fireEvent.change(input(), { target: { value: 'abc' } });
    vi.advanceTimersByTime(10 * VALUE_DEBOUNCE_MS);
    await settle();
    expect(sent).not.toHaveBeenCalled();

    fireEvent.blur(input());
    await settle();
    expect(sent.mock.calls).toEqual([changedTo('abc')]);
  });

  it('sends commit after the value when it is left after an edit with commitOnBlur', async () => {
    const sent = mountText({ value: '', commitOnBlur: true });

    fireEvent.change(input(), { target: { value: 'abc' } });
    fireEvent.blur(input());
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('abc'), [CMD_COMMIT, {}]]);
  });

  it('sends submit with the text on Enter with submitOnEnter', async () => {
    const sent = mountText({ value: 'abc', submitOnEnter: true });

    fireEvent.keyDown(input(), { key: 'Enter' });
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_SUBMIT, { [ARG_VALUE]: 'abc' }]]);
  });

  it('submits nothing on Enter without submitOnEnter', async () => {
    const sent = mountText({ value: 'abc' });

    fireEvent.keyDown(input(), { key: 'Enter' });
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('completes a typed web address when it is left', async () => {
    const sent = mountText({ value: '', inputType: 'url', sendValueOnBlur: true });

    fireEvent.change(input(), { target: { value: 'example.org' } });
    fireEvent.blur(input());
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('https://example.org')]);
  });

  it('renders a text area of the given rows for a multi-line field', () => {
    mountText({ value: 'a\nb', multiline: true, rows: 5 });

    const area = input();
    expect(area.tagName).toBe('TEXTAREA');
    expect(area.getAttribute('rows')).toBe('5');
  });

  it('empties the input at once with its clear button', async () => {
    const sent = mountText({ value: 'abc', clearable: true });

    await userEvent.click(await screen.findByRole('button', { name: KEY_CLEAR }));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('')]);
    expect(input().value).toBe('');
  });

  it('marks an invalid mandatory field', () => {
    mountText({ value: 'x', hasError: true, errorMessage: 'Falsch', mandatory: true });

    const box = input();
    expect(box.getAttribute('aria-invalid')).toBe('true');
    expect(box.getAttribute('aria-required')).toBe('true');
    expect(box.closest('.Mui-error')).not.toBeNull();
  });

  it('shows the value as text while read-only, an e-mail address as link', () => {
    mountText({ value: 'a@b.de', inputType: 'email', editable: false });

    expect(screen.queryByRole('textbox')).toBeNull();
    const link = screen.getByRole('link', { name: 'a@b.de' });
    expect(link.id).toBe(CONTROL_ID);
    expect(link.getAttribute('href')).toBe('mailto:a@b.de');
  });

  it('accepts no input and sends nothing while disabled', async () => {
    const sent = mountText({ value: 'abc', editable: false, disabled: true });

    const box = input();
    expect(box.disabled).toBe(true);
    await userEvent.setup({ pointerEventsCheck: 0 }).type(box, 'x');
    // Real time: longer than the debounce after which a typed value would be sent.
    await new Promise(resolve => setTimeout(resolve, VALUE_DEBOUNCE_MS + 50));
    await settle();

    expect(box.value).toBe('abc');
    expect(sent).not.toHaveBeenCalled();
  });

  it('renders nothing while hidden', () => {
    mountText({ value: 'abc', hidden: true });

    expect(screen.queryByRole('textbox')).toBeNull();
  });
});
