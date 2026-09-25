import { React, useTLState, useTLCommand, CMD_VALUE_CHANGED, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectState } from 'tl-react-bridge';
import { ARG_OPTION, CMD_GOTO, OptionImage, ReadonlyValue, withPill } from './selectOptions';
import type { OptionDescriptor } from './selectOptions';

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
 * The server hands this control the complete option list as soon as it is displayed - there is no
 * moment at which it could ask for it.
 */
const TLOptionChips: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<DropdownSelectState>();
  const sendCommand = useTLCommand();

  const value = (state.value ?? []) as OptionDescriptor[];
  const options = (state.options ?? []) as OptionDescriptor[];
  const multiSelect = state.multiSelect === true;
  const mandatory = state.mandatory === true;
  const editable = state.editable !== false;
  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;

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
    [multiSelect, mandatory, send]
  );

  /** Leads to the place the given option is displayed at. */
  const goto = useCallback(
    (optionValue: string) => {
      sendCommand(CMD_GOTO, { [ARG_OPTION]: optionValue });
    },
    [sendCommand]
  );

  if (!editable) {
    return (
      <div id={controlId} className={rootClassName(state, 'tlOptionChips', 'tlOptionChips--immutable')}>
        {value.map((v) => (
          <ReadonlyValue key={v.value} option={v} onGoto={goto} />
        ))}
      </div>
    );
  }

  return (
    <div
      id={controlId}
      role="group"
      className={rootClassName(
        state,
        'tlOptionChips',
        hasError && 'tlOptionChips--error',
        !hasError && hasWarnings && 'tlOptionChips--warning'
      )}
    >
      {options.map((option) => {
        const selected = selectedIds.has(option.value);
        return (
          <button
            key={option.value}
            type="button"
            className={
              'tlOptionChips__chip' + (selected ? ' tlOptionChips__chip--selected' : '')
            }
            aria-pressed={selected}
            onClick={() => toggle(option)}
          >
            {withPill(option.color, (
              <>
                <OptionImage image={option.image} />
                <span className="tlOptionChips__chipLabel">{option.label}</span>
              </>
            ))}
          </button>
        );
      })}
    </div>
  );
};

export default TLOptionChips;
