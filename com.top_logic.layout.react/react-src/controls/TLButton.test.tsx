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
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { React, ButtonDefaults, register, registerPart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLButton from './TLButton';
import TLAudioPlayer from './TLAudioPlayer';
import ButtonPart from './button/ButtonPart';
import MenuTriggerPart from './button/MenuTriggerPart';
import BadgePart from './chip/BadgePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const ROOT_ID = 'button';

const LABEL = 'Save';

/** The commands sent so far, by name. */
let sent: string[] = [];

/** The answers of the commands sent so far, in order, to be released by the test. */
let answers: Array<() => void> = [];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  registerPart('Badge', BadgePart);
  register('TLButton', TLButton);
  register('RowTrigger', ({ controlId }: TLCellProps) => (
    <ButtonDefaults small><MenuTriggerPart id={controlId} appearance="ghost" expanded={false} label={LABEL} icon={ICON} /></ButtonDefaults>
  ));
  register('HoverTrigger', ({ controlId }: TLCellProps) => (
    <MenuTriggerPart id={controlId} appearance="ghost" expanded={false} label={LABEL} icon={ICON} data-tl-trigger="hover" />
  ));
  register('RowButton', (props: TLCellProps) => <ButtonDefaults small><TLButton {...props} /></ButtonDefaults>);
  register('RowPlayer', (props: TLCellProps) => <ButtonDefaults small><TLAudioPlayer {...props} /></ButtonDefaults>);
});

