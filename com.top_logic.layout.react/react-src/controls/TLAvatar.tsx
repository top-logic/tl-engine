import { React, useTLState, useImageSrc, usePart } from 'tl-react-bridge';
import type { TLCellProps, AvatarStateJson } from 'tl-react-bridge';

/**
 * Circular avatar for a person or object, `tl-avatar` of the design system, drawn through the part
 * `Avatar`. It shows the picture of the one it represents; where there is none, initials on the
 * category the display name maps to; without a name, the neutral circle.
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

  return (
    <Avatar
      id={controlId}
      name={state.name}
      src={src}
      size={state.size ?? 'medium'}
      className={typeof state.cssClass === 'string' ? state.cssClass : undefined}
    />
  );
};

export default TLAvatar;
