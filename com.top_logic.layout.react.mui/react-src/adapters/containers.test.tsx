// The containers of wave 4c (spec 2026-10-16): a panel handles like a window, a card takes its surface from where it
// lies. The test registry has no parts of its own, so the parts the controls look up are the MUI ones, as in
// inline-buttons.test.tsx; what is tested here is the behaviour of the controls, not the drawing of the parts.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { React, SurfaceProvider, panelSurface, replacePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLPanel from '../../../com.top_logic.layout.react/react-src/controls/TLPanel';
import TLCard from '../../../com.top_logic.layout.react/react-src/controls/TLCard';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiChipAdapter from './MuiChipAdapter';
import { childControl, mountAdapter, settle } from './wire-test-support';

// The panel looks up the part Button; the table (Task 7) also the toggle of its tree rows, the selection boxes of its
// rows, the boxes of its search and the chips of its named filters. Without them a control throws ("No component
// registered for the part …") instead of failing an expectation.
replacePart('Button', MuiButtonAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Chip', MuiChipAdapter);

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

const PANEL = { title: 'Kunden', showMinimize: true, showMaximize: true, child: childControl('x', 'Inhalt') };

function head(): HTMLElement {
  return screen.getByRole('heading', { name: 'Kunden' }).parentElement!;
}

describe('a panel handles like a window', () => {
  it('maximizes on a double click on its head', async () => {
    const sent = mountAdapter(TLPanel, { ...PANEL, expansionState: 'NORMALIZED' });
    await settle();
    await userEvent.dblClick(head());
    await settle();
    expect(sent).toHaveBeenCalledWith('toggleMaximize', expect.anything());
  });

  it('does not maximize on a double click on one of its head buttons', async () => {
    const sent = mountAdapter(TLPanel, { ...PANEL, expansionState: 'NORMALIZED' });
    await settle();
    await userEvent.dblClick(screen.getByRole('button', { name: 'js.panel.minimize' }));
    await settle();
    expect(sent).not.toHaveBeenCalledWith('toggleMaximize', expect.anything());
  });

  it('restores on Escape while maximized, unless something inside used the key', async () => {
    const sent = mountAdapter(TLPanel, { ...PANEL, expansionState: 'MAXIMIZED' });
    await settle();
    const used = new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true });
    used.preventDefault();
    screen.getByText('Inhalt').dispatchEvent(used);
    await settle();
    expect(sent).not.toHaveBeenCalledWith('toggleMaximize', expect.anything());

    fireEvent.keyDown(screen.getByText('Inhalt'), { key: 'Escape' });
    await settle();
    expect(sent).toHaveBeenCalledWith('toggleMaximize', expect.anything());
  });

  it('opens when minimized on a click anywhere on its head, and reports the state at its button', async () => {
    const sent = mountAdapter(TLPanel, { ...PANEL, expansionState: 'MINIMIZED' });
    await settle();
    const restore = screen.getByRole('button', { name: 'js.panel.restore' });
    expect(restore.getAttribute('aria-expanded')).toBe('false');
    expect(restore.hasAttribute('aria-controls')).toBe(false);
    await userEvent.click(head());
    await settle();
    expect(sent).toHaveBeenCalledWith('toggleMinimize', expect.anything());
  });

  // Review Focus 6: the toolbar's slot belongs to the toolbar, its empty space too.
  it('neither opens nor maximizes on a gesture in the toolbar of its head', async () => {
    const withToolbar = { ...PANEL, toolbar: childControl('t', 'Werkzeuge') };
    const sent = mountAdapter(TLPanel, { ...withToolbar, expansionState: 'MINIMIZED' });
    await settle();
    await userEvent.click(screen.getByText('Werkzeuge'));
    await settle();
    expect(sent).not.toHaveBeenCalledWith('toggleMinimize', expect.anything());
    cleanup();

    const sentOpen = mountAdapter(TLPanel, { ...withToolbar, expansionState: 'NORMALIZED' });
    await settle();
    await userEvent.dblClick(screen.getByText('Werkzeuge'));
    await settle();
    expect(sentOpen).not.toHaveBeenCalledWith('toggleMaximize', expect.anything());
  });

  it('names the body it controls while open', async () => {
    mountAdapter(TLPanel, { ...PANEL, expansionState: 'NORMALIZED' });
    await settle();
    const minimize = screen.getByRole('button', { name: 'js.panel.minimize' });
    expect(minimize.getAttribute('aria-expanded')).toBe('true');
    const body = document.getElementById(minimize.getAttribute('aria-controls')!);
    expect(body?.classList).toContain('tl-panel__body');
  });

  it('does not open when normal on a click on its head', async () => {
    const sent = mountAdapter(TLPanel, { ...PANEL, expansionState: 'NORMALIZED' });
    await settle();
    await userEvent.click(head());
    await settle();
    expect(sent).not.toHaveBeenCalledWith('toggleMinimize', expect.anything());
  });
});

describe('a card takes its surface from where it lies', () => {
  const CARD = { title: 'Notiz', child: childControl('y', 'Text') };
  const within = (level: 'layer' | 'overlay'): React.FC<TLCellProps> => (props) => (
    <SurfaceProvider value={{ level, inset: 'page' }}><TLCard {...props} /></SurfaceProvider>
  );

  it('on the page: the layer', async () => {
    mountAdapter(TLCard, CARD);
    await settle();
    const card = document.querySelector('.tl-card')!;
    expect(card.classList).not.toContain('tl-card--nested');
    expect(card.classList).not.toContain('tl-card--overlay');
  });

  it('inside a surface: the nested step', async () => {
    mountAdapter(within('layer'), CARD);
    await settle();
    expect(document.querySelector('.tl-card')!.classList).toContain('tl-card--nested');
  });

  it('in a dialog: the surface of the dialog, never the nested step', async () => {
    mountAdapter(within('overlay'), CARD);
    await settle();
    const card = document.querySelector('.tl-card')!;
    expect(card.classList).toContain('tl-card--overlay');
    expect(card.classList).not.toContain('tl-card--nested');
  });

  // Hendrik 16.10.: only what paints a surface raises the level - a plain panel paints none.
  it('a plain panel passes on the level it lies on; a panel drawn as a card raises it', () => {
    const page = { level: 'base', inset: 'page' } as const;
    const dialog = { level: 'overlay', inset: 'page' } as const;
    expect(panelSurface(page, false)).toEqual({ level: 'base', inset: 'panel' });
    expect(panelSurface(dialog, false)).toEqual({ level: 'overlay', inset: 'panel' });
    expect(panelSurface(page, true)).toEqual({ level: 'layer', inset: 'card' });
    expect(panelSurface(dialog, true)).toEqual({ level: 'overlay', inset: 'card' });
  });
});
