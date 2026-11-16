import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// The image field: a preview per image, buttons on each tile while edited, the area taking an image.

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLImageField from './TLImageField';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** A command the field sent: its name and its arguments. */
type Sent = { command: string; args: Record<string, unknown> };

const CONTROL_ID = 'images';

let sent: Sent[] = [];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  registerPart('Button', ButtonPart);
  register('TLImageField', TLImageField);
});

afterEach(async () => {
  act(() => unmount(CONTROL_ID));
  findMountPoint(CONTROL_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Lets the command channel hand over what was sent. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** Mounts an image field with the given state, records the commands sent, and answers its root. */
function mountField(state: Record<string, unknown>): HTMLElement {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.command) {
      sent.push({ command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, CONTROL_ID);
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TLImageField', state, 'w', '/ctx'));
  return element.querySelector('.tl-image-field')!;
}

/** The images of the given field, by the key their preview is fetched with. */
function previewKeys(field: HTMLElement): (string | null)[] {
  return Array.from(field.querySelectorAll('img.tl-image-field__image'))
    .map(img => new URL(img.getAttribute('src')!, 'http://host/').searchParams.get('key'));
}

const A = { key: '0', name: 'a.png' };
const B = { key: '1', name: 'b.png' };

describe('image field', () => {
  it('offers the area taking an image while a single image field holds none', () => {
    const field = mountField({ editable: true, multiple: false, images: [], accept: 'image/*' });

    const drop = field.querySelector('.tl-image-field__drop')!;
    expect(drop.tagName).toBe('LABEL');
    const input = drop.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input.accept).toBe('image/*');
    expect(input.multiple).toBe(false);
    expect(previewKeys(field)).toEqual([]);
  });

  it('shows the preview of a single image instead of the area, and removes it by its button', async () => {
    const field = mountField({ editable: true, multiple: false, images: [A] });

    expect(previewKeys(field)).toEqual(['0']);
    expect(field.querySelector('img')!.getAttribute('alt')).toBe('a.png');
    expect(field.querySelector('.tl-image-field__drop')).toBeNull();

    const remove = field.querySelector('button[aria-label="Remove a.png"]') as HTMLButtonElement;
    act(() => remove.click());
    await settle();

    expect(sent).toEqual([{ command: 'removeImage', args: { key: '0' } }]);
  });

  it('shows several images as tiles followed by the area adding further ones', () => {
    const field = mountField({ editable: true, multiple: true, ordered: false, images: [A, B] });

    expect(previewKeys(field)).toEqual(['0', '1']);
    const input = field.querySelector('.tl-image-field__drop input[type="file"]') as HTMLInputElement;
    expect(input.multiple).toBe(true);
    expect(field.querySelector('button[aria-label="Move a.png"]')).toBeNull();
  });

  it('offers a handle moving each image where the order is part of the value', () => {
    const field = mountField({ editable: true, multiple: true, ordered: true, images: [A, B] });

    expect(field.querySelectorAll('.tl-image-field__actions button[aria-describedby]')).toHaveLength(2);
  });

  it('shows the previews alone where the field is only displayed', () => {
    const field = mountField({ editable: false, multiple: true, ordered: true, images: [A, B] });

    expect(previewKeys(field)).toEqual(['0', '1']);
    expect(field.querySelector('button')).toBeNull();
    expect(field.querySelector('input')).toBeNull();
  });

  it('keeps the buttons and the area of a disabled field inactive', () => {
    const field = mountField({ editable: false, disabled: true, multiple: true, images: [A] });

    for (const button of Array.from(field.querySelectorAll('button'))) {
      expect((button as HTMLButtonElement).disabled).toBe(true);
    }
    const drop = field.querySelector('.tl-image-field__drop')!;
    expect(drop.getAttribute('aria-disabled')).toBe('true');
    expect((drop.querySelector('input') as HTMLInputElement).disabled).toBe(true);
  });
});
