import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A text input field rendered via React. Renders a single-line input by default, or a multi-line
 * text area when the field state requests it (state.multiline with state.rows).
 */
const TLTextInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setValue(e.target.value);
    },
    [setValue]
  );

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
