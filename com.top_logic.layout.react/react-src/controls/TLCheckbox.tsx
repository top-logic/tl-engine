import { React, useTLFieldValue, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef, useEffect } = React;

/** The `display` a switch is drawn for; any other value is drawn as a box that is ticked. */
const DISPLAY_SWITCH = 'switch';

/**
 * A boolean field rendered via React: a box that is ticked, or — with `display` set to
 * `switch` — a toggle sliding between its two states.
 *
 * With `triState` the field has a third state for "no value": it renders as indeterminate, and a
 * click cycles through checked, unchecked and unset.
 */
const TLCheckbox: React.FC<TLCellProps> = ({ controlId, state }) => {
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

  if (state.editable === false) {
    return (
      <input
        type="checkbox"
        id={controlId}
        ref={boxRef}
        role={asSwitch ? 'switch' : undefined}
        checked={value === true}
        disabled
        className={rootClassName(
          state,
          ['tlReactCheckbox', 'tlReactCheckbox--immutable', asSwitch ? 'tlReactCheckbox--switch' : '']
            .filter(Boolean)
            .join(' ')
        )}
      />
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactCheckbox',
    asSwitch ? 'tlReactCheckbox--switch' : '',
    hasError ? 'tlReactCheckbox--error' : '',
    !hasError && hasWarnings ? 'tlReactCheckbox--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <input
      type="checkbox"
      id={controlId}
      ref={boxRef}
      role={asSwitch ? 'switch' : undefined}
      checked={value === true}
      onChange={handleChange}
      disabled={state.disabled === true}
      className={rootClassName(state, cls)}
      aria-invalid={hasError || undefined}
      aria-checked={triState && value !== true && value !== false ? 'mixed' : value === true}
    />
  );
};

export default TLCheckbox;
