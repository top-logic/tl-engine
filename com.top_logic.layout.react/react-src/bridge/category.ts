/**
 * Maps a name to one of the eight category roles of the design system, 1 to 8: the display name
 * of an avatar, the category of a calendar event. The same name gives the same category
 * everywhere; the category, not a color value, is what reaches the stylesheet.
 *
 * An empty name maps to category 1. A caller for which "no name" means "no category" (an event
 * without a category) checks that itself and does not ask.
 */
export function categoryOf(name: string): number {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) | 0;
  }
  return 1 + (Math.abs(hash) % 8);
}
