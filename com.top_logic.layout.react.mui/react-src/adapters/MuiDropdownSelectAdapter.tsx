import {
  React, useTLCommand, useFieldLabelProps, fieldInputId, rootClassName, anchoredOverlayProps, useI18N, usePart,
  OptionValues,
} from 'tl-react-bridge';
import type { TLCellProps, ChipTone } from 'tl-react-bridge';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import { OptionContent, useChoice } from './choice';
import type { OptionDescriptor } from './choice';
import { FIELD_ROOT_STYLE, fieldAriaProps, fieldColor, showsValueOnly } from './field';

const { useState } = React;

/** Command asking the server for the options, answered by a state with optionsLoaded set. */
const CMD_LOAD_OPTIONS = 'loadOptions';

/** The key of the name of the button removing a chosen option, `{0}` standing for its label. */
const KEY_REMOVE_CHIP = 'js.dropdownSelect.removeChip';

/** The texts of the field, by key, with their English defaults. */
const I18N_KEYS = { [KEY_REMOVE_CHIP]: 'Remove {0}' };

/** Whether two options name the same object. */
function sameOption(option: OptionDescriptor, value: OptionDescriptor): boolean {
  return option.value === value.value;
}

/**
 * Renders the state of a TopLogic choice field whose options open in a list (module name
 * `TLDropdownSelect`, `DropdownSelectState`) with the MUI `Autocomplete`.
 *
 * <p>The list opens on demand. While the options are not loaded (optionsLoaded), opening sends
 * `loadOptions` and the list shows MUI's loading state until the server's answer brings the
 * options. The options are filtered by the text typed, on the client, as TLDropdownSelect does; the
 * chosen options are left out of the list. Choosing sends `valueChanged` with the values of the
 * chosen options (see {@link useChoice}); a field choosing several (multiSelect) shows them as
 * chips drawn by the part `Chip`, each removable, a new one appended at the end.</p>
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

  const handleOpen = () => {
    setOpen(true);
    if (!optionsLoaded) {
      sendCommand(CMD_LOAD_OPTIONS);
    }
  };

  const handleChange = (_event: React.SyntheticEvent, newValue: OptionDescriptor | OptionDescriptor[] | null) => {
    send(newValue === null ? [] : Array.isArray(newValue) ? newValue : [newValue]);
  };

  return (
    <span id={controlId} style={FIELD_ROOT_STYLE}>
      <Autocomplete<OptionDescriptor, boolean, boolean, false>
        id={inputId}
        multiple={multiSelect}
        value={multiSelect ? value : value[0] ?? null}
        options={optionsLoaded ? options : []}
        open={open}
        onOpen={handleOpen}
        onClose={() => setOpen(false)}
        loading={open && !optionsLoaded}
        onChange={handleChange}
        filterSelectedOptions
        disableClearable={state.mandatory === true}
        disabled={state.disabled === true}
        isOptionEqualToValue={sameOption}
        getOptionLabel={option => option.label}
        getOptionKey={option => option.value}
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
                tone={(option.colorRole || undefined) as ChipTone | undefined} onRemove={onDelete}
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
