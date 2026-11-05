// The code editor (spec 2026-10-20 wave 5d §9): its root is a field of the design system (the frame draws border, hover
// and error), the text is code (`tl-type-code`), the root keeps the class that scopes the CodeMirror rules of the engine
// sheets. An error is announced at the textbox (`role="textbox"` of CodeMirror), which assistive technology reads; the
// frame shows it through the same attribute.

import { describe, it, expect, afterEach, vi } from 'vitest';
import { act, cleanup, render } from '@testing-library/react';
import { React } from 'tl-react-bridge';
// The CodeMirror the editor runs on: the copy installed in the editor module, the one the editor imports.
import { EditorView } from '../../../com.top_logic.layout.react.codeedit/node_modules/@codemirror/view';
import CodeEditor from '../../../com.top_logic.layout.react.codeedit/react-src/controls/CodeEditor';
import TLCodeEditor from '../../../com.top_logic.layout.react.codeedit/react-src/controls/TLCodeEditor';
import { enqueueCommand } from '../../../com.top_logic.layout.react/react-src/bridge/command-channel';
import { CONTROL_ID, fakeDebounceTimers, mountAdapter, patchState, settle } from './wire-test-support';
import type { SentCommand } from './wire-test-support';

afterEach(() => {
  cleanup();
  document.body.replaceChildren();
});

/** The frame of the editor mounted last. */
function frame(): HTMLElement {
  const root = document.querySelector<HTMLElement>('.tlCodeEditorSurface');
  expect(root).not.toBeNull();
  return root as HTMLElement;
}

/** The textbox of CodeMirror in the editor mounted last. */
function textbox(): HTMLElement {
  const box = frame().querySelector<HTMLElement>('[role="textbox"]');
  expect(box, 'CodeMirror gives its content the role of a textbox').not.toBeNull();
  return box as HTMLElement;
}

describe('TLCodeEditor', () => {
  it('is a field with code text and keeps the classes that scope the CodeMirror rules', async () => {
    mountAdapter(TLCodeEditor, { value: 'a = 1', language: 'plain' });
    await settle();
    const classes = Array.from(frame().classList);
    expect(classes).toEqual(expect.arrayContaining(['tlCodeEditor', 'tlCodeEditorSurface', 'tl-field', 'tl-type-code']));
    expect(classes.filter(c => c.includes('--'))).toEqual([]);
  });

  it('keeps the field frame when it is read-only, with the scope class and the code text', async () => {
    mountAdapter(TLCodeEditor, { value: 'a = 1', language: 'plain', editable: false });
    await settle();
    const classes = Array.from(frame().classList);
    expect(classes).toEqual(expect.arrayContaining(['tlCodeEditor', 'tlCodeEditorSurface', 'tl-field', 'tl-type-code']));
  });

  it('announces an error at the textbox and shows it at the frame, without a modifier class', async () => {
    mountAdapter(TLCodeEditor, { value: 'a = 1', language: 'plain', hasError: true });
    await settle();
    expect(textbox().getAttribute('aria-invalid')).toBe('true');
    expect(frame().getAttribute('aria-invalid')).toBe('true');
    expect(frame().classList.contains('tlCodeEditor--error')).toBe(false);
  });

  it('has no aria-invalid without an error', async () => {
    mountAdapter(TLCodeEditor, { value: 'a = 1', language: 'plain' });
    await settle();
    expect(textbox().hasAttribute('aria-invalid')).toBe(false);
    expect(frame().hasAttribute('aria-invalid')).toBe(false);
  });
});

describe('CodeEditor', () => {
  it('hands an error that is reported or cleared after the mount to the textbox, keeping the id and label', () => {
    const attributes = { id: 'f1', 'aria-labelledby': 'l1' };
    const { rerender } = render(
      <CodeEditor controlId="c1" value="x" readOnly={false} contentAttributes={attributes} />);
    expect(textbox().hasAttribute('aria-invalid')).toBe(false);

    rerender(<CodeEditor controlId="c1" value="x" readOnly={false} contentAttributes={attributes} invalid />);
    expect(textbox().getAttribute('aria-invalid')).toBe('true');
    expect(frame().getAttribute('aria-invalid')).toBe('true');
    expect(textbox().id).toBe('f1');
    expect(textbox().getAttribute('aria-labelledby')).toBe('l1');

    rerender(<CodeEditor controlId="c1" value="x" readOnly={false} contentAttributes={attributes} />);
    expect(textbox().hasAttribute('aria-invalid')).toBe(false);
    expect(frame().hasAttribute('aria-invalid')).toBe(false);
  });
});

/** The command reporting the text of the editor. */
const VALUE_CHANGED = 'valueChanged';

/** The command the tests send as an action of the user, e.g. a keyboard shortcut. */
const ACTION = 'save';

/** The pause the editor waits out before it reports a text. */
const DELAY = 300;

/** The texts the editor reported, in order. */
function reported(sent: { mock: { calls: SentCommand[] } }): unknown[] {
  return sent.mock.calls.filter(([command]) => command === VALUE_CHANGED).map(([, args]) => args.value);
}

describe('TLCodeEditor and a value of the server', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  /** Mounts the editor with the given text, the cursor at its end, the debounce timers faked. */
  async function mountFocused(text: string) {
    fakeDebounceTimers();
    const sent = mountAdapter(TLCodeEditor, { value: text, language: 'plain' });
    await settle();
    const view = EditorView.findFromDOM(frame())!;
    view.focus();
    view.dispatch({ selection: { anchor: text.length } });
    return { sent, view };
  }

  /** Types the given text at the cursor, as the user does. */
  function type(view: EditorView, text: string) {
    act(() => {
      view.dispatch(view.state.replaceSelection(text), { userEvent: 'input.type' });
    });
  }

  /** Waits out the debounce pause of the editor. */
  async function pause() {
    act(() => { vi.advanceTimersByTime(DELAY); });
    await settle();
  }

  it('a value the server sets while the editor has the focus replaces the text', async () => {
    const { sent, view } = await mountFocused('a = 1');
    type(view, ';');
    await pause();
    expect(reported(sent)).toEqual(['a = 1;']);

    patchState({ value: '' });

    expect(view.state.doc.toString()).toBe('');
    expect(view.hasFocus).toBe(true);
  });

  it('the echo of the reported text leaves what was typed since', async () => {
    const { sent, view } = await mountFocused('a = 1');
    type(view, ';');
    await pause();
    type(view, ' b');

    patchState({ value: 'a = 1;' });
    await pause();

    expect(view.state.doc.toString()).toBe('a = 1; b');
    expect(reported(sent)).toEqual(['a = 1;', 'a = 1; b']);
  });

  it('a different value of the server wins over text not reported yet, the cursor stays', async () => {
    const { sent, view } = await mountFocused('a = 1');
    type(view, ';');

    patchState({ value: 'b = 1;' });
    await pause();

    expect(view.state.doc.toString()).toBe('b = 1;');
    expect(view.state.selection.main.head).toBe(6);
    expect(reported(sent)).toEqual([]);
  });

  it('an action command reports the text typed before it is sent', async () => {
    const { sent, view } = await mountFocused('a = 1');
    type(view, ';');

    await act(() => enqueueCommand('react-api/command', {
      controlId: CONTROL_ID, windowName: 'w', command: ACTION, arguments: {},
    }));

    expect(sent.mock.calls.map(([command]) => command)).toEqual([VALUE_CHANGED, ACTION]);
    expect(reported(sent)).toEqual(['a = 1;']);
  });
});
