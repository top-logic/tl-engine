import { React, ThemeIcon, tooltipProps } from 'tl-react-bridge';
import type { TextInputPartProps } from 'tl-react-bridge';
import { fieldStateAttrs } from './fieldState';

const { useCallback } = React;

/** The classes of the input of the design system. */
const FIELD_CLASS = 'tl-field tl-type-body';

/** The classes of the text area of the design system. */
const MULTILINE_CLASS = 'tl-field tl-field--multiline tl-type-body';

/** The class of the element around the input, which places icon and action inside its frame. */
const GROUP_CLASS = 'tl-field-group';

/** The class of the icon ahead of the text. */
const ICON_CLASS = 'tl-field-group__icon';

/** Size class of the design system for an icon inside an input. */
const ICON_SIZE_CLASS = 'tl-icon-sm';

/** The number of visible lines of a multi-line box that names none. */
const DEFAULT_ROWS = 3;

/** The classes, without the empty ones, joined. */
function classes(...names: (string | false | undefined)[]): string {
  return names.filter(Boolean).join(' ');
}

/**
 * The TopLogic drawing of the part `TextInput` ({@link TextInputPartProps}): the input of the design
 * system (tl-field), a text area (tl-field--multiline) for a multi-line box.
 *
 * <p>The input stands in the group of the design system (tl-field-group), which carries the
 * classes of the box: the icon (tl-field-group__icon) ahead of the input and the action after it
 * lie inside the frame of the input, which makes room for them. The group is drawn whether or not
 * the box has an icon or an action, so that the input keeps its place in the tree - and with it the
 * focus - while they come and go.</p>
 *
 * <p>The design system reads the state from the input itself: `:disabled`, `aria-invalid`,
 * `aria-required` and `data-tl-state` (see fieldStateAttrs).</p>
 */
const TextInputPart: React.FC<TextInputPartProps> = ({
  id, ref, value, type, inputMode, placeholder, multiline, rows, icon, action, disabled, readOnly, required,
  validation, tooltip, autoFocus, name, maxLength, min, max, className, onChange, onKeyDown, onFocus, onBlur,
  ...attributes
}) => {
  const handleChange = useCallback((event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    if (readOnly || disabled) return;
    onChange?.(event.target.value);
  }, [readOnly, disabled, onChange]);

  const common = {
    id,
    name,
    value,
    placeholder,
    disabled,
    readOnly,
    autoFocus,
    maxLength,
    inputMode,
    onChange: handleChange,
    onFocus,
    onBlur,
    ...fieldStateAttrs({
      hasError: validation === 'error',
      hasWarnings: validation === 'warning',
      mandatory: required,
    }),
    ...tooltipProps(tooltip),
    ...attributes,
  };

  if (multiline) {
    return (
      <span className={classes(GROUP_CLASS, className)}>
        <textarea
          ref={ref as React.Ref<HTMLTextAreaElement>}
          rows={rows ?? DEFAULT_ROWS}
          className={MULTILINE_CLASS}
          {...common}
        />
      </span>
    );
  }

  return (
    <span className={classes(GROUP_CLASS, className)}>
      {icon && (
        <span className={ICON_CLASS} aria-hidden="true">
          <ThemeIcon encoded={icon} className={ICON_SIZE_CLASS} />
        </span>
      )}
      <input
        ref={ref as React.Ref<HTMLInputElement>}
        type={type ?? 'text'}
        min={min}
        max={max}
        className={FIELD_CLASS}
        onKeyDown={onKeyDown}
        {...common}
      />
      {action}
    </span>
  );
};

export default TextInputPart;
