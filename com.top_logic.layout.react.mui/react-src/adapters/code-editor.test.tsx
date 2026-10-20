// The code editor (spec 2026-10-20 wave 5d §9): its root is a field of the design system (the frame draws border, hover
// and error), the text is code (`tl-type-code`), the root keeps the class that scopes the CodeMirror rules of the engine
// sheets. An error is announced at the textbox (`role="textbox"` of CodeMirror), which assistive technology reads; the
// frame shows it through the same attribute.

import { describe, it, expect, afterEach } from 'vitest';
import { cleanup, render } from '@testing-library/react';
import { React } from 'tl-react-bridge';
import CodeEditor from '../../../com.top_logic.layout.react.codeedit/react-src/controls/CodeEditor';
import TLCodeEditor from '../../../com.top_logic.layout.react.codeedit/react-src/controls/TLCodeEditor';
import { mountAdapter, settle } from './wire-test-support';

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

  it('is no field when it is read-only, and keeps the scope class and the code text', async () => {
    mountAdapter(TLCodeEditor, { value: 'a = 1', language: 'plain', editable: false });
    await settle();
    const classes = Array.from(frame().classList);
    expect(classes).toEqual(expect.arrayContaining(['tlCodeEditor', 'tlCodeEditorSurface', 'tl-type-code']));
    expect(classes).not.toContain('tl-field');
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
