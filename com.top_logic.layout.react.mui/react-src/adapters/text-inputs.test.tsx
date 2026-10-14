// The text boxes drawn by the part TextInput with Material UI: the text, password and number fields,
// the search and the filter name of a table, the search of its column selection, the filter of a
// dropdown and of the icon popup. One replacement of the part reaches all of them; the wire
// contract of the fields is unchanged.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {
  CMD_COMMIT, CMD_SUBMIT, CMD_VALUE_CHANGED, VALUE_DEBOUNCE_MS, TOOLTIP_ATTR, fieldLabel, replacePart,
} from 'tl-react-bridge';
import type { TextInputStateJson } from 'tl-react-bridge';
import TLTextInput from '../../../com.top_logic.layout.react/react-src/controls/TLTextInput';
import TLPasswordInput from '../../../com.top_logic.layout.react/react-src/controls/TLPasswordInput';
import TLNumberInput from '../../../com.top_logic.layout.react/react-src/controls/TLNumberInput';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLColumnSelect from '../../../com.top_logic.layout.react/react-src/controls/TLColumnSelect';
import TLDropdownSelect from '../../../com.top_logic.layout.react/react-src/controls/TLDropdownSelect';
import IconSelectPopup from '../../../com.top_logic.layout.react/react-src/controls/icon/IconSelectPopup';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiFieldValueAdapter from './MuiFieldValueAdapter';
import { CONTROL_ID, childControl, fakeDebounceTimers, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';
import MuiChipAdapter from './MuiChipAdapter';
import MuiProgressBarAdapter from './MuiProgressBarAdapter';
import MuiSpinnerAdapter from './MuiSpinnerAdapter';

replacePart('TextInput', MuiTextInputAdapter);
replacePart('FieldValue', MuiFieldValueAdapter);
// The fields, the table and the popups look up their buttons, checkboxes and toggles as well.
replacePart('Button', MuiButtonAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
// The table and the dropdown draw chips as well.
replacePart('Chip', MuiChipAdapter);
// The dropdown shows a bar while its options are being loaded, the icon popup a spinner.
replacePart('ProgressBar', MuiProgressBarAdapter);
replacePart('Spinner', MuiSpinnerAdapter);

/** The argument of {@link CMD_VALUE_CHANGED} and {@link CMD_SUBMIT} holding the text. */
const ARG_VALUE = 'value';

/** The translation key of the label of the clear button; the test server answers it with itself. */
const KEY_CLEAR = 'js.textInput.clear';

/** The translation key of the label of the link opening what the field holds. */
const KEY_OPEN = 'js.textInput.open';

/** The ID of the input inside the control. */
const INPUT_ID = CONTROL_ID + '-input';

/** The ID of the form field around the input. */
const FIELD_ID = 'f1';

/** A resize observer that never reports: jsdom lays nothing out. */
class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

function changedTo(value: string | null) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: value }];
}

function input(): HTMLInputElement {
  return screen.getByRole('textbox') as HTMLInputElement;
}

function inputById(): HTMLInputElement {
  return document.getElementById(INPUT_ID) as HTMLInputElement;
}

/** Asserts that the given input is the one of an MUI text field and not the box of TopLogic. */
function expectMuiBox(box: HTMLElement) {
  expect(box.closest('.MuiTextField-root')).not.toBeNull();
  expect(box.classList).not.toContain('tl-field');
}

