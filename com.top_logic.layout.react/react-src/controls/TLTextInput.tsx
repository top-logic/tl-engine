import {
  React,
  useTLState,
  useTLTypingField,
  useI18N,
  rootClassName,
  useFieldLabelProps,
  fieldInputId,
  usePart,
  useTLCommand,
  useSuggestions,
  CMD_SUGGEST,
  ARG_SUGGEST_TEXT,
  ARG_SUGGEST_CARET,
  ICON_CLEAR,
} from 'tl-react-bridge';
import type { TLCellProps, TextInputStateJson, SuggestionsAnswer, ListboxEntry, ListboxPartProps } from 'tl-react-bridge';
import { showsValueOnly, validationOf } from './form/fieldState';

const { useCallback, useLayoutEffect, useMemo, useRef } = React;

/** Options of the command asking for suggestions: it carries the text, the form is not read. */
const SUGGEST_OPTIONS = { flushFields: false } as const;

const I18N_KEYS = {
  'js.textInput.open': 'Open in a new tab',
  'js.textInput.clear': 'Clear the input',
};

/** The icon of the link that opens what the field holds. */
const OPEN_ICON = 'css:fa-solid fa-arrow-up-right-from-square';

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

/** The list of suggestions, drawn by the part `Listbox` while it is open. */
const SuggestionList: React.FC<ListboxPartProps> = props => {
  const Listbox = usePart('Listbox');
  return <Listbox {...props} />;
};

