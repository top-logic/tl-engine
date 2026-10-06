// The wire contract of TLDropdownSelect, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ANCHORED_OVERLAY_ATTR, CMD_VALUE_CHANGED, fieldLabel } from 'tl-react-bridge';
import { deDE } from '@mui/material/locale';
import MuiDropdownSelectAdapter from './MuiDropdownSelectAdapter';
import { CONTROL_ID, mountAdapter, patchState, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** Command asking the server for the options. */
const CMD_LOAD_OPTIONS = 'loadOptions';

/** Command following the link of a displayed value. */
const CMD_GOTO = 'goto';

/** Argument of {@link CMD_GOTO}: the value of the option. */
const ARG_OPTION = 'option';

/** Argument of {@link CMD_VALUE_CHANGED}: the values of the chosen options. */
const ARG_VALUE = 'value';

/** The ID of the input inside the control. */
const INPUT_ID = CONTROL_ID + '-input';

/** The ID of the form field around the select. */
const FIELD_ID = 'f1';

const ALPHA = { value: 'a', label: 'Alpha' };
const BETA = { value: 'b', label: 'Beta', colorRole: 'error', image: 'css:fa-solid fa-star' };
const GAMMA = { value: 'c', label: 'Gamma' };
const OPTIONS = [ALPHA, BETA, GAMMA];

function mountDropdown(state: Record<string, unknown>, options?: MountOptions) {
  return mountAdapter(MuiDropdownSelectAdapter, state, options);
}

function changedTo(...values: string[]) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: values }];
}

/** Opens the list of the field and returns it. */
async function openList() {
  await userEvent.click(screen.getByRole('button', { name: 'Open' }));
  return screen.findByRole('listbox');
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  document.documentElement.lang = '';
});

describe('TLDropdownSelect as MUI Autocomplete', () => {
  it('shows the chosen option in its input, labelled by the form field', () => {
    const setInputId = vi.fn();
    mountDropdown({ value: [ALPHA], cssClass: 'my-class' },
      { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, setInputId) });

    const input = screen.getByRole('combobox') as HTMLInputElement;
    expect(input.id).toBe(INPUT_ID);
    expect(input.value).toBe('Alpha');
    expect(input.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(setInputId).toHaveBeenCalledWith(INPUT_ID);
    expect(document.getElementById(CONTROL_ID)!.querySelector('.MuiAutocomplete-root.my-class')).not.toBeNull();
  });

  it('asks for its options when opened, shows the loading state, then the options not chosen', async () => {
    const sent = mountDropdown({ value: [ALPHA] });

    await userEvent.click(screen.getByRole('button', { name: 'Open' }));
    await settle();
    expect(sent.mock.calls).toEqual([[CMD_LOAD_OPTIONS, {}]]);
    expect(screen.getByText('Loading…')).toBeTruthy();

    patchState({ optionsLoaded: true, options: OPTIONS });
    const list = await screen.findByRole('listbox');
    expect(within(list).getAllByRole('option').map(option => option.textContent)).toEqual(['Beta', 'Gamma']);
    expect(list.closest(`[${ANCHORED_OVERLAY_ATTR}]`)).not.toBeNull();
  });

  it('does not ask again for options it has', async () => {
    const sent = mountDropdown({ value: [], optionsLoaded: true, options: OPTIONS });

    await openList();
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('sends the value of the chosen option', async () => {
    const sent = mountDropdown({ value: [ALPHA], optionsLoaded: true, options: OPTIONS });

    const list = await openList();
    await userEvent.click(within(list).getByRole('option', { name: 'Gamma' }));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('c')]);
  });

  it('draws an option with a color role as a chip of that role with its icon', async () => {
    mountDropdown({ value: [], optionsLoaded: true, options: OPTIONS });

    const list = await openList();
    const beta = within(list).getByRole('option', { name: 'Beta' });
    expect(beta.querySelector('.MuiChip-colorError i.fa-star')).not.toBeNull();
  });

  it('sends the empty choice from its clear button, which a mandatory field lacks', async () => {
    const sent = mountDropdown({ value: [ALPHA], emptyOptionLabel: 'Keine Auswahl' });

    // MUI shows the clear button while the field is pointed at or focused; a hidden element has no
    // accessible name, so it is found by its title.
    await userEvent.setup({ pointerEventsCheck: 0 }).click(screen.getByTitle('Clear'));
    await settle();
    expect(sent.mock.calls).toEqual([changedTo()]);
    expect((screen.getByRole('combobox') as HTMLInputElement).placeholder).toBe('Keine Auswahl');

    cleanup();
    mountDropdown({ value: [ALPHA], mandatory: true });
    expect(screen.queryByTitle('Clear')).toBeNull();
  });

  it('shows the chosen options of a multi-valued field as chips and appends a new one', async () => {
    const sent = mountDropdown({ value: [ALPHA, BETA], multiSelect: true, optionsLoaded: true, options: OPTIONS });

    const root = document.getElementById(CONTROL_ID)!;
    expect(Array.from(root.querySelectorAll('.MuiChip-label')).map(chip => chip.textContent)).toEqual(['Alpha', 'Beta']);

    const list = await openList();
    expect(within(list).getAllByRole('option').map(option => option.textContent)).toEqual(['Gamma']);
    await userEvent.click(within(list).getByRole('option', { name: 'Gamma' }));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a', 'b', 'c')]);
  });

  it('removes a chosen option of a multi-valued field with the button of its chip', async () => {
    const sent = mountDropdown({ value: [ALPHA, BETA], multiSelect: true });

    const chip = document.getElementById(CONTROL_ID)!.querySelectorAll('.MuiChip-root')[0];
    await userEvent.click(chip.querySelector('.MuiChip-deleteIcon')!);
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('b')]);
  });

  it('names its loading state in the page language', async () => {
    document.documentElement.lang = 'de';
    mountDropdown({ value: [] });

    await userEvent.click(screen.getByRole('button', { name: deDE.components!.MuiAutocomplete!.defaultProps!.openText }));

    expect(screen.getByText(deDE.components!.MuiAutocomplete!.defaultProps!.loadingText as string)).toBeTruthy();
  });

  it('shows the chosen options while read-only, following a link with goto', async () => {
    const sent = mountDropdown({ value: [{ ...ALPHA, link: true }, BETA], editable: false });

    expect(screen.queryByRole('combobox')).toBeNull();
    await userEvent.click(screen.getByRole('button', { name: 'Alpha' }));
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_GOTO, { [ARG_OPTION]: 'a' }]]);
    expect(document.getElementById(CONTROL_ID)!.textContent).toContain('Beta');
  });

  it('opens nothing and sends nothing while disabled', async () => {
    const sent = mountDropdown({ value: [ALPHA], editable: false, disabled: true });

    expect((screen.getByRole('combobox') as HTMLInputElement).disabled).toBe(true);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(screen.getByRole('combobox'));
    await settle();

    expect(screen.queryByRole('listbox')).toBeNull();
    expect(sent).not.toHaveBeenCalled();
  });
});