afterEach(() => {
  cleanup();
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('a text field as MUI TextField', () => {
  function mountText(state: Partial<TextInputStateJson>, options?: MountOptions) {
    return mountAdapter(TLTextInput, state, options);
  }

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
    expectMuiBox(box);
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

  it('sends commit after the value when it is left after an edit with commitEdits', async () => {
    const sent = mountText({ value: '', commitEdits: true });

    fireEvent.change(input(), { target: { value: 'abc' } });
    fireEvent.blur(input());
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('abc'), [CMD_COMMIT, {}]]);
  });

  it('sends no commit when it is left without an edit', async () => {
    const sent = mountText({ value: 'abc', commitEdits: true });

    fireEvent.blur(input());
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('sends no commit after an edit without commitEdits', async () => {
    const sent = mountText({ value: '' });

    fireEvent.change(input(), { target: { value: 'abc' } });
    fireEvent.keyDown(input(), { key: 'Enter' });
    fireEvent.blur(input());
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('abc')]);
  });

  it('sends commit after the value on Enter after an edit, and lets Enter pass on', async () => {
    const sent = mountText({ value: '', commitEdits: true });

    fireEvent.change(input(), { target: { value: 'abc' } });
    const passedOn = fireEvent.keyDown(input(), { key: 'Enter' });
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('abc'), [CMD_COMMIT, {}]]);
    expect(passedOn).toBe(true);
  });

  it('sends no second commit when it is left right after Enter', async () => {
    const sent = mountText({ value: '', commitEdits: true });

    fireEvent.change(input(), { target: { value: 'abc' } });
    fireEvent.keyDown(input(), { key: 'Enter' });
    fireEvent.blur(input());
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('abc'), [CMD_COMMIT, {}]]);
  });

  it('commits each edit once: Enter, a further edit, leaving the field', async () => {
    const sent = mountText({ value: '', commitEdits: true });

    fireEvent.change(input(), { target: { value: 'abc' } });
    fireEvent.keyDown(input(), { key: 'Enter' });
    fireEvent.change(input(), { target: { value: 'abcd' } });
    fireEvent.blur(input());
    await settle();

    expect(sent.mock.calls).toEqual([
      changedTo('abc'), [CMD_COMMIT, {}], changedTo('abcd'), [CMD_COMMIT, {}],
    ]);
  });

  it('sends no commit on Enter without an edit, but still the submit', async () => {
    const sent = mountText({ value: 'abc', commitEdits: true, submitOnEnter: true });

    fireEvent.keyDown(input(), { key: 'Enter' });
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_SUBMIT, { [ARG_VALUE]: 'abc' }]]);
  });

  it('sends the commit before the submit on Enter after an edit', async () => {
    const sent = mountText({ value: '', commitEdits: true, submitOnEnter: true });

    fireEvent.change(input(), { target: { value: 'abc' } });
    const passedOn = fireEvent.keyDown(input(), { key: 'Enter' });
    await settle();

    expect(sent.mock.calls).toEqual([
      changedTo('abc'), [CMD_COMMIT, {}], [CMD_SUBMIT, { [ARG_VALUE]: 'abc' }],
    ]);
    expect(passedOn).toBe(false);
  });

  it('commits nothing on Enter with a modifier', async () => {
    const sent = mountText({ value: '', commitEdits: true });

    fireEvent.change(input(), { target: { value: 'abc' } });
    fireEvent.keyDown(input(), { key: 'Enter', shiftKey: true });
    await settle();

    expect(sent.mock.calls).toEqual([]);
  });

  it('commits an edit of a text area only when it is left, Enter being text there', async () => {
    const sent = mountText({ value: '', commitEdits: true, multiline: true });

    fireEvent.change(input(), { target: { value: 'a' } });
    fireEvent.keyDown(input(), { key: 'Enter' });
    await settle();
    expect(sent.mock.calls.filter(call => call[0] === CMD_COMMIT)).toEqual([]);

    fireEvent.blur(input());
    await settle();
    expect(sent.mock.calls).toEqual([changedTo('a'), [CMD_COMMIT, {}]]);
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
    expect(document.activeElement).toBe(input());
    expect(screen.queryByRole('button', { name: KEY_CLEAR })).toBeNull();
  });

  it('draws the clear button as action inside the frame of the MUI box', async () => {
    mountText({ value: 'abc', clearable: true, icon: 'css:my-icon' });

    const clear = await screen.findByRole('button', { name: KEY_CLEAR });
    expect(clear.closest('.MuiInputAdornment-positionEnd')).not.toBeNull();
    expect(clear.closest('.MuiInputBase-root')).toBe(input().closest('.MuiInputBase-root'));
    expect(input().closest('.MuiInputBase-root')!.querySelector('.MuiInputAdornment-positionStart .my-icon'))
      .not.toBeNull();
  });

  it('offers no clear button while the input is empty or disabled', async () => {
    mountText({ value: '', clearable: true });
    await settle();
    expect(screen.queryByRole('button')).toBeNull();
    cleanup();

    mountText({ value: 'abc', clearable: true, disabled: true });
    await settle();
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('offers a link opening a web address, in place of which the clear button stands', async () => {
    mountText({ value: 'https://example.org', inputType: 'url' });

    const open = await screen.findByRole('link', { name: KEY_OPEN });
    expect(open.getAttribute('href')).toBe('https://example.org');
    expect(open.closest('.MuiInputAdornment-positionEnd')).not.toBeNull();
    cleanup();

    mountText({ value: 'https://example.org', inputType: 'url', clearable: true });
    await screen.findByRole('button', { name: KEY_CLEAR });
    expect(screen.queryByRole('link')).toBeNull();
  });

  it('marks an invalid mandatory field', () => {
    mountText({ value: 'x', hasError: true, errorMessage: 'Falsch', mandatory: true });

    const box = input();
    expect(box.getAttribute('aria-invalid')).toBe('true');
    expect(box.getAttribute('aria-required')).toBe('true');
    expect(box.closest('.Mui-error')).not.toBeNull();
    expect(box.getAttribute(TOOLTIP_ATTR)).toBe('text:Falsch');
  });

  it('draws a warning in the warning color, without marking the value invalid', () => {
    mountText({ value: 'x', hasWarnings: true });

    const box = input();
    expect(box.hasAttribute('aria-invalid')).toBe(false);
    expect(box.closest('.MuiInputBase-colorWarning')).not.toBeNull();
  });

  it('shows the value as text while read-only, an e-mail address as link', () => {
    mountText({ value: 'a@b.de', inputType: 'email', editable: false });

    expect(screen.queryByRole('textbox')).toBeNull();
    const link = screen.getByRole('link', { name: 'a@b.de' });
    expect(link.id).toBe(CONTROL_ID);
    expect(link.getAttribute('href')).toBe('mailto:a@b.de');
    expect(link.getAttribute('target')).toBe('_blank');
    expect(link.classList).toContain('MuiLink-root');
    expect(link.classList).not.toContain('tl-field-value');
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

describe('a password field as MUI TextField', () => {
  it('renders a password input without a button revealing it', () => {
    mountAdapter(TLPasswordInput, { value: 'geheim' });

    const box = inputById();
    expect(box.type).toBe('password');
    expectMuiBox(box);
    expect(box.value).toBe('geheim');
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('sends the typed password after the debounce', async () => {
    fakeDebounceTimers();
    const sent = mountAdapter(TLPasswordInput, { value: '' });

    fireEvent.change(inputById(), { target: { value: 'pw' } });
    vi.advanceTimersByTime(VALUE_DEBOUNCE_MS - 1);
    await settle();
    expect(sent).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    await settle();
    expect(sent.mock.calls).toEqual([changedTo('pw')]);
  });

  it('shows a mask instead of the password while read-only', () => {
    mountAdapter(TLPasswordInput, { value: 'geheim', editable: false });

    expect(inputById()).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('••••••••');
  });

  it('marks an invalid password, and is labelled by the form field', () => {
    const setInputId = vi.fn();
    mountAdapter(TLPasswordInput, { value: 'x', hasError: true, mandatory: true },
      { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, setInputId) });

    const box = inputById();
    expect(box.getAttribute('aria-invalid')).toBe('true');
    expect(box.getAttribute('aria-required')).toBe('true');
    expect(box.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(setInputId).toHaveBeenCalledWith(INPUT_ID);
  });
});

describe('a number field as MUI TextField', () => {
  it('renders a text input with the on-screen keyboard of the state', () => {
    mountAdapter(TLNumberInput, { value: '12,50', inputMode: 'decimal' });

    const box = inputById();
    expect(box.type).toBe('text');
    expectMuiBox(box);
    expect(box.getAttribute('inputmode')).toBe('decimal');
    expect(box.value).toBe('12,50');
  });

  it('sends the text as typed, and an empty text as null, when it is left', async () => {
    const sent = mountAdapter(TLNumberInput, { value: '1', sendValueOnBlur: true });

    fireEvent.change(inputById(), { target: { value: 'foo' } });
    fireEvent.blur(inputById());
    await settle();
    fireEvent.change(inputById(), { target: { value: '' } });
    fireEvent.blur(inputById());
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('foo'), changedTo(null)]);
  });

  it('sends submit with the text on Enter with submitOnEnter', async () => {
    const sent = mountAdapter(TLNumberInput, { value: '42', submitOnEnter: true });

    fireEvent.keyDown(inputById(), { key: 'Enter' });
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_SUBMIT, { [ARG_VALUE]: '42' }]]);
  });

  it('commits an edit on Enter after the value held back, then submits it', async () => {
    const sent = mountAdapter(TLNumberInput,
      { value: '1', sendValueOnBlur: true, commitEdits: true, submitOnEnter: true });

    fireEvent.change(inputById(), { target: { value: '42' } });
    fireEvent.keyDown(inputById(), { key: 'Enter' });
    fireEvent.blur(inputById());
    await settle();

    expect(sent.mock.calls).toEqual([
      changedTo('42'), [CMD_COMMIT, {}], [CMD_SUBMIT, { [ARG_VALUE]: '42' }],
    ]);
  });

  it('shows the text while read-only and sends nothing', async () => {
    const sent = mountAdapter(TLNumberInput, { value: '1.234,5', editable: false });
    await settle();

    expect(inputById()).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('1.234,5');
    expect(sent).not.toHaveBeenCalled();
  });

  it('marks an invalid mandatory number', () => {
    mountAdapter(TLNumberInput, { value: 'x', hasError: true, errorMessage: 'Keine Zahl', mandatory: true });

    const box = inputById();
    expect(box.getAttribute('aria-invalid')).toBe('true');
    expect(box.getAttribute('aria-required')).toBe('true');
    expect(box.getAttribute(TOOLTIP_ATTR)).toBe('text:Keine Zahl');
  });

  it('renders nothing while hidden', () => {
    mountAdapter(TLNumberInput, { value: '1', hidden: true });

    expect(inputById()).toBeNull();
    expect(document.getElementById(CONTROL_ID)).toBeNull();
  });
});

describe('the search and the filter name of a table as MUI text fields', () => {
  /** The labels of the boxes and buttons of the filter bar: the keys, translated to themselves. */
  const LABEL_SEARCH = 'js.table.searchHint';
  const LABEL_FILTER_NAME = 'js.table.filterName';
  const LABEL_SAVE_FILTER = 'js.table.saveFilter';

  /** The pause after which the table sends a typed search term. */
  const SEARCH_DEBOUNCE_MS = 300;

  function mountTable(state: Record<string, unknown> = {}) {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      rows: [{ id: 'r0', index: 0, selected: false, cells: { name: childControl('c0', 'Zeile 0') } }],
      totalRowCount: 1,
      filterBar: true,
      ...state,
    });
  }

  it('draws the search with the magnifier and sends the term after the pause', async () => {
    fakeDebounceTimers();
    const sent = mountTable({ search: 'al' });
    await settle();

    const search = screen.getByRole('searchbox', { name: LABEL_SEARCH }) as HTMLInputElement;
    expectMuiBox(search);
    expect(search.value).toBe('al');
    expect(search.closest('.tlTableView__search')).not.toBeNull();
    expect(search.closest('.MuiInputBase-root')!.querySelector('.MuiInputAdornment-positionStart i'))
      .not.toBeNull();
    expect(search.getAttribute(TOOLTIP_ATTR)).toBe('text:' + LABEL_SEARCH);

    fireEvent.change(search, { target: { value: 'alpha' } });
    expect(search.value).toBe('alpha');
    vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS - 1);
    await settle();
    expect(sent).not.toHaveBeenCalledWith('search', expect.anything());

    vi.advanceTimersByTime(1);
    await settle();
    expect(sent).toHaveBeenCalledWith('search', { term: 'alpha' });
  });

  it('sends the term at once on Enter', async () => {
    const sent = mountTable();
    await settle();

    const search = screen.getByRole('searchbox', { name: LABEL_SEARCH });
    fireEvent.change(search, { target: { value: 'beta' } });
    const passedOn = fireEvent.keyDown(search, { key: 'Enter' });
    await settle();

    expect(sent).toHaveBeenCalledWith('search', { term: 'beta' });
    expect(passedOn).toBe(false);
  });

  it('names a filter to save in a focused box, saved on Enter and given up on Escape', async () => {
    const sent = mountTable({ filterSaving: true });
    await settle();

    await userEvent.click(screen.getByRole('button', { name: LABEL_SAVE_FILTER }));
    const name = screen.getByRole('textbox', { name: LABEL_FILTER_NAME }) as HTMLInputElement;
    expectMuiBox(name);
    expect(document.activeElement).toBe(name);
    expect(name.closest('.tlTableView__saveInput')).not.toBeNull();

    // An empty name saves nothing.
    fireEvent.keyDown(name, { key: 'Enter' });
    await settle();
    expect(sent).not.toHaveBeenCalledWith('saveNamedFilter', expect.anything());

    await userEvent.type(name, 'Meins');
    fireEvent.keyDown(name, { key: 'Enter' });
    await settle();
    expect(sent).toHaveBeenCalledWith('saveNamedFilter', { filterName: 'Meins' });
    expect(screen.queryByRole('textbox', { name: LABEL_FILTER_NAME })).toBeNull();

    await userEvent.click(screen.getByRole('button', { name: LABEL_SAVE_FILTER }));
    const again = screen.getByRole('textbox', { name: LABEL_FILTER_NAME });
    const passedOn = fireEvent.keyDown(again, { key: 'Escape' });
    expect(passedOn).toBe(false);
    expect(screen.queryByRole('textbox', { name: LABEL_FILTER_NAME })).toBeNull();
  });

  it('has no search without the filter bar', async () => {
    mountTable({ filterBar: false });
    await settle();

    expect(screen.queryByRole('searchbox')).toBeNull();
  });
});

