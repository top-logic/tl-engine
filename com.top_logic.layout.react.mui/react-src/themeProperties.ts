// The styling properties of the TopLogic components, derived from the MUI theme.
//
// The components TopLogic keeps (table, tree, panels, sidebar, toolbars, …) read their colors,
// fonts, corners, shadows and heights from CSS custom properties: the roles of the design system
// (`--tl-…`, tokens.css) and the tokens of the UI themes (`--text-primary`, …, written by the
// UIThemeService). This module computes values for these properties from the MUI theme the MUI
// components render with, the way MUI computes the corresponding values of its own components, and
// writes them into the page as one style element in the CSS cascade layer of Material UI. The
// values of the theme are taken as they are: there is no contrast correction.

import { alpha, darken, decomposeColor, emphasize, hslToRgb, lighten, recomposeColor } from '@mui/material/styles';
import type { Palette, Theme } from '@mui/material/styles';

/**
 * The CSS cascade layer of the rules of Material UI: the layer the emotion cache of the root wrapper
 * puts its rules into (MuiRoot), named after the layer `tl` of the TopLogic stylesheets in the layer
 * order of the page (ClientResources).
 */
export const CSS_LAYER = 'mui';

/** The id of the style element holding the derived properties. */
export const STYLE_ELEMENT_ID = 'tl-mui-theme-properties';

/** A color scheme of a MUI theme. */
export type SchemeName = 'light' | 'dark';

/** The values of the styling properties, by property name. */
export type Properties = Record<string, string>;

/** The values of the styling properties for each color scheme the theme defines. */
export type SchemeProperties = Partial<Record<SchemeName, Properties>>;

/** The CSS property naming the color scheme of the native parts of the page (scrollbars, …). */
export const COLOR_SCHEME = 'color-scheme';

/** The roles of the design system (tokens.css) this module sets. */
export const DS = {
  fontFamilySans: '--tl-font-family-sans',
  fontSizeLabel: '--tl-font-size-label',
  fontSizeBody: '--tl-font-size-body',
  fontSizeHeadingSm: '--tl-font-size-heading-sm',
  fontSizeHeadingMd: '--tl-font-size-heading-md',
  fontSizeHeadingLg: '--tl-font-size-heading-lg',
  fontSizeDisplay: '--tl-font-size-display',
  radiusSm: '--tl-radius-sm',
  radiusMd: '--tl-radius-md',
  sizeControl: '--tl-size-control',
  sizeControlSm: '--tl-size-control-sm',
  sizeRow: '--tl-size-row',
  shadowPopover: '--tl-shadow-popover',
  shadowDialog: '--tl-shadow-dialog',
  shadowDrag: '--tl-shadow-drag',
  surfaceBase: '--tl-surface-base',
  surfaceLayer: '--tl-surface-layer',
  surfaceLayerNested: '--tl-surface-layer-nested',
  surfaceOverlay: '--tl-surface-overlay',
  surfaceField: '--tl-surface-field',
  surfaceInteractiveHover: '--tl-surface-interactive-hover',
  surfaceInteractiveSelected: '--tl-surface-interactive-selected',
  surfaceInteractiveActive: '--tl-surface-interactive-active',
  surfaceDisabled: '--tl-surface-disabled',
  surfaceBrand: '--tl-surface-brand',
  surfaceBrandHover: '--tl-surface-brand-hover',
  surfaceBrandActive: '--tl-surface-brand-active',
  surfaceBrandSubtle: '--tl-surface-brand-subtle',
  surfaceBrandSubtleHover: '--tl-surface-brand-subtle-hover',
  textPrimary: '--tl-text-primary',
  textSecondary: '--tl-text-secondary',
  textHelper: '--tl-text-helper',
  textPlaceholder: '--tl-text-placeholder',
  textDisabled: '--tl-text-disabled',
  textLink: '--tl-text-link',
  textLinkHover: '--tl-text-link-hover',
  textOnBrand: '--tl-text-on-brand',
  textOnBrandHover: '--tl-text-on-brand-hover',
  textOnBrandActive: '--tl-text-on-brand-active',
  iconPrimary: '--tl-icon-primary',
  iconSecondary: '--tl-icon-secondary',
  iconDisabled: '--tl-icon-disabled',
  iconOnBrand: '--tl-icon-on-brand',
  borderSeparator: '--tl-border-separator',
  borderControl: '--tl-border-control',
  borderEmphasis: '--tl-border-emphasis',
  borderInteractive: '--tl-border-interactive',
  borderDisabled: '--tl-border-disabled',
  focusRing: '--tl-focus-ring',
  statusErrorSurfaceHover: '--tl-status-error-surface-hover',
  statusErrorSurfaceActive: '--tl-status-error-surface-active',
  textOnStatusError: '--tl-text-on-status-error',
  textOnStatusErrorHover: '--tl-text-on-status-error-hover',
  textOnStatusErrorActive: '--tl-text-on-status-error-active',
  textOnStatusWarning: '--tl-text-on-status-warning',
  textOnStatusSuccess: '--tl-text-on-status-success',
  textOnStatusInfo: '--tl-text-on-status-info',
} as const;

