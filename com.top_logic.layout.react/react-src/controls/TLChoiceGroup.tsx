import { React, useTLState, useTLCommand, CMD_VALUE_CHANGED, rootClassName, useFieldLabelProps } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson } from 'tl-react-bridge';
import { ARG_OPTION, CMD_GOTO, OptionContent, ReadonlyValues } from './selectOptions';
import type { OptionDescriptor } from './selectOptions';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';

const { useCallback, useMemo, useRef } = React;

/** The orientation laying the options out side by side; any other lays them out one below the other. */
const ORIENTATION_HORIZONTAL: DropdownSelectStateJson.Orientation = 'horizontal';

/**
 * A select field offering every option as a radio button, or as a checkbox where it takes several
 * values.
 *
 * Every option is read at a glance together with its label, which suits a handful of mutually
 * exclusive options in a form. A field taking one value that is not mandatory offers the choice of
 * no value as a radio of its own, the first one, labelled with the empty option label.
 *
 * The inputs are native: the radios of a field share one name, so the browser makes the group one
 * stop in the tab order whose arrow keys move the selection; each checkbox is a tab stop of its own
 * and toggles with the space key. An option's text names its input through the `label` wrapping
 * both, which also makes the text part of the input's hit area; the field's label names the group as
 * a whole (see FieldLabelContext).
 *
 * Design system: radios are `tl-radio`, checkboxes `tl-checkbox`, each in a `tl-choice` label within a
 * `tl-choice-group` (role `radiogroup` for one value, `group` for several). The options stand one
 * below the other (`tl-choice-group--vertical`) unless the server asks for them side by side, where
 * they wrap onto further lines. The field's state is an attribute (see fieldStateAttrs): on the radio
 * group, or on every checkbox, since a `group` takes no `aria-invalid`. A read-only field shows its
 * values in `tl-select__values`, as the dropdown does; a disabled field renders every input inactive
 * (native `disabled`, see showsValueOnly).
 *
 * The server hands this control the complete option list as soon as it is displayed - there is no
 * moment at which it could ask for it.
 */
const TLChoiceGroup: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DropdownSelectStateJson>>();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const sendCommand = useTLCommand();

  const value = (state.value ?? []) as OptionDescriptor[];
  const options = (state.options ?? []) as OptionDescriptor[];
  const multiSelect = state.multiSelect === true;
  const mandatory = state.mandatory === true;
  const disabled = state.disabled === true;
  const horizontal = state.orientation === ORIENTATION_HORIZONTAL;

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
      send(selected ? selection.filter((v) => v.value !== option.value) : [...selection, option]);
    },
    [disabled, send]
  );

  /** Chooses the given option as the only value, or no value where the option is absent. */
  const choose = useCallback(
    (option: OptionDescriptor | null) => {
      if (disabled) return;
      send(option ? [option] : []);
    },
    [disabled, send]
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
      <ReadonlyValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />
    );
  }

  const groupClass = horizontal ? 'tl-choice-group' : 'tl-choice-group tl-choice-group--vertical';

  if (multiSelect) {
    // A group takes no aria-invalid: each checkbox carries the field's check state itself.
    const checkboxAttrs = fieldStateAttrs({ hasError: state.hasError, hasWarnings: state.hasWarnings });
    return (
      <div id={controlId} {...labelProps} role="group" className={rootClassName(state, groupClass)}>
        {options.map((option) => (
          <label key={option.value} className="tl-choice tl-type-body">
            <input
              type="checkbox"
              className="tl-checkbox"
              checked={selectedIds.has(option.value)}
              disabled={disabled}
              {...checkboxAttrs}
              onChange={() => toggle(option)}
            />
            <OptionContent option={option} />
          </label>
        ))}
      </div>
    );
  }

  return (
    <div
      id={controlId}
      {...labelProps}
      role="radiogroup"
      className={rootClassName(state, groupClass)}
      {...fieldStateAttrs(state)}
    >
      {!mandatory && (
        <label className="tl-choice tl-type-body">
          <input
            type="radio"
            className="tl-radio"
            name={controlId}
            checked={value.length === 0}
            disabled={disabled}
            onChange={() => choose(null)}
          />
          <span>{state.emptyOptionLabel ?? ''}</span>
        </label>
      )}
      {options.map((option) => (
        <label key={option.value} className="tl-choice tl-type-body">
          <input
            type="radio"
            className="tl-radio"
            name={controlId}
            checked={selectedIds.has(option.value)}
            disabled={disabled}
            onChange={() => choose(option)}
          />
          <OptionContent option={option} />
        </label>
      ))}
    </div>
  );
};

export default TLChoiceGroup;
