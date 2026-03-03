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
 * - tooltip?: string    - plain text tooltip
 * - hasLink: boolean    - whether clicking navigates to the object
 */
const TLResourceCell: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const iconCss = state.iconCss as string | undefined;
  const iconSrc = state.iconSrc as string | undefined;
  const label = state.label as string | undefined;
  const cssClass = state.cssClass as string | undefined;
  const tooltip = state.tooltip as string | undefined;
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

  if (hasLink) {
    return (
      <a className={className} href="#" onClick={handleClick} title={tooltip}>
        {content}
      </a>
    );
  }

  return (
    <span className={className} title={tooltip}>
      {content}
    </span>
  );
};

export default TLResourceCell;
