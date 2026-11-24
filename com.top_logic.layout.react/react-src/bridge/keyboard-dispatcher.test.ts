import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { addBinding, type GestureHandler, initKeyboardDispatcher, pageScope } from './keyboard-dispatcher';

/** Presses the given key on the focused element, as the browser does, bubbling to the document. */
function press(key: string): KeyboardEvent {
  const event = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true });
  (document.activeElement ?? document.body).dispatchEvent(event);
  return event;
}

describe('keyboard dispatcher', () => {
  let handler: ReturnType<typeof vi.fn<GestureHandler>>;
  let removers: Array<() => void>;

  beforeEach(() => {
    initKeyboardDispatcher();
    document.body.innerHTML = '';
    handler = vi.fn<GestureHandler>(() => true);
    removers = [addBinding(pageScope(), 'Delete', handler), addBinding(pageScope(), 'Backspace', handler)];
  });

  afterEach(() => {
    removers.forEach(r => r());
    document.body.innerHTML = '';
  });

  it.each(['Delete', 'Backspace'])('leaves %s in a text field to the field', key => {
    const field = document.createElement('input');
    field.type = 'text';
    document.body.appendChild(field);
    field.focus();

    const event = press(key);

    expect(handler).not.toHaveBeenCalled();
    expect(event.defaultPrevented).toBe(false);
  });

  it('leaves Delete in a multi-line text to the text', () => {
    const area = document.createElement('textarea');
    document.body.appendChild(area);
    area.focus();

    press('Delete');

    expect(handler).not.toHaveBeenCalled();
  });

  it('runs the binding of Delete outside a text field', () => {
    const button = document.createElement('button');
    document.body.appendChild(button);
    button.focus();

    const event = press('Delete');

    expect(handler).toHaveBeenCalledTimes(1);
    expect(event.defaultPrevented).toBe(true);
  });

  it('runs the binding of Delete in a checkbox', () => {
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    document.body.appendChild(checkbox);
    checkbox.focus();

    press('Delete');

    expect(handler).toHaveBeenCalledTimes(1);
  });
});
