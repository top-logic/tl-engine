import { React, useTLState, useTLTypingField, rootClassName, useFieldLabelProps, fieldInputId, usePart } from 'tl-react-bridge';
import type { TLCellProps, DatePickerStateJson } from 'tl-react-bridge';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import { ClearButton, offersClearByModel } from './form/ClearButton';

const { useCallback, useRef } = React;

/**
 * The keys without a character that change a segment of a native date or time input.
 */
const SEGMENT_EDITING_KEYS: ReadonlySet<string> = new Set([
  'Backspace', 'Delete', 'ArrowUp', 'ArrowDown', 'PageUp', 'PageDown',
]);

/**
 * Whether the given key changes a segment of a native date or time input from the keyboard: a
 * character typed into it (a digit, the letter of a month name or of AM/PM), or a key stepping or
 * clearing it. A shortcut with Control, Meta or Alt (pasting, say) is none.
 */
function isSegmentEditingKey(e: React.KeyboardEvent): boolean {
  if (e.ctrlKey || e.metaKey || e.altKey) {
    return false;
  }
  return e.key.length === 1 || SEGMENT_EDITING_KEYS.has(e.key);
}

/**
 * Whether the given input holds something that is no value: segments left incomplete, which the
 * native input reports as the empty value.
 */
function holdsIncompleteInput(input: HTMLInputElement): boolean {
  return input.value === '' && input.validity.badInput;
}

/**
 * A field for a point in time rendered via React.
 *
 * Which HTML input it is - a date, a time of day, or both - the server decides from the attribute's
 * type and states in `inputType`; the value is exchanged in the ISO form belonging to that input.
 * A field whose `precision` is `seconds` lets the seconds of a time of day be entered (`step="1"`)
 * and exchanges the time with its seconds.
 *
 * The native input reports a value as soon as every segment holds a number, so typing a date
 * segment by segment passes through dates nobody meant (the year 0002 on the way to 2027) and
 * through no value while a segment is cleared. A change made from the keyboard is therefore held
 * back (the server sets state.sendValueOnBlur) and sent when the field is left or on Enter - with a
 * 'commit' command after it while state.commitEdits is set, as useTLTypingField does for every
 * typed field. A change no key in a segment led to - a date chosen in the browser's picker, a value
 * pasted - is a finished edit of its own and is sent at once.
 *
 * A field left (or Enter pressed) with segments still incomplete sends nothing: it shows the value
 * the server holds again, so a half-typed date never removes a stored one. A field whose segments
 * were all cleared sends no value.
 *
 * A read-only field shows the localized value, drawn by the part `FieldValue`; a disabled field
 * renders the input as an inactive one (native `disabled`, see showsValueOnly).
 *
 * The native input is emptied segment by segment only. A field that is not mandatory therefore
 * offers the ClearButton while it is editable and holds a value (see offersClearByModel), for a
 * date, a time of day and both alike: a click sends no value at once and hands the focus back to
 * the input.
 *
 * An empty field shows `state.placeholder` - e.g. the value it inherits, written in the format of
 * the field - as a greyed hint beside the input, since the native input has no place for a text of
 * its own. The hint describes the input for assistive technology.
 */
const TLDatePicker: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DatePickerStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const { text, setText, commit, revert, onBlur, onKeyDown } =
    useTLTypingField({ emptyValue: null, finishOnEnter: true });
  const FieldValue = usePart('FieldValue');

  // Whether the change the input reports next comes from a key pressed in a segment.
  const segmentKeyRef = useRef(false);

  // Drops what the input holds and shows the value the server holds. The native input keeps the
  // segments it is given until its value is set, also when the value React renders is unchanged.
  const revertInput = useCallback((input: HTMLInputElement) => {
    input.value = revert();
  }, [revert]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Enter' && holdsIncompleteInput(e.currentTarget)) {
        revertInput(e.currentTarget);
        return;
      }
      segmentKeyRef.current = isSegmentEditingKey(e);
      onKeyDown?.(e);
    },
    [revertInput, onKeyDown]
  );

  const handleKeyUp = useCallback(() => {
    segmentKeyRef.current = false;
  }, []);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const typed = segmentKeyRef.current || holdsIncompleteInput(e.target);
      segmentKeyRef.current = false;
      setText(e.target.value);
      if (!typed) {
        void commit();
      }
    },
    [setText, commit]
  );

  const inputRef = useRef<HTMLInputElement | null>(null);

  const handleClear = useCallback(async () => {
    segmentKeyRef.current = false;
    setText('');
    // The gesture is the whole edit, so it is reported at once rather than when the field is left.
    await commit();
    inputRef.current?.focus();
  }, [setText, commit]);

  const handleBlur = useCallback(
    (e: React.FocusEvent<HTMLInputElement>) => {
      segmentKeyRef.current = false;
      if (holdsIncompleteInput(e.currentTarget)) {
        revertInput(e.currentTarget);
        return;
      }
      void onBlur();
    },
    [revertInput, onBlur]
  );

  if (showsValueOnly(state)) {
    // Read-only: show the localized value (e.g. "01.06.2026") supplied by the server, falling
    // back to the ISO value if no localized form was emitted.
    const display = state.displayValue ?? text;
    return <FieldValue id={controlId} className={rootClassName(state) || undefined} text={display} />;
  }

  const hint = text === '' ? (state.placeholder ?? null) : null;
  const hintId = `${controlId}-hint`;
  // The clear button is the action of a field group, which has a value; the hint stands beside an
  // empty input. The input stays the first child either way, so that it keeps the focus.
  const clearable = offersClearByModel(state, text !== '');
  const rootClass = clearable ? 'tl-field-group' : hint !== null ? 'tl-field-hint-host' : undefined;

  return (
    <span id={controlId} className={rootClass}>
      <input
        ref={inputRef}
        type={state.inputType ?? 'date'}
        step={state.precision === 'seconds' ? 1 : undefined}
        value={text}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        onKeyUp={handleKeyUp}
        onBlur={handleBlur}
        disabled={state.disabled === true}
        className={rootClassName(state, 'tl-field tl-type-body')}
        {...fieldStateAttrs(state)}
        id={inputId}
        {...labelProps}
        aria-describedby={hint !== null ? hintId : undefined}
      />
      {clearable && <ClearButton onClick={handleClear} />}
      {hint !== null && <span id={hintId} className="tl-field-hint tl-type-body">{hint}</span>}
    </span>
  );
};

export default TLDatePicker;
