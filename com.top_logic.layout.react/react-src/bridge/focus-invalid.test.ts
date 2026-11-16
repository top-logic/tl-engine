import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

/** The number of the last event the tests' stream has brought. */
let seq = 0;

vi.mock('./sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('./sse-client')>()),
  connect: () => {},
  expectEvents: () => {},
  lastEventSeq: () => seq,
}));

import { enqueueCommand } from './command-channel';
import { firstInvalidField, focusFirstInvalidField } from './focus-invalid';

const CONTROL_ID = 'table1';

/** Builds the control element: a valid field, then two fields showing an error. */
function render(): { valid: HTMLInputElement; first: HTMLInputElement; second: HTMLInputElement } {
  document.body.innerHTML = '';
  const outside = document.createElement('input');
  outside.setAttribute('aria-invalid', 'true');
  document.body.appendChild(outside);
  const control = document.createElement('div');
  control.id = CONTROL_ID;
  const valid = document.createElement('input');
  const first = document.createElement('input');
  first.setAttribute('aria-invalid', 'true');
  const second = document.createElement('input');
  second.setAttribute('aria-invalid', 'true');
  control.append(valid, first, second);
  document.body.appendChild(control);
  return { valid, first, second };
}

/** Lets the server answer every command with the given body, naming the given last event. */
function answer(body: string, lastEvent: number): void {
  vi.stubGlobal(
    'fetch',
    vi.fn(async () => new Response(body, { status: 200, headers: { 'X-TL-Last-Event': String(lastEvent) } })),
  );
}

beforeEach(() => {
  seq = 0;
});

afterEach(() => {
  vi.unstubAllGlobals();
  document.body.innerHTML = '';
});

describe('focusFirstInvalidField', () => {
  it('finds the first field showing an error within the control', () => {
    const { first } = render();
    expect(firstInvalidField(CONTROL_ID)).toBe(first);
  });

  it('focuses the first field showing an error within the control', async () => {
    const { first } = render();
    focusFirstInvalidField(CONTROL_ID);
    await vi.waitFor(() => expect(document.activeElement).toBe(first));
  });

  it('waits for the events of the refusal', async () => {
    const { first, valid } = render();
    valid.focus();
    focusFirstInvalidField(CONTROL_ID, 5);
    // A few frames pass without the events: the focus stays.
    await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    expect(document.activeElement).toBe(valid);

    seq = 5;
    await vi.waitFor(() => expect(document.activeElement).toBe(first));
  });

  it('takes a field that shows its error only after the events were rendered', async () => {
    const { valid } = render();
    document.getElementById(CONTROL_ID)!.querySelectorAll('[aria-invalid]').forEach((e) => e.remove());
    focusFirstInvalidField(CONTROL_ID);
    await new Promise((resolve) => requestAnimationFrame(resolve));
    valid.setAttribute('aria-invalid', 'true');
    await vi.waitFor(() => expect(document.activeElement).toBe(valid));
  });
});

describe('a refused command', () => {
  it('focuses the first field showing an error after a refusal because of invalid input', async () => {
    const { first } = render();
    seq = 3;
    answer('{"success":false,"errorCode":"invalid-input","error":"Fix the row."}', 3);

    await enqueueCommand('/cmd', { controlId: CONTROL_ID, command: 'select', windowName: 'w', arguments: {} });

    await vi.waitFor(() => expect(document.activeElement).toBe(first));
  });

  it('leaves the focus after any other failure', async () => {
    const { valid } = render();
    valid.focus();
    seq = 3;
    answer('{"success":false,"error":"Storage is full."}', 3);

    await enqueueCommand('/cmd', { controlId: CONTROL_ID, command: 'select', windowName: 'w', arguments: {} });
    await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));

    expect(document.activeElement).toBe(valid);
  });
});
