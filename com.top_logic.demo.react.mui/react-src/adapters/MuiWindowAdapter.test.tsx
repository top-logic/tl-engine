// The wire contract of TLWindow, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { screen, cleanup, act, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { WindowStateJson } from 'tl-react-bridge';
import MuiWindowAdapter from './MuiWindowAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';

/** Command closing the window. */
const CMD_CLOSE = 'close';

/** Command reporting the size the user gave the window. */
const CMD_RESIZE = 'resize';

/** Command making the server forget that size. */
const CMD_RESET_SIZE = 'resetSize';

/** The labels of the title-bar buttons: the keys, as the test stub translates each key to itself. */
const LABEL_CLOSE = 'js.window.close';
const LABEL_MAXIMIZE = 'js.window.maximize';
const LABEL_RESTORE = 'js.window.restore';

/** The ID of the pointer driving a gesture. */
const POINTER = 1;

function mountWindow(state: Partial<WindowStateJson>, outer?: () => void) {
  return mountAdapter(MuiWindowAdapter, {
    title: 'Bearbeiten',
    child: childControl('body', 'Name', true),
    ...state,
  }, outer ? { outer: { gesture: 'ESCAPE', onGesture: outer } } : {});
}

function windowElement(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

function pressEscape() {
  (document.activeElement ?? document.body).dispatchEvent(
    new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true }));
}

/**
 * Dispatches a pointer event of the main button at the given point (jsdom has no PointerEvent: a
 * mouse event of the pointer type, carrying the pointer ID).
 */
function pointer(target: Element, type: string, x: number, y: number) {
  const event = new MouseEvent(type, { bubbles: true, cancelable: true, button: 0, clientX: x, clientY: y });
  Object.defineProperty(event, 'pointerId', { value: POINTER });
  act(() => {
    target.dispatchEvent(event);
  });
}

/** Drags the given handle by the given distance with the main button. */
function drag(handle: Element, dx: number, dy: number) {
  pointer(handle, 'pointerdown', 0, 0);
  pointer(handle, 'pointermove', dx, dy);
  pointer(handle, 'pointerup', dx, dy);
}

beforeEach(() => {
  // jsdom implements no pointer capture, which a drag gesture of the bridge relies on.
  HTMLElement.prototype.setPointerCapture = () => {};
  HTMLElement.prototype.releasePointerCapture = () => {};
  HTMLElement.prototype.hasPointerCapture = () => false;
});

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

describe('TLWindow as MUI dialog surface', () => {
  it('renders a Paper named by its title, with the content in DialogContent and the footer in DialogActions', () => {
    mountWindow({
      cssClass: 'my-window',
      toolbar: childControl('tools', 'Werkzeuge'),
      footer: childControl('footer', 'Speichern'),
    });

    const dialog = screen.getByRole('dialog', { name: 'Bearbeiten' });
    expect(dialog.id).toBe(CONTROL_ID);
    expect(dialog.getAttribute('aria-modal')).toBe('true');
    expect(dialog.classList).toContain('MuiPaper-root');
    expect(dialog.classList).toContain('my-window');
    expect(dialog.querySelector('.MuiDialogTitle-root #tools')?.textContent).toBe('Werkzeuge');
    expect(dialog.querySelector('.MuiDialogContent-root #body')?.textContent).toBe('Name');
    expect(dialog.querySelector('.MuiDialogActions-root #footer')?.textContent).toBe('Speichern');
  });

  it('takes the configured width, and the remembered size while it fits', () => {
    mountWindow({ width: '40rem', customWidth: 500, customHeight: 300 });

    expect(windowElement().style.width).toBe('500px');
    expect(windowElement().style.minHeight).toBe('300px');
    cleanup();

    mountWindow({ width: '40rem', customWidth: 5000, customHeight: 300 });
    expect(windowElement().style.width).toBe('40rem');
  });

  it('moves the focus to the first field of its content', () => {
    // The focus trap takes only a field that is rendered, which jsdom, laying out nothing, denies.
    vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
    mountWindow({});

    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Name' }));
  });

  it('sends close for its close button and for Escape', async () => {
    const sent = mountWindow({});

    await userEvent.click(screen.getByRole('button', { name: LABEL_CLOSE }));
    pressEscape();
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLOSE, {}], [CMD_CLOSE, {}]]);
  });

  it('cannot be closed while not closable, and keeps Escape from the page behind', async () => {
    const outer = vi.fn();
    const sent = mountWindow({ closable: false }, outer);

    const close = screen.getByRole('button', { name: LABEL_CLOSE });
    expect(close).toHaveProperty('disabled', true);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(close);
    pressEscape();
    await settle();

    expect(sent).not.toHaveBeenCalled();
    expect(outer).not.toHaveBeenCalled();
  });

  it('offers neither maximizing nor resizing unless resizable', () => {
    mountWindow({});

    expect(screen.queryByRole('button', { name: LABEL_MAXIMIZE })).toBeNull();
    expect(windowElement().querySelector('[data-resize]')).toBeNull();
  });

  it('maximizes a resizable window and restores it', async () => {
    const sent = mountWindow({ resizable: true, width: '40rem' });

    await userEvent.click(screen.getByRole('button', { name: LABEL_MAXIMIZE }));
    expect(windowElement().style.width).toBe('100vw');
    expect(windowElement().querySelector('[data-resize]')).toBeNull();

    await userEvent.click(screen.getByRole('button', { name: LABEL_RESTORE }));
    expect(windowElement().style.width).toBe('40rem');
    await settle();

    // Maximizing is the client's alone.
    expect(sent).not.toHaveBeenCalled();
  });

  it('sends resize with the size the user dragged the window to', async () => {
    const sent = mountWindow({ resizable: true });

    // A centered window grows to both sides: twice the distance moved.
    drag(windowElement().querySelector('[data-resize="se"]')!, 150, 120);
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_RESIZE, { width: 300, height: 240 }]]);
    expect(windowElement().style.width).toBe('300px');
  });

  it('sends resetSize for a double click on a resize handle', async () => {
    const sent = mountWindow({ resizable: true, customWidth: 500, customHeight: 300 });

    await userEvent.dblClick(windowElement().querySelector('[data-resize="e"]')!);
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_RESET_SIZE, {}]]);
  });

  it('moves by its title bar without telling the server', async () => {
    const sent = mountWindow({});

    drag(within(windowElement()).getByText('Bearbeiten'), 40, 30);
    await settle();

    expect(windowElement().style.position).toBe('absolute');
    expect(sent).not.toHaveBeenCalled();
  });

  it('keeps its place and its height limit when a move starts', async () => {
    mountWindow({});
    // The centered window as the browser lays it out.
    vi.spyOn(windowElement(), 'getBoundingClientRect').mockReturnValue(new DOMRect(100, 90, 640, 720));

    drag(within(windowElement()).getByText('Bearbeiten'), 1, 0);

    expect(windowElement().style.left).toBe('101px');
    expect(windowElement().style.top).toBe('90px');
    expect(windowElement().style.maxHeight).toBe('');
    expect(getComputedStyle(windowElement()).maxHeight).toBe('80vh');
    expect(windowElement().style.height).toBe('');
  });

  it('renders nothing while hidden', () => {
    mountWindow({ hidden: true });

    expect(screen.queryByRole('dialog')).toBeNull();
  });
});
