import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A date picker field rendered via React.
 */
const TLDatePicker: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  if (state.editable === false) {
    return (
      <span id={controlId} className="tlReactDatePicker tlReactDatePicker--immutable">
        {(value as string) ?? ''}
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
        type="date"
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
      />
    </span>
  );
};

export default TLDatePicker;