/**
 * A text input field rendered via React, drawn by the part `TextInput`: a single-line box by
 * default, or a multi-line text area when the field state requests it (state.multiline with
 * state.rows, state.preformatted for a text shown as it is written).
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
 * link while the field is empty or holds a value the server rejected. The link is the action of the
 * box, which comes and goes beside an input that keeps its place, so that typing keeps the focus.
 *
 * A 'url' is completed to an `https` address when the field is left and when it is submitted with
 * Enter, so that a typed bare host is stored as an address a browser can follow; a value naming its
 * own scheme ('mailto:', 'ftp:') is left as typed. The server holds such a field back until it is
 * left (state.sendValueOnBlur), so that the half-typed address in between is never judged.
 *
 * Three further states turn the single-line input into a search field. state.icon is the icon of
 * the box ahead of what is typed - the magnifier of a search box -, decoration hidden from assistive
 * technology, so the field is still named by its label or its placeholder. state.clearable adds a
 * button that empties the input, shown only while the input holds something, which writes the empty
 * value at once instead of after the debounce and hands the focus back to the input.
 * state.debounceMs names the span a typed value is held back, defaulting to VALUE_DEBOUNCE_MS and
 * overridden by state.sendValueOnBlur, which holds a value back entirely. The box has room for one
 * action, so the clear button, while it is shown, takes the place of the link.
 *
 * With state.offersSuggestions, the editable input is a combobox offering suggestions while the
 * user types (see useSuggestions): it asks the server with the command CMD_SUGGEST for the text and
 * the caret position, and shows the answer (state.suggestions, computed for state.suggestionsText
 * and state.suggestionsCaret) in the part `Listbox` below the input. A picked suggestion is entered
 * like typed text, with the caret behind it.
 *
 * The state of the value is the validation of the box (see validationOf), a mandatory field a
 * required box, and the message of an error the tooltip of the input. A read-only field renders
 * no input but the value as text (part `FieldValue`), one line per line of a multi-line text.
 * A disabled field renders the input as an inactive one, without the clear button and without the
 * link (see showsValueOnly). A hidden field renders nothing.
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
  const TextInput = usePart('TextInput');
  const FieldValue = usePart('FieldValue');
  const Button = usePart('Button');
  const sendCommand = useTLCommand();
  const inputRef = useRef<HTMLInputElement | HTMLTextAreaElement | null>(null);

  const multiline = state.multiline === true;
  const preformatted = multiline && state.preformatted === true;
  const disabled = state.disabled === true;
  const offersSuggestions = state.offersSuggestions === true && !disabled && !showsValueOnly(state);

  const stateSuggestions = state.suggestions;
  const suggestionsText = state.suggestionsText;
  const suggestionsCaret = state.suggestionsCaret;
  const answer = useMemo<SuggestionsAnswer | null>(
    () => stateSuggestions === undefined || stateSuggestions === null
      ? null
      : { entries: stateSuggestions, text: suggestionsText ?? '', caret: suggestionsCaret ?? 0 },
    [stateSuggestions, suggestionsText, suggestionsCaret]);

  // The caret position a picked suggestion puts the caret at, once the input shows its text.
  const pendingCaretRef = useRef<number | null>(null);

  const suggestions = useSuggestions({
    id: inputId,
    enabled: offersSuggestions,
    text,
    answer,
    getCaret: () => inputRef.current?.selectionStart ?? text.length,
    request: (typed, caret) => {
      void sendCommand(CMD_SUGGEST, { [ARG_SUGGEST_TEXT]: typed, [ARG_SUGGEST_CARET]: caret }, SUGGEST_OPTIONS);
    },
    apply: (picked, caret) => {
      pendingCaretRef.current = caret;
      setText(picked);
    },
  });

  useLayoutEffect(() => {
    const caret = pendingCaretRef.current;
    const input = inputRef.current;
    if (caret === null || input === null || input.value !== text) return;
    pendingCaretRef.current = null;
    input.setSelectionRange(caret, caret);
  }, [text]);

  const { entries: suggestionEntries, optionId, changed, onKeyDown: suggestionKeyDown, dismiss } = suggestions;
  const listEntries = useMemo<ListboxEntry[]>(
    () => suggestionEntries.map((entry, index) => ({ id: optionId(index), label: entry.label })),
    [suggestionEntries, optionId]);

  const handleChange = useCallback((typed: string) => {
    setText(typed);
    changed(typed, inputRef.current?.selectionStart ?? typed.length);
  }, [setText, changed]);

  const handleKeyDown = useCallback((event: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    if (suggestionKeyDown(event)) return;
    // The key handler of the typing field finishes a single-line edit; in a text area Enter is text.
    if (!multiline) onKeyDown?.(event);
  }, [suggestionKeyDown, multiline, onKeyDown]);

  const handleBlur = useCallback(() => {
    dismiss();
    return onBlur();
  }, [dismiss, onBlur]);

  const handleClear = useCallback(async () => {
    setText('');
    // The gesture is the whole edit, so it is reported at once rather than after the debounce.
    await commit();
    inputRef.current?.focus();
  }, [setText, commit]);

  if (state.hidden === true) {
    return null;
  }

  const hasError = state.hasError === true;
  const href = hasError || multiline ? null : linkHref(inputType, text);

  if (showsValueOnly(state)) {
    return (
      <FieldValue id={controlId} className={rootClassName(state) || undefined} text={text} href={href ?? undefined}
        multiline={multiline} preformatted={preformatted} />
    );
  }

  const icon = state.icon;
  const hasIcon = !multiline && !!icon && icon !== 'none';
  const clearable = !multiline && !disabled && state.clearable === true && text !== '';

  // The box has room for one action at its end: the clear button while it is shown, otherwise the
  // link. An inactive input offers neither.
  const openHref = clearable || disabled ? null : href;
  const action = clearable
    ? (
      <Button
        label={t['js.textInput.clear']}
        image={ICON_CLEAR}
        display="icon-only"
        appearance="ghost"
        small
        onClick={handleClear}
      />
    )
    : openHref !== null
      ? (
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
      )
      : undefined;

  return (
    <span id={controlId}>
      <TextInput
        id={inputId}
        ref={inputRef}
        {...labelProps}
        {...(offersSuggestions ? suggestions.comboboxAttributes : undefined)}
        value={text}
        type={multiline ? undefined : inputType}
        multiline={multiline}
        rows={multiline ? state.rows : undefined}
        preformatted={preformatted}
        placeholder={state.placeholder}
        icon={hasIcon ? icon : undefined}
        action={action}
        disabled={disabled}
        required={state.mandatory === true}
        validation={validationOf(state)}
        tooltip={hasError ? state.errorMessage : undefined}
        className={rootClassName(state) || undefined}
        onChange={offersSuggestions ? handleChange : setText}
        onBlur={offersSuggestions ? handleBlur : onBlur}
        onKeyDown={offersSuggestions ? handleKeyDown : multiline ? undefined : onKeyDown}
      />
      {suggestions.open && (
        <SuggestionList
          id={suggestions.listboxId}
          aria-labelledby={labelProps['aria-labelledby']}
          anchor={inputRef.current}
          entries={listEntries}
          activeIndex={suggestions.activeIndex}
          onPick={suggestions.pick}
          onActivate={suggestions.activate}
          onDismiss={dismiss}
        />
      )}
    </span>
  );
};

export default TLTextInput;
