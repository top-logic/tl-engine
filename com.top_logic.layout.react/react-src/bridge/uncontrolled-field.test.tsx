import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { CMD_VALUE_CHANGED, enqueueCommand } from './command-channel';
import { useUncontrolledFieldSync } from './uncontrolled-field';
import type { UncontrolledFieldSync } from './uncontrolled-field';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** The debounce delay of the editor under test. */
const DELAY = 300;

/** The command the tests send as an action of the user, e.g. a button. */
const CMD_ACTION = 'action';

/** The URL the tests send commands to. */
const COMMAND_URL = '/ctx/react-api/command';

/** The editor under test: its text, and the synchronization it reports through. */
const editor: { text: string; sync?: UncontrolledFieldSync } = { text: '' };

/** What the editor reported, and the values the synchronization showed in it. */
let sent: string[];
let applied: string[];

/** Reports a text as the field of the bridge does: the value follows it, the command is sent. */
let report: (text: string) => void;

/** An uncontrolled editor: it keeps its text itself and reports edits through the synchronization. */
const Editor: React.FC<{ value: string }> = ({ value }) => {
  editor.sync = useUncontrolledFieldSync({
    value,
    debounceMs: DELAY,
    send: (text: string) => report(text),
    apply: (text: string) => {
      applied.push(text);
      editor.text = text;
    },
  });
  return null;
};

let root: Root;

/** Renders the editor with the given value of the server. */
function render(value: string): void {
  act(() => root.render(<Editor value={value} />));
}

/** The user types the given text. */
function type(text: string): void {
  editor.text = text;
  editor.sync!.edited(() => editor.text);
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
});

beforeEach(() => {
  vi.useFakeTimers();
  sent = [];
  applied = [];
  editor.text = 'start';
  // The value following a reported text is rendered by a test where it matters.
  report = (text: string) => {
    sent.push(text);
  };
  root = createRoot(document.createElement('div'));
  render('start');
});

afterEach(() => {
  act(() => root.unmount());
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('useUncontrolledFieldSync', () => {
  it('reports a text after the debounce delay', () => {
    type('start typed');
    expect(sent).toEqual([]);

    act(() => vi.advanceTimersByTime(DELAY));

    expect(sent).toEqual(['start typed']);
  });

  it('shows a value of the server while there is no input waiting', () => {
    type('my comment');
    act(() => vi.advanceTimersByTime(DELAY));

    // The comment is stored and the field emptied, e.g. by a keyboard shortcut that keeps the focus.
    render('');

    expect(applied).toEqual(['']);
    expect(editor.text).toBe('');
  });

  it('ignores the echo of the reported text, keeping what was typed since', () => {
    type('abc');
    act(() => vi.advanceTimersByTime(DELAY));
    type('abcd');

    // The value follows the reported text while the user goes on typing.
    render('abc');
    act(() => vi.advanceTimersByTime(DELAY));

    expect(applied).toEqual([]);
    expect(editor.text).toBe('abcd');
    expect(sent).toEqual(['abc', 'abcd']);
  });

  it('lets a different value of the server win over input not reported yet', () => {
    type('typed');

    report = () => { throw new Error('must not report'); };
    render('from server');
    act(() => vi.advanceTimersByTime(DELAY));

    expect(applied).toEqual(['from server']);
    expect(editor.text).toBe('from server');
  });

  it('does not report a text equal to the one the server holds', () => {
    type('start');
    act(() => vi.advanceTimersByTime(DELAY));

    expect(sent).toEqual([]);
  });

  it('reports waiting input before an action command', async () => {
    const commands: string[] = [];
    vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
      commands.push(JSON.parse(String(init?.body)).command);
      return new Response('{"success":true}', { status: 200 });
    }));
    report = (text: string) => {
      sent.push(text);
      void enqueueCommand(COMMAND_URL, {
        controlId: 'c', windowName: 'w', command: CMD_VALUE_CHANGED, arguments: { value: text },
      });
    };
    type('typed');

    await act(() => enqueueCommand(COMMAND_URL, {
      controlId: 'c', windowName: 'w', command: CMD_ACTION, arguments: {},
    }));

    expect(sent).toEqual(['typed']);
    expect(commands).toEqual([CMD_VALUE_CHANGED, CMD_ACTION]);

    // The input reported is reported once.
    act(() => vi.advanceTimersByTime(DELAY));
    expect(sent).toEqual(['typed']);
  });

  it('reports waiting input when the editor is unmounted', () => {
    type('last words');

    act(() => root.unmount());
    root = createRoot(document.createElement('div'));

    expect(sent).toEqual(['last words']);
  });
});
