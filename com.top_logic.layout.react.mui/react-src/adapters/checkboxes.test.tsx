// The checkboxes a control draws - the input of a boolean field, the selection boxes of a table, the
// boxes of the column selection, the options of a choice group of several values - drawn by the MUI adapter of the part Checkbox: one replacement of
// the part reaches all of them.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CMD_VALUE_CHANGED, fieldLabel, replacePart } from 'tl-react-bridge';
import type { CheckboxStateJson } from 'tl-react-bridge';
import TLCheckbox from '../../../com.top_logic.layout.react/react-src/controls/TLCheckbox';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLColumnSelect from '../../../com.top_logic.layout.react/react-src/controls/TLColumnSelect';
import TLChoiceGroup from '../../../com.top_logic.layout.react/react-src/controls/TLChoiceGroup';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';
import MuiChipAdapter from './MuiChipAdapter';

replacePart('Checkbox', MuiCheckboxAdapter);
// The table and the column selection look up their buttons and toggles as well.
replacePart('Button', MuiButtonAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
// The table and the column selection draw their search boxes as well.
replacePart('TextInput', MuiTextInputAdapter);
// The table draws its named filters as chips.
replacePart('Chip', MuiChipAdapter);

/** The argument of {@link CMD_VALUE_CHANGED} holding the new value. */
const ARG_VALUE = 'value';

/** The ID of the form field around the checkbox. */
const FIELD_ID = 'f1';

/** The names of the selection boxes of a table: the keys, as the test stub translates each key to itself. */
const LABEL_SELECT_ALL = 'js.table.selectAllRows';
const LABEL_SELECT_ROW = 'js.table.selectRow';

/** The commands the table and the column selection send for their boxes. */
const CMD_SELECT = 'select';
const CMD_SELECT_ALL = 'selectAll';
const CMD_COLUMN_VISIBLE = 'columnVisible';

/** A resize observer that never reports: jsdom lays nothing out. */
class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

/** Asserts that the given input is the one of an MUI checkbox and not the box of TopLogic. */
function expectMuiBox(box: HTMLElement) {
  expect(box.closest('.MuiCheckbox-root')).not.toBeNull();
  expect(box.classList).not.toContain('tl-checkbox');
}

describe('a boolean field as MUI checkbox', () => {
  function mountCheckbox(state: Partial<CheckboxStateJson>, options?: MountOptions) {
    return mountAdapter(TLCheckbox, state, options);
  }

  /** The command a change of the value to the given one sends. */
  function changedTo(value: boolean | null) {
    return [CMD_VALUE_CHANGED, { [ARG_VALUE]: value }];
  }

  it('draws an MUI checkbox whose input carries the control ID', () => {
    mountCheckbox({ value: true, cssClass: 'my-class' });

    const box = screen.getByRole('checkbox');
    expectMuiBox(box);
    expect(box.id).toBe(CONTROL_ID);
    expect(box).toHaveProperty('checked', true);
    // The box of a field lines up with the field's label: its touch area hangs over the start edge.
    expect(getComputedStyle(box.closest('.MuiCheckbox-root')!).marginLeft).toBe('-12px');
    expect(box.closest('.MuiCheckbox-root')!.classList).toContain('my-class');
  });

  it('sends the new value when it is ticked', async () => {
    const sent = mountCheckbox({ value: false });

    await userEvent.click(screen.getByRole('checkbox'));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo(true)]);
  });

  it('shows "no value" of a tri-state field as indeterminate and cycles on click', async () => {
    const sent = mountCheckbox({ value: null, triState: true });

    const box = screen.getByRole('checkbox') as HTMLInputElement;
    expect(box.getAttribute('aria-checked')).toBe('mixed');
    expect(box.indeterminate).toBe(true);
    expect(box.closest('.MuiCheckbox-root')!.classList).toContain('MuiCheckbox-indeterminate');

    // unset -> checked -> unchecked -> unset
    await userEvent.click(box);
    expect(box.hasAttribute('aria-checked')).toBe(false);
    expect(box.checked).toBe(true);
    await userEvent.click(box);
    await userEvent.click(box);
    await settle();

    expect(sent.mock.calls).toEqual([changedTo(true), changedTo(false), changedTo(null)]);
    expect(box.getAttribute('aria-checked')).toBe('mixed');
  });

  it('shows "no value" of a field without third state as unticked', () => {
    mountCheckbox({ value: null });

    const box = screen.getByRole('checkbox') as HTMLInputElement;
    expect(box.checked).toBe(false);
    expect(box.indeterminate).toBe(false);
    expect(box.hasAttribute('aria-checked')).toBe(false);
  });

  it('draws a switch for display switch', async () => {
    const sent = mountCheckbox({ value: true, display: 'switch' });

    const toggle = screen.getByRole('switch');
    expect(toggle.id).toBe(CONTROL_ID);
    expect(toggle.closest('.MuiSwitch-root')).not.toBeNull();

    await userEvent.click(toggle);
    await settle();

    expect(sent.mock.calls).toEqual([changedTo(false)]);
  });

  it('marks an invalid mandatory field', () => {
    mountCheckbox({ value: false, hasError: true, mandatory: true });

    const box = screen.getByRole('checkbox');
    expect(box.getAttribute('aria-invalid')).toBe('true');
    expect(box.getAttribute('aria-required')).toBe('true');
    expect(box.closest('.MuiCheckbox-root')!.classList).toContain('MuiCheckbox-colorError');
  });

  it('is labelled by the form field around it', () => {
    const setInputId = vi.fn();
    mountCheckbox({ value: false }, { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, setInputId) });

    const box = screen.getByRole('checkbox');
    expect(box.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(setInputId).toHaveBeenCalledWith(CONTROL_ID);
  });

  it('sends nothing while read-only, keeps its value and shows no validation state', async () => {
    const sent = mountCheckbox({ value: true, editable: false, hasError: true, mandatory: true });

    const box = screen.getByRole('checkbox');
    expect(box.getAttribute('aria-readonly')).toBe('true');
    expect(box.hasAttribute('aria-invalid')).toBe(false);
    expect(box.hasAttribute('aria-required')).toBe(false);
    await userEvent.click(box);
    await settle();

    expect(box).toHaveProperty('checked', true);
    expect(sent).not.toHaveBeenCalled();
  });

  it('sends nothing while disabled', async () => {
    const sent = mountCheckbox({ value: false, editable: false, disabled: true });

    const box = screen.getByRole('checkbox');
    expect(box).toHaveProperty('disabled', true);
    expect(box.hasAttribute('aria-readonly')).toBe(false);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(box);
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('renders nothing while hidden', () => {
    mountCheckbox({ value: true, hidden: true });

    expect(screen.queryByRole('checkbox')).toBeNull();
  });
});

describe('the selection boxes of a table as MUI checkboxes', () => {
  function row(index: number, extra: Record<string, unknown> = {}) {
    return { id: 'r' + index, index, selected: false, cells: { name: childControl('c' + index, 'Zeile ' + index) }, ...extra };
  }

  function mountTable(state: Record<string, unknown>) {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    const rows = state.rows as unknown[];
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      totalRowCount: rows.length,
      selectionMode: 'multi',
      ...state,
    });
  }

  it('marks the box selecting all rows indeterminate while some rows are selected, and selects all', async () => {
    const sent = mountTable({ rows: [row(0, { selected: true }), row(1)], selectAllState: 'some' });
    await settle();

    const all = screen.getByRole('checkbox', { name: LABEL_SELECT_ALL }) as HTMLInputElement;
    expectMuiBox(all);
    // Small, to fit the selection column.
    expect(all.closest('.MuiCheckbox-root')!.classList).toContain('MuiCheckbox-sizeSmall');
    expect(all.indeterminate).toBe(true);
    expect(all.getAttribute('aria-checked')).toBe('mixed');
    expect(all.checked).toBe(false);
    await userEvent.click(all);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_SELECT_ALL, { selected: true });
  });

  it('ticks the box selecting all rows while all are, and deselects all', async () => {
    const sent = mountTable({ rows: [row(0, { selected: true })], selectAllState: 'all' });
    await settle();

    const all = screen.getByRole('checkbox', { name: LABEL_SELECT_ALL }) as HTMLInputElement;
    expect(all.checked).toBe(true);
    expect(all.indeterminate).toBe(false);
    expect(all.hasAttribute('aria-checked')).toBe(false);
    await userEvent.click(all);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_SELECT_ALL, { selected: false });
  });

  it('toggles the selection of a row by its box, out of the tab order', async () => {
    const sent = mountTable({ rows: [row(0, { selected: true }), row(1)], selectAllState: 'some' });
    await settle();

    const boxes = screen.getAllByRole('checkbox', { name: LABEL_SELECT_ROW }) as HTMLInputElement[];
    expect(boxes).toHaveLength(2);
    boxes.forEach(expectMuiBox);
    boxes.forEach(box => expect(box.closest('.MuiCheckbox-root')!.classList).toContain('MuiCheckbox-sizeSmall'));
    expect(boxes.map(box => box.checked)).toEqual([true, false]);
    expect(boxes[1].getAttribute('tabindex')).toBe('-1');
    await userEvent.click(boxes[1]);
    await settle();

    // The press toggles the row alone: no plain row click selecting it exclusively.
    expect(sent.mock.calls.filter(([command]) => command === CMD_SELECT))
      .toEqual([[CMD_SELECT, { rowIndex: 1, ctrlKey: true, shiftKey: false }]]);
  });

  it('has no selection box for a group row', async () => {
    mountTable({ rows: [row(0, { groupCount: 3 }), row(1)], selectAllState: 'none' });
    await settle();

    expect(screen.getAllByRole('checkbox', { name: LABEL_SELECT_ROW })).toHaveLength(1);
  });

  it('has no selection boxes in a table selecting a single row', async () => {
    mountTable({ rows: [row(0), row(1)], selectionMode: 'single' });
    await settle();

    expect(screen.queryByRole('checkbox')).toBeNull();
  });
});

