import { React, ThemeIcon, usePart } from 'tl-react-bridge';
import type { ColorRole, FieldValuePartProps } from 'tl-react-bridge';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';

/** What stands in an empty line of a multi-line value, so that the line keeps its height. */
const EMPTY_LINE = ' ';

/** The style of a line of a multi-line value: a block of its own. */
const LINE_STYLE: React.CSSProperties = { display: 'block' };

/**
 * The style of a multi-line value shown as it is written: the code font of the design system, lines
 * not wrapped, a value with a wide line scrolling horizontally.
 */
const PREFORMATTED_SX = { fontFamily: 'var(--tl-font-family-mono)', whiteSpace: 'pre', overflowX: 'auto' } as const;

/** The layout of a multi-line value with an action: the value, and the action at the top end beside it. */
const WITH_ACTION_SX = {
  display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) auto', alignItems: 'start', columnGap: 0.5,
} as const;

/** Size class of the design system for the icon ahead of the text. */
const ICON_CLASS = 'tl-icon-sm';

/** The style of a value with an icon: icon and text side by side. */
const WITH_ICON_SX = { display: 'inline-flex', alignItems: 'center', gap: 0.5 } as const;

/** The browsing context a link opens its address in: a window of its own. */
const LINK_TARGET = '_blank';

/** The relation of the page opened by a link to the page it is opened from: none. */
const LINK_REL = 'noopener noreferrer';

/**
 * The value of a color role: the part `Chip` of that role, holding icon and text. The chip is looked
 * up only for a value that has a role.
 */
function RolePill({ colorRole, icon, text }: { colorRole: ColorRole; icon?: string; text: string }) {
  const Chip = usePart('Chip');
  return <Chip colorRole={colorRole} icon={icon} label={text} />;
}

/**
 * Draws the part `FieldValue` ({@link FieldValuePartProps}) with Material UI: the text in a
 * `Typography` of the variant `body2`.
 *
 * <p>Mapping from the props: a single-line value → a `span` that does not wrap and ends with an
 * ellipsis (`noWrap`); href → a `Link` of the same variant opening the address in a window of its
 * own; onClick → a `Link` drawn as a button that runs it; icon → the icon ahead of the text; role
 * (with a text) → the part `Chip` of that role holding icon and text;
 * multiline → a `div` with one block per line; preformatted → that `div` in the code font of the
 * design system, its lines not wrapped; action (of a multi-line value) → beside the value at its top
 * end, in a grid that gives it a column of its own, className then on the grid. id, className and the passed-through attributes →
 * the drawn element.</p>
 */
const MuiFieldValueAdapter: React.FC<FieldValuePartProps> = ({
  id, text, href, multiline, preformatted, icon, onClick, colorRole, action, className, ...attributes
}) => {
  if (multiline) {
    const value = (
      <Typography id={id} component="div" variant="body2" sx={preformatted ? PREFORMATTED_SX : undefined}
        className={action ? undefined : className} {...attributes}>
        {text.split('\n').map((line, index) => (
          <span key={index} style={LINE_STYLE}>{line === '' ? EMPTY_LINE : line}</span>
        ))}
      </Typography>
    );
    if (!action) return value;
    return (
      <Box sx={WITH_ACTION_SX} className={className}>
        {value}
        {action}
      </Box>
    );
  }
  const inChip = colorRole !== undefined && colorRole !== 'neutral' && text !== '';
  const sx = icon && !inChip ? WITH_ICON_SX : undefined;
  const content = inChip
    ? <RolePill colorRole={colorRole} icon={icon} text={text} />
    : (
      <>
        {icon && <ThemeIcon encoded={icon} className={ICON_CLASS} />}
        {text}
      </>
    );
  if (href !== undefined) {
    return (
      <Link id={id} variant="body2" sx={sx} className={className} href={href} target={LINK_TARGET} rel={LINK_REL}
        {...attributes}>
        {content}
      </Link>
    );
  }
  if (onClick !== undefined) {
    return (
      <Link id={id} component="button" type="button" variant="body2" sx={sx} className={className} onClick={onClick}
        {...attributes}>
        {content}
      </Link>
    );
  }
  return (
    <Typography id={id} component="span" variant="body2" noWrap sx={sx} className={className} {...attributes}>
      {content}
    </Typography>
  );
};

export default MuiFieldValueAdapter;