/** The status colors of a MUI palette; the design system has a role group of the same name each. */
export const STATUSES = ['error', 'warning', 'success', 'info'] as const;

/** A status color. */
export type Status = typeof STATUSES[number];

/** The parts of the role group of a status in the design system. */
export type StatusPart = 'text' | 'border' | 'surface' | 'subtle';

/** The prefix of the role groups of the statuses in the design system. */
const STATUS_ROLE_PREFIX = '--tl-status-';

/** The role of the design system for a part of a status, e.g. `--tl-status-error-text`. */
export function statusRole(status: Status, part: StatusPart): string {
  return `${STATUS_ROLE_PREFIX}${status}-${part}`;
}

/** The tokens of the UI themes (tl-react-theme.config.xml) this module sets. */
export const THEME = {
  fontFamily: '--font-family',
  fontFamilyDisplay: '--font-family-display',
  body02FontSize: '--body-02-font-size',
  body02LineHeight: '--body-02-line-height',
  bodyCompact01FontSize: '--body-compact-01-font-size',
  bodyCompact01LineHeight: '--body-compact-01-line-height',
  label01FontSize: '--label-01-font-size',
  label01LineHeight: '--label-01-line-height',
  headingCompact02FontSize: '--heading-compact-02-font-size',
  headingCompact02LineHeight: '--heading-compact-02-line-height',
  headingCompact02LetterSpacing: '--heading-compact-02-letter-spacing',
  headingCompact03FontSize: '--heading-compact-03-font-size',
  heading03FontSize: '--heading-03-font-size',
  heading03LineHeight: '--heading-03-line-height',
  heading04FontSize: '--heading-04-font-size',
  heading04LineHeight: '--heading-04-line-height',
  display01FontSize: '--display-01-font-size',
  display01LineHeight: '--display-01-line-height',
  cornerRadius: '--corner-radius',
  borderRadius02: '--border-radius-02',
  shadowRaised: '--shadow-raised',
  shadowMenu: '--shadow-menu',
  shadowDialog: '--shadow-dialog',
  background: '--background',
  backgroundInverse: '--background-inverse',
  backgroundSelected: '--background-selected',
  colorSurface: '--color-surface',
  colorDropZone: '--color-drop-zone',
  layer: '--layer',
  layer01: '--layer-01',
  layer02: '--layer-02',
  layerHover: '--layer-hover',
  layerActive: '--layer-active',
  layerSelected: '--layer-selected',
  layerSelectedHover: '--layer-selected-hover',
  field: '--field',
  fieldDisabled: '--field-disabled',
  borderSubtle: '--border-subtle',
  borderStrong: '--border-strong',
  borderInteractive: '--border-interactive',
  focus: '--focus',
  focusInset: '--focus-inset',
  interactive: '--interactive',
  linkPrimary: '--link-primary',
  linkPrimaryHover: '--link-primary-hover',
  buttonPrimary: '--button-primary',
  buttonPrimaryHover: '--button-primary-hover',
  buttonPrimaryActive: '--button-primary-active',
  buttonSecondary: '--button-secondary',
  buttonSecondaryHover: '--button-secondary-hover',
  buttonSecondaryActive: '--button-secondary-active',
  buttonDanger: '--button-danger',
  buttonDangerHover: '--button-danger-hover',
  buttonDisabled: '--button-disabled',
  textPrimary: '--text-primary',
  textSecondary: '--text-secondary',
  textHelper: '--text-helper',
  textPlaceholder: '--text-placeholder',
  textDisabled: '--text-disabled',
  textInverse: '--text-inverse',
  textOnColor: '--text-on-color',
  textOnColorDisabled: '--text-on-color-disabled',
  textError: '--text-error',
  supportError: '--support-error',
  supportWarning: '--support-warning',
  supportSuccess: '--support-success',
  iconPrimary: '--icon-primary',
  iconOnColor: '--icon-on-color',
  iconDisabled: '--icon-disabled',
} as const;

