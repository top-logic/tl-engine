import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// The footer of a filter popover (#30102) in the design system's own drawing: its actions are buttons of the part
// Button, so the package draws them with the classes of every button - and a component library replaces them.

vi.mock('../../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { mount, unmount } from '../../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLFilterList from './TLFilterList';
import TLFilterForm from './TLFilterForm';
import ButtonPart from '../button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const ROOT_ID = 'root';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  register('TLFilterList', TLFilterList);
  register('TLFilterForm', TLFilterForm);
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

/** Mounts the given control with the given state; the texts are the defaults of the control. */
function mountRoot(module: string, state: Record<string, unknown>): void {
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  const element = document.createElement('div');
  element.id = ROOT_ID;
  document.body.appendChild(element);
  act(() => mount(ROOT_ID, module, state, 'w', '/ctx'));
}

async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** The buttons of the footer, in their order. */
function footerButtons(): HTMLButtonElement[] {
  return [...document.querySelectorAll<HTMLButtonElement>('.tl-filter-list__footer > button')];
}

describe('the footer of a filter popover', () => {
  it('draws "Remove filter" and "Invert selection" of a value list as ghost buttons in body type', async () => {
    mountRoot('TLFilterList', {
      label: 'Status', searchable: false, active: false,
      entries: [{ key: '0', label: 'Offen', checked: true }, { key: '1', label: 'Erledigt', checked: true }],
    });
    await settle();

    const [clear, invert] = footerButtons();
    for (const button of [clear, invert]) {
      expect([...button.classList]).toEqual(expect.arrayContaining(['tl-button', 'tl-button--ghost', 'tl-type-body']));
      expect(button.classList).not.toContain('tl-type-label');
      // The label stands in the label element of the button, as in every button.
      expect(button.querySelector('.tl-button__label')).not.toBeNull();
    }
    expect(clear.disabled).toBe(true);
    expect(invert.disabled).toBe(false);
  });

  it('draws "Apply" of a form as the primary button at the end, after "Remove filter"', async () => {
    mountRoot('TLFilterForm', { active: true, applies: true });
    await settle();

    const [clear, apply] = footerButtons();
    expect([...clear.classList]).toEqual(expect.arrayContaining(['tl-button', 'tl-button--ghost', 'tl-type-body']));
    expect(clear.disabled).toBe(false);
    expect([...apply.classList]).toEqual(expect.arrayContaining(['tl-button', 'tl-button--primary', 'tl-type-body']));
    expect(apply.classList).not.toContain('tl-type-label');
  });
});
