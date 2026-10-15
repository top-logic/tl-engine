import { React, useI18N, useMergeRefs, tooltipProps, usePart, ICON_SEARCH, ICON_CLEAR } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.textInput.clear': 'Clear the input',
};

/**
 * The one search field of the controls that narrow what they show by a typed term (design system:
 * tl-field-group): the table's search across its columns and the search of the column selection.
 *
 * The part `TextInput` of the type `search` with the magnifier as its icon and, while text stands in
 * it, the part `Button` clearing it as its action. The button replaces the browser's own clear
 * affordance (field.css hides it beside a button), and once it has vanished with the text the focus
 * is back in the field (WCAG 2.4.3). An element of its own around the part places it in its control
 * and carries the tooltip.
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
  /** The class placing the field in its control (e.g. `tl-table__search`). */
  className?: string;
  /** A ref to the input, for a control that moves the focus into it. */
  inputRef?: React.Ref<HTMLInputElement>;
}) {
  const i18n = useI18N(I18N_KEYS);
  const TextInput = usePart('TextInput');
  const Button = usePart('Button');
  const ownRef = React.useRef<HTMLInputElement | null>(null);
  const setInput = useMergeRefs<HTMLInputElement | HTMLTextAreaElement>([ownRef, inputRef]);

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
    <div className={className} {...tooltipProps(tooltip)}>
      <TextInput
        ref={setInput}
        type="search"
        icon={ICON_SEARCH}
        placeholder={placeholder}
        aria-label={label}
        value={value}
        onChange={onChange}
        onKeyDown={onKeyDown}
        action={value !== '' && (
          <Button
            label={i18n['js.textInput.clear']}
            image={ICON_CLEAR}
            display="icon-only"
            appearance="ghost"
            small
            onClick={handleClear}
          />
        )}
      />
    </div>
  );
}
