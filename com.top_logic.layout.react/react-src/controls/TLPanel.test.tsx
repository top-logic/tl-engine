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
import { mount, unmount } from '../bridge/tl-react-bridge';
import { register, registerPart, TOOLTIP_ATTR } from 'tl-react-bridge';
import TLPanel from './TLPanel';
import TLStack from './TLStack';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const PANEL_ID = 'panel';

/** The explanation of what the panel shows. */
const EXPLANATION = 'The people who contributed.';

/** The name of the help and info icon for assistive technology, the fallback of its translation. */
const HELP_NAME = 'Help';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('TLPanel', TLPanel);
  register('TLStack', TLStack);
  registerPart('Button', ButtonPart);
  // The panel fetches the labels of its buttons; the server answers with none.
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
});

afterEach(async () => {
  act(() => unmount(PANEL_ID));
  document.getElementById(PANEL_ID)?.remove();
  await settle();
});

afterAll(async () => {
  // The bridge entry adds a tooltip host to the page. It goes while the test environment exists, so that the DOM
  // observer of the bridge does not see its removal by the teardown of the environment.
  document.body.replaceChildren();
  await settle();
});

/** Lets the DOM observer of the bridge and the translations the panel asked for settle. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/** Mounts a panel around an empty column, with the given state. */
async function mountPanel(state: Record<string, unknown>): Promise<void> {
  const element = document.createElement('div');
  element.id = PANEL_ID;
  document.body.appendChild(element);
  act(() => mount(PANEL_ID, 'TLPanel', {
    child: { controlId: 'body', module: 'TLStack', state: { children: [] } },
    ...state,
  }, 'w', '/ctx'));
  await settle();
}

function root(): HTMLElement {
  return document.querySelector<HTMLElement>('.tl-panel')!;
}

function title(): HTMLElement | null {
  return root().querySelector<HTMLElement>('.tl-titlebar__title');
}

function helpText(): HTMLElement | null {
  return root().querySelector<HTMLElement>('.tl-panel__help-text');
}

describe('the explanation of a panel', () => {
  it('is the tooltip of the title, without icon while the content is not edited', async () => {
    await mountPanel({ title: 'Contributors', explanation: EXPLANATION });

    expect(title()!.getAttribute(TOOLTIP_ATTR)).toBe(`text:${EXPLANATION}`);
    expect(root().querySelector('.tl-titlebar__explanation')).toBeNull();
    expect(root().querySelector('.tl-panel__info')).toBeNull();
    expect(helpText()).toBeNull();
  });

  it('is offered by a help icon beside the title in edit mode, which discloses it as help text', async () => {
    await mountPanel({ title: 'Contributors', explanation: EXPLANATION, explanationHelp: true });

    expect(title()!.getAttribute(TOOLTIP_ATTR)).toBe(`text:${EXPLANATION}`);
    const help = root().querySelector<HTMLElement>('.tl-titlebar__explanation > .tl-titlebar__help')!;
    expect(help.tagName).toBe('BUTTON');
    expect(help.getAttribute(TOOLTIP_ATTR)).toBe(`text:${EXPLANATION}`);
    expect(help.getAttribute('aria-label')).toBe(HELP_NAME);
    expect(help.getAttribute('aria-expanded')).toBe('false');
    expect(help.getAttribute('aria-controls')).toBe(helpText()!.id);
    expect(helpText()!.hidden).toBe(true);

    act(() => help.click());

    expect(helpText()!.hidden).toBe(false);
    expect(helpText()!.textContent).toBe(EXPLANATION);
    expect(help.getAttribute('aria-expanded')).toBe('true');
  });

  it('is shown through an info icon beside the content of a panel without title', async () => {
    await mountPanel({ explanation: EXPLANATION });

    expect(root().querySelector('.tl-titlebar')).toBeNull();
    const row = root().querySelector<HTMLElement>(':scope > .tl-panel__explained')!;
    expect(row.querySelector(':scope > .tl-panel__body')).not.toBeNull();
    const icon = row.querySelector<HTMLElement>(':scope > .tl-panel__info > .tl-panel__help')!;
    expect(icon.getAttribute('role')).toBe('img');
    expect(icon.getAttribute(TOOLTIP_ATTR)).toBe(`text:${EXPLANATION}`);
    expect(icon.getAttribute('aria-label')).toBe(HELP_NAME);
  });

  it('discloses the help text from the info icon of a panel without title in edit mode', async () => {
    await mountPanel({ explanation: EXPLANATION, explanationHelp: true });

    const icon = root().querySelector<HTMLElement>('.tl-panel__info > .tl-panel__help')!;
    expect(icon.tagName).toBe('BUTTON');
    expect(helpText()!.hidden).toBe(true);

    act(() => icon.click());

    expect(helpText()!.hidden).toBe(false);
  });

  it('is absent without explanation', async () => {
    await mountPanel({ title: 'Contributors', explanationHelp: true });

    expect(title()!.hasAttribute(TOOLTIP_ATTR)).toBe(false);
    expect(root().querySelector('.tl-titlebar__explanation')).toBeNull();
    expect(root().querySelector('.tl-panel__explained')).toBeNull();
    expect(root().querySelector('.tl-panel__info')).toBeNull();
    expect(helpText()).toBeNull();
  });
});
