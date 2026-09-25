import {
  React, useTLState, useTLCommand, useKeyboardBinding, rootClassName, ThemeIcon,
  TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED,
} from 'tl-react-bridge';
import type { TLCellProps, ButtonState } from 'tl-react-bridge';
import { BrandButton } from '../example-lib';

const { useCallback } = React;

/** Command a button sends when it is clicked. */
const CMD_CLICK = 'click';

/** The display mode of a button whose state names none. */
const DEFAULT_DISPLAY_MODE: ButtonState.DisplayMode = 'label-only';

/**
 * Renders the state of a TopLogic button (module name `TLButton`) with the library's
 * {@link BrandButton}.
 *
 * <p>Mapping from the control state to the library props:</p>
 * <ul>
 * <li>label → children, disabled → disabled, active → pressed;</li>
 * <li>image → icon, rendered by the bridge's {@link ThemeIcon}; displayMode decides what is shown:
 *     `icon-only` the icon alone (the label then names the button as `aria-label` and tooltip; a
 *     button without image shows its label instead), `icon-label` icon and label, `label-only` or
 *     no display mode the label alone;</li>
 * <li>size `small` of an icon-only button → size `small`;</li>
 * <li>appearance `primary` → variant `primary`, tone `danger` → variant `danger`, else
 *     `secondary`;</li>
 * <li>hidden → nothing is rendered;</li>
 * <li>the configured CSS class and the command's CSS classes → className (through
 *     {@link rootClassName});</li>
 * <li>tooltip → the tooltip attribute of the bridge's tooltip host. Without an explicit tooltip the
 *     label is the tooltip: always for an icon-only button, otherwise only while the label is
 *     clipped.</li>
 * </ul>
 *
 * <p>The library's click callback sends the button's click command, or navigates to the button's
 * URL where the server configured one. The button's keyboard gesture stays bound.</p>
 *
 * <p>Deliberately not reproduced: the appearance defaults a container sets for its buttons, and
 * the compact toolbar dropping the labels of buttons with an icon (the toolbar's stylesheet
 * addresses the classes of the TopLogic button).</p>
 */
const BrandButtonAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<ButtonState>();
  const sendCommand = useTLCommand();

  const label = state.label;
  const image = state.image;
  const disabled = state.disabled === true;
  const hidden = state.hidden === true;
  const tooltip = state.tooltip;
  const navigateUrl = state.navigateUrl;
  const navigateNewWindow = state.navigateNewWindow === true;
  const displayMode = state.displayMode ?? DEFAULT_DISPLAY_MODE;
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

  const showIcon = !!image && displayMode !== 'label-only';
  const iconOnly = showIcon && displayMode === 'icon-only';

  const tooltipText = tooltip ?? label;
  const tooltipProps: Record<string, string> = {};
  if (tooltipText) {
    tooltipProps[TOOLTIP_ATTR] = `text:${tooltipText}`;
    if (!tooltip && !iconOnly) {
      tooltipProps[TOOLTIP_WHEN_ATTR] = WHEN_TRUNCATED;
    }
  }

  return (
    <BrandButton
      id={controlId}
      variant={variant}
      size={iconOnly && state.size === 'small' ? 'small' : 'medium'}
      disabled={disabled}
      pressed={state.active === true}
      icon={showIcon ? <ThemeIcon encoded={image!} /> : undefined}
      aria-label={showIcon ? label : undefined}
      className={rootClassName(state, state.cssClasses)}
      onClick={handleClick}
      {...tooltipProps}
    >
      {iconOnly ? undefined : label}
    </BrandButton>
  );
};

export default BrandButtonAdapter;