describe('the search of the column selection as MUI text field', () => {
  /** The label of the search: the key, translated to itself. */
  const LABEL_COLUMN_SEARCH = 'js.table.columnSearch';

  function entries(count: number) {
    return Array.from({ length: count }, (_, index) => (
      { name: 'c' + index, label: 'Spalte ' + index, visible: true, grouped: false }));
  }

  it('narrows the listed columns to the ones matching the search', async () => {
    mountAdapter(TLColumnSelect, { entries: entries(12) });
    await settle();

    const search = screen.getByRole('searchbox', { name: LABEL_COLUMN_SEARCH }) as HTMLInputElement;
    expectMuiBox(search);
    expect(search.closest('.tlColumnSelect__search')).not.toBeNull();
    expect(screen.getAllByRole('checkbox')).toHaveLength(12);

    await userEvent.type(search, 'Spalte 1');

    // "Spalte 1", "Spalte 10" and "Spalte 11".
    expect(screen.getAllByRole('checkbox').map(box => box.getAttribute('aria-label') ?? box.closest('label')!.textContent))
      .toEqual(['Spalte 1', 'Spalte 10', 'Spalte 11']);
  });

  it('offers no search for a short list', async () => {
    mountAdapter(TLColumnSelect, { entries: entries(3) });
    await settle();

    expect(screen.queryByRole('searchbox')).toBeNull();
  });
});

