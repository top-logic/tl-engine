import { React, ThemeIcon, buttonPartAttributes, buttonLinkAttributes } from 'tl-react-bridge';
import type { ButtonPartProps, PartRef } from 'tl-react-bridge';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ListItemButton from '@mui/material/ListItemButton';
import ToggleButton from '@mui/material/ToggleButton';
import type { SxProps, Theme } from '@mui/material/styles';
import {
  ACTIVE_SX, LINK_SX, ICON_ALONE_CLASS, ICON_WITH_LABEL_CLASS, buttonColor, buttonVariant, iconButtonColor,
} from './button-look';

/** An entry of a menu spans the menu. */
const MENU_ITEM_SX: SxProps<Theme> = { width: '100%' };

/** A destructive entry of a menu. */
const DANGER_MENU_ITEM_SX: SxProps<Theme> = { ...MENU_ITEM_SX, color: 'error.main' };

/** The icon of a destructive menu entry takes the color of the entry. */
const DANGER_ICON_SX: SxProps<Theme> = { color: 'inherit' };

/**
 * The value of the MUI `ToggleButton`, which requires one; outside a `ToggleButtonGroup` it carries
 * no meaning.
 */
const TOGGLE_VALUE = 'on';

/**
 * Draws the part `Button` ({@link ButtonPartProps}) with Material UI:
 *
 * <ul>
 * <li>an entry of a menu (appearance `menu-item`) → a `ListItemButton` showing icon and label (the
 *     MUI `MenuItem` works only inside an MUI menu, and the menu around the entry is TopLogic's);
 *     active → `selected`; danger → the error color;</li>
 * <li>a toggle ({@link ButtonPartProps.checked}) outside a menu → a `ToggleButton`, `selected` while
 *     checked;</li>
 * <li>a button showing its icon alone → an `IconButton`, color `error` for danger, `primary` for
 *     the appearance `primary`;</li>
 * <li>otherwise → a `Button`: appearance → variant (`primary` → `contained`, `ghost` → `text`,
 *     `link` → `text` drawn as an underlined inline link, otherwise `outlined`), danger → color
 *     `error` (not for a link), the icon → `startIcon`.</li>
 * </ul>
 *
 * <p>A link ({@link ButtonPartProps.href}) is the `IconButton` or `Button` drawn as an anchor
 * (`component` `a`) with the attributes {@link buttonLinkAttributes} gives.</p>
 *
 * <p>small → size `small`; active → the selected background of the MUI theme. The accessible name,
 * the pressed, current or checked state, the role of a menu entry and the tooltip are the ones
 * {@link buttonPartAttributes} gives.</p>
 */
const MuiButtonAdapter: React.FC<ButtonPartProps> = (props) => {
  const {
    id, ref, href, target: _target, label, image, display, appearance, danger: dangerTone, small, active, checked,
    disabled, tooltip: _tooltip, className, onClick, onMouseDown, ...attributes
  } = props;
  const isLink = appearance === 'link';
  const danger = dangerTone === true && !isLink;
  const showIcon = !!image && display !== 'label-only';
  const accessibility: Record<string, string | number | boolean | undefined> = { ...buttonPartAttributes(props), ...attributes };
  const common = { id, ref: ref as PartRef<HTMLButtonElement>, disabled, className, onClick, onMouseDown };
  // A link is an anchor also while disabled, when it has no address.
  const link = href === undefined ? {} : { component: 'a' as const, ...buttonLinkAttributes(props) };

  if (appearance === 'menu-item') {
    return (
      <ListItemButton
        {...common}
        component="button"
        type="button"
        selected={active === true}
        sx={danger ? DANGER_MENU_ITEM_SX : MENU_ITEM_SX}
        {...accessibility}
      >
        {showIcon && (
          <ListItemIcon sx={danger ? DANGER_ICON_SX : undefined}>
            <ThemeIcon encoded={image} className={ICON_WITH_LABEL_CLASS} />
          </ListItemIcon>
        )}
        <ListItemText>{label}</ListItemText>
      </ListItemButton>
    );
  }

  const size = small ? 'small' : 'medium';

  if (checked !== undefined) {
    // The MUI toggle button reports its pressed state itself, also while not pressed.
    const { 'aria-pressed': _pressed, ...toggleAttributes } = accessibility;
    return (
      <ToggleButton {...common} value={TOGGLE_VALUE} selected={checked} size={size} {...toggleAttributes}>
        {label}
      </ToggleButton>
    );
  }

  if (showIcon && display === 'icon-only') {
    return (
      <IconButton
        {...common}
        size={size}
        color={iconButtonColor(appearance, danger)}
        sx={active ? ACTIVE_SX : undefined}
        {...link}
        {...accessibility}
      >
        <ThemeIcon encoded={image} className={ICON_ALONE_CLASS} />
      </IconButton>
    );
  }

  return (
    <Button
      {...common}
      size={size}
      variant={buttonVariant(appearance)}
      color={buttonColor(danger)}
      startIcon={showIcon ? <ThemeIcon encoded={image} className={ICON_WITH_LABEL_CLASS} /> : undefined}
      sx={[isLink && LINK_SX, active && ACTIVE_SX].filter(Boolean) as SxProps<Theme>}
      {...link}
      {...accessibility}
    >
      {label}
    </Button>
  );
};

export default MuiButtonAdapter;
