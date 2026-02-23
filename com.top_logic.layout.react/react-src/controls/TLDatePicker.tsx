import React, { useCallback } from 'react';
import type { TLCellProps } from '../bridge/types';
import { useTLFieldValue } from '../bridge/tl-react-bridge';

/**
 * A date picker field rendered via React.
 */
const TLDatePicker: React.FC<TLCellProps> = ({ state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  return (
    <input
      type="date"
      value={(value as string) ?? ''}
      onChange={handleChange}
      disabled={state.disabled === true || state.editable === false}
      className="tlReactDatePicker"
    />
  );
};

export default TLDatePicker;