describe('the filter of a dropdown as MUI text field', () => {
  /** The label of the filter: the key, translated to itself. */
  const LABEL_FILTER = 'js.dropdownSelect.filterPlaceholder';

  const OPTIONS = [
    { value: 'a', label: 'Alpha' },
    { value: 'b', label: 'Beta' },
    { value: 'c', label: 'Gamma' },
  ];

  async function openDropdown(state: Record<string, unknown> = {}) {
    // jsdom lays nothing out and has no scrolling; the list scrolls the highlighted option into view.
    Element.prototype.scrollIntoView = () => {};
    const sent = mountAdapter(TLDropdownSelect, { value: [], options: OPTIONS, optionsLoaded: true, ...state });
    await settle();
    await userEvent.click(screen.getByRole('combobox'));
    await settle();
    return sent;
  }

  it('takes the focus when the list opens and narrows the options', async () => {
    await openDropdown();

    const filter = screen.getByRole('textbox', { name: LABEL_FILTER }) as HTMLInputElement;
    expectMuiBox(filter);
    expect(document.activeElement).toBe(filter);
    expect(filter.closest('.tl-select__search-group')).not.toBeNull();
    expect(filter.getAttribute('aria-controls')).toBe(CONTROL_ID + '-listbox');

    await userEvent.type(filter, 'ta');

    expect(screen.getAllByRole('option').map(option => option.textContent)).toEqual(['Beta']);
  });

  it('moves through the options with the arrow keys and selects with Enter', async () => {
    const sent = await openDropdown();

    const filter = screen.getByRole('textbox', { name: LABEL_FILTER });
    fireEvent.keyDown(filter, { key: 'ArrowDown' });
    fireEvent.keyDown(filter, { key: 'ArrowDown' });
    expect(filter.getAttribute('aria-activedescendant')).toBe(CONTROL_ID + '-opt-1');
    fireEvent.keyDown(filter, { key: 'Enter' });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_VALUE_CHANGED, { [ARG_VALUE]: ['b'] });
  });

  it('closes on Escape without selecting', async () => {
    const sent = await openDropdown();

    fireEvent.keyDown(screen.getByRole('textbox', { name: LABEL_FILTER }), { key: 'Escape' });
    await settle();

    expect(screen.queryByRole('textbox', { name: LABEL_FILTER })).toBeNull();
    expect(sent).not.toHaveBeenCalledWith(CMD_VALUE_CHANGED, expect.anything());
  });

  it('has no filter where the field asks for none', async () => {
    await openDropdown({ noFilter: true });

    expect(screen.queryByRole('textbox')).toBeNull();
    expect(screen.getAllByRole('option')).toHaveLength(3);
  });
});

