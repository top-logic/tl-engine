import { React } from 'tl-react-bridge';
import type { CheckboxPartProps, FieldValidation } from 'tl-react-bridge';
import Checkbox from '@mui/material/Checkbox';
import Switch from '@mui/material/Switch';
import FormControlLabel from '@mui/material/FormControlLabel';

/** The MUI color of the box for the state of its value; `primary` for a valid one. */
const COLOR: Record<FieldValidation, 'error' | 'warning'> = {
  error: 'error',
  warning: 'warning',
};

/**
 * Draws the part `Checkbox` ({@link CheckboxPartProps}) with Material UI: the MUI `Checkbox`, or the
 * MUI `Switch` for display `switch`; a box with a label stands in a `FormControlLabel`.
 *
 * <p>Mapping from the props to the MUI props:</p>
 * <ul>
 * <li>checked → `checked`, indeterminate → `indeterminate`, which sets the DOM property of the
 *     input; the input is also marked `aria-checked="mixed"`, which MUI does not set;</li>
 * <li>readOnly → `readOnly` and `aria-readonly`; a press then calls no onChange, and the controlled
 *     input keeps its state; disabled → `disabled`;</li>
 * <li>small → size `small`: a box of 38 pixels with its padding, which fits the selection column of
 *     a table;</li>
 * <li>validation → color `error` (with `aria-invalid`) or `warning`; required →
 *     `aria-required`;</li>
 * <li>id, ref, name, tabIndex and the passed-through attributes → the input; className → the
 *     `FormControlLabel` of a box with a label, else the root of the box.</li>
 * </ul>
 */
const MuiCheckboxAdapter: React.FC<CheckboxPartProps> = ({
  id, ref, checked, indeterminate, display, label, small, disabled, readOnly, required, validation, name, tabIndex,
  className, onChange, ...attributes
}) => {
  const asSwitch = display === 'switch';
  const mixed = indeterminate === true && !asSwitch;

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (readOnly || disabled) return;
    onChange?.(event.target.checked);
  };

  const inputProps = {
    ...attributes,
    ref,
    tabIndex,
    'aria-readonly': readOnly || undefined,
    'aria-checked': mixed ? 'mixed' as const : undefined,
    'aria-invalid': validation === 'error' ? true : undefined,
    'aria-required': required || undefined,
  };
  const common = {
    id,
    name,
    checked,
    onChange: handleChange,
    disabled,
    readOnly,
    size: small ? 'small' as const : undefined,
    color: validation === undefined ? 'primary' as const : COLOR[validation],
    className: label === undefined ? className : undefined,
    slotProps: { input: inputProps },
  };

  const box = asSwitch ? <Switch {...common} /> : <Checkbox {...common} indeterminate={mixed} />;
  if (label === undefined) {
    return box;
  }
  return <FormControlLabel className={className} control={box} label={label} disabled={disabled} />;
};

export default MuiCheckboxAdapter;