/** Selector of the page root: the properties of a theme with a single color scheme. */
const ROOT_SELECTOR = ':root';

/**
 * The attribute of `<html>` naming the appearance mode of the design system, `light` or `dark`
 * (written by the UI theme in effect).
 */
export const MODE_ATTRIBUTE = 'data-tl-mode';

/** The selector of each mode of the design system; the light one is also the page default. */
const MODE_SELECTORS: Record<SchemeName, string> = {
  light: `${ROOT_SELECTOR}, [${MODE_ATTRIBUTE}="light"]`,
  dark: `[${MODE_ATTRIBUTE}="dark"]`,
};

/**
 * The opacity of the placeholder of an MUI input in each scheme (InputBase draws it in the text
 * color at this opacity).
 */
const PLACEHOLDER_OPACITY: Record<SchemeName, number> = { light: 0.42, dark: 0.5 };

/** The opacity of the border of an outlined MUI input over the text color of the background. */
const CONTROL_BORDER_OPACITY = 0.23;

/** How far MUI moves the light status color to the background for a standard alert. */
const STATUS_SUBTLE_COEFFICIENT = 0.9;

/** How far MUI moves the background color for the dark surface of a snackbar. */
const INVERSE_COEFFICIENT = 0.8;

/** The line height of an MUI input, relative to its font size (InputBase). */
const INPUT_LINE_HEIGHT = 1.4375;

/** Vertical padding of a small outlined MUI input, top plus bottom (OutlinedInput). */
const INPUT_PADDING_SMALL = '17px';

/** Font size of a small MUI button, in px before the scaling of the theme (Button). */
const BUTTON_FONT_SIZE_SMALL_PX = 13;

/** Vertical padding of a small MUI button, top plus bottom, its border included (Button). */
const BUTTON_PADDING_SMALL = '8px';

/** Vertical padding of a small MUI table cell, top plus bottom, plus its bottom border (TableCell). */
const TABLE_CELL_PADDING_SMALL = '13px';

/** The elevations of MUI: a card, a menu or popover, a drawer, a dialog. */
const ELEVATION_RAISED = 1;
const ELEVATION_MENU = 8;
const ELEVATION_DRAG = 16;
const ELEVATION_DIALOG = 24;

/** A length of a theme: a number is a length in pixels, a string a CSS length. */
function length(value: string | number | undefined): string {
  return typeof value === 'number' ? `${value}px` : String(value);
}

/**
 * A line height as a length: a number is relative to the font size, as MUI writes it; a string is
 * a CSS length.
 */
function lineHeight(variant: { fontSize?: string | number; lineHeight?: string | number }): string {
  const size = length(variant.fontSize);
  const height = variant.lineHeight;
  return typeof height === 'number' ? `calc(${size} * ${height})` : length(height);
}

/** The color with its opacity scaled, as an element of that color at the given opacity shows. */
function fade(color: string, opacity: number): string {
  const [r, g, b, a] = rgbValues(color);
  return recomposeColor({ type: 'rgba', values: [r, g, b, a * opacity] });
}

/**
 * The opaque color a translucent color shows on an opaque background. A surface TopLogic draws
 * over scrolling content (a frozen table cell) must not let it shine through.
 */
