// The containers of wave 4c (spec 2026-10-16): a panel handles like a window, a card takes its surface from where it
// lies. The test registry has no parts of its own, so the parts the controls look up are the MUI ones, as in
// inline-buttons.test.tsx; what is tested here is the behaviour of the controls, not the drawing of the parts.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { React, createPortal, SurfaceProvider, panelSurface, replacePart, register } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLStack from '../../../com.top_logic.layout.react/react-src/controls/TLStack';
import TLPanel from '../../../com.top_logic.layout.react/react-src/controls/TLPanel';
import TLCard from '../../../com.top_logic.layout.react/react-src/controls/TLCard';
import TLSplitPanel from '../../../com.top_logic.layout.react/react-src/controls/TLSplitPanel';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
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

// A control that draws into the page, not into its place: the overflow menu of a toolbar does so.
register('ContainersPortaledChild', () => createPortal(<button type="button">Menueeintrag</button>, document.body));
register('ContainersItemButton', () => <button type="button">Oeffnen</button>);
const withButton = { controlId: 'bt', module: 'ContainersItemButton', state: {} } as ReturnType<typeof childControl>;
const portaled = { controlId: 'p', module: 'ContainersPortaledChild', state: {} } as ReturnType<typeof childControl>;

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

  it('opens on a click on its restore button once, not twice', async () => {
    const sent = mountAdapter(TLPanel, { ...PANEL, expansionState: 'MINIMIZED' });
    await settle();
    await userEvent.click(screen.getByRole('button', { name: 'js.panel.restore' }));
    await settle();
    expect(sent.mock.calls.filter(call => call[0] === 'toggleMinimize')).toHaveLength(1);
  });

  // A portaled element lies outside the head in the DOM but inside it in React's event tree.
  it('is not opened or maximized by a gesture in something portaled out of its head', async () => {
    const withPortal = { ...PANEL, toolbar: portaled };
    const sent = mountAdapter(TLPanel, { ...withPortal, expansionState: 'MINIMIZED' });
    await settle();
    await userEvent.click(screen.getByText('Menueeintrag'));
    await settle();
    expect(sent).not.toHaveBeenCalledWith('toggleMinimize', expect.anything());
    cleanup();

    const sentOpen = mountAdapter(TLPanel, { ...withPortal, expansionState: 'NORMALIZED' });
    await settle();
    await userEvent.dblClick(screen.getByText('Menueeintrag'));
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

class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

describe('a split handle is a separator', () => {
  const SPLIT = {
    orientation: 'horizontal', resizable: true,
    children: [
      { control: childControl('a', 'A'), size: 30, unit: '%', minSize: 0, scrolling: 'auto', collapsed: false },
      { control: childControl('b', 'B'), size: 70, unit: '%', minSize: 0, scrolling: 'auto', collapsed: false },
    ],
  };

  it('is focusable, says its share and the pane it controls', async () => {
    mountAdapter(TLSplitPanel, SPLIT);
    await settle();
    const handle = screen.getByRole('separator', { name: 'js.splitPanel.handle' });
    expect(handle.getAttribute('tabindex')).toBe('0');
    expect(handle.getAttribute('aria-valuenow')).toBe('30');
    expect(handle.getAttribute('aria-orientation')).toBe('vertical');
    expect(document.getElementById(handle.getAttribute('aria-controls')!)?.classList).toContain('tl-split__pane');
  });

  it('sends the sizes when a key is released, and resets on a double click', async () => {
    const sent = mountAdapter(TLSplitPanel, SPLIT);
    await settle();
    const handle = screen.getByRole('separator', { name: 'js.splitPanel.handle' });
    handle.focus();
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(sent).toHaveBeenCalledWith('updateSizes', expect.objectContaining({ sizes: expect.any(Object) }));
    fireEvent.doubleClick(handle);
    await settle();
    expect(sent).toHaveBeenCalledWith('resetSizes', expect.anything());
  });
});

describe('the width handle of a table column takes the keyboard', () => {
  it('widens the column by one step and reports it when the key is released', async () => {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    const sent = mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      rows: [],
      totalRowCount: 0,
    });
    await settle();
    const handle = screen.getByRole('separator', { name: 'js.table.columnWidth' });
    handle.focus();
    const seenAbove: string[] = [];
    const record = (e: KeyboardEvent) => seenAbove.push(e.key);
    document.addEventListener('keydown', record);
    const width = Number(handle.getAttribute('aria-valuenow'));
    const notPrevented = fireEvent.keyDown(handle, { key: 'ArrowRight' });
    document.removeEventListener('keydown', record);
    await settle();
    // The key is taken: the table's own keyboard does not see it, and nothing is reported before it is released.
    expect(notPrevented).toBe(false);
    expect(seenAbove).toEqual([]);
    expect(sent).not.toHaveBeenCalledWith('columnResize', expect.anything());
    expect(Number(handle.getAttribute('aria-valuenow'))).toBe(width + 16);
    fireEvent.keyUp(handle, { key: 'ArrowRight' });
    await settle();
    expect(sent).toHaveBeenCalledWith('columnResize', { column: 'name', width: 136 });
  });

  it('fits the column to its content on End, at once', async () => {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    const sent = mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      rows: [],
      totalRowCount: 0,
    });
    await settle();
    const handle = screen.getByRole('separator', { name: 'js.table.columnWidth' });
    handle.focus();
    await userEvent.keyboard('{End}');
    await settle();
    // jsdom measures no content: the fitted width is the minimum. What counts is that End reports the column at once.
    expect(sent).toHaveBeenCalledWith('columnResize', expect.objectContaining({ column: 'name' }));
  });
});

// Q24 (#29755): a selectable stack is a list box; the state of an item is aria-selected, not a class.
describe('the items of a selectable container', () => {
  const LIST = {
    children: [childControl('a', 'Altbau'), childControl('b', 'Loft')],
    selectable: true,
    itemKeys: ['ka', 'kb'],
    selected: ['kb'],
  };

  it('are options of a list box, the selected one by aria-selected only', async () => {
    mountAdapter(TLStack, LIST);
    await settle();
    expect(screen.getByRole('listbox')).toBeTruthy();
    const options = screen.getAllByRole('option');
    expect(options).toHaveLength(2);
    expect(options[1].getAttribute('aria-selected')).toBe('true');
    expect(options[0].getAttribute('aria-selected')).toBe('false');
    for (const option of options) {
      expect(option.classList).toContain('tl-item');
      expect(option.className).not.toMatch(/--selectable|--selected|tlItem/);
    }
  });

  it('select an item on a click', async () => {
    const sent = mountAdapter(TLStack, LIST);
    await settle();
    await userEvent.click(screen.getByText('Altbau'));
    await settle();
    expect(sent).toHaveBeenCalledWith('selectItem', { key: 'ka', toggle: false, range: false });
  });

  it('do not select on a click on a button inside the item', async () => {
    const sent = mountAdapter(TLStack, { ...LIST, children: [withButton, childControl('b', 'Loft')] });
    await settle();
    await userEvent.click(screen.getByRole('button', { name: 'Oeffnen' }));
    await settle();
    expect(sent).not.toHaveBeenCalledWith('selectItem', expect.anything());
  });
});
