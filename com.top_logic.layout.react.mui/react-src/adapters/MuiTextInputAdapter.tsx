import { React, ThemeIcon, tooltipProps } from 'tl-react-bridge';
import type { TextInputPartProps } from 'tl-react-bridge';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';

/** Size class of the design system for an icon inside an input. */
const ICON_CLASS = 'tl-icon-sm';

/** The number of visible lines of a multi-line box that names none. */
const DEFAULT_ROWS = 3;

/**
 * Draws the part `TextInput` ({@link TextInputPartProps}) with Material UI: a small MUI `TextField`
 * of full width without a label of its own - the box is named by the control, and the label of a
 * form field is drawn by the form field.
 *
 * <p>Mapping from the props to the MUI props:</p>
 * <ul>
 * <li>value → `value`, onChange → `onChange` with the text of the input; type → `type`;
 *     placeholder → `placeholder`;</li>
 * <li>multiline, rows → a multi-line `TextField`, which takes no key handler;</li>
 * <li>icon → a start adornment ({@link ThemeIcon}); action → an end adornment;</li>
 * <li>disabled → `disabled`; readOnly → `readOnly` of the input;</li>
 * <li>validation → `error` (with `aria-invalid`) or color `warning`; required →
 *     `aria-required` - not `required`, which would mark a label the field does not have;</li>
 * <li>tooltip → the tooltip of the input;</li>
 * <li>id, ref, name, autoFocus, inputMode, maxLength, min, max, the key, focus and blur handlers
 *     and the passed-through attributes → the input; className → the root of the
 *     `TextField`.</li>
 * </ul>
 */
const MuiTextInputAdapter: React.FC<TextInputPartProps> = ({
  id, ref, value, type, inputMode, placeholder, multiline, rows, icon, action, disabled, readOnly, required,
  validation, tooltip, autoFocus, name, maxLength, min, max, className, onChange, onKeyDown, onFocus, onBlur,
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
          inputMode,
          maxLength,
          min,
          max,
          'aria-invalid': validation === 'error' ? true : undefined,
          'aria-required': required || undefined,
          // On the input itself: a handler reads the text from the element it is bound to.
          onKeyDown: multiline ? undefined : onKeyDown,
          ...tooltipProps(tooltip),
        },
      }}
    />
  );
};

export default MuiTextInputAdapter;
