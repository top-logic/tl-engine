import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// The maximum number of values (state key maxCount): the controls adding values offer no further one
// at the maximum, and offer it again below it.

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { React, register, registerPart } from 'tl-react-bridge';
import TLFileChips from './TLFileChips';
import TLImageField from './TLImageField';
import TLValueList from './TLValueList';
import ButtonPart from './button/ButtonPart';
import ChipPart from './chip/ChipPart';
import { isFull } from './form/valueCount';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'values';

/** The module of a value of the value list; what it draws does not matter here. */
const VALUE_MODULE = 'TestValue';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  registerPart('Button', ButtonPart);
  registerPart('Chip', ChipPart);
  register('TLFileChips', TLFileChips);
  register('TLImageField', TLImageField);
  register('TLValueList', TLValueList);
  register(VALUE_MODULE, () => <span className="test-value" />);
});

afterEach(() => {
  act(() => unmount(CONTROL_ID));
  findMountPoint(CONTROL_ID)?.remove();
  vi.unstubAllGlobals();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Mounts the given module with the given state and answers its mount point. */
function mountControl(module: string, state: Record<string, unknown>): HTMLElement {
  // The server answers every request, the translations included, with nothing; the texts keep their defaults.
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, CONTROL_ID);
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, module, state, 'w', '/ctx'));
  return element;
}

/** The given number of values of the value list. */
function elements(count: number): unknown[] {
  return Array.from({ length: count }, (_, n) => ({ controlId: 'value' + n, module: VALUE_MODULE, state: {} }));
}

/** The given number of file chips. */
function chips(count: number): unknown[] {
  return Array.from({ length: count }, (_, n) => ({ key: String(n), name: n + '.txt', size: 1, hasData: false }));
}

/** The given number of image tiles. */
function images(count: number): unknown[] {
  return Array.from({ length: count }, (_, n) => ({ key: String(n), name: n + '.png' }));
}

describe('isFull', () => {
  it('is full at and beyond a positive maximum only', () => {
    expect(isFull(2, 3)).toBe(false);
    expect(isFull(3, 3)).toBe(true);
    expect(isFull(4, 3)).toBe(true);
    expect(isFull(10, 0)).toBe(false);
    expect(isFull(10, undefined)).toBe(false);
  });
});

describe('value list', () => {
  const addButton = (root: HTMLElement) => root.querySelector('button[aria-label="Add a value"]');
  const addFilesButton = (root: HTMLElement) => root.querySelector('button[aria-label="Add files"]');

  it('offers no further value at the maximum', () => {
    const root = mountControl('TLValueList', { editable: true, elements: elements(3), maxCount: 3 });
    expect(root.querySelectorAll('.test-value')).toHaveLength(3);
    expect(addButton(root)).toBeNull();
  });

  it('offers a further value below the maximum', () => {
    const root = mountControl('TLValueList', { editable: true, elements: elements(2), maxCount: 3 });
    expect(addButton(root)).not.toBeNull();
  });

  it('offers no further file at the maximum where values are uploaded', () => {
    const full = mountControl('TLValueList', { editable: true, upload: true, elements: elements(3), maxCount: 3 });
    expect(addFilesButton(full)).toBeNull();
  });

  it('offers a further file below the maximum where values are uploaded', () => {
    const root = mountControl('TLValueList', { editable: true, upload: true, elements: elements(2), maxCount: 3 });
    expect(addFilesButton(root)).not.toBeNull();
  });

  it('offers a further value without a maximum', () => {
    const root = mountControl('TLValueList', { editable: true, elements: elements(5), maxCount: 0 });
    expect(addButton(root)).not.toBeNull();
  });
});

describe('file chips', () => {
  const addButton = (root: HTMLElement) =>
    Array.from(root.querySelectorAll('button')).find(button => button.textContent === 'Add file') ?? null;
  const fileInput = (root: HTMLElement) => root.querySelector('input[type="file"]');

  it('offers no further file at the maximum, but keeps removing files', () => {
    const root = mountControl('TLFileChips', { editable: true, chips: chips(3), maxCount: 3 });
    expect(addButton(root)).toBeNull();
    expect(fileInput(root)).toBeNull();
    expect(root.querySelectorAll('button[aria-label^="Remove"]')).toHaveLength(3);
  });

  it('offers a further file below the maximum', () => {
    const root = mountControl('TLFileChips', { editable: true, chips: chips(2), maxCount: 3 });
    expect(addButton(root)).not.toBeNull();
    expect(fileInput(root)).not.toBeNull();
  });
});

describe('image field', () => {
  const dropArea = (root: HTMLElement) => root.querySelector('.tl-image-field__drop');

  it('leaves out the area adding images at the maximum, but keeps replacing them', () => {
    const root = mountControl('TLImageField', { editable: true, multiple: true, images: images(3), maxCount: 3 });
    expect(dropArea(root)).toBeNull();
    expect(root.querySelectorAll('button[aria-label^="Replace"]')).toHaveLength(3);
  });

  it('offers the area adding images below the maximum', () => {
    const root = mountControl('TLImageField', { editable: true, multiple: true, images: images(2), maxCount: 3 });
    expect(dropArea(root)).not.toBeNull();
  });
});
