// The styling properties of the TopLogic components, derived from an MUI theme.

import { describe, it, expect, afterEach } from 'vitest';
import { createTheme } from '@mui/material/styles';
import customerTheme from './customerTheme';
import {
  COLOR_SCHEME, DS, STYLE_ELEMENT_ID, THEME, installThemeProperties, statusRole, themeProperties, themeStylesheet,
} from './themeProperties';

const DEFAULT_THEME = createTheme({ cssVariables: true });

const CUSTOMER_THEME = createTheme({ ...customerTheme, cssVariables: true });

const TWO_SCHEME_THEME = createTheme({ cssVariables: true, colorSchemes: { light: true, dark: true } });

/** A theme without CSS variables, defining its single scheme by `palette.mode`. */
const DARK_ONLY_THEME = createTheme({ palette: { mode: 'dark' } });

afterEach(() => {
  document.getElementById(STYLE_ELEMENT_ID)?.remove();
});

describe('themeProperties', () => {
  it('derives the properties of the MUI default theme', () => {
    const light = themeProperties(DEFAULT_THEME).light!;

    expect(light[COLOR_SCHEME]).toBe('light');
    expect(light[DS.surfaceBrand]).toBe('#1976d2');
    expect(light[THEME.buttonPrimary]).toBe('#1976d2');
    expect(light[DS.surfaceBrandHover]).toBe('#1565c0');
    expect(light[DS.textOnBrand]).toBe('#fff');
    expect(light[DS.focusRing]).toBe('#1976d2');
    expect(light[DS.textPrimary]).toBe('rgba(0, 0, 0, 0.87)');
    expect(light[THEME.textSecondary]).toBe('rgba(0, 0, 0, 0.6)');
    expect(light[DS.borderSeparator]).toBe('rgba(0, 0, 0, 0.12)');
    expect(light[DS.borderControl]).toBe('rgba(0, 0, 0, 0.23)');
    expect(light[DS.surfaceBase]).toBe('#fff');
    expect(light[THEME.layer01]).toBe('#fff');
    // The hover of MUI (4 % black) on white paper, opaque.
    expect(light[THEME.layerHover]).toBe('rgb(245, 245, 245)');
    // A selected MUI table row: the primary color at the selected opacity, on the paper.
    expect(light[THEME.layerSelected]).toBe('rgb(237, 244, 251)');
    expect(light[DS.textPlaceholder]).toBe('rgba(0, 0, 0, 0.3654)');
    expect(light[statusRole('error', 'text')]).toBe('#d32f2f');
    expect(light[statusRole('error', 'border')]).toBe('#ef5350');
    expect(light[statusRole('error', 'surface')]).toBe('#d32f2f');
    // The background of a standard MUI alert.
    expect(light[statusRole('error', 'subtle')]).toBe('rgb(253, 237, 237)');
    expect(light[DS.fontFamilySans]).toBe('"Roboto", "Helvetica", "Arial", sans-serif');
    expect(light[DS.fontSizeBody]).toBe('0.875rem');
    expect(light[THEME.bodyCompact01LineHeight]).toBe('calc(0.875rem * 1.43)');
    expect(light[DS.radiusSm]).toBe('4px');
    expect(light[THEME.borderRadius02]).toBe('4px');
    expect(light[DS.shadowPopover]).toBe(DEFAULT_THEME.shadows[8]);
    expect(light[DS.shadowDialog]).toBe(DEFAULT_THEME.shadows[24]);
    expect(light[DS.shadowDrag]).toBe(DEFAULT_THEME.shadows[16]);
    // A small outlined text field: 1rem × 1.4375 + 2 × 8.5px = 40px.
    expect(light[DS.sizeControl]).toBe('calc(1rem * 1.4375 + 17px)');
    // A small button: 13px × 1.75 + 2 × 4px.
    expect(light[DS.sizeControlSm]).toBe('calc(0.8125rem * 1.75 + 8px)');
    // A small table cell: 0.875rem × 1.43 + 2 × 6px + 1px border.
    expect(light[DS.sizeRow]).toBe('calc(0.875rem * 1.43 + 13px)');
  });

  it('takes the colors and fonts of the customer theme as they are', () => {
    const schemes = themeProperties(CUSTOMER_THEME);
    expect(Object.keys(schemes)).toEqual(['light']);
    const light = schemes.light!;

    expect(light[DS.surfaceBrand]).toBe('#28282a');
    expect(light[THEME.interactive]).toBe('#28282a');
    expect(light[DS.surfaceBrandHover]).toBe('#1e1e1f');
    // No contrast correction: the light warning color is the warning text.
    expect(light[statusRole('warning', 'text')]).toBe('#ffc071');
    expect(light[THEME.supportWarning]).toBe('#ffc071');
    expect(light[THEME.supportError]).toBe('#f44336');
    expect(light[DS.fontFamilySans]).toBe("'Work Sans', sans-serif");
    expect(light[THEME.fontFamily]).toBe("'Work Sans', sans-serif");
    expect(light[THEME.fontFamilyDisplay]).toBe("'Roboto Condensed', sans-serif");
    expect(light[DS.fontSizeHeadingSm]).toBe('18px');
    expect(light[DS.fontSizeBody]).toBe('14px');
    expect(light[THEME.bodyCompact01LineHeight]).toBe('calc(14px * 1.5)');
    expect(light[DS.sizeControl]).toBe('calc(16px * 1.4375 + 17px)');
  });

  it('derives the properties of each scheme of a theme with a light and a dark scheme', () => {
    const schemes = themeProperties(TWO_SCHEME_THEME);

    expect(Object.keys(schemes).sort()).toEqual(['dark', 'light']);
    expect(schemes.light![COLOR_SCHEME]).toBe('light');
    expect(schemes.light![DS.surfaceBrand]).toBe('#1976d2');
    expect(schemes.light![DS.surfaceBase]).toBe('#fff');
    expect(schemes.dark![COLOR_SCHEME]).toBe('dark');
    expect(schemes.dark![DS.surfaceBrand]).toBe('#90caf9');
    expect(schemes.dark![DS.surfaceBase]).toBe('#121212');
    expect(schemes.dark![DS.textPrimary]).toBe('#fff');
    expect(schemes.dark![DS.borderControl]).toBe('rgba(255, 255, 255, 0.23)');
  });
});

