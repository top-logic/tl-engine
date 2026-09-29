import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

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
 * Circular initials avatar for a person or object. Initials and a stable background color are
 * derived from the display name.
 *
 * State:
 * - name: string | null
 */
const TLAvatar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const name = state.name as string | null;

  if (!name) {
    return <span id={controlId} className="tlAvatar tlAvatar--empty" />;
  }

  return (
    <span
      id={controlId}
      className="tlAvatar"
      style={{ backgroundColor: `hsl(${hue(name)}, 45%, 45%)` }}
      title={name}
      aria-label={name}
    >
      {initials(name)}
    </span>
  );
};

export default TLAvatar;
