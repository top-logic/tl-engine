import {
  React, useTLState, useTLCommand, useKeyboardBinding, rootClassName, ThemeIcon,
  TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED, useButtonDefaults, menuItemProps,
} from 'tl-react-bridge';
import type { TLCellProps, ButtonStateJson, ButtonAppearance } from 'tl-react-bridge';
import Button from '@mui/material/Button';
import type { ButtonProps } from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ListItemButton from '@mui/material/ListItemButton';
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

/** The appearance of a button neither its state nor its container names. */
const DEFAULT_APPEARANCE: ButtonAppearance = 'secondary';

/** The MUI variant of each appearance of a button outside a menu. */
const VARIANTS: Record<Exclude<ButtonAppearance, 'menu-item'>, ButtonProps['variant']> = {
  secondary: 'outlined',
  primary: 'contained',
  ghost: 'text',
  link: 'text',
};

/** An entry of a menu spans the menu. */
const MENU_ITEM_SX: SxProps<Theme> = { width: '100%' };

/** A destructive entry of a menu. */
const DANGER_MENU_ITEM_SX: SxProps<Theme> = { ...MENU_ITEM_SX, color: 'error.main' };

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
 * Renders the state of a TopLogic button (module name `TLButton`) with the MUI `Button`, with the
 * MUI `IconButton` when it shows its icon alone, or with the MUI `ListItemButton` inside a menu.
 *
 * <p>Mapping from the control state to the MUI props:</p>
 * <ul>
 * <li>label → children; disabled → disabled; hidden → nothing is rendered;</li>
 * <li>appearance → variant: `primary` → `contained`, `ghost` → `text`, `link` → `text` drawn as an
 *     underlined inline link, otherwise `outlined`. An absent or `default` appearance is the one
 *     the container suggests ({@link useButtonDefaults}): `ghost` in a toolbar or an app bar,
 *     `secondary` (`outlined`) in the button bar of a window;</li>
 * <li>inside a menu (the container's appearance `menu-item`, which wins over the state) → a
 *     `ListItemButton` showing icon and label (the MUI `MenuItem` works only inside an MUI menu,
 *     and the menu around the entry is TopLogic's), with the role and the roving tabindex of a menu entry
 *     ({@link menuItemProps}), so that the menu's keyboard navigation finds it; active →
 *     `selected` and `aria-current`; tone `danger` → the error color;</li>
 * <li>tone `danger` → color `error` (not for a link, as in TLButton);</li>
 * <li>size `small` → size `small`;</li>
 * <li>image → `startIcon`, rendered by the bridge's {@link ThemeIcon}; displayMode decides what is
 *     shown: `icon-only` the icon alone in an `IconButton` (the label then names the button as
 *     `aria-label` and tooltip; a button without image shows its label instead), `icon-label` icon
 *     and label, `label-only` or no display mode the label alone. A compact toolbar (the
 *     container's `iconOnly`) shows every button that shows its icon by its icon alone;</li>
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
 */
const MuiButtonAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ButtonStateJson>>();
  const sendCommand = useTLCommand();
  const defaults = useButtonDefaults();

  const label = state.label;
  const image = state.image;
  const disabled = state.disabled === true;
  const hidden = state.hidden === true;
  const active = state.active === true;
  const tooltip = state.tooltip;
  const navigateUrl = state.navigateUrl;
  const navigateNewWindow = state.navigateNewWindow === true;
  // The default appearance of the server is the one the container suggests; inside a menu the
  // container wins over the server, as every button there is an entry.
  const serverAppearance = state.appearance === 'default' ? undefined : state.appearance;
  const appearance: ButtonAppearance = defaults.appearance === 'menu-item'
    ? 'menu-item'
    : serverAppearance ?? defaults.appearance ?? DEFAULT_APPEARANCE;
  const asMenuItem = appearance === 'menu-item';
  const stateMode = state.displayMode ?? DEFAULT_DISPLAY_MODE;
  // Only a button that shows its icon goes compact; inside a menu every entry shows its label.
  const showsIcon = stateMode !== 'label-only' && !!image;
  const displayMode: ButtonStateJson.DisplayMode = defaults.iconOnly && showsIcon
    ? 'icon-only'
    : asMenuItem && image ? 'icon-label' : stateMode;
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

  if (asMenuItem) {
    return (
      <ListItemButton
        id={controlId}
        component="button"
        type="button"
        disabled={disabled}
        onClick={handleClick}
        selected={active}
        aria-current={active ? 'true' : undefined}
        className={rootClassName(state, state.cssClasses)}
        sx={danger ? DANGER_MENU_ITEM_SX : MENU_ITEM_SX}
        {...tooltipProps}
        {...menuItemProps(defaults)}
      >
        {showIcon && (
          <ListItemIcon sx={danger ? { color: 'inherit' } : undefined}>
            <ThemeIcon encoded={image!} className={ICON_WITH_LABEL_CLASS} />
          </ListItemIcon>
        )}
        <ListItemText>{label}</ListItemText>
      </ListItemButton>
    );
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
