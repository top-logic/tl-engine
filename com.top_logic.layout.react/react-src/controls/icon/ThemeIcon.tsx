import { React } from 'tl-react-bridge';

/**
 * Renders a ThemeImage from its encoded form as an {@code <i>} or {@code <img>} element.
 *
 * <p>Supported encoded formats:</p>
 * <ul>
 *   <li>{@code css:fa-solid fa-home} — icon font (monochrome)</li>
 *   <li>{@code colored:fa-solid fa-star} — icon font (colored variant)</li>
 *   <li>{@code /icons/edit.png} or {@code theme:Icons.EDIT} — image reference</li>
 * </ul>
 */
export function ThemeIcon({ encoded, className }: { encoded: string; className?: string }) {
  if (encoded.startsWith('css:')) {
    const cssClass = encoded.substring(4);
    return <i className={cssClass + (className ? ' ' + className : '')} />;
  }
  if (encoded.startsWith('colored:')) {
    const cssClass = encoded.substring(8);
    return <i className={cssClass + (className ? ' ' + className : '')} />;
  }
  if (encoded.startsWith('/') || encoded.startsWith('theme:')) {
    return <img src={encoded} alt="" className={className} style={{ width: '1em', height: '1em' }} />;
  }
  // Fallback: try as CSS class
  return <i className={encoded + (className ? ' ' + className : '')} />;
}
