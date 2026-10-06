import {
  React, useTLState, useTLFieldValue, useTLCommand, useTLSubmitOnEnter, VALUE_DEBOUNCE_MS,
} from 'tl-react-bridge';
import type { FieldStateJson, TypingFieldStateJson } from 'tl-react-bridge';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';

const { useCallback, useRef } = React;

/** Command a typing field sends when it is left after an edit, see `TypingFieldState.commitOnBlur`. */
const CMD_COMMIT = 'commit';

/** What stands in an empty line of a multi-line value, so that the line keeps its height. */
const EMPTY_LINE = ' ';

/** The style of the element carrying the control ID around an MUI input: a block of full width. */
export const FIELD_ROOT_STYLE: React.CSSProperties = { display: 'block' };

/**
 * Whether a form field displays its value only instead of an input: it is not editable and not
 * disabled. A disabled field renders its input as an inactive one.
 */
export function showsValueOnly(state: Partial<FieldStateJson>): boolean {
  return state.editable === false && state.disabled !== true;
}

/**
 * The ARIA attributes of a field's focusable element for the state of its value: `aria-invalid`
 * for an error, `aria-required` for a mandatory field.
 */
export function fieldAriaProps(state: Partial<FieldStateJson>): Record<string, string> {
  const attrs: Record<string, string> = {};
  if (state.hasError === true) attrs['aria-invalid'] = 'true';
  if (state.mandatory === true) attrs['aria-required'] = 'true';
  return attrs;
}

/**
 * The MUI color of a field input for the state of its value: `warning` for a value that is
 * questionable without being invalid. An error is shown through the `error` prop instead.
 */
export function fieldColor(state: Partial<FieldStateJson>): 'primary' | 'warning' {
  return state.hasError !== true && state.hasWarnings === true ? 'warning' : 'primary';
}

/**
 * The value of a field that is not editable, shown as text instead of an input (as TopLogic does).
 *
 * <p>A multi-line value keeps its line breaks, a value pointing somewhere (`href`) is a link
 * opening in a new tab.</p>
 */
export function MuiFieldValue({ id, className, text, href, multiline = false }: {
  id: string;
  className?: string;
  text: string;
  href?: string | null;
  multiline?: boolean;
}) {
  if (multiline) {
    return (
      <Typography id={id} component="div" variant="body2" className={className}>
        {text.split('\n').map((line, index) => (
          <span key={index} style={FIELD_ROOT_STYLE}>{line === '' ? EMPTY_LINE : line}</span>
        ))}
      </Typography>
    );
  }
  if (href != null) {
    return (
      <Link id={id} variant="body2" className={className} href={href} target="_blank" rel="noopener noreferrer">
        {text}
      </Link>
    );
  }
  return (
    <Typography id={id} component="span" variant="body2" noWrap className={className}>
      {text}
    </Typography>
  );
}

/** The value and handlers a typing field puts on its input, see {@link useTypingField}. */
export interface TypingField {
  /** The text the field shows, `''` for no value. */
  text: string;

  /** Sets the text typed: shown at once, sent as the state's typing behaviour says. */
  setText: (text: string) => void;

  /** Sends a value held back at once, see {@link useTLFieldValue}. */
  flush: () => Promise<void>;

  /** The blur handler of the input: sends a value held back, then the commit command if asked for. */
  onBlur: () => Promise<void>;

  /** The key handler of a single-line input sending the submit command on Enter, if asked for. */
  onSubmitKey?: (event: React.KeyboardEvent<HTMLInputElement>) => void;
}

/**
 * The typing behaviour of a field with a `TypingFieldState`, shared by the text, password and
 * number adapters.
 *
 * <p>The text typed is shown at once. It is sent as `valueChanged` after `debounceMs` (default
 * {@link VALUE_DEBOUNCE_MS}), or - with `sendValueOnBlur` - only when the field is left; leaving
 * the field always sends a value still held back. With `commitOnBlur` leaving the field after an
 * edit also sends `commit`. With `submitOnEnter` Enter in a single-line input sends `submit` with
 * the text.</p>
 *
 * @param emptyValue the value an empty text is sent as (`''` for a text, `null` for a number)
 */
export function useTypingField(emptyValue: '' | null): TypingField {
  const state = useTLState<Partial<TypingFieldStateJson>>();
  const sendCommand = useTLCommand();
  const [value, setValue, flush] = useTLFieldValue({
    debounceMs: state.debounceMs ?? VALUE_DEBOUNCE_MS,
    sendOnBlur: state.sendValueOnBlur === true,
  });
  const dirtyRef = useRef(false);
  const commitOnBlur = state.commitOnBlur === true;

  const setText = useCallback((text: string) => {
    dirtyRef.current = true;
    setValue(text === '' ? emptyValue : text);
  }, [setValue, emptyValue]);

  const onBlur = useCallback(async () => {
    // Await the value, so that a following commit runs after the value is applied on the server.
    await flush();
    if (commitOnBlur && dirtyRef.current) {
      dirtyRef.current = false;
      sendCommand(CMD_COMMIT);
    }
  }, [flush, commitOnBlur, sendCommand]);

  return {
    text: value == null ? '' : String(value),
    setText,
    flush,
    onBlur,
    onSubmitKey: useTLSubmitOnEnter(),
  };
}
