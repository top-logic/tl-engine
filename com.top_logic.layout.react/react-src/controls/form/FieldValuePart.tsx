import { React, ThemeIcon, usePart } from 'tl-react-bridge';
import type { ColorRole, FieldValuePartProps } from 'tl-react-bridge';

/** What stands in an empty line, so that the line keeps its height. */
const EMPTY_LINE = ' ';

/**
 * The class of the value of the design system. It sets no type: the value takes the type of the
 * place it stands in.
 */
const VALUE_CLASS = 'tl-field-value';

/** The class of a value shown with its line breaks. */
const MULTILINE_CLASS = 'tl-field-value--multiline';

/**
 * The classes of a multi-line value shown as it is written: in the code type, lines not wrapped, a
 * line wider than the value scrolling horizontally.
 */
const PREFORMATTED_CLASS = 'tl-field-value--preformatted tl-type-code';

/** The class of the element holding a multi-line value and its action side by side. */
const GROUP_CLASS = 'tl-field-value-group';

/** The class of the action of a multi-line value. */
const ACTION_CLASS = 'tl-field-value-group__action';

/** The class of the text of the value, which ends with an ellipsis where it is cut off. */
const TEXT_CLASS = 'tl-field-value__text';

/** The browsing context a link opens its address in: a window of its own. */
const LINK_TARGET = '_blank';

/** The relation of the page opened by a link to the page it is opened from: none. */
const LINK_REL = 'noopener noreferrer';

/** The address of a link that runs an action instead of leading to an address. */
const ACTION_HREF = '#';

/** The class of the icon ahead of the text. */
const ICON_CLASS = 'tl-icon-sm';

/** The classes, without the empty ones, joined. */
function classes(...names: (string | false | undefined)[]): string {
  return names.filter(Boolean).join(' ');
}

/**
 * The value of a color role: the part `Chip` of that role, holding icon and text. The chip is looked
 * up only for a value that has a role.
 */
function RolePill({ colorRole, icon, text }: { colorRole: ColorRole; icon?: string; text: string }) {
  const Chip = usePart('Chip');
  return <Chip colorRole={colorRole} icon={icon} label={text} />;
}

/**
 * The TopLogic drawing of the part `FieldValue` ({@link FieldValuePartProps}): the read-only value
 * of the design system (tl-field-value).
 *
 * <p>The value is a `span`, or an `a` where it points somewhere or runs an action, with the icon
 * ahead of the text in a `tl-field-value__text` that ends with an ellipsis. A link running an action
 * has the address `#`, which it does not follow. A multi-line value is a `div` with the class
 * `tl-field-value--multiline` and one `tl-field-value__text` per line, so that its line breaks show
 * without a white-space rule. A preformatted multi-line value also has the classes
 * `tl-field-value--preformatted` and `tl-type-code`. A multi-line value with an action stands in a
 * `tl-field-value-group` (which carries the className) beside a `tl-field-value-group__action`
 * holding the action, so that the action takes room of its own and covers no line.</p>
 *
 * <p>A value of a color role is the part `Chip` of that role inside the value, icon and label side by
 * side within it; a value without a text shows no empty tinted box.</p>
 */
const FieldValuePart: React.FC<FieldValuePartProps> = ({
  id, text, href, multiline, preformatted, icon, onClick, colorRole, action, className, ...attributes
}) => {
  if (multiline) {
    const value = (
      <div id={id}
        className={classes(VALUE_CLASS, MULTILINE_CLASS, preformatted && PREFORMATTED_CLASS, !action && className)}
        {...attributes}>
        {text.split('\n').map((line, index) => (
          <span key={index} className={TEXT_CLASS}>{line === '' ? EMPTY_LINE : line}</span>
        ))}
      </div>
    );
    if (!action) return value;
    return (
      <div className={classes(GROUP_CLASS, className)}>
        {value}
        <span className={ACTION_CLASS}>{action}</span>
      </div>
    );
  }
  // A pill is drawn around content only: a value without a text shows no empty tinted box.
  const content = colorRole !== undefined && colorRole !== 'neutral' && text !== ''
    ? <RolePill colorRole={colorRole} icon={icon} text={text} />
    : (
      <>
        {icon && <ThemeIcon encoded={icon} className={ICON_CLASS} />}
        {text !== '' && <span className={TEXT_CLASS}>{text}</span>}
      </>
    );
  if (href !== undefined) {
    return (
      <a id={id} className={classes(VALUE_CLASS, className)} href={href} target={LINK_TARGET} rel={LINK_REL}
        {...attributes}>
        {content}
      </a>
    );
  }
  if (onClick !== undefined) {
    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
      event.preventDefault();
      onClick(event);
    };
    return (
      <a id={id} className={classes(VALUE_CLASS, className)} href={ACTION_HREF} onClick={handleClick} {...attributes}>
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
