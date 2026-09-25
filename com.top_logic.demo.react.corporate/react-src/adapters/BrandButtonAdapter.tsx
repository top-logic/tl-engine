import { React, useTLState, useTLCommand, useKeyboardBinding, rootClassName, TOOLTIP_ATTR } from 'tl-react-bridge';
import type { TLCellProps, ButtonState } from 'tl-react-bridge';
import { BrandButton } from '../example-lib';

const { useCallback } = React;

/** Command a button sends when it is clicked. */
const CMD_CLICK = 'click';

/**
 * Renders the state of a TopLogic button (module name `TLButton`) with the library's
 * {@link BrandButton}.
 *
 * <p>Mapping from the control state to the library props:</p>
 * <ul>
 * <li>label → children, disabled → disabled, active → pressed;</li>
 * <li>appearance `primary` → variant `primary`, tone `danger` → variant `danger`, else
 *     `secondary`;</li>
 * <li>hidden → nothing is rendered;</li>
 * <li>the configured CSS class and the command's CSS classes → className (through
 *     {@link rootClassName});</li>
 * <li>an explicit tooltip → the tooltip attribute of the bridge's tooltip host.</li>
 * </ul>
 *
 * <p>The library's click callback sends the button's click command, or navigates to the button's
 * URL where the server configured one. The button's keyboard gesture stays bound.</p>
 *
 * <p>Deliberately not reproduced: the theme icon (every button shows its label, also one the
 * server displays as icon only), the button size and display mode, the label tooltip that
 * TopLogic shows while a label is clipped, and the appearance defaults a container sets for its
 * buttons.</p>
 */
const BrandButtonAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<ButtonState>();
  const sendCommand = useTLCommand();

  const label = state.label;
  const disabled = state.disabled === true;
  const hidden = state.hidden === true;
  const tooltip = state.tooltip;
  const navigateUrl = state.navigateUrl;
  const navigateNewWindow = state.navigateNewWindow === true;
  const variant = state.tone === 'danger' ? 'danger'
    : state.appearance === 'primary' ? 'primary' : 'secondary';

  const handleClick = useCallback(() => {
    if (navigateUrl) {
      if (navigateNewWindow) {
        window.open(navigateUrl, '_blank');
      } else {
        window.location.assign(navigateUrl);
      }
      return;
    }
    sendCommand(CMD_CLICK);
  }, [sendCommand, navigateUrl, navigateNewWindow]);

  // A hidden or disabled button declines the gesture, so it falls through to an outer binding.
  useKeyboardBinding(state.keyGesture, () => {
    if (disabled || hidden) {
      return false;
    }
    handleClick();
    return true;
  });

  if (hidden) {
    return null;
  }

  const tooltipProps: Record<string, string> = tooltip ? { [TOOLTIP_ATTR]: `text:${tooltip}` } : {};
  return (
    <BrandButton
      id={controlId}
      variant={variant}
      disabled={disabled}
      pressed={state.active === true}
      className={rootClassName(state, state.cssClasses)}
      onClick={handleClick}
      {...tooltipProps}
    >
      {label}
    </BrandButton>
  );
};

export default BrandButtonAdapter;
