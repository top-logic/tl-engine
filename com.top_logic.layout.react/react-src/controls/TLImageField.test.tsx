import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// The image field: the stage showing one image at a time, the arrange view while edited, the view
// filling the page while displayed, the area taking an image.

/** The listener of each mounted control, through which a test sends it an update of its state. */
const listeners = vi.hoisted(() => new Map<string, (state: Record<string, unknown>) => void>());

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: (id: string, listener: (state: Record<string, unknown>) => void) => { listeners.set(id, listener); },
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

/** The uploads the field sent. */
let uploads: FormData[] = [];

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
  uploads = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    if (init?.body instanceof FormData) {
      uploads.push(init.body);
      return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
    }
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

/** The images in the given element, by the key they are fetched with. */
function previewKeys(element: ParentNode, selector = 'img.tl-image-field__image'): (string | null)[] {
  return Array.from(element.querySelectorAll(selector))
    .map(img => new URL(img.getAttribute('src')!, 'http://host/').searchParams.get('key'));
}

/** The field of the mounted control, found anew after a render that replaced it. */
function currentField(): HTMLElement {
  return document.querySelector('.tl-image-field')!;
}

/** Sends the mounted control an update of its state, as the server does. */
function update(patch: Record<string, unknown>): void {
  act(() => listeners.get(CONTROL_ID)!(patch));
}

/** Clicks the button with the given accessible name within the given element. */
function click(element: ParentNode, label: string): void {
  const button = element.querySelector(`button[aria-label="${label}"]`) as HTMLButtonElement | null;
  expect(button, label).not.toBeNull();
  act(() => button!.click());
}

/** Presses the given key on the given element. */
function press(element: Element, key: string): void {
  act(() => {
    element.dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true }));
  });
}

/** Drops the given files on the given element. */
function dropFiles(element: Element, files: File[]): void {
  const dataTransfer = { types: ['Files'], files, dropEffect: 'none' };
  for (const type of ['dragover', 'drop']) {
    const event = new Event(type, { bubbles: true, cancelable: true });
    Object.defineProperty(event, 'dataTransfer', { value: dataTransfer });
    act(() => { element.dispatchEvent(event); });
  }
}

/** The text of the part of the given upload with the given name. */
async function partText(upload: FormData, name: string): Promise<string | null> {
  const part = upload.get(name);
  return part instanceof Blob ? await part.text() : null;
}

const A = { key: '0', name: 'a.png' };
const B = { key: '1', name: 'b.png' };
const C = { key: '2', name: 'c.png' };

