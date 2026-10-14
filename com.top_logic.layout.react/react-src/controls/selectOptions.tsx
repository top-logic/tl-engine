import { React, ThemeIcon, usePart } from 'tl-react-bridge';
import type { ChipTone, OptionDescriptor } from 'tl-react-bridge';

export type { OptionDescriptor } from 'tl-react-bridge';

/** Command sent when the user follows the link of a displayed option. */
export const CMD_GOTO = 'goto';

/** Argument of {@link CMD_GOTO}: the value of the option to display. */
export const ARG_OPTION = 'option';

/**
 * Draws a value's presentation as the part `Chip` of the given color role, where the model gives the
 * value one.
 *
 * <p>Used for every presentation of an option - the rows of an open dropdown, the chosen value while
 * editing, the segments of a segmented choice and the labels of a choice group - and of a colored
 * object in a cell, so a colored value looks the same wherever a control shows it.</p>
 *
 * @param role the color role of the value; absent: the value is drawn as it is
 * @param icon the image of the value, drawn ahead of its label
 * @param label the label of the value
 * @param children what stands for a value without a color role
 */
export function Pill({
  role,
  icon,
  label,
  children,
}: {
  role?: string;
  icon?: string;
  label: React.ReactNode;
  children: React.ReactNode;
}) {
  const Chip = usePart('Chip');
  return role ? <Chip tone={role as ChipTone} icon={icon} label={label} /> : <>{children}</>;
}

/**
 * Renders an option as its image followed by its label, as the part `Chip` where the option has a
 * color role (see {@link Pill}).
 *
 * @param option the option to render
 * @param label what stands for the label, e.g. the label with the search match set off; the
 *        option's label where omitted
 * @param labelClassName the class of the element holding the label of an option without a color
 *        role, if any
 */
export function OptionContent({
  option,
  label,
  labelClassName,
}: {
  option: OptionDescriptor;
  label?: React.ReactNode;
  labelClassName?: string;
}) {
  const text = label ?? option.label;
  return (
    <Pill role={option.colorRole} icon={option.image} label={text}>
      <ThemeIcon encoded={option.image} className="tl-icon-sm" />
      <span className={labelClassName}>{text}</span>
    </Pill>
  );
}
