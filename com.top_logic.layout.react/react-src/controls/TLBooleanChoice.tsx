import { React, useTLFieldValue, rootClassName, useFieldLabelProps } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { fieldStateAttrs } from './form/fieldState';
import { FieldValue } from './form/FieldValue';

const { useCallback } = React;

interface BooleanOption {
  value: boolean | null;
  label: string;
}

/**
 * A boolean field offering its values as a choice — radio buttons or a select — rendered via React.
 *
 * The server states which presentation the attribute asks for and supplies the labelled options; a
 * tri-state field has a third option for "no value".
 *
 * A radio option's text names its radio through the `label` wrapping both, which also makes the
 * text part of the radio's hit area. In a form field, the field's label names the choice as a whole:
 * the select, or the radio group (see FieldLabelContext).
 *
 * Design system: radios are `tl-radio` in `tl-choice` labels within a `tl-choice-group`, the select
 * is a `tl-field`; the state is an attribute (see fieldStateAttrs). A field that is not editable
 * shows the label of its value as a `tl-field-value`.
 */
const TLBooleanChoice: React.FC<TLCellProps> = ({ controlId, state }) => {
  const labelProps = useFieldLabelProps(controlId, controlId);
  const [value, setValue] = useTLFieldValue();
  const options = (state.options as BooleanOption[]) ?? [];
  const asSelect = state.presentation === 'select';
  const disabled = state.disabled === true;

  // The value travels as a boolean or null; over the wire an option is addressed by its index, so
  // that "no value" is distinguishable from "not chosen".
  const handleSelect = useCallback(
    (index: number) => {
      const option = options[index];
      setValue(option ? option.value : null);
    },
    [options, setValue]
  );

  const current = options.findIndex((option) => option.value === (value ?? null));

  if (state.editable === false) {
    return (
      <FieldValue id={controlId} className={rootClassName(state)} text={current >= 0 ? options[current].label : ''} />
    );
  }

  if (asSelect) {
    return (
      <select
        id={controlId}
        {...labelProps}
        className={rootClassName(state, 'tl-field tl-type-body')}
        value={current >= 0 ? String(current) : ''}
        disabled={disabled}
        {...fieldStateAttrs(state)}
        onChange={(e) => handleSelect(Number(e.target.value))}
      >
        {current < 0 && <option value="" />}
        {options.map((option, index) => (
          <option key={index} value={String(index)}>{option.label}</option>
        ))}
      </select>
    );
  }

  return (
    <span id={controlId} className={rootClassName(state, 'tl-choice-group')} role="radiogroup"
      {...fieldStateAttrs(state)} {...labelProps}>
      {options.map((option, index) => (
        <label key={index} className="tl-choice tl-type-body">
          <input
            type="radio"
            className="tl-radio"
            name={controlId}
            checked={current === index}
            disabled={disabled}
            onChange={() => handleSelect(index)}
          />
          {option.label}
        </label>
      ))}
    </span>
  );
};

export default TLBooleanChoice;