afterEach(async () => {
  act(() => unmount(ROOT_ID));
  findMountPoint(ROOT_ID)?.remove();
  for (const release of answers) release();
  await settle();
  vi.unstubAllGlobals();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts a button whose commands wait for their answer until the test releases it. */
function mountButton(state: Record<string, unknown> = {}, component = 'TLButton'): void {
  sent = [];
  answers = [];
  vi.stubGlobal('fetch', vi.fn((url: string, init?: RequestInit) => {
    if (url.includes('react-api/i18n')) {
      // Translations requested before the mount are sent once the mount makes the context path known.
      return Promise.resolve(new Response('{}', { status: 200 }));
    }
    sent.push(JSON.parse(String(init?.body ?? '{}')).command);
    return new Promise<Response>((resolve) => {
      answers.push(() => resolve(new Response('{"success":true}', { status: 200 })));
    });
  }));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, ROOT_ID);
  document.body.appendChild(element);
  act(() => mount(ROOT_ID, component, { label: LABEL, ...state }, 'w', '/ctx'));
}

/** Waits until the command channel has handed all queued commands to fetch, and their answers back. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

function button(): HTMLButtonElement {
  return document.getElementById(ROOT_ID) as HTMLButtonElement;
}

describe('TLButton', () => {
  it('ignores a second click while the command of the first awaits its answer', async () => {
    mountButton();

    act(() => button().click());
    act(() => button().click());
    await settle();
    expect(sent).toEqual(['click']);

    // The second click left nothing queued behind the first.
    answers[0]();
    await settle();
    expect(sent).toEqual(['click']);

    act(() => button().click());
    await settle();
    expect(sent).toEqual(['click', 'click']);
  });
});

/** The icon of the buttons carrying a badge. */
const ICON = 'css:bi bi-inbox';

describe('TLButton with a badge', () => {
  function badge(): HTMLElement {
    return button().querySelector('.tl-badge') as HTMLElement;
  }

  it('draws the count after the label, as text of the button in the role of the badge', () => {
    mountButton({ badge: { content: '3', role: 'error' } });

    expect(badge().textContent).toBe('3');
    expect(badge().getAttribute('data-tl-role')).toBe('error');
    expect(badge().getAttribute('role')).toBeNull();
    // The accessible name of a button showing its label is its text: the label and the count.
    expect(button().getAttribute('aria-label')).toBeNull();
    expect(button().textContent).toBe(LABEL + ' 3');
  });

  it('draws the brand without a role attribute', () => {
    mountButton({ badge: { content: '99+' } });

    expect(badge().textContent).toBe('99+');
    expect(badge().hasAttribute('data-tl-role')).toBe(false);
  });

  it('draws a dot as an image named by the label of the badge', () => {
    mountButton({ badge: { dot: true, role: 'warning', label: 'New entries' } });

    expect(badge().classList).toContain('tl-badge--dot');
    expect(badge().textContent).toBe('');
    expect(badge().getAttribute('data-tl-role')).toBe('warning');
    expect(badge().getAttribute('role')).toBe('img');
    expect(badge().getAttribute('aria-label')).toBe('New entries');
  });

  it('puts the badge over the corner of the icon of a button showing its icon alone, and into its name', () => {
    mountButton({ image: ICON, displayMode: 'icon-only', badge: { content: '12' } });

    expect(badge().parentElement!.classList).toContain('tl-icon-badge__count');
    // The button names the count; the badge over the icon is not read a second time.
    expect(badge().getAttribute('aria-hidden')).toBe('true');
    expect(badge().hasAttribute('role')).toBe(false);
    expect(button().getAttribute('aria-label')).toBe(LABEL + ', 12');
  });

  it('combines the label of a counting badge with its count, never replacing it', () => {
    mountButton({ image: ICON, displayMode: 'icon-only', badge: { content: '1', label: 'Open tickets' } });

    expect(button().getAttribute('aria-label')).toBe(LABEL + ', Open tickets: 1');
  });

  it('names a button with icon and label by both, as its aria-label replaces the text', () => {
    mountButton({ image: ICON, displayMode: 'icon-label', badge: { content: '5', label: 'new' } });

    expect(button().getAttribute('aria-label')).toBe(LABEL + ', new: 5');
    expect(badge().textContent).toBe('5');
  });

  it('reads a counting badge with a label beside a visible label as an image of label and count', () => {
    mountButton({ badge: { content: '5', label: 'new' } });

    expect(badge().getAttribute('role')).toBe('img');
    expect(badge().getAttribute('aria-label')).toBe('new: 5');
  });

  it('draws no badge without one', () => {
    mountButton();

    expect(badge()).toBeNull();
  });

  it('reads a badge of null as none, at a button with icon and label and at one showing its icon alone', () => {
    mountButton({ image: ICON, displayMode: 'icon-label', badge: null });
    expect(badge()).toBeNull();
    expect(button().getAttribute('aria-label')).toBe(LABEL);
    act(() => unmount(ROOT_ID));
    findMountPoint(ROOT_ID)?.remove();

    mountButton({ image: ICON, displayMode: 'icon-only', badge: null });
    expect(badge()).toBeNull();
    expect(button().querySelector('.tl-icon-badge')).toBeNull();
    expect(button().getAttribute('aria-label')).toBe(LABEL);
  });
});

describe('TLButton in a table row (ButtonDefaults small)', () => {
  const small = () => button().classList.contains('tl-button--sm');

  it('draws an icon button with an image small', () => {
    mountButton({ image: ICON, displayMode: 'icon-only' }, 'RowButton');
    expect(small()).toBe(true);
  });

  it('draws a button with a label as it is', () => {
    mountButton({ image: ICON, displayMode: 'icon-label' }, 'RowButton');
    expect(small()).toBe(false);
  });

  it('draws an icon-only button without an image as a label button, not small', () => {
    mountButton({ displayMode: 'icon-only' }, 'RowButton');
    expect(small()).toBe(false);
  });

  it('draws an icon button outside a row as it is', () => {
    mountButton({ image: ICON, displayMode: 'icon-only' });
    expect(small()).toBe(false);
  });

  it('draws the icon trigger of a menu or popover small, too', () => {
    mountButton({}, 'RowTrigger');
    expect(small()).toBe(true);
  });

  it('keeps a media button at its size: it is the exception that needs a surface', () => {
    mountButton({}, 'RowPlayer');
    const play = document.querySelector<HTMLButtonElement>('.tl-button--icon');
    expect(play).not.toBeNull();
    expect(play!.classList.contains('tl-button--sm')).toBe(false);
  });

  it('names an icon trigger by its label and shows the label as tooltip, except on a trigger opening on hover', () => {
    mountButton({}, 'RowTrigger');
    expect(button().hasAttribute('data-tooltip')).toBe(true);
    act(() => unmount(ROOT_ID));
    findMountPoint(ROOT_ID)?.remove();

    mountButton({}, 'HoverTrigger');
    expect(button().getAttribute('aria-label')).toBe(LABEL);
    expect(button().hasAttribute('data-tooltip')).toBe(false);
  });
});
