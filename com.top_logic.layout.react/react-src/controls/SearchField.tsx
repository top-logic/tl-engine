import { React, useI18N, useMergeRefs, tooltipProps, ThemeIcon, ICON_SEARCH, ICON_CLEAR } from 'tl-react-bridge';
import { buttonClassName } from './button/ButtonDefaults';

const I18N_KEYS = {
  'js.textInput.clear': 'Clear the input',
};

/**
 * The one search field of the controls that narrow what they show by a typed term (design system:
 * tl-field-group): the table's search across its columns and the search of the column selection.
 *
 * A `tl-field-group` holding the magnifier, an `input.tl-field type="search"` and, while text stands
 * in it, the button clearing it (`tl-button--ghost --sm --icon`). The button replaces the browser's
 * own clear affordance (field.css hides it beside a button), and once it has vanished with the text
 * the focus is back in the field (WCAG 2.4.3).
 *
 * The search of the select popup is no such field: it is part of the combobox pattern and stays a
 * text input there.
 */
export function SearchField({
  value,
  onChange,
  onClear,
  onKeyDown,
  placeholder,
  label,
  tooltip,
  className,
  inputRef,
}: {
  /** The typed term. */
  value: string;
  /** Called with the term on every change of the input. */
  onChange: (value: string) => void;
  /** Called when the clear button empties the field; without it, the field reports the empty term
   *  through `onChange`. */
  onClear?: () => void;
  onKeyDown?: (event: React.KeyboardEvent<HTMLInputElement>) => void;
  placeholder?: string;
  /** The name of the input (`aria-label`). */
  label: string;
  /** A tooltip on the whole group, when the field says more than its name. */
  tooltip?: string;
  /** The class placing the group in its control (e.g. `tl-table__search`). */
  className?: string;
  /** A ref to the input, for a control that moves the focus into it. */
  inputRef?: React.Ref<HTMLInputElement>;
}) {
  const i18n = useI18N(I18N_KEYS);
  const ownRef = React.useRef<HTMLInputElement | null>(null);
  const setInput = useMergeRefs([ownRef, inputRef]);

  const handleClear = React.useCallback(() => {
    if (onClear) {
      onClear();
    } else {
      onChange('');
    }
    // The button vanishes with the text; the focus stays in the search.
    ownRef.current?.focus();
  }, [onClear, onChange]);

  return (
    <span className={['tl-field-group', className].filter(Boolean).join(' ')} {...(tooltip ? tooltipProps(tooltip) : {})}>
      <span className="tl-field-group__icon" aria-hidden="true">
        <ThemeIcon encoded={ICON_SEARCH} className="tl-icon-sm" />
      </span>
      <input
        ref={setInput}
        type="search"
        className="tl-field tl-type-body"
        placeholder={placeholder}
        aria-label={label}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={onKeyDown}
      />
      {value !== '' && (
        <button
          type="button"
          className={buttonClassName({ appearance: 'ghost', small: true, icon: true })}
          aria-label={i18n['js.textInput.clear']}
          {...tooltipProps(i18n['js.textInput.clear'])}
          onClick={handleClear}
        >
          <ThemeIcon encoded={ICON_CLEAR} className="tl-button__icon tl-icon-sm" />
        </button>
      )}
    </span>
  );
}
