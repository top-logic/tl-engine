import { React, useTLState, rootClassName, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useImageSrc } from './imageSource';

/** Derives up to two initials from a display name. */
function initials(name: string): string {
  const words = name.trim().split(/\s+/).filter(Boolean);
  if (words.length === 0) {
    return '?';
  }
  if (words.length === 1) {
    return words[0].slice(0, 2).toUpperCase();
  }
  return (words[0][0] + words[words.length - 1][0]).toUpperCase();
}

/**
 * Maps a name to one of the eight category roles of the design system, 1 to 8. The same name gives
 * the same category everywhere; the category, not a color value, is what reaches the stylesheet.
 */
export function categoryOf(name: string): number {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) | 0;
  }
  return 1 + (Math.abs(hash) % 8);
}

/** The type class the initials of a size read in - set from outside, as the design system asks. */
const TYPE_CLASS: Record<string, string> = {
  sm: 'tl-type-label-strong',
  md: 'tl-type-body-strong',
  lg: 'tl-type-heading-md',
  xl: 'tl-type-heading-lg',
};

/**
 * Circular avatar for a person or object, `tl-avatar` of the design system. It shows the picture
 * of the one it represents; where there is none, initials on the category the display name maps
 * to; without a name, the neutral circle.
 *
 * State:
 * - name: string | null
 * - size: "sm" | "md" | "lg" | "xl"
 * - url / hasData / dataRevision: the picture, see useImageSrc
 */
const TLAvatar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const src = useImageSrc();

  const name = state.name as string | null;
  const size = (state.size as string) ?? 'md';
  const sizeClass = size === 'md' ? '' : `tl-avatar--${size}`;

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
        ['tl-avatar', sizeClass, `tl-avatar--category-${categoryOf(name)}`, TYPE_CLASS[size] ?? TYPE_CLASS.md]
          .filter(Boolean)
          .join(' ')
      )}
      role="img"
      aria-label={name}
      {...tooltipProps(name)}
    >
      {initials(name)}
    </span>
  );
};

export default TLAvatar;
