import { React, useTLState, useImageSrc, usePart, StateBadge } from 'tl-react-bridge';
import type { TLCellProps, AvatarStateJson } from 'tl-react-bridge';

/**
 * Circular avatar for a person or object, `tl-avatar` of the design system, drawn through the part
 * `Avatar`. It shows the picture of the one it represents; where there is none, initials on the
 * category the display name maps to; without a name, the neutral circle.
 *
 * A badge of the avatar stands over its corner (tl-icon-badge), drawn through the part `Badge` as an
 * image named by the label of the badge, or by its content.
 *
 * The state is described by AvatarStateJson.
 */
const TLAvatar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AvatarStateJson>>();
  const src = useImageSrc();
  const Avatar = usePart('Avatar');

  if (state.hidden === true) {
    return null;
  }

  const cssClass = typeof state.cssClass === 'string' ? state.cssClass : undefined;
  const badge = state.badge;
  if (!badge) {
    return <Avatar id={controlId} name={state.name} src={src} size={state.size ?? 'medium'} className={cssClass} />;
  }

  // The badge over the corner of the avatar: the element around the part places it (view-layer/parts.md).
  return (
    <span id={controlId} className={cssClass ? 'tl-icon-badge ' + cssClass : 'tl-icon-badge'}>
      <Avatar name={state.name} src={src} size={state.size ?? 'medium'} />
      <span className="tl-icon-badge__count"><StateBadge badge={badge} alone={true} /></span>
    </span>
  );
};

export default TLAvatar;