describe('themeStylesheet', () => {
  it('sets each scheme for its mode of the design system', () => {
    const css = themeStylesheet(TWO_SCHEME_THEME);

    expect(css).toMatch(/^:root, \[data-tl-mode="light"\] \{[^}]*--tl-surface-brand: #1976d2;/);
    expect(css).toMatch(/\[data-tl-mode="dark"\] \{[^}]*--tl-surface-brand: #90caf9;/);
  });

  it('sets the single scheme of a theme for every mode', () => {
    const css = themeStylesheet(CUSTOMER_THEME);

    expect(css).toMatch(/^:root \{/);
    expect(css).not.toContain('data-tl-mode');
    expect(css).toContain(`${DS.surfaceBrand}: #28282a;`);
    expect(css).toContain('color-scheme: light;');
  });

  it('sets the single dark scheme of a theme without CSS variables for every mode', () => {
    const css = themeStylesheet(DARK_ONLY_THEME);

    expect(css).toMatch(/^:root \{/);
    expect(css).toContain('color-scheme: dark;');
    expect(css).toContain(`${DS.surfaceBrand}: #90caf9;`);
  });
});

describe('installThemeProperties', () => {
  it('writes one style element at the end of the head', () => {
    document.head.appendChild(document.createElement('link'));

    installThemeProperties(CUSTOMER_THEME);

    const styles = document.querySelectorAll(`#${STYLE_ELEMENT_ID}`);
    expect(styles).toHaveLength(1);
    expect(document.head.lastElementChild).toBe(styles[0]);
    expect(styles[0].textContent).toBe(themeStylesheet(CUSTOMER_THEME));
  });

  it('replaces the element on a second installation', () => {
    installThemeProperties(DEFAULT_THEME);
    document.head.appendChild(document.createElement('link'));

    installThemeProperties(CUSTOMER_THEME);

    const styles = document.querySelectorAll(`#${STYLE_ELEMENT_ID}`);
    expect(styles).toHaveLength(1);
    expect(document.head.lastElementChild).toBe(styles[0]);
    expect(styles[0].textContent).toContain(`${DS.surfaceBrand}: #28282a;`);
  });
});
