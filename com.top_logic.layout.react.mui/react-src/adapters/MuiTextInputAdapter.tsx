import {
  React, useTLState, useI18N, useFieldLabelProps, fieldInputId, rootClassName, tooltipProps, ThemeIcon,
} from 'tl-react-bridge';
import type { TLCellProps, TextInputStateJson } from 'tl-react-bridge';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import {
  FIELD_ROOT_STYLE, MuiFieldValue, fieldAriaProps, fieldColor, showsValueOnly, useTypingField,
} from './field';

const { useCallback, useRef } = React;

/** The labels of the buttons beside the input, with their English defaults. */
const I18N_KEYS = {
  'js.textInput.open': 'Open in a new tab',
  'js.textInput.clear': 'Clear the input',
};

/** The icon of the link that opens what the field holds (the theme image TLTextInput uses). */
const OPEN_ICON = 'css:fa-solid fa-arrow-up-right-from-square';

/** The icon of the button that empties the input (the theme image TLTextInput uses). */
const CLEAR_ICON = 'css:fa-solid fa-xmark';

/** Size class of the design system for an icon inside an input. */
const ICON_CLASS = 'tl-icon-sm';

/** The number of rows of a multi-line field whose state names none. */
const DEFAULT_ROWS = 3;

/** A text naming its scheme is an address a browser can follow on its own. */
const SCHEME = /^[A-Za-z][A-Za-z0-9+.-]*:/;

/**
 * The address a value of the given input type points at, or `null` where the type has no link or
 * the field is empty.
 */
function linkHref(inputType: string, value: string): string | null {
  if (value === '') return null;
  switch (inputType) {
    case 'url':
      return value;
    case 'email':
      return 'mailto:' + value;
    case 'tel':
      return 'tel:' + value.replace(/\s+/g, '');
    default:
      return null;
  }
}

/** A web address as it is stored: trimmed, and completed with `https` where a bare host was typed. */
function normalizeUrl(value: string): string {
  const trimmed = value.trim();
  if (trimmed === '' || SCHEME.test(trimmed)) return trimmed;
  return 'https://' + trimmed;
}

/**
 * Renders the state of a TopLogic text field (module name `TLTextInput`) with the MUI `TextField`.
 *
 * <p>The typing behaviour is that of TLTextInput (see {@link useTypingField}): debounce, send on
 * blur, commit on blur, submit on Enter. Mapping from the control state:</p>
 * <ul>
 * <li>value → the text; inputType → the `type` of the input; placeholder → placeholder;</li>
 * <li>multiline, rows → a multi-line `TextField` (Enter is text there, so nothing is submitted);</li>
 * <li>icon → a start adornment ({@link ThemeIcon}); clearable → an end adornment button emptying
 *     the input while it holds a value, sending the empty value at once; a `url`, `email` or `tel`
 *     value → an end adornment link opening it, while the clear button is not shown;</li>
 * <li>a `url` is completed to an `https` address when the field is left or submitted;</li>
 * <li>hasError → `error`, `aria-invalid` and the error message as tooltip; hasWarnings → color
 *     `warning`; mandatory → `aria-required`;</li>
 * <li>a field that is not editable shows its value as text (a link where it points somewhere), as
 *     TLTextInput does; disabled → `disabled`, without clear button and link;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `TextField`.</li>
 * </ul>
 *
 * <p>The control ID is on an element around the `TextField`, as TLTextInput has it, since the
 * `id` of a `TextField` names its input. The input carries {@link fieldInputId} and is labelled by
 * the surrounding form field ({@link useFieldLabelProps}); the MUI floating label is not used.</p>
 *
 * <p>Not used: label (rendered by the form field), tooltip and nullable.</p>
 */
const MuiTextInputAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TextInputStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const field = useTypingField('');
  const t = useI18N(I18N_KEYS);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const { text, setText, flush, onBlur, onSubmitKey } = field;
  const inputType = state.inputType ?? 'text';
  const multiline = state.multiline === true;

  const handleBlur = useCallback(async () => {
    if (inputType === 'url') {
      const normalized = normalizeUrl(text);
      if (normalized !== text) {
        setText(normalized);
      }
    }
    await onBlur();
  }, [inputType, text, setText, onBlur]);

  const handleKeyDown = useCallback((event: React.KeyboardEvent<HTMLInputElement>) => {
    if (onSubmitKey === undefined) return;
    if (inputType === 'url' && event.key === 'Enter') {
      const normalized = normalizeUrl(event.currentTarget.value);
      if (normalized !== event.currentTarget.value) {
        event.currentTarget.value = normalized;
        setText(normalized);
      }
    }
    onSubmitKey(event);
  }, [onSubmitKey, inputType, setText]);

  const handleClear = useCallback(async () => {
    setText('');
    // The gesture is the whole edit, so it is reported at once rather than after the debounce.
    await flush();
    inputRef.current?.focus();
  }, [setText, flush]);

  if (state.hidden === true) {
    return null;
  }

  const hasError = state.hasError === true;
  const disabled = state.disabled === true;
  const href = hasError || multiline ? null : linkHref(inputType, text);

  if (showsValueOnly(state)) {
    return (
      <MuiFieldValue id={controlId} className={rootClassName(state)} text={text} href={href}
        multiline={multiline} />
    );
  }

  const icon = state.icon;
  const hasIcon = !multiline && !!icon && icon !== 'none';
  const clearable = !multiline && !disabled && state.clearable === true && text !== '';
  const openHref = clearable || disabled ? null : href;

  const endAdornment = clearable
    ? (
      <InputAdornment position="end">
        <IconButton size="small" edge="end" onClick={handleClear} aria-label={t['js.textInput.clear']}
          {...tooltipProps(t['js.textInput.clear'])}>
          <ThemeIcon encoded={CLEAR_ICON} className={ICON_CLASS} />
        </IconButton>
      </InputAdornment>
    )
    : openHref !== null
      ? (
        <InputAdornment position="end">
          <IconButton size="small" edge="end" href={openHref} target="_blank" rel="noopener noreferrer"
            aria-label={t['js.textInput.open']} {...tooltipProps(text)}>
            <ThemeIcon encoded={OPEN_ICON} className={ICON_CLASS} />
          </IconButton>
        </InputAdornment>
      )
      : undefined;

  return (
    <span id={controlId} style={FIELD_ROOT_STYLE}>
      <TextField
        id={inputId}
        inputRef={inputRef}
        type={multiline ? undefined : inputType}
        multiline={multiline}
        rows={multiline ? state.rows ?? DEFAULT_ROWS : undefined}
        value={text}
        placeholder={state.placeholder}
        onChange={event => setText(event.target.value)}
        onBlur={handleBlur}
        disabled={disabled}
        error={hasError}
        color={fieldColor(state)}
        size="small"
        fullWidth
        className={rootClassName(state)}
        slotProps={{
          input: {
            startAdornment: hasIcon
              ? <InputAdornment position="start"><ThemeIcon encoded={icon} className={ICON_CLASS} /></InputAdornment>
              : undefined,
            endAdornment,
          },
          htmlInput: {
            ...labelProps,
            ...fieldAriaProps(state),
            // On the input itself: the handler reads the text from the element it is bound to.
            onKeyDown: multiline || onSubmitKey === undefined ? undefined : handleKeyDown,
            ...tooltipProps(hasError ? state.errorMessage : undefined),
          },
        }}
      />
    </span>
  );
};

export default MuiTextInputAdapter;
