import { React, ThemeIcon, tooltipProps } from 'tl-react-bridge';
import type { TextInputPartProps } from 'tl-react-bridge';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';

/** Size class of the design system for an icon inside an input. */
const ICON_CLASS = 'tl-icon-sm';

/** The number of visible lines of a multi-line box that names none. */
const DEFAULT_ROWS = 3;

/**
 * The style of a text area showing its text as it is written: the code font of the design system,
 * lines not wrapped, a wide line scrolling horizontally.
 */
const PREFORMATTED_SX = {
  '& textarea': {
    fontFamily: 'var(--tl-font-family-mono)', whiteSpace: 'pre', overflowWrap: 'normal', overflowX: 'auto',
  },
} as const;

/** Hides the browser's own clear button of a search box, which the action of the box replaces. */
const NO_NATIVE_CLEAR_SX = { '& input::-webkit-search-cancel-button': { display: 'none' } } as const;

/**
 * Draws the part `TextInput` ({@link TextInputPartProps}) with Material UI: a small MUI `TextField`
 * of full width without a label of its own - the box is named by the control, and the label of a
 * form field is drawn by the form field.
 *
 * <p>Mapping from the props to the MUI props:</p>
 * <ul>
 * <li>value → `value`, onChange → `onChange` with the text of the input; type → `type`;
 *     placeholder → `placeholder`;</li>
 * <li>multiline, rows → a multi-line `TextField`; preformatted → its text area in the code font of
 *     the design system, without wrapping its lines (`wrap="off"`);</li>
 * <li>icon → a start adornment ({@link ThemeIcon}); action → an end adornment;</li>
 * <li>disabled → `disabled`; readOnly → `readOnly` of the input;</li>
 * <li>validation → `error` (with `aria-invalid`) or color `warning`; required →
 *     `aria-required` - not `required`, which would mark a label the field does not have;</li>
 * <li>tooltip → the tooltip of the input;</li>
 * <li>id, ref, role, name, autoFocus, inputMode, maxLength, min, max, the key, focus and blur
 *     handlers and the passed-through attributes (with the combobox attributes of a box offering
 *     suggestions) → the input or text area; className → the root of the `TextField`.</li>
 * </ul>
 */
const MuiTextInputAdapter: React.FC<TextInputPartProps> = ({
  id, ref, role, value, type, inputMode, placeholder, multiline, rows, preformatted, icon, action, disabled, readOnly,
  required, validation, tooltip, autoFocus, name, maxLength, min, max, className, onChange, onKeyDown, onFocus, onBlur,
  ...attributes
}) => {
  const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    if (readOnly || disabled) return;
    onChange?.(event.target.value);
  };

  return (
    <TextField
      id={id}
      inputRef={ref}
      name={name}
      type={multiline ? undefined : type}
      multiline={multiline}
      rows={multiline ? rows ?? DEFAULT_ROWS : undefined}
      value={value}
      placeholder={placeholder}
      onChange={handleChange}
      onFocus={onFocus}
      onBlur={onBlur}
      disabled={disabled}
      autoFocus={autoFocus}
      error={validation === 'error'}
      color={validation === 'warning' ? 'warning' : 'primary'}
      size="small"
      fullWidth
      className={className}
      sx={!multiline && action ? NO_NATIVE_CLEAR_SX : multiline && preformatted ? PREFORMATTED_SX : undefined}
      slotProps={{
        input: {
          readOnly,
          startAdornment: !multiline && icon
            ? <InputAdornment position="start"><ThemeIcon encoded={icon} className={ICON_CLASS} /></InputAdornment>
            : undefined,
          endAdornment: !multiline && action
            ? <InputAdornment position="end">{action}</InputAdornment>
            : undefined,
        },
        htmlInput: {
          ...attributes,
          role,
          inputMode,
          wrap: multiline && preformatted ? 'off' : undefined,
          maxLength,
          min,
          max,
          'aria-invalid': validation === 'error' ? true : undefined,
          'aria-required': required || undefined,
          // On the input itself: a handler reads the text and the caret from the element it is bound to.
          onKeyDown,
          ...tooltipProps(tooltip),
        },
      }}
    />
  );
};

export default MuiTextInputAdapter;
