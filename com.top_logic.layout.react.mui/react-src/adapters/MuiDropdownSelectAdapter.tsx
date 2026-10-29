import {
  React, useTLCommand, useFieldLabelProps, fieldInputId, rootClassName, anchoredOverlayProps, useI18N, usePart,
  OptionValues, labelMatches,
} from 'tl-react-bridge';
import type { TLCellProps, ColorRole } from 'tl-react-bridge';
import Autocomplete, { createFilterOptions } from '@mui/material/Autocomplete';
import type {
  AutocompleteChangeReason, AutocompleteInputChangeReason, FilterOptionsState,
} from '@mui/material/useAutocomplete';
import TextField from '@mui/material/TextField';
import { ARG_TEXT, CMD_ENTER_TEXT, OptionContent, useChoice } from './choice';
import type { OptionDescriptor } from './choice';
import { FIELD_ROOT_STYLE, fieldAriaProps, fieldColor, showsValueOnly } from './field';

const { useRef, useState } = React;

/** Command asking the server for the options, answered by a state with optionsLoaded set. */
const CMD_LOAD_OPTIONS = 'loadOptions';

/** The key of the name of the button removing a chosen option, `{0}` standing for its label. */
const KEY_REMOVE_CHIP = 'js.dropdownSelect.removeChip';

/** The key of the label of the entry taking the typed text, `{0}` standing for the text. */
const KEY_USE_TEXT = 'js.dropdownSelect.useText';

/** The texts of the field, by key, with their English defaults. */
const I18N_KEYS = { [KEY_REMOVE_CHIP]: 'Remove {0}', [KEY_USE_TEXT]: 'Use "{0}"' };

/** The key of the entry taking the typed text, which no option of the server has. */
const ENTRY_KEY = '\u0000entry';

/**
 * An option of the list: an option of the server, or the entry taking the typed text of a field
 * accepting free entry, which carries that text in {@link ListOption#entryText}.
 */
interface ListOption extends OptionDescriptor {
  /** The typed text the entry takes; absent for an option of the server. */
  entryText?: string;
}

/** MUI's filter of the options by the typed text, as the adapter uses it without free entry. */
const defaultFilter = createFilterOptions<ListOption>();

/** Whether two options name the same object; a typed text (free solo value) names none. */
function sameOption(option: ListOption, value: ListOption | string): boolean {
  return typeof value !== 'string' && option.value === value.value;
}

/**
 * Renders the state of a TopLogic choice field whose options open in a list (module name
 * `TLDropdownSelect`, `DropdownSelectState`) with the MUI `Autocomplete`.
 *
 * <p>The list opens on demand. While the options are not loaded (optionsLoaded), opening sends
 * `loadOptions` and the list shows MUI's loading state until the server's answer brings the
 * options. The options are filtered by the text typed, on the client, as TLDropdownSelect does. The
 * list of a field choosing one option holds all options, MUI marking the chosen one; the list of a
 * field choosing several (multiSelect) leaves the chosen options out. Choosing sends `valueChanged`
 * with the values of the chosen options (see {@link useChoice}), choosing the chosen option again
 * sends nothing; a field choosing several shows them as chips drawn by the part `Chip`, each with
 * a button removing it, a new one appended at the end.</p>
 *
 * <p>A field that is not mandatory offers MUI's clear button, which sends the empty choice; while
 * nothing is chosen, emptyOptionLabel is the placeholder. An option → its icon and label, the
 * part `Chip` of its color role where it has one ({@link OptionContent}); a chip of a chosen option
 * takes the icon and the color role of its option.</p>
 *
 * <p>The list is portaled to the document body; its popper carries {@link anchoredOverlayProps}, so
 * that the focus trap of a dialog around the field lets the focus into it. The control ID is on the
 * element around the `Autocomplete`, its input carries {@link fieldInputId} and is labelled by the
 * surrounding form field. hasError → `error` and `aria-invalid`, hasWarnings → color `warning`,
 * mandatory → `aria-required`; a field that is not editable shows the chosen values
 * ({@link OptionValues}); disabled → `disabled`; hidden → nothing is rendered. MUI's texts
 * (no options, loading, clear) come from the theme locale of the page language.</p>
 *
 * <p>A field accepting free entry (freeEntry) is MUI's `freeSolo`: Enter on a typed text that no
 * highlighted option takes, the entry taking the text (`Use "xyz"`, offered at the end of the list
 * while the text names no option and no chosen value, see labelMatches), and leaving the field with
 * a typed text send `enterText` with the trimmed text; Escape drops the typed text. The server
 * chooses the option the text names, or the value it makes of the text, and reports a text it
 * refuses in the window's snackbar. Without free entry, typing only filters.</p>
 *
 * <p>Not reproduced: reordering the chosen options by drag and drop (customOrder) - the MUI chips
 * cannot be dragged, the order is the one in which the options were chosen -, and a list without
 * filter input (noFilter), whose type-ahead the `Autocomplete` does not offer: it always filters.
 * Not used: label, errorMessage, tooltip, placeholder, nullable, submitOnEnter, orientation.</p>
 */
const MuiDropdownSelectAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const { state, value, options, send, goto } = useChoice();
  const sendCommand = useTLCommand();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [open, setOpen] = useState(false);
  // The text in the input, and the part of it the user typed and has not taken yet.
  const [inputText, setInputText] = useState<string | undefined>(undefined);
  const typedRef = useRef('');
  const i18n = useI18N(I18N_KEYS);
  const Chip = usePart('Chip');

  if (state.hidden === true) {
    return null;
  }
  if (showsValueOnly(state)) {
    return <OptionValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />;
  }

  const multiSelect = state.multiSelect === true;
  const optionsLoaded = state.optionsLoaded === true;
  const hasError = state.hasError === true;
  const freeEntry = state.freeEntry === true;

  /** Sends the given text as a free entry; a blank one is not sent. */
  const sendText = (text: string) => {
    typedRef.current = '';
    const entered = text.trim();
    if (entered !== '') {
      sendCommand(CMD_ENTER_TEXT, { [ARG_TEXT]: entered });
    }
  };

  /** The text shown in the input while nothing is typed: the label of a single chosen option. */
  const valueText = multiSelect ? '' : value[0]?.label ?? '';

  const handleInputChange = (_event: React.SyntheticEvent, text: string, reason: AutocompleteInputChangeReason) => {
    setInputText(text);
    if (reason === 'input') {
      typedRef.current = text;
    } else if (reason !== 'blur') {
      // The input shows a chosen value or is cleared: nothing typed is pending.
      typedRef.current = '';
    }
  };

  /** Leaving the field takes a pending typed text as free entry. */
  const handleBlur = () => {
    if (freeEntry) {
      sendText(typedRef.current);
    }
  };

  const handleClose = (_event: React.SyntheticEvent, reason: string) => {
    setOpen(false);
    if (reason === 'escape' && freeEntry) {
      typedRef.current = '';
      setInputText(valueText);
    }
  };

  /** The options the typed text leaves, followed by the entry taking the text, where offered. */
  const filterOptions = (listed: ListOption[], filterState: FilterOptionsState<ListOption>) => {
    const filtered = defaultFilter(listed, filterState);
    const text = filterState.inputValue.trim();
    if (freeEntry && optionsLoaded && text !== '' && text !== valueText
      && !options.some(option => labelMatches(option, text)) && !value.some(option => labelMatches(option, text))) {
      filtered.push({ value: ENTRY_KEY, label: i18n[KEY_USE_TEXT].replace('{0}', text), entryText: text });
    }
    return filtered;
  };

  const handleOpen = () => {
    setOpen(true);
    if (!optionsLoaded) {
      sendCommand(CMD_LOAD_OPTIONS);
    }
  };

  const handleChange = (
    event: React.SyntheticEvent,
    newValue: ListOption | string | (ListOption | string)[] | null,
    reason: AutocompleteChangeReason,
  ) => {
    if (reason === 'clear' && event.type === 'change') {
      // MUI's free solo clears the value of a single-valued field when its input is emptied by
      // typing; the field keeps its value until a text is entered or the clear button is pressed.
      return;
    }
    const chosen = newValue === null ? [] : Array.isArray(newValue) ? newValue : [newValue];
    // A typed text (MUI's free solo value) or the entry taking it is entered as text; it is the
    // last element, MUI appending it to the chosen options of a multi-valued field.
    const entered = chosen.find(option => typeof option === 'string' || option.entryText !== undefined);
    if (entered !== undefined) {
      sendText(typeof entered === 'string' ? entered : entered.entryText!);
      setInputText(valueText);
      return;
    }
    typedRef.current = '';
    send(chosen as OptionDescriptor[]);
  };

  return (
    <span id={controlId} style={FIELD_ROOT_STYLE} onBlur={handleBlur}>
      <Autocomplete<ListOption, boolean, boolean, boolean>
        id={inputId}
        multiple={multiSelect}
        value={multiSelect ? value : value[0] ?? null}
        options={optionsLoaded ? options : []}
        open={open}
        onOpen={handleOpen}
        onClose={handleClose}
        loading={open && !optionsLoaded}
        onChange={handleChange}
        freeSolo={freeEntry}
        clearOnBlur
        inputValue={freeEntry ? inputText ?? valueText : undefined}
        onInputChange={handleInputChange}
        filterOptions={filterOptions}
        filterSelectedOptions={multiSelect}
        disableClearable={state.mandatory === true}
        disabled={state.disabled === true}
        isOptionEqualToValue={sameOption}
        getOptionLabel={option => (typeof option === 'string' ? option : option.entryText ?? option.label)}
        getOptionKey={option => (typeof option === 'string' ? option : option.value)}
        renderOption={({ key, ...props }, option) => (
          <li key={key} {...props}><OptionContent option={option} /></li>
        )}
        renderValue={multiSelect
          ? (chosen, getItemProps) => (chosen as OptionDescriptor[]).map((option, index) => {
            const { className, disabled, tabIndex, onDelete, 'data-item-index': itemIndex } = getItemProps({ index });
            return (
              <Chip key={option.value} className={className} disabled={disabled} tabIndex={tabIndex}
                data-item-index={itemIndex}
                label={option.label} icon={option.image}
                colorRole={(option.colorRole || undefined) as ColorRole | undefined} onRemove={onDelete}
                removeLabel={i18n[KEY_REMOVE_CHIP].replace('{0}', option.label)} />
            );
          })
          : undefined}
        size="small"
        fullWidth
        className={rootClassName(state)}
        slotProps={{ popper: { ...anchoredOverlayProps } }}
        renderInput={params => (
          <TextField
            {...params}
            error={hasError}
            color={fieldColor(state)}
            placeholder={value.length === 0 ? state.emptyOptionLabel : undefined}
            slotProps={{
              ...params.slotProps,
              htmlInput: { ...params.slotProps.htmlInput, ...labelProps, ...fieldAriaProps(state) },
            }}
          />
        )}
      />
    </span>
  );
};

export default MuiDropdownSelectAdapter;
