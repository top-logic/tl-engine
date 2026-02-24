import React, { useCallback } from 'react';
import type { TLCellProps } from 'tl-react-bridge';
import { useTLFieldValue } from 'tl-react-bridge';

/**
 * A text input field rendered via React.
 */
const TLTextInput: React.FC<TLCellProps> = ({ state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value);
    },
    [setValue]
  );

  return (
    <input
      type="text"
      value={(value as string) ?? ''}
      onChange={handleChange}
      disabled={state.disabled === true || state.editable === false}
      className="tlReactTextInput"
    />
  );
};

export default TLTextInput;
