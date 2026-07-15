import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/** Debounce for transmitting a typed value to the server (see TLTextInput). */
const VALUE_DEBOUNCE_MS = 300;

/**
 * A number input field rendered via React.
 *
 * Uses type="text" with inputMode="decimal" instead of type="number" so that
 * invalid input (e.g. "foo") is actually sent to the server for validation.
 * With type="number", browsers silently discard non-numeric input and return
 * an empty string, making server-side error reporting impossible.
 *
 * Typing updates the local value immediately but the server `valueChanged` is debounced and
 * flushed on blur, so the field is not round-tripped on every keystroke.
 */
const TLNumberInput: React.FC<TLCellProps> = ({ controlId, state, config }) => {
  const [value, setValue, flushValue] = useTLFieldValue({ debounceMs: VALUE_DEBOUNCE_MS });

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const raw = e.target.value;
      setValue(raw === '' ? null : raw);
    },
    [setValue]
  );

  const handleBlur = useCallback(() => { void flushValue(); }, [flushValue]);

  if (state.editable === false) {
    return (
      <span id={controlId} className="tlReactNumberInput tlReactNumberInput--immutable">
        {value != null ? String(value) : ''}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const errorMessage = state.errorMessage as string | undefined;
  const cls = [
    'tlReactNumberInput',
    hasError ? 'tlReactNumberInput--error' : '',
    !hasError && hasWarnings ? 'tlReactNumberInput--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <input
        type="text"
        inputMode={config?.decimal ? 'decimal' : 'numeric'}
        value={value != null ? String(value) : ''}
        onChange={handleChange}
        onBlur={handleBlur}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
        title={hasError && errorMessage ? errorMessage : undefined}
      />
    </span>
  );
};

export default TLNumberInput;
