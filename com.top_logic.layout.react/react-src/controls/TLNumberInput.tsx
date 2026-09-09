import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/** Debounce for transmitting a typed value to the server (see TLTextInput). */
const VALUE_DEBOUNCE_MS = 300;

/**
 * A number input field rendered via React.
 *
 * The value is text: the server sends the number formatted in the user's locale and with the digits
 * the field asks for, and receives back the text exactly as typed. All number handling - the decimal
 * separator, the grouping separator, the number of digits - happens on the server, through the one
 * format that also writes the value into a table cell.
 *
 * Uses type="text" with the inputMode the server names in state.inputMode ('numeric', 'decimal' or
 * 'text', chosen from the field's format) so that invalid input (e.g. "foo") is actually sent to the
 * server for validation. With type="number", browsers silently discard input they do not read as a
 * number - which includes a locale decimal separator and the words of a duration - making
 * server-side error reporting impossible.
 *
 * Typing updates the local value immediately. Since the server rewrites the text it is given (12,5
 * comes back as 12,50), state.sendValueOnBlur holds the value back until the field is left, so a
 * mid-edit round-trip cannot re-render the input from the normalized text.
 */
const TLNumberInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue, flushValue] = useTLFieldValue({
    debounceMs: VALUE_DEBOUNCE_MS,
    sendOnBlur: state.sendValueOnBlur === true,
  });

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const raw = e.target.value;
      setValue(raw === '' ? null : raw);
    },
    [setValue]
  );

  const handleBlur = useCallback(() => { void flushValue(); }, [flushValue]);

  const text = value == null ? '' : String(value);

  if (state.editable === false) {
    return (
      <span id={controlId} className="tlReactNumberInput tlReactNumberInput--immutable">
        {text}
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
        inputMode={(state.inputMode as 'numeric' | 'decimal' | 'text' | undefined) ?? 'numeric'}
        value={text}
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
