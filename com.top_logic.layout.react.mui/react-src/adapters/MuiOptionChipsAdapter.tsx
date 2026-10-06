import { React, useFieldLabelProps, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import Chip from '@mui/material/Chip';
import Stack from '@mui/material/Stack';
import { MuiReadonlyValues, optionIcon, roleColor, useChoice } from './choice';
import { showsValueOnly } from './field';

/**
 * Renders the state of a TopLogic choice field offering every option as a toggle of its own
 * (module name `TLOptionChips`, `DropdownSelectState`) as a group of MUI filter `Chip`s.
 *
 * <p>A chosen option is a filled chip, any other an outlined one; each chip is a button with
 * `aria-pressed`, a tab stop of its own that Space and Enter press (MUI's chip keyboard handling).
 * Pressing toggles the option in a field choosing several; in a field choosing one it chooses the
 * option, and pressing the chosen one clears the field unless it is mandatory, as TLOptionChips
 * does. Each change sends `valueChanged` with the values of the chosen options (see
 * {@link useChoice}).</p>
 *
 * <p>Mapping from the control state besides: an option's image → the chip's icon
 * ({@link optionIcon}), its color role → the chip's color ({@link roleColor}), a chosen option without one in
 * `primary`; hasError →
 * `aria-invalid` on the group and the chips in color `error`; a field that is not editable shows
 * the chosen values ({@link MuiReadonlyValues}); disabled → every chip disabled; hidden → nothing
 * is rendered. The group (role `group`) carries the control ID and is labelled by the surrounding
 * form field.</p>
 *
 * <p>Not used: label, errorMessage, hasWarnings, tooltip, placeholder, nullable, submitOnEnter,
 * optionsLoaded (the server sends the complete option list with the control), customOrder,
 * noFilter, emptyOptionLabel, orientation.</p>
 */
const MuiOptionChipsAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const { state, value, options, press, goto } = useChoice();
  const labelProps = useFieldLabelProps(controlId, controlId);

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return <MuiReadonlyValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />;
  }

  const disabled = state.disabled === true;
  const hasError = state.hasError === true;
  const selected = new Set(value.map(option => option.value));
  return (
    <Stack id={controlId} role="group" {...labelProps} aria-invalid={hasError || undefined}
      direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }} className={rootClassName(state)}>
      {options.map(option => {
        const on = selected.has(option.value);
        const role = roleColor(option.colorRole);
        return (
          <Chip
            key={option.value}
            label={option.label}
            icon={optionIcon(option)}
            color={hasError ? 'error' : role === 'default' && on ? 'primary' : role}
            variant={on ? 'filled' : 'outlined'}
            aria-pressed={on}
            disabled={disabled}
            onClick={() => press(option)}
          />
        );
      })}
    </Stack>
  );
};

export default MuiOptionChipsAdapter;
