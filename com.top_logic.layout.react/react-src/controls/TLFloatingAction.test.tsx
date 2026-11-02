import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

/**
 * The SSE listeners subscribed by the bridge, by control ID: delivering an event to a control calls
 * its listeners like the SSE client does with a patch of the server.
 */
const sse = vi.hoisted(() => {
  const listeners = new Map<string, Set<(data: Record<string, unknown>) => void>>();
  return {
    listeners,
    deliver(controlId: string, data: Record<string, unknown>): void {
      for (const listener of listeners.get(controlId) ?? []) {
        listener(data);
      }
    },
  };
});

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: (controlId: string, listener: (data: Record<string, unknown>) => void) => {
    let set = sse.listeners.get(controlId);
    if (!set) {
      set = new Set();
      sse.listeners.set(controlId, set);
    }
    set.add(listener);
  },
  unsubscribe: (controlId: string, listener: (data: Record<string, unknown>) => void) => {
    const set = sse.listeners.get(controlId);
    set?.delete(listener);
    if (set?.size === 0) {
      sse.listeners.delete(controlId);
    }
  },
}));

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import {
  React, register, registerPart, registerTransientOverlay, TLChild, useTLState, useChildCommand, clearance,
  useFloatingFrame, FloatingFrame, FloatingRail,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLFloatingAction from './TLFloatingAction';
import TLButton from './TLButton';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** A command a control sent: its control, its name and its arguments. */
type Sent = { controlId: string; command: string; args: Record<string, unknown> };

const ROOT_ID = 'root';

const FAB_ID = 'fab';

const ICON = 'css:fa-solid fa-pen';

let sent: Sent[] = [];

/** A frame offering its rails around a body holding its child, as a panel does. */
const TestFrame: React.FC<TLCellProps> = () => {
  const state = useTLState<{ child: unknown }>();
  const floating = useFloatingFrame();
  return (
    <div className="test-frame">
      <FloatingRail host={floating} edge="top" />
      <div className="test-frame__body">
        <FloatingFrame host={floating}>
          <TLChild control={state.child} />
        </FloatingFrame>
      </div>
      <FloatingRail host={floating} edge="bottom" />
    </div>
  );
};

/** Draws its child itself and presses it with useChildCommand, as a component library may. */
const TestPresser: React.FC<TLCellProps> = () => {
  const state = useTLState<{ child: { controlId: string } }>();
  const sendToChild = useChildCommand();
  return <button type="button" onClick={() => sendToChild(state.child.controlId, 'click')}>Press</button>;
};

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  register('TLButton', TLButton);
  register('TLFloatingAction', TLFloatingAction);
  register('TestFrame', TestFrame);
  register('TestPresser', TestPresser);
});

afterEach(async () => {
  act(() => unmount(ROOT_ID));
  document.getElementById(ROOT_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** A button control as the server describes it as an action. */
function action(controlId: string, label: string, state: Record<string, unknown> = {}) {
  return { controlId, module: 'TLButton', state: { label, image: ICON, displayMode: 'icon-only', ...state } };
}

/** The floating action control with the given state, as the child of a control. */
function fab(state: Record<string, unknown>) {
  return { controlId: FAB_ID, module: 'TLFloatingAction', state };
}

/** Mounts the given component with the given state and records the commands sent. */
function mountRoot(module: string, state: Record<string, unknown>): void {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.command) {
      sent.push({ controlId: body.controlId, command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.id = ROOT_ID;
  document.body.appendChild(element);
  act(() => mount(ROOT_ID, module, state, 'w', '/ctx'));
}

/** Mounts the floating actions in a frame. */
function mountInFrame(state: Record<string, unknown>): void {
  mountRoot('TestFrame', { child: fab(state) });
}

/** Waits until the command channel has handed all queued commands to fetch. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** The root of the floating actions, wherever they are portaled to. */
function root(): HTMLElement | null {
  return document.querySelector<HTMLElement>('.tl-fab');
}

function trigger(): HTMLButtonElement {
  return root()!.querySelector<HTMLButtonElement>('.tl-fab__main button')!;
}

function actionsElement(): HTMLElement {
  return root()!.querySelector<HTMLElement>('.tl-fab__actions')!;
}

function button(name: string): HTMLButtonElement {
  return root()!.querySelector<HTMLButtonElement>(`button[aria-label="${name}"]`)!;
}

/** A click from the keyboard: no pointer press, a click count of zero. */
function keyboardClick(element: HTMLElement): void {
  act(() => {
    element.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, detail: 0 }));
  });
}

/** A click of the pointer: the press first, then the click. */
function pointerClick(element: Element): void {
  act(() => {
    element.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, cancelable: true }));
    element.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, detail: 1 }));
  });
}

