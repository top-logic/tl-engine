import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A number input field rendered via React.
 */
const TLNumberInput: React.FC<TLCellProps> = ({ controlId, state, config }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const raw = e.target.value;
      const parsed = raw === '' ? null : Number(raw);
      setValue(parsed);
    },
    [setValue]
  );

  const step = config?.decimal ? '0.01' : '1';

  if (state.editable === false) {
    return (
      <span id={controlId} className="tlReactNumberInput tlReactNumberInput--immutable">
        {value != null ? String(value) : ''}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactNumberInput',
    hasError ? 'tlReactNumberInput--error' : '',
    !hasError && hasWarnings ? 'tlReactNumberInput--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <input
        type="number"
        value={value != null ? String(value) : ''}
        onChange={handleChange}
        step={step}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
      />
    </span>
  );
};

export default TLNumberInput;
