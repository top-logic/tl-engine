import { React, useFieldLabelProps, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson, CheckboxValidation } from 'tl-react-bridge';
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
 * `RadioGroup` and `Radio`s, each option in a `FormControlLabel`, or - for a field choosing several
 * options - a `FormGroup` of checkboxes drawn by the part `Checkbox` with the option as their label,
 * so that they look like the checkboxes everywhere else.
 *
 * <p>A field choosing one option that is not mandatory offers the choice of no option as a radio
 * of its own, the first one, labelled with emptyOptionLabel, as TLChoiceGroup does. The inputs are
 * native: the radios share one name, so the group is one tab stop whose arrow keys move the
 * choice; each checkbox is a tab stop of its own that Space toggles. Each change sends
 * `valueChanged` with the values of the chosen options (see {@link useChoice}).</p>
 *
 * <p>Mapping from the control state besides: orientation `horizontal` → `row`; an option → its
 * icon and label ({@link OptionContent}); hasError → `aria-invalid` and the color `error` on the radio
 * group, or the validation `error` of every checkbox (a group of several takes none); hasWarnings →
 * the validation `warning` of every checkbox; a field that is not editable
 * shows the chosen values ({@link MuiReadonlyValues}); disabled → every input disabled; hidden →
 * nothing is rendered. The group (role `radiogroup` or `group`) carries the control ID and is
 * labelled by the surrounding form field.</p>
 *
 * <p>Not used: label, errorMessage, hasWarnings of a radio group, tooltip, placeholder, nullable, submitOnEnter,
 * optionsLoaded (the server sends the complete option list with the control), customOrder,
 * noFilter.</p>
 */
const MuiChoiceGroupAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const { state, value, options, press, send, goto } = useChoice();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const Checkbox = usePart('Checkbox');

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
    const validation: CheckboxValidation | undefined =
      hasError ? 'error' : state.hasWarnings === true ? 'warning' : undefined;
    return (
      <FormGroup id={controlId} role="group" {...labelProps} row={row} className={rootClassName(state)}>
        {options.map(option => (
          <Checkbox
            key={option.value}
            label={<OptionContent option={option} />}
            checked={chosen.has(option.value)}
            disabled={disabled}
            validation={validation}
            onChange={() => press(option)}
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
