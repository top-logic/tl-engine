import { React, useTLState, useTLFieldValue, rootClassName, useFieldLabelProps, fieldInputId, usePart } from 'tl-react-bridge';
import type { TLCellProps, DatePickerStateJson } from 'tl-react-bridge';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';

const { useCallback } = React;

/**
 * A field for a point in time rendered via React.
 *
 * Which HTML input it is - a date, a time of day, or both - the server decides from the attribute's
 * type and states in `inputType`; the value is exchanged in the ISO form belonging to that input.
 * A field whose `precision` is `seconds` lets the seconds of a time of day be entered (`step="1"`)
 * and exchanges the time with its seconds.
 *
 * A read-only field shows the localized value, drawn by the part `FieldValue`; a disabled field
 * renders the input as an inactive one (native `disabled`, see showsValueOnly).
 *
 * An empty field shows `state.placeholder` - e.g. the value it inherits, written in the format of
 * the field - as a greyed hint beside the input, since the native input has no place for a text of
 * its own. The hint describes the input for assistive technology.
 */
const TLDatePicker: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DatePickerStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [value, setValue] = useTLFieldValue();
  const FieldValue = usePart('FieldValue');

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  if (showsValueOnly(state)) {
    // Read-only: show the localized value (e.g. "01.06.2026") supplied by the server, falling
    // back to the ISO value if no localized form was emitted.
    const display = state.displayValue ?? (value as string) ?? '';
    return <FieldValue id={controlId} className={rootClassName(state) || undefined} text={display} />;
  }

  const hint = value == null || value === '' ? (state.placeholder ?? null) : null;
  const hintId = `${controlId}-hint`;

  return (
    <span id={controlId} className={hint !== null ? 'tl-field-hint-host' : undefined}>
      <input
        type={state.inputType ?? 'date'}
        step={state.precision === 'seconds' ? 1 : undefined}
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={rootClassName(state, 'tl-field tl-type-body')}
        {...fieldStateAttrs(state)}
        id={inputId}
        {...labelProps}
        aria-describedby={hint !== null ? hintId : undefined}
      />
      {hint !== null && <span id={hintId} className="tl-field-hint tl-type-body">{hint}</span>}
    </span>
  );
};

export default TLDatePicker;
