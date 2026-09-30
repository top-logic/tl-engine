import { React, useTLState, useTLFieldValue, rootClassName, useFieldLabelProps, fieldInputId } from 'tl-react-bridge';
import type { TLCellProps, DatePickerStateJson } from 'tl-react-bridge';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import { FieldValue } from './form/FieldValue';

const { useCallback } = React;

/**
 * A field for a point in time rendered via React.
 *
 * Which HTML input it is - a date, a time of day, or both - the server decides from the attribute's
 * type and states in `inputType`; the value is exchanged in the ISO form belonging to that input.
 *
 * A read-only field shows the localized value as a tl-field-value; a disabled field renders the
 * input as an inactive one (native `disabled`, see showsValueOnly).
 */
const TLDatePicker: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DatePickerStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [value, setValue] = useTLFieldValue();

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
    return <FieldValue id={controlId} className={rootClassName(state)} text={display} />;
  }

  return (
    <span id={controlId}>
      <input
        type={state.inputType ?? 'date'}
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={rootClassName(state, 'tl-field tl-type-body')}
        {...fieldStateAttrs(state)}
        id={inputId}
        {...labelProps}
      />
    </span>
  );
};

export default TLDatePicker;
