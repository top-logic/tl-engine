import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import {
  resetTableViewPreferencesForTest,
  useTableViewPreferences,
} from './table-view-preferences';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** Shows the preferences the hook hands back, as a table does. */
function Probe({ name }: { name: string }) {
  const { striped, columnLines } = useTableViewPreferences();
  return <span data-probe={name}>{`${striped}|${columnLines}`}</span>;
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

function mark(attributes: Record<string, string>): HTMLElement {
  const element = document.createElement('div');
  element.setAttribute('data-tl-table-view', '');
  for (const [key, value] of Object.entries(attributes)) {
    element.setAttribute(key, value);
  }
  document.body.appendChild(element);
  return element;
}

/** Runs the change and waits until the probe shows what is expected, as the observer reports it asynchronously. */
async function changeAndExpect(change: () => void, name: string, expected: string) {
  await act(async () => {
    change();
    await vi.waitFor(() => expect(text(name)).toBe(expected));
  });
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
    mark({ 'data-tl-table-striped': '', 'data-tl-table-column-lines': '' });
    render('a');
    expect(text('a')).toBe('true|true');
  });

  it('has the factory values without a marked element', () => {
    render('a');
    expect(text('a')).toBe('false|false');
  });

  it('follows the attributes of the marked element, for every component using the hook', async () => {
    const element = mark({});
    render('a', 'b');

    await changeAndExpect(() => element.setAttribute('data-tl-table-striped', ''), 'a', 'true|false');
    expect(text('b')).toBe('true|false');

    await changeAndExpect(() => element.setAttribute('data-tl-table-column-lines', ''), 'a', 'true|true');
    await changeAndExpect(() => element.removeAttribute('data-tl-table-striped'), 'a', 'false|true');
    expect(text('b')).toBe('false|true');
  });

  it('ignores other attributes of the marked element', async () => {
    const element = mark({});
    render('a');
    await act(async () => {
      element.setAttribute('data-other', '');
      await new Promise((resolve) => setTimeout(resolve, 0));
    });
    expect(text('a')).toBe('false|false');
  });

  it('reads the element afresh for the next table once the last one is gone', async () => {
    const element = mark({});
    render('a');
    act(() => root.unmount());
    element.setAttribute('data-tl-table-striped', '');
    root = createRoot(host);
    render('a');
    expect(text('a')).toBe('true|false');
    // The new table follows the element again.
    await changeAndExpect(() => element.setAttribute('data-tl-table-column-lines', ''), 'a', 'true|true');
  });

  it('reads the DOM again after a reset', () => {
    render('a');
    act(() => root.unmount());
    root = createRoot(host);
    resetTableViewPreferencesForTest();
    mark({ 'data-tl-table-striped': '' });
    render('a');
    expect(text('a')).toBe('true|false');
  });
});
