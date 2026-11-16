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
import { pushTrap } from './focus-trap';

const CONTROL_ID = 'table1';

/** The form a Save button stores. */
const FORM_ID = 'form1';

/** The Save button, in a toolbar outside of the form. */
const SAVE_ID = 'save1';

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

/**
 * Builds a toolbar holding the Save button and, beside it, a form: a valid field, then two fields
 * showing an error. Also renders the control of {@link render} with fields showing an error.
 */
function renderForm(): { save: HTMLButtonElement; valid: HTMLInputElement; first: HTMLInputElement } {
  render();
  const toolbar = document.createElement('div');
  const save = document.createElement('button');
  save.id = SAVE_ID;
  toolbar.appendChild(save);
  const form = document.createElement('div');
  form.id = FORM_ID;
  const valid = document.createElement('input');
  const first = document.createElement('input');
  first.setAttribute('aria-invalid', 'true');
  const second = document.createElement('input');
  second.setAttribute('aria-invalid', 'true');
  form.append(valid, first, second);
  document.body.append(toolbar, form);
  return { save, valid, first };
}

/** A refusal of a save because of the errors of the form {@link FORM_ID}. */
const FORM_REFUSAL =
  `{"success":false,"errorCode":"invalid-input","invalidInputScope":"${FORM_ID}","error":"Fix the errors."}`;

/** Resolves after the page has rendered the given number of frames. */
function frames(count: number): Promise<void> {
  return new Promise((resolve) => {
    const step = (left: number): void => {
      if (left === 0) {
        resolve();
      } else {
        requestAnimationFrame(() => step(left - 1));
      }
    };
    step(count);
  });
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

describe('a refused save', () => {
  it('focuses the first field showing an error in the form the refusal names', async () => {
    const { save, first } = renderForm();
    save.focus();
    seq = 3;
    answer(FORM_REFUSAL, 3);

    await enqueueCommand('/cmd', { controlId: SAVE_ID, command: 'click', windowName: 'w', arguments: {} });

    await vi.waitFor(() => expect(document.activeElement).toBe(first));
  });

  it('leaves the focus after another failure of the save', async () => {
    const { save } = renderForm();
    save.focus();
    seq = 3;
    answer(`{"success":false,"invalidInputScope":"${FORM_ID}","error":"Storage is full."}`, 3);

    await enqueueCommand('/cmd', { controlId: SAVE_ID, command: 'click', windowName: 'w', arguments: {} });
    await frames(2);

    expect(document.activeElement).toBe(save);
  });

  it('focuses the field once the modal dialog covering the form has closed', async () => {
    const { first, save } = renderForm();
    const dialog = document.createElement('div');
    const dialogSave = document.createElement('button');
    dialog.appendChild(dialogSave);
    document.body.appendChild(dialog);
    const release = pushTrap(() => dialog);
    dialogSave.focus();
    seq = 3;
    answer(FORM_REFUSAL, 3);

    await enqueueCommand('/cmd', { controlId: 'dialogSave', command: 'click', windowName: 'w', arguments: {} });
    await frames(3);
    expect(document.activeElement, 'The dialog keeps the focus while it is open.').toBe(dialogSave);

    // The dialog closes: it releases its trap and gives the focus back to where it was.
    release();
    dialog.remove();
    save.focus();

    await vi.waitFor(() => expect(document.activeElement).toBe(first));
  });

  it('leaves the focus when the form shows no error after the dialog closed', async () => {
    const { first, save } = renderForm();
    const dialog = document.createElement('div');
    document.body.appendChild(dialog);
    const release = pushTrap(() => dialog);
    seq = 3;
    answer(FORM_REFUSAL, 3);

    await enqueueCommand('/cmd', { controlId: 'dialogSave', command: 'click', windowName: 'w', arguments: {} });
    await frames(3);

    // The input was discarded in the dialog.
    document.getElementById(FORM_ID)!.querySelectorAll('[aria-invalid]').forEach((e) => e.removeAttribute('aria-invalid'));
    release();
    dialog.remove();
    save.focus();
    await frames(3);

    expect(document.activeElement).toBe(save);
    expect(first.hasAttribute('aria-invalid')).toBe(false);
  });
});
