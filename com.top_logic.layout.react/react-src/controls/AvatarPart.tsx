import { React, avatarInitials, categoryOf, tooltipProps } from 'tl-react-bridge';
import type { AvatarPartProps, AvatarSize } from 'tl-react-bridge';

/**
 * The scale abbreviation of a size name, which the classes of the design system carry. `medium` - and
 * a missing size - is the step without a modifier.
 */
const SIZE_CLASS: Partial<Record<AvatarSize, string>> = {
  small: 'sm',
  large: 'lg',
  'x-large': 'xl',
};

/** The type class the initials of a size read in - set from outside, as the design system asks. */
const TYPE_CLASS: Record<AvatarSize, string> = {
  small: 'tl-type-label-strong',
  medium: 'tl-type-body-strong',
  large: 'tl-type-heading-md',
  'x-large': 'tl-type-heading-lg',
};

/**
 * The TopLogic drawing of the part `Avatar` ({@link AvatarPartProps}): the avatar of the design
 * system (tl-avatar). The picture where there is one, else the initials of the name on its category,
 * else the neutral circle.
 */
const AvatarPart: React.FC<AvatarPartProps> = ({ id, name, src, size = 'medium', decorative, className, ...attributes }) => {
  const abbreviation = SIZE_CLASS[size];
  const classes = (...more: (string | undefined)[]) =>
    ['tl-avatar', abbreviation ? `tl-avatar--${abbreviation}` : '', ...more, className].filter(Boolean).join(' ');

  if (src) {
    return decorative ? (
      <span id={id} className={classes()} aria-hidden="true" {...attributes}>
        <img className="tl-avatar__image" src={src} alt="" />
      </span>
    ) : (
      <span id={id} className={classes()} {...tooltipProps(name ?? undefined)} {...attributes}>
        <img className="tl-avatar__image" src={src} alt={name ?? ''} />
      </span>
    );
  }

  if (!name) {
    return <span id={id} className={classes()} aria-hidden="true" {...attributes} />;
  }

  const named = decorative
    ? { 'aria-hidden': true as const }
    : { role: 'img', 'aria-label': name, ...tooltipProps(name) };
  return (
    <span
      id={id}
      className={classes(TYPE_CLASS[size] ?? TYPE_CLASS.medium)}
      data-tl-role={`category-${categoryOf(name)}`}
      {...named}
      {...attributes}
    >
      {avatarInitials(name)}
    </span>
  );
};

export default AvatarPart;
