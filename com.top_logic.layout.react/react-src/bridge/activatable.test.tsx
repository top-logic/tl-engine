import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ACTIVATABLE_CLASS, ACTIVATION_ACTION_CLASS, ActivationButton, ActivationOpenButton, activationProps } from './activatable';
import type { ActivationAction, ActivationMode } from './activatable';
import { registerPart } from './parts';

// The button part as a plain button, named by its label.
registerPart('Button', ({ label, disabled, onClick }) => (
  <button type="button" aria-label={label} disabled={disabled} onClick={onClick} />
));

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** An action the element offers. */
const OPEN: ActivationAction = { label: 'Open project', disabled: false, tooltip: null };

/** An action that is currently refused. */
const REFUSED: ActivationAction = { label: 'Open project', disabled: true, tooltip: 'Locked.' };

let root: Root;

let container: HTMLElement;

let activate: ReturnType<typeof vi.fn<() => void>>;

beforeEach(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  container = document.createElement('div');
  document.body.appendChild(container);
  root = createRoot(container);
  activate = vi.fn<() => void>();
});

afterEach(() => {
  act(() => root.unmount());
  container.remove();
  window.getSelection()?.removeAllRanges();
});

/**
 * Renders an element made openable as a list item or a tile is: with its hidden button in the
 * `click` mode, its open button in the `double` mode, and content holding text, a button and a field.
 */
function render(action: ActivationAction | null, mode: ActivationMode, enabled = true): HTMLElement {
  const Element = () => {
    const props = activationProps(action, activate, mode, enabled);
    // In the double mode, the element is an option of a list box, interactive by its role.
    return (
      <div id="element" tabIndex={0} role={mode === 'double' ? 'option' : undefined} {...props}>
        {action && mode === 'click' && <ActivationButton action={action} activate={activate} enabled={enabled} />}
        <span id="text">Project Alpha</span>
        <button type="button" id="inner">Like</button>
        <input id="field" />
        {action && mode === 'double' && <ActivationOpenButton action={action} activate={activate} />}
      </div>
    );
  };
  act(() => root.render(<Element />));
  return document.getElementById('element')!;
}

function byId(id: string): HTMLElement {
  return document.getElementById(id)!;
}

function click(target: Element, init: MouseEventInit = {}): void {
  act(() => {
    target.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, detail: 1, ...init }));
  });
}

function doubleClick(target: Element): void {
  act(() => {
    target.dispatchEvent(new MouseEvent('dblclick', { bubbles: true, cancelable: true, detail: 2 }));
  });
}

/** Dispatches a key press and tells whether it was handled (its default prevented). */
function key(target: Element, name: string): boolean {
  const event = new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true });
  act(() => {
    target.dispatchEvent(event);
  });
  return event.defaultPrevented;
}

/** Marks the text of the given element, as the user does by dragging over it. */
function selectText(element: Element): void {
  const range = document.createRange();
  range.selectNodeContents(element);
  const selection = window.getSelection()!;
  selection.removeAllRanges();
  selection.addRange(range);
}

