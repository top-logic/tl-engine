import React, { act, useState } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { useListReorder } from './list-reorder';
import type { ListReorder, ListReorderOptions, ReorderSide } from './list-reorder';

// The labels as given: no server to translate them.
vi.mock('./i18n', () => ({ useI18N: (keys: Record<string, string>) => keys }));

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** What the hook handed back at the last render of the list under test. */
let latest: ListReorder;

/** Shows the list under test in a new order, as a control does when the server's order arrives. */
let setOrder: (items: string[]) => void;

/** The items of the list under test, by name, in the order of its last render. */
let currentItems: string[];

interface ListProps {

  /** The items, by name, in their order at mount time. */
  initial: string[];

  /** Elements standing for a zone rather than an item (a placeholder of an empty zone), after the items. */
  zones?: string[];

  /** Whether the items are keyed by name (the default) or by position. */
  keyed?: boolean;

  /** Options of the hook beyond the ones the list fills in itself. */
  options?: Partial<ListReorderOptions>;
}

/**
 * A list arranged with the hook, as a control renders one: each item with its handle, the zone elements after the
 * items, the announcer next to the list.
 */
function List({ initial, zones = [], keyed = true, options = {} }: ListProps) {
  const [items, setItems] = useState(initial);
  setOrder = setItems;
  currentItems = items;
  const reorder = useListReorder({
    onMove: () => {},
    count: items.length,
    labelOf: (index) => items[index],
    ...(keyed ? { keyOf: (index: number) => items[index] } : {}),
    ...options,
  });
  latest = reorder;
  return (
    <div data-container="" {...reorder.containerProps}>
      <ul>
        {items.map((item, index) => (
          <li key={keyed ? item : index} data-item={item} {...reorder.itemProps(index)}>
            <button type="button" data-handle={item} {...reorder.handleProps(index)}>{item}</button>
          </li>
        ))}
        {zones.map((zone) => (
          <li key={'zone-' + zone} data-zone={zone} {...reorder.zoneProps(zone)}>{zone}</li>
        ))}
      </ul>
      {reorder.announcer}
    </div>
  );
}

/** The items of a list in two sections and the number of items in the first one. */
interface Split {
  items: string[];
  split: number;
}

/** The list in two sections under test, as of its last render. */
let currentSplit: Split;

/** Shows the list in two sections under test with new items or a new line between its sections. */
let setSplit: (next: Split) => void;

/**
 * A list in two sections, each an element of its own, keyed by name: an item moved across the line between them is a
 * new element in the other section, as in a control listing its sections one after the other.
 */
function Sections({ initial, options = {} }: { initial: Split; options?: Partial<ListReorderOptions> }) {
  const [state, setState] = useState(initial);
  currentSplit = state;
  setSplit = setState;
  const reorder = useListReorder({
    onMove: () => {},
    count: state.items.length,
    labelOf: (index) => state.items[index],
    keyOf: (index) => state.items[index],
    ...options,
  });
  latest = reorder;
  const row = (name: string, index: number) => (
    <li key={name} data-item={name} {...reorder.itemProps(index)}>
      <button type="button" data-handle={name} {...reorder.handleProps(index)}>{name}</button>
    </li>
  );
  return (
    <div {...reorder.containerProps}>
      <ul data-section="first">{state.items.slice(0, state.split).map((name, index) => row(name, index))}</ul>
      <ul data-section="second">
        {state.items.slice(state.split).map((name, index) => row(name, index + state.split))}
      </ul>
      {reorder.announcer}
    </div>
  );
}

/**
 * A row of headings as the table header renders it: the item takes Alt + arrow from everything inside it, the first
 * button is the handle the focus returns to, a second button (a filter, a width handle) stands beside it.
 */
function Headings({ initial, options = {} }: { initial: string[]; options?: Partial<ListReorderOptions> }) {
  const [items, setItems] = useState(initial);
  setOrder = setItems;
  const reorder = useListReorder({
    onMove: () => {},
    count: items.length,
    labelOf: (index) => items[index],
    keyOf: (index) => items[index],
    ...options,
  });
  return (
    <div>
      {items.map((name, index) => {
        const { ref, onKeyDown } = reorder.handleProps(index);
        return (
          <div key={name} data-item={name} onKeyDown={onKeyDown}>
            <button type="button" data-handle={name} ref={ref}>{name}</button>
            <button type="button" data-beside={name}>{name} filter</button>
          </div>
        );
      })}
      {reorder.announcer}
    </div>
  );
}

