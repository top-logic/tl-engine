import { React, useTLState, useTLFieldValue, rootClassName, useFieldLabelProps } from 'tl-react-bridge';
import type { TLCellProps, CheckboxStateJson } from 'tl-react-bridge';
import { fieldStateAttrs } from './form/fieldState';
import type { DisabledFieldState } from './form/fieldState';

const { useCallback, useRef, useEffect } = React;

/** The `display` a switch is drawn for; any other value is drawn as a box that is ticked. */
const DISPLAY_SWITCH: CheckboxStateJson.Display = 'switch';

/**
 * A boolean field rendered via React: a box that is ticked, or — with `display` set to
 * `switch` — a toggle sliding between its two states.
 *
 * With `triState` the field has a third state for "no value": it renders as indeterminate, and a
 * click cycles through checked, unchecked and unset.
 *
 * Design system: `tl-checkbox`, with `tl-checkbox--switch` and `role="switch"` for the switch. The
 * state is an attribute (see fieldStateAttrs), never a class. A field that is not editable keeps
 * the same box, read-only rather than disabled: it carries `aria-readonly="true"`, and its change
 * and click handlers block any change, so the native state cannot flip. Unlike a disabled box, a
 * read-only one keeps the brand fill when checked, and with it the contrast of its value.
 * `disabled` stays the inactive state, read from `state.disabled`.
 */
const TLCheckbox: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<CheckboxStateJson> & DisabledFieldState>();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const [value, setValue] = useTLFieldValue();
  const triState = state.triState === true;
  const asSwitch = state.display === DISPLAY_SWITCH;
  const editable = state.editable !== false;
  const boxRef = useRef<HTMLInputElement | null>(null);

  // "No value" has no checked attribute of its own; the DOM property is the only way to show it.
  useEffect(() => {
    if (boxRef.current) {
      boxRef.current.indeterminate = triState && value !== true && value !== false;
    }
  }, [triState, value]);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      if (!editable) return;
      if (!triState) {
        setValue(e.target.checked);
        return;
      }
      // checked -> unchecked -> unset -> checked
      setValue(value === true ? false : value === false ? null : true);
    },
    [editable, setValue, triState, value]
  );

  // A read-only box must not flip its native state, not even for the moment until React resets it.
  const handleClick = useCallback(
    (e: React.MouseEvent<HTMLInputElement>) => {
      if (!editable) e.preventDefault();
    },
    [editable]
  );

  const cls = asSwitch ? 'tl-checkbox tl-checkbox--switch' : 'tl-checkbox';

  return (
    <input
      type="checkbox"
      id={controlId}
      {...labelProps}
      ref={boxRef}
      role={asSwitch ? 'switch' : undefined}
      checked={value === true}
      onChange={handleChange}
      onClick={handleClick}
      disabled={state.disabled === true}
      aria-readonly={editable ? undefined : true}
      className={rootClassName(state, cls)}
      {...(editable ? fieldStateAttrs(state) : {})}
      aria-checked={triState && value !== true && value !== false ? 'mixed' : value === true}
    />
  );
};

export default TLCheckbox;
