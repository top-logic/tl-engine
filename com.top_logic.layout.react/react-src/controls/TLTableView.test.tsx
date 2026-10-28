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

/** A table of three columns of 150 px and two rows. */
function mountTable(): void {
  vi.stubGlobal('ResizeObserver', ResizeObserverStub);
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  const element = document.createElement('div');
  element.id = TABLE_ID;
  document.body.appendChild(element);
  const columns = ['person', 'budget', 'used'].map(name => ({ name, label: name, width: 150, sortable: false }));
  const rows = [0, 1].map(index => ({ id: 'r' + index, index, cells: Object.fromEntries(columns.map(column => [column.name, cell(`c${index}-${column.name}`)])) }));
  act(() => mount(TABLE_ID, 'TLTableView', {
    columns, rows, totalRowCount: rows.length, scrollStart: 0, selectionMode: 'single', selectedRows: [],
    cursorIndex: -1,
  }, 'w', '/ctx'));
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
