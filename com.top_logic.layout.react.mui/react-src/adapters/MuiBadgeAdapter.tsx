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
 * Draws the part `Badge` ({@link BadgePartProps}) with the MUI `Badge` in the color `primary`,
 * standing on its own: the round marker of MUI that counts the new entries of a navigation item,
 * without an element it is anchored to. Where it stands - beside a label or over the corner of an
 * icon - comes from the className, as for the TopLogic badge.
 *
 * <p>Mapping from the props: content → `badgeContent`; id, className and the passed-through
 * attributes → the root of the badge.</p>
 */
const MuiBadgeAdapter: React.FC<BadgePartProps> = ({ id, content, className, ...attributes }) => (
  <Badge id={id} badgeContent={content} color="primary" sx={STANDALONE_SX} className={className} {...attributes} />
);

export default MuiBadgeAdapter;
