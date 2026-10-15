import { React } from 'tl-react-bridge';
import type { CheckboxPartProps } from 'tl-react-bridge';
import { fieldStateAttrs } from './fieldState';

const { useCallback, useLayoutEffect, useRef } = React;

/** The class of the box of the design system. */
const BOX_CLASS = 'tl-checkbox';

/** The class of the box drawn as switch. */
const SWITCH_CLASS = 'tl-checkbox--switch';

/** The classes of the label around a box with a label of its own. */
const LABEL_CLASS = 'tl-choice tl-type-body';

/** The classes, without the empty ones, joined. */
function classes(...names: (string | false | undefined)[]): string {
  return names.filter(Boolean).join(' ');
}

/**
 * The TopLogic drawing of the part `Checkbox` ({@link CheckboxPartProps}): the native checkbox of
 * the design system (tl-checkbox), a switch with `tl-checkbox--switch` and the role `switch`. A box
 * with a label stands in the label of the design system (tl-choice), the label after it: a text in a
 * `span` of its own, other content as it is given. The design system has a single size of the box,
 * which a small box keeps. The native box has no padded touch area, so it starts at the start edge
 * of its space whether it is aligned there or not.
 *
 * <p>The design system reads the state from the input itself: `:checked`, `:indeterminate`,
 * `:disabled`, `aria-readonly`, `aria-invalid` and `data-tl-state` (see fieldStateAttrs). An
 * indeterminate box has no attribute of its own, so its DOM property is set after every render and
 * after every press, which clears it natively.</p>
 *
 * <p>A press of a read-only box calls no {@link CheckboxPartProps.onChange}; the controlled input
 * keeps the state it is given.</p>
 */
const CheckboxPart: React.FC<CheckboxPartProps> = ({
  id, ref, checked, indeterminate, display, label, small: _small, alignStart: _alignStart, disabled, readOnly, required, validation, name,
  tabIndex, className, onChange, ...attributes
}) => {
  const asSwitch = display === 'switch';
  const mixed = indeterminate === true && !asSwitch;
  const boxRef = useRef<HTMLInputElement | null>(null);

  const setBox = useCallback((element: HTMLInputElement | null) => {
    boxRef.current = element;
    if (typeof ref === 'function') {
      ref(element);
    } else if (ref) {
      ref.current = element;
    }
  }, [ref]);

  useLayoutEffect(() => {
    if (boxRef.current) {
      boxRef.current.indeterminate = mixed;
    }
  });

  const handleChange = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    if (readOnly || disabled) return;
    onChange?.(event.target.checked);
  }, [readOnly, disabled, onChange]);

  const handleClick = useCallback((event: React.MouseEvent<HTMLInputElement>) => {
    event.currentTarget.indeterminate = mixed;
  }, [mixed]);

  const box = (
    <input
      type="checkbox"
      id={id}
      ref={setBox}
      name={name}
      role={asSwitch ? 'switch' : undefined}
      className={classes(BOX_CLASS, asSwitch && SWITCH_CLASS, label === undefined && className)}
      checked={checked}
      onChange={handleChange}
      onClick={handleClick}
      disabled={disabled}
      tabIndex={tabIndex}
      aria-readonly={readOnly || undefined}
      {...fieldStateAttrs({
        hasError: validation === 'error',
        hasWarnings: validation === 'warning',
        mandatory: required,
      })}
      {...attributes}
    />
  );
  if (label === undefined) {
    return box;
  }
  return (
    <label className={classes(LABEL_CLASS, className)}>
      {box}
      {typeof label === 'string' ? <span>{label}</span> : label}
    </label>
  );
};

export default CheckboxPart;
