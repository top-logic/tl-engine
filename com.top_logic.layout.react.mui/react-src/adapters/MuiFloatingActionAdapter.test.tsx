// The wire contract of TLFloatingAction, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, act, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { registerTransientOverlay } from 'tl-react-bridge';
import type { FloatingActionStateJson } from 'tl-react-bridge';
import MuiFloatingActionAdapter from './MuiFloatingActionAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';

/** The command pressing an action. */
const CMD_CLICK = 'click';

const ICON = 'css:fa-solid fa-pen';

/** A button control as the server describes it as an action. */
function action(controlId: string, label: string, state: Record<string, unknown> = {}) {
  return { controlId, module: 'TLButton', state: { label, image: ICON, displayMode: 'icon-only', ...state } };
}

function mountFab(state: Partial<Record<keyof FloatingActionStateJson, unknown>>) {
  return mountAdapter(MuiFloatingActionAdapter, { triggerLabel: 'Actions', triggerImage: ICON, ...state });
}

/** The commands sent to child controls: the control, the command. */
function childCommands(): [string, string][] {
  const fetchMock = globalThis.fetch as unknown as { mock: { calls: [string, RequestInit?][] } };
  return fetchMock.mock.calls
    .map(([, init]) => JSON.parse(String(init?.body ?? '{}')))
    .filter(body => body.command !== undefined && body.controlId !== CONTROL_ID)
    .map(body => [body.controlId, body.command]);
}

function rect(top: number, left: number, height: number, width: number): DOMRect {
  return {
    top, left, bottom: top + height, right: left + width, height, width, x: left, y: top,
    toJSON: () => ({}),
  } as DOMRect;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLFloatingAction as MUI Fab', () => {
  it('shows a single action as a round MUI Fab over the corner of the window', () => {
    mountFab({ actions: [action('a1', 'Create')], cssClass: 'my-fab' });

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.parentElement).toBe(document.body);
    expect(root.dataset.tlAnchor).toBe('viewport');
    expect(root.dataset.tlPosition).toBe('bottom-end');
    expect(root.classList).toContain('my-fab');
    const fab = screen.getByRole('button', { name: 'Create' });
    expect(fab.id).toBe('a1');
    expect(fab.classList).toContain('MuiFab-root');
    expect(fab.classList).toContain('MuiFab-circular');
    expect(fab.classList).toContain('MuiFab-primary');
    expect(fab.textContent).toBe('');
    expect(document.querySelector('.MuiSpeedDial-root')).toBeNull();
  });

  it('shows an action whose display mode shows its label as an extended Fab', () => {
    mountFab({ actions: [action('a1', 'Create note', { displayMode: 'icon-label' })] });

    const fab = screen.getByRole('button', { name: 'Create note' });
    expect(fab.classList).toContain('MuiFab-extended');
    expect(fab.textContent).toBe('Create note');
    expect(fab.querySelector('.tl-icon-sm')).not.toBeNull();
  });

  it('shows a disabled action as a disabled Fab', () => {
    mountFab({ actions: [action('a1', 'Create', { disabled: true })] });

    expect((screen.getByRole('button', { name: 'Create' }) as HTMLButtonElement).disabled).toBe(true);
  });

  it('presses the action by sending click to its button control', async () => {
    const sent = mountFab({ actions: [action('a1', 'Create')] });

    await userEvent.click(screen.getByRole('button', { name: 'Create' }));
    await settle();

    expect(childCommands()).toEqual([['a1', CMD_CLICK]]);
    expect(sent).not.toHaveBeenCalled();
  });

  it('presses the action by its keyboard gesture', async () => {
    mountFab({ actions: [action('a1', 'Create', { keyGesture: 'Ctrl+N' })] });

    fireEvent.keyDown(document.body, { key: 'n', code: 'KeyN', ctrlKey: true });
    await settle();

    expect(childCommands()).toEqual([['a1', CMD_CLICK]]);
  });

  it('renders nothing while all actions are hidden', () => {
    mountFab({ actions: [action('a1', 'Create', { hidden: true }), action('a2', 'Upload', { hidden: true })] });

    expect(document.getElementById(CONTROL_ID)).toBeNull();
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('shows the one action not hidden as a Fab', () => {
    mountFab({ actions: [action('a1', 'Create', { hidden: true }), action('a2', 'Upload')] });

    expect(screen.getByRole('button', { name: 'Upload' }).classList).toContain('MuiFab-root');
    expect(document.querySelector('.MuiSpeedDial-root')).toBeNull();
  });
});

