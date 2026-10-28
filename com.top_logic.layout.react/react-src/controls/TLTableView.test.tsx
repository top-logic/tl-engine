import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
// The controls with their parts, as the page registers them: the table draws its buttons and boxes as parts.
import '../controls-entry';
import { React, register } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const TABLE_ID = 'table';

/** The stand-in for the control of a cell. */
const Cell: React.FC<TLCellProps> = ({ controlId }) => <span id={controlId} />;

function cell(controlId: string): Record<string, unknown> {
  return { controlId, module: 'TableTestCell', state: {} };
}

/** The callbacks of the resize observers the table installed: jsdom has no layout and no ResizeObserver. */
let resized: Array<() => void> = [];

class ResizeObserverStub {
  constructor(private readonly callback: () => void) {}

  observe(): void {
    resized.push(this.callback);
  }

  unobserve(): void {}

  disconnect(): void {
    resized = resized.filter(callback => callback !== this.callback);
  }
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('TableTestCell', Cell);
});

afterEach(async () => {
  act(() => unmount(TABLE_ID));
  document.getElementById(TABLE_ID)?.remove();
  vi.unstubAllGlobals();
  resized = [];
  await settle();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

async function settle(): Promise<void> {
  for (let round = 0; round < 5; round++) {
    await new Promise<void>(resolve => setTimeout(resolve, 0));
  }
}

/** The commands the table sent: their names and arguments. */
let sent: Array<{ command: string, args: Record<string, unknown> }> = [];

/**
 * A table of three columns of 150 px and two rows, in a window of the given display class; the column "budget" is
 * left out in the compact display.
 */
function mountTable({ compact = false, frozenColumnCount = 0 } = {}): void {
  vi.stubGlobal('ResizeObserver', ResizeObserverStub);
  vi.stubGlobal('matchMedia', (query: string) => ({
    matches: compact, media: query, addEventListener: () => {}, removeEventListener: () => {},
  }));
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === TABLE_ID) {
      sent.push({ command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.id = TABLE_ID;
  document.body.appendChild(element);
  const columns = ['person', 'budget', 'used'].map(name =>
    ({ name, label: name, width: 150, sortable: false, ...(name === 'budget' ? { compact: false } : {}) }));
  const rows = [0, 1].map(index => ({ id: 'r' + index, index, cells: Object.fromEntries(columns.map(column => [column.name, cell(`c${index}-${column.name}`)])) }));
  act(() => mount(TABLE_ID, 'TLTableView', {
    columns, rows, totalRowCount: rows.length, scrollStart: 0, selectionMode: 'single', selectedRows: [],
    cursorIndex: -1, frozenColumnCount,
  }, 'w', '/ctx'));
}

function headings(): string[] {
  return [...document.querySelectorAll(`#${TABLE_ID} [role=columnheader]`)].map(heading => heading.textContent ?? '');
}

function cellsOfFirstRow(): string[] {
  return [...document.querySelectorAll(`#${TABLE_ID} [id^="c0-"]`)].map(element => element.id);
}

function body(): HTMLElement {
  return document.querySelector<HTMLElement>(`#${TABLE_ID} .tl-table__body`)!;
}

function grid(): HTMLElement {
  return document.querySelector<HTMLElement>(`#${TABLE_ID} .tl-table__grid`)!;
}

/** Lays the body out as the given content in the given box, and tells the table its size changed. */
function layOut(content: { width: number, height: number }, box: { width: number, height: number }): void {
  const element = body();
  Object.defineProperty(element, 'scrollWidth', { configurable: true, get: () => content.width });
  Object.defineProperty(element, 'scrollHeight', { configurable: true, get: () => content.height });
  Object.defineProperty(element, 'clientWidth', { configurable: true, get: () => box.width });
  Object.defineProperty(element, 'clientHeight', { configurable: true, get: () => box.height });
  act(() => resized.forEach(callback => callback()));
}

describe('the body of a table', () => {
  it('is no tab stop of its own while everything fits, and hands its focus on to the grid', () => {
    mountTable();
    layOut({ width: 450, height: 72 }, { width: 800, height: 400 });

    expect(body().tabIndex).toBe(-1);
    expect(body().hasAttribute('aria-label')).toBe(false);
    act(() => body().focus());
    expect(document.activeElement).toBe(grid());
  });

  it('is a tab stop with a name while its columns do not fit, and keeps the focus the keyboard gave it', () => {
    mountTable();
    layOut({ width: 482, height: 72 }, { width: 327, height: 400 });

    expect(body().tabIndex).toBe(0);
    expect(body().getAttribute('aria-label')).toBe('Rows of the table');
    act(() => body().focus());
    expect(document.activeElement).toBe(body());
  });

  it('is a tab stop while its rows do not fit, and is none again once they do', () => {
    mountTable();
    layOut({ width: 450, height: 900 }, { width: 800, height: 400 });
    expect(body().tabIndex).toBe(0);

    layOut({ width: 450, height: 72 }, { width: 800, height: 400 });
    expect(body().tabIndex).toBe(-1);
  });
});

describe('the columns of a table on a phone', () => {
  it('are all of them in the regular display', () => {
    mountTable();
    expect(headings()).toEqual(['person', 'budget', 'used']);
    expect(cellsOfFirstRow()).toEqual(['c0-person', 'c0-budget', 'c0-used']);
  });

  it('leave out the ones a phone can do without in the compact display', () => {
    mountTable({ compact: true });
    expect(headings()).toEqual(['person', 'used']);
    expect(cellsOfFirstRow()).toEqual(['c0-person', 'c0-used']);
    expect(document.querySelector(`#${TABLE_ID} [role=grid]`)!.getAttribute('aria-colcount')).toBe('2');
  });

  it('freeze up to a column counting the columns left out', async () => {
    mountTable({ compact: true });
    const used = [...document.querySelectorAll<HTMLElement>(`#${TABLE_ID} [role=columnheader]`)][1];
    act(() => {
      used.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true, clientX: 10, clientY: 10 }));
    });
    const freeze = [...document.querySelectorAll<HTMLElement>('[role=menuitem]')]
      .find(item => item.textContent === 'Freeze up to here')!;
    act(() => freeze.click());
    await settle();

    expect(sent).toContainEqual({ command: 'setFrozenColumnCount', args: { count: 3 } });
  });
});
