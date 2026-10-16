// The look of a TopLogic button in Material UI, shared by the parts drawn as MUI buttons (a button,
// the trigger of a menu), so that a menu trigger looks like the buttons beside it.

import type { ButtonAppearance } from 'tl-react-bridge';
import type { ButtonProps } from '@mui/material/Button';
import type { IconButtonProps } from '@mui/material/IconButton';
import type { SxProps, Theme } from '@mui/material/styles';

/** The MUI variant of each appearance of a button outside a menu. */
const VARIANTS: Record<Exclude<ButtonAppearance, 'menu-item'>, ButtonProps['variant']> = {
  secondary: 'outlined',
  primary: 'contained',
  ghost: 'text',
  link: 'text',
};

/** Size class of the design system for an icon beside a label. */
export const ICON_WITH_LABEL_CLASS = 'tl-icon-sm';

/** Size class of the design system for an icon standing alone. */
export const ICON_ALONE_CLASS = 'tl-icon-md';

/** The look of a pressed button: the alternative in force, or a pressed toggle. */
export const ACTIVE_SX: SxProps<Theme> = { bgcolor: 'action.selected' };

/** The look of a button with appearance `link`: an inline text link rather than a button. */
export const LINK_SX: SxProps<Theme> = {
  minWidth: 0,
  padding: 0,
  textTransform: 'none',
  textDecoration: 'underline',
  verticalAlign: 'baseline',
  '&:hover': { textDecoration: 'underline', bgcolor: 'transparent' },
};

/**
 * The MUI `Button` variant of an appearance: `primary` → `contained`, `ghost` and `link` →
 * `text`, otherwise `outlined`.
 */
export function buttonVariant(appearance: ButtonAppearance): ButtonProps['variant'] {
  return appearance === 'menu-item' ? 'text' : VARIANTS[appearance];
}

/** The MUI `Button` color: `error` for a destructive action, otherwise `primary`. */
export function buttonColor(danger: boolean): ButtonProps['color'] {
  return danger ? 'error' : 'primary';
}

/**
 * The MUI `IconButton` color: `error` for a destructive action, `primary` for the appearance
 * `primary`, otherwise `default`.
 */
export function iconButtonColor(appearance: ButtonAppearance, danger: boolean): IconButtonProps['color'] {
  return danger ? 'error' : appearance === 'primary' ? 'primary' : 'default';
}