describe('the boxes of the column selection as MUI checkboxes', () => {
  function entry(name: string, visible: boolean) {
    return { name, label: 'Spalte ' + name, visible, grouped: false };
  }

  it('shows or hides a column by its labelled box', async () => {
    const sent = mountAdapter(TLColumnSelect, { entries: [entry('a', true), entry('b', false)] });
    await settle();

    const a = screen.getByRole('checkbox', { name: 'Spalte a' }) as HTMLInputElement;
    expectMuiBox(a);
    expect(a.checked).toBe(true);
    expect(a.closest('label')!.classList).toContain('MuiFormControlLabel-root');
    expect(a.closest('label')!.parentElement!.classList).toContain('tlColumnSelect__label');
    await userEvent.click(screen.getByText('Spalte b'));
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_VISIBLE, { column: 'b', visible: true });
  });

  it('keeps the last shown column from being hidden', async () => {
    const sent = mountAdapter(TLColumnSelect, { entries: [entry('a', true), entry('b', false)] });
    await settle();

    const a = screen.getByRole('checkbox', { name: 'Spalte a' }) as HTMLInputElement;
    expect(a.disabled).toBe(true);
    expect(screen.getByRole('checkbox', { name: 'Spalte b' })).toHaveProperty('disabled', false);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(a);
    await settle();

    expect(sent).not.toHaveBeenCalledWith(CMD_COLUMN_VISIBLE, expect.anything());
  });
});

