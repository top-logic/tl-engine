import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A checkbox field rendered via React.
 */
const TLCheckbox: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.checked);
    },
    [setValue]
  );

  if (state.editable === false) {
    return (
      <input
        type="checkbox"
        id={controlId}
        checked={value === true}
        disabled
        className="tlReactCheckbox tlReactCheckbox--immutable"
      />
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactCheckbox',
    hasError ? 'tlReactCheckbox--error' : '',
    !hasError && hasWarnings ? 'tlReactCheckbox--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <input
      type="checkbox"
      id={controlId}
      checked={value === true}
      onChange={handleChange}
      disabled={state.disabled === true}
      className={cls}
      aria-invalid={hasError || undefined}
    />
  );
};

export default TLCheckbox;
