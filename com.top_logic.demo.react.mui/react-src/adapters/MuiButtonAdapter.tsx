import {
  React, useTLState, useTLCommand, useKeyboardBinding, rootClassName, ThemeIcon,
  TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED,
} from 'tl-react-bridge';
import type { TLCellProps, ButtonStateJson } from 'tl-react-bridge';
import Button from '@mui/material/Button';
import type { ButtonProps } from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import type { SxProps, Theme } from '@mui/material/styles';

const { useCallback } = React;

/** Command a button sends when it is clicked. */
const CMD_CLICK = 'click';

/** The display mode of a button whose state names none. */
const DEFAULT_DISPLAY_MODE: ButtonStateJson.DisplayMode = 'label-only';

/** Size class of the design system for an icon beside a label. */
const ICON_WITH_LABEL_CLASS = 'tl-icon-sm';

/** Size class of the design system for an icon standing alone. */
const ICON_ALONE_CLASS = 'tl-icon-md';

/** The MUI variant of each appearance a button can have; an absent appearance is `default`. */
const VARIANTS: Record<ButtonStateJson.Appearance, ButtonProps['variant']> = {
  default: 'outlined',
  primary: 'contained',
  ghost: 'text',
  link: 'text',
};

/** The look of a pressed button: the alternative in force, or a pressed toggle. */
const ACTIVE_SX: SxProps<Theme> = { bgcolor: 'action.selected' };

/** The look of a button with appearance `link`: an inline text link rather than a button. */
const LINK_SX: SxProps<Theme> = {
  minWidth: 0,
  padding: 0,
  textTransform: 'none',
  textDecoration: 'underline',
  verticalAlign: 'baseline',
  '&:hover': { textDecoration: 'underline', bgcolor: 'transparent' },
};

/**
 * Renders the state of a TopLogic button (module name `TLButton`) with the MUI `Button`, or with
 * the MUI `IconButton` when it shows its icon alone.
 *
 * <p>Mapping from the control state to the MUI props:</p>
 * <ul>
 * <li>label → children; disabled → disabled; hidden → nothing is rendered;</li>
 * <li>appearance → variant: absent or `default` → `outlined`, `primary` → `contained`, `ghost` →
 *     `text`, `link` → `text` drawn as an underlined inline link;</li>
 * <li>tone `danger` → color `error` (not for a link, as in TLButton);</li>
 * <li>size `small` → size `small`;</li>
 * <li>image → `startIcon`, rendered by the bridge's {@link ThemeIcon}; displayMode decides what is
 *     shown: `icon-only` the icon alone in an `IconButton` (the label then names the button as
 *     `aria-label` and tooltip; a button without image shows its label instead), `icon-label` icon
 *     and label, `label-only` or no display mode the label alone;</li>
 * <li>active → `aria-pressed` and the selected background of the MUI theme;</li>
 * <li>the configured CSS class and the command's CSS classes (cssClasses) → className (through
 *     {@link rootClassName});</li>
 * <li>tooltip → the tooltip attribute of the bridge's tooltip host. Without an explicit tooltip the
 *     label is the tooltip: always for an icon-only button, otherwise only while the label is
 *     clipped;</li>
 * <li>navigateUrl, navigateNewWindow → a click navigates instead of sending the click command;</li>
 * <li>keyGesture → bound through {@link useKeyboardBinding}; declined while hidden or disabled.</li>
 * </ul>
 *
 * <p>Not reproduced: the defaults a container sets for its buttons (a toolbar's appearance, a
 * compact toolbar's icon-only presentation, the menu entry inside a menu with role `menuitem`).
 * TLButton reads them from a context that 'tl-react-bridge' does not export.</p>
 */
const MuiButtonAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ButtonStateJson>>();
  const sendCommand = useTLCommand();

  const label = state.label;
  const image = state.image;
  const disabled = state.disabled === true;
  const hidden = state.hidden === true;
  const active = state.active === true;
  const tooltip = state.tooltip;
  const navigateUrl = state.navigateUrl;
  const navigateNewWindow = state.navigateNewWindow === true;
  const displayMode = state.displayMode ?? DEFAULT_DISPLAY_MODE;
  const appearance = state.appearance ?? 'default';
  const isLink = appearance === 'link';
  const danger = state.tone === 'danger' && !isLink;
  const size: ButtonProps['size'] = state.size === 'small' ? 'small' : 'medium';

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

  const common = {
    id: controlId,
    disabled,
    size,
    onClick: handleClick,
    'aria-pressed': active ? true : undefined,
    'aria-label': showIcon ? label : undefined,
    className: rootClassName(state, state.cssClasses),
    ...tooltipProps,
  };

  if (iconOnly) {
    return (
      <IconButton
        {...common}
        color={danger ? 'error' : appearance === 'primary' ? 'primary' : 'default'}
        sx={active ? ACTIVE_SX : undefined}
      >
        <ThemeIcon encoded={image!} className={ICON_ALONE_CLASS} />
      </IconButton>
    );
  }

  return (
    <Button
      {...common}
      variant={VARIANTS[appearance]}
      color={danger ? 'error' : 'primary'}
      startIcon={showIcon ? <ThemeIcon encoded={image!} className={ICON_WITH_LABEL_CLASS} /> : undefined}
      sx={[isLink && LINK_SX, active && ACTIVE_SX].filter(Boolean) as SxProps<Theme>}
    >
      {label}
    </Button>
  );
};

export default MuiButtonAdapter;
