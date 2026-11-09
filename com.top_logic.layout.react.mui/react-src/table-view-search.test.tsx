// The search of the React table never loses a typed term and never sends it after a command the user
// gave later (#30102, K6): the term waits out a typing pause, but a click on a preset chip or a sort
// header, leaving the field, and the table going away all send it first.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import TLTableView from '../../com.top_logic.layout.react/react-src/controls/TLTableView';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiChipAdapter from './adapters/MuiChipAdapter';
import MuiMenuTriggerAdapter from './adapters/MuiMenuTriggerAdapter';
import MuiDisclosureToggleAdapter from './adapters/MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import { mountAdapter, settle, fakeDebounceTimers } from './adapters/wire-test-support';

replacePart('Button', MuiButtonAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Chip', MuiChipAdapter);
replacePart('MenuTrigger', MuiMenuTriggerAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);

class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

afterEach(() => {
  cleanup();
  vi.useRealTimers();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

function mountTable() {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  return mountAdapter(TLTableView, {
    columns: [{ name: 'name', label: 'Name', width: 120, sortable: true, filterable: false }],
    rows: [],
    totalRowCount: 0,
    namedFilters: [{ id: 'mine', label: 'Mine', deletable: false }],
    activeNamedFilter: '',
    filterBar: true,
  });
}

function searchField(): HTMLElement {
  return screen.getByRole('searchbox');
}

function type(term: string) {
  fireEvent.change(searchField(), { target: { value: term } });
}

/** The names of the commands sent so far. */
function names(sent: ReturnType<typeof mountTable>): string[] {
  return sent.mock.calls.map(call => call[0]);
}

describe('the search of the table', () => {
  it('is sent before the command of a preset chip clicked within the pause', async () => {
    fakeDebounceTimers();
    const sent = mountTable();
    type('abc');
    fireEvent.click(screen.getByText('Mine'));
    await settle();
    expect(names(sent)).toEqual(['search', 'applyNamedFilter']);
    expect(sent.mock.calls[0][1]).toEqual({ term: 'abc' });
  });

  it('is sent before the sort of a header clicked within the pause', async () => {
    fakeDebounceTimers();
    const sent = mountTable();
    type('abc');
    fireEvent.click(screen.getByRole('button', { name: /^Name$/ }));
    await settle();
    expect(names(sent)).toEqual(['search', 'sort']);
  });

  it('is sent at once when the field is left', async () => {
    fakeDebounceTimers();
    const sent = mountTable();
    type('abc');
    fireEvent.blur(searchField());
    await settle();
    expect(names(sent)).toEqual(['search']);
  });

  it('is sent when the table goes away within the pause', async () => {
    fakeDebounceTimers();
    const sent = mountTable();
    type('abc');
    cleanup();
    await settle();
    expect(names(sent)).toEqual(['search']);
    expect(sent.mock.calls[0][1]).toEqual({ term: 'abc' });
  });

  it('is sent once when Enter and the blur follow', async () => {
    fakeDebounceTimers();
    const sent = mountTable();
    type('abc');
    fireEvent.keyDown(searchField(), { key: 'Enter' });
    fireEvent.blur(searchField());
    await vi.advanceTimersByTimeAsync(1000);
    await settle();
    expect(names(sent)).toEqual(['search']);
  });

  it('is sent once when the pause ends and the blur follows', async () => {
    fakeDebounceTimers();
    const sent = mountTable();
    type('abc');
    await vi.advanceTimersByTimeAsync(1000);
    fireEvent.blur(searchField());
    await settle();
    expect(names(sent)).toEqual(['search']);
  });

  it('sends nothing when nothing was typed and the field is left', async () => {
    const sent = mountTable();
    fireEvent.blur(searchField());
    cleanup();
    await settle();
    expect(names(sent)).toEqual([]);
  });
});
