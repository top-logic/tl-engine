// What an avatar shows of a display name where there is no picture: the initials, on the category
// the name maps to. Every component rendering the avatar state derives both the same way.

/** Up to two initials of a display name, upper case; `?` for a name without any word. */
export function avatarInitials(name: string): string {
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
 * Maps a display name to one of the eight category roles of the design system, 1 to 8. The same
 * name gives the same category everywhere; the category, not a color value, is what reaches the
 * stylesheet.
 */
export function avatarCategory(name: string): number {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) | 0;
  }
  return 1 + (Math.abs(hash) % 8);
}
