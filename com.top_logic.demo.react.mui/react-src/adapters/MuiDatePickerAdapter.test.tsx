// The wire contract of TLDatePicker, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ANCHORED_OVERLAY_ATTR, CMD_VALUE_CHANGED, fieldLabel } from 'tl-react-bridge';
import type { DatePickerStateJson } from 'tl-react-bridge';
import MuiDatePickerAdapter, { fromIso, toIso } from './MuiDatePickerAdapter';
import { CONTROL_ID, matchDesktopPointer, mountAdapter, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** The argument of {@link CMD_VALUE_CHANGED} holding the ISO form of the value. */
const ARG_VALUE = 'value';

/** The ID of the hidden input of the picker field, which the label of the form field refers to. */
const INPUT_ID = CONTROL_ID + '-input';

/** The ID of the form field around the picker. */
const FIELD_ID = 'f1';

function mountDate(state: Partial<DatePickerStateJson>, options?: MountOptions) {
  matchDesktopPointer();
  return mountAdapter(MuiDatePickerAdapter, state, options);
}

function changedTo(value: string | null) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: value }];
}

/** Steps the section of the picker field with the given name one up, from the keyboard. */
async function stepUp(section: string) {
  await userEvent.click(screen.getByRole('spinbutton', { name: section }));
  await userEvent.keyboard('{ArrowUp}');
  await settle();
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  document.documentElement.lang = '';
});

describe('ISO form of a TLDatePicker value', () => {
  it('reads and writes a date', () => {
    const date = fromIso('date', '2026-06-01')!;
    expect([date.year(), date.month(), date.date(), date.hour()]).toEqual([2026, 5, 1, 0]);
    expect(toIso('date', date)).toBe('2026-06-01');
  });

  it('reads and writes a time of day', () => {
    const time = fromIso('time', '14:30')!;
    expect([time.hour(), time.minute()]).toEqual([14, 30]);
    expect(toIso('time', time)).toBe('14:30');
  });

  it('reads a local date-time without a zone and writes it to the minute', () => {
    const dateTime = fromIso('datetime-local', '2026-06-01T14:30:15')!;
    expect([dateTime.date(), dateTime.hour(), dateTime.minute()]).toEqual([1, 14, 30]);
    expect(toIso('datetime-local', dateTime)).toBe('2026-06-01T14:30');
  });

  it('reads no value and an invalid text as null', () => {
    expect(fromIso('date', undefined)).toBeNull();
    expect(fromIso('date', '')).toBeNull();
    expect(fromIso('date', 'kein Datum')).toBeNull();
    expect(toIso('date', null)).toBeNull();
  });
});

describe('TLDatePicker as MUI X picker', () => {
  it('renders a date field in the format of the page language, labelled by the form field', () => {
    document.documentElement.lang = 'de';
    const setInputId = vi.fn();
    mountDate({ value: '2026-06-01', inputType: 'date', cssClass: 'my-class' },
      { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, setInputId) });

    const input = document.getElementById(INPUT_ID) as HTMLInputElement;
    expect(input.value).toBe('01.06.2026');
    expect(setInputId).toHaveBeenCalledWith(INPUT_ID);
    const group = screen.getByRole('group');
    expect(group.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(document.getElementById(CONTROL_ID)!.querySelector('.my-class')).not.toBeNull();
  });

  it('sends the ISO form of a changed date', async () => {
    const sent = mountDate({ value: '2026-06-01', inputType: 'date' });

    await stepUp('Day');

    expect(sent.mock.calls).toEqual([changedTo('2026-06-02')]);
  });

  it('sends the ISO form of a changed time of day', async () => {
    const sent = mountDate({ value: '14:30', inputType: 'time' });

    await stepUp('Minutes');

    expect(sent.mock.calls).toEqual([changedTo('14:31')]);
  });

  it('sends the ISO form of a changed date-time', async () => {
    const sent = mountDate({ value: '2026-06-01T14:30', inputType: 'datetime-local' });

    await stepUp('Day');

    expect(sent.mock.calls).toEqual([changedTo('2026-06-02T14:30')]);
  });

  it('sends null when the field is emptied', async () => {
    const sent = mountDate({ value: '2026-06-01', inputType: 'date' });

    await userEvent.click(screen.getByRole('spinbutton', { name: 'Day' }));
    await userEvent.keyboard('{Control>}a{/Control}{Backspace}');
    await settle();

    expect(sent.mock.calls).toEqual([changedTo(null)]);
  });

  it('marks its popup as an overlay anchored to the field', async () => {
    mountDate({ value: '2026-06-01', inputType: 'date' });

    await userEvent.click(screen.getByRole('button', { name: /Choose date/ }));

    const popup = await screen.findByRole('dialog');
    expect(popup.closest(`[${ANCHORED_OVERLAY_ATTR}]`)).not.toBeNull();
  });

  it('shows the display value as text while read-only', () => {
    mountDate({ value: '2026-06-01', displayValue: '01.06.2026', editable: false });

    expect(screen.queryByRole('group')).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('01.06.2026');
  });

  it('sends nothing while disabled', async () => {
    const sent = mountDate({ value: '2026-06-01', inputType: 'date', editable: false, disabled: true });

    await userEvent.setup({ pointerEventsCheck: 0 }).click(screen.getByRole('spinbutton', { name: 'Day' }));
    await userEvent.keyboard('{ArrowUp}');
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });
});
