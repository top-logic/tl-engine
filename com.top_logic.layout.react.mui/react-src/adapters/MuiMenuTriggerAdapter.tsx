import { React, ThemeIcon, tooltipProps } from 'tl-react-bridge';
import type { MenuTriggerPartProps } from 'tl-react-bridge';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import { ICON_ALONE_CLASS, ICON_WITH_LABEL_CLASS, LINK_SX, buttonColor, buttonVariant, iconButtonColor } from './button-look';

/** The chevron after the label of a labeled trigger, pointing at the menu. */
const CHEVRON_ICON = 'css:fa-solid fa-chevron-down';

/**
 * Draws the part `MenuTrigger` ({@link MenuTriggerPartProps}) with Material UI, in the look of the
 * MUI buttons beside it (the appearance maps to variant and color as for the part `Button`):
 *
 * <ul>
 * <li>free content → a `Button` around that content;</li>
 * <li>an icon → an `IconButton` named by the label, which is also its tooltip;</li>
 * <li>otherwise → a `Button` showing the label, with a chevron as `endIcon`.</li>
 * </ul>
 *
 * <p>Every trigger carries `aria-haspopup` with the kind of surface it opens (`menu` unless given)
 * and `aria-expanded` while that surface is open.</p>
 */
const MuiMenuTriggerAdapter: React.FC<MenuTriggerPartProps> = ({
  id, ref, appearance, expanded, haspopup = 'menu', label, icon, children, className, style, onClick, onMouseDown, ...attributes
}) => {
  const common = {
    id,
    ref,
    className,
    style,
    onClick,
    onMouseDown,
    'aria-expanded': expanded,
    'aria-haspopup': haspopup,
  };

  if (children === undefined && icon) {
    return (
      <IconButton {...common} color={iconButtonColor(appearance, false)} aria-label={label} {...tooltipProps(label)} {...attributes}>
        <ThemeIcon encoded={icon} className={ICON_ALONE_CLASS} />
      </IconButton>
    );
  }

  const sx = appearance === 'link' ? LINK_SX : undefined;
  if (children !== undefined) {
    return (
      <Button {...common} variant={buttonVariant(appearance)} color={buttonColor(false)} sx={sx} {...attributes}>
        {children}
      </Button>
    );
  }
  return (
    <Button
      {...common}
      variant={buttonVariant(appearance)}
      color={buttonColor(false)}
      sx={sx}
      endIcon={<ThemeIcon encoded={CHEVRON_ICON} className={ICON_WITH_LABEL_CLASS} />}
      {...attributes}
    >
      {label}
    </Button>
  );
};

export default MuiMenuTriggerAdapter;