describe('image field', () => {
  it('makes the stage the area taking an image while an edited field holds none', () => {
    const field = mountField({ editable: true, multiple: false, images: [], accept: 'image/*' });

    const stage = field.querySelector('.tl-image-field__stage')!;
    expect(stage.getAttribute('data-tl-state')).toBe('empty');
    const drop = stage.querySelector('.tl-image-field__drop')!;
    expect(drop.tagName).toBe('LABEL');
    const input = drop.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input.accept).toBe('image/*');
    expect(input.multiple).toBe(false);
    expect(previewKeys(field)).toEqual([]);
  });

  it('shows nothing where a displayed field holds no image', () => {
    const field = mountField({ editable: false, multiple: true, images: [] });

    expect(field.querySelector('.tl-image-field__stage')).toBeNull();
    expect(field.querySelector('input')).toBeNull();
  });

  it('shows a single image on the stage, without stepping', () => {
    const field = mountField({ editable: true, multiple: false, images: [A], height: '300px' });

    expect(previewKeys(field)).toEqual(['0']);
    expect(field.querySelector('img')!.getAttribute('alt')).toBe('a.png');
    expect(field.querySelector('.tl-image-field__drop')).toBeNull();
    expect(field.querySelector('.tl-image-field__step')).toBeNull();
    expect(field.querySelector('.tl-image-field__counter')).toBeNull();
    expect((field.querySelector('.tl-image-field__stage') as HTMLElement).style.blockSize).toBe('300px');
  });

  it('steps through several images with its buttons and the arrow keys, wrapping around', () => {
    const field = mountField({ editable: false, multiple: true, images: [A, B, C] });

    expect(previewKeys(field)).toEqual(['0']);
    expect(field.querySelector('.tl-image-field__counter')!.textContent).toBe('1 of 3');

    click(field, 'Next image');
    expect(previewKeys(currentField())).toEqual(['1']);
    expect(currentField().querySelector('.tl-image-field__counter')!.textContent).toBe('2 of 3');

    const stage = currentField().querySelector('.tl-image-field__stage')!;
    press(stage, 'ArrowRight');
    press(stage, 'ArrowRight');
    expect(previewKeys(currentField())).toEqual(['0']);

    click(currentField(), 'Previous image');
    expect(previewKeys(currentField())).toEqual(['2']);
  });

  it('opens the image of a displayed field in a view filling the page, closed by Escape', () => {
    const field = mountField({ editable: false, multiple: true, images: [A, B] });

    click(field, 'Show a.png full page');
    const viewer = document.body.querySelector('.tl-image-viewer')!;
    expect(viewer.getAttribute('role')).toBe('dialog');
    expect(previewKeys(viewer, 'img.tl-image-viewer__image')).toEqual(['0']);

    press(viewer, 'ArrowRight');
    expect(previewKeys(document.body, 'img.tl-image-viewer__image')).toEqual(['1']);
    // The stage follows the view.
    expect(previewKeys(currentField())).toEqual(['1']);

    press(document.body.querySelector('.tl-image-viewer')!, 'Escape');
    expect(document.body.querySelector('.tl-image-viewer')).toBeNull();
  });

  it('opens the arrange view of an edited field, and goes back to the stage at the tile clicked', () => {
    const field = mountField({ editable: true, multiple: true, ordered: true, images: [A, B] });

    click(field, 'Edit a.png');
    const arrange = currentField();
    expect(arrange.classList.contains('tl-image-field--arrange')).toBe(true);
    expect(previewKeys(arrange)).toEqual(['0', '1']);
    // A handle moving each image, since the order is part of the value.
    expect(arrange.querySelectorAll('.tl-image-field__actions button[aria-describedby]')).toHaveLength(2);
    const input = arrange.querySelector('.tl-image-field__drop input[type="file"]') as HTMLInputElement;
    expect(input.multiple).toBe(true);

    click(arrange, 'Show b.png');
    expect(currentField().classList.contains('tl-image-field--arrange')).toBe(false);
    expect(previewKeys(currentField())).toEqual(['1']);
  });

  it('removes an image in the arrange view, and leaves it by Done', async () => {
    const field = mountField({ editable: true, multiple: false, images: [A] });

    click(field, 'Edit a.png');
    // A single image has no area adding further ones and nothing to move.
    expect(currentField().querySelector('.tl-image-field__drop')).toBeNull();
    expect(currentField().querySelector('button[aria-label="Move a.png"]')).toBeNull();

    click(currentField(), 'Remove a.png');
    await settle();
    expect(sent).toEqual([{ command: 'removeImage', args: { key: '0' } }]);

    const done = Array.from(currentField().querySelectorAll('button')).find(b => b.textContent === 'Done')!;
    act(() => done.click());
    expect(currentField().classList.contains('tl-image-field--arrange')).toBe(false);
  });

  it('inserts files dropped on the stage after the image shown, and shows the first one added', async () => {
    const field = mountField({ editable: true, multiple: true, ordered: true, images: [A, B] });
    click(field, 'Next image');

    dropFiles(currentField().querySelector('.tl-image-field__stage')!, [new File(['x'], 'new.png')]);
    await settle();

    expect(uploads).toHaveLength(1);
    expect(await partText(uploads[0], 'after')).toBe('1');
    expect(uploads[0].get('replace')).toBeNull();

    update({ images: [A, B, C] });
    expect(previewKeys(currentField())).toEqual(['2']);
  });

  it('replaces a single image by a file dropped on the stage', async () => {
    const field = mountField({ editable: true, multiple: false, images: [A] });

    dropFiles(field.querySelector('.tl-image-field__stage')!, [new File(['x'], 'new.png')]);
    await settle();

    expect(uploads).toHaveLength(1);
    expect(await partText(uploads[0], 'replace')).toBe('0');
  });

  it('keeps its place when the image shown is removed', () => {
    const field = mountField({ editable: false, multiple: true, images: [A, B, C] });
    click(field, 'Next image');

    update({ images: [A, C] });
    expect(previewKeys(currentField())).toEqual(['2']);
  });

  it('opens a disabled field in the view filling the page, and keeps its area inactive', () => {
    const field = mountField({ editable: false, disabled: true, multiple: true, images: [] });

    const drop = field.querySelector('.tl-image-field__drop')!;
    expect(drop.getAttribute('aria-disabled')).toBe('true');
    expect((drop.querySelector('input') as HTMLInputElement).disabled).toBe(true);

    update({ images: [A] });
    click(currentField(), 'Show a.png full page');
    expect(document.body.querySelector('.tl-image-viewer')).not.toBeNull();
  });
});
