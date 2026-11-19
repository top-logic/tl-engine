import { React } from 'tl-react-bridge';
import type { SkeletonPartProps } from 'tl-react-bridge';

/** The class of the skeleton of the design system; alone it is a rectangle. */
const SKELETON_CLASS = 'tl-skeleton';

/** The class of a line of body text of the design system. */
const TEXT_CLASS = 'tl-skeleton--text';

/** The class of a circle of the design system. */
const CIRCLE_CLASS = 'tl-skeleton--circle';

/**
 * The TopLogic drawing of the part `Skeleton` ({@link SkeletonPartProps}): the skeleton of the design
 * system (tl-skeleton), a recessed area pulsing slowly. A text skeleton (tl-skeleton--text) is as high
 * as a line of body text; several lines stand in an element of their own, which carries
 * `aria-hidden`, the class, the style and the attributes. A circle is tl-skeleton--circle.
 */
const SkeletonPart: React.FC<SkeletonPartProps> = ({ shape, lines, className, style, ...attributes }) => {
  const shapeClass = shape === 'text' ? TEXT_CLASS : shape === 'circle' ? CIRCLE_CLASS : '';
  const count = shape === 'text' ? Math.max(1, Math.floor(lines ?? 1)) : 1;
  if (count === 1) {
    const classes = [SKELETON_CLASS, shapeClass, className ?? ''].filter(Boolean).join(' ');
    return <span className={classes} style={style} {...attributes} aria-hidden="true" />;
  }
  const lineClasses = SKELETON_CLASS + ' ' + TEXT_CLASS;
  return (
    <div className={className} style={style} {...attributes} aria-hidden="true">
      {Array.from({ length: count }, (_, index) => <span key={index} className={lineClasses} />)}
    </div>
  );
};

export default SkeletonPart;
