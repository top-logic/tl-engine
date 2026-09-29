import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

interface SelectOption {
  value: string;
  label: string;
}

/**
 * A select dropdown rendered via React.
 */
const TLSelect: React.FC<TLCellProps> = ({ controlId, state, config }) => {
  const [value, setValue] = useTLFieldValue();

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLSelectElement>) => {
      setValue(e.target.value || null);
    },
    [setValue]
  );

  const options = ((state.options ?? config?.options) as SelectOption[]) ?? [];

  if (state.editable === false) {
    const selectedLabel = options.find((opt) => opt.value === value)?.label ?? '';
    return (
      <span id={controlId} className="tlReactSelect tlReactSelect--immutable">
        {selectedLabel}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const hasWarnings = state.hasWarnings === true;
  const cls = [
    'tlReactSelect',
    hasError ? 'tlReactSelect--error' : '',
    !hasError && hasWarnings ? 'tlReactSelect--warning' : '',
  ].filter(Boolean).join(' ');

  return (
    <span id={controlId}>
      <select
        value={(value as string) ?? ''}
        onChange={handleChange}
        disabled={state.disabled === true}
        className={cls}
        aria-invalid={hasError || undefined}
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
