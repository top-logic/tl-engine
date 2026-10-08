// The wire contract of TLSegmentedChoice, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CMD_VALUE_CHANGED, fieldLabel } from 'tl-react-bridge';
import MuiSegmentedChoiceAdapter from './MuiSegmentedChoiceAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** Argument of {@link CMD_VALUE_CHANGED}: the values of the chosen options. */
const ARG_VALUE = 'value';

/** The ID of the form field around the bar. */
const FIELD_ID = 'f1';

const ALPHA = { value: 'a', label: 'Alpha' };
const BETA = { value: 'b', label: 'Beta' };
const OPTIONS = [ALPHA, BETA];

function mountSegmented(state: Record<string, unknown>, options?: MountOptions) {
  return mountAdapter(MuiSegmentedChoiceAdapter, { options: OPTIONS, display: 'segmented', ...state }, options);
}

function changedTo(...values: string[]) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: values }];
}

function segment(name: string) {
  return screen.getByRole('button', { name });
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLSegmentedChoice as MUI ToggleButtonGroup', () => {
  it('renders a pressed segment for the chosen option, in a labelled group', () => {
    mountSegmented({ value: [BETA] }, { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, vi.fn()) });

    const group = screen.getByRole('group');
    expect(group.id).toBe(CONTROL_ID);
    expect(group.classList).toContain('MuiToggleButtonGroup-root');
    expect(group.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(segment('Beta').getAttribute('aria-pressed')).toBe('true');
    expect(segment('Alpha').getAttribute('aria-pressed')).toBe('false');
  });

  it('chooses one option, and clears it on a second press of a field that is not mandatory', async () => {
    const sent = mountSegmented({ value: [] });

    await userEvent.click(segment('Alpha'));
    await userEvent.click(segment('Alpha'));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a'), changedTo()]);
  });

  it('toggles the options of a multi-valued field', async () => {
    const sent = mountSegmented({ value: [ALPHA], multiSelect: true });

    await userEvent.click(segment('Beta'));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a', 'b')]);
  });

  it('is pressed from the keyboard', async () => {
    const sent = mountSegmented({ value: [] });

    await userEvent.tab();
    expect(document.activeElement).toBe(segment('Alpha'));
    await userEvent.keyboard(' ');
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a')]);
  });

  it('shows the chosen values while read-only', () => {
    mountSegmented({ value: [ALPHA], editable: false });

    expect(screen.queryByRole('group')).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('Alpha');
  });

  it('sends nothing while disabled', async () => {
    const sent = mountSegmented({ value: [], editable: false, disabled: true });

    await userEvent.setup({ pointerEventsCheck: 0 }).click(segment('Alpha'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });
});
