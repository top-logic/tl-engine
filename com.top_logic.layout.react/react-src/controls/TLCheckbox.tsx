import { React, useTLState, useTLFieldValue, rootClassName, useFieldLabelProps, usePart } from 'tl-react-bridge';
import type { TLCellProps, CheckboxStateJson, CheckboxDisplay } from 'tl-react-bridge';
import { showsValueOnly, validationOf } from './form/fieldState';

const { useCallback } = React;

/** The `display` a switch is drawn for; any other value is drawn as a box that is ticked. */
const DISPLAY_SWITCH: CheckboxStateJson.Display = 'switch';

/** The shape of the part for a switch. */
const PART_SWITCH: CheckboxDisplay = 'switch';

/**
 * A boolean field rendered via React: a box that is ticked, or — with `display` set to
 * `switch` — a toggle sliding between its two states, drawn by the part `Checkbox`.
 *
 * With `triState` the field has a third state for "no value": it renders as indeterminate, and a
 * click cycles through checked, unchecked and unset.
 *
 * A field that is not editable is either read-only - the part shows its value and ignores a press -
 * or disabled - the part is drawn inactive (see showsValueOnly). A read-only field shows no
 * validation state. The input carries the control ID, and the label of the surrounding form field
 * refers to it.
 */
const TLCheckbox: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<CheckboxStateJson>>();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const [value, setValue] = useTLFieldValue();
  const Checkbox = usePart('Checkbox');
  const triState = state.triState === true;
  const editable = state.editable !== false;
  const readOnly = showsValueOnly(state);

  const handleChange = useCallback(
    (checked: boolean) => {
      if (!editable) return;
      if (!triState) {
        setValue(checked);
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

  return (
    <Checkbox
      id={controlId}
      {...labelProps}
      checked={value === true}
      indeterminate={triState && value !== true && value !== false}
      display={state.display === DISPLAY_SWITCH ? PART_SWITCH : undefined}
      alignStart
      onChange={handleChange}
      disabled={state.disabled === true}
      readOnly={readOnly}
      required={!readOnly && state.mandatory === true}
      validation={readOnly ? undefined : validationOf(state)}
      className={rootClassName(state) || undefined}
    />
  );
};

export default TLCheckbox;
