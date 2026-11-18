import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// A picture with a height or an aspect ratio shows the part Skeleton over its box until it has loaded.

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLImage from './TLImage';
import SkeletonPart from './skeleton/SkeletonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'image';

/** The address of the picture. */
const URL = '/picture.png';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  registerPart('Skeleton', SkeletonPart);
  register('TLImage', TLImage);
});

afterEach(() => {
  act(() => unmount(CONTROL_ID));
  findMountPoint(CONTROL_ID)?.remove();
  vi.unstubAllGlobals();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Mounts a picture with the given state and answers the box it is drawn in. */
function mountImage(state: Record<string, unknown>): HTMLElement {
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, CONTROL_ID);
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TLImage', { url: URL, fit: 'cover', ...state }, 'w', '/ctx'));
  return element.querySelector('.tl-media')!;
}

/** The skeleton over the box, null if there is none. */
function skeleton(box: HTMLElement): Element | null {
  return box.querySelector('.tl-media__loading > .tl-skeleton');
}

/** Fires the given event at the picture of the box. */
function fire(box: HTMLElement, type: 'load' | 'error'): void {
  act(() => {
    box.querySelector('img')!.dispatchEvent(new Event(type));
  });
}

describe('a picture with a height or an aspect ratio', () => {
  it('shows a rectangle skeleton over its box and marks it busy until the picture has loaded', () => {
    const box = mountImage({ width: '20rem', aspectRatio: '4 / 3' });

    expect(skeleton(box)!.className).toBe('tl-skeleton');
    expect(skeleton(box)!.getAttribute('aria-hidden')).toBe('true');
    expect(box.getAttribute('aria-busy')).toBe('true');

    fire(box, 'load');

    expect(skeleton(box)).toBeNull();
    expect(box.querySelector('.tl-media__loading')).toBeNull();
    expect(box.hasAttribute('aria-busy')).toBe(false);
  });

  it('removes the skeleton when the picture fails to load', () => {
    const box = mountImage({ height: '10rem' });
    expect(skeleton(box)).not.toBeNull();

    fire(box, 'error');

    expect(skeleton(box)).toBeNull();
    expect(box.hasAttribute('aria-busy')).toBe(false);
  });
});

describe('a picture with a width alone', () => {
  it('shows no skeleton, since its box is as high as the picture', () => {
    const box = mountImage({ width: '16rem' });

    expect(box.querySelector('.tl-media__loading')).toBeNull();
    expect(box.hasAttribute('aria-busy')).toBe(false);
  });
});

describe('a picture without a size option', () => {
  it('shows no skeleton while the picture loads', () => {
    const box = mountImage({});

    expect(box.querySelector('img')).not.toBeNull();
    expect(box.querySelector('.tl-media__loading')).toBeNull();
    expect(box.hasAttribute('aria-busy')).toBe(false);
  });
});

describe('a box without a picture', () => {
  it('shows no skeleton', () => {
    const box = mountImage({ url: null, width: '20rem', height: '10rem' });

    expect(box.querySelector('.tl-media__placeholder')).not.toBeNull();
    expect(box.querySelector('.tl-media__loading')).toBeNull();
    expect(box.hasAttribute('aria-busy')).toBe(false);
  });
});
