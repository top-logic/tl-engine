// The wire contract of TLPasswordInput, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import { CMD_VALUE_CHANGED, VALUE_DEBOUNCE_MS } from 'tl-react-bridge';
import MuiPasswordInputAdapter from './MuiPasswordInputAdapter';
import { CONTROL_ID, fakeDebounceTimers, mountAdapter, settle } from './wire-test-support';

/** The argument of {@link CMD_VALUE_CHANGED} holding the password. */
const ARG_VALUE = 'value';

/** The ID of the input inside the control. */
const INPUT_ID = CONTROL_ID + '-input';

function changedTo(value: string) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: value }];
}

function inputById(): HTMLInputElement {
  return document.getElementById(INPUT_ID) as HTMLInputElement;
}

afterEach(() => {
  cleanup();
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('TLPasswordInput as MUI TextField', () => {
  it('renders a password input without a button revealing it', () => {
    mountAdapter(MuiPasswordInputAdapter, { value: 'geheim' });

    const box = inputById();
    expect(box.type).toBe('password');
    expect(box.value).toBe('geheim');
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('sends the typed password after the debounce', async () => {
    fakeDebounceTimers();
    const sent = mountAdapter(MuiPasswordInputAdapter, { value: '' });

    fireEvent.change(inputById(), { target: { value: 'pw' } });
    vi.advanceTimersByTime(VALUE_DEBOUNCE_MS - 1);
    await settle();
    expect(sent).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    await settle();
    expect(sent.mock.calls).toEqual([changedTo('pw')]);
  });

  it('shows a mask instead of the password while read-only', () => {
    mountAdapter(MuiPasswordInputAdapter, { value: 'geheim', editable: false });

    expect(inputById()).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('••••••••');
  });
});
