import { React, useTLState, useTLFieldValue, useFieldLabelProps, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, CheckboxStateJson } from 'tl-react-bridge';
import Checkbox from '@mui/material/Checkbox';
import Switch from '@mui/material/Switch';

const { useCallback } = React;

/** The `display` a switch is drawn for; any other value is drawn as a box that is ticked. */
const DISPLAY_SWITCH: CheckboxStateJson.Display = 'switch';

/**
 * Renders the state of a TopLogic boolean field (module name `TLCheckbox`) with the MUI
 * `Checkbox`, or with the MUI `Switch` for display `switch`.
 *
 * <p>The field value is read and written through {@link useTLFieldValue}: a change sends the new
 * value as `valueChanged`, which makes the MUI component part of the form's edit and save cycle.
 * Mapping from the control state to the MUI props:</p>
 * <ul>
 * <li>value → `checked`; a tri-state field (triState) shows "no value" as `indeterminate` (with
 *     `aria-checked="mixed"`), and a click cycles through checked, unchecked and unset, as
 *     TLCheckbox does;</li>
 * <li>editable `false` → a read-only box: the same input with `aria-readonly`, as TLCheckbox draws
 *     it; MUI's `readOnly` ignores its changes, and the controlled input keeps the value;
 *     disabled → `disabled`;</li>
 * <li>hasError → color `error` and `aria-invalid`, otherwise hasWarnings → color `warning`;
 *     mandatory → `aria-required`;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the MUI root (through
 *     {@link rootClassName}).</li>
 * </ul>
 *
 * <p>MUI puts the `id` of a checkbox or switch on its `input`, not on its root element. The input
 * therefore carries the control ID, as the input of TLCheckbox does, and the label of the
 * surrounding form field refers to it ({@link useFieldLabelProps}).</p>
 *
 * <p>Not used: label, errorMessage, tooltip, placeholder, nullable and submitOnEnter of the field
 * state. The surrounding form field renders the label, the error message and the help text, as for
 * TLCheckbox; a boolean field has no placeholder and nothing to submit.</p>
 */
const MuiCheckboxAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<CheckboxStateJson>>();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const [value, setValue] = useTLFieldValue();

  const triState = state.triState === true;
  const editable = state.editable !== false;
  const disabled = state.disabled === true;
  const readOnly = !editable && !disabled;
  const unset = triState && value !== true && value !== false;

  const handleChange = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      if (!editable) return;
      if (!triState) {
        setValue(event.target.checked);
        return;
      }
      // checked -> unchecked -> unset -> checked
      setValue(value === true ? false : value === false ? null : true);
    },
    [editable, setValue, triState, value]
  );

  if (state.hidden === true) {
    return null;
  }

  const color = state.hasError === true ? 'error' : state.hasWarnings === true ? 'warning' : 'primary';
  const inputProps = {
    ...labelProps,
    'aria-readonly': readOnly || undefined,
    'aria-invalid': state.hasError === true || undefined,
    'aria-required': state.mandatory === true || undefined,
  };
  const common = {
    id: controlId,
    checked: value === true,
    onChange: handleChange,
    disabled,
    readOnly: !editable,
    color,
    className: rootClassName(state),
    slotProps: { input: inputProps },
  } as const;

  if (state.display === DISPLAY_SWITCH) {
    return <Switch {...common} />;
  }
  return <Checkbox {...common} indeterminate={unset} />;
};

export default MuiCheckboxAdapter;
