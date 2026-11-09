import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import {
  resetTableViewPreferencesForTest,
  setTableViewPreferences,
  useTableViewPreferences,
} from './table-view-preferences';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** Shows the preferences the hook hands back, as a table does. */
function Probe({ name }: { name: string }) {
  const { density, striped, columnLines } = useTableViewPreferences();
  return <span data-probe={name}>{`${density}|${striped}|${columnLines}`}</span>;
}

let host: HTMLElement;
let root: Root;

function text(name: string): string {
  return host.querySelector(`[data-probe="${name}"]`)!.textContent!;
}

function render(...names: string[]) {
  act(() => {
    root.render(<>{names.map((n) => <Probe key={n} name={n} />)}</>);
  });
}

function mark(attributes: Record<string, string>) {
  const element = document.createElement('div');
  element.setAttribute('data-tl-table-view', '');
  for (const [key, value] of Object.entries(attributes)) {
    element.setAttribute(key, value);
  }
  document.body.appendChild(element);
}

describe('table-view-preferences', () => {
  beforeAll(() => {
    globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  });

  beforeEach(() => {
    resetTableViewPreferencesForTest();
    host = document.createElement('div');
    document.body.appendChild(host);
    root = createRoot(host);
  });

  afterEach(() => {
    act(() => root.unmount());
    host.remove();
    document.querySelectorAll('[data-tl-table-view]').forEach((e) => e.remove());
    resetTableViewPreferencesForTest();
  });

  it('reads the start values from the marked element', () => {
    mark({ 'data-tl-table-density': 'compact', 'data-tl-table-striped': '', 'data-tl-table-column-lines': '' });
    render('a');
    expect(text('a')).toBe('compact|true|true');
  });

  it('has the factory values without a marked element', () => {
    render('a');
    expect(text('a')).toBe('|false|false');
  });

  it('takes an unknown density value as the factory density', () => {
    mark({ 'data-tl-table-density': 'huge' });
    render('a');
    expect(text('a')).toBe('|false|false');
  });

  it('re-renders all components using the hook after a change', () => {
    render('a', 'b');
    act(() => setTableViewPreferences({ density: 'normal', striped: true, columnLines: false }));
    expect(text('a')).toBe('normal|true|false');
    expect(text('b')).toBe('normal|true|false');
  });

  it('reads the DOM again after a reset', () => {
    render('a');
    act(() => setTableViewPreferences({ density: 'normal', striped: true, columnLines: true }));
    act(() => root.unmount());
    root = createRoot(host);
    resetTableViewPreferencesForTest();
    mark({ 'data-tl-table-striped': '' });
    render('a');
    expect(text('a')).toBe('|true|false');
  });
});
