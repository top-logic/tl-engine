import { useButtonDefaults } from 'tl-react-bridge';
import type { ButtonAppearance, ButtonStateJson } from 'tl-react-bridge';

/**
 * What a button control asks for, before its container has a say: its icon, its display mode and
 * its appearance. Absent values fall back to the defaults of the container.
 */
export interface ButtonRequest {
  /** The icon, a theme image in its encoded form; absent: none. */
  image?: string;
  /** The display mode; absent: `label-only`. */
  displayMode?: ButtonStateJson.DisplayMode;
  /** The appearance; absent or `default`: the one the container suggests, else `secondary`. */
  appearance?: ButtonAppearance | ButtonStateJson.Appearance;
}

/**
 * The appearance and display mode the part `Button` draws a button control with.
 */
export interface ButtonPresentation {
  appearance: ButtonAppearance;
  display: ButtonStateJson.DisplayMode;
}

/**
 * Resolves what a button control draws from what it asks for ({@link ButtonRequest}) and the
 * defaults of its container (`useButtonDefaults`).
 *
 * <ul>
 *   <li>Inside a menu the container wins: every button there is an entry (`menu-item`) and shows
 *       its label, with its icon if it has one.</li>
 *   <li>Otherwise the appearance asked for wins, then the one the container suggests, then
 *       `secondary`.</li>
 *   <li>A compact toolbar (`iconOnly`) makes a button that shows its icon a square icon button.</li>
 * </ul>
 */
export function useButtonPresentation({ image, displayMode, appearance }: ButtonRequest): ButtonPresentation {
  const defaults = useButtonDefaults();
  const requested = appearance === 'default' ? undefined : appearance as ButtonAppearance | undefined;
  const resolvedAppearance: ButtonAppearance = defaults.appearance === 'menu-item'
    ? 'menu-item'
    : (requested ?? defaults.appearance ?? 'secondary');
  const mode = displayMode ?? 'label-only';
  // Only a button that shows its icon goes compact; inside a menu every button shows its label.
  const shows = mode !== 'label-only' && !!image;
  const display: ButtonStateJson.DisplayMode = defaults.iconOnly && shows
    ? 'icon-only'
    : resolvedAppearance === 'menu-item' && image ? 'icon-label' : mode;
  return { appearance: resolvedAppearance, display };
}
