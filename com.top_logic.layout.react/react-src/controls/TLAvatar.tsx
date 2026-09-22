import { React, useTLState, tooltipProps } from 'tl-react-bridge';
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

/** Maps a name to a stable background hue. */
function hue(name: string): number {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) | 0;
  }
  return Math.abs(hash) % 360;
}

/**
 * Circular avatar for a person or object. It shows the picture of the one it represents; where
 * there is none, initials and a stable background color derived from the display name.
 *
 * State:
 * - name: string | null
 * - size: "small" | "default" | "large" | "x-large"
 * - url / hasData / dataRevision: the picture, see useImageSrc
 */
const TLAvatar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const src = useImageSrc();

  const name = state.name as string | null;
  const size = (state.size as string) ?? 'default';
  const sizeClass = size === 'default' ? '' : `tlAvatar--${size}`;

  if (src) {
    return (
      <span
        id={controlId}
        className={['tlAvatar', sizeClass].filter(Boolean).join(' ')}
        {...tooltipProps(name)}
      >
        <img className="tlAvatar__image" src={src} alt={name ?? ''} />
      </span>
    );
  }

  if (!name) {
    return <span id={controlId} className={['tlAvatar', 'tlAvatar--empty', sizeClass].filter(Boolean).join(' ')} />;
  }

  return (
    <span
      id={controlId}
      className={['tlAvatar', sizeClass].filter(Boolean).join(' ')}
      style={{ backgroundColor: `hsl(${hue(name)}, 45%, 45%)` }}
      aria-label={name}
      {...tooltipProps(name)}
    >
      {initials(name)}
    </span>
  );
};

export default TLAvatar;
