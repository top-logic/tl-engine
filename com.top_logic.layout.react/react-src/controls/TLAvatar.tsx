import {
  React,
  useTLState,
  useImageSrc,
  avatarInitials,
  categoryOf,
  rootClassName,
  tooltipProps,
} from 'tl-react-bridge';
import type { TLCellProps, AvatarStateJson } from 'tl-react-bridge';

/**
 * The scale abbreviation of an external size name, which the CSS classes of the design system carry.
 * `medium` - and a missing size - is the step without a modifier.
 */
const SIZE_CLASS: Partial<Record<AvatarStateJson.Size, string>> = {
  small: 'sm',
  large: 'lg',
  'x-large': 'xl',
};

/** The type class the initials of a size read in - set from outside, as the design system asks. */
const TYPE_CLASS: Record<AvatarStateJson.Size, string> = {
  small: 'tl-type-label-strong',
  medium: 'tl-type-body-strong',
  large: 'tl-type-heading-md',
  'x-large': 'tl-type-heading-lg',
};

/**
 * Circular avatar for a person or object, `tl-avatar` of the design system. It shows the picture
 * of the one it represents; where there is none, initials on the category the display name maps
 * to; without a name, the neutral circle.
 *
 * The state is described by AvatarStateJson.
 */
const TLAvatar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AvatarStateJson>>();
  const src = useImageSrc();

  const name = state.name;
  const size = state.size ?? 'medium';
  const abbreviation = SIZE_CLASS[size];
  const sizeClass = abbreviation ? `tl-avatar--${abbreviation}` : '';

  if (src) {
    return (
      <span
        id={controlId}
        className={rootClassName(state, ['tl-avatar', sizeClass].filter(Boolean).join(' '))}
        {...tooltipProps(name)}
      >
        <img className="tl-avatar__image" src={src} alt={name ?? ''} />
      </span>
    );
  }

  if (!name) {
    return (
      <span
        id={controlId}
        className={rootClassName(state, ['tl-avatar', sizeClass].filter(Boolean).join(' '))}
        aria-hidden="true"
      />
    );
  }

  return (
    <span
      id={controlId}
      className={rootClassName(
        state,
        ['tl-avatar', sizeClass, TYPE_CLASS[size] ?? TYPE_CLASS.medium]
          .filter(Boolean)
          .join(' ')
      )}
      data-tl-role={`category-${categoryOf(name)}`}
      role="img"
      aria-label={name}
      {...tooltipProps(name)}
    >
      {avatarInitials(name)}
    </span>
  );
};

export default TLAvatar;
