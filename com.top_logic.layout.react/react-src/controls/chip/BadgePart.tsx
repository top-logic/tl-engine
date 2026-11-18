import { React } from 'tl-react-bridge';
import type { BadgePartProps } from 'tl-react-bridge';

/**
 * The classes of the badge of the design system: the marker (tl-badge) with the strong label type
 * and digits of equal width, which the design system leaves to the element.
 */
const BADGE_CLASS = 'tl-badge tl-type-label-strong tl-nums';

/** The modifier of the badge of the design system drawing a dot without content. */
const DOT_CLASS = 'tl-badge--dot';

/** The color role the badge of the design system draws without an attribute. */
const DEFAULT_ROLE = 'brand';

/**
 * The TopLogic drawing of the part `Badge` ({@link BadgePartProps}): the badge of the design system.
 * The color role is the attribute `data-tl-role`, a dot the modifier `tl-badge--dot`.
 */
const BadgePart: React.FC<BadgePartProps> = ({ id, content, dot, colorRole, label, className, ...attributes }) => {
  let classes = dot ? BADGE_CLASS + ' ' + DOT_CLASS : BADGE_CLASS;
  if (className) {
    classes += ' ' + className;
  }
  return (
    <span
      id={id}
      className={classes}
      data-tl-role={colorRole && colorRole !== DEFAULT_ROLE ? colorRole : undefined}
      role={label ? 'img' : undefined}
      aria-label={label}
      {...attributes}
    >
      {dot ? null : content}
    </span>
  );
};

export default BadgePart;
