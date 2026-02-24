import React, { useCallback } from 'react';
import type { TLCellProps } from 'tl-react-bridge';
import { useTLFieldValue } from 'tl-react-bridge';

interface SelectOption {
  value: string;
  label: string;
}

/**
 * A select dropdown rendered via React.
 */
const TLSelect: React.FC<TLCellProps> = ({ state, config }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLSelectElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  const options = (config?.options as SelectOption[]) ?? [];

  return (
    <select
      value={(value as string) ?? ''}
      onChange={handleChange}
      disabled={state.disabled === true || state.editable === false}
      className="tlReactSelect"
    >
      <option value=""></option>
      {options.map((opt) => (
        <option key={opt.value} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>
  );
};

export default TLSelect;
