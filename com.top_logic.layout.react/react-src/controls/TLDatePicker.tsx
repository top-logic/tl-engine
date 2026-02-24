import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

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
