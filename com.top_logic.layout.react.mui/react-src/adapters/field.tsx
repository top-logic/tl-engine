import { React } from 'tl-react-bridge';
import type { FieldStateJson } from 'tl-react-bridge';

/** The style of the element carrying the control ID around an MUI input: a block of full width. */
export const FIELD_ROOT_STYLE: React.CSSProperties = { display: 'block' };

/**
 * Whether a form field displays its value only instead of an input: it is not editable and not
 * disabled. A disabled field renders its input as an inactive one.
 */
export function showsValueOnly(state: Partial<FieldStateJson>): boolean {
  return state.editable === false && state.disabled !== true;
}

/**
 * The ARIA attributes of a field's focusable element for the state of its value: `aria-invalid`
 * for an error, `aria-required` for a mandatory field.
 */
export function fieldAriaProps(state: Partial<FieldStateJson>): Record<string, string> {
  const attrs: Record<string, string> = {};
  if (state.hasError === true) attrs['aria-invalid'] = 'true';
  if (state.mandatory === true) attrs['aria-required'] = 'true';
  return attrs;
}

/**
 * The MUI color of a field input for the state of its value: `warning` for a value that is
 * questionable without being invalid. An error is shown through the `error` prop instead.
 */
export function fieldColor(state: Partial<FieldStateJson>): 'primary' | 'warning' {
  return state.hasError !== true && state.hasWarnings === true ? 'warning' : 'primary';
}
