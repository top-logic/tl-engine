// What an avatar shows of a display name where there is no picture: the initials, on the category
// the name maps to (categoryOf, category.ts). Every component rendering the avatar state derives both
// the same way.

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