describe('the options of a choice group of several values as MUI checkboxes', () => {
  const ALPHA = { value: 'a', label: 'Alpha' };
  const BETA = { value: 'b', label: 'Beta' };

  function mountGroup(state: Record<string, unknown>) {
    return mountAdapter(TLChoiceGroup, { options: [ALPHA, BETA], display: 'radio', ...state });
  }

  it('draws each option as the label of a box and toggles it', async () => {
    const sent = mountGroup({ value: [ALPHA], multiSelect: true });

    const group = screen.getByRole('group');
    expect(group.id).toBe(CONTROL_ID);
    const alpha = screen.getByRole('checkbox', { name: 'Alpha' }) as HTMLInputElement;
    expectMuiBox(alpha);
    expect(alpha.checked).toBe(true);
    expect(alpha.closest('label')!.classList).toContain('MuiFormControlLabel-root');
    await userEvent.click(screen.getByText('Beta'));
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_VALUE_CHANGED, { [ARG_VALUE]: ['a', 'b'] });
  });

  it('marks every box of an invalid field, not the group', () => {
    mountGroup({ value: [], multiSelect: true, hasError: true });

    const boxes = screen.getAllByRole('checkbox');
    expect(boxes.every(box => box.getAttribute('aria-invalid') === 'true')).toBe(true);
    expect(screen.getByRole('group').hasAttribute('aria-invalid')).toBe(false);
  });

  it('sends nothing while disabled', async () => {
    const sent = mountGroup({ value: [], multiSelect: true, editable: false, disabled: true });

    const boxes = screen.getAllByRole('checkbox') as HTMLInputElement[];
    expect(boxes.every(box => box.disabled)).toBe(true);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(boxes[0]);
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('keeps the radios of a group of one value', () => {
    mountGroup({ value: [ALPHA], mandatory: true });

    expect(screen.queryByRole('checkbox')).toBeNull();
    const radio = screen.getByRole('radio', { name: 'Alpha' });
    expect(radio.classList).toContain('tl-radio');
  });
});