describe('activationProps in the click mode', () => {
  it('opens the element by a click anywhere on it', () => {
    const element = render(OPEN, 'click');
    expect(element.classList).toContain(ACTIVATABLE_CLASS);

    click(byId('text'));
    expect(activate).toHaveBeenCalledTimes(1);
  });

  it('leaves a click on interactive content to that content', () => {
    render(OPEN, 'click');

    click(byId('inner'));
    click(byId('field'));
    expect(activate).not.toHaveBeenCalled();
  });

  it('does not open the element by a click ending a text selection', () => {
    render(OPEN, 'click');

    selectText(byId('text'));
    click(byId('text'));
    expect(activate).not.toHaveBeenCalled();

    window.getSelection()!.removeAllRanges();
    click(byId('text'));
    expect(activate).toHaveBeenCalledTimes(1);
  });

  it('is operated by the keyboard through its hidden button, opening it once', () => {
    const element = render(OPEN, 'click');
    const button = element.querySelector<HTMLButtonElement>(`:scope > .${ACTIVATION_ACTION_CLASS}`)!;
    expect(button.classList).toContain('tl-visually-hidden');
    expect(button.tabIndex).toBe(0);
    expect(button.textContent).toBe(OPEN.label);

    // Enter and Space on a button press it, which is this click.
    click(button);
    expect(activate).toHaveBeenCalledTimes(1);
  });

  it('shows a refused action and does nothing when pressed', () => {
    // The stylesheet reads the refusal from the disabled button: no pointer, no highlight.
    const element = render(REFUSED, 'click');
    const button = element.querySelector<HTMLButtonElement>(`:scope > .${ACTIVATION_ACTION_CLASS}`)!;
    expect(button.disabled).toBe(true);

    click(byId('text'));
    expect(activate).not.toHaveBeenCalled();
  });

  it('offers nothing while not enabled, but keeps its tooltip', () => {
    const element = render(REFUSED, 'click', false);
    expect(element.classList).not.toContain(ACTIVATABLE_CLASS);
    expect(element.getAttributeNames().some(name => element.getAttribute(name)?.includes('Locked.'))).toBe(true);

    click(byId('text'));
    expect(activate).not.toHaveBeenCalled();
  });

  it('has no button to focus or press while not enabled', () => {
    const element = render(OPEN, 'click', false);
    expect(element.querySelector(`.${ACTIVATION_ACTION_CLASS}`)).toBeNull();
    expect(Array.from(element.querySelectorAll('button')).map(b => b.textContent)).toEqual(['Like']);

    click(byId('text'));
    expect(key(element, 'Enter')).toBe(false);
    expect(activate).not.toHaveBeenCalled();
  });

  it('gives an element without action nothing', () => {
    const element = render(null, 'click');
    expect(element.classList).not.toContain(ACTIVATABLE_CLASS);

    click(byId('text'));
    expect(activate).not.toHaveBeenCalled();
  });
});

describe('activationProps in the double mode', () => {
  it('leaves a click to the selection and opens the element by a double-click', () => {
    const element = render(OPEN, 'double');
    expect(element.classList).not.toContain(ACTIVATABLE_CLASS);

    click(byId('text'));
    expect(activate).not.toHaveBeenCalled();

    doubleClick(byId('text'));
    expect(activate).toHaveBeenCalledTimes(1);
  });

  it('leaves a double-click on interactive content to that content', () => {
    render(OPEN, 'double');

    doubleClick(byId('field'));
    expect(activate).not.toHaveBeenCalled();
  });

  it('keeps the second press of a double-click from selecting a word', () => {
    render(OPEN, 'double');
    const event = new MouseEvent('mousedown', { bubbles: true, cancelable: true, detail: 2 });
    act(() => {
      byId('text').dispatchEvent(event);
    });
    expect(event.defaultPrevented).toBe(true);
  });

  it('opens the focused element by Enter, and leaves Space and keys of its content to others', () => {
    const element = render(OPEN, 'double');

    expect(key(element, ' ')).toBe(false);
    expect(key(byId('field'), 'Enter')).toBe(false);
    expect(activate).not.toHaveBeenCalled();

    expect(key(element, 'Enter')).toBe(true);
    expect(activate).toHaveBeenCalledTimes(1);
  });

  it('opens the element by its open button', () => {
    const element = render(OPEN, 'double');
    const open = element.querySelector('.tl-overlay__layer--top-right .tl-overlay__action button')!;
    expect(open.getAttribute('aria-label')).toBe(OPEN.label);

    click(open);
    expect(activate).toHaveBeenCalledTimes(1);
  });

  it('leaves Enter on a refused element to the selection, and disables its open button', () => {
    const element = render(REFUSED, 'double');

    expect(key(element, 'Enter')).toBe(false);
    doubleClick(byId('text'));
    expect(activate).not.toHaveBeenCalled();

    const open = element.querySelector<HTMLButtonElement>('.tl-overlay__action button')!;
    expect(open.disabled).toBe(true);
  });
});
