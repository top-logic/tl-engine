import { React } from 'tl-react-bridge';

/**
 * The classes of a pill of the given role: `tl-pill`, its role modifier (none for neutral or no
 * role), then the given further classes.
 *
 * <p>
 * For an element that is a pill but holds more than the label - the chip of a multi-valued
 * selection carries its drag handle and remove button beside the label, within the pill.
 * </p>
 */
export function pillClassName(role?: string, className?: string): string {
  return ['tl-pill', role && role !== 'neutral' ? 'tl-pill--' + role : '', className ?? '']
    .filter(Boolean).join(' ');
}

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
  return (
    <span className={pillClassName(role, className)}>
      <span className="tl-pill__label">{children}</span>
    </span>
  );
}

export default TLPill;
