// The installation of Material UI on the page: the adapters it registers, the theme the MUI
// components render with, the styling properties of the TopLogic components.

import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { act, cleanup, render, screen } from '@testing-library/react';
import { STYLE_ELEMENT_ID, DS, MODE_ATTRIBUTE } from './themeProperties';

vi.mock('tl-react-bridge', async importOriginal => ({
  ...await importOriginal<typeof import('tl-react-bridge')>(),
  replace: vi.fn(),
  registerRootWrapper: vi.fn(),
}));

/** The primary color of the theme the tests install. */
const PRIMARY = '#123456';

/** The primary color of the dark scheme of the theme with two schemes. */
const DARK_PRIMARY = '#abcdef';

/** The options of a theme with a light and a dark color scheme. */
const TWO_SCHEMES = {
  cssVariables: { cssVarPrefix: 'app' },
  colorSchemes: {
    light: { palette: { primary: { main: PRIMARY } } },
    dark: { palette: { primary: { main: DARK_PRIMARY } } },
  },
};

/**
 * The module under test, the bridge it registers with and the MUI theme hook, loaded afresh for each
 * test: {@link installMui} runs once per page, i.e. once per instance of the module.
 */
async function load() {
  vi.resetModules();
  const install = await import('./install');
  const bridge = await import('tl-react-bridge');
  const { useTheme, useColorScheme } = await import('@mui/material/styles');
  return {
    ...install,
    replace: vi.mocked(bridge.replace),
    registerRootWrapper: vi.mocked(bridge.registerRootWrapper),
    useTheme,
    useColorScheme,
  };
}

/**
 * Installs Material UI with the given theme and renders a root wrapper around a probe showing the
 * mode of the MUI color scheme context.
 */
async function renderModeProbe(theme: object) {
  const { installMui, registerRootWrapper, useColorScheme } = await load();
  installMui({ theme, replace: [] });
  const Root = registerRootWrapper.mock.calls[0][0];
  function Probe() {
    const { mode, colorScheme } = useColorScheme();
    return <span data-testid="mode">{`${mode} ${colorScheme}`}</span>;
  }
  render(<Root><Probe /></Root>);
  return () => screen.getByTestId('mode').textContent;
}

/** The stylesheet with the CSS custom properties of the MUI theme. */
function muiVariables(): string {
  return Array.from(document.head.querySelectorAll('style[data-emotion="mui-global"]'))
    .map(style => style.textContent)
    .join('\n');
}

/** The names and values of the attributes of `<html>`. */
function htmlAttributes(): string[] {
  return Array.from(document.documentElement.attributes).map(attr => `${attr.name}=${attr.value}`);
}

/** The names the components were replaced under, in the order of the calls. */
function replacedNames(replace: { mock: { calls: unknown[][] } }): unknown[] {
  return replace.mock.calls.map(call => call[0]);
}

beforeEach(() => {
  vi.clearAllMocks();
});

afterEach(() => {
  cleanup();
  document.getElementById(STYLE_ELEMENT_ID)?.remove();
  document.head.querySelectorAll('style[data-emotion]').forEach(style => style.remove());
  document.documentElement.removeAttribute(MODE_ATTRIBUTE);
  localStorage.clear();
});

