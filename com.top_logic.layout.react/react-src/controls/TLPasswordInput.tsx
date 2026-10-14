import { React, useTLState, useTLTypingField, rootClassName, useFieldLabelProps, fieldInputId, usePart } from 'tl-react-bridge';
import type { TLCellProps, PasswordInputStateJson } from 'tl-react-bridge';
import { showsValueOnly, validationOf } from './form/fieldState';

/** What a read-only password field shows in place of the password. */
const MASK = '••••••••';

/**
 * A masked password input field rendered via React, drawn by the part `TextInput` of type
 * `password`.
 *
 * Typing updates the local value immediately while the server `valueChanged` is debounced and
 * flushed on blur. state.debounceMs names the span the value is held back, defaulting to
 * VALUE_DEBOUNCE_MS. With state.commitEdits each finished edit - leaving the field or pressing
 * Enter after a change - sends a 'commit' command once; with state.submitOnEnter Enter sends a
 * 'submit' command carrying the password. The typing behaviour is that of useTLTypingField.
 *
 * The state of the value is the validation of the box (see validationOf), a mandatory field a
 * required box, and the message of an error the tooltip of the input. A read-only field shows a
 * mask in place of the value (part `FieldValue`); a disabled field renders the input as an inactive
 * one (see showsValueOnly). A hidden field renders nothing.
 */
const TLPasswordInput: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<PasswordInputStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const { text, setText, onBlur, onKeyDown } = useTLTypingField();
  const TextInput = usePart('TextInput');
  const FieldValue = usePart('FieldValue');

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return <FieldValue id={controlId} className={rootClassName(state) || undefined} text={MASK} />;
  }

  return (
    <span id={controlId}>
      <TextInput
        id={inputId}
        {...labelProps}
        type="password"
        value={text}
        placeholder={state.placeholder}
        disabled={state.disabled === true}
        required={state.mandatory === true}
        validation={validationOf(state)}
        tooltip={state.hasError === true ? state.errorMessage : undefined}
        className={rootClassName(state) || undefined}
        onChange={setText}
        onBlur={onBlur}
        onKeyDown={onKeyDown}
      />
    </span>
  );
};

export default TLPasswordInput;
