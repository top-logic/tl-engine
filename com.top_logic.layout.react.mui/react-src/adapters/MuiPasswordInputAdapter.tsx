import { React, useTLState, useFieldLabelProps, fieldInputId, rootClassName, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps, PasswordInputStateJson } from 'tl-react-bridge';
import TextField from '@mui/material/TextField';
import { FIELD_ROOT_STYLE, MuiFieldValue, fieldAriaProps, fieldColor, showsValueOnly, useTypingField } from './field';

/** What a read-only password field shows in place of the password. */
const MASK = '••••••••';

/**
 * Renders the state of a TopLogic password field (module name `TLPasswordInput`) with the MUI
 * `TextField` of type `password`.
 *
 * <p>The typing behaviour is the one of the `TypingFieldState` (see {@link useTypingField}).
 * Mapping from the control state: value → the password; placeholder → placeholder; hasError →
 * `error`, `aria-invalid` and the error message as tooltip; hasWarnings → color `warning`;
 * mandatory → `aria-required`; a field that is not editable shows a mask instead of the password,
 * as TLPasswordInput does; disabled → `disabled`; hidden → nothing is rendered; the configured CSS
 * class → className of the `TextField`. The control ID is on the element around the `TextField`,
 * the input carries {@link fieldInputId} and is labelled by the surrounding form field.</p>
 *
 * <p>No button revealing the password: TLPasswordInput offers none. Not used: label (rendered by
 * the form field), tooltip and nullable.</p>
 */
const MuiPasswordInputAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<PasswordInputStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const { text, setText, onBlur, onSubmitKey } = useTypingField('');

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return <MuiFieldValue id={controlId} className={rootClassName(state)} text={MASK} />;
  }

  const hasError = state.hasError === true;
  return (
    <span id={controlId} style={FIELD_ROOT_STYLE}>
      <TextField
        id={inputId}
        type="password"
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
            onKeyDown: onSubmitKey,
          },
        }}
      />
    </span>
  );
};

export default MuiPasswordInputAdapter;
