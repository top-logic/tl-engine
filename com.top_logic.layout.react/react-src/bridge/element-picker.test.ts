import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('./tl-react-bridge', () => ({
  getApiBase: () => '/ctx/',
  isMountedControl: () => false,
}));

import { handlePick } from './element-picker';

/** The view file the element the test picks belongs to. */
const VIEW_SOURCE = '/WEB-INF/views/projects/milestone-list.view.xml';

let fetchMock: ReturnType<typeof vi.fn>;

/** A view of the page, holding one button. */
function viewWithButton(disabled: boolean): HTMLButtonElement {
  const view = document.createElement('div');
  view.dataset.viewSource = VIEW_SOURCE;
  const button = document.createElement('button');
  button.disabled = disabled;
  view.appendChild(button);
  document.body.appendChild(view);
  return button;
}

function press(el: Element): void {
  el.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, cancelable: true, button: 0 }));
}

function release(el: Element): void {
  el.dispatchEvent(new PointerEvent('pointerup', { bubbles: true, cancelable: true, button: 0 }));
}

function click(el: Element): void {
  el.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, button: 0 }));
}

/** The view sources the picker reported to the server. */
function reportedPaths(): string[] {
  return fetchMock.mock.calls.map(call => JSON.parse((call[1] as RequestInit).body as string).path);
}

describe('element picker', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    fetchMock = vi.fn(() => Promise.resolve(new Response('{}')));
    vi.stubGlobal('fetch', fetchMock);
    handlePick({ token: 't1', targetWindowId: '', kind: 'view' });
  });

  afterEach(() => {
    vi.runAllTimers();
    vi.useRealTimers();
    vi.unstubAllGlobals();
    document.body.innerHTML = '';
  });

  it('picks a disabled element, which receives a press but no click', () => {
    const button = viewWithButton(true);

    press(button);
    release(button);
    vi.runAllTimers();

    expect(reportedPaths()).toEqual([VIEW_SOURCE]);
  });

  it('does not execute the picked element', () => {
    const button = viewWithButton(false);
    const executed = vi.fn();
    button.addEventListener('click', executed);

    press(button);
    release(button);
    click(button);
    vi.runAllTimers();

    expect(reportedPaths()).toEqual([VIEW_SOURCE]);
    expect(executed).not.toHaveBeenCalled();
  });

  it('lets the clicks after the pick through', () => {
    const disabled = viewWithButton(true);
    const other = viewWithButton(false);
    const executed = vi.fn();
    other.addEventListener('click', executed);

    // The press of the disabled element ends without a click of its own.
    press(disabled);
    release(disabled);
    vi.runAllTimers();

    click(other);

    expect(executed).toHaveBeenCalledTimes(1);
    expect(reportedPaths()).toEqual([VIEW_SOURCE]);
  });

  it('ignores a press of another button', () => {
    const button = viewWithButton(false);

    button.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, cancelable: true, button: 2 }));

    expect(fetchMock).not.toHaveBeenCalled();
    press(button);
    expect(reportedPaths()).toEqual([VIEW_SOURCE]);
  });
});
