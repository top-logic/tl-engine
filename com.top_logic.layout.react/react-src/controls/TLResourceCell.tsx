import { React, useTLState, useTLCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { withPill } from './selectOptions';

/** Command sent when the user follows the link of the displayed value. */
const CMD_GOTO = 'goto';

/**
 * Displays a business object with optional icon, label, and goto link.
 *
 * State:
 * - label?: string      - display text
 * - iconCss?: string    - CSS icon class (e.g. "bi bi-person-fill")
 * - iconSrc?: string    - image URL for non-CSS icons
 * - hasTooltip: boolean - whether the server provides a rich tooltip (fetched lazily)
 * - hasLink: boolean    - whether clicking navigates to the object
 * - colorRole?: string  - color role the object carries in the model; shown as a pill of that role
 *
 * Design system: a read-only value, `tl-field-value` (an `a` where it is a link), with the icon
 * in `tl-icon-sm` and the label in `tl-field-value__text`.
 */
const TLResourceCell: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const iconCss = state.iconCss as string | undefined;
  const iconSrc = state.iconSrc as string | undefined;
  const label = state.label as string | undefined;
  const hasTooltip = state.hasTooltip === true;
  const hasLink = state.hasLink as boolean;
  const colorRole = (state.colorRole as string) || undefined;

  const icon = iconCss
    ? <i className={iconCss + ' tl-icon-sm'} aria-hidden="true" />
    : iconSrc
    ? <img src={iconSrc} className="tl-icon-sm" alt="" />
    : null;

  // A colored object is drawn as a pill; one the model gives no role stays plain.
  const content = withPill(colorRole, (
    <>
      {icon}
      {label && <span className="tl-field-value__text">{label}</span>}
    </>
  ));

  const handleClick = React.useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    sendCommand(CMD_GOTO, {});
  }, [sendCommand]);

  const className = rootClassName(state, 'tl-field-value tl-type-body');
  const tooltipAttr = hasTooltip ? 'key:tooltip' : undefined;

  if (hasLink) {
    return (
      <a
        id={controlId}
        className={className}
        href="#"
        onClick={handleClick}
        data-tooltip={tooltipAttr}
      >
        {content}
      </a>
    );
  }

  return (
    <span id={controlId} className={className} data-tooltip={tooltipAttr}>
      {content}
    </span>
  );
};

export default TLResourceCell;
