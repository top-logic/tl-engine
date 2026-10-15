import { React, ThemeIcon, tooltipProps } from 'tl-react-bridge';
import type { ChipPartProps, ColorRole } from 'tl-react-bridge';
import Chip from '@mui/material/Chip';
import type { ChipProps } from '@mui/material/Chip';
import SvgIcon from '@mui/material/SvgIcon';
import type { SvgIconProps } from '@mui/material/SvgIcon';

/** Size class of the design system for the icon of a chip. */
const ICON_CLASS = 'tl-icon-sm';

/** The Material icon `Cancel`, the glyph MUI draws to remove the value of a chip. */
const CANCEL_PATH = 'M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2m5 13.59L15.59 17 12 13.41 '
  + '8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12z';

/** The keys MUI removes the value of a focused chip with. */
const REMOVE_KEYS = 'Backspace Delete';

/** The style of the less prominent text after the label. */
const DETAIL_STYLE: React.CSSProperties = { marginInlineStart: '0.5em', opacity: 0.7 };

/** The glyph removing the value of a chip. */
function RemoveIcon(props: SvgIconProps) {
  return <SvgIcon {...props}><path d={CANCEL_PATH} /></SvgIcon>;
}

/**
 * The MUI color of the given color role. The roles without an MUI counterpart (neutral, the
 * categories) are drawn in the default color.
 */
export function roleColor(role: ColorRole | undefined): ChipProps['color'] {
  switch (role) {
    case 'brand':
      return 'primary';
    case 'error':
    case 'warning':
    case 'success':
    case 'info':
      return role;
    default:
      return 'default';
  }
}

/**
 * Draws the part `Chip` ({@link ChipPartProps}) with the MUI `Chip` of size `small`.
 *
 * <p>Mapping from the props: label → `label`, followed by the detail in a lighter text; icon →
 * `icon`; colorRole → `color` ({@link roleColor}); a toggle (pressed) → `aria-pressed` and the variant
 * `filled` while pressed, `outlined` while not, a pressed toggle without a color role in `primary`;
 * validation of a toggle → the color `error` or `warning`; onClick → `onClick`, which makes the
 * chip a button; onRemove → MUI's own delete: the delete icon, with removeLabel as its tooltip, and
 * the keys Backspace and Delete on the focused chip, announced by `aria-keyshortcuts` (the icon is
 * hidden from assistive technology, so that it does not add to the name of the chip); disabled →
 * `disabled`; tabIndex → `tabIndex` of the chip; tooltip → the tooltip of the page; handle → ahead of
 * the label; id, ref, className, dragSource and the passed-through attributes → the root of the
 * chip.</p>
 */
const MuiChipAdapter: React.FC<ChipPartProps> = ({
  id, ref, label, detail, icon, colorRole, pressed, validation, disabled, tabIndex, tooltip, className, onClick,
  onRemove, removeLabel, handle, dragSource, ...attributes
}) => {
  const toggle = pressed !== undefined;
  const color: ChipProps['color'] = toggle && validation !== undefined
    ? validation
    : colorRole === undefined && pressed === true ? 'primary' : roleColor(colorRole);
  return (
    <Chip
      id={id}
      ref={ref as React.Ref<HTMLDivElement>}
      size="small"
      label={<>{handle}{label}{detail !== undefined && <span style={DETAIL_STYLE}>{detail}</span>}</>}
      icon={icon ? <ThemeIcon encoded={icon} className={ICON_CLASS} /> : undefined}
      color={color}
      variant={pressed === false ? 'outlined' : 'filled'}
      aria-pressed={pressed}
      disabled={disabled}
      tabIndex={tabIndex}
      onClick={onClick}
      onDelete={onRemove}
      deleteIcon={onRemove ? <RemoveIcon {...tooltipProps(removeLabel)} /> : undefined}
      aria-keyshortcuts={onRemove ? REMOVE_KEYS : undefined}
      className={className}
      {...tooltipProps(tooltip)}
      {...dragSource}
      {...attributes}
    />
  );
};

export default MuiChipAdapter;
