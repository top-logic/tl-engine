import React, { useCallback } from 'react';
import type { TLCellProps } from 'tl-react-bridge';
import { useTLFieldValue } from 'tl-react-bridge';

/**
 * A number input field rendered via React.
 */
const TLNumberInput: React.FC<TLCellProps> = ({ state, config }) => {
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

  return (
    <input
      type="number"
      value={value != null ? String(value) : ''}
      onChange={handleChange}
      step={step}
      disabled={state.disabled === true || state.editable === false}
      className="tlReactNumberInput"
    />
  );
};

export default TLNumberInput;