let container: HTMLElement;
let root: Root;

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
});

beforeEach(() => {
  container = document.createElement('div');
  document.body.appendChild(container);
  root = createRoot(container);
});

afterEach(() => {
  act(() => root.unmount());
  container.remove();
});

function render(props: ListProps): void {
  act(() => root.render(<List {...props} />));
}

/** The `dataTransfer` of a drag, as far as the hook reads and writes it. */
interface Transfer {
  effectAllowed: string;
  dropEffect: string;
  setData(type: string, data: string): void;
}

function transfer(): Transfer {
  return { effectAllowed: 'uninitialized', dropEffect: 'none', setData() {} };
}

/** A drag event with the given transfer; jsdom has no drag events, a mouse event carries the position. */
function dragEvent(type: string, data: Transfer, clientY = 0): MouseEvent {
  const event = new MouseEvent(type, { bubbles: true, cancelable: true, clientY });
  Object.defineProperty(event, 'dataTransfer', { value: data });
  return event;
}

function fire<E extends Event>(target: Element, event: E): E {
  act(() => {
    target.dispatchEvent(event);
  });
  return event;
}

function item(name: string): HTMLElement {
  return container.querySelector(`[data-item="${name}"]`) as HTMLElement;
}

function zone(name: string): HTMLElement {
  return container.querySelector(`[data-zone="${name}"]`) as HTMLElement;
}

function handle(name: string): HTMLElement {
  return container.querySelector(`[data-handle="${name}"]`) as HTMLElement;
}

/** The names of the items in the order the list shows them. */
function shown(): string[] {
  return [...container.querySelectorAll('[data-item]')].map((element) => element.getAttribute('data-item')!);
}

function announced(): string {
  return container.querySelector('[role="status"]')!.textContent ?? '';
}

/** Picks the named item up; the hook marks it dragged once the drag image is taken. */
async function pickUp(name: string, data: Transfer): Promise<void> {
  fire(item(name), dragEvent('dragstart', data));
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 0));
  });
}

/** Moves the pointer over the given element: into its front half for `before`, its back half for `after`. */
function over(element: HTMLElement, data: Transfer, side: ReorderSide = 'after'): MouseEvent {
  element.getBoundingClientRect = () => ({ top: 0, height: 20, left: 0, width: 100 }) as DOMRect;
  return fire(element, dragEvent('dragover', data, side === 'before' ? 2 : 18));
}

function drop(element: HTMLElement, data: Transfer): void {
  fire(element, dragEvent('drop', data));
}

/** Alt + arrow on the handle of the named item, which has the focus. */
function press(name: string, key: 'ArrowUp' | 'ArrowDown'): void {
  fire(handle(name), new KeyboardEvent('keydown', { key, altKey: true, bubbles: true, cancelable: true }));
}

/** The given items with the one at `from` moved to `to`. */
function moved(items: string[], from: number, to: number): string[] {
  const result = [...items];
  const [entry] = result.splice(from, 1);
  result.splice(to, 0, entry);
  return result;
}

