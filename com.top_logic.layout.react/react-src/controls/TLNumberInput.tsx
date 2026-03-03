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

  return (
    <input
      type="number"
      id={controlId}
      value={value != null ? String(value) : ''}
      onChange={handleChange}
      step={step}
      disabled={state.disabled === true}
      className="tlReactNumberInput"
    />
  );
};

export default TLNumberInput;
