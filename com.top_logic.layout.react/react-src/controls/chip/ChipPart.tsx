import { React, ThemeIcon, tooltipProps, usePart } from 'tl-react-bridge';
import type { ChipPartProps, ChipTone, PartRef } from 'tl-react-bridge';

/** The icon of the button removing the value of a chip. */
const REMOVE_ICON = 'css:fa-solid fa-xmark';

/** The classes of a pill of the design system: a value set apart in a tinted capsule. */
const PILL_CLASS = 'tl-pill';

/** The classes of a chip of the design system: a toggle that is pressed or not. */
const CHIP_CLASS = 'tl-chip';

/** The class of the design system laying out a chip that holds a remove button beside its label. */
const REMOVABLE_CLASS = 'tl-select__chip';

/** The type style of every chip. */
const TYPE_CLASS = 'tl-type-label';

/**
 * The class of the button inside a removable chip that runs the action of the chip or toggles it:
 * it takes the whole chip but the remove button, and draws nothing of its own.
 */
const ACTION_CLASS = 'tlChip__action';

/** The class of the less prominent text after the label. */
const DETAIL_CLASS = 'tlChip__detail tl-nums';

/** The classes, without the empty ones, joined. */
function classes(...names: (string | false | undefined)[]): string {
  return names.filter(Boolean).join(' ');
}

/** The classes of a pill of the given color role; the neutral role has no class of its own. */
function pillClassName(tone: ChipTone | undefined): string {
  return classes(PILL_CLASS, tone !== undefined && tone !== 'neutral' && PILL_CLASS + '--' + tone);
}

/**
 * The TopLogic drawing of the part `Chip` ({@link ChipPartProps}), with the pill and the chip of the
 * design system.
 *
 * <ul>
 * <li>A toggle is a chip (tl-chip), a button whose `aria-pressed` the design system draws; a color
 *     role tints the content as a pill inside it. The label is a `tl-chip__label`, which ends with
 *     an ellipsis.</li>
 * <li>Any other chip is a pill (tl-pill) of its color role, neutral without one; a pill running an
 *     action is a button. The label is a `tl-pill__label`.</li>
 * <li>A removable chip also carries the class `tl-select__chip`, which lays out the remove button
 *     inside the rounded end: the part `Button`, icon-only, ghost and small. Pressing or toggling
 *     the chip is then a button of its own (tlChip__action) beside the remove button.</li>
 * </ul>
 *
 * <p>The design system marks the state of the value of a toggle on the group of its field
 * (tl-chip-group), not on the chip: the control puts it there, so {@link ChipPartProps.validation} is
 * not drawn here.</p>
 */
const ChipPart: React.FC<ChipPartProps> = ({
  id, ref, label, detail, icon, tone, pressed, validation: _validation, disabled, tabIndex, tooltip, className,
  onClick, onRemove, removeLabel, ...attributes
}) => {
  const Button = usePart('Button');
  const toggle = pressed !== undefined;
  const pressable = toggle || onClick !== undefined;
  const removable = onRemove !== undefined;

  const labelContent = (
    <>
      {icon && <ThemeIcon encoded={icon} className={toggle ? 'tl-icon-sm' : 'tl-pill__icon tl-icon-sm'} />}
      <span className={toggle ? 'tl-chip__label' : 'tl-pill__label'}>{label}</span>
      {detail !== undefined && <span className={DETAIL_CLASS}>{detail}</span>}
    </>
  );
  // A toggle of a color role is a chip around a pill of that role.
  const content = toggle && tone !== undefined
    ? <span className={pillClassName(tone)}>{labelContent}</span>
    : labelContent;
  const rootClass = classes(toggle ? CHIP_CLASS : pillClassName(tone), removable && REMOVABLE_CLASS, TYPE_CLASS,
    className);

  if (!removable) {
    if (pressable) {
      return (
        <button
          type="button"
          id={id}
          ref={ref as PartRef<HTMLButtonElement>}
          className={rootClass}
          aria-pressed={pressed}
          disabled={disabled}
          tabIndex={tabIndex}
          onClick={onClick}
          {...tooltipProps(tooltip)}
          {...attributes}
        >
          {content}
        </button>
      );
    }
    return (
      <span id={id} ref={ref as PartRef<HTMLSpanElement>} className={rootClass} {...tooltipProps(tooltip)}
        {...attributes}>
        {content}
      </span>
    );
  }

  return (
    <span id={id} ref={ref as PartRef<HTMLSpanElement>} className={rootClass}
      {...(pressable ? {} : tooltipProps(tooltip))} {...attributes}>
      {pressable ? (
        <button
          type="button"
          className={ACTION_CLASS}
          aria-pressed={pressed}
          disabled={disabled}
          tabIndex={tabIndex}
          onClick={onClick}
          {...tooltipProps(tooltip)}
        >
          {content}
        </button>
      ) : content}
      <Button
        label={removeLabel}
        image={REMOVE_ICON}
        display="icon-only"
        appearance="ghost"
        small
        disabled={disabled}
        tabIndex={tabIndex}
        onClick={onRemove}
      />
    </span>
  );
};

export default ChipPart;
