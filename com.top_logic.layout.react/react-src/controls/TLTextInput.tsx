import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

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

  if (state.editable === false) {
    return (
      <span className="tlReactTextInput tlReactTextInput--immutable">
        {(value as string) ?? ''}
      </span>
    );
  }

  return (
    <input
      type="text"
      value={(value as string) ?? ''}
      onChange={handleChange}
      disabled={state.disabled === true}
      className="tlReactTextInput"
    />
  );
};

export default TLTextInput;
