// The popover of tl-react-bridge (TLPopover) with its trigger drawn by the MUI adapter of the part
// MenuTrigger: the open state is the server's, the component asks for changes by the commands open
// and close, moves the focus, and places the surface at the trigger.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart, ANCHORED_OVERLAY_ATTR } from 'tl-react-bridge';
import TLPopover from '../../../com.top_logic.layout.react/react-src/controls/TLPopover';
import MuiMenuTriggerAdapter from './MuiMenuTriggerAdapter';
import { CONTROL_ID, childControl, fakeDebounceTimers, mountAdapter, patchState, settle } from './wire-test-support';

replacePart('MenuTrigger', MuiMenuTriggerAdapter);

/** The command asking the server to open the popover. */
const CMD_OPEN = 'open';

/** The command asking the server to close the popover. */
const CMD_CLOSE = 'close';

/** The state of a popover whose trigger shows the text "Details". */
function popoverState(extra: Record<string, unknown> = {}) {
  return { open: false, mode: 'click', placement: 'bottom-start', trigger: childControl('t1', 'Details'), ...extra };
}

/** The content of the surface: a field labelled "Name". */
const CONTENT = childControl('c2', 'Name', true);

function trigger(): HTMLElement {
  return document.getElementById(CONTROL_ID) as HTMLElement;
}

function surface(): HTMLElement | null {
  return document.body.querySelector('[role="dialog"]');
}

/** Runs the timers of the hover delays and waits for the commands they send. */
async function elapse(ms: number) {
  act(() => {
    vi.advanceTimersByTime(ms);
  });
  await settle();
}

/**
 * Asserts that the focus is in the surface. Without layout (jsdom), no element of the content counts
 * as visible, so the focus lands on the surface itself rather than on its first field.
 */
function expectFocusInSurface() {
  const dialog = surface();
  expect(dialog).not.toBeNull();
  expect(dialog?.contains(document.activeElement)).toBe(true);
}

afterEach(() => {
  cleanup();
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('a popover opened by a click', () => {
  it('draws the trigger announcing a closed dialog, and no surface', () => {
    mountAdapter(TLPopover, popoverState());

    expect(trigger().getAttribute('aria-haspopup')).toBe('dialog');
    expect(trigger().getAttribute('aria-expanded')).toBe('false');
    expect(trigger().querySelector('#t1')?.textContent).toBe('Details');
    expect(surface()).toBeNull();
  });

  it('draws a trigger of label and icon, naming trigger and surface by the label', () => {
    mountAdapter(TLPopover, { open: false, mode: 'click', label: 'Details', icon: 'css:bi bi-info' });

    const button = screen.getByRole('button', { name: 'Details' });
    expect(button).toBe(trigger());
    expect(button.querySelector('i.bi-info')).not.toBeNull();
    expect(button.querySelector('button')).toBeNull();

    patchState({ open: true, content: CONTENT });
    expect(screen.getByRole('dialog', { name: 'Details' })).toBe(surface());
  });

  it('names trigger content by the label', () => {
    mountAdapter(TLPopover, popoverState({ label: 'Show details' }));

    expect(screen.getByRole('button', { name: 'Show details' })).toBe(trigger());
    expect(trigger().querySelector('#t1')?.textContent).toBe('Details');
  });

  it('asks to open on a click on the trigger', async () => {
    const sent = mountAdapter(TLPopover, popoverState());

    await userEvent.click(trigger());
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_OPEN, {});
    expect(sent).not.toHaveBeenCalledWith(CMD_CLOSE, expect.anything());
  });

  it('shows the content on a surface in the body, labelled by the trigger, with the focus in it', () => {
    mountAdapter(TLPopover, popoverState());
    patchState({ open: true, content: CONTENT });

    const dialog = surface() as HTMLElement;
    expect(dialog).not.toBeNull();
    expect(dialog.parentElement).toBe(document.body);
    expect(dialog.classList).toContain('tl-popover');
    expect(dialog.classList).toContain('tl-popover--content');
    expect(dialog.hasAttribute(ANCHORED_OVERLAY_ATTR)).toBe(true);
    expect(dialog.getAttribute('aria-labelledby')).toBe(CONTROL_ID);
    expect(trigger().getAttribute('aria-expanded')).toBe('true');
    expect(trigger().getAttribute('aria-controls')).toBe(dialog.id);
    expectFocusInSurface();
  });

  it('gives the focus back to the trigger on closing', () => {
    mountAdapter(TLPopover, popoverState());
    act(() => trigger().focus());
    patchState({ open: true, content: CONTENT });
    expectFocusInSurface();

    patchState({ open: false, content: null });

    expect(surface()).toBeNull();
    expect(document.activeElement).toBe(trigger());
  });

  it('leaves the focus where the user put it while the popover was closing', () => {
    const outside = document.createElement('input');
    document.body.appendChild(outside);
    try {
      mountAdapter(TLPopover, popoverState());
      act(() => trigger().focus());
      patchState({ open: true, content: CONTENT });

      act(() => outside.focus());
      patchState({ open: false, content: null });

      expect(document.activeElement).toBe(outside);
    } finally {
      outside.remove();
    }
  });

  it('asks to close on a further click on the trigger', async () => {
    const sent = mountAdapter(TLPopover, popoverState({ open: true, content: CONTENT }));

    await userEvent.click(trigger());
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_CLOSE, {});
    expect(sent).not.toHaveBeenCalledWith(CMD_OPEN, expect.anything());
  });

  it('asks to close on Escape', async () => {
    const sent = mountAdapter(TLPopover, popoverState({ open: true, content: CONTENT }));

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_CLOSE, {});
  });

  it('asks to close on a press outside, not on a press in the surface', async () => {
    const sent = mountAdapter(TLPopover, popoverState({ open: true, content: CONTENT }));

    fireEvent.pointerDown(screen.getByLabelText('Name'));
    await settle();
    expect(sent).not.toHaveBeenCalled();

    fireEvent.pointerDown(document.body);
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_CLOSE, {});
  });
});

