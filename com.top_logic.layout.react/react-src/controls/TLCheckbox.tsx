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

  if (state.editable === false) {
    return (
      <input
        type="checkbox"
        checked={value === true}
        disabled
        className="tlReactCheckbox tlReactCheckbox--immutable"
      />
    );
  }

  return (
    <input
      type="checkbox"
      checked={value === true}
      onChange={handleChange}
      disabled={state.disabled === true}
      className="tlReactCheckbox"
    />
  );
};

export default TLCheckbox;
