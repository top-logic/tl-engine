import React, { act } from 'react';
import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';

vi.mock('./sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('./sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

import { mount, unmount, useTLCommand, useTLUpload } from './tl-react-bridge';
import { register } from './registry';
import type { CommandResponse } from './command-channel';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** Name of the reply field the tests' server answers with. */
const FIELD_ID = 'id';

/** The command and upload functions the mounted probe component received from the bridge. */
const hooks: {
  command?: ReturnType<typeof useTLCommand>;
  upload?: ReturnType<typeof useTLUpload>;
} = {};

/** A component handing out the command and upload functions of its control. */
const Probe: React.FC = () => {
  hooks.command = useTLCommand();
  hooks.upload = useTLUpload();
  return null;
};

const CONTROL_ID = 'probe1';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('TestReplyProbe', Probe);
  const element = document.createElement('div');
  element.id = CONTROL_ID;
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TestReplyProbe', {}, 'w', '/ctx'));
});

afterEach(() => {
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

/** Lets the server answer every request with the given body and status. */
function answer(body: string, status = 200): void {
  vi.stubGlobal('fetch', vi.fn(async () => new Response(body, { status })));
}

describe('the command function of useTLCommand', () => {
  it('resolves with the reply fields of the answer', async () => {
    answer('{"success":true,"id":"s-17","count":3}');

    const result: CommandResponse | undefined = await hooks.command!('create', { name: 'x' });

    expect(result?.success).toBe(true);
    expect(result?.[FIELD_ID]).toBe('s-17');
    expect(result?.count).toBe(3);
  });

  it('resolves with the bare answer of a command without reply', async () => {
    answer('{"success":true}');

    await expect(hooks.command!('click')).resolves.toEqual({ success: true });
  });

  it('resolves with undefined for a command answered with an error status', async () => {
    answer('{"success":false,"error":"Control not found: probe1"}', 404);
    vi.spyOn(console, 'error').mockImplementation(() => {});

    await expect(hooks.command!('click')).resolves.toBeUndefined();
  });
});

describe('the upload function of useTLUpload', () => {
  it('resolves with the reply fields of the answer', async () => {
    answer('{"success":true,"id":"f-4"}');

    const formData = new FormData();
    formData.append('file', new Blob(['data']), 'a.txt');
    const result = await hooks.upload!(formData);

    expect(result?.[FIELD_ID]).toBe('f-4');
  });

  it('resolves with undefined for a refused upload', async () => {
    answer('{"success":false,"error":"too large"}', 413);
    vi.spyOn(console, 'error').mockImplementation(() => {});

    await expect(hooks.upload!(new FormData())).resolves.toBeUndefined();
  });
});

afterAll(() => {
  act(() => unmount(CONTROL_ID));
  document.getElementById(CONTROL_ID)?.remove();
});
