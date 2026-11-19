import { React } from 'tl-react-bridge';
import type { BadgePartProps } from 'tl-react-bridge';
import Badge from '@mui/material/Badge';
import type { SxProps, Theme } from '@mui/material/styles';

/**
 * The badge standing on its own: MUI places the badge over the corner of the element it wraps; a
 * badge part wraps nothing and stands where the element around it puts it.
 */
const STANDALONE_SX: SxProps<Theme> = {
  '& .MuiBadge-badge': { position: 'static', transform: 'none' },
};

/**
 * The props of the marker of a badge without a label: MUI hides the content of its marker from
 * assistive technology, but a badge without a label is read as text of the element around it.
 */
const READ_CONTENT = { badge: { 'aria-hidden': undefined } };

/** The MUI color of each color role of a badge. */
const COLORS = {
  brand: 'primary',
  error: 'error',
  warning: 'warning',
  success: 'success',
  info: 'info',
} as const;

/**
 * Draws the part `Badge` ({@link BadgePartProps}) with the MUI `Badge`, standing on its own: the
 * round marker of MUI that counts the new entries of a navigation item, without an element it is
 * anchored to. Where it stands - beside a label or over the corner of an icon - comes from the
 * element around it.
 *
 * <p>Mapping from the props: content → `badgeContent`; dot → `variant="dot"`; colorRole → `color`
 * (`brand` → `primary`, the status roles → the MUI color of the same name); label → `role="img"`
 * and `aria-label` on the root, whose content MUI hides; without a label the content is text read
 * with the element around the badge; id, className and the passed-through attributes → the root of
 * the badge.</p>
 */
const MuiBadgeAdapter: React.FC<BadgePartProps> = ({ id, content, dot, colorRole, label, className, ...attributes }) => (
  <Badge
    id={id}
    badgeContent={dot ? undefined : content}
    variant={dot ? 'dot' : 'standard'}
    color={COLORS[colorRole ?? 'brand']}
    sx={STANDALONE_SX}
    className={className}
    role={label ? 'img' : undefined}
    aria-label={label}
    slotProps={label ? undefined : READ_CONTENT}
    {...attributes}
  />
);

export default MuiBadgeAdapter;