function rect(top: number, left: number, height: number, width: number): DOMRect {
  return {
    top, left, bottom: top + height, right: left + width, height, width, x: left, y: top,
    toJSON: () => ({}),
  } as DOMRect;
}

describe('a single action', () => {
  it('is a round button in the bottom rail of the frame', () => {
    mountInFrame({ actions: [action('a1', 'Create')] });

    const fabRoot = root()!;
    expect(fabRoot.parentElement?.classList).toContain('tl-floating-rail');
    expect(fabRoot.parentElement?.getAttribute('data-tl-edge')).toBe('bottom');
    expect(fabRoot.getAttribute('data-tl-position')).toBe('bottom-end');
    expect(fabRoot.querySelector('.tl-fab__actions')).toBeNull();
    const main = button('Create');
    expect(main.closest('.tl-fab__main')).not.toBeNull();
    expect(main.classList).toContain('tl-button--primary');
    expect(main.classList).toContain('tl-button--icon');
  });

  it('shows its label when its display mode asks for it', () => {
    mountInFrame({ actions: [action('a1', 'Create', { displayMode: 'icon-label' })] });

    expect(root()!.querySelector('.tl-fab__main button')!.textContent).toContain('Create');
  });

  it('is placed against the top of the body for a top position', () => {
    mountInFrame({ actions: [action('a1', 'Create')], position: 'top-start' });

    expect(root()!.parentElement?.getAttribute('data-tl-edge')).toBe('top');
    expect(root()!.getAttribute('data-tl-position')).toBe('top-start');
  });

  it('floats over the browser window outside of a frame', () => {
    mountRoot('TLFloatingAction', { actions: [action('a1', 'Create')] });

    expect(root()!.parentElement).toBe(document.body);
    expect(root()!.getAttribute('data-tl-anchor')).toBe('viewport');
  });

  it('sends the click of its button', async () => {
    mountInFrame({ actions: [action('a1', 'Create')] });

    act(() => button('Create').click());
    await settle();

    expect(sent).toEqual([{ controlId: 'a1', command: 'click', args: {} }]);
  });
});

describe('the frame', () => {
  it('renders no rails without a floating element', () => {
    mountRoot('TestFrame', { child: { controlId: 'b', module: 'TLButton', state: { label: 'Plain' } } });

    expect(document.querySelector('.tl-floating-rail')).toBeNull();
  });
});

describe('all actions hidden', () => {
  it('renders nothing', () => {
    mountInFrame({ actions: [action('a1', 'Create', { hidden: true }), action('a2', 'Upload', { hidden: true })] });

    expect(root()).toBeNull();
  });

  it('shows the action becoming visible', () => {
    mountInFrame({ actions: [action('a1', 'Create', { hidden: true }), action('a2', 'Upload', { hidden: true })] });

    act(() => sse.deliver('a2', { hidden: false }));

    expect(root()).not.toBeNull();
    expect(root()!.querySelector('.tl-fab__actions')).toBeNull();
    expect(button('Upload')).not.toBeNull();
  });
});

