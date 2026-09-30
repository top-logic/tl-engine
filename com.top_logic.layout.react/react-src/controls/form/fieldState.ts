/**
 * The state attributes of a form field, from the field model's state. Error wins over warning:
 * a field with an error shows no warning. The design system reads exactly these attributes
 * (tl-field, tl-checkbox, tl-choice-group); a class for a state does not exist.
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
