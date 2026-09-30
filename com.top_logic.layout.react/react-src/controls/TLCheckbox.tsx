import { React, useTLFieldValue, rootClassName, useFieldLabelProps } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { fieldStateAttrs } from './form/fieldState';

const { useCallback, useRef, useEffect } = React;

/** The `display` a switch is drawn for; any other value is drawn as a box that is ticked. */
const DISPLAY_SWITCH = 'switch';

/**
 * A boolean field rendered via React: a box that is ticked, or — with `display` set to
 * `switch` — a toggle sliding between its two states.
 *
 * With `triState` the field has a third state for "no value": it renders as indeterminate, and a
 * click cycles through checked, unchecked and unset.
 *
 * Design system: `tl-checkbox`, with `tl-checkbox--switch` and `role="switch"` for the switch. The
 * state is an attribute (see fieldStateAttrs), never a class. A field that is not editable keeps
 * the same box, disabled.
 */
const TLCheckbox: React.FC<TLCellProps> = ({ controlId, state }) => {
  const labelProps = useFieldLabelProps(controlId, controlId);
  const [value, setValue] = useTLFieldValue();
  const triState = state.triState === true;
  const asSwitch = state.display === DISPLAY_SWITCH;
  const boxRef = useRef<HTMLInputElement | null>(null);

  // "No value" has no checked attribute of its own; the DOM property is the only way to show it.
  useEffect(() => {
    if (boxRef.current) {
      boxRef.current.indeterminate = triState && value !== true && value !== false;
    }
  }, [triState, value]);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      if (!triState) {
        setValue(e.target.checked);
        return;
      }
      // checked -> unchecked -> unset -> checked
      setValue(value === true ? false : value === false ? null : true);
    },
    [setValue, triState, value]
  );

  const cls = asSwitch ? 'tl-checkbox tl-checkbox--switch' : 'tl-checkbox';

  if (state.editable === false) {
    return (
      <input
        type="checkbox"
        id={controlId}
        {...labelProps}
        ref={boxRef}
        role={asSwitch ? 'switch' : undefined}
        checked={value === true}
        disabled
        className={rootClassName(state, cls)}
      />
    );
  }

  return (
    <input
      type="checkbox"
      id={controlId}
      {...labelProps}
      ref={boxRef}
      role={asSwitch ? 'switch' : undefined}
      checked={value === true}
      onChange={handleChange}
      disabled={state.disabled === true}
      className={rootClassName(state, cls)}
      {...fieldStateAttrs(state)}
      aria-checked={triState && value !== true && value !== false ? 'mixed' : value === true}
    />
  );
};

export default TLCheckbox;
