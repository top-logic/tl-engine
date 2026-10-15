import { React, ThemeIcon, tooltipProps, usePart, ICON_REMOVE } from 'tl-react-bridge';
import type { ChipPartProps, ColorRole, PartRef } from 'tl-react-bridge';

/** The class of a pill of the design system: a value set apart in a tinted capsule. */
const PILL_CLASS = 'tl-pill';

/** The class of a chip of the design system: a toggle, or a chip holding a toggle and a remove button. */
const CHIP_CLASS = 'tl-chip';

/** The class of the design system laying out a pill that holds buttons (drag handle, remove) inside it. */
const REMOVABLE_CLASS = 'tl-select__chip';

/** The class of the button inside a chip that toggles it or runs its action, beside its remove button. */
const TOGGLE_CLASS = 'tl-chip__toggle';

/** The class of the button removing the value of a chip. */
const REMOVE_CLASS = 'tl-chip__remove';

/** The type style of every chip. */
const TYPE_CLASS = 'tl-type-label';

/** The class of the less prominent text after the label. */
const DETAIL_CLASS = 'tlChip__detail tl-nums';

/** The classes, without the empty ones, joined. */
function classes(...names: (string | false | undefined)[]): string {
  return names.filter(Boolean).join(' ');
}

/** The design system's `data-tl-role` of the given color role; the neutral role is the plain look. */
function roleAttr(role: ColorRole | undefined): string | undefined {
  return role && role !== 'neutral' ? role : undefined;
}

/**
 * The TopLogic drawing of the part `Chip` ({@link ChipPartProps}), with the pill and the chip of the
 * design system. The color role is the design system's `data-tl-role`.
 *
 * <ul>
 * <li>A chip that only shows its value is a pill (tl-pill), the label a `tl-pill__label`.</li>
 * <li>A toggle or a chip running an action is a chip (tl-chip), a button whose `aria-pressed` the
 *     design system draws, the label a `tl-chip__label`, which ends with an ellipsis.</li>
 * <li>A removable toggle or action is a chip holding the toggle (tl-chip__toggle) and the remove
 *     button (tl-chip__remove).</li>
 * <li>A removable value is a pill with the class `tl-select__chip`, which lays out the drag handle
 *     and the remove button inside the rounded ends: the remove button is the part `Button`,
 *     icon-only, ghost and small.</li>
 * </ul>
 *
 * <p>The design system marks the state of the value of a toggle on the group of its field
 * (tl-chip-group), not on the chip: the control puts it there, so {@link ChipPartProps.validation} is
 * not drawn here.</p>
 */
const ChipPart: React.FC<ChipPartProps> = ({
  id, ref, label, detail, icon, colorRole, pressed, validation: _validation, disabled, tabIndex, tooltip, className,
  onClick, onRemove, removeLabel, handle, dragSource, ...attributes
}) => {
  const Button = usePart('Button');
  const pressable = pressed !== undefined || onClick !== undefined;
  const removable = onRemove !== undefined;
  const dataRole = roleAttr(colorRole);

  const content = (labelClass: string, iconClass: string) => (
    <>
      {icon && <ThemeIcon encoded={icon} className={iconClass} />}
      <span className={labelClass}>{label}</span>
      {detail !== undefined && <span className={DETAIL_CLASS}>{detail}</span>}
    </>
  );
  const removeButton = removable && (
    <Button
      label={removeLabel}
      image={ICON_REMOVE}
      display="icon-only"
      appearance="ghost"
      small
      disabled={disabled}
      tabIndex={tabIndex}
      className={pressable ? REMOVE_CLASS : undefined}
      onClick={onRemove}
    />
  );

  if (!pressable) {
    return (
      <span id={id} ref={ref as PartRef<HTMLSpanElement>}
        className={classes(PILL_CLASS, TYPE_CLASS, (removable || !!handle) && REMOVABLE_CLASS, className)}
        data-tl-role={dataRole} {...tooltipProps(tooltip)} {...dragSource} {...attributes}>
        {handle}
        {content('tl-pill__label', 'tl-pill__icon tl-icon-sm')}
        {removeButton}
      </span>
    );
  }
  const chipContent = content('tl-chip__label', 'tl-icon-sm');
  if (!removable) {
    return (
      <button
        type="button"
        id={id}
        ref={ref as PartRef<HTMLButtonElement>}
        className={classes(CHIP_CLASS, TYPE_CLASS, className)}
        data-tl-role={dataRole}
        aria-pressed={pressed}
        disabled={disabled}
        tabIndex={tabIndex}
        onClick={onClick}
        {...tooltipProps(tooltip)}
        {...dragSource}
        {...attributes}
      >
        {chipContent}
      </button>
    );
  }
  return (
    <span id={id} ref={ref as PartRef<HTMLSpanElement>} className={classes(CHIP_CLASS, TYPE_CLASS, className)}
      data-tl-role={dataRole} {...dragSource} {...attributes}>
      {handle}
      <button
        type="button"
        className={TOGGLE_CLASS}
        aria-pressed={pressed}
        disabled={disabled}
        tabIndex={tabIndex}
        onClick={onClick}
        {...tooltipProps(tooltip)}
      >
        {chipContent}
      </button>
      {removeButton}
    </span>
  );
};

export default ChipPart;
