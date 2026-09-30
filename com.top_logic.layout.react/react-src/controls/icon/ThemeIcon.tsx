import { React } from 'tl-react-bridge';

/**
 * Renders a ThemeImage from its encoded form as an {@code <i>} or {@code <img>} element.
 *
 * <p>The glyph is decorative and hidden from assistive technology; whoever needs a name puts it on
 * the element around the icon. The size is a class ({@code tl-icon-sm} etc., design system), never an
 * inline width - an {@code <img>} carrying a size class is a square of that size.</p>
 *
 * <p>Supported encoded formats:</p>
 * <ul>
 *   <li>{@code css:fa-solid fa-home} — icon font (monochrome)</li>
 *   <li>{@code colored:fa-solid fa-star} — icon font (colored variant)</li>
 *   <li>{@code /icons/edit.png} or {@code theme:Icons.EDIT} — image reference</li>
 *   <li>{@code none} — the invisible image, which renders nothing at all</li>
 * </ul>
 */
export function ThemeIcon({ encoded, className }: { encoded: string; className?: string }) {
  if (!encoded || encoded === 'none') {
    // The invisible image occupies no space: an element carrying an icon class that draws nothing
    // still takes the width of an icon, which indents whatever it sits beside.
    return null;
  }
  if (encoded.startsWith('css:')) {
    const cssClass = encoded.substring(4);
    return <i className={cssClass + (className ? ' ' + className : '')} aria-hidden="true" />;
  }
  if (encoded.startsWith('colored:')) {
    const cssClass = encoded.substring(8);
    return <i className={cssClass + (className ? ' ' + className : '')} aria-hidden="true" />;
  }
  if (encoded.startsWith('/') || encoded.startsWith('theme:')) {
    return <img src={encoded} alt="" className={className} />;
  }
  // Fallback: try as CSS class
  return <i className={encoded + (className ? ' ' + className : '')} aria-hidden="true" />;
}
