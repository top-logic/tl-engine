import { React } from 'tl-react-bridge';

/**
 * Renders an encoded ThemeImage (e.g. "css:fas fa-home") as a font icon.
 *
 * The icon is decorative: it is hidden from assistive technology.
 */
const FontIcon: React.FC<{ image?: string; className?: string }> = ({ image, className }) => {
  if (!image) return null;
  // Strip "css:" or "colored:" prefix from ThemeImage.toEncodedForm() output.
  const cssClass = image.startsWith('css:') ? image.substring(4)
    : image.startsWith('colored:') ? image.substring(8)
    : image;
  return <span className={`${className ? className + ' ' : ''}${cssClass}`} aria-hidden="true" />;
};

export default FontIcon;
