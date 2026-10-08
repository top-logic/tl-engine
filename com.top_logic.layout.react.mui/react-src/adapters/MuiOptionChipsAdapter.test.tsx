// The wire contract of TLOptionChips, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CMD_VALUE_CHANGED, fieldLabel } from 'tl-react-bridge';
import MuiOptionChipsAdapter from './MuiOptionChipsAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** Argument of {@link CMD_VALUE_CHANGED}: the values of the chosen options. */
const ARG_VALUE = 'value';

/** The ID of the form field around the chips. */
const FIELD_ID = 'f1';

const ALPHA = { value: 'a', label: 'Alpha' };
const BETA = { value: 'b', label: 'Beta', colorRole: 'success' };
const OPTIONS = [ALPHA, BETA];

function mountChips(state: Record<string, unknown>, options?: MountOptions) {
  return mountAdapter(MuiOptionChipsAdapter, { options: OPTIONS, display: 'chips', ...state }, options);
}

function changedTo(...values: string[]) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: values }];
}

function chip(name: string) {
  return screen.getByRole('button', { name });
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLOptionChips as MUI filter chips', () => {
  it('renders a chip per option, a chosen one filled and pressed, in a labelled group', () => {
    mountChips({ value: [ALPHA] }, { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, vi.fn()) });

    const group = screen.getByRole('group');
    expect(group.id).toBe(CONTROL_ID);
    expect(group.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(chip('Alpha').getAttribute('aria-pressed')).toBe('true');
    expect(chip('Alpha').classList).toContain('MuiChip-filled');
    expect(chip('Beta').getAttribute('aria-pressed')).toBe('false');
    expect(chip('Beta').classList).toContain('MuiChip-outlined');
    expect(chip('Beta').classList).toContain('MuiChip-colorSuccess');
  });

  it('toggles the options of a multi-valued field, also before the server answers', async () => {
    const sent = mountChips({ value: [ALPHA], multiSelect: true });

    await userEvent.click(chip('Beta'));
    await userEvent.click(chip('Alpha'));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a', 'b'), changedTo('b')]);
  });

  it('chooses one option of a single-valued field and clears it on a second press', async () => {
    const sent = mountChips({ value: [ALPHA] });

    await userEvent.click(chip('Beta'));
    await userEvent.click(chip('Beta'));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('b'), changedTo()]);
  });

  it('keeps the option of a mandatory single-valued field on a second press', async () => {
    const sent = mountChips({ value: [ALPHA], mandatory: true });

    await userEvent.click(chip('Alpha'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('is pressed from the keyboard', async () => {
    const sent = mountChips({ value: [], multiSelect: true });

    await userEvent.tab();
    expect(document.activeElement).toBe(chip('Alpha'));
    await userEvent.keyboard(' ');
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a')]);
  });

  it('shows the chosen values while read-only', () => {
    mountChips({ value: [BETA], editable: false });

    expect(screen.queryByRole('group')).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('Beta');
  });

  it('sends nothing while disabled', async () => {
    const sent = mountChips({ value: [], editable: false, disabled: true });

    await userEvent.setup({ pointerEventsCheck: 0 }).click(chip('Alpha'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });
});
