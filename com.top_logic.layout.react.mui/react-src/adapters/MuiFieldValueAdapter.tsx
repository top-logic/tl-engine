import { React, ThemeIcon } from 'tl-react-bridge';
import type { FieldValuePartProps } from 'tl-react-bridge';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';

/** What stands in an empty line of a multi-line value, so that the line keeps its height. */
const EMPTY_LINE = ' ';

/** The style of a line of a multi-line value: a block of its own. */
const LINE_STYLE: React.CSSProperties = { display: 'block' };

/** Size class of the design system for the icon ahead of the text. */
const ICON_CLASS = 'tl-icon-sm';

/** The style of a value with an icon: icon and text side by side. */
const WITH_ICON_SX = { display: 'inline-flex', alignItems: 'center', gap: 0.5 } as const;

/** The browsing context a link opens its address in: a window of its own. */
const LINK_TARGET = '_blank';

/** The relation of the page opened by a link to the page it is opened from: none. */
const LINK_REL = 'noopener noreferrer';

/**
 * Draws the part `FieldValue` ({@link FieldValuePartProps}) with Material UI: the text in a
 * `Typography` of the variant `body2`.
 *
 * <p>Mapping from the props: a single-line value → a `span` that does not wrap and ends with an
 * ellipsis (`noWrap`); href → a `Link` of the same variant opening the address in a window of its
 * own; onClick → a `Link` drawn as a button that runs it; icon → the icon ahead of the text;
 * multiline → a `div` with one block per line. id, className and the passed-through attributes →
 * the drawn element.</p>
 */
const MuiFieldValueAdapter: React.FC<FieldValuePartProps> = ({
  id, text, href, multiline, icon, onClick, className, ...attributes
}) => {
  if (multiline) {
    return (
      <Typography id={id} component="div" variant="body2" className={className} {...attributes}>
        {text.split('\n').map((line, index) => (
          <span key={index} style={LINE_STYLE}>{line === '' ? EMPTY_LINE : line}</span>
        ))}
      </Typography>
    );
  }
  const sx = icon ? WITH_ICON_SX : undefined;
  const content = (
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
