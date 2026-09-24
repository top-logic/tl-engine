import { React } from 'tl-react-bridge';

/**
 * Displays a value the model gives a color role as a pill of that role.
 *
 * <p>
 * The role arrives from the server by its external name - neutral, brand, error, warning, success,
 * info, category-1 to category-8 - and becomes the modifier class {@code tl-pill--<role>}; how a
 * role looks is the design system's (pill.css). Nothing but a class is set: no color value, no
 * custom property. A pill without a role is neutral.
 * </p>
 */
export function TLPill({
  role,
  className,
  children,
}: {
  role?: string;
  className?: string;
  children?: React.ReactNode;
}) {
  const classes = ['tl-pill', role && role !== 'neutral' ? 'tl-pill--' + role : '', className ?? '']
    .filter(Boolean).join(' ');
  return (
    <span className={classes}>
      <span className="tl-pill__label">{children}</span>
    </span>
  );
}

export default TLPill;
