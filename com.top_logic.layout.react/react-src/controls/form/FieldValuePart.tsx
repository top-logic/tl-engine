import { React } from 'tl-react-bridge';
import type { FieldValuePartProps } from 'tl-react-bridge';

/** What stands in an empty line, so that the line keeps its height. */
const EMPTY_LINE = ' ';

/** The classes of the value of the design system. */
const VALUE_CLASS = 'tl-field-value tl-type-body';

/** The class of a value shown with its line breaks. */
const MULTILINE_CLASS = 'tl-field-value--multiline';

/** The class of the text of the value, which ends with an ellipsis where it is cut off. */
const TEXT_CLASS = 'tl-field-value__text';

/** The browsing context a link opens its address in: a window of its own. */
const LINK_TARGET = '_blank';

/** The relation of the page opened by a link to the page it is opened from: none. */
const LINK_REL = 'noopener noreferrer';

/** The classes, without the empty ones, joined. */
function classes(...names: (string | false | undefined)[]): string {
  return names.filter(Boolean).join(' ');
}

/**
 * The TopLogic drawing of the part `FieldValue` ({@link FieldValuePartProps}): the read-only value
 * of the design system (tl-field-value).
 *
 * <p>The value is a `span`, or an `a` where it points somewhere, with the text in a
 * `tl-field-value__text` that ends with an ellipsis. A multi-line value is a `div` with the class
 * `tl-field-value--multiline` and one `tl-field-value__text` per line, so that its line breaks show
 * without a white-space rule.</p>
 */
const FieldValuePart: React.FC<FieldValuePartProps> = ({ id, text, href, multiline, className, ...attributes }) => {
  if (multiline) {
    return (
      <div id={id} className={classes(VALUE_CLASS, MULTILINE_CLASS, className)} {...attributes}>
        {text.split('\n').map((line, index) => (
          <span key={index} className={TEXT_CLASS}>{line === '' ? EMPTY_LINE : line}</span>
        ))}
      </div>
    );
  }
  const content = <span className={TEXT_CLASS}>{text}</span>;
  if (href !== undefined) {
    return (
      <a id={id} className={classes(VALUE_CLASS, className)} href={href} target={LINK_TARGET} rel={LINK_REL}
        {...attributes}>
        {content}
      </a>
    );
  }
  return (
    <span id={id} className={classes(VALUE_CLASS, className)} {...attributes}>
      {content}
    </span>
  );
};

export default FieldValuePart;
