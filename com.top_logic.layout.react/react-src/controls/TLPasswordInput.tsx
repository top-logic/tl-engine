import { React, useTLState, useTLTypingField, rootClassName, tooltipProps, useFieldLabelProps, fieldInputId } from 'tl-react-bridge';
import type { TLCellProps, PasswordInputStateJson } from 'tl-react-bridge';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import { FieldValue } from './form/FieldValue';

/**
 * A masked password input field rendered via React.
 *
 * Mirrors {@link TLTextInput} but renders an {@code <input type="password">}: typing updates the
 * local value immediately while the server `valueChanged` is debounced and flushed on blur.
 * state.debounceMs names the span the value is held back, defaulting to VALUE_DEBOUNCE_MS. With
 * state.commitEdits each finished edit - leaving the field or pressing Enter after a change - sends
 * a 'commit' command once; with state.submitOnEnter Enter sends a 'submit' command carrying the
 * password. The typing behaviour is that of useTLTypingField.
 *
 * A read-only field shows a mask in place of the value (tl-field-value); a disabled field renders
 * the input as an inactive one (native `disabled`, see showsValueOnly).
 */
const TLPasswordInput: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<PasswordInputStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const { text, setText, onBlur, onKeyDown } = useTLTypingField();

  if (showsValueOnly(state)) {
    return <FieldValue id={controlId} className={rootClassName(state)} text="••••••••" />;
  }

  const hasError = state.hasError === true;
  const errorMessage = state.errorMessage;

  return (
    <span id={controlId}>
      <input
        type="password"
        value={text}
        onChange={e => setText(e.target.value)}
        onBlur={onBlur}
        onKeyDown={onKeyDown}
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
