import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

/** The options of every command enqueued to the command channel, by command name. */
const enqueued = vi.hoisted(() => [] as { command: string; options: unknown }[]);

vi.mock('../bridge/command-channel', async (importOriginal) => {
  const original = await importOriginal<typeof import('../bridge/command-channel')>();
  return {
    ...original,
    enqueueCommand: (...args: Parameters<typeof original.enqueueCommand>) => {
      enqueued.push({ command: args[1].command, options: args[2] });
      return original.enqueueCommand(...args);
    },
  };
});

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { React, register, registerPart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLTableView from './TLTableView';
import ButtonPart from './button/ButtonPart';
import DisclosureTogglePart from './button/DisclosureTogglePart';
import CheckboxPart from './form/CheckboxPart';
import TextInputPart from './form/TextInputPart';
import ChipPart from './chip/ChipPart';
import SkeletonPart from './skeleton/SkeletonPart';
import MenuTriggerPart from './button/MenuTriggerPart';
import BadgePart from './chip/BadgePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** A command the table sent: its name and its arguments. */
type Sent = { command: string; args: Record<string, unknown> };

const TABLE_ID = 'table';

/** The command selecting a row (TableViewControl.CMD_SELECT). */
const CMD_SELECT = 'select';

/** The argument of a {@link CMD_SELECT} naming the row. */
const ARG_ROW_ID = 'rowId';

/** The argument of a {@link CMD_SELECT} toggling the row into the selection. */
const ARG_CTRL_KEY = 'ctrlKey';

/** The argument of a {@link CMD_SELECT} extending the selection up to the row. */
const ARG_SHIFT_KEY = 'shiftKey';

/** The state key switching on the selection of a row by the focus of a control in its cells. */
const SELECT_ON_EDITOR_FOCUS = 'selectOnEditorFocus';

/** The column holding an editor in every row. */
const COL_NAME = 'name';

/** The column displaying text in every row. */
const COL_INFO = 'info';

/** The stand-in for an editor in a cell. */
const EditorCell: React.FC<TLCellProps> = ({ controlId }) => <input id={controlId} />;

/** The stand-in for a value displayed in a cell. */
const TextCell: React.FC<TLCellProps> = ({ controlId }) => <span id={controlId} tabIndex={-1}>text</span>;

function row(index: number): Record<string, unknown> {
  return {
    id: 'r' + index,
    index,
    cells: {
      [COL_NAME]: { controlId: editorId(index), module: 'TableTestEditor', state: {} },
      [COL_INFO]: { controlId: textId(index), module: 'TableTestText', state: {} },
    },
  };
}

function editorId(index: number): string {
  return 'editor-' + index;
}

function textId(index: number): string {
  return 'text-' + index;
}

let sent: Sent[] = [];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  // jsdom lays nothing out.
  Element.prototype.scrollIntoView = () => {};
  registerPart('Button', ButtonPart);
  registerPart('DisclosureToggle', DisclosureTogglePart);
  registerPart('Checkbox', CheckboxPart);
  registerPart('TextInput', TextInputPart);
  registerPart('Chip', ChipPart);
  registerPart('Skeleton', SkeletonPart);
  // The gear of the view is on every table.
  registerPart('MenuTrigger', MenuTriggerPart);
  registerPart('Badge', BadgePart);
  register('TableTestEditor', EditorCell);
  register('TableTestText', TextCell);
  register('TLTableView', TLTableView);
});

