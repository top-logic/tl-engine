// The wire contract of TLNumberInput, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup, fireEvent } from '@testing-library/react';
import { CMD_SUBMIT, CMD_VALUE_CHANGED } from 'tl-react-bridge';
import MuiNumberInputAdapter from './MuiNumberInputAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';

/** The argument of {@link CMD_VALUE_CHANGED} and {@link CMD_SUBMIT} holding the text. */
const ARG_VALUE = 'value';

/** The ID of the input inside the control. */
const INPUT_ID = CONTROL_ID + '-input';

function changedTo(value: string | null) {
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

describe('TLNumberInput as MUI TextField', () => {
  it('renders a text input with the on-screen keyboard of the state', () => {
    mountAdapter(MuiNumberInputAdapter, { value: '12,50', inputMode: 'decimal' });

    const box = inputById();
    expect(box.type).toBe('text');
    expect(box.getAttribute('inputmode')).toBe('decimal');
    expect(box.value).toBe('12,50');
  });

  it('sends the text as typed, and an empty text as null, when it is left', async () => {
    const sent = mountAdapter(MuiNumberInputAdapter, { value: '1', sendValueOnBlur: true });

    fireEvent.change(inputById(), { target: { value: 'foo' } });
    fireEvent.blur(inputById());
    await settle();
    fireEvent.change(inputById(), { target: { value: '' } });
    fireEvent.blur(inputById());
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('foo'), changedTo(null)]);
  });

  it('sends submit with the text on Enter with submitOnEnter', async () => {
    const sent = mountAdapter(MuiNumberInputAdapter, { value: '42', submitOnEnter: true });

    fireEvent.keyDown(inputById(), { key: 'Enter' });
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_SUBMIT, { [ARG_VALUE]: '42' }]]);
  });

  it('shows the text while read-only and sends nothing', async () => {
    const sent = mountAdapter(MuiNumberInputAdapter, { value: '1.234,5', editable: false });
    await settle();

    expect(inputById()).toBeNull();
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('1.234,5');
    expect(sent).not.toHaveBeenCalled();
  });
});
