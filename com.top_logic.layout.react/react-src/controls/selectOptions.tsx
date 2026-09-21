import { React } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';
import { TLPill } from './pill/TLPill';

const { useCallback } = React;

/**
 * One option of a select field, as the server describes it.
 *
 * <p>
 * The same descriptor carries an option of the list to pick from and a value that is picked, so a
 * value looks the same wherever a control shows it.
 * </p>
 */
export interface OptionDescriptor {
  value: string;
  label: string;
  image?: string;
  /** The CSS color the value carries in the model, if any. */
  color?: string;
  /** Whether the option leads to the place the application displays it at. */
  link?: boolean;
}

/** Command sent when the user follows the link of a displayed option. */
export const CMD_GOTO = 'goto';

/** Argument of {@link CMD_GOTO}: the value of the option to display. */
export const ARG_OPTION = 'option';

/**
 * Wraps a value's presentation in a pill when the model gives that value a color.
 *
 * <p>
 * Used for every presentation of an option - the rows of an open dropdown, the toggles of a chip
 * cloud, the chips of the selection while editing, and the read-only display - so a colored value
 * looks the same wherever a control shows it.
 * </p>
 */
export function withPill(color: string | undefined, content: React.ReactNode) {
  return color ? <TLPill color={color}>{content}</TLPill> : content;
}

/** Renders an option's image, whatever encoded form it arrives in. */
export function OptionImage({ image }: { image?: string }) {
  if (!image) return null;
  if (image.startsWith('/')) {
    return <img src={image} alt="" className="tlOptionImage" />;
  }
  return <ThemeIcon encoded={image} className="tlOptionIcon" />;
}

/**
 * Renders a selected value of a field that only displays its value.
 *
 * <p>A value the application displays somewhere is a link there, and wears the same look as the
 * linked value of a table cell.</p>
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

  const content = withPill(option.color, (
    <>
      <OptionImage image={option.image} />
      <span>{option.label}</span>
    </>
  ));

  if (option.link) {
    return (
      <a className="tlOptionValue tlResourceCell" href="#" onClick={handleClick}>
        {content}
      </a>
    );
  }
  return <span className="tlOptionValue">{content}</span>;
}