afterEach(async () => {
  act(() => unmount(TABLE_ID));
  findMountPoint(TABLE_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts a table of three rows with the given state, and records the commands it sends. */
function mountTable(state: Record<string, unknown>): void {
  sent = [];
  enqueued.length = 0;
  vi.stubGlobal('ResizeObserver', class {
    observe(): void {}
    unobserve(): void {}
    disconnect(): void {}
  });
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === TABLE_ID) {
      sent.push({ command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, TABLE_ID);
  document.body.appendChild(element);
  act(() => mount(TABLE_ID, 'TLTableView', {
    columns: [
      { name: COL_NAME, label: 'Name', width: 100, sortable: false },
      { name: COL_INFO, label: 'Info', width: 100, sortable: false },
    ],
    rows: [row(0), row(1), row(2)],
    totalRowCount: 3,
    viewportStart: 0,
    selectionMode: 'single',
    ...state,
  }, 'w', '/ctx'));
}

/** Waits until the command channel has handed all queued commands to fetch. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** Moves the focus to the element of the given ID, as a click or Tab does. */
function focus(id: string): void {
  act(() => document.getElementById(id)!.focus());
}

/** The selections the table sent. */
function selections(): Sent[] {
  return sent.filter(command => command.command === CMD_SELECT);
}

describe('a table whose rows are all edited', () => {
  it('selects the row of an editor taking the focus', async () => {
    mountTable({ [SELECT_ON_EDITOR_FOCUS]: true, selectedRows: [0] });
    focus(editorId(2));
    await settle();

    expect(selections()).toEqual([{ command: CMD_SELECT, args: { [ARG_ROW_ID]: 'r2', [ARG_CTRL_KEY]: false, [ARG_SHIFT_KEY]: false } }]);
  });

  it('selects the row of an editor clicked with Ctrl only by the focus', async () => {
    mountTable({ [SELECT_ON_EDITOR_FOCUS]: true, selectionMode: 'multi', selectedRows: [0] });
    const editor = document.getElementById(editorId(2))!;
    focus(editorId(2));
    act(() => {
      editor.dispatchEvent(new MouseEvent('click', { bubbles: true, ctrlKey: true }));
    });
    await settle();

    expect(selections()).toEqual([{ command: CMD_SELECT, args: { [ARG_ROW_ID]: 'r2', [ARG_CTRL_KEY]: false, [ARG_SHIFT_KEY]: false } }]);
  });

  it('sends nothing for the focus of an editor in the selected row', async () => {
    mountTable({ [SELECT_ON_EDITOR_FOCUS]: true, selectedRows: [1] });
    focus(editorId(1));
    await settle();

    expect(selections()).toEqual([]);
  });

  it('sends nothing for the focus of a displayed value', async () => {
    mountTable({ [SELECT_ON_EDITOR_FOCUS]: true, selectedRows: [0] });
    focus(textId(2));
    await settle();

    expect(selections()).toEqual([]);
  });
});

describe('a table whose rows are not all edited', () => {
  it('leaves the selection alone when an editor takes the focus', async () => {
    mountTable({ selectedRows: [0] });
    focus(editorId(2));
    await settle();

    expect(selections()).toEqual([]);
  });
});

describe('the heading of a column that requires a value', () => {
  const REQUIRED = '.tl-table__required';

  it('shows the required mark, hidden from assistive technology, in a sortable and in a plain heading', () => {
    mountTable({
      columns: [
        { name: COL_NAME, label: 'Name', width: 100, sortable: true, required: true },
        { name: COL_INFO, label: 'Info', width: 100, sortable: false, required: true },
      ],
    });

    const marks = document.querySelectorAll(REQUIRED);
    expect(marks).toHaveLength(2);
    marks.forEach(mark => expect(mark.getAttribute('aria-hidden')).toBe('true'));
    expect(document.querySelector('.tl-table__sort ' + REQUIRED)).not.toBeNull();
  });

  it('shows no mark for a column that does not require a value', () => {
    mountTable({});

    expect(document.querySelector(REQUIRED)).toBeNull();
  });
});

describe('a table whose rows in the visible window are not all sent', () => {
  it('draws the missing rows as text skeletons and marks the grid busy', async () => {
    // jsdom lays nothing out: the body shows five rows of the fallback row height (36px).
    const clientHeight = vi.spyOn(Element.prototype, 'clientHeight', 'get')
      .mockImplementation(function (this: Element) { return this.classList.contains('tl-table__body') ? 5 * 36 : 0; });
    try {
      mountTable({ totalRowCount: 10 });
      await settle();

      const placeholders = Array.from(document.querySelectorAll('.tl-table__placeholder'));
      expect(placeholders).toHaveLength(2);
      for (const placeholder of placeholders) {
        expect(placeholder.getAttribute('aria-hidden')).toBe('true');
        const skeleton = placeholder.firstElementChild!;
        expect(skeleton.className).toBe('tl-skeleton tl-skeleton--text');
        expect(skeleton.getAttribute('aria-hidden')).toBe('true');
      }
      expect(document.querySelector('.tl-table__grid')!.getAttribute('aria-busy')).toBe('true');
    } finally {
      clientHeight.mockRestore();
    }
  });

  it('does not mark the grid busy when all rows of the window are sent', async () => {
    mountTable({});
    await settle();

    expect(document.querySelectorAll('.tl-table__placeholder')).toHaveLength(0);
    expect(document.querySelector('.tl-table__grid')!.hasAttribute('aria-busy')).toBe(false);
  });
});

/** The command reporting the rows the table shows (TableViewControl.CMD_SCROLL). */
const CMD_SCROLL = 'scroll';

/** The delay after which the table reports the rows a scroll brought into view. */
const SCROLL_REPORT_DELAY = 80;

/** Gives the body of the table the height of two rows and scrolls it, as the user does. */
function scrollBody(): void {
  const body = document.querySelector<HTMLElement>(`#${TABLE_ID} .tl-table__body`)!;
  Object.defineProperty(body, 'clientHeight', { configurable: true, get: () => 72 });
  act(() => {
    body.dispatchEvent(new Event('scroll'));
  });
}

/** The scroll commands the table sent. */
function scrolls(): Sent[] {
  return sent.filter(command => command.command === CMD_SCROLL);
}

describe('the viewport report of a table', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('reports the rows a scroll brought into view in the background, ignoring a disposed table', async () => {
    mountTable({});
    await settle();
    vi.useFakeTimers();
    scrollBody();
    act(() => {
      vi.advanceTimersByTime(SCROLL_REPORT_DELAY);
    });
    vi.useRealTimers();
    await settle();

    expect(scrolls()).toEqual([{ command: CMD_SCROLL, args: { start: 0, count: 2 } }]);
    expect(enqueued.filter(entry => entry.command === CMD_SCROLL))
      .toEqual([{ command: CMD_SCROLL, options: { background: true, ignoreStaleUi: true } }]);
  });

  it('reports nothing when it is removed before the report is due', async () => {
    mountTable({});
    await settle();
    vi.useFakeTimers();
    scrollBody();
    act(() => unmount(TABLE_ID));

    expect(vi.getTimerCount()).toBe(0);
    act(() => {
      vi.advanceTimersByTime(SCROLL_REPORT_DELAY);
    });
    vi.useRealTimers();
    await settle();

    expect(scrolls()).toEqual([]);
  });
});

/** State key of the grouping: the names of the grouping columns, outermost group level first. */
const STATE_GROUPING = 'grouping';

describe('the marks in the header of a table', () => {
  /** The header cell of the given column. */
  function header(column: string): HTMLElement {
    return document.querySelector<HTMLElement>(`.tl-table__header-cell[data-col="${column}"]`)!;
  }

  /** Mounts the table sorted by the given priorities of its two columns (0: unsorted) and grouped as given. */
  function mountMarked(namePriority: number, infoPriority: number, grouping: string[]): void {
    const sort = (priority: number) => priority > 0 ? { sortDirection: 'asc', sortPriority: priority } : {};
    mountTable({
      columns: [
        { name: COL_NAME, label: 'Name', width: 100, sortable: true, ...sort(namePriority) },
        { name: COL_INFO, label: 'Info', width: 100, sortable: true, ...sort(infoPriority) },
      ],
      [STATE_GROUPING]: grouping,
    });
  }

  it('numbers the group level of each grouping column while the rows are grouped by several', () => {
    mountMarked(0, 0, [COL_INFO, COL_NAME]);

    // The badge stands inside the mark, so that the mark's tooltip covers it.
    const outer = header(COL_INFO).querySelector('.tl-table__mark > .tl-badge')!;
    expect(outer.textContent).toBe('1');
    expect(outer.getAttribute('aria-hidden')).toBe('true');
    const mark = header(COL_NAME).querySelector('.tl-table__mark')!;
    expect(mark.querySelector(':scope > .tl-badge')!.textContent).toBe('2');
    expect(header(COL_NAME).querySelectorAll('.tl-badge')).toHaveLength(1);
    expect(mark.getAttribute('data-tooltip')).toBe('text:The rows are grouped by this column, level 2');
  });

  it('shows no group level while the rows are grouped by a single column', () => {
    mountMarked(0, 0, [COL_NAME]);

    expect(header(COL_NAME).querySelector('.tl-table__mark')).not.toBeNull();
    expect(header(COL_NAME).querySelector('.tl-badge')).toBeNull();
    expect(header(COL_NAME).querySelector('.tl-table__mark')!.getAttribute('data-tooltip'))
      .toBe('text:The rows are grouped by this column');
  });

  it('draws the sort priority through the part Badge, read with the label', () => {
    mountMarked(2, 1, []);

    const badge = header(COL_NAME).querySelector('.tl-table__sort .tl-badge')!;
    expect(badge.textContent).toBe('2');
    expect(badge.getAttribute('role')).toBe('img');
    expect(badge.getAttribute('aria-label')).toBe('Sort priority: 2');
    expect(header(COL_INFO).querySelector('.tl-table__sort .tl-badge')!.textContent).toBe('1');
  });

  it('shows no sort priority while the table is sorted by a single column', () => {
    mountMarked(1, 0, []);

    expect(document.querySelector('.tl-table__header-cell .tl-badge')).toBeNull();
  });
});
