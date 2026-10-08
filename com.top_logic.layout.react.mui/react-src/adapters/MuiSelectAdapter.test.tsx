// The wire contract of TLSelect, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ANCHORED_OVERLAY_ATTR, CMD_VALUE_CHANGED, fieldLabel } from 'tl-react-bridge';
import type { SelectStateJson } from 'tl-react-bridge';
import MuiSelectAdapter from './MuiSelectAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** The argument of {@link CMD_VALUE_CHANGED} holding the chosen value. */
const ARG_VALUE = 'value';

/** The ID of the combobox inside the control. */
const INPUT_ID = CONTROL_ID + '-input';

/** The ID of the form field around the select. */
const FIELD_ID = 'f1';

/** Options whose values are JSON values other than strings. */
const OPTIONS: SelectStateJson.Option[] = [
  { value: 1, label: 'Eins' },
  { value: { id: 2 }, label: 'Zwei' },
];

function mountSelect(state: Partial<SelectStateJson>, options?: MountOptions) {
  return mountAdapter(MuiSelectAdapter, state, options);
}

function changedTo(value: unknown) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: value }];
}

/** Opens the menu of the select and returns its list. */
async function openMenu() {
  await userEvent.click(screen.getByRole('combobox'));
  return screen.findByRole('listbox');
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLSelect as MUI Select', () => {
  it('shows the label of the chosen option, labelled by the form field', () => {
    const setInputId = vi.fn();
    mountSelect({ value: { id: 2 }, options: OPTIONS, cssClass: 'my-class' },
      { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, setInputId) });

    const combobox = screen.getByRole('combobox');
    expect(combobox.id).toBe(INPUT_ID);
    expect(combobox.textContent).toBe('Zwei');
    expect(combobox.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(setInputId).toHaveBeenCalledWith(INPUT_ID);
    expect(document.getElementById(CONTROL_ID)!.querySelector('.MuiInputBase-root.my-class')).not.toBeNull();
  });

  it('offers an empty option first and portals its menu as an anchored overlay', async () => {
    mountSelect({ value: 1, options: OPTIONS });

    const list = await openMenu();
    const items = within(list).getAllByRole('option');
    expect(items.map(item => item.textContent?.trim())).toEqual(['', 'Eins', 'Zwei']);
    expect(list.closest(`[${ANCHORED_OVERLAY_ATTR}]`)).not.toBeNull();
  });

  it('offers no empty option for a field that is not nullable', async () => {
    mountSelect({ value: 1, options: OPTIONS, nullable: false });

    const list = await openMenu();
    expect(within(list).getAllByRole('option').map(item => item.textContent)).toEqual(['Eins', 'Zwei']);
  });

  it('sends the JSON value of the chosen option', async () => {
    const sent = mountSelect({ value: 1, options: OPTIONS });

    const list = await openMenu();
    await userEvent.click(within(list).getByRole('option', { name: 'Zwei' }));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo({ id: 2 })]);
  });

  it('sends null for the empty option', async () => {
    const sent = mountSelect({ value: 1, options: OPTIONS });

    const list = await openMenu();
    await userEvent.click(within(list).getAllByRole('option')[0]);
    await settle();

    expect(sent.mock.calls).toEqual([changedTo(null)]);
  });

  it('marks an invalid mandatory field', () => {
    mountSelect({ value: 1, options: OPTIONS, hasError: true, mandatory: true });

    const combobox = screen.getByRole('combobox');
    expect(combobox.getAttribute('aria-invalid')).toBe('true');
    expect(combobox.getAttribute('aria-required')).toBe('true');
    expect(combobox.closest('.Mui-error')).not.toBeNull();
  });

  it('shows the label of the chosen option as text while read-only', () => {
    mountSelect({ value: 1, options: OPTIONS, editable: false });

    expect(screen.queryByRole('combobox')).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('Eins');
  });

  it('opens no menu and sends nothing while disabled', async () => {
    const sent = mountSelect({ value: 1, options: OPTIONS, editable: false, disabled: true });

    await userEvent.setup({ pointerEventsCheck: 0 }).click(screen.getByRole('combobox'));
    await settle();

    expect(screen.queryByRole('listbox')).toBeNull();
    expect(sent).not.toHaveBeenCalled();
  });
});
