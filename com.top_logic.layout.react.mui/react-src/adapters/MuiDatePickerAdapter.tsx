import {
  React, useTLState, useTLTypingField, useFieldLabelProps, fieldInputId, rootClassName, anchoredOverlayProps, usePart,
} from 'tl-react-bridge';
import type { TLCellProps, DatePickerStateJson } from 'tl-react-bridge';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';
import type { DateView, FieldRef, PickerChangeHandlerContext, TimeView } from '@mui/x-date-pickers/models';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { TimePicker } from '@mui/x-date-pickers/TimePicker';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { FIELD_ROOT_STYLE, fieldAriaProps, showsValueOnly } from './field';

const { useState, useEffect, useRef } = React;

/** The part of a point in time a field edits whose state names none. */
const DEFAULT_INPUT_TYPE: DatePickerStateJson.InputType = 'date';

/** The precision of a field whose state names none. */
const DEFAULT_PRECISION: DatePickerStateJson.Precision = 'minutes';

/**
 * The ISO form the server exchanges a value in, per input type and precision (the form of the
 * HTML input of that type, see ReactDatePickerControl.Kind). A time of day is written with its
 * seconds for the precision of seconds.
 */
const ISO_FORMATS: Record<DatePickerStateJson.Precision, Record<DatePickerStateJson.InputType, string>> = {
  minutes: {
    date: 'YYYY-MM-DD',
    time: 'HH:mm',
    'datetime-local': 'YYYY-MM-DDTHH:mm',
  },
  seconds: {
    date: 'YYYY-MM-DD',
    time: 'HH:mm:ss',
    'datetime-local': 'YYYY-MM-DDTHH:mm:ss',
  },
};

/** The sections of a time of day a picker edits, per precision. */
const TIME_VIEWS = {
  minutes: ['hours', 'minutes'],
  seconds: ['hours', 'minutes', 'seconds'],
} as const satisfies Record<DatePickerStateJson.Precision, readonly TimeView[]>;

/** The sections of a date a date-time picker edits besides the time of day. */
const DATE_VIEWS = ['year', 'month', 'day'] as const satisfies readonly DateView[];

/** The source of a change made in the popup of a picker (calendar, clock, action bar). */
const SOURCE_VIEW: PickerChangeHandlerContext<unknown>['source'] = 'view';

/** The day a time of day without a date is placed on, to make it a dayjs value. */
const TIME_BASE_DAY = '1970-01-01T';

/**
 * The dayjs value of the ISO form of a value of the given input type, or `null` for no value.
 *
 * <p>The ISO forms carry no zone: dayjs reads a date and a local date-time as local time, which is
 * what the field shows.</p>
 */
export function fromIso(inputType: DatePickerStateJson.InputType, iso: unknown): Dayjs | null {
  if (typeof iso !== 'string' || iso === '') {
    return null;
  }
  const parsed = dayjs(inputType === 'time' ? TIME_BASE_DAY + iso : iso);
  return parsed.isValid() ? parsed : null;
}

/**
 * The ISO form of a dayjs value for the given input type and precision, or `null` for no value.
 */
export function toIso(
  inputType: DatePickerStateJson.InputType, precision: DatePickerStateJson.Precision, value: Dayjs | null,
): string | null {
  return value === null ? null : value.format(ISO_FORMATS[precision][inputType]);
}

/**
 * Renders the state of a TopLogic date field (module name `TLDatePicker`) with the MUI X
 * `DatePicker`, `TimePicker` or `DateTimePicker`, by the state's inputType. A time of day is
 * edited in hours and minutes, and in seconds as well for the state's precision of seconds.
 *
 * <p>The value is exchanged in its ISO form ({@link fromIso}, {@link toIso}). The display format
 * and the language come from the `LocalizationProvider` of the root wrapper: the state carries no
 * pattern.</p>
 *
 * <p>The typing behaviour is that of useTLTypingField, with the server's state.sendValueOnBlur: a
 * value typed into the sections of the picker field is held back - every section edit is a value
 * of its own, the year 0002 on the way to 2027 - and sent as `valueChanged` when the field is left
 * or Enter is pressed, followed by a `commit` while state.commitEdits is set. A value accepted in
 * the popup of the picker (`onAccept` from the source `view`: a day chosen, the popup closed) is a
 * finished edit of its own and is sent at once. A field whose sections are all emptied sends `null`.
 * A field left (or Enter pressed) with sections still incomplete - no valid date - sends nothing
 * and shows the value the server holds again, so a half-typed date never removes a stored one.</p>
 *
 * <p>A field that is not mandatory offers MUI's clear button of the picker field while it is
 * editable and holds a value - for a date, a time of day and both alike -, since the sections of
 * the field are emptied one by one only. A click on it is a finished edit of its own: `null` is
 * sent at once, followed by a `commit` while state.commitEdits is set.</p>
 *
 * <p>The popup is portaled to the document body: the desktop popper and the mobile dialog carry
 * {@link anchoredOverlayProps}, so that the focus trap of a dialog around the field lets the focus
 * into the popup.</p>
 *
 * <p>Mapping from the control state besides: hasError → `error` and `aria-invalid`, mandatory →
 * `aria-required`; a field that is not editable shows displayValue (the value in the user's
 * format, the ISO form where it is missing) as text (part `FieldValue`), as TLDatePicker does;
 * disabled → `disabled`; hidden → nothing is rendered; the configured CSS class → className of the
 * picker. The control ID is on the element around the picker. The hidden input of the picker field carries {@link fieldInputId},
 * which the label of the surrounding form field refers to; a click on the label focuses the field
 * through it. The input group of the field (role `group`) is labelled by the form field.</p>
 *
 * <p>An empty field shows placeholder - e.g. the value it inherits, in the format of the field - as
 * helper text below the picker field, which describes the field: the picker field shows the pattern
 * of the format where it is empty and has no place for a text of its own.</p>
 *
 * <p>Not used: label (rendered by the form field), errorMessage, hasWarnings (the picker field has
 * no warning color), tooltip, nullable and submitOnEnter.</p>
 */
const MuiDatePickerAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DatePickerStateJson>>();
  const FieldValue = usePart('FieldValue');
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const { text, setText, commit, revert } = useTLTypingField({ emptyValue: null });
  const inputType = state.inputType ?? DEFAULT_INPUT_TYPE;
  const precision = state.precision ?? DEFAULT_PRECISION;

  // The picker field, whose sections are emptied when an incomplete input reverts to no value.
  const fieldRef = useRef<FieldRef<Dayjs | null>>(null);

  // Whether the change the picker reports next comes from its clear button.
  const clearingRef = useRef(false);

  // The value the picker shows: the one held, or a value still being typed that is no date yet.
  const [shown, setShown] = useState<Dayjs | null>(() => fromIso(inputType, text));
  useEffect(() => {
    setShown(fromIso(inputType, text));
  }, [inputType, text]);

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return (
      <FieldValue id={controlId} className={rootClassName(state) || undefined}
        text={state.displayValue ?? text} />
    );
  }

  const handleChange = (newValue: Dayjs | null) => {
    const cleared = clearingRef.current;
    clearingRef.current = false;
    setShown(newValue);
    if (newValue === null) {
      setText('');
      if (cleared) {
        // The click on the clear button is the whole edit, so it is reported at once.
        void commit();
      }
    } else if (newValue.isValid()) {
      setText(toIso(inputType, precision, newValue)!);
    }
  };

  // The picker reports the emptied value through handleChange right after this.
  const handleClear = () => {
    clearingRef.current = true;
  };

  const handleAccept = (_value: Dayjs | null, context: PickerChangeHandlerContext<unknown>) => {
    if (context.source === SOURCE_VIEW) {
      void commit();
    }
  };

  // Whether the sections of the picker field are incomplete. The picker reports no date then - an
  // invalid one, or none at all once a section of a complete date is cleared; only the hidden input
  // of the field, empty exactly when all sections are, tells this from an emptied field.
  const holdsIncompleteInput = () => {
    if (shown !== null) {
      return !shown.isValid();
    }
    const hiddenInput = document.getElementById(inputId) as HTMLInputElement | null;
    return hiddenInput !== null && hiddenInput.value !== '';
  };

  // Finishes the edit of the picker field: sends a value held back, or - for sections still
  // incomplete - drops it and shows the value the server holds again.
  const finishEdit = () => {
    if (holdsIncompleteInput()) {
      const restored = fromIso(inputType, revert());
      if (restored === null && shown === null) {
        // The picker reports no value already, so a value of null changes nothing it shows: its
        // sections are emptied directly.
        fieldRef.current?.clearValue();
      } else {
        setShown(restored);
      }
    } else {
      void commit();
    }
  };

  // Only events of the picker field itself: React lets the events of the popup, which is portaled
  // to the document body, bubble here as well.
  const handleBlur = (e: React.FocusEvent<HTMLSpanElement>) => {
    const root = e.currentTarget;
    if (!root.contains(e.target) || (e.relatedTarget !== null && root.contains(e.relatedTarget as Node))) {
      return;
    }
    finishEdit();
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLSpanElement>) => {
    if (e.key !== 'Enter' || e.shiftKey || e.altKey || e.ctrlKey || e.metaKey
      || !e.currentTarget.contains(e.target as Node)) {
      return;
    }
    finishEdit();
  };

  const hint = shown === null ? (state.placeholder ?? undefined) : undefined;
  const pickerProps = {
    value: shown,
    onChange: handleChange,
    onAccept: handleAccept,
    disabled: state.disabled === true,
    className: rootClassName(state),
    slotProps: {
      textField: {
        id: inputId,
        size: 'small' as const,
        fullWidth: true,
        error: state.hasError === true,
        helperText: hint,
        // The label names the input group (role `group`, the root of the field input); the
        // field's label refers to the hidden input carrying the ID, which hands the focus on to
        // the group.
        slotProps: { input: { slotProps: { root: { ...labelProps, ...fieldAriaProps(state) } } } },
      },
      field: {
        fieldRef,
        clearable: state.mandatory !== true && state.disabled !== true && text !== '',
        onClear: handleClear,
      },
      popper: { ...anchoredOverlayProps },
      dialog: { ...anchoredOverlayProps },
    },
  };
  const timeViews = TIME_VIEWS[precision];

  return (
    <span id={controlId} style={FIELD_ROOT_STYLE} onBlur={handleBlur} onKeyDown={handleKeyDown}>
      {inputType === 'time' ? <TimePicker {...pickerProps} views={timeViews} />
        : inputType === 'datetime-local' ? <DateTimePicker {...pickerProps} views={[...DATE_VIEWS, ...timeViews]} />
        : <DatePicker {...pickerProps} />}
    </span>
  );
};

export default MuiDatePickerAdapter;
