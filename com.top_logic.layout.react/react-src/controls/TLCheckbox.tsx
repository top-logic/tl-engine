import React, { useCallback } from 'react';
import type { TLCellProps } from '../bridge/types';
import { useTLFieldValue } from '../bridge/tl-react-bridge';

/**
 * A checkbox field rendered via React.
 */
const TLCheckbox: React.FC<TLCellProps> = ({ state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.checked);
    },
    [setValue]
  );

  return (
    <input
      type="checkbox"
      checked={value === true}
      onChange={handleChange}
      disabled={state.disabled === true || state.editable === false}
      className="tlReactCheckbox"
    />
  );
};

export default TLCheckbox;