describe('several actions', () => {
  const DIAL = {
    actions: [action('a1', 'Create'), action('a2', 'Upload')],
    triggerImage: 'css:fa-solid fa-plus',
    triggerLabel: 'Actions',
  };

  it('are a speed dial: a closed trigger and the actions as icon buttons named by their labels', () => {
    mountInFrame(DIAL);

    expect(trigger().getAttribute('aria-label')).toBe('Actions');
    expect(trigger().getAttribute('aria-expanded')).toBe('false');
    expect(trigger().getAttribute('aria-controls')).toBe(actionsElement().id);
    expect(actionsElement().hidden).toBe(true);
    expect(button('Create').closest('.tl-fab__action')).not.toBeNull();
    expect(button('Create').classList).toContain('tl-button--icon');
    expect(button('Create').classList).toContain('tl-button--secondary');
  });

  it('opens from the keyboard with the focus on the first action, and closes on Escape', async () => {
    mountInFrame(DIAL);

    keyboardClick(trigger());
    await settle();
    expect(trigger().getAttribute('aria-expanded')).toBe('true');
    expect(actionsElement().hidden).toBe(false);
    expect(document.activeElement).toBe(button('Create'));

    act(() => {
      document.activeElement!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true }));
    });
    expect(trigger().getAttribute('aria-expanded')).toBe('false');
    expect(actionsElement().hidden).toBe(true);
    expect(document.activeElement).toBe(trigger());
  });

  it('closes after an action ran', async () => {
    mountInFrame(DIAL);
    pointerClick(trigger());
    expect(trigger().getAttribute('aria-expanded')).toBe('true');

    act(() => button('Upload').click());
    await settle();

    expect(sent).toEqual([{ controlId: 'a2', command: 'click', args: {} }]);
    expect(trigger().getAttribute('aria-expanded')).toBe('false');
  });

  it('closes on a press outside of it', () => {
    mountInFrame(DIAL);
    pointerClick(trigger());
    expect(trigger().getAttribute('aria-expanded')).toBe('true');

    pointerClick(document.body);

    expect(trigger().getAttribute('aria-expanded')).toBe('false');
  });

  it('is a single button again when all but one action are hidden', () => {
    mountInFrame(DIAL);

    act(() => sse.deliver('a1', { hidden: true }));

    expect(root()!.querySelector('.tl-fab__actions')).toBeNull();
    expect(button('Upload').classList).toContain('tl-button--primary');
  });
});

describe('a transient overlay', () => {
  it('moves the actions up while it covers them', () => {
    mountInFrame({ actions: [action('a1', 'Create')] });
    root()!.getBoundingClientRect = () => rect(500, 700, 48, 48);
    const overlay = document.createElement('div');
    overlay.getBoundingClientRect = () => rect(520, 400, 60, 400);
    document.body.appendChild(overlay);

    let withdraw: () => void = () => {};
    act(() => { withdraw = registerTransientOverlay(overlay); });

    // The overlay's top (520), the gap without the design system (12), the bottom of the actions (548).
    expect(root()!.style.transform).toBe('translateY(-40px)');

    act(() => withdraw());
    expect(root()!.style.transform).toBe('');
    overlay.remove();
  });

  it('leaves the actions in place while it does not cover them', () => {
    mountInFrame({ actions: [action('a1', 'Create')] });
    root()!.getBoundingClientRect = () => rect(500, 700, 48, 48);
    const overlay = document.createElement('div');
    overlay.getBoundingClientRect = () => rect(520, 100, 60, 400);
    document.body.appendChild(overlay);

    let withdraw: () => void = () => {};
    act(() => { withdraw = registerTransientOverlay(overlay); });

    expect(root()!.style.transform).toBe('');
    act(() => withdraw());
    overlay.remove();
  });
});

describe('clearance', () => {
  const BOX = { top: 100, bottom: 148, left: 200, right: 248 };

  it('moves a box at the top edge down below an overlay it overlaps', () => {
    expect(clearance(BOX, [{ top: 90, bottom: 120, left: 0, right: 1000 }], 'top', 8)).toBe(28);
  });

  it('ignores an overlay of no extent', () => {
    expect(clearance(BOX, [{ top: 0, bottom: 0, left: 0, right: 0 }], 'bottom', 8)).toBe(0);
  });
});

describe('useChildCommand', () => {
  it('sends a command to a child drawn by its container', async () => {
    mountRoot('TestPresser', { child: { controlId: 'c1', module: 'TLButton', state: { label: 'C' } } });

    act(() => document.querySelector<HTMLButtonElement>('#root button')!.click());
    await settle();

    expect(sent).toEqual([{ controlId: 'c1', command: 'click', args: {} }]);
  });
});
