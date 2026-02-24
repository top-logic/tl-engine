import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

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
