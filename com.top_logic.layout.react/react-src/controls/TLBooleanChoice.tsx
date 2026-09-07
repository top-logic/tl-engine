import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

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
 */
const TLBooleanChoice: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();
  const options = (state.options as BooleanOption[]) ?? [];
  const asSelect = state.presentation === 'select';
  const disabled = state.disabled === true;
  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;

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
      <span id={controlId} className="tlBooleanChoice tlBooleanChoice--immutable">
        {current >= 0 ? options[current].label : ''}
      </span>
    );
  }

  const cls = [
    'tlBooleanChoice',
    hasError ? 'tlBooleanChoice--error' : '',
    !hasError && hasWarnings ? 'tlBooleanChoice--warning' : '',
  ].filter(Boolean).join(' ');

  if (asSelect) {
    return (
      <select
        id={controlId}
        className={cls + ' tlReactSelect'}
        value={current >= 0 ? String(current) : ''}
        disabled={disabled}
        aria-invalid={hasError || undefined}
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
    <span id={controlId} className={cls + ' tlBooleanChoice--radio'} role="radiogroup"
      aria-invalid={hasError || undefined}>
      {options.map((option, index) => (
        <label key={index} className="tlBooleanChoice__option">
          <input
            type="radio"
            name={controlId}
            checked={current === index}
            disabled={disabled}
            onChange={() => handleSelect(index)}
          />
          <span className="tlBooleanChoice__label">{option.label}</span>
        </label>
      ))}
    </span>
  );
};

export default TLBooleanChoice;
