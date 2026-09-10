import { React } from 'tl-react-bridge';

/**
 * Displays a value that the model gives a color as a pill in that color.
 *
 * <p>
 * The color arrives from the server as the CSS it is applied with - a color value, or a
 * {@code var(--token)} reference to a design token of the active theme - and is handed to the
 * stylesheet as the custom property {@code --tlPill-color}, so the tinted background, the border
 * and the text are all composed from the one color and follow a theme switch. There is no class
 * per color.
 * </p>
 */
export function TLPill({
  color,
  className,
  children,
}: {
  color: string;
  className?: string;
  children?: React.ReactNode;
}) {
  return (
    <span
      className={className ? 'tlPill ' + className : 'tlPill'}
      style={{ ['--tlPill-color' as string]: color } as React.CSSProperties}
    >
      {children}
    </span>
  );
}

export default TLPill;
