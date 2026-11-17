import { React } from 'tl-react-bridge';
import type { DividerPartProps } from 'tl-react-bridge';

/** The class of the divider of the design system. */
const DIVIDER_CLASS = 'tl-divider';

/** The class of a vertical divider of the design system, which stands in a row. */
const VERTICAL_CLASS = 'tl-divider--vertical';

/** The class of a divider with a text in its line. */
const LABELED_CLASS = 'tl-divider--labeled';

/** The class of a divider whose text stands at the start of the line. */
const LABEL_START_CLASS = 'tl-divider--label-start';

/** The class of a divider whose text stands at the end of the line. */
const LABEL_END_CLASS = 'tl-divider--label-end';

/** The class of a divider shortened at its start by the indent of an entry of a list. */
const INSET_CLASS = 'tl-divider--inset';

/** The class of the text in the line. */
const LABEL_CLASS = 'tl-divider__label';

/** The typographic class of the text in the line. */
const LABEL_TYPE_CLASS = 'tl-type-label';

/**
 * The TopLogic drawing of the part `Divider` ({@link DividerPartProps}): the divider of the design
 * system (tl-divider), a rule in the color of a separator. A vertical one (tl-divider--vertical)
 * stretches to the height of the row it stands in. A horizontal one with a text is an element of the
 * role `separator` holding the text between two lines (tl-divider--labeled), since an `hr` carries
 * no content; without a text it is an `hr`. An inset one (tl-divider--inset) begins at the indent of
 * an entry of a list.
 */
const DividerPart: React.FC<DividerPartProps> = ({
  orientation, label, labelPosition, inset, className, ...attributes
}) => {
  const vertical = orientation === 'vertical';
  const labeled = !vertical && !!label;
  const classes = [
    DIVIDER_CLASS,
    vertical ? VERTICAL_CLASS : '',
    labeled ? LABELED_CLASS : '',
    labeled && labelPosition === 'start' ? LABEL_START_CLASS : '',
    labeled && labelPosition === 'end' ? LABEL_END_CLASS : '',
    inset === 'start' ? INSET_CLASS : '',
    className ?? '',
  ].filter(Boolean).join(' ');

  if (labeled) {
    return (
      <div className={classes} role="separator" {...attributes}>
        <span className={`${LABEL_CLASS} ${LABEL_TYPE_CLASS}`}>{label}</span>
      </div>
    );
  }
  return <hr className={classes} aria-orientation={vertical ? 'vertical' : undefined} {...attributes} />;
};

export default DividerPart;
