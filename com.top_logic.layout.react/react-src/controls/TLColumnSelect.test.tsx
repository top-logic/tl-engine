// The group level of a grouping column in the column selection: a badge of the part Badge at the grouping toggle,
// part of the toggle's accessible name, shown only while the rows are grouped by several columns.

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
import { register, registerPart } from 'tl-react-bridge';
import TLColumnSelect from './TLColumnSelect';
import ButtonPart from './button/ButtonPart';
import BadgePart from './chip/BadgePart';
import TextInputPart from './form/TextInputPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const ROOT_ID = 'columns';

/** State key of the grouping: the names of the grouping columns, outermost group level first. */
const STATE_GROUPING = 'grouping';

const COL_A = 'a';
const COL_B = 'b';
const COL_C = 'c';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  registerPart('Badge', BadgePart);
  registerPart('TextInput', TextInputPart);
  register('TLColumnSelect', TLColumnSelect);
});

afterEach(() => {
  act(() => unmount(ROOT_ID));
  findMountPoint(ROOT_ID)?.remove();
  vi.unstubAllGlobals();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Draws the column selection of the columns a, b, c with the rows grouped by the given columns. */
function draw(grouping: string[]): HTMLElement {
  // The translations stay the English defaults of the control: the server answers without any.
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, ROOT_ID);
  document.body.appendChild(element);
  act(() => mount(ROOT_ID, 'TLColumnSelect', {
    entries: [COL_A, COL_B, COL_C].map((name) => ({
      name,
      label: name.toUpperCase(),
      visible: true,
      locked: false,
      grouped: grouping.includes(name),
    })),
    [STATE_GROUPING]: grouping,
  }, 'w', '/ctx'));
  return element;
}

/** The grouping toggle in the row of the column of the given label. */
function groupToggle(root: HTMLElement, label: string): HTMLElement {
  const item = Array.from(root.querySelectorAll<HTMLElement>('.tl-reorder-list__item'))
    .find((entry) => entry.querySelector('.tl-reorder-list__label')!.textContent === label)!;
  return item.querySelector<HTMLElement>('.tl-reorder-list__actions > button')!;
}

describe('the grouping toggle of the column selection', () => {
  it('carries the group level of each grouping column, the outermost one 1', () => {
    const root = draw([COL_B, COL_A]);

    expect(groupToggle(root, 'B').querySelector('.tl-badge')!.textContent).toBe('1');
    expect(groupToggle(root, 'A').querySelector('.tl-badge')!.textContent).toBe('2');
    expect(groupToggle(root, 'A').getAttribute('aria-label'))
      .toBe('Remove grouping by this column, Grouping level: 2');
    expect(groupToggle(root, 'C').querySelector('.tl-badge')).toBeNull();
    expect(groupToggle(root, 'C').getAttribute('aria-label')).toBe('Additionally group by this column');
  });

  it('carries no level while the rows are grouped by a single column', () => {
    const root = draw([COL_A]);

    expect(root.querySelector('.tl-badge')).toBeNull();
    expect(groupToggle(root, 'A').getAttribute('aria-label')).toBe('Remove grouping by this column');
  });
});
