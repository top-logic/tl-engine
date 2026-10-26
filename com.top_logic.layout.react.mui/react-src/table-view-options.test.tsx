// The gear of the React table opens the popover "View" (UI-Durchgang 1, table): density, stripes and
// column lines take effect at once, the column selection and the reset are entries of it, and Escape
// gives the focus back to the gear.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import TLTableView from '../../com.top_logic.layout.react/react-src/controls/TLTableView';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiMenuTriggerAdapter from './adapters/MuiMenuTriggerAdapter';
import MuiDisclosureToggleAdapter from './adapters/MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiChipAdapter from './adapters/MuiChipAdapter';
import { mountAdapter, patchState, settle } from './adapters/wire-test-support';

// The test registry has no parts of its own: the table looks up the ones it draws with.
replacePart('Button', MuiButtonAdapter);
replacePart('MenuTrigger', MuiMenuTriggerAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Chip', MuiChipAdapter);

class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

/** The texts are answered with their keys. */
const VIEW = 'js.table.view';
const DENSITY_DEFAULT = 'js.table.viewDensityDefault';
const DENSITY_NORMAL = 'js.table.viewDensityNormal';
const DENSITY_COMPACT = 'js.table.viewDensityCompact';
const STRIPES = 'js.table.viewStripes';
const COLUMN_LINES = 'js.table.viewColumnLines';
const CHOOSE_COLUMNS = 'js.table.viewChooseColumns';
const RESET = 'js.table.viewReset';

function mountTable(state: Record<string, unknown> = {}) {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  return mountAdapter(TLTableView, {
    columns: [{ name: 'name', label: 'Name', width: 120, sortable: false, filterable: false }],
    rows: [],
    totalRowCount: 0,
    viewOptions: true,
    ...state,
  });
}

function gear(): HTMLElement {
  return screen.getByRole('button', { name: VIEW });
}

async function openView(): Promise<HTMLElement> {
  await userEvent.click(gear());
  await settle();
  return screen.getByRole('dialog', { name: VIEW });
}

describe('the gear of the table', () => {
  it('is there for every table that offers the view, with or without column selection', async () => {
    mountTable({ columnSelect: false });
    await settle();
    expect(gear()).toBeTruthy();
    cleanup();
    mountTable({ columnSelect: true });
    await settle();
    expect(gear()).toBeTruthy();
  });

  it('is not there for a table that does not offer the view', async () => {
    mountTable({ viewOptions: false });
    await settle();
    expect(screen.queryByRole('button', { name: VIEW })).toBeNull();
  });

  it('opens a dialog named "View" and says so on the gear', async () => {
    mountTable();
    await settle();
    expect(gear().getAttribute('aria-haspopup')).toBe('dialog');
    expect(gear().getAttribute('aria-expanded')).toBe('false');
    const dialog = await openView();
    expect(dialog).toBeTruthy();
    expect(gear().getAttribute('aria-expanded')).toBe('true');
  });

  it('opens the dialog without sending anything', async () => {
    const sent = mountTable({ columnSelect: true });
    await settle();
    await openView();
    expect(sent).not.toHaveBeenCalled();
  });
});

describe('the entries of the view', () => {
  it('shows density, stripes and column lines, and the current values', async () => {
    mountTable({ viewDensity: 'normal', striped: true, columnLines: false });
    await settle();
    await openView();
    expect(screen.getByRole('radiogroup')).toBeTruthy();
    expect(screen.getByRole('radio', { name: DENSITY_DEFAULT }).getAttribute('aria-checked')).toBe('false');
    expect(screen.getByRole('radio', { name: DENSITY_NORMAL }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('radio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('false');
    expect((screen.getByRole('switch', { name: STRIPES }) as HTMLInputElement).checked).toBe(true);
    expect((screen.getByRole('switch', { name: COLUMN_LINES }) as HTMLInputElement).checked).toBe(false);
  });

  it('reads a missing density as "as everywhere"', async () => {
    mountTable({});
    await settle();
    await openView();
    expect(screen.getByRole('radio', { name: DENSITY_DEFAULT }).getAttribute('aria-checked')).toBe('true');
  });

  it('sends the density at once, with the other two values as they are', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('radio', { name: DENSITY_COMPACT }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: 'compact', striped: false, columnLines: false });
    expect(screen.getByRole('radio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('true');
  });

  it('sends the empty density for "as everywhere"', async () => {
    const sent = mountTable({ viewDensity: 'compact', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('radio', { name: DENSITY_DEFAULT }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('sends the stripes at once', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('switch', { name: STRIPES }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('sends the column lines at once, keeping what the table shows', async () => {
    const sent = mountTable({ viewDensity: 'normal', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('switch', { name: COLUMN_LINES }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: 'normal', striped: true, columnLines: true });
  });

  it('keeps a change that the server has not echoed yet when the next one is sent', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('radio', { name: DENSITY_COMPACT }));
    await userEvent.click(screen.getByRole('switch', { name: STRIPES }));
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'compact', striped: true, columnLines: false });
  });

  it('follows the server when it echoes other values', async () => {
    mountTable();
    await settle();
    await openView();
    patchState({ viewDensity: 'compact', striped: true, columnLines: true });
    await settle();
    expect(screen.getByRole('radio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('true');
    expect((screen.getByRole('switch', { name: STRIPES }) as HTMLInputElement).checked).toBe(true);
    expect((screen.getByRole('switch', { name: COLUMN_LINES }) as HTMLInputElement).checked).toBe(true);
  });
});

describe('the density segments by keyboard', () => {
  it('moves to the next segment with the arrow, sends it and keeps the focus on it', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    // The popover starts on the segment in force; the other segments are no tab stop.
    const asEverywhere = screen.getByRole('radio', { name: DENSITY_DEFAULT });
    expect(document.activeElement).toBe(asEverywhere);
    expect(screen.getByRole('radio', { name: DENSITY_NORMAL }).getAttribute('tabindex')).toBe('-1');
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'normal', striped: false, columnLines: false });
    expect(document.activeElement).toBe(screen.getByRole('radio', { name: DENSITY_NORMAL }));
  });

  it('wraps around at both ends', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.keyboard('{ArrowLeft}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'compact', striped: false, columnLines: false });
    await userEvent.keyboard('{ArrowDown}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: false, columnLines: false });
  });

  it('closes on Escape also from a segment', async () => {
    mountTable();
    await settle();
    await openView();
    await userEvent.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).toBeNull();
  });
});

describe('the column selection and the reset', () => {
  it('offers the column selection only where the table offers it', async () => {
    mountTable({ columnSelect: false });
    await settle();
    await openView();
    expect(screen.queryByRole('button', { name: CHOOSE_COLUMNS })).toBeNull();
    expect(screen.getByRole('button', { name: RESET })).toBeTruthy();
  });

  it('opens the column selection and closes the view', async () => {
    const sent = mountTable({ columnSelect: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('button', { name: CHOOSE_COLUMNS }));
    await settle();
    expect(sent).toHaveBeenCalledWith('openColumnSelect', {});
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('resets the view and stays open', async () => {
    const sent = mountTable({ viewDensity: 'compact', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('button', { name: RESET }));
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(screen.getByRole('dialog', { name: VIEW })).toBeTruthy();
  });
});

describe('closing the view', () => {
  it('closes on Escape and gives the focus back to the gear', async () => {
    mountTable({ columnSelect: true });
    await settle();
    await openView();
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
    expect(gear().getAttribute('aria-expanded')).toBe('false');
  });

  it('closes on a second press of the gear', async () => {
    mountTable();
    await settle();
    await openView();
    await userEvent.click(gear());
    await settle();
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('is operable with the keyboard alone: Enter on the gear opens it', async () => {
    mountTable();
    await settle();
    gear().focus();
    await userEvent.keyboard('{Enter}');
    await settle();
    expect(screen.getByRole('dialog', { name: VIEW })).toBeTruthy();
  });
});