describe('a popover opened by hovering', () => {
  it('asks to open after the pointer has rested on the trigger, and to close after it has left', async () => {
    fakeDebounceTimers();
    const sent = mountAdapter(TLPopover, popoverState({ mode: 'hover' }));

    fireEvent.mouseEnter(trigger());
    await elapse(100);
    expect(sent).not.toHaveBeenCalled();
    await elapse(400);
    expect(sent).toHaveBeenCalledWith(CMD_OPEN, {});

    patchState({ open: true, content: CONTENT });
    expect(surface()).not.toBeNull();
    expect(document.activeElement).toBe(document.body);

    fireEvent.mouseLeave(trigger(), { clientX: 0, clientY: 0 });
    await elapse(1000);
    expect(sent).toHaveBeenCalledWith(CMD_CLOSE, {});
  });

  it('does not open when the pointer leaves the trigger before the delay', async () => {
    fakeDebounceTimers();
    const sent = mountAdapter(TLPopover, popoverState({ mode: 'hover' }));

    fireEvent.mouseEnter(trigger());
    await elapse(100);
    fireEvent.mouseLeave(trigger(), { clientX: 0, clientY: 0 });
    await elapse(1000);

    expect(sent).not.toHaveBeenCalledWith(CMD_OPEN, expect.anything());
  });

  it('asks to open when the keyboard brings the focus to the trigger, and moves the focus in', async () => {
    const sent = mountAdapter(TLPopover, popoverState({ mode: 'hover' }));

    await userEvent.tab();
    expect(document.activeElement).toBe(trigger());
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_OPEN, {});

    patchState({ open: true, content: CONTENT });
    expectFocusInSurface();
  });

  it('does not reopen when closing gives the focus back to the trigger', async () => {
    const sent = mountAdapter(TLPopover, popoverState({ mode: 'hover' }));
    await userEvent.tab();
    await settle();
    patchState({ open: true, content: CONTENT });
    expectFocusInSurface();
    sent.mockClear();

    patchState({ open: false, content: null });
    await settle();

    expect(document.activeElement).toBe(trigger());
    expect(sent).not.toHaveBeenCalled();
  });

  it('asks to close when the focus leaves trigger and surface', async () => {
    const after = document.createElement('button');
    document.body.appendChild(after);
    try {
      const sent = mountAdapter(TLPopover, popoverState({ mode: 'hover', open: true, content: CONTENT }));
      expectFocusInSurface();

      act(() => after.focus());
      await settle();

      expect(sent).toHaveBeenCalledWith(CMD_CLOSE, {});
    } finally {
      after.remove();
    }
  });

  it('opens on a click on the trigger and does not close on a further one', async () => {
    const sent = mountAdapter(TLPopover, popoverState({ mode: 'hover', open: true, content: CONTENT }));

    await userEvent.click(trigger());
    await settle();

    expect(sent).not.toHaveBeenCalledWith(CMD_CLOSE, expect.anything());
  });
});
