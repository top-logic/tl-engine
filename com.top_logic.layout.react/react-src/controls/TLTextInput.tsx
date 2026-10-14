import {
  React,
  useTLState,
  useTLTypingField,
  useI18N,
  rootClassName,
  tooltipProps,
  useFieldLabelProps,
  fieldInputId,
  ThemeIcon,
  usePart,
} from 'tl-react-bridge';
import type { TLCellProps, TextInputStateJson } from 'tl-react-bridge';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import { FieldValue } from './form/FieldValue';

const { useCallback, useRef } = React;

const I18N_KEYS = {
  'js.textInput.open': 'Open in a new tab',
  'js.textInput.clear': 'Clear the input',
};

/** The icon of the link that opens what the field holds. */
const OPEN_ICON = 'css:fa-solid fa-arrow-up-right-from-square';

/** The icon of the button that empties the input. */
const CLEAR_ICON = 'css:fa-solid fa-xmark';

/** A text naming its scheme is an address a browser can follow on its own. */
const SCHEME = /^[A-Za-z][A-Za-z0-9+.-]*:/;

/**
 * The address a value of the given input type points at, or `null` where the type has no link or
 * the field is empty.
 */
const linkHref = (inputType: string, value: string): string | null => {
  if (value === '') return null;
  switch (inputType) {
    case 'url':
      return value;
    case 'email':
      return 'mailto:' + value;
    case 'tel':
      // A phone number is written with spaces for legibility; the dialer takes the digits.
      return 'tel:' + value.replace(/\s+/g, '');
    default:
      return null;
  }
};

/**
 * A web address as it is stored: trimmed, and completed with the `https` scheme where the user
 * typed a bare host. A text that names its own scheme is kept as typed.
 */
const normalizeUrl = (value: string): string => {
  const trimmed = value.trim();
  if (trimmed === '' || SCHEME.test(trimmed)) return trimmed;
  return 'https://' + trimmed;
};

/**
 * A text input field rendered via React. Renders a single-line input by default, or a multi-line
 * text area when the field state requests it (state.multiline with state.rows).
 *
 * Typing updates the local value immediately but the server `valueChanged` is debounced (and
 * always flushed on blur), so a field is not round-tripped on every keystroke. When
 * state.sendValueOnBlur is set, nothing is sent while the user is still in the field at all - for a
 * field whose model rewrites the text it is given, where any mid-edit round-trip would re-render
 * the field from the normalized value and throw away what was being typed. When state.commitEdits
 * is set, each finished edit - leaving the field after a change, or Enter in the single-line input
 * after a change - sends a 'commit' command once, so the server can run per-edit work (e.g. i18n
 * auto-translation, a command bound to the field). When state.submitOnEnter is set, Enter in the
 * single-line input sends a 'submit' command carrying the text, so the server can run a command
 * over what was entered; a text area takes no Enter handler, where Enter is text and the edit is
 * finished by leaving the field only. The typing behaviour is that of useTLTypingField.
 *
 * state.inputType says what the text means and becomes the `type` of the single-line input
 * ('text' when absent; a text area has no type). A value of a type that points somewhere - 'url'
 * at the address itself, 'email' at a `mailto:` address, 'tel' at a `tel:` number without its
 * spaces - is offered as a link: as the displayed text while the field is read-only, and as an
 * icon beside the input while it is edited, so that what is being typed can be opened. There is no
 * link while the field is empty or holds a value the server rejected. The link comes and goes
 * beside an input that keeps its place: the control's root element carries the tl-field-group class
 * while the input has an icon or an action, so that gaining or losing the link leaves the input
 * itself in place and typing keeps the focus.
 *
 * A 'url' is completed to an `https` address when the field is left and when it is submitted with
 * Enter, so that a typed bare host is stored as an address a browser can follow; a value naming its
 * own scheme ('mailto:', 'ftp:') is left as typed. The server holds such a field back until it is
 * left (state.sendValueOnBlur), so that the half-typed address in between is never judged.
 *
 * Three further states turn the single-line input into a search field. state.icon draws a
 * ThemeImage inside the input ahead of what is typed - the magnifier of a search box - as
 * decoration hidden from assistive technology, so the field is still named by its label or its
 * placeholder. state.clearable adds a button that empties the input, shown only while the input
 * holds something, which writes the empty value at once instead of after the debounce and hands
 * the focus back to the input. state.debounceMs names the span a typed value is held back,
 * defaulting to VALUE_DEBOUNCE_MS and overridden by state.sendValueOnBlur, which holds a value
 * back entirely. Icon and clear button live in the same group as the link that opens what the
 * field holds, in the order [icon] input [action]; the group has room for one action, so the clear
 * button, while it is shown, takes the place of the link.
 *
 * The state is carried as attributes (fieldStateAttrs): aria-invalid for an error,
 * data-tl-state="warning" for a warning, aria-required for a mandatory field. A read-only field
 * renders no input but the value as text (tl-field-value), one line per line of a multi-line text.
 * A disabled field renders the input as an inactive one (native `disabled`), without the clear
 * button and without the link (see showsValueOnly).
 */
