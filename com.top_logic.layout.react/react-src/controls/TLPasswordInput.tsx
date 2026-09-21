import { React, useTLFieldValue, rootClassName, VALUE_DEBOUNCE_MS } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A masked password input field rendered via React.
 *
 * Mirrors {@link TLTextInput} but renders an {@code <input type="password">}: typing updates the
 * local value immediately while the server `valueChanged` is debounced and flushed on blur.
 * state.debounceMs names the span the value is held back, defaulting to VALUE_DEBOUNCE_MS.
 */
const TLPasswordInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue, flushValue] = useTLFieldValue({
    debounceMs: (state.debounceMs as number) ?? VALUE_DEBOUNCE_MS,
  });

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setValue(e.target.value);
    },
    [setValue]
  );

  const handleBlur = useCallback(() => { void flushValue(); }, [flushValue]);

  if (state.editable === false) {
    return <span id={controlId} className={rootClassName(state, 'tlReactTextInput tlReactTextInput--immutable')}>••••••••</span>;
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
        onBlur={handleBlur}
        disabled={state.disabled === true}
        className={rootClassName(state, cls)}
        aria-invalid={hasError || undefined}
        title={hasError && errorMessage ? errorMessage : undefined}
      />
    </span>
  );
};

export default TLPasswordInput;
