import {
  React,
  useTLState,
  useTLTypingField,
  rootClassName,
  useFieldLabelProps,
  fieldInputId,
  usePart,
} from 'tl-react-bridge';
import type { TLCellProps, NumberInputStateJson } from 'tl-react-bridge';
import { showsValueOnly, validationOf } from './form/fieldState';

/**
 * A number input field rendered via React, drawn by the part `TextInput`.
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
 *
 * When state.commitEdits is set, each finished edit - leaving the field or pressing Enter after a
 * change - sends a 'commit' command once. When state.submitOnEnter is set, Enter sends a 'submit'
 * command carrying the text, so the server can run a command over the number that was entered. An
 * empty text is sent as no value. The typing behaviour is that of useTLTypingField.
 *
 * state.placeholder holds the text shown while the field is empty, stating what the field is for
 * where no label does. state.debounceMs names the span a typed value is held back before it is
 * sent, defaulting to VALUE_DEBOUNCE_MS; state.sendValueOnBlur overrides it, and this field sets
 * it, so the span matters here only where the server turns the blur behaviour off.
 *
 * The state of the value is the validation of the box (see validationOf), a mandatory field a
 * required box, and the message of an error the tooltip of the input. A read-only field shows the
 * text, drawn by the part `FieldValue`; a disabled field renders the input as an inactive one (see
 * showsValueOnly). A hidden field renders nothing.
 */
const TLNumberInput: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<NumberInputStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const { text, setText, onBlur, onKeyDown } = useTLTypingField({ emptyValue: null });
  const TextInput = usePart('TextInput');
  const FieldValue = usePart('FieldValue');

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return <FieldValue id={controlId} className={rootClassName(state) || undefined} text={text} />;
  }

  return (
    <span id={controlId}>
      <TextInput
        id={inputId}
        {...labelProps}
        type="text"
        inputMode={state.inputMode ?? 'numeric'}
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

export default TLNumberInput;