const TLTextInput: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TextInputStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const inputType = state.inputType ?? 'text';
  // A web address is stored the way it is followed: completed when the field is left or the edit
  // is finished with Enter, never mid-word, where completing a scheme would fight the cursor.
  const { text, setText, commit, onBlur, onKeyDown } = useTLTypingField({
    normalize: inputType === 'url' ? normalizeUrl : undefined,
  });
  const t = useI18N(I18N_KEYS);
  const Button = usePart('Button');
  const inputRef = useRef<HTMLInputElement | null>(null);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setText(e.target.value);
    },
    [setText]
  );

  const handleClear = useCallback(async () => {
    setText('');
    // The gesture is the whole edit, so it is reported at once rather than after the debounce.
    await commit();
    inputRef.current?.focus();
  }, [setText, commit]);

  const multiline = state.multiline === true;
  const hasError = state.hasError === true;
  const disabled = state.disabled === true;
  const href = hasError || multiline ? null : linkHref(inputType, text);

  if (showsValueOnly(state)) {
    return (
      <FieldValue id={controlId} className={rootClassName(state)} text={text} href={href} multiline={multiline} />
    );
  }

  const errorMessage = state.errorMessage;
  const icon = state.icon;
  const hasIcon = !multiline && !!icon && icon !== 'none';
  const clearable = !multiline && !disabled && state.clearable === true && text !== '';

  if (multiline) {
    return (
      <span id={controlId}>
        <textarea
          rows={state.rows ?? 3}
          value={text}
          placeholder={state.placeholder}
          onChange={handleChange}
          onBlur={onBlur}
          disabled={disabled}
          className={rootClassName(state, 'tl-field tl-field--multiline tl-type-body')}
          {...fieldStateAttrs(state)}
          {...tooltipProps(hasError ? errorMessage : undefined)}
          id={inputId}
          {...labelProps}
        />
      </span>
    );
  }

  const input = (
    <input
      ref={inputRef}
      type={inputType}
      value={text}
      placeholder={state.placeholder}
      onChange={handleChange}
      onBlur={onBlur}
      onKeyDown={onKeyDown}
      disabled={disabled}
      className={rootClassName(state, 'tl-field tl-type-body')}
      {...fieldStateAttrs(state)}
      {...tooltipProps(hasError ? errorMessage : undefined)}
      id={inputId}
      {...labelProps}
    />
  );

  // The group has room for one action at its end: the clear button while it is shown, otherwise the
  // link. An inactive input offers neither.
  const openHref = clearable || disabled ? null : href;
  const grouped = hasIcon || clearable || openHref !== null;

  // The root element becomes the group rather than wrapping the input in one, so that the input
  // keeps its place in the tree - and with it the focus - while icon and action come and go.
  return (
    <span id={controlId} className={grouped ? 'tl-field-group' : undefined}>
      {hasIcon && (
        <span className="tl-field-group__icon">
          <ThemeIcon encoded={icon} className="tl-icon-sm" />
        </span>
      )}
      {input}
      {clearable && (
        <Button
          label={t['js.textInput.clear']}
          image={CLEAR_ICON}
          display="icon-only"
          appearance="ghost"
          small
          onClick={handleClear}
        />
      )}
      {openHref !== null && (
        <Button
          label={t['js.textInput.open']}
          image={OPEN_ICON}
          display="icon-only"
          appearance="ghost"
          small
          tooltip={text}
          href={openHref}
          target="_blank"
        />
      )}
    </span>
  );
};

export default TLTextInput;
