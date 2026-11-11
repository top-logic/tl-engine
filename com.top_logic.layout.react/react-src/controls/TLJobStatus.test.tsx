import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// The error of a failed job, with the problems behind it listed below.

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLJobStatus from './TLJobStatus';
import ButtonPart from './button/ButtonPart';
import ProgressBarPart from './progress/ProgressBarPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'job';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  registerPart('Button', ButtonPart);
  registerPart('ProgressBar', ProgressBarPart);
  register('TLJobStatus', TLJobStatus);
});

afterEach(() => {
  act(() => unmount(CONTROL_ID));
  document.getElementById(CONTROL_ID)?.remove();
  vi.unstubAllGlobals();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Mounts a job status with the given state and answers the element it is drawn into. */
function mountJobStatus(state: Record<string, unknown>): HTMLElement {
  // The translations of the control's texts are fetched from the server; the test answers none.
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  const element = document.createElement('div');
  element.id = CONTROL_ID;
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TLJobStatus', state, 'w', '/ctx'));
  return document.getElementById(CONTROL_ID)!;
}

describe('the error of a failed job', () => {
  it('lists the problems behind the error inside the alert, in their order', () => {
    const root = mountJobStatus({
      status: 'failed',
      error: 'Cannot store',
      errorDetails: ['Value too large', 'Name missing'],
    });

    const alert = root.querySelector('.tl-job-status__error')!;
    expect(alert.getAttribute('role')).toBe('alert');
    const items = Array.from(alert.querySelectorAll('ul.tl-job-status__details > li')).map(li => li.textContent);
    expect(items).toEqual(['Value too large', 'Name missing']);
    expect(alert.textContent).toBe('Cannot storeValue too largeName missing');
  });

  it('draws no list for an error without details', () => {
    const root = mountJobStatus({ status: 'failed', error: 'Connection lost', errorDetails: [] });

    const alert = root.querySelector('.tl-job-status__error')!;
    expect(alert.textContent).toBe('Connection lost');
    expect(alert.querySelector('.tl-job-status__details')).toBeNull();
  });
});
