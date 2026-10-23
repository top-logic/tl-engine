import { React, useTLState, useImageSrc, avatarInitials, categoryOf, rootClassName, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps, AvatarStateJson } from 'tl-react-bridge';
import Avatar from '@mui/material/Avatar';
import type { CSSObject, Theme } from '@mui/material/styles';
import type { TypographyVariant } from '@mui/material/styles';

/**
 * The attribute of the avatar root naming the category of the design system the display name
 * maps to, `1` to `8` (see `categoryOf` of 'tl-react-bridge'). Present whenever the avatar has
 * a name. A theme restyles a category through `components.MuiAvatar.styleOverrides.root`, e.g.
 * `{ '&[data-category="3"]': { backgroundColor: … } }`.
 */
export const CATEGORY_ATTRIBUTE = 'data-category';

/**
 * The attribute of the avatar root naming its size: `small`, `medium`, `large` or `x-large` (the
 * values of AvatarStateJson.Size), `medium` where the state names none. A theme restyles a size
 * through `components.MuiAvatar.styleOverrides.root`, e.g. `{ '&[data-size="small"]': { … } }`.
 */
export const SIZE_ATTRIBUTE = 'data-size';

/** The number of category roles of the design system. */
const CATEGORY_COUNT = 8;

/** The size of an avatar whose state names none. */
const DEFAULT_SIZE: AvatarStateJson.Size = 'medium';

/**
 * The diameter of each size, the design system's size token, and the typography variant of the
 * MUI theme its initials take the font size from: the variants the styling properties of the
 * label, body and heading type styles of the design system are derived from, as TLAvatar reads
 * its initials in `tl-type-label-strong`, `tl-type-body-strong`, `tl-type-heading-md` and
 * `tl-type-heading-lg`.
 */
const SIZES: Record<AvatarStateJson.Size, { diameter: string; type: TypographyVariant }> = {
  small: { diameter: 'var(--tl-size-avatar-sm)', type: 'caption' },
  medium: { diameter: 'var(--tl-size-avatar-md)', type: 'body2' },
  large: { diameter: 'var(--tl-size-avatar-lg)', type: 'h5' },
  'x-large': { diameter: 'var(--tl-size-avatar-xl)', type: 'h4' },
};

/** The selector of the avatar roots carrying the given value of the given attribute. */
function withAttribute(attribute: string, value: string | number): string {
  return `&[${attribute}="${value}"]`;
}

/**
 * The style the avatar root has before the theme's `styleOverrides` of `MuiAvatar` (see
 * `withRootStyleDefaults` of MuiRoot, which puts it in front of them): the neutral circle of the
 * design system with its border, the surface and border of the category for an avatar with a name,
 * the initials in the primary text color of the theme, and the diameter and font size of each size.
 * Since a style override of the theme follows it in the same rule, the theme wins on every
 * property it sets.
 */
export function avatarRootStyle({ theme }: { theme: Theme }): CSSObject {
  const style: CSSObject = {
    boxSizing: 'border-box',
    border: 'var(--tl-border-width-sm) solid var(--tl-border-control)',
    backgroundColor: 'var(--tl-surface-interactive-selected)',
    color: (theme.vars ?? theme).palette.text.primary,
    fontWeight: theme.typography.fontWeightMedium,
  };
  for (let category = 1; category <= CATEGORY_COUNT; category++) {
    style[withAttribute(CATEGORY_ATTRIBUTE, category)] = {
      backgroundColor: `var(--tl-category-${category}-subtle)`,
      borderColor: `var(--tl-category-${category}-border)`,
    };
  }
  for (const [size, { diameter, type }] of Object.entries(SIZES)) {
    style[withAttribute(SIZE_ATTRIBUTE, size)] = {
      width: diameter,
      height: diameter,
      fontSize: theme.typography[type].fontSize,
    };
  }
  return style;
}

/** The content of the neutral circle: nothing, instead of the person icon of the MUI `Avatar`. */
const NO_CONTENT = <></>;

/**
 * Renders the state of a TopLogic avatar (module name `TLAvatar`) with the MUI `Avatar`.
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>picture (url, or hasData with dataRevision, read through `useImageSrc`) → `src`, with
 *     `alt` = name; where the picture fails to load, the initials of the name;</li>
 * <li>no picture, a name → the initials of the name (`avatarInitials`) as children,
 *     `role="img"` and `aria-label` = name;</li>
 * <li>no picture, no name → the empty neutral circle, `aria-hidden`;</li>
 * <li>name → the category (`categoryOf`) in {@link CATEGORY_ATTRIBUTE}, and the tooltip
 *     of the name (`tooltipProps`), as TLAvatar shows it;</li>
 * <li>size (absent: `medium`) → {@link SIZE_ATTRIBUTE};</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `Avatar`, which
 *     carries the control ID.</li>
 * </ul>
 *
 * <p>The colors, diameters and font sizes are the default root style {@link avatarRootStyle}
 * of the theme, keyed by the two attributes, so that a theme's `styleOverrides` of `MuiAvatar`
 * restyle them.</p>
 */
const MuiAvatarAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AvatarStateJson>>();
  const src = useImageSrc();

  if (state.hidden === true) {
    return null;
  }

  const name = state.name || undefined;
  const rootProps = {
    id: controlId,
    className: rootClassName(state) || undefined,
    [SIZE_ATTRIBUTE]: state.size ?? DEFAULT_SIZE,
  };

  if (name === undefined) {
    return src
      ? <Avatar {...rootProps} src={src} alt="" />
      : <Avatar {...rootProps} aria-hidden="true">{NO_CONTENT}</Avatar>;
  }

  const named = { ...rootProps, [CATEGORY_ATTRIBUTE]: categoryOf(name), ...tooltipProps(name) };
  return src
    ? <Avatar {...named} src={src} alt={name}>{avatarInitials(name)}</Avatar>
    : <Avatar {...named} role="img" aria-label={name}>{avatarInitials(name)}</Avatar>;
};

export default MuiAvatarAdapter;
