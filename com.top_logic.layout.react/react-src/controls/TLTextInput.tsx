import { React, useTLFieldValue, useTLCommand, useTLSubmitOnEnter, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import FontIcon from './FontIcon';

const { useCallback, useRef } = React;

const I18N_KEYS = {
  'js.textInput.open': 'Open in a new tab',
};

/**
 * Debounce for transmitting a typed value to the server: long enough to coalesce a burst of
 * keystrokes into one round-trip, short enough that server-side validation can surface while the
 * user pauses (and the final value is always sent on blur). See {@link handleBlur}.
 */
const VALUE_DEBOUNCE_MS = 300;

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

/**
 * A text input field rendered via React. Renders a single-line input by default, or a multi-line
 * text area when the field state requests it (state.multiline with state.rows).
 *
 * Typing updates the local value immediately but the server `valueChanged` is debounced (and
 * always flushed on blur), so a field is not round-tripped on every keystroke. When
 * state.sendValueOnBlur is set, nothing is sent while the user is still in the field at all - for a
 * field whose model rewrites the text it is given, where any mid-edit round-trip would re-render
 * the field from the normalized value and throw away what was being typed. When state.commitOnBlur
 * is set, losing focus after an actual edit also sends a 'commit' command so the server can run
 * deferred per-field work (e.g. i18n auto-translation) once. When state.submitOnEnter is set,
 * Enter in the single-line input sends a 'submit' command carrying the text, so the server can run
 * a command over what was entered; a text area takes no such handler, where Enter is text.
 *
 * state.inputType says what the text means and becomes the `type` of the single-line input
 * ('text' when absent; a text area has no type). A value of a type that points somewhere - 'url'
 * at the address itself, 'email' at a `mailto:` address, 'tel' at a `tel:` number without its
 * spaces - is offered as a link: as the displayed text while the field is read-only, and as an
 * icon beside the input while it is edited, so that what is being typed can be opened. There is no
 * link while the field is empty or holds a value the server rejected; the icon comes and goes
 * inside a wrapper the single-line input always has, so that gaining or losing the link leaves the
 * input itself in place and typing keeps the focus.
 *
 * A 'url' is completed to an `https` address when the field is left and when it is submitted with
 * Enter, so that a typed bare host is stored as an address a browser can follow; a value naming its
 * own scheme ('mailto:', 'ftp:') is left as typed. The server holds such a field back until it is
 * left (state.sendValueOnBlur), so that the half-typed address in between is never judged.
 */
const TLTextInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue, flushValue] = useTLFieldValue({
    debounceMs: VALUE_DEBOUNCE_MS,
    sendOnBlur: state.sendValueOnBlur === true,
  });
  const sendCommand = useTLCommand();
  const t = useI18N(I18N_KEYS);
  const dirtyRef = useRef(false);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      dirtyRef.current = true;
      setValue(e.target.value);
    },
    [setValue]
  );

  const text = (value as string) ?? '';
  const inputType = (state.inputType as string) ?? 'text';

  const commitOnBlur = state.commitOnBlur === true;
  const handleBlur = useCallback(async () => {
    if (inputType === 'url') {
      // Store the address the way it is followed, but only once the user has stopped typing:
      // completing a scheme mid-word would fight the cursor.
      const normalized = normalizeUrl(text);
      if (normalized !== text) {
        setValue(normalized);
      }
    }
    // Always send the final value when leaving the field, even if a keystroke is still within the
    // debounce window; await it so a follow-up 'commit' runs after the value is applied server-side.
    await flushValue();
    if (commitOnBlur && dirtyRef.current) {
      dirtyRef.current = false;
      sendCommand('commit');
    }
  }, [flushValue, commitOnBlur, sendCommand, inputType, text, setValue]);

  const submitKey = useTLSubmitOnEnter();
  const handleSubmitKey = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (submitKey === undefined) {
        return;
      }
      if (inputType === 'url' && e.key === 'Enter') {
        // What is submitted is the address as it is stored, so complete the scheme before the
        // handler reads the input.
        const normalized = normalizeUrl(e.currentTarget.value);
        if (normalized !== e.currentTarget.value) {
          e.currentTarget.value = normalized;
          setValue(normalized);
        }
      }
      submitKey(e);
    },
    [submitKey, inputType, setValue]
  );

  const multiline = state.multiline === true;
  const hasError = state.hasError === true;
  const href = hasError || multiline ? null : linkHref(inputType, text);

  if (state.editable === false) {
    const immutableCls =
      'tlReactTextInput tlReactTextInput--immutable' +
      (multiline ? ' tlReactTextInput--multiline' : '');
    if (href !== null) {
      return (
        <a
          id={controlId}
          className={immutableCls + ' tlReactTextInput--link'}
          href={href}
          target="_blank"
          rel="noopener noreferrer"
        >
          {text}
        </a>
      );
    }
    return (
      <span id={controlId} className={immutableCls}>
        {text}
      </span>
    );
  }

  const hasWarnings = state.hasWarnings === true;
  const errorMessage = state.errorMessage as string | undefined;
  const cls = [
    'tlReactTextInput',
    multiline ? 'tlReactTextInput--multiline' : '',
    hasError ? 'tlReactTextInput--error' : '',
    !hasError && hasWarnings ? 'tlReactTextInput--warning' : '',
  ].filter(Boolean).join(' ');

  if (multiline) {
    return (
      <span id={controlId}>
        <textarea
          rows={(state.rows as number) ?? 3}
          value={text}
          placeholder={(state.placeholder as string) ?? undefined}
          onChange={handleChange}
          onBlur={handleBlur}
          disabled={state.disabled === true}
          className={cls}
          aria-invalid={hasError || undefined}
          title={hasError && errorMessage ? errorMessage : undefined}
        />
      </span>
    );
  }

  const input = (
    <input
      type={inputType}
      value={text}
      placeholder={(state.placeholder as string) ?? undefined}
      onChange={handleChange}
      onBlur={handleBlur}
      onKeyDown={submitKey === undefined ? undefined : handleSubmitKey}
      disabled={state.disabled === true}
      className={cls}
      aria-invalid={hasError || undefined}
      title={hasError && errorMessage ? errorMessage : undefined}
    />
  );

  return (
    <span id={controlId}>
      <span className="tlReactTextInput__row">
        {input}
        {href !== null && (
          <a
            className="tlReactTextInput__open"
            href={href}
            target="_blank"
            rel="noopener noreferrer"
            title={text}
            aria-label={t['js.textInput.open']}
          >
            <FontIcon image={OPEN_ICON} />
          </a>
        )}
      </span>
    </span>
  );
};

export default TLTextInput;
