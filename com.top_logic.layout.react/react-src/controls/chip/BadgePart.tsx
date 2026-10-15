import { React } from 'tl-react-bridge';
import type { BadgePartProps } from 'tl-react-bridge';

/**
 * The classes of the badge of the design system: the marker (tl-badge) with the strong label type
 * and digits of equal width, which the design system leaves to the element.
 */
const BADGE_CLASS = 'tl-badge tl-type-label-strong tl-nums';

/** The TopLogic drawing of the part `Badge` ({@link BadgePartProps}): the badge of the design system. */
const BadgePart: React.FC<BadgePartProps> = ({ id, content, className, ...attributes }) => (
  <span id={id} className={className ? BADGE_CLASS + ' ' + className : BADGE_CLASS} {...attributes}>
    {content}
  </span>
);

export default BadgePart;
