import { React, ThemeIcon, usePart } from 'tl-react-bridge';
import type { ColorRole, OptionDescriptor } from 'tl-react-bridge';

export type { OptionDescriptor } from 'tl-react-bridge';

/** Command sent when the user follows the link of a displayed option. */
export const CMD_GOTO = 'goto';

/** Argument of {@link CMD_GOTO}: the value of the option to display. */
export const ARG_OPTION = 'option';

/**
 * Command entering a text of the user's own as the value of a select field that accepts free entry
 * (freeEntry). Must match `EnterTextArguments#COMMAND`.
 */
export const CMD_ENTER_TEXT = 'enterText';

/** Argument of {@link CMD_ENTER_TEXT}: the text entered. Must match `EnterTextArguments#TEXT`. */
export const ARG_TEXT = 'text';

/**
 * Renders an option as its image followed by its label, for an element that carries the option's
 * color role itself (data-tl-role, see {@link optionRoleAttr}): a segment. Image and label are
 * siblings; nothing is nested in a pill.
 *
 * @param option the option to render
 * @param label what stands for the label, e.g. the label with the search match set off; the
 *        option's label where omitted
 * @param labelClassName the class of the element holding the label, if any
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
  return (
    <>
      {option.image && <ThemeIcon encoded={option.image} className="tl-icon-sm" />}
      <span className={labelClassName}>{label ?? option.label}</span>
    </>
  );
}

/**
 * Renders an option where the element showing it carries no role of its own - a row of the open list,
 * the single chosen value, the label of a choice: the part `Chip` of the option's color role, or image
 * and label as they are for a value without a role (see {@link OptionContent}).
 *
 * @param option the option to render
 * @param label what stands for the label, e.g. the label with the search match set off; the
 *        option's label where omitted
 * @param labelClassName the class of the element holding the label of an option without a color
 *        role, if any
 */
export function OptionLabel({
  option,
  label,
  labelClassName,
}: {
  option: OptionDescriptor;
  label?: React.ReactNode;
  labelClassName?: string;
}) {
  const Chip = usePart('Chip');
  if (!option.colorRole) {
    return <OptionContent option={option} label={label} labelClassName={labelClassName} />;
  }
  return <Chip colorRole={option.colorRole as ColorRole} icon={option.image} label={label ?? option.label} />;
}

/** The data-tl-role an element showing the option carries: its color role, none for a neutral one. */
export function optionRoleAttr(option: OptionDescriptor): string | undefined {
  const role = option.colorRole;
  return role && role !== 'neutral' ? role : undefined;
}