describe('dropping on an item', () => {
  it('passes where the item was dropped along with the move', async () => {
    const onMove = vi.fn();
    render({ initial: ['a', 'b', 'c', 'd'], options: { onMove } });
    const data = transfer();

    await pickUp('a', data);
    expect(latest.dragIndex).toBe(0);
    over(item('c'), data, 'after');
    expect(latest.dropTarget).toEqual({ index: 2, side: 'after' });
    drop(item('c'), data);

    expect(onMove).toHaveBeenCalledTimes(1);
    expect(onMove).toHaveBeenCalledWith(0, 2, { index: 2, side: 'after' });
    expect(latest.dragIndex).toBeNull();
    expect(latest.dropTarget).toBeNull();
  });

  it('reports no move for a drop that keeps the item at its position', async () => {
    const onMove = vi.fn();
    render({ initial: ['a', 'b', 'c'], options: { onMove } });
    const data = transfer();

    await pickUp('b', data);
    over(item('a'), data, 'after');
    drop(item('a'), data);

    expect(onMove).not.toHaveBeenCalled();
  });

  describe('at the line between two blocks', () => {
    // Block A: a, b; block B: c, d. After the last of A and before the first of B is one position.
    const initial = ['a', 'b', 'c', 'd'];

    it('reports the drop at the item\'s own position where the list asks for it', async () => {
      const onMove = vi.fn();
      render({ initial, options: { onMove, reportUnmoved: true } });
      const data = transfer();

      // The first of block B after the last of block A.
      await pickUp('c', data);
      over(item('b'), data, 'after');
      drop(item('b'), data);
      expect(onMove).toHaveBeenCalledTimes(1);
      expect(onMove).toHaveBeenCalledWith(2, 2, { index: 1, side: 'after' });

      // The last of block A before the first of block B.
      await pickUp('b', data);
      over(item('c'), data, 'before');
      drop(item('c'), data);
      expect(onMove).toHaveBeenCalledTimes(2);
      expect(onMove).toHaveBeenLastCalledWith(1, 1, { index: 2, side: 'before' });
    });

    it('reports no drop at the item\'s own position by default', async () => {
      const onMove = vi.fn();
      render({ initial, options: { onMove } });
      const data = transfer();

      await pickUp('c', data);
      over(item('b'), data, 'after');
      drop(item('b'), data);

      expect(onMove).not.toHaveBeenCalled();
    });

    it('leaves the keyboard steps as they are', () => {
      const onMove = vi.fn();
      render({ initial, options: { onMove, reportUnmoved: true } });

      handle('a').focus();
      press('a', 'ArrowUp');
      expect(onMove).not.toHaveBeenCalled();

      handle('c').focus();
      press('c', 'ArrowUp');
      expect(onMove).toHaveBeenCalledTimes(1);
      expect(onMove).toHaveBeenCalledWith(2, 1);
    });
  });
});

