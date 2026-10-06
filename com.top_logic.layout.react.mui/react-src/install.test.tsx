// The installation of Material UI on the page: the adapters it registers, the theme the MUI
// components render with, the styling properties of the TopLogic components.

import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { STYLE_ELEMENT_ID, DS } from './themeProperties';

vi.mock('tl-react-bridge', async importOriginal => ({
  ...await importOriginal<typeof import('tl-react-bridge')>(),
  replace: vi.fn(),
  registerRootWrapper: vi.fn(),
}));

/** The primary color of the theme the tests install. */
const PRIMARY = '#123456';

/**
 * The module under test, the bridge it registers with and the MUI theme hook, loaded afresh for each
 * test: {@link installMui} runs once per page, i.e. once per instance of the module.
 */
async function load() {
  vi.resetModules();
  const install = await import('./install');
  const bridge = await import('tl-react-bridge');
  const { useTheme } = await import('@mui/material/styles');
  return {
    ...install,
    replace: vi.mocked(bridge.replace),
    registerRootWrapper: vi.mocked(bridge.registerRootWrapper),
    useTheme,
  };
}

/** The names the components were replaced under, in the order of the calls. */
function replacedNames(replace: { mock: { calls: unknown[][] } }): unknown[] {
  return replace.mock.calls.map(call => call[0]);
}

beforeEach(() => {
  vi.clearAllMocks();
});

afterEach(() => {
  document.getElementById(STYLE_ELEMENT_ID)?.remove();
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
