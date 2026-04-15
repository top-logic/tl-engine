import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Displays a business object with optional icon, label, and goto link.
 *
 * State:
 * - label?: string      - display text
 * - iconCss?: string    - CSS icon class (e.g. "bi bi-person-fill")
 * - iconSrc?: string    - image URL for non-CSS icons
 * - cssClass?: string   - type-specific CSS class
 * - hasTooltip: boolean - whether the server provides a rich tooltip (fetched lazily)
 * - hasLink: boolean    - whether clicking navigates to the object
 */
const TLResourceCell: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const iconCss = state.iconCss as string | undefined;
  const iconSrc = state.iconSrc as string | undefined;
  const label = state.label as string | undefined;
  const cssClass = state.cssClass as string | undefined;
  const hasTooltip = state.hasTooltip === true;
  const hasLink = state.hasLink as boolean;

  const icon = iconCss
    ? <i className={iconCss} />
    : iconSrc
    ? <img src={iconSrc} className="tlTypeIcon" alt="" />
    : null;

  const content = (
    <>
      {icon}
      {label && <span className="tlResourceLabel">{label}</span>}
    </>
  );

  const handleClick = React.useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    sendCommand('goto', {});
  }, [sendCommand]);

  const className = ['tlResourceCell', cssClass].filter(Boolean).join(' ');
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
