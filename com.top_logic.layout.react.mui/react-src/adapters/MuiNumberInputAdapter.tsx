import { React, useTLState, useFieldLabelProps, fieldInputId, rootClassName, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps, NumberInputStateJson } from 'tl-react-bridge';
import TextField from '@mui/material/TextField';
import { FIELD_ROOT_STYLE, MuiFieldValue, fieldAriaProps, fieldColor, showsValueOnly, useTypingField } from './field';

/** The on-screen keyboard of a number field whose state names none. */
const DEFAULT_INPUT_MODE: NumberInputStateJson.InputMode = 'numeric';

/**
 * Renders the state of a TopLogic number field (module name `TLNumberInput`) with the MUI
 * `TextField`.
 *
 * <p>The value is text, as in TLNumberInput: the server sends the number written in the user's
 * format and parses the text typed, so the adapter edits the text only. The input is of type
 * `text` with the `inputMode` of the state, so that a text the browser does not read as a number
 * still reaches the server's validation. An empty text is sent as `null`.</p>
 *
 * <p>The typing behaviour is the one of the `TypingFieldState` (see {@link useTypingField}).
 * Mapping from the control state: placeholder → placeholder; hasError → `error`, `aria-invalid` and
 * the error message as tooltip; hasWarnings → color `warning`; mandatory → `aria-required`; a field
 * that is not editable shows the text, as TLNumberInput does; disabled → `disabled`; hidden →
 * nothing is rendered; the configured CSS class → className of the `TextField`. The control ID is
 * on the element around the `TextField`, the input carries {@link fieldInputId} and is labelled by
 * the surrounding form field.</p>
 *
 * <p>Not used: label (rendered by the form field), tooltip and nullable.</p>
 */
const MuiNumberInputAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<NumberInputStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const { text, setText, onBlur, onSubmitKey } = useTypingField(null);

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return <MuiFieldValue id={controlId} className={rootClassName(state)} text={text} />;
  }

  const hasError = state.hasError === true;
  return (
    <span id={controlId} style={FIELD_ROOT_STYLE}>
      <TextField
        id={inputId}
        type="text"
        value={text}
        placeholder={state.placeholder}
        onChange={event => setText(event.target.value)}
        onBlur={onBlur}
        disabled={state.disabled === true}
        error={hasError}
        color={fieldColor(state)}
        size="small"
        fullWidth
        className={rootClassName(state)}
        slotProps={{
          htmlInput: {
            ...labelProps,
            ...fieldAriaProps(state),
            ...tooltipProps(hasError ? state.errorMessage : undefined),
            inputMode: state.inputMode ?? DEFAULT_INPUT_MODE,
            onKeyDown: onSubmitKey,
          },
        }}
      />
    </span>
  );
};

export default MuiNumberInputAdapter;
