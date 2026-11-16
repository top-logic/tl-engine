import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// A form showing its message instead of its fields while it has no model keeps its fields following
// the server, so that they show their live state when they are shown again.

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

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLFormLayout from './TLFormLayout';
import TLImageField from './TLImageField';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const FORM_ID = 'form';

const IMAGES_ID = 'images';

/** The image field of the form, as the form was serialized: showing the image of key 0. */
const IMAGES = {
  controlId: IMAGES_ID,
  module: 'TLImageField',
  state: { editable: false, multiple: true, images: [{ key: '0', name: 'old.png' }] },
};

/** The form measures its width to place its labels; the test has no layout to measure. */
class InertResizeObserver {
  observe(): void {}
  unobserve(): void {}
  disconnect(): void {}
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  vi.stubGlobal('ResizeObserver', InertResizeObserver);
  registerPart('Button', ButtonPart);
  register('TLFormLayout', TLFormLayout);
  register('TLImageField', TLImageField);
});

afterEach(() => {
  act(() => unmount(FORM_ID));
  findMountPoint(FORM_ID)?.remove();
});

afterAll(() => {
  document.body.replaceChildren();
  vi.unstubAllGlobals();
});

/** Mounts the form holding the image field and answers the element it is mounted in. */
function mountForm(): HTMLElement {
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, FORM_ID);
  document.body.appendChild(element);
  act(() => mount(FORM_ID, 'TLFormLayout', { children: [IMAGES] }, 'w', '/ctx'));
  return element;
}

/** The keys the previews of the given element fetch their images by. */
function previewKeys(element: HTMLElement): (string | null)[] {
  return Array.from(element.querySelectorAll('img'))
    .map(img => new URL(img.getAttribute('src')!, 'http://host/').searchParams.get('key'));
}

describe('form without model', () => {
  it('shows its fields again in their live state, not in the state they were serialized with', () => {
    const element = mountForm();
    expect(previewKeys(element)).toEqual(['0']);

    act(() => sse.deliver(FORM_ID, { noModelMessage: 'Nothing selected.' }));
    expect(element.querySelector('img')).toBeNull();

    // The image of key 0 is gone while the form shows its message.
    act(() => sse.deliver(IMAGES_ID, { images: [{ key: '1', name: 'new.png' }] }));

    // The previews the form creates when it shows its fields again: each fetches its image.
    const observer = new MutationObserver(() => {});
    observer.observe(element, { childList: true, subtree: true });
    act(() => sse.deliver(FORM_ID, { noModelMessage: null }));
    const fetched = observer.takeRecords()
      .flatMap(record => Array.from(record.addedNodes))
      .filter((node): node is Element => node instanceof Element)
      .flatMap(node => [node, ...Array.from(node.querySelectorAll('*'))])
      .filter(node => node.tagName === 'IMG')
      .map(img => new URL(img.getAttribute('src')!, 'http://host/').searchParams.get('key'));
    observer.disconnect();

    expect(previewKeys(element)).toEqual(['1']);
    expect(fetched).not.toContain('0');
  });
});
