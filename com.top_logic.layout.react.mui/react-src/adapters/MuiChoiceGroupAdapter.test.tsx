// The wire contract of TLChoiceGroup, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CMD_VALUE_CHANGED, fieldLabel } from 'tl-react-bridge';
import MuiChoiceGroupAdapter from './MuiChoiceGroupAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** Argument of {@link CMD_VALUE_CHANGED}: the values of the chosen options. */
const ARG_VALUE = 'value';

/** The ID of the form field around the group. */
const FIELD_ID = 'f1';

/** The label of the choice of no option. */
const NONE = 'Keine';

const ALPHA = { value: 'a', label: 'Alpha' };
const BETA = { value: 'b', label: 'Beta' };
const GAMMA = { value: 'c', label: 'Gamma' };
const OPTIONS = [ALPHA, BETA, GAMMA];

function mountGroup(state: Record<string, unknown>, options?: MountOptions) {
  return mountAdapter(MuiChoiceGroupAdapter,
    { options: OPTIONS, display: 'radio', emptyOptionLabel: NONE, ...state }, options);
}

function changedTo(...values: string[]) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: values }];
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLChoiceGroup as MUI RadioGroup', () => {
  it('renders a radio per option after the empty option, in a labelled radio group', () => {
    mountGroup({ value: [BETA] }, { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, vi.fn()) });

    const group = screen.getByRole('radiogroup');
    expect(group.id).toBe(CONTROL_ID);
    expect(group.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(screen.getAllByRole('radio').map(radio => radio.closest('label')!.textContent))
      .toEqual([NONE, 'Alpha', 'Beta', 'Gamma']);
    expect(screen.getByRole('radio', { name: 'Beta' })).toHaveProperty('checked', true);
  });

  it('offers no empty option for a mandatory field and lays the options out in a row', () => {
    mountGroup({ value: [], mandatory: true, orientation: 'horizontal' });

    expect(screen.queryByRole('radio', { name: NONE })).toBeNull();
    expect(screen.getByRole('radiogroup').classList).toContain('MuiFormGroup-row');
  });

  it('sends the chosen option, and the empty choice for the empty option', async () => {
    const sent = mountGroup({ value: [ALPHA] });

    await userEvent.click(screen.getByRole('radio', { name: 'Gamma' }));
    await userEvent.click(screen.getByRole('radio', { name: NONE }));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('c'), changedTo()]);
  });

  it('moves the choice with the arrow keys', async () => {
    const sent = mountGroup({ value: [ALPHA], mandatory: true });

    screen.getByRole('radio', { name: 'Alpha' }).focus();
    await userEvent.keyboard('{ArrowDown}');
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('b')]);
  });

  it('shows the chosen values while read-only', () => {
    mountGroup({ value: [GAMMA], editable: false });

    expect(screen.queryByRole('radiogroup')).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('Gamma');
  });

  it('sends nothing while disabled', async () => {
    const sent = mountGroup({ value: [], editable: false, disabled: true });

    const radio = screen.getByRole('radio', { name: 'Beta' });
    expect(radio).toHaveProperty('disabled', true);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(radio);
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });
});

describe('TLChoiceGroup of several options as MUI checkboxes', () => {
  it('renders a checkbox per option in a labelled group, without an empty option', () => {
    mountGroup({ value: [ALPHA, GAMMA], multiSelect: true },
      { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, vi.fn()) });

    const group = screen.getByRole('group');
    expect(group.id).toBe(CONTROL_ID);
    expect(group.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(screen.getAllByRole('checkbox').map(box => (box as HTMLInputElement).checked)).toEqual([true, false, true]);
  });

  it('toggles the options, also before the server answers', async () => {
    const sent = mountGroup({ value: [ALPHA], multiSelect: true });

    await userEvent.click(screen.getByRole('checkbox', { name: 'Beta' }));
    await userEvent.click(screen.getByRole('checkbox', { name: 'Alpha' }));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a', 'b'), changedTo('b')]);
  });

  it('marks every checkbox of an invalid field', () => {
    mountGroup({ value: [], multiSelect: true, hasError: true });

    expect(screen.getAllByRole('checkbox').every(box => box.getAttribute('aria-invalid') === 'true')).toBe(true);
  });
});
