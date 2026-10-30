// The behavior every drawing of the part CopyButton shares, run against a drawing: the copy through
// the clipboard API in a secure context, through the copy command of the document otherwise or where
// the API refuses, and the failure where both fail.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { act, cleanup, fireEvent, screen } from '@testing-library/react';
import type { CopyButtonPartProps } from 'tl-react-bridge';

/** The text copied: several lines, one indented by a tab. */
const TEXT = 'java.lang.RuntimeException: x\n\tat A.b(A.java:1)';

/** The labels of the button. */
const LABELS = { label: 'Kopieren', copiedLabel: 'Kopiert', failedLabel: 'Kopieren nicht möglich' } as const;

/** What the page offers for copying. */
interface Environment {
  /** Whether the page is a secure context. */
  secure: boolean;

  /** The writeText of navigator.clipboard; absent: no clipboard API. */
  writeText?: (text: string) => Promise<void>;

  /** The copy command of the document; absent: none. */
  execCommand?: (command: string) => boolean;
}

/** Installs the given environment on window, navigator and document. */
function install({ secure, writeText, execCommand }: Environment) {
  Object.defineProperty(window, 'isSecureContext', { value: secure, configurable: true });
  Object.defineProperty(navigator, 'clipboard', {
    value: writeText ? { writeText } : undefined, configurable: true,
  });
  Object.defineProperty(document, 'execCommand', { value: execCommand, configurable: true });
}

/** A copy command recording the text selected in the focused text area when it runs. */
function recordingCommand(result: boolean) {
  const selected: string[] = [];
  const command = vi.fn((name: string) => {
    const area = document.activeElement as HTMLTextAreaElement | null;
    if (name === 'copy' && area instanceof HTMLTextAreaElement) {
      selected.push(area.value.substring(area.selectionStart, area.selectionEnd));
    }
    return result;
  });
  return { command, selected };
}

/** The icon the button shows: the Font Awesome glyph class. */
function glyph(button: HTMLElement): string | undefined {
  return ['fa-copy', 'fa-check', 'fa-triangle-exclamation'].find(name => button.querySelector('.' + name));
}

async function press(button: HTMLElement) {
  await act(async () => {
    fireEvent.click(button);
  });
}

/**
 * Describes the shared behavior of a drawing of the part `CopyButton`.
 *
 * @param name the name of the drawing
 * @param draw renders the drawing with the given props
 */
export function describeCopyButton(name: string, draw: (props: CopyButtonPartProps) => void | Promise<void>) {
  describe(`the part CopyButton as ${name}`, () => {
    afterEach(() => {
      cleanup();
      install({ secure: true });
      vi.useRealTimers();
    });

    async function button() {
      await draw({ text: TEXT, ...LABELS });
      return screen.getByRole('button', { name: LABELS.label });
    }

    it('copies through the clipboard API in a secure context, shows and announces it, and returns', async () => {
      vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
      const writeText = vi.fn(() => Promise.resolve());
      const { command } = recordingCommand(true);
      install({ secure: true, writeText, execCommand: command });
      const pressed = await button();
      expect(glyph(pressed)).toBe('fa-copy');
      expect(screen.getByRole('status').textContent).toBe('');

      await press(pressed);

      expect(writeText).toHaveBeenCalledWith(TEXT);
      expect(command).not.toHaveBeenCalled();
      expect(glyph(pressed)).toBe('fa-check');
      expect(screen.getByRole('status').textContent).toBe(LABELS.copiedLabel);

      act(() => {
        vi.advanceTimersByTime(1500);
      });

      expect(glyph(pressed)).toBe('fa-copy');
      expect(screen.getByRole('status').textContent).toBe('');
    });

    it('copies through the copy command where the page is no secure context, restoring the focus', async () => {
      const writeText = vi.fn(() => Promise.resolve());
      const { command, selected } = recordingCommand(true);
      install({ secure: false, writeText, execCommand: command });
      const pressed = await button();
      pressed.focus();

      await press(pressed);

      expect(writeText).not.toHaveBeenCalled();
      expect(command).toHaveBeenCalledWith('copy');
      expect(selected).toEqual([TEXT]);
      expect(document.querySelector('textarea')).toBeNull();
      expect(document.activeElement).toBe(pressed);
      expect(glyph(pressed)).toBe('fa-check');
      expect(screen.getByRole('status').textContent).toBe(LABELS.copiedLabel);
    });

    it('copies through the copy command where the clipboard API refuses the text', async () => {
      const writeText = vi.fn(() => Promise.reject(new Error('denied')));
      const { command, selected } = recordingCommand(true);
      install({ secure: true, writeText, execCommand: command });
      const pressed = await button();

      await press(pressed);

      expect(writeText).toHaveBeenCalledWith(TEXT);
      expect(selected).toEqual([TEXT]);
      expect(glyph(pressed)).toBe('fa-check');
    });

    it('shows and announces the failure where nothing can copy, and returns', async () => {
      vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
      const { command } = recordingCommand(false);
      install({ secure: false, execCommand: command });
      const pressed = await button();

      await press(pressed);

      expect(command).toHaveBeenCalledWith('copy');
      expect(glyph(pressed)).toBe('fa-triangle-exclamation');
      expect(screen.getByRole('button', { name: LABELS.failedLabel })).toBe(pressed);
      expect(screen.getByRole('status').textContent).toBe(LABELS.failedLabel);

      act(() => {
        vi.advanceTimersByTime(1500);
      });

      expect(glyph(pressed)).toBe('fa-copy');
      expect(screen.getByRole('button', { name: LABELS.label })).toBe(pressed);
      expect(screen.getByRole('status').textContent).toBe('');
    });

    it('shows the failure where the document has no copy command either', async () => {
      install({ secure: false });
      const pressed = await button();

      await press(pressed);

      expect(glyph(pressed)).toBe('fa-triangle-exclamation');
    });
  });
}
