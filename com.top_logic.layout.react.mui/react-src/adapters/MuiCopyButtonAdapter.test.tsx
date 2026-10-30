// The part CopyButton drawn with Material UI: a small MUI IconButton copying its text.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import type { CopyButtonPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiCopyButtonAdapter from './MuiCopyButtonAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

function draw(props: Partial<CopyButtonPartProps>) {
  render(<MuiRoot><MuiCopyButtonAdapter text="a" label="Kopieren" copiedLabel="Kopiert" {...props} /></MuiRoot>);
  return screen.getByRole('button', { name: props.label ?? 'Kopieren' });
}

/** Installs the given clipboard on the navigator, undefined for none. */
function installClipboard(clipboard: { writeText: (text: string) => Promise<void> } | undefined) {
  Object.defineProperty(navigator, 'clipboard', { value: clipboard, configurable: true });
}

afterEach(() => {
  cleanup();
  installClipboard(undefined);
  vi.useRealTimers();
});

describe('the part CopyButton as MUI IconButton', () => {
  it('copies its text, shows and announces the copy, and returns after a while', async () => {
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
    const writeText = vi.fn(() => Promise.resolve());
    installClipboard({ writeText });

    const button = draw({ text: 'a\nb', className: 'x', 'data-testid': 'c' });

    expect(button.classList).toContain('MuiIconButton-root');
    expect(screen.getByTestId('c').classList).toContain('x');
    expect(button.querySelector('.fa-copy')).not.toBeNull();

    await act(async () => {
      fireEvent.click(button);
    });

    expect(writeText).toHaveBeenCalledWith('a\nb');
    expect(button.querySelector('.fa-check')).not.toBeNull();
    expect(screen.getByRole('status').textContent).toBe('Kopiert');

    act(() => {
      vi.advanceTimersByTime(1500);
    });

    expect(button.querySelector('.fa-copy')).not.toBeNull();
    expect(screen.getByRole('status').textContent).toBe('');
  });

  it('stays as it is where the clipboard refuses the text', async () => {
    installClipboard({ writeText: vi.fn(() => Promise.reject(new Error('denied'))) });

    const button = draw({});
    await act(async () => {
      fireEvent.click(button);
    });

    expect(button.querySelector('.fa-copy')).not.toBeNull();
    expect(screen.getByRole('status').textContent).toBe('');
  });
});
