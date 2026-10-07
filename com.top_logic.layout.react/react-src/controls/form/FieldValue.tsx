import { React, ThemeIcon } from 'tl-react-bridge';
import { TLPill } from '../pill/TLPill';

/** What stands in an empty line, so that the line keeps its height. */
const EMPTY_LINE = ' ';

/**
 * The one renderer of a read-only value (design system: tl-field-value): the form field that is not
 * editable, the chosen values of a select, the business object in a table cell.
 *
 * The value is a `span`, or an `a` where it leads somewhere - either out of the application (`href`,
 * opened in a new tab, as a web address in a form) or to the place the application displays the value
 * at (`onGoto`, the command `goto`, as a select value or a table cell). Its text stands in a
 * `tl-field-value__text` that ends with an ellipsis; a multi-line value is a `div` with
 * `tl-field-value--multiline` and one text per line. An icon comes through ThemeIcon. A value the
 * model gives a color role is a `tl-pill` carrying the role as `data-tl-role`, icon and label side by
 * side within it.
 *
 * @param id the id of the element, the control id of the field
 * @param className further classes after the design system's, e.g. the result of rootClassName
 * @param text the value as text
 * @param href the address the value points at outside the application; no link where absent
 * @param onGoto leads to the place the value is displayed at; no link where absent
 * @param multiline whether the text is shown with its line breaks
 * @param icon the encoded theme image of the value (ThemeIcon), none where absent
 * @param role the color role of the value; plain where absent
 * @param tooltip the data-tooltip of the value (`key:tooltip` for a tooltip the server provides)
 */
export function FieldValue({
  id,
  className,
  text,
  href,
  onGoto,
  multiline = false,
  icon,
  role,
  tooltip,
}: {
  id?: string;
  className?: string;
  text: string;
  href?: string | null;
  onGoto?: () => void;
  multiline?: boolean;
  icon?: string;
  role?: string;
  tooltip?: string;
}) {
  const cls = (base: string) => (className ? base + ' ' + className : base);
  if (multiline) {
    return (
      <div id={id} className={cls('tl-field-value tl-field-value--multiline tl-type-body')} data-tooltip={tooltip}>
        {text.split('\n').map((line, index) => (
          <span key={index} className="tl-field-value__text">{line === '' ? EMPTY_LINE : line}</span>
        ))}
      </div>
    );
  }
  // A pill is drawn around content only: a value without a label shows no empty tinted box.
  const inPill = !!role && text !== '';
  const image = icon
    ? <ThemeIcon encoded={icon} className={inPill ? 'tl-icon-sm tl-pill__icon' : 'tl-icon-sm'} />
    : null;
  const label = text !== '' ? <span className="tl-field-value__text">{text}</span> : null;
  const content = inPill
    ? <TLPill role={role} className="tl-type-label" icon={image}>{text}</TLPill>
    : <>{image}{label}</>;
  if (href != null) {
    return (
      <a id={id} className={cls('tl-field-value tl-type-body')} href={href} target="_blank" rel="noopener noreferrer"
        data-tooltip={tooltip}>
        {content}
      </a>
    );
  }
  if (onGoto) {
    const follow = (e: React.MouseEvent) => {
      e.preventDefault();
      onGoto();
    };
    return (
      <a id={id} className={cls('tl-field-value tl-type-body')} href="#" onClick={follow} data-tooltip={tooltip}>
        {content}
      </a>
    );
  }
  return (
    <span id={id} className={cls('tl-field-value tl-type-body')} data-tooltip={tooltip}>
      {content}
    </span>
  );
}
