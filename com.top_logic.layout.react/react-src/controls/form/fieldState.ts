/**
 * The state attributes of a form field, from the field model's state. Error wins over warning:
 * a field with an error shows no warning. The design system reads exactly these attributes
 * (tl-field, tl-checkbox, tl-choice-group); a class for a state does not exist.
 *
 * @param state the control state with the keys of ReactFormFieldControl
 * @param ariaInvalidAllowed false for elements whose role does not allow aria-invalid (role="group"),
 *        which then carry data-tl-state="error"; such a role allows no aria-required either, so a
 *        mandatory group carries none
 */
export function fieldStateAttrs(
  state: { hasError?: unknown; hasWarnings?: unknown; mandatory?: unknown },
  ariaInvalidAllowed = true,
): Record<string, string> {
  const attrs: Record<string, string> = {};
  if (state.hasError === true) {
    if (ariaInvalidAllowed) attrs['aria-invalid'] = 'true';
    else attrs['data-tl-state'] = 'error';
  } else if (state.hasWarnings === true) {
    attrs['data-tl-state'] = 'warning';
  }
  if (ariaInvalidAllowed && state.mandatory === true) attrs['aria-required'] = 'true';
  return attrs;
}
