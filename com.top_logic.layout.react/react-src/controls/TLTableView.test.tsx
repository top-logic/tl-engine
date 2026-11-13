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
import { React, register, registerPart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLTableView from './TLTableView';
import ButtonPart from './button/ButtonPart';
import DisclosureTogglePart from './button/DisclosureTogglePart';
import CheckboxPart from './form/CheckboxPart';
import TextInputPart from './form/TextInputPart';
import ChipPart from './chip/ChipPart';

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
  register('TableTestEditor', EditorCell);
  register('TableTestText', TextCell);
  register('TLTableView', TLTableView);
});

afterEach(async () => {
  act(() => unmount(TABLE_ID));
  document.getElementById(TABLE_ID)?.remove();
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
  element.id = TABLE_ID;
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