describe('zones', () => {
  const initial = ['a', 'b', 'c', 'h', 'i'];
  const zoneOf = (index: number) => (index >= 3 ? 'hidden' : null);

  it('makes the zone the target while the pointer is over one of its items, without an insertion mark', async () => {
    const onMove = vi.fn();
    const onZoneDrop = vi.fn();
    render({ initial, options: { onMove, zoneOf, acceptsZone: () => true, onZoneDrop } });
    const data = transfer();

    await pickUp('a', data);
    const event = over(item('h'), data, 'before');

    expect(event.defaultPrevented).toBe(true);
    expect(data.dropEffect).toBe('move');
    expect(latest.dropZone).toBe('hidden');
    expect(latest.dropTarget).toBeNull();
    expect(latest.itemState(3)).toEqual({ dragging: false, dropBefore: false, dropAfter: false });

    drop(item('h'), data);
    expect(onZoneDrop).toHaveBeenCalledTimes(1);
    expect(onZoneDrop).toHaveBeenCalledWith(0, 'hidden');
    expect(onMove).not.toHaveBeenCalled();
    expect(latest.dropZone).toBeNull();
    expect(latest.dragIndex).toBeNull();
  });

  it('refuses a zone that does not take the dragged item: no drop, the pointer shows none', async () => {
    const acceptsZone = vi.fn(() => false);
    render({ initial, options: { zoneOf, acceptsZone, onZoneDrop: () => {} } });
    const data = transfer();

    await pickUp('b', data);
    const event = over(item('i'), data);

    expect(acceptsZone).toHaveBeenCalledWith(1, 'hidden');
    expect(event.defaultPrevented).toBe(false);
    expect(data.dropEffect).toBe('none');
    expect(latest.dropZone).toBeNull();
    expect(latest.dropTarget).toBeNull();
  });

  it('takes no drop into a zone while the control does not handle one', async () => {
    render({ initial, options: { zoneOf } });
    const data = transfer();

    await pickUp('a', data);
    const event = over(item('h'), data);

    expect(event.defaultPrevented).toBe(false);
    expect(latest.dropZone).toBeNull();
  });

  it('takes nothing that is not dragged from the list', () => {
    render({ initial, zones: ['locked'], options: { zoneOf, onZoneDrop: () => {} } });
    const data = transfer();

    expect(over(item('h'), data).defaultPrevented).toBe(false);
    expect(over(zone('locked'), data).defaultPrevented).toBe(false);
    expect(latest.dropZone).toBeNull();
  });

  it('switches between the zone and the insertion mark as the pointer moves on', async () => {
    render({ initial, options: { zoneOf, onZoneDrop: () => {} } });
    const data = transfer();

    await pickUp('a', data);
    over(item('h'), data);
    expect(latest.dropZone).toBe('hidden');

    over(item('c'), data, 'before');
    expect(latest.dropZone).toBeNull();
    expect(latest.dropTarget).toEqual({ index: 2, side: 'before' });

    over(item('i'), data);
    expect(latest.dropZone).toBe('hidden');
    expect(latest.dropTarget).toBeNull();

    // Over itself: no target at all.
    over(item('a'), data);
    expect(latest.dropZone).toBeNull();
    expect(latest.dropTarget).toBeNull();
  });

  it('forgets the zone when the drag ends without a drop', async () => {
    const onZoneDrop = vi.fn();
    render({ initial, options: { zoneOf, onZoneDrop } });
    const data = transfer();

    await pickUp('a', data);
    over(item('h'), data);
    fire(item('a'), dragEvent('dragend', data));

    expect(latest.dropZone).toBeNull();
    expect(latest.dragIndex).toBeNull();
    expect(onZoneDrop).not.toHaveBeenCalled();
  });

  it('completes a drop released next to the items into the zone hovered last', async () => {
    const onZoneDrop = vi.fn();
    render({ initial, options: { zoneOf, onZoneDrop } });
    const data = transfer();

    await pickUp('b', data);
    over(item('h'), data);
    drop(container.querySelector('[data-container]')!, data);

    expect(onZoneDrop).toHaveBeenCalledTimes(1);
    expect(onZoneDrop).toHaveBeenCalledWith(1, 'hidden');
  });

  it('lets an element standing for a zone take the drop, if the zone takes the item', async () => {
    const onMove = vi.fn();
    const onZoneDrop = vi.fn();
    const acceptsZone = vi.fn((from: number, name: string) => name === 'locked');
    render({ initial, zones: ['locked', 'movable'], options: { onMove, zoneOf, acceptsZone, onZoneDrop } });
    const data = transfer();

    await pickUp('b', data);
    const refused = over(zone('movable'), data);
    expect(refused.defaultPrevented).toBe(false);
    expect(latest.dropZone).toBeNull();

    const taken = over(zone('locked'), data);
    expect(taken.defaultPrevented).toBe(true);
    expect(data.dropEffect).toBe('move');
    expect(latest.dropZone).toBe('locked');
    expect(latest.dropTarget).toBeNull();

    drop(zone('locked'), data);
    expect(onZoneDrop).toHaveBeenCalledTimes(1);
    expect(onZoneDrop).toHaveBeenCalledWith(1, 'locked');
    expect(onMove).not.toHaveBeenCalled();
    expect(latest.dropZone).toBeNull();
  });

  it('lets the zone decide on its own item dragged over itself', async () => {
    let taken = false;
    const acceptsZone = vi.fn(() => taken);
    render({ initial: ['a', 'h'], options: { zoneOf: (index) => (index === 1 ? 'hidden' : null), acceptsZone,
      onZoneDrop: () => {} } });
    const data = transfer();

    await pickUp('h', data);
    const refused = over(item('h'), data);
    expect(acceptsZone).toHaveBeenCalledWith(1, 'hidden');
    expect(refused.defaultPrevented).toBe(false);
    expect(latest.dropZone).toBeNull();

    taken = true;
    const accepted = over(item('h'), data);
    expect(accepted.defaultPrevented).toBe(true);
    expect(latest.dropZone).toBe('hidden');
    expect(latest.dropTarget).toBeNull();
  });

  it('gives an element standing for a zone nothing while the list is not arranged', () => {
    render({ initial, zones: ['locked'], options: { enabled: false, zoneOf, onZoneDrop: () => {} } });

    expect(latest.zoneProps('locked')).toEqual({});
  });
});

