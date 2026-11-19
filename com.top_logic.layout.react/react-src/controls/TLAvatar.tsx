import { React, useTLState, useImageSrc, usePart, StateBadge, labelWithBadge } from 'tl-react-bridge';
import type { TLCellProps, AvatarStateJson } from 'tl-react-bridge';

/**
 * Circular avatar for a person or object, `tl-avatar` of the design system, drawn through the part
 * `Avatar`. It shows the picture of the one it represents; where there is none, initials on the
 * category the display name maps to; without a name, the neutral circle.
 *
 * A badge of the avatar stands over its corner (tl-icon-badge), drawn through the part `Badge`. The
 * avatar with its badge is then one image named by the name and the badge ({@link labelWithBadge},
 * e.g. "Anna Meier, Open tickets: 3"); the circle and the badge inside are hidden from assistive
 * technology.
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
    <span
      id={controlId}
      className={cssClass ? 'tl-icon-badge ' + cssClass : 'tl-icon-badge'}
      role="img"
      aria-label={labelWithBadge(state.name ?? undefined, badge)}
    >
      <Avatar name={state.name} src={src} size={state.size ?? 'medium'} aria-hidden={true} />
      <span className="tl-icon-badge__count"><StateBadge badge={badge} alone={true} /></span>
    </span>
  );
};

export default TLAvatar;
