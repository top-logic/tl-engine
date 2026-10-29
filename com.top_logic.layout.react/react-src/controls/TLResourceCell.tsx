import { React, useTLState, useTLCommand, rootClassName, usePart, TOOLTIP_ATTR } from 'tl-react-bridge';
import type { TLCellProps, ColorRole } from 'tl-react-bridge';

/** Command sent when the user follows the link of the displayed value. */
const CMD_GOTO = 'goto';

/** The prefix of the encoded form of a theme image drawn by a CSS class (see ThemeIcon). */
const CSS_ICON_PREFIX = 'css:';

/** The {@link TOOLTIP_ATTR} value showing the rich tooltip the server answers for the cell. */
const SERVER_TOOLTIP = 'key:tooltip';

/**
 * Displays a business object with optional icon, label, and goto link.
 *
 * State:
 * - label?: string      - display text
 * - iconCss?: string    - CSS icon class (e.g. "bi bi-person-fill")
 * - iconSrc?: string    - image URL for non-CSS icons
 * - hasTooltip: boolean - whether the server provides a rich tooltip (fetched lazily)
 * - hasLink: boolean    - whether clicking navigates to the object
 * - colorRole?: string  - color role the object carries in the model
 *
 * The object is drawn as the part `FieldValue`, the one read-only value, a link running
 * {@link CMD_GOTO} where it has one; an object the model gives a color role is a pill of that role
 * within it.
 */
const TLResourceCell: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const FieldValue = usePart('FieldValue');

  const iconCss = state.iconCss as string | undefined;
  const iconSrc = state.iconSrc as string | undefined;
  const label = (state.label as string | undefined) ?? '';
  const hasTooltip = state.hasTooltip === true;
  const hasLink = state.hasLink === true;
  const colorRole = (state.colorRole as string) || undefined;

  const handleClick = React.useCallback(() => {
    sendCommand(CMD_GOTO, {});
  }, [sendCommand]);

  const icon = iconCss ? CSS_ICON_PREFIX + iconCss : iconSrc;
  const className = rootClassName(state) || undefined;
  const tooltip = { [TOOLTIP_ATTR]: hasTooltip ? SERVER_TOOLTIP : undefined };
  const onClick = hasLink ? handleClick : undefined;

  return (
    <FieldValue id={controlId} text={label} icon={icon} colorRole={colorRole as ColorRole | undefined} onClick={onClick}
      className={className} {...tooltip} />
  );
};

export default TLResourceCell;
