import { React } from 'tl-react-bridge';
import type { SkeletonPartProps, SkeletonShape } from 'tl-react-bridge';
import Skeleton from '@mui/material/Skeleton';
import useMediaQuery from '@mui/material/useMediaQuery';

/** The MUI variant drawing each shape of the part. */
const VARIANT: Record<SkeletonShape, 'text' | 'rectangular' | 'circular'> = {
  text: 'text',
  rect: 'rectangular',
  circle: 'circular',
};

/** The media query of a viewer who asked for reduced motion. */
const REDUCED_MOTION_QUERY = '(prefers-reduced-motion: reduce)';

/** The width of the last of several lines: the end of a paragraph. */
const LAST_LINE_WIDTH = '50%';

/**
 * Draws the part `Skeleton` ({@link SkeletonPartProps}) with the MUI `Skeleton`.
 *
 * <p>Mapping from the props: shape → `variant` (`text` → `text`, `rect` → `rectangular`, `circle` →
 * `circular`, absent → `rectangular`); lines of text above one → as many text skeletons in an element
 * of their own, the last one half as wide; the skeleton pulses (`animation="pulse"`), for a viewer who
 * asked for reduced motion it stands still (`animation={false}`); it is always `aria-hidden`;
 * className, style and the passed-through attributes → the outermost element.</p>
 */
const MuiSkeletonAdapter: React.FC<SkeletonPartProps> = ({ shape, lines, className, style, ...attributes }) => {
  const reduced = useMediaQuery(REDUCED_MOTION_QUERY);
  const animation = reduced ? false : 'pulse';
  const variant = VARIANT[shape ?? 'rect'];
  const count = shape === 'text' ? Math.max(1, Math.floor(lines ?? 1)) : 1;
  if (count === 1) {
    return (
      <Skeleton variant={variant} animation={animation} className={className} style={style}
        {...attributes} aria-hidden="true" />
    );
  }
  return (
    <div className={className} style={style} {...attributes} aria-hidden="true">
      {Array.from({ length: count }, (_, index) => (
        <Skeleton key={index} variant="text" animation={animation}
          width={index === count - 1 ? LAST_LINE_WIDTH : undefined} />
      ))}
    </div>
  );
};

export default MuiSkeletonAdapter;
