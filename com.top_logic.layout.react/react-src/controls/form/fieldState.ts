/**
 * The state attributes of a form field, from the field model's state. Error wins over warning:
 * a field with an error shows no warning. The design system reads exactly these attributes
 * (tl-field, tl-checkbox, tl-choice-group); a class for a state does not exist.
 *
 * <p>`disabled` is not among these attributes: every control sets it on its element itself, from
 * `state.disabled` (see {@link FieldStateJson.disabled} in `state/control-state.ts`).</p>
 *
 * @param state the control state with the keys of ReactFormFieldControl
 * @param roleAllowsAria whether the element's role allows `aria-invalid` and `aria-required`
 *        (ARIA 1.2: not on `role="group"`); where it does not, an error is carried as
 *        data-tl-state="error" and mandatory is not carried at all
 */
export function fieldStateAttrs(
  state: { hasError?: unknown; hasWarnings?: unknown; mandatory?: unknown },
  roleAllowsAria = true,
): Record<string, string> {
  const attrs: Record<string, string> = {};
  if (state.hasError === true) {
    if (roleAllowsAria) attrs['aria-invalid'] = 'true';
    else attrs['data-tl-state'] = 'error';
  } else if (state.hasWarnings === true) {
    attrs['data-tl-state'] = 'warning';
  }
  if (roleAllowsAria && state.mandatory === true) attrs['aria-required'] = 'true';
  return attrs;
}

/**
 * Whether a form field displays its value only instead of an input: it is not editable and not
 * disabled.
 *
 * <p>A field that is not editable is either read-only or disabled. A read-only field shows its
 * value as text (see FieldValue). A disabled field renders the input it has while editable, as an
 * inactive input: natively `disabled` where the element supports it, `aria-disabled` and out of the
 * tab order otherwise, with every affordance that changes the value (clear button, popup, drag,
 * file picker) left out. The server never sends a disabled field as editable and ignores value
 * changes to it.</p>
 *
 * @param state the control state with the keys of ReactFormFieldControl
 */
export function showsValueOnly(state: { editable?: unknown; disabled?: unknown }): boolean {
  return state.editable === false && state.disabled !== true;
}
