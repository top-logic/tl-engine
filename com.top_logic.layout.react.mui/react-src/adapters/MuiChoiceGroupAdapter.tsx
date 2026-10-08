import { React, useFieldLabelProps, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson } from 'tl-react-bridge';
import Checkbox from '@mui/material/Checkbox';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormGroup from '@mui/material/FormGroup';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import { MuiReadonlyValues, OptionContent, useChoice } from './choice';
import { showsValueOnly } from './field';

/** The orientation laying the options out side by side; any other lays them out one below the other. */
const ORIENTATION_HORIZONTAL: DropdownSelectStateJson.Orientation = 'horizontal';

/** The value of the radio button choosing no option. */
const EMPTY = '';

/**
 * Renders the state of a TopLogic choice field offering a radio button or a checkbox for every
 * option (module name `TLChoiceGroup`, `DropdownSelectState` with display `radio`) with MUI
 * `RadioGroup` and `Radio`s, or - for a field choosing several options - a `FormGroup` of
 * `Checkbox`es, each option in a `FormControlLabel`.
 *
 * <p>A field choosing one option that is not mandatory offers the choice of no option as a radio
 * of its own, the first one, labelled with emptyOptionLabel, as TLChoiceGroup does. The inputs are
 * native: the radios share one name, so the group is one tab stop whose arrow keys move the
 * choice; each checkbox is a tab stop of its own that Space toggles. Each change sends
 * `valueChanged` with the values of the chosen options (see {@link useChoice}).</p>
 *
 * <p>Mapping from the control state besides: orientation `horizontal` → `row`; an option → its
 * icon and label ({@link OptionContent}); hasError → `aria-invalid` on the radio group or on every
 * checkbox (a group of several takes none) and the color `error`; a field that is not editable
 * shows the chosen values ({@link MuiReadonlyValues}); disabled → every input disabled; hidden →
 * nothing is rendered. The group (role `radiogroup` or `group`) carries the control ID and is
 * labelled by the surrounding form field.</p>
 *
 * <p>Not used: label, errorMessage, hasWarnings, tooltip, placeholder, nullable, submitOnEnter,
 * optionsLoaded (the server sends the complete option list with the control), customOrder,
 * noFilter.</p>
 */
const MuiChoiceGroupAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const { state, value, options, press, send, goto } = useChoice();
  const labelProps = useFieldLabelProps(controlId, controlId);

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return <MuiReadonlyValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />;
  }

  const disabled = state.disabled === true;
  const hasError = state.hasError === true;
  const row = state.orientation === ORIENTATION_HORIZONTAL;
  const color = hasError ? 'error' : 'primary';
  const chosen = new Set(value.map(option => option.value));

  if (state.multiSelect === true) {
    return (
      <FormGroup id={controlId} role="group" {...labelProps} row={row} className={rootClassName(state)}>
        {options.map(option => (
          <FormControlLabel
            key={option.value}
            label={<OptionContent option={option} />}
            disabled={disabled}
            control={(
              <Checkbox checked={chosen.has(option.value)} color={color} onChange={() => press(option)}
                slotProps={{ input: { 'aria-invalid': hasError || undefined } as object }} />
            )}
          />
        ))}
      </FormGroup>
    );
  }

  const handleChange = (_event: React.ChangeEvent<HTMLInputElement>, optionValue: string) => {
    const option = options.find(candidate => candidate.value === optionValue);
    send(option ? [option] : []);
  };

  return (
    <RadioGroup
      id={controlId}
      {...labelProps}
      aria-invalid={hasError || undefined}
      name={controlId}
      row={row}
      value={value[0]?.value ?? EMPTY}
      onChange={handleChange}
      className={rootClassName(state)}
    >
      {state.mandatory !== true && (
        <FormControlLabel value={EMPTY} label={state.emptyOptionLabel ?? ''} disabled={disabled}
          control={<Radio color={color} />} />
      )}
      {options.map(option => (
        <FormControlLabel
          key={option.value}
          value={option.value}
          label={<OptionContent option={option} />}
          disabled={disabled}
          control={<Radio color={color} />}
        />
      ))}
    </RadioGroup>
  );
};

export default MuiChoiceGroupAdapter;
