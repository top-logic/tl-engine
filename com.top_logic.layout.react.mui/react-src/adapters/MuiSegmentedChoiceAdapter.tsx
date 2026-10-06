import { React, useFieldLabelProps, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';
import { MuiReadonlyValues, OptionContent, useChoice } from './choice';
import { showsValueOnly } from './field';

/**
 * Renders the state of a TopLogic choice field whose options are the segments of one bar (module
 * name `TLSegmentedChoice`, `DropdownSelectState`) as an MUI `ToggleButtonGroup`.
 *
 * <p>A field choosing one option is an `exclusive` group: pressing a segment chooses its option,
 * pressing the chosen one clears the field unless it is mandatory, as TLSegmentedChoice does. A
 * field choosing several toggles each segment. Each change sends `valueChanged` with the values of
 * the chosen options (see {@link useChoice}). Each segment is a button with `aria-pressed` and a tab
 * stop of its own that Space and Enter press.</p>
 *
 * <p>Mapping from the control state besides: an option → its icon and label, a pill-like chip
 * where it has a color role ({@link OptionContent}); hasError → `aria-invalid` and the color
 * `error`; a field that is not editable shows the chosen values ({@link MuiReadonlyValues});
 * disabled → the group disabled; hidden → nothing is rendered. The group carries the control ID
 * and is labelled by the surrounding form field.</p>
 *
 * <p>Not reproduced: the radio keyboard pattern of a field choosing one (TLSegmentedChoice is one
 * tab stop whose arrow keys move the choice); the MUI group has no such mode. Not used: label,
 * errorMessage, hasWarnings, tooltip, placeholder, nullable, submitOnEnter, optionsLoaded (the
 * server sends the complete option list with the control), customOrder, noFilter,
 * emptyOptionLabel, orientation.</p>
 */
const MuiSegmentedChoiceAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const { state, value, options, press, goto } = useChoice();
  const labelProps = useFieldLabelProps(controlId, controlId);

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return <MuiReadonlyValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />;
  }

  const multiSelect = state.multiSelect === true;
  const hasError = state.hasError === true;
  const chosen = value.map(option => option.value);
  return (
    <ToggleButtonGroup
      id={controlId}
      {...labelProps}
      aria-invalid={hasError || undefined}
      exclusive={!multiSelect}
      value={multiSelect ? chosen : chosen[0] ?? null}
      disabled={state.disabled === true}
      color={hasError ? 'error' : 'primary'}
      size="small"
      className={rootClassName(state)}
    >
      {options.map(option => (
        <ToggleButton key={option.value} value={option.value} onClick={() => press(option)}>
          <OptionContent option={option} />
        </ToggleButton>
      ))}
    </ToggleButtonGroup>
  );
};

export default MuiSegmentedChoiceAdapter;
