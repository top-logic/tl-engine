import { React, useTLState, useTLFieldValue, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, DatePickerState } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A field for a point in time rendered via React.
 *
 * Which HTML input it is - a date, a time of day, or both - the server decides from the attribute's
 * type and states in `inputType`; the value is exchanged in the ISO form belonging to that input.
 */
const TLDatePicker: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<DatePickerState>();
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  if (state.editable === false) {
    // View mode: show the localized value (e.g. "01.06.2026") supplied by the server, falling
    // back to the ISO value if no localized form was emitted.
    const display = state.displayValue ?? (value as string) ?? '';
    return (
      <span id={controlId} className={rootClassName(state, 'tlReactDatePicker tlReactDatePicker--immutable')}>
        {display}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactDatePicker',
    hasError ? 'tlReactDatePicker--error' : '',
    !hasError && hasWarnings ? 'tlReactDatePicker--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <input
        type={state.inputType ?? 'date'}
        value={(value as string) ?? ''}
        onChange={handleChange}
        className={rootClassName(state, cls)}
        aria-invalid={hasError || undefined}
      />
    </span>
  );
};

export default TLDatePicker;
