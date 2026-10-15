import {
  React, useTLState, useTLCommand, CMD_VALUE_CHANGED, rootClassName, useFieldLabelProps, usePart, OptionValues,
} from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson, ColorRole } from 'tl-react-bridge';
import { ARG_OPTION, CMD_GOTO } from './selectOptions';
import type { OptionDescriptor } from './selectOptions';
import { fieldStateAttrs, showsValueOnly, validationOf } from './form/fieldState';

const { useCallback, useMemo, useRef } = React;

/**
 * A select field whose every option is a toggle of its own, laid out as a cloud that wraps onto as
 * many lines as it needs.
 *
 * The whole option list is on screen, so what is on, what is off and what else could be chosen are
 * read without opening anything, and each is changed with one click. A field taking several values
 * toggles each independently; a field taking one switches the selection to the clicked option, and
 * a click on the selected option clears it unless the field is mandatory.
 *
 * Each option is a toggle drawn by the part `Chip`, pressed while the option is chosen, with the
 * image and the color role of the option, in a `tl-chip-group` of the design system, which carries
 * the field's state (see fieldStateAttrs); every chip is given that state as its validation, too. A
 * read-only field shows its values as the other choice fields do (OptionValues); a disabled field
 * renders every chip disabled (see showsValueOnly).
 *
 * The server hands this control the complete option list as soon as it is displayed - there is no
 * moment at which it could ask for it.
 */
const TLOptionChips: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DropdownSelectStateJson>>();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const sendCommand = useTLCommand();
  const Chip = usePart('Chip');

  const value = (state.value ?? []) as OptionDescriptor[];
  const options = (state.options ?? []) as OptionDescriptor[];
  const multiSelect = state.multiSelect === true;
  const mandatory = state.mandatory === true;
  const disabled = state.disabled === true;

  // Tracks the latest selection so that a second click lands on what the first one produced, even
  // while the echo of the first has not arrived yet.
  const valueRef = useRef(value);
  valueRef.current = value;

  const selectedIds = useMemo(() => new Set(value.map((v) => v.value)), [value]);

  const send = useCallback(
    (selection: OptionDescriptor[]) => {
      valueRef.current = selection;
      sendCommand(CMD_VALUE_CHANGED, { value: selection.map((v) => v.value) });
    },
    [sendCommand]
  );

  const toggle = useCallback(
    (option: OptionDescriptor) => {
      if (disabled) return;
      const selection = valueRef.current;
      const selected = selection.some((v) => v.value === option.value);
      if (multiSelect) {
        send(selected ? selection.filter((v) => v.value !== option.value) : [...selection, option]);
        return;
      }
      if (!selected) {
        send([option]);
      } else if (!mandatory) {
        // The only way to empty a single-valued field: there is no separate clear button to do it.
        send([]);
      }
    },
    [disabled, multiSelect, mandatory, send]
  );

  /** Leads to the place the given option is displayed at. */
  const goto = useCallback(
    (optionValue: string) => {
      sendCommand(CMD_GOTO, { [ARG_OPTION]: optionValue });
    },
    [sendCommand]
  );

  if (showsValueOnly(state)) {
    return (
      <OptionValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />
    );
  }

  const validation = validationOf(state);
  return (
    <div
      id={controlId}
      {...labelProps}
      role="group"
      className={rootClassName(state, 'tl-chip-group')}
      {...fieldStateAttrs(state, false)}
    >
      {options.map((option) => (
        <Chip
          key={option.value}
          label={option.label}
          icon={option.image}
          colorRole={(option.colorRole || undefined) as ColorRole | undefined}
          pressed={selectedIds.has(option.value)}
          validation={validation}
          disabled={disabled}
          onClick={() => toggle(option)}
        />
      ))}
    </div>
  );
};

export default TLOptionChips;
