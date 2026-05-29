import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A masked password input field rendered via React.
 *
 * Mirrors {@link TLTextInput} but renders an {@code <input type="password">}.
 */
const TLPasswordInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value);
    },
    [setValue]
  );

  if (state.editable === false) {
    return <span id={controlId} className="tlReactTextInput tlReactTextInput--immutable">••••••••</span>;
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const errorMessage = state.errorMessage as string | undefined;
  const cls = [
    'tlReactTextInput',
    hasError ? 'tlReactTextInput--error' : '',
    !hasError && hasWarnings ? 'tlReactTextInput--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <input
        type="password"
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
        title={hasError && errorMessage ? errorMessage : undefined}
      />
    </span>
  );
};

export default TLPasswordInput;
