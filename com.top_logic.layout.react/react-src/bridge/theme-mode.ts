// The appearance mode of the page: holding the page in one mode, light or dark, whatever UI theme
// the user has selected. The page's theme script (UIThemeService.writeThemeScript) defines the
// client API this module calls.

/** An appearance mode of the design system, as `data-tl-mode` of `<html>` names it. */
export type ThemeMode = 'light' | 'dark';

/** The global object of the theme script (UIThemeService.CLIENT_API). */
export const THEME_CLIENT_API = 'tlTheme';

/** The function of {@link THEME_CLIENT_API} holding the page in a mode (UIThemeService.LOCK_MODE_FUNCTION). */
export const LOCK_MODE_FUNCTION = 'lockMode';

/** The part of the theme script's client API this module uses. */
type ThemeClientApi = { [LOCK_MODE_FUNCTION]?: (mode: ThemeMode | null) => void };

/**
 * Holds the page in the given appearance mode, or releases it again with `null`.
 *
 * <p>While the page is held in a mode, `data-tl-mode` of `<html>` names that mode, and a selected
 * UI theme of the other mode is represented by the theme the operating system's preference for the
 * held mode selects, so that the styling properties of the design system and of the UI themes are
 * all in the held mode. The user's selection is kept: selecting another theme or following the
 * operating system works as before, within the held mode, and releasing the page puts the
 * selection back into effect.</p>
 *
 * <p>A page whose components render in a single color scheme of their own calls it once, e.g. a
 * component library styled with a light theme only, so that no part of the page switches to dark.
 * On a page without the theme script the call has no effect.</p>
 */
export function lockMode(mode: ThemeMode | null): void {
  const api = (window as unknown as Record<string, ThemeClientApi | undefined>)[THEME_CLIENT_API];
  api?.[LOCK_MODE_FUNCTION]?.(mode);
}