describe('the filter of the icon popup as MUI text field', () => {
  /** The labels of the filter and of the button emptying it: the keys, translated to themselves. */
  const LABEL_FILTER = 'js.iconSelect.filterPlaceholder';
  const LABEL_CLEAR_FILTER = 'js.iconSelect.clearFilter';

  const ICONS = [
    { prefix: 'house', label: 'Haus', variants: [{ encoded: 'css:fa-solid fa-house' }] },
    { prefix: 'star', label: 'Stern', variants: [{ encoded: 'css:fa-solid fa-star' }] },
  ];

  function mountPopup() {
    const anchorRef = { current: document.createElement('button') };
    // The popup asks the bridge for its translations; the stubbed server answers each key with itself.
    mountAdapter(() => (
      <IconSelectPopup anchorRef={anchorRef} currentValue={null} icons={ICONS} iconsLoaded
        onSelect={() => {}} onCancel={() => {}} onLoadIcons={async () => {}} />
    ), {});
  }

  it('narrows the icons, and its clear button empties it and keeps the focus in it', async () => {
    mountPopup();
    await settle();

    const filter = screen.getByRole('textbox', { name: LABEL_FILTER }) as HTMLInputElement;
    expectMuiBox(filter);
    expect(document.activeElement).toBe(filter);
    expect(screen.queryByRole('button', { name: LABEL_CLEAR_FILTER })).toBeNull();

    await userEvent.type(filter, 'stern');
    expect(screen.getAllByRole('option').map(option => option.getAttribute('aria-label'))).toEqual(['Stern']);

    const clear = screen.getByRole('button', { name: LABEL_CLEAR_FILTER });
    expect(clear.closest('.MuiInputAdornment-positionEnd')).not.toBeNull();
    await userEvent.click(clear);

    expect(filter.value).toBe('');
    expect(document.activeElement).toBe(filter);
    expect(screen.getAllByRole('option')).toHaveLength(2);
    expect(screen.queryByRole('button', { name: LABEL_CLEAR_FILTER })).toBeNull();
  });
});
