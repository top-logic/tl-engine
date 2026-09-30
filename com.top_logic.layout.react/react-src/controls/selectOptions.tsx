import { React, ThemeIcon } from 'tl-react-bridge';
import type { DropdownSelectStateJson } from 'tl-react-bridge';
import { TLPill } from './pill/TLPill';

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
 * Wraps a value's presentation in a pill when the model gives that value a color role.
 *
 * <p>
 * Used for every presentation of an option - the rows of an open dropdown, the toggles of a chip
 * cloud, the chosen value while editing, and the read-only display - so a colored value looks the
 * same wherever a control shows it. The pill sets its text in the label style (`tl-type-label`),
 * as the design system's select does.
 * </p>
 */
export function withPill(role: string | undefined, content: React.ReactNode) {
  return role ? <TLPill role={role} className="tl-type-label">{content}</TLPill> : content;
}

/** Renders an option's image, whatever encoded form it arrives in, as a small icon. */
export function OptionImage({ image }: { image?: string }) {
  if (!image) return null;
  if (image.startsWith('/')) {
    return <img src={image} alt="" className="tl-icon-sm" />;
  }
  return <ThemeIcon encoded={image} className="tl-icon-sm" />;
}

/**
 * Renders an option as its image followed by its label, as a pill where the option has a color
 * role.
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
  return withPill(option.colorRole, (
    <>
      <OptionImage image={option.image} />
      <span className={labelClassName}>{label ?? option.label}</span>
    </>
  ));
}

/**
 * Renders a selected value of a field that only displays its value: a `tl-field-value`.
 *
 * <p>A value the application displays somewhere is a link there (an `a` sending {@link CMD_GOTO}),
 * and wears the same look as the linked value of a table cell. The text stands in a
 * `tl-field-value__text`, in a pill where the value has a color role.</p>
 */
export function ReadonlyValue({
  option,
  onGoto,
}: {
  option: OptionDescriptor;
  onGoto: (value: string) => void;
}) {
  const handleClick = useCallback(
    (e: React.MouseEvent) => {
      e.preventDefault();
      onGoto(option.value);
    },
    [onGoto, option.value]
  );

  const content = <OptionContent option={option} labelClassName="tl-field-value__text" />;

  if (option.link) {
    return (
      <a className="tl-field-value tl-type-body" href="#" onClick={handleClick}>
        {content}
      </a>
    );
  }
  return <span className="tl-field-value tl-type-body">{content}</span>;
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
