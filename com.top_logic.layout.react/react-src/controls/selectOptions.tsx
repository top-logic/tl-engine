import { React, ThemeIcon } from 'tl-react-bridge';
import type { DropdownSelectStateJson } from 'tl-react-bridge';
import { TLPill, roleAttr } from './pill/TLPill';
import { FieldValue } from './form/FieldValue';

const { useCallback } = React;

/**
 * One option of a select field, as the server describes it.
 *
 * <p>
 * The same descriptor carries an option of the list to pick from and a value that is picked, so a
 * value looks the same wherever a control shows it. The server always sends its value and label.
 * </p>
 */
export type OptionDescriptor = Partial<DropdownSelectStateJson.Option>
  & Required<Pick<DropdownSelectStateJson.Option, 'value' | 'label'>>;

/** Command sent when the user follows the link of a displayed option. */
export const CMD_GOTO = 'goto';

/** Argument of {@link CMD_GOTO}: the value of the option to display. */
export const ARG_OPTION = 'option';

/**
 * Renders an option's image, whatever encoded form it arrives in, as a small icon.
 *
 * @param image the encoded image of the option, none where absent
 * @param inPill whether the icon stands in a pill (`tl-pill__icon`)
 */
export function OptionImage({ image, inPill = false }: { image?: string; inPill?: boolean }) {
  if (!image) return null;
  const className = inPill ? 'tl-icon-sm tl-pill__icon' : 'tl-icon-sm';
  if (image.startsWith('/')) {
    return <img src={image} alt="" className={className} />;
  }
  return <ThemeIcon encoded={image} className={className} />;
}

/**
 * Renders an option as its image followed by its label, for an element that carries the option's
 * color role itself (data-tl-role, see {@link optionRoleAttr}): a chip, a segment. Image and label
 * are siblings; nothing is nested in a pill.
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
      <OptionImage image={option.image} />
      <span className={labelClassName}>{label ?? option.label}</span>
    </>
  );
}

/**
 * Renders an option where the element showing it carries no role of its own - a row of the open list,
 * the single chosen value: a `tl-pill` of the option's color role (image and label side by side in
 * it, the label in `tl-pill__label`), or image and label as they are for a value without a role, the
 * label in `labelClassName`.
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
  if (!option.colorRole) {
    return <OptionContent option={option} label={label} labelClassName={labelClassName} />;
  }
  return (
    <TLPill role={option.colorRole} className="tl-type-label" icon={<OptionImage image={option.image} inPill />}>
      {label ?? option.label}
    </TLPill>
  );
}

/** The data-tl-role an element showing the option carries: its color role, none for a neutral one. */
export function optionRoleAttr(option: OptionDescriptor): string | undefined {
  return roleAttr(option.colorRole);
}

/**
 * Renders a selected value of a field that only displays its value, rendered by FieldValue, the one
 * read-only value (N1): a `tl-field-value`, with the image through ThemeIcon and a value with a color
 * role as a pill carrying `data-tl-role`.
 *
 * <p>A value the application displays somewhere is a link there (the inner kind, an `a` sending
 * {@link CMD_GOTO}), and wears the same look as the linked value of a table cell.</p>
 */
export function ReadonlyValue({
  option,
  onGoto,
}: {
  option: OptionDescriptor;
  onGoto: (value: string) => void;
}) {
  const goto = useCallback(() => onGoto(option.value), [onGoto, option.value]);
  return (
    <FieldValue
      text={option.label}
      icon={option.image}
      role={option.colorRole}
      onGoto={option.link ? goto : undefined}
    />
  );
}

/**
 * Renders the values of a select field that only displays its value: a `tl-select__values` holding
 * one {@link ReadonlyValue} per value. An empty selection renders an empty list - the "empty
 * option" label is an edit affordance and would mislead in a read-only display.
 *
 * @param id the id of the element, the control id of the field
 * @param className further classes, e.g. the result of rootClassName
 * @param value the selected values
 * @param onGoto what follows the link of a value
 */
export function ReadonlyValues({
  id,
  className,
  value,
  onGoto,
}: {
  id?: string;
  className?: string;
  value: OptionDescriptor[];
  onGoto: (value: string) => void;
}) {
  return (
    <span id={id} className={className ? 'tl-select__values ' + className : 'tl-select__values'}>
      {value.map((v) => (
        <ReadonlyValue key={v.value} option={v} onGoto={onGoto} />
      ))}
    </span>
  );
}
