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
import { register, registerPart } from 'tl-react-bridge';
import TLButton from './TLButton';
import ButtonPart from './button/ButtonPart';

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
  register('TLButton', TLButton);
});

afterEach(async () => {
  act(() => unmount(ROOT_ID));
  document.getElementById(ROOT_ID)?.remove();
  for (const release of answers) release();
  await settle();
  vi.unstubAllGlobals();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Mounts a button whose commands wait for their answer until the test releases it. */
function mountButton(): void {
  sent = [];
  answers = [];
  vi.stubGlobal('fetch', vi.fn((_url: string, init?: RequestInit) => {
    sent.push(JSON.parse(String(init?.body ?? '{}')).command);
    return new Promise<Response>((resolve) => {
      answers.push(() => resolve(new Response('{"success":true}', { status: 200 })));
    });
  }));
  const element = document.createElement('div');
  element.id = ROOT_ID;
  document.body.appendChild(element);
  act(() => mount(ROOT_ID, 'TLButton', { label: LABEL }, 'w', '/ctx'));
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
  return document.querySelector<HTMLButtonElement>(`#${ROOT_ID} button`)!;
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
