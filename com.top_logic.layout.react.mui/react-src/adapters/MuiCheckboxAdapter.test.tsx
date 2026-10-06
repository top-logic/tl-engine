// The wire contract of TLCheckbox, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CMD_VALUE_CHANGED, fieldLabel } from 'tl-react-bridge';
import type { CheckboxStateJson } from 'tl-react-bridge';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** The argument of {@link CMD_VALUE_CHANGED} holding the new value. */
const ARG_VALUE = 'value';

/** The ID of the form field around the checkbox. */
const FIELD_ID = 'f1';

function mountCheckbox(state: Partial<CheckboxStateJson>, options?: MountOptions) {
  return mountAdapter(MuiCheckboxAdapter, state, options);
}

/** The command a change of the value to the given one sends. */
function changedTo(value: boolean | null) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: value }];
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLCheckbox as MUI Checkbox', () => {
  it('renders an MUI checkbox whose input carries the control ID', () => {
    mountCheckbox({ value: true, cssClass: 'my-class' });

    const box = screen.getByRole('checkbox');
    expect(box.id).toBe(CONTROL_ID);
    expect(box).toHaveProperty('checked', true);
    const root = box.closest('.MuiCheckbox-root');
    expect(root).not.toBeNull();
    expect(root!.classList).toContain('my-class');
  });

  it('sends the new value when it is ticked', async () => {
    const sent = mountCheckbox({ value: false });

    await userEvent.click(screen.getByRole('checkbox'));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo(true)]);
  });

  it('shows "no value" of a tri-state field as indeterminate and cycles on click', async () => {
    const sent = mountCheckbox({ value: null, triState: true });

    const box = screen.getByRole('checkbox');
    expect(box.getAttribute('aria-checked')).toBe('mixed');
    expect(box.closest('.MuiCheckbox-root')!.classList).toContain('MuiCheckbox-indeterminate');

    // unset -> checked -> unchecked -> unset
    await userEvent.click(box);
    await userEvent.click(box);
    await userEvent.click(box);
    await settle();

    expect(sent.mock.calls).toEqual([changedTo(true), changedTo(false), changedTo(null)]);
  });

  it('renders a switch for display switch', async () => {
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

  it('sends nothing while read-only and keeps its value', async () => {
    const sent = mountCheckbox({ value: true, editable: false });

    const box = screen.getByRole('checkbox');
    expect(box.getAttribute('aria-readonly')).toBe('true');
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
