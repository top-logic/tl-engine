import { React, useTLState, useTLCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FieldValue } from './form/FieldValue';

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
 * Design system: a read-only value, rendered by FieldValue - `tl-field-value` (an `a` sending
 * `goto` where it is a link), the icon through ThemeIcon, a colored object as a `tl-pill` carrying
 * its role as `data-tl-role`.
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

  const handleGoto = React.useCallback(() => {
    sendCommand(CMD_GOTO, {});
  }, [sendCommand]);

  // One renderer of a read-only value (N1): the icon through ThemeIcon, a colored object as a pill
  // carrying data-tl-role, a link as the inner kind (goto).
  return (
    <FieldValue
      id={controlId}
      className={rootClassName(state)}
      text={label ?? ''}
      icon={iconCss ? 'css:' + iconCss : iconSrc}
      role={colorRole}
      onGoto={hasLink ? handleGoto : undefined}
      tooltip={hasTooltip ? 'key:tooltip' : undefined}
    />
  );
};

export default TLResourceCell;
