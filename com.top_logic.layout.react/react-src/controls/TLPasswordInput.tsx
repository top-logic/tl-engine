import { React, useTLFieldValue, rootClassName, VALUE_DEBOUNCE_MS, tooltipProps, useFieldLabelProps, fieldInputId } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { fieldStateAttrs } from './form/fieldState';
import { FieldValue } from './form/FieldValue';

const { useCallback } = React;

/**
 * A masked password input field rendered via React.
 *
 * Mirrors {@link TLTextInput} but renders an {@code <input type="password">}: typing updates the
 * local value immediately while the server `valueChanged` is debounced and flushed on blur.
 * state.debounceMs names the span the value is held back, defaulting to VALUE_DEBOUNCE_MS.
 */
const TLPasswordInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
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
    return <FieldValue id={controlId} className={rootClassName(state)} text="••••••••" />;
  }

  const hasError = state.hasError === true;
  const errorMessage = state.errorMessage as string | undefined;

  return (
    <span id={controlId}>
      <input
        type="password"
        value={(value as string) ?? ''}
        onChange={handleChange}
        onBlur={handleBlur}
        disabled={state.disabled === true}
        className={rootClassName(state, 'tl-field tl-type-body')}
        {...fieldStateAttrs(state)}
        {...tooltipProps(hasError ? errorMessage : undefined)}
        id={inputId}
        {...labelProps}
      />
    </span>
  );
};

export default TLPasswordInput;
