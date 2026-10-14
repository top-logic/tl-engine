// The chips of the chosen values of a TopLogic dropdown (TLDropdownSelect) in the TopLogic drawing of
// the parts: each chip offers its remove button as a tab stop of its own.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CMD_VALUE_CHANGED, registerPart } from 'tl-react-bridge';
import TLDropdownSelect from '../../com.top_logic.layout.react/react-src/controls/TLDropdownSelect';
import ButtonPart from '../../com.top_logic.layout.react/react-src/controls/button/ButtonPart';
import TextInputPart from '../../com.top_logic.layout.react/react-src/controls/form/TextInputPart';
import FieldValuePart from '../../com.top_logic.layout.react/react-src/controls/form/FieldValuePart';
import ChipPart from '../../com.top_logic.layout.react/react-src/controls/chip/ChipPart';
import { CONTROL_ID, mountAdapter, patchState, settle } from './adapters/wire-test-support';

registerPart('Button', ButtonPart);
registerPart('TextInput', TextInputPart);
registerPart('FieldValue', FieldValuePart);
registerPart('Chip', ChipPart);

/** The name of the remove button of a chip: the key, as the test stub translates each key to itself. */
const LABEL_REMOVE_VALUE = 'js.dropdownSelect.removeChip';

const ALPHA = { value: 'a', label: 'Alpha' };
const BETA = { value: 'b', label: 'Beta', colorRole: 'success' };

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

function mountDropdown(state: Record<string, unknown>) {
  return mountAdapter(TLDropdownSelect, { options: [ALPHA, BETA], optionsLoaded: true, multiSelect: true, ...state });
}

describe('the chosen values of a dropdown in the TopLogic drawing', () => {
  it('draws each value as a pill whose remove button is a tab stop after the field', async () => {
    mountDropdown({ value: [ALPHA, BETA] });
    await settle();

    const removes = screen.getAllByRole('button', { name: LABEL_REMOVE_VALUE });
    expect(removes).toHaveLength(2);
    removes.forEach(remove => {
      expect(remove.closest('.tl-pill.tl-select__chip')).not.toBeNull();
      expect(remove.hasAttribute('tabindex')).toBe(false);
    });
    await userEvent.tab();
    expect(document.activeElement!.id).toBe(CONTROL_ID);
    await userEvent.tab();
    expect(document.activeElement).toBe(removes[0]);
    await userEvent.tab();
    expect(document.activeElement).toBe(removes[1]);
  });

  it('removes a value by Enter on its remove button and focuses the remove button of the next chip', async () => {
    const sent = mountDropdown({ value: [ALPHA, BETA] });
    await settle();

    screen.getAllByRole('button', { name: LABEL_REMOVE_VALUE })[0].focus();
    await userEvent.keyboard('{Enter}');
    patchState({ value: [BETA] });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_VALUE_CHANGED, { value: ['b'] });
    expect(screen.queryByRole('listbox')).toBeNull();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: LABEL_REMOVE_VALUE }));
  });
});
