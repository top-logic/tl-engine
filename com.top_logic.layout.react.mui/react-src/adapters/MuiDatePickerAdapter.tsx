import {
  React, useTLState, useTLFieldValue, useFieldLabelProps, fieldInputId, rootClassName, anchoredOverlayProps,
} from 'tl-react-bridge';
import type { TLCellProps, DatePickerStateJson } from 'tl-react-bridge';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { TimePicker } from '@mui/x-date-pickers/TimePicker';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { FIELD_ROOT_STYLE, MuiFieldValue, fieldAriaProps, showsValueOnly } from './field';

const { useState, useEffect } = React;

/** The part of a point in time a field edits whose state names none. */
const DEFAULT_INPUT_TYPE: DatePickerStateJson.InputType = 'date';

/**
 * The ISO form the server exchanges a value in, per input type (the form of the HTML input of
 * that type, see ReactDatePickerControl.Kind). The server also reads seconds, but never needs them.
 */
const ISO_FORMATS: Record<DatePickerStateJson.InputType, string> = {
  date: 'YYYY-MM-DD',
  time: 'HH:mm',
  'datetime-local': 'YYYY-MM-DDTHH:mm',
};

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

/** The ISO form of a dayjs value for the given input type, or `null` for no value. */
export function toIso(inputType: DatePickerStateJson.InputType, value: Dayjs | null): string | null {
  return value === null ? null : value.format(ISO_FORMATS[inputType]);
}

/**
 * Renders the state of a TopLogic date field (module name `TLDatePicker`) with the MUI X
 * `DatePicker`, `TimePicker` or `DateTimePicker`, by the state's inputType.
 *
 * <p>The value is exchanged in its ISO form ({@link fromIso}, {@link toIso}). A complete value is
 * sent as `valueChanged` at once; an emptied field sends `null`; a value still being typed (not yet
 * a valid date) is kept in the field and not sent. The display format and the language come from
 * the `LocalizationProvider` of the root wrapper: the state carries no pattern.</p>
 *
 * <p>The popup is portaled to the document body: the desktop popper and the mobile dialog carry
 * {@link anchoredOverlayProps}, so that the focus trap of a dialog around the field lets the focus
 * into the popup.</p>
 *
 * <p>Mapping from the control state besides: hasError → `error` and `aria-invalid`, mandatory →
 * `aria-required`; a field that is not editable shows displayValue (the value in the user's
 * format, the ISO form where it is missing), as TLDatePicker does; disabled → `disabled`; hidden →
 * nothing is rendered; the configured CSS class → className of the picker. The control ID is on
 * the element around the picker. The hidden input of the picker field carries {@link fieldInputId},
 * which the label of the surrounding form field refers to; a click on the label focuses the field
 * through it. The input group of the field (role `group`) is labelled by the form field.</p>
 *
 * <p>Not used: label (rendered by the form field), errorMessage, hasWarnings (the picker field has
 * no warning color), tooltip, placeholder, nullable and submitOnEnter.</p>
 */
const MuiDatePickerAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DatePickerStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [value, setValue] = useTLFieldValue();
  const inputType = state.inputType ?? DEFAULT_INPUT_TYPE;

  // The value the picker shows: the server's, or a value still being typed that is no date yet.
  const [shown, setShown] = useState<Dayjs | null>(() => fromIso(inputType, value));
  useEffect(() => {
    setShown(fromIso(inputType, value));
  }, [inputType, value]);

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return (
      <MuiFieldValue id={controlId} className={rootClassName(state)}
        text={state.displayValue ?? (typeof value === 'string' ? value : '')} />
    );
  }

  const handleChange = (newValue: Dayjs | null) => {
    setShown(newValue);
    if (newValue === null) {
      setValue(null);
    } else if (newValue.isValid()) {
      setValue(toIso(inputType, newValue));
    }
  };

  const Picker = inputType === 'time' ? TimePicker
    : inputType === 'datetime-local' ? DateTimePicker : DatePicker;

  return (
    <span id={controlId} style={FIELD_ROOT_STYLE}>
      <Picker
        value={shown}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={rootClassName(state)}
        slotProps={{
          textField: {
            id: inputId,
            size: 'small',
            fullWidth: true,
            error: state.hasError === true,
            // The label names the input group (role `group`, the root of the field input); the
            // field's label refers to the hidden input carrying the ID, which hands the focus on to
            // the group.
            slotProps: { input: { slotProps: { root: { ...labelProps, ...fieldAriaProps(state) } } } },
          },
          popper: { ...anchoredOverlayProps },
          dialog: { ...anchoredOverlayProps },
        }}
      />
    </span>
  );
};

export default MuiDatePickerAdapter;