describe('keyboard step of the control', () => {
  it('is asked before the list checks the position: a step beyond the end or refused by accepts is its own', () => {
    const onMove = vi.fn();
    const onStep = vi.fn((index: number, direction: -1 | 1) => (
      { to: index, announcement: `step ${index} ${direction}` }));
    render({ initial: ['a', 'b', 'c'], options: { onMove, onStep, accepts: () => false } });

    handle('c').focus();
    press('c', 'ArrowDown');
    expect(onStep).toHaveBeenCalledWith(2, 1);
    expect(announced()).toBe('step 2 1');

    handle('a').focus();
    press('a', 'ArrowUp');
    expect(onStep).toHaveBeenLastCalledWith(0, -1);
    expect(announced()).toBe('step 0 -1');

    handle('b').focus();
    press('b', 'ArrowDown');
    expect(onStep).toHaveBeenLastCalledWith(1, 1);
    expect(announced()).toBe('step 1 1');

    expect(onMove).not.toHaveBeenCalled();
  });

  it('announces its text and returns the focus to the handle at the position it names', () => {
    let order = ['a', 'b', 'c', 'd'];
    const onMove = vi.fn();
    const onStep = vi.fn((index: number, direction: -1 | 1) => {
      // The control sends its own command and shows the item at its new place at once.
      order = moved(order, index, index + direction);
      setOrder(order);
      return { to: index + direction, announcement: `${order[index + direction]}, frozen, position ${index + direction + 1}` };
    });
    // Keyed by position: the moved item's handle is a different element than the one pressed.
    render({ initial: order, keyed: false, options: { onMove, onStep } });

    handle('b').focus();
    press('b', 'ArrowDown');

    expect(onStep).toHaveBeenCalledWith(1, 1);
    expect(shown()).toEqual(['a', 'c', 'b', 'd']);
    expect(announced()).toBe('b, frozen, position 3');
    expect(document.activeElement).toBe(handle('b'));
    expect(container.querySelectorAll('button')[2]).toBe(handle('b'));
    expect(onMove).not.toHaveBeenCalled();
  });

  it('does nothing for a step the control refuses', () => {
    const onMove = vi.fn();
    const onStep = vi.fn(() => null);
    render({ initial: ['a', 'b', 'c'], options: { onMove, onStep } });

    handle('b').focus();
    const event = new KeyboardEvent('keydown', { key: 'ArrowDown', altKey: true, bubbles: true, cancelable: true });
    fire(handle('b'), event);

    expect(onStep).toHaveBeenCalledWith(1, 1);
    expect(event.defaultPrevented).toBe(true);
    expect(onMove).not.toHaveBeenCalled();
    expect(announced()).toBe('');
    expect(shown()).toEqual(['a', 'b', 'c']);
    expect(document.activeElement).toBe(handle('b'));
  });

  it('leaves the step to the list where the control has nothing to say', () => {
    const onMove = vi.fn();
    const onStep = vi.fn(() => undefined);
    render({ initial: ['a', 'b', 'c', 'd'], options: { onMove, onStep } });

    handle('b').focus();
    press('b', 'ArrowDown');

    expect(onStep).toHaveBeenCalledWith(1, 1);
    expect(onMove).toHaveBeenCalledTimes(1);
    expect(onMove).toHaveBeenCalledWith(1, 2);
    expect(announced()).toBe('b, position 3 of 4');

    // The standard way still stops at the end of the list.
    handle('d').focus();
    press('d', 'ArrowDown');
    expect(onMove).toHaveBeenCalledTimes(1);
  });

  it('queues the steps pressed while the item waits for the server and asks the control for each', () => {
    const calls: [number, number][] = [];
    const onStep = (index: number, direction: -1 | 1) => {
      // The control sends the command; the new order comes with the server's answer.
      calls.push([index, direction]);
      return { to: index + direction, announcement: `moved to ${index + direction + 1}` };
    };
    render({ initial: ['a', 'b', 'c', 'd'], options: { onStep } });

    handle('b').focus();
    press('b', 'ArrowDown');
    press('b', 'ArrowDown');
    expect(calls).toEqual([[1, 1]]);
    expect(announced()).toBe('moved to 3');

    // The server put the item where the first step sent it: the queued step follows from there.
    act(() => setOrder(['a', 'c', 'b', 'd']));
    expect(calls).toEqual([[1, 1], [2, 1]]);
    expect(announced()).toBe('moved to 4');
    expect(document.activeElement).toBe(handle('b'));

    act(() => setOrder(['a', 'c', 'd', 'b']));
    expect(calls).toEqual([[1, 1], [2, 1]]);
    expect(document.activeElement).toBe(handle('b'));
  });

  it('asks the control for each press at once while the control shows each step at once, across a section line too',
    () => {
      const calls: [number, number][] = [];
      const onStep = (index: number, direction: -1 | 1) => {
        calls.push([index, direction]);
        const { items, split } = currentSplit;
        if (direction === 1 && index === split - 1) {
          // Down from the last of the first section: the first of the second one, at the same position.
          setSplit({ items, split: split - 1 });
          return { to: index, announcement: `${items[index]}, second section` };
        }
        setSplit({ items: moved(items, index, index + direction), split });
        return { to: index + direction, announcement: `${items[index]}, position ${index + direction + 1}` };
      };
      act(() => root.render(<Sections initial={{ items: ['a', 'b', 'c', 'd'], split: 2 }} options={{ onStep }} />));

      handle('b').focus();
      press('b', 'ArrowDown');
      // The item is a new element in the other section; the focus has followed it there.
      expect(container.querySelector('[data-section="second"] [data-item="b"]')).not.toBeNull();
      expect(document.activeElement).toBe(handle('b'));
      expect(announced()).toBe('b, second section');

      // No answer of a server in between: each press is asked from where the step before showed the item.
      press('b', 'ArrowDown');
      expect(document.activeElement).toBe(handle('b'));
      press('b', 'ArrowDown');

      expect(calls).toEqual([[1, 1], [1, 1], [2, 1]]);
      expect(currentSplit.items).toEqual(['a', 'c', 'd', 'b']);
      expect(announced()).toBe('b, position 4');
      expect(document.activeElement).toBe(handle('b'));
    });

  it('takes every press of a step in place, also when it announces the same text as the one before', () => {
    // A step across the freeze line keeps the item at its position: the order does not change, and the next render
    // (from the announcement alone) is the only one that tells the list the item stands where the step put it.
    const calls: [number, number][] = [];
    const onStep = (index: number, direction: -1 | 1) => {
      calls.push([index, direction]);
      return { to: index, announcement: 'b, position 2 of 4' };
    };
    render({ initial: ['a', 'b', 'c', 'd'], options: { onStep } });

    handle('b').focus();
    press('b', 'ArrowDown');
    press('b', 'ArrowUp');
    press('b', 'ArrowDown');

    expect(calls).toEqual([[1, 1], [1, -1], [1, 1]]);
    // The live region changes with each step, so a screen reader speaks the same text again.
    expect(announced().trim()).toBe('b, position 2 of 4');
    expect(document.activeElement).toBe(handle('b'));
  });

  it('returns the focus to the element the step was pressed on, not to the handle', () => {
    const beside = (name: string) => container.querySelector(`[data-beside="${name}"]`) as HTMLElement;
    const altKey = (target: HTMLElement, key: 'ArrowUp' | 'ArrowDown') =>
      fire(target, new KeyboardEvent('keydown', { key, altKey: true, bubbles: true, cancelable: true }));
    let inPlace = false;
    const onStep = (index: number, direction: -1 | 1) =>
      ({ to: inPlace ? index : index + direction, announcement: `step ${index} ${direction}` });
    act(() => root.render(<Headings initial={['a', 'b', 'c', 'd']} options={{ onStep }} />));

    // A step to the next place: the element moves with the server's order and gets its focus back.
    beside('b').focus();
    altKey(beside('b'), 'ArrowDown');
    act(() => setOrder(['a', 'c', 'b', 'd']));
    expect(document.activeElement).toBe(beside('b'));

    // A step in place (across the freeze line): the focus stays where it was.
    inPlace = true;
    altKey(beside('b'), 'ArrowUp');
    expect(document.activeElement).toBe(beside('b'));

    // Pressed on the handle itself, the handle keeps it.
    inPlace = false;
    handle('c').focus();
    altKey(handle('c'), 'ArrowUp');
    act(() => setOrder(['c', 'a', 'b', 'd']));
    expect(document.activeElement).toBe(handle('c'));
  });

  it('asks the control for the queued steps one by one, until it makes none', () => {
    const calls: [number, number][] = [];
    // The first step waits for the server; from its answer on, the control shows each step at once.
    let showAtOnce = false;
    const onStep = (index: number, direction: -1 | 1) => {
      calls.push([index, direction]);
      if (index + direction >= currentItems.length) {
        return null;
      }
      if (showAtOnce) {
        setOrder(moved(currentItems, index, index + direction));
      }
      return { to: index + direction, announcement: `moved to ${index + direction + 1}` };
    };
    render({ initial: ['a', 'b', 'c', 'd', 'e'], options: { onStep } });

    handle('a').focus();
    // One step sent, four queued - the last one beyond the end of the list.
    for (let n = 0; n < 5; n++) {
      press('a', 'ArrowDown');
    }
    expect(calls).toEqual([[0, 1]]);

    showAtOnce = true;
    act(() => setOrder(['b', 'a', 'c', 'd', 'e']));

    expect(calls).toEqual([[0, 1], [1, 1], [2, 1], [3, 1], [4, 1]]);
    expect(shown()).toEqual(['b', 'c', 'd', 'e', 'a']);
    expect(announced()).toBe('moved to 5');
    expect(document.activeElement).toBe(handle('a'));
  });
});
