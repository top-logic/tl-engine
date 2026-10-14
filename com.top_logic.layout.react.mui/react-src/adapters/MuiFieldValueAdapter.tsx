import { React } from 'tl-react-bridge';
import type { FieldValuePartProps } from 'tl-react-bridge';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';

/** What stands in an empty line of a multi-line value, so that the line keeps its height. */
const EMPTY_LINE = ' ';

/** The style of a line of a multi-line value: a block of its own. */
const LINE_STYLE: React.CSSProperties = { display: 'block' };

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
 * own; multiline → a `div` with one block per line. id, className and the passed-through attributes
 * → the drawn element.</p>
 */
const MuiFieldValueAdapter: React.FC<FieldValuePartProps> = ({ id, text, href, multiline, className, ...attributes }) => {
  if (multiline) {
    return (
      <Typography id={id} component="div" variant="body2" className={className} {...attributes}>
        {text.split('\n').map((line, index) => (
          <span key={index} style={LINE_STYLE}>{line === '' ? EMPTY_LINE : line}</span>
        ))}
      </Typography>
    );
  }
  if (href !== undefined) {
    return (
      <Link id={id} variant="body2" className={className} href={href} target={LINK_TARGET} rel={LINK_REL}
        {...attributes}>
        {text}
      </Link>
    );
  }
  return (
    <Typography id={id} component="span" variant="body2" noWrap className={className} {...attributes}>
      {text}
    </Typography>
  );
};

export default MuiFieldValueAdapter;