describe('installMui', () => {
  it('replaces all components by default', async () => {
    const { installMui, COMPONENT_NAMES, replace } = await load();

    installMui({ theme: {} });

    expect(COMPONENT_NAMES).toHaveLength(25);
    expect(replacedNames(replace)).toEqual([...COMPONENT_NAMES]);
    replace.mock.calls.forEach(([, adapter]) => expect(adapter).toBeTypeOf('function'));
  });

  it('replaces all components for the selection "all"', async () => {
    const { installMui, COMPONENT_NAMES, ALL_COMPONENTS, replace } = await load();

    installMui({ theme: {}, replace: ALL_COMPONENTS });

    expect(replacedNames(replace)).toEqual([...COMPONENT_NAMES]);
  });

  it('replaces exactly the selected components', async () => {
    const { installMui, replace } = await load();

    installMui({ theme: {}, replace: ['TLButton', 'TLText'] });

    expect(replacedNames(replace)).toEqual(['TLButton', 'TLText']);
  });

  it('registers the root wrapper for an empty selection too', async () => {
    const { installMui, replace, registerRootWrapper } = await load();

    installMui({ theme: {}, replace: [] });

    expect(replace).not.toHaveBeenCalled();
    expect(registerRootWrapper).toHaveBeenCalledTimes(1);
  });

  it('provides the theme to the MUI components below the root wrapper', async () => {
    const { installMui, registerRootWrapper, useTheme } = await load();

    installMui({
      theme: {
        palette: { primary: { main: PRIMARY } },
        // Options computed from the theme are evaluated by createTheme.
        typography: palette => ({ h6: { color: palette.primary.main } }),
      },
    });

    expect(registerRootWrapper).toHaveBeenCalledTimes(1);
    const Root = registerRootWrapper.mock.calls[0][0];
    function Probe() {
      const theme = useTheme();
      return <span>{`${theme.palette.primary.main} ${String(theme.typography.h6.color)}`}</span>;
    }
    render(<Root><Probe /></Root>);

    expect(screen.getByText(`${PRIMARY} ${PRIMARY}`)).toBeTruthy();
  });

  it('writes the styling properties derived from the theme into the page', async () => {
    const { installMui } = await load();

    installMui({ theme: { palette: { primary: { main: PRIMARY } } } });

    const style = document.getElementById(STYLE_ELEMENT_ID);
    expect(style?.textContent).toContain(`${DS.surfaceBrand}: ${PRIMARY};`);
  });

  it('fails on a second installation', async () => {
    const { installMui, replace } = await load();
    installMui({ theme: {}, replace: ['TLButton'] });

    expect(() => installMui({ theme: {} })).toThrow(/already installed/);
    expect(replace).toHaveBeenCalledTimes(1);
  });

  it('fails for a component without adapter and installs nothing', async () => {
    const { installMui, replace, registerRootWrapper } = await load();

    expect(() => installMui({ theme: {}, replace: ['TLTable' as never] })).toThrow(/TLTable/);
    expect(replace).not.toHaveBeenCalled();
    expect(registerRootWrapper).not.toHaveBeenCalled();
    expect(document.getElementById(STYLE_ELEMENT_ID)).toBeNull();
  });
});

describe('the color schemes of the MUI theme', () => {
  it('are in effect in the mode of the design system they are named after', async () => {
    await renderModeProbe(TWO_SCHEMES);

    const css = muiVariables();
    const dark = css.indexOf(`[${MODE_ATTRIBUTE}="dark"]{`);
    expect(css).toContain(`:root,[${MODE_ATTRIBUTE}="light"]{`);
    expect(dark).toBeGreaterThan(0);
    expect(css.substring(dark)).toContain(`--app-palette-primary-main:${DARK_PRIMARY};`);
  });

  it('leave <html> and the local storage to the UI theme', async () => {
    document.documentElement.setAttribute(MODE_ATTRIBUTE, 'dark');
    const before = htmlAttributes();

    await renderModeProbe(TWO_SCHEMES);
    document.documentElement.setAttribute(MODE_ATTRIBUTE, 'light');
    await act(async () => {});

    expect(htmlAttributes()).toEqual(before.map(attr => attr.replace('=dark', '=light')));
    expect(localStorage.length).toBe(0);
  });

  it('follow the mode of the design system in the color scheme context', async () => {
    document.documentElement.setAttribute(MODE_ATTRIBUTE, 'dark');
    const mode = await renderModeProbe(TWO_SCHEMES);
    expect(mode()).toBe('dark dark');

    await act(async () => document.documentElement.setAttribute(MODE_ATTRIBUTE, 'light'));
    expect(mode()).toBe('light light');

    await act(async () => document.documentElement.setAttribute(MODE_ATTRIBUTE, 'dark'));
    expect(mode()).toBe('dark dark');
  });

  it('of a theme with a single scheme is in effect in every mode', async () => {
    document.documentElement.setAttribute(MODE_ATTRIBUTE, 'dark');
    const mode = await renderModeProbe({ palette: { primary: { main: PRIMARY } } });

    const css = muiVariables();
    expect(css).toContain(`--mui-palette-primary-main:${PRIMARY};`);
    expect(css).not.toContain(`[${MODE_ATTRIBUTE}="dark"]`);
    expect(mode()).toBe('light light');
    expect(localStorage.length).toBe(0);
  });
});
