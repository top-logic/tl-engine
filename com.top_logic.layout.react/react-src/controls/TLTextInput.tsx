import { React, useTLFieldValue, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef } = React;

/**
 * Debounce for transmitting a typed value to the server: long enough to coalesce a burst of
 * keystrokes into one round-trip, short enough that server-side validation can surface while the
 * user pauses (and the final value is always sent on blur). See {@link handleBlur}.
 */
const VALUE_DEBOUNCE_MS = 300;

/**
 * A text input field rendered via React. Renders a single-line input by default, or a multi-line
 * text area when the field state requests it (state.multiline with state.rows).
 *
 * Typing updates the local value immediately but the server `valueChanged` is debounced (and
 * always flushed on blur), so a field is not round-tripped on every keystroke. When
 * state.commitOnBlur is set, losing focus after an actual edit also sends a 'commit' command so the
 * server can run deferred per-field work (e.g. i18n auto-translation) once.
 */
const TLTextInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue, flushValue] = useTLFieldValue({ debounceMs: VALUE_DEBOUNCE_MS });
  const sendCommand = useTLCommand();
  const dirtyRef = useRef(false);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      dirtyRef.current = true;
      setValue(e.target.value);
    },
    [setValue]
  );

  const commitOnBlur = state.commitOnBlur === true;
  const handleBlur = useCallback(async () => {
    // Always send the final value when leaving the field, even if a keystroke is still within the
    // debounce window; await it so a follow-up 'commit' runs after the value is applied server-side.
    await flushValue();
    if (commitOnBlur && dirtyRef.current) {
      dirtyRef.current = false;
      sendCommand('commit');
    }
  }, [flushValue, commitOnBlur, sendCommand]);

  const multiline = state.multiline === true;

  if (state.editable === false) {
    const immutableCls =
      'tlReactTextInput tlReactTextInput--immutable' +
      (multiline ? ' tlReactTextInput--multiline' : '');
    return (
      <span
        id={controlId}
        className={immutableCls}
        style={multiline ? { whiteSpace: 'pre-wrap' } : undefined}
      >
        {(value as string) ?? ''}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const errorMessage = state.errorMessage as string | undefined;
  const cls = [
    'tlReactTextInput',
    multiline ? 'tlReactTextInput--multiline' : '',
    hasError ? 'tlReactTextInput--error' : '',
    !hasError && hasWarnings ? 'tlReactTextInput--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      {multiline ? (
        <textarea
          rows={(state.rows as number) ?? 3}
          value={(value as string) ?? ''}
          placeholder={(state.placeholder as string) ?? undefined}
          onChange={handleChange}
          onBlur={handleBlur}
          disabled={state.disabled === true}
          className={cls}
          aria-invalid={hasError || undefined}
          title={hasError && errorMessage ? errorMessage : undefined}
        />
      ) : (
        <input
          type="text"
          value={(value as string) ?? ''}
          placeholder={(state.placeholder as string) ?? undefined}
          onChange={handleChange}
          onBlur={handleBlur}
          disabled={state.disabled === true}
          className={cls}
          aria-invalid={hasError || undefined}
          title={hasError && errorMessage ? errorMessage : undefined}
        />
      )}
    </span>
  );
};

export default TLTextInput;