function over(background: string, color: string): string {
  const bg = rgbValues(background);
  const fg = rgbValues(color);
  const a = fg[3];
  const mixed = [0, 1, 2].map(i => Math.round(fg[i] * a + bg[i] * (1 - a)));
  return recomposeColor({ type: 'rgb', values: [mixed[0], mixed[1], mixed[2]] });
}

/** The red, green, blue and opacity of a color. */
function rgbValues(color: string): [number, number, number, number] {
  const decomposed = decomposeColor(decomposeColor(color).type.startsWith('hsl') ? hslToRgb(color) : color);
  const [r, g, b] = decomposed.values;
  return [r, g, b, decomposed.values.length > 3 ? decomposed.values[3]! : 1];
}

/**
 * The values of the styling properties for one color scheme of the theme.
 *
 * @param theme The theme (typography, shape, shadows).
 * @param palette The palette of the scheme.
 * @param scheme The name of the scheme.
 */
function schemeProperties(theme: Theme, palette: Palette, scheme: SchemeName): Properties {
  const { typography, shadows } = theme;
  const { primary, text, background, action, divider, common } = palette;
  const light = scheme === 'light';
  const statusBackground = light ? lighten : darken;

  const paper = background.paper;
  const radius = length(theme.shape.borderRadius);
  const onBackground = light ? common.black : common.white;
  const controlBorder = alpha(onBackground, CONTROL_BORDER_OPACITY);
  const hover = over(paper, action.hover);
  const selected = over(paper, action.selected);
  const pressed = over(paper, action.focus);
  const brandSubtle = over(paper, alpha(primary.main, action.selectedOpacity));
  const brandSubtleHover = over(paper, alpha(primary.main, action.selectedOpacity + action.hoverOpacity));
  const inverse = emphasize(background.default, INVERSE_COEFFICIENT);

  const fieldHeight = `calc(${length(typography.body1.fontSize)} * ${INPUT_LINE_HEIGHT} + ${INPUT_PADDING_SMALL})`;
  const buttonHeightSmall = `calc(${typography.pxToRem(BUTTON_FONT_SIZE_SMALL_PX)} * ${typography.button.lineHeight} + ${BUTTON_PADDING_SMALL})`;
  const rowHeight = `calc(${length(typography.body2.fontSize)} * ${typography.body2.lineHeight} + ${TABLE_CELL_PADDING_SMALL})`;

  const result: Properties = {};
  function set(value: string, ...names: string[]) {
    for (const name of names) {
      result[name] = value;
    }
  }

  set(palette.mode, COLOR_SCHEME);

  // Typography.
  set(String(typography.fontFamily), DS.fontFamilySans, THEME.fontFamily);
  set(String(typography.h6.fontFamily ?? typography.fontFamily), THEME.fontFamilyDisplay);
  set(length(typography.body2.fontSize), DS.fontSizeBody, THEME.body02FontSize, THEME.bodyCompact01FontSize);
  set(lineHeight(typography.body2), THEME.body02LineHeight, THEME.bodyCompact01LineHeight);
  set(length(typography.caption.fontSize), DS.fontSizeLabel, THEME.label01FontSize);
  set(lineHeight(typography.caption), THEME.label01LineHeight);
  set(length(typography.subtitle2.fontSize), THEME.headingCompact02FontSize);
  set(lineHeight(typography.subtitle2), THEME.headingCompact02LineHeight);
  set(length(typography.subtitle2.letterSpacing ?? 0), THEME.headingCompact02LetterSpacing);
  set(length(typography.subtitle1.fontSize), THEME.headingCompact03FontSize);
  set(length(typography.h6.fontSize), DS.fontSizeHeadingSm, THEME.heading03FontSize);
  set(lineHeight(typography.h6), THEME.heading03LineHeight);
  set(length(typography.h5.fontSize), DS.fontSizeHeadingMd, THEME.heading04FontSize);
  set(lineHeight(typography.h5), THEME.heading04LineHeight);
  set(length(typography.h4.fontSize), DS.fontSizeHeadingLg, THEME.display01FontSize);
  set(lineHeight(typography.h4), THEME.display01LineHeight);
  set(length(typography.h3.fontSize), DS.fontSizeDisplay);

  // Shape, elevation and the heights of the MUI controls the adapters render.
  set(radius, DS.radiusSm, DS.radiusMd, THEME.cornerRadius, THEME.borderRadius02);
  set(shadows[ELEVATION_RAISED], THEME.shadowRaised);
  set(shadows[ELEVATION_MENU], DS.shadowPopover, THEME.shadowMenu);
  set(shadows[ELEVATION_DRAG], DS.shadowDrag);
  set(shadows[ELEVATION_DIALOG], DS.shadowDialog, THEME.shadowDialog);
  set(fieldHeight, DS.sizeControl);
  set(buttonHeightSmall, DS.sizeControlSm);
  set(rowHeight, DS.sizeRow);

  // Surfaces.
  set(background.default, DS.surfaceBase, THEME.background);
  set(paper, DS.surfaceLayer, DS.surfaceLayerNested, DS.surfaceOverlay, DS.surfaceField,
    THEME.colorSurface, THEME.layer01, THEME.layer02, THEME.field, THEME.fieldDisabled, THEME.focusInset);
  set(over(background.default, action.hover), THEME.layer);
  set(hover, DS.surfaceInteractiveHover, THEME.layerHover);
  set(selected, DS.surfaceInteractiveSelected, THEME.backgroundSelected);
  set(pressed, DS.surfaceInteractiveActive, THEME.layerActive);
  set(action.disabledBackground, DS.surfaceDisabled, THEME.buttonDisabled);
  set(brandSubtle, DS.surfaceBrandSubtle, THEME.layerSelected);
  set(brandSubtleHover, DS.surfaceBrandSubtleHover, THEME.layerSelectedHover);
  set(alpha(primary.main, action.selectedOpacity), THEME.colorDropZone);
  set(inverse, THEME.backgroundInverse);
  set(palette.getContrastText(inverse), THEME.textInverse);

  // The brand: the primary color.
  set(primary.main, DS.surfaceBrand, DS.borderInteractive, DS.focusRing, DS.textLink,
    THEME.buttonPrimary, THEME.buttonSecondary, THEME.interactive, THEME.borderInteractive, THEME.focus,
    THEME.linkPrimary);
  set(primary.dark, DS.surfaceBrandHover, DS.surfaceBrandActive, DS.textLinkHover,
    THEME.buttonPrimaryHover, THEME.buttonPrimaryActive, THEME.buttonSecondaryHover, THEME.buttonSecondaryActive,
    THEME.linkPrimaryHover);
  set(primary.contrastText, DS.textOnBrand, DS.textOnBrandHover, DS.textOnBrandActive, DS.iconOnBrand,
    THEME.textOnColor, THEME.iconOnColor);

  // Text, icons and lines.
  set(text.primary, DS.textPrimary, THEME.textPrimary);
  set(text.secondary, DS.textSecondary, DS.textHelper, DS.iconSecondary, THEME.textSecondary, THEME.textHelper);
  set(fade(text.primary, PLACEHOLDER_OPACITY[scheme]), DS.textPlaceholder, THEME.textPlaceholder);
  set(text.disabled, DS.textDisabled, THEME.textDisabled);
  set(action.active, DS.iconPrimary, THEME.iconPrimary);
  set(action.disabled, DS.iconDisabled, DS.borderDisabled, THEME.iconDisabled, THEME.textOnColorDisabled);
  set(divider, DS.borderSeparator, THEME.borderSubtle);
  set(controlBorder, DS.borderControl, THEME.borderStrong);
  set(text.primary, DS.borderEmphasis);

  // The status colors.
  for (const status of STATUSES) {
    const color = palette[status];
    // Text in a status color is drawn in its main color, as by a MUI Typography of that color.
    set(color.main, statusRole(status, 'text'));
    set(color.light, statusRole(status, 'border'));
    set(light ? color.main : color.dark, statusRole(status, 'surface'));
    set(statusBackground(color.light, STATUS_SUBTLE_COEFFICIENT), statusRole(status, 'subtle'));
  }
  set(palette.error.dark, DS.statusErrorSurfaceHover, DS.statusErrorSurfaceActive, THEME.buttonDangerHover);
  set(palette.error.contrastText, DS.textOnStatusError, DS.textOnStatusErrorHover, DS.textOnStatusErrorActive);
  set(palette.warning.contrastText, DS.textOnStatusWarning);
  set(palette.success.contrastText, DS.textOnStatusSuccess);
  set(palette.info.contrastText, DS.textOnStatusInfo);
  set(palette.error.main, THEME.textError, THEME.supportError, THEME.buttonDanger);
  set(palette.warning.main, THEME.supportWarning);
  set(palette.success.main, THEME.supportSuccess);

  return result;
}

