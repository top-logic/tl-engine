import { React, useTLState, useTLCommand, CMD_VALUE_CHANGED, rootClassName, useFieldLabelProps } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson } from 'tl-react-bridge';
import { ARG_OPTION, CMD_GOTO, OptionContent, ReadonlyValues } from './selectOptions';
import type { OptionDescriptor } from './selectOptions';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';

const { useCallback, useMemo, useRef } = React;

/**
 * A select field whose options are the segments of one bar, the selected one filled.
 *
 * The bar reads as one control whose position is the value, which suits a few mutually exclusive
 * options that belong together as one setting. A field taking one value follows the radio-group
 * pattern: the arrow keys move the selection from segment to segment. A field taking several fills
 * every segment that is on.
 *
 * Design system: `tl-segmented` with `tl-segmented__segment` buttons. Selected is an attribute -
 * `aria-checked` in a radio group, `aria-pressed` in a group of several - and so is the field's
 * state (see fieldStateAttrs). The label stands in a `tl-segmented__label`, which ends in an ellipsis
 * where the segment is too narrow. A read-only field shows its values in `tl-select__values`, as the
 * dropdown does; a disabled field renders the bar with every segment an inactive button (native
 * `disabled`, see showsValueOnly).
 *
 * The server hands this control the complete option list as soon as it is displayed - there is no
 * moment at which it could ask for it.
 */
const TLSegmentedChoice: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DropdownSelectStateJson>>();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const sendCommand = useTLCommand();

  const value = (state.value ?? []) as OptionDescriptor[];
  const options = (state.options ?? []) as OptionDescriptor[];
  const multiSelect = state.multiSelect === true;
  const mandatory = state.mandatory === true;
  const disabled = state.disabled === true;

  // Tracks the latest selection so that a second click lands on what the first one produced, even
  // while the echo of the first has not arrived yet.
  const valueRef = useRef(value);
  valueRef.current = value;

  const segmentRefs = useRef<(HTMLButtonElement | null)[]>([]);

  const selectedIds = useMemo(() => new Set(value.map((v) => v.value)), [value]);

  /** The first selected segment, or -1 while nothing is selected. */
  const selectedIndex = useMemo(
    () => options.findIndex((o) => selectedIds.has(o.value)),
    [options, selectedIds]
  );

  const send = useCallback(
    (selection: OptionDescriptor[]) => {
      valueRef.current = selection;
      sendCommand(CMD_VALUE_CHANGED, { value: selection.map((v) => v.value) });
    },
    [sendCommand]
  );

  const choose = useCallback(
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

  // The radio pattern: the arrows move the selection, so the bar is operated without the pointer.
  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (multiSelect || disabled || options.length === 0) return;
      let step = 0;
      if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
        step = 1;
      } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
        step = -1;
      } else {
        return;
      }
      e.preventDefault();
      e.stopPropagation();
      const from = selectedIndex < 0 ? (step > 0 ? -1 : 0) : selectedIndex;
      const next = (from + step + options.length) % options.length;
      segmentRefs.current[next]?.focus();
      send([options[next]]);
    },
    [multiSelect, disabled, options, selectedIndex, send]
  );

  if (showsValueOnly(state)) {
    return (
      <ReadonlyValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />
    );
  }

  // Exactly one segment is reachable by Tab; within the bar the arrows move on, which is what makes
  // a group of radios one stop in the tab order rather than one stop per option.
  const tabStop = selectedIndex < 0 ? 0 : selectedIndex;

  return (
    <div
      id={controlId}
      {...labelProps}
      role={multiSelect ? 'group' : 'radiogroup'}
      className={rootClassName(state, 'tl-segmented')}
      {...fieldStateAttrs(state, !multiSelect)}
      onKeyDown={handleKeyDown}
    >
      {options.map((option, index) => {
        const selected = selectedIds.has(option.value);
        return (
          <button
            key={option.value}
            ref={(element) => {
              segmentRefs.current[index] = element;
            }}
            type="button"
            role={multiSelect ? undefined : 'radio'}
            aria-checked={multiSelect ? undefined : selected}
            aria-pressed={multiSelect ? selected : undefined}
            tabIndex={multiSelect || index === tabStop ? 0 : -1}
            className="tl-segmented__segment tl-type-label"
            disabled={disabled}
            onClick={() => choose(option)}
          >
            <OptionContent option={option} labelClassName="tl-segmented__label" />
          </button>
        );
      })}
    </div>
  );
};

export default TLSegmentedChoice;
