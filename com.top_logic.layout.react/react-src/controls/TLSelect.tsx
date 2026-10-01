import { React, useTLState, useTLFieldValue, rootClassName, useFieldLabelProps, fieldInputId } from 'tl-react-bridge';
import type { TLCellProps, SelectStateJson } from 'tl-react-bridge';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import { FieldValue } from './form/FieldValue';

const { useCallback } = React;

interface SelectOption {
  value: string;
  label: string;
}

/**
 * A select dropdown rendered via React.
 *
 * A read-only field shows the label of the selected option as a tl-field-value; a disabled field
 * renders the select as an inactive one (native `disabled`, see showsValueOnly).
 */
const TLSelect: React.FC<TLCellProps> = ({ controlId, config }) => {
  const state = useTLState<Partial<SelectStateJson>>();
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLSelectElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  // The options of this component carry string values.
  const options = (state.options ?? config?.options ?? []) as SelectOption[];

  if (showsValueOnly(state)) {
    const selectedLabel = options.find((opt) => opt.value === value)?.label ?? '';
    return <FieldValue id={controlId} className={rootClassName(state)} text={selectedLabel} />;
  }

  return (
    <span id={controlId}>
      <select
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={rootClassName(state, 'tl-field tl-type-body')}
        {...fieldStateAttrs(state)}
        id={inputId}
        {...labelProps}
      >
        {state.nullable !== false && <option value=""></option>}
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </span>
  );
};

export default TLSelect;
