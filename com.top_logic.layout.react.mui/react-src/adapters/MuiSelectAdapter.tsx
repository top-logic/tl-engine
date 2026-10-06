import {
  React, useTLState, useTLFieldValue, useFieldLabelProps, fieldInputId, rootClassName, anchoredOverlayProps,
} from 'tl-react-bridge';
import type { TLCellProps, SelectStateJson } from 'tl-react-bridge';
import Select from '@mui/material/Select';
import type { SelectChangeEvent } from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { FIELD_ROOT_STYLE, MuiFieldValue, fieldAriaProps, fieldColor, showsValueOnly } from './field';

/** The MUI value of the empty option: the field holds no value. */
const EMPTY = '';

/** The MUI value of the option at the given index; MUI compares option values as strings. */
function optionKey(index: number): string {
  return String(index);
}

/** Whether two values in their JSON form are the same value. */
function sameValue(a: unknown, b: unknown): boolean {
  return a === b || JSON.stringify(a) === JSON.stringify(b);
}

/**
 * Renders the state of a TopLogic select field (module name `TLSelect`) with the MUI `Select` and
 * a `MenuItem` per option.
 *
 * <p>An option's value is any JSON value (`SelectState.Option.value`); MUI keys the options by
 * their position, and a choice sends the option's own value as `valueChanged`. A field that is not
 * explicitly not nullable offers an empty option first, as TLSelect does, which sends `null`.</p>
 *
 * <p>The menu is portaled to the document body. Its root carries {@link anchoredOverlayProps}, so
 * that the focus trap of a dialog around the field lets the focus into the menu.</p>
 *
 * <p>Mapping from the control state besides: hasError → `error` and `aria-invalid`, hasWarnings →
 * color `warning`, mandatory → `aria-required`; a field that is not editable shows the label of the
 * chosen option as text, as TLSelect does; disabled → `disabled`; hidden → nothing is rendered; the
 * configured CSS class → className of the `Select`. The control ID is on the element around the
 * `Select`; its combobox carries {@link fieldInputId} and is labelled by the surrounding form
 * field.</p>
 *
 * <p>Not used: label (rendered by the form field), errorMessage, tooltip, placeholder and
 * submitOnEnter. The contract has no options that cannot be chosen, so no option is disabled.</p>
 */
const MuiSelectAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<SelectStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [value, setValue] = useTLFieldValue();

  if (state.hidden === true) {
    return null;
  }

  const options = state.options ?? [];
  const selected = value == null ? -1 : options.findIndex(option => sameValue(option.value, value));

  if (showsValueOnly(state)) {
    return (
      <MuiFieldValue id={controlId} className={rootClassName(state)}
        text={selected < 0 ? '' : options[selected].label ?? ''} />
    );
  }

  const handleChange = (event: SelectChangeEvent<string>) => {
    const key = event.target.value;
    setValue(key === EMPTY ? null : options[Number(key)].value ?? null);
  };

  return (
    <span id={controlId} style={FIELD_ROOT_STYLE}>
      <Select<string>
        value={selected < 0 ? EMPTY : optionKey(selected)}
        onChange={handleChange}
        displayEmpty
        disabled={state.disabled === true}
        error={state.hasError === true}
        color={fieldColor(state)}
        size="small"
        fullWidth
        className={rootClassName(state)}
        SelectDisplayProps={{ id: inputId, ...labelProps, ...fieldAriaProps(state) }}
        MenuProps={{ slotProps: { root: { ...anchoredOverlayProps } } }}
      >
        {state.nullable !== false && <MenuItem value={EMPTY}>&nbsp;</MenuItem>}
        {options.map((option, index) => (
          <MenuItem key={optionKey(index)} value={optionKey(index)}>{option.label}</MenuItem>
        ))}
      </Select>
    </span>
  );
};

export default MuiSelectAdapter;
