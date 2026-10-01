import { React } from 'tl-react-bridge';

/** What stands in an empty line, so that the line keeps its height. */
const EMPTY_LINE = ' ';

/**
 * The read-only value of a form field (design system: tl-field-value), rendered instead of an input
 * while the field is not editable.
 *
 * The value is a `span`, or an `a` opening `href` in a new tab where the value points somewhere,
 * with the text in a `tl-field-value__text` that ends with an ellipsis. A multi-line value is a
 * `div` with the class `tl-field-value--multiline` and one `tl-field-value__text` per line, so that
 * its line breaks show without a white-space rule; an empty line keeps its height.
 *
 * @param id the id of the element, the control id of the field
 * @param className further classes after the design system's, e.g. the result of rootClassName
 * @param text the value as text
 * @param href the address the value points at, or null/undefined where it is no link
 * @param multiline whether the text is shown with its line breaks
 */
export function FieldValue({
  id,
  className,
  text,
  href,
  multiline = false,
}: {
  id?: string;
  className?: string;
  text: string;
  href?: string | null;
  multiline?: boolean;
}) {
  const cls = (base: string) => (className ? base + ' ' + className : base);
  if (multiline) {
    return (
      <div id={id} className={cls('tl-field-value tl-field-value--multiline tl-type-body')}>
        {text.split('\n').map((line, index) => (
          <span key={index} className="tl-field-value__text">{line === '' ? EMPTY_LINE : line}</span>
        ))}
      </div>
    );
  }
  const content = <span className="tl-field-value__text">{text}</span>;
  if (href != null) {
    return (
      <a id={id} className={cls('tl-field-value tl-type-body')} href={href} target="_blank" rel="noopener noreferrer">
        {content}
      </a>
    );
  }
  return (
    <span id={id} className={cls('tl-field-value tl-type-body')}>
      {content}
    </span>
  );
}
