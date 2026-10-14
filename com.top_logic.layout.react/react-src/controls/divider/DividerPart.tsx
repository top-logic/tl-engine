import { React } from 'tl-react-bridge';
import type { DividerPartProps } from 'tl-react-bridge';

/** The class of the divider of the design system. */
const DIVIDER_CLASS = 'tl-divider';

/** The class of a vertical divider of the design system, which stands in a row. */
const VERTICAL_CLASS = 'tl-divider--vertical';

/**
 * The TopLogic drawing of the part `Divider` ({@link DividerPartProps}): the divider of the design
 * system (tl-divider), a rule in the color of a separator. A vertical one (tl-divider--vertical)
 * stretches to the height of the row it stands in.
 */
const DividerPart: React.FC<DividerPartProps> = ({ orientation, className, ...attributes }) => {
  const vertical = orientation === 'vertical';
  const classes = [DIVIDER_CLASS, vertical ? VERTICAL_CLASS : '', className ?? ''].filter(Boolean).join(' ');
  return <hr className={classes} aria-orientation={vertical ? 'vertical' : undefined} {...attributes} />;
};

export default DividerPart;
