import { React, useI18N, usePart, ICON_CLEAR } from 'tl-react-bridge';
import { showsValueOnly } from './fieldState';

const I18N_KEYS = {
  'js.clearButton.label': 'Clear the input',
};

/**
 * Whether a field whose value cannot be emptied by deleting it - a date, a time of day, a selection -
 * offers its clear button: while it is editable, not mandatory and holds a value. Whether it may be
 * emptied is the model's decision (the field's `mandatory`), not a setting of the field.
 *
 * @param state the control state with the keys of ReactFormFieldControl
 * @param hasValue whether the field holds a value
 */
export function offersClearByModel(
  state: { editable?: unknown; disabled?: unknown; mandatory?: unknown },
  hasValue: boolean,
): boolean {
  return hasValue && state.mandatory !== true && state.disabled !== true && !showsValueOnly(state);
}

/**
 * Whether a field the user empties by deleting what is typed - a text, a password, a number -
 * offers its clear button: only where the field asks for one (`clearable`, e.g. a search field),
 * while it is editable and holds a value.
 *
 * @param state the control state with the keys of ReactFormFieldControl and `clearable`
 * @param hasValue whether the field holds a value
 */
export function offersClearOnRequest(
  state: { editable?: unknown; disabled?: unknown; clearable?: unknown },
  hasValue: boolean,
): boolean {
  return hasValue && state.clearable === true && state.disabled !== true && !showsValueOnly(state);
}

/**
 * The button that empties a field at once, drawn by the part `Button`: the clear icon alone, small
 * and without a frame, named by its label for assistive technology and as its tooltip.
 *
 * The one clear button of the fields: a text, a password, a number, a date and a selection. A field shows it
 * where offersClearByModel or offersClearOnRequest says so; what a click does - writing the empty
 * value, moving the focus - is the field's.
 */
export function ClearButton({
  label,
  onClick,
}: {
  /** The name of the button; absent: a name saying that the input is cleared. */
  label?: string;
  /** Called when the button is pressed. */
  onClick: (event: React.MouseEvent<HTMLElement>) => void;
}) {
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');
  return (
    <Button
      label={label ?? i18n['js.clearButton.label']}
      image={ICON_CLEAR}
      display="icon-only"
      appearance="ghost"
      small
      onClick={onClick}
    />
  );
}