describe('TLFloatingAction as MUI SpeedDial', () => {
  const ACTIONS = [
    action('a1', 'Create'),
    action('a2', 'Upload', { disabled: true }),
    action('a3', 'Hidden', { hidden: true }),
    action('a4', 'Export'),
  ];

  it('shows several actions as a speed dial named by the trigger label, opening upwards', () => {
    mountFab({ actions: ACTIONS });

    const dial = document.querySelector('.MuiSpeedDial-root')!;
    expect(dial.classList).toContain('MuiSpeedDial-directionUp');
    const trigger = screen.getByRole('button', { name: 'Actions' });
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    const items = screen.getAllByRole('menuitem', { hidden: true });
    expect(items.map(item => item.getAttribute('aria-label'))).toEqual(['Create', 'Upload', 'Export']);
    expect((items[1] as HTMLButtonElement).disabled).toBe(true);
  });

  it('opens downwards in a top corner', () => {
    mountFab({ actions: ACTIONS, position: 'top-start' });

    expect(document.querySelector('.MuiSpeedDial-root')!.classList).toContain('MuiSpeedDial-directionDown');
    expect(document.getElementById(CONTROL_ID)!.dataset.tlPosition).toBe('top-start');
  });

  it('shows the label of an action as its tooltip', async () => {
    mountFab({ actions: ACTIONS });

    fireEvent.click(screen.getByRole('button', { name: 'Actions' }));
    await userEvent.hover(screen.getByRole('menuitem', { name: 'Create' }));

    expect((await screen.findByRole('tooltip')).textContent).toBe('Create');
  });

  it('opens on a click of its button only, not when hovered or focused', async () => {
    mountFab({ actions: ACTIONS });

    const dial = document.querySelector('.MuiSpeedDial-root')!;
    const trigger = screen.getByRole('button', { name: 'Actions' });
    fireEvent.mouseEnter(dial);
    act(() => trigger.focus());
    await settle();
    expect(trigger.getAttribute('aria-expanded')).toBe('false');

    fireEvent.click(trigger);
    expect(trigger.getAttribute('aria-expanded')).toBe('true');

    fireEvent.mouseLeave(dial);
    await settle();
    expect(trigger.getAttribute('aria-expanded')).toBe('true');

    fireEvent.click(trigger);
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
  });

  it('closes on Escape and on a press outside of it', async () => {
    mountFab({ actions: ACTIONS });

    const trigger = screen.getByRole('button', { name: 'Actions' });
    fireEvent.click(trigger);
    fireEvent.keyDown(trigger, { key: 'Escape' });
    expect(trigger.getAttribute('aria-expanded')).toBe('false');

    // Escape with the focus outside of the dial closes it as well and brings the focus to its button.
    const outside = document.createElement('button');
    document.body.appendChild(outside);
    fireEvent.click(trigger);
    expect(trigger.getAttribute('aria-expanded')).toBe('true');
    act(() => outside.focus());
    fireEvent.keyDown(outside, { key: 'Escape' });
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    expect(document.activeElement).toBe(trigger);
    outside.remove();

    fireEvent.click(trigger);
    expect(trigger.getAttribute('aria-expanded')).toBe('true');
    await userEvent.click(document.body);
    await settle();
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
  });

  it('presses an action by sending click to its button control and closes', async () => {
    mountFab({ actions: ACTIONS });

    const trigger = screen.getByRole('button', { name: 'Actions' });
    fireEvent.click(trigger);
    expect(trigger.getAttribute('aria-expanded')).toBe('true');

    await userEvent.click(screen.getByRole('menuitem', { name: 'Export' }));
    await settle();

    expect(childCommands()).toEqual([['a4', CMD_CLICK]]);
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
  });
});

describe('TLFloatingAction as MUI Fab beside a transient overlay', () => {
  it('moves up while the overlay covers it', () => {
    mountFab({ actions: [action('a1', 'Create')] });
    const root = document.getElementById(CONTROL_ID)!;
    root.getBoundingClientRect = () => rect(500, 700, 56, 56);
    const overlay = document.createElement('div');
    overlay.getBoundingClientRect = () => rect(520, 400, 60, 400);
    document.body.appendChild(overlay);

    let withdraw: () => void = () => {};
    act(() => { withdraw = registerTransientOverlay(overlay); });

    // The overlay's top (520), the gap without the design system (12), the bottom of the action (556).
    expect(root.style.transform).toBe('translateY(-48px)');

    act(() => withdraw());
    expect(root.style.transform).toBe('');
    overlay.remove();
  });
});