/** A theme with CSS variables: the palette of each of its color schemes. */
type ThemeWithSchemes = { colorSchemes?: Partial<Record<SchemeName, { palette: Palette }>> };

/**
 * The values of the styling properties of the TopLogic components for each color scheme the MUI
 * theme defines: both schemes of a theme with a light and a dark one, the one scheme of a theme
 * without color schemes (its `palette.mode`).
 */
export function themeProperties(theme: Theme): SchemeProperties {
  const result: SchemeProperties = {};
  const schemes = (theme as Theme & ThemeWithSchemes).colorSchemes;
  if (schemes !== undefined && Object.keys(schemes).length > 0) {
    for (const scheme of Object.keys(schemes) as SchemeName[]) {
      const palette = schemes[scheme]?.palette;
      if (palette !== undefined) {
        result[scheme] = schemeProperties(theme, palette, scheme);
      }
    }
  } else {
    result[theme.palette.mode] = schemeProperties(theme, theme.palette, theme.palette.mode);
  }
  return result;
}

/**
 * The color scheme of a theme with a single one, or `null` for a theme with a light and a dark
 * scheme. The MUI components render in that scheme in every mode of the design system, so the
 * whole page is held in its mode.
 */
export function singleScheme(theme: Theme): SchemeName | null {
  const names = Object.keys(themeProperties(theme)) as SchemeName[];
  return names.length === 1 ? names[0] : null;
}

/** A CSS rule setting the given properties. */
function rule(selector: string, properties: Properties): string {
  const declarations = Object.entries(properties).map(([name, value]) => `  ${name}: ${value};`);
  return `${selector} {\n${declarations.join('\n')}\n}\n`;
}

/**
 * The stylesheet setting the styling properties of the TopLogic components from the MUI theme.
 *
 * <p>A theme with a light and a dark scheme sets each scheme for the mode of the design system
 * (`data-tl-mode` of `<html>`). A theme with a single scheme sets it for the page root, so that it
 * is in effect in every mode; the page is held in the mode of that scheme (see
 * {@link singleScheme}), so that the properties this module does not set are in that mode as
 * well.</p>
 *
 * <p>The rules are in the layer {@link CSS_LAYER}, which comes after the layer `tl` of the theme
 * tokens and the design system, so they win against these. A rule of the application setting one
 * of the properties is unlayered and wins against them in turn.</p>
 */
export function themeStylesheet(theme: Theme): string {
  const schemes = themeProperties(theme);
  const names = Object.keys(schemes) as SchemeName[];
  const rules = names.length === 1
    ? rule(ROOT_SELECTOR, schemes[names[0]]!)
    : names.map(scheme => rule(MODE_SELECTORS[scheme], schemes[scheme]!)).join('');
  return `@layer ${CSS_LAYER} {\n${rules}}\n`;
}

/**
 * Writes the styling properties derived from the MUI theme into the page: one style element with
 * the id {@link STYLE_ELEMENT_ID}, appended to the end of `<head>`. Its rules are in the layer
 * {@link CSS_LAYER} (see {@link themeStylesheet}). An element written before is replaced.
 */
export function installThemeProperties(theme: Theme): void {
  document.getElementById(STYLE_ELEMENT_ID)?.remove();
  const style = document.createElement('style');
  style.id = STYLE_ELEMENT_ID;
  style.textContent = themeStylesheet(theme);
  document.head.appendChild(style);
}
