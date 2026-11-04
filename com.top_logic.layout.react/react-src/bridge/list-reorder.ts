/**
 * Reordering the items of a list by dragging one of them onto another one.
 *
 * The gesture is the same wherever a control lets the user arrange its items: an item is picked up,
 * the item under the pointer shows on which of its sides the dragged one would land - decided by
 * the half of that item the pointer is in - and the drop moves the picked item to that position.
 * {@link useListReorder} holds that gesture, so a control contributes only its own rendering: the
 * drag handlers for an item, the state its indicator classes are composed from, and the reaction to
 * a completed move.
 *
 * The hook addresses items by their position within the list it reorders. That position is the
 * control's own - a control rendering only part of its list (e.g. narrowed by a search) passes the
 * position within the whole list, and the drop then names positions of the whole list as well, so
 * an item can be dropped next to one that is currently not rendered.
 *
 * The move is reported as the position the dragged item ends up at, counted within the list as it is
 * after the move. That is the position the server-side commands of the reorderable controls take,
 * and it is what the caller forwards unchanged. A drop reports the item and side it was released on
 * as well ({@link ReorderDropTarget}): in a list whose items form sections, the place after the last
 * item of one section and the place before the first item of the next are the same position, and
 * only the side of the drop tells into which section the item goes; such a list also has the drop at
 * the item's own position reported ({@link ListReorderOptions.reportUnmoved}).
 *
 * A list may group items into zones ({@link ListReorderOptions.zoneOf}): a zone takes an item as a
 * whole - its own items keep their order - so its items show no insertion mark, and the pointer over
 * any of them makes the zone the target ({@link ListReorder.dropZone}). An element standing for a
 * zone rather than an item - the placeholder of an empty zone, the heading of a section - takes a
 * drop into it through {@link ListReorder.zoneProps}. A zone that does not take the dragged item
 * leaves the drop refused, so the pointer shows that the gesture would do nothing.
 *
 * An item whose content is a text field is dragged from a handle ({@link ListReorderOptions.handle}):
 * a permanently draggable element hands its whole content to the drag gesture, so selecting text
 * with the mouse inside a field within it would start a drag of the item instead of selecting. Such
 * a list therefore arms the drag on a press of the item's handle and keeps every item undraggable
 * until then, while the item as a whole stays the area a drop is released on.
 *
 * The handle is a button: Alt + arrow moves the item, the live region announces the new position, and
 * the focus stays on the moved item's handle (3b §8). In a list keyed by value, the steps pressed while a
 * move waits for the server queue up and follow one by one, each from where the server put the item.
 * A control whose steps do more than swap two neighbours - across a section line, into or out of a
 * zone - makes each step itself ({@link ListReorderOptions.onStep}); the hook then keeps the focus,
 * the announcement and the queue.
 */

import React, { useCallback, useEffect, useId, useLayoutEffect, useRef, useState } from 'react';
import { useI18N } from './i18n';
import { afterDragImage } from './drag-drop';

/** Texts of the keyboard gesture; the server translates them (I18NConstants, js.reorder.*). */
const I18N_KEYS = {
  'js.reorder.handle': 'Move {0}',
  'js.reorder.hint': 'Alt + arrow key moves the entry',
  'js.reorder.moved': '{0}, position {1} of {2}',
  'js.reorder.position': 'Position {0} of {1}',
};

/** Fills the placeholders {0}, {1}, ... of a translated text. */
function format(template: string, ...args: string[]): string {
  return args.reduce((text, arg, i) => text.replace('{' + i + '}', arg), template);
}

/** The direction the items of a reorderable list follow each other in. */
export type ReorderAxis = 'vertical' | 'horizontal';

/** Which side of the hovered item the dragged one would land on. */
export type ReorderSide = 'before' | 'after';

/** The item the pointer hovers over during a drag, and the side the drop would land on. */
export interface ReorderDropTarget {

  /** Position of the hovered item. */
  index: number;

  /** Whether the dragged item would land before or after the hovered one. */
  side: ReorderSide;
}

/** How a single item takes part in the drag currently going on. */
export interface ReorderItemState {

  /** Whether this item is the one being dragged. */
  dragging: boolean;

  /** Whether a drop would insert the dragged item before this one. */
  dropBefore: boolean;

  /** Whether a drop would insert the dragged item after this one. */
  dropAfter: boolean;
}

/** The drag properties of a single item, to spread onto the element that is dragged and dropped on. */
export interface ReorderItemProps {
  draggable: boolean;
  onDragStart?(event: React.DragEvent): void;
  onDragOver?(event: React.DragEvent): void;
  onDrop?(event: React.DragEvent): void;
  onDragEnd?(event: React.DragEvent): void;
}

/**
 * The properties of the element an item is dragged by, see {@link ListReorderOptions.handle}.
 *
 * The press arms the item for dragging and nothing else, so a handle that is a button still takes
 * the focus and keeps whatever the control does with the keyboard.
 */
export interface ReorderHandleProps {
  onPointerDown?(event: React.PointerEvent): void;
  /** Alt + arrow moves the item by one position (WCAG 2.5.7: dragging has a single-pointer-free alternative). */
  onKeyDown?(event: React.KeyboardEvent): void;
  /** Points at the hint naming the keyboard gesture, rendered by {@link ListReorder.announcer}. */
  'aria-describedby'?: string;
  /** "Move <name>", present where {@link ListReorderOptions.labelOf} names the item. */
  'aria-label'?: string;
  /**
   * Registers the handle, so that the focus can return to it after a keyboard move; a control spreads
   * it onto the handle and sets no ref of its own there.
   */
  ref?: (element: HTMLElement | null) => void;
}

/**
 * The drag properties of the element containing the items.
 *
 * Spreading them completes a drop that is released next to the items rather than on one of them -
 * in the padding of the list, or in the space a scrolling list leaves below its last item.
 */
export interface ReorderContainerProps {
  onDrop?(event: React.DragEvent): void;
}

/**
 * The drag properties of an element standing for a zone rather than an item, see {@link ListReorder.zoneProps}:
 * the placeholder of an empty zone, the heading of a section.
 */
export interface ReorderZoneProps {
  onDragOver?(event: React.DragEvent): void;
  onDrop?(event: React.DragEvent): void;
}

/** The direction of a keyboard step: back (`-1`) or forth (`1`) along the axis of the list. */
export type ReorderDirection = -1 | 1;

/** A keyboard step a control made itself, see {@link ListReorderOptions.onStep}. */
export interface ReorderStep {

  /** The position the moved item is shown at after the step; the focus returns to its handle there. */
  to: number;

  /** What the live region announces for the step. */
  announcement: string;
}

/** What {@link useListReorder} is told about the list it reorders. */
export interface ListReorderOptions {

  /** The direction the items follow each other in; decides which half of an item is its front. */
  axis?: ReorderAxis;

  /**
   * Called for a completed gesture that moves an item.
   *
   * @param from The position the item is dragged from.
   * @param to The position it ends up at, within the list as it is after the move.
   * @param target The item and side the drop was released on; `undefined` for a keyboard move.
   */
  onMove(from: number, to: number, target?: ReorderDropTarget): void;

  /** Whether items can be dragged at all; a list that is only displayed passes `false`. */
  enabled?: boolean;

  /**
   * Whether an item becomes draggable only while its handle is pressed, see {@link handleProps}.
   *
   * A list whose items contain a text field passes `true`, so that the mouse selects text within the
   * field instead of picking the item up.
   */
  handle?: boolean;

  /** The number of items; a keyboard move stops at both ends. */
  count: number;

  /**
   * Whether an item may be moved to the position of another one: dragging shows no insertion mark over an item it
   * does not accept, a drop there moves nothing, and a keyboard move stops there without a word. A list whose items
   * form blocks that keep their members (the frozen and the movable columns) refuses the position across a block
   * line. Absent: every position is accepted.
   *
   * @param from The position of the moved item.
   * @param to The position of the item it would move to or over.
   */
  accepts?(from: number, to: number): boolean;

  /** The name of the item at the given position, for the handle's label and the announcement of a move. */
  labelOf?(index: number): string;

  /**
   * The React key of the item at the given position, for a list keyed by its values: after a keyboard
   * move the hook gives the focus back to the moved item's handle once the list shows the item at its
   * new position. Without it the list is keyed by position, and the focus goes to the handle at the
   * new position right away.
   */
  keyOf?(index: number): string;

  /**
   * Whether a drop that leaves the item at its own position is reported as well, as `onMove(from, from, target)`.
   *
   * In a plain list such a drop - right after the item's predecessor, right before its successor - changes nothing
   * and is not reported. A list whose positions carry a state needs it: where two blocks meet (the frozen and the
   * movable columns), "after the last of the one" and "before the first of the other" are the same position, and
   * only the {@link ReorderDropTarget} tells into which block the item goes - dropping the first of the second block
   * after the last of the first one moves it across the line without changing its position. Keyboard steps are not
   * affected. Absent: `false`.
   */
  reportUnmoved?: boolean;

  /**
   * The zone the item at the given position belongs to, or `null` for an item that is a position of its own.
   *
   * An item of a zone shows no insertion mark: the pointer over it makes its zone the target, and a drop there
   * goes to {@link onZoneDrop}. This holds for the dragged item itself as well - over an item of its own zone, the
   * zone is the target, and {@link acceptsZone} decides on it like for any other item. Absent: no item belongs to
   * a zone.
   */
  zoneOf?(index: number): string | null;

  /**
   * Whether the given zone takes the item dragged from the given position. A zone it refuses takes no drop, and
   * the pointer over it shows that the gesture would do nothing. Absent: every zone takes every item, provided
   * there is an {@link onZoneDrop}.
   *
   * @param from The position of the dragged item.
   * @param zone The zone the pointer is over.
   */
  acceptsZone?(from: number, zone: string): boolean;

  /**
   * Called for a drop into a zone, on one of its items or on an element standing for it
   * ({@link ListReorder.zoneProps}). Absent: no zone takes a drop.
   *
   * @param from The position of the dropped item.
   * @param zone The zone it was dropped into.
   */
  onZoneDrop?(from: number, zone: string): void;

  /**
   * Makes a keyboard step of the item at the given position in the control's own way, for a list whose steps do
   * more than swap two neighbours (across a section line, into or out of a zone).
   *
   * Asked for every Alt + arrow before the hook checks the position itself, so it decides on the ends of the list
   * and on positions {@link accepts} refuses as well:
   * - a {@link ReorderStep}: the control has made the move (sent its command); the hook announces its text and
   *   returns the focus to the handle at the position it names, as for a move of its own;
   * - `null`: no step; nothing is moved or announced;
   * - `undefined`: the hook makes the step as without this option (one position, {@link onMove}).
   *
   * @param index The position of the item.
   * @param direction The direction of the step.
   */
  onStep?(index: number, direction: ReorderDirection): ReorderStep | null | undefined;
}

/** What {@link useListReorder} hands back to the control. */
export interface ListReorder {

  /** Position of the item being dragged, or `null` while no drag is going on. */
  dragIndex: number | null;

  /** Where a drop would currently land, or `null` while the pointer is over no item. */
  dropTarget: ReorderDropTarget | null;

  /**
   * The zone a drop would currently go into, or `null` while the pointer is over no zone taking the dragged item.
   *
   * A zone that refuses the item ({@link ListReorderOptions.acceptsZone}) is no target and is not set here, so a
   * control highlighting the target zone from this value never highlights a refused one. Set exclusively of
   * {@link dropTarget}: the pointer moving on to an item, to another zone or to a refused target replaces or clears
   * both, a drop and the end of the drag clear both. Leaving the items for the padding of the list keeps the last
   * one, so that a drop released there ({@link containerProps}) still goes where the pointer was last.
   */
  dropZone: string | null;

  /** The drag properties of the item at the given position. */
  itemProps(index: number): ReorderItemProps;

  /**
   * The properties of the element the item at the given position is dragged by.
   *
   * Empty unless {@link ListReorderOptions.handle} is set, so a control spreads them onto its handle
   * without knowing which of the two gestures it was configured for.
   */
  handleProps(index: number): ReorderHandleProps;

  /** The drag state of the item at the given position, to compose its indicator classes from. */
  itemState(index: number): ReorderItemState;

  /** The drag properties of the element containing the items. */
  containerProps: ReorderContainerProps;

  /**
   * The drag properties of an element standing for the given zone rather than an item - the placeholder of an empty
   * zone, the heading of a section: the pointer over it makes the zone the target, as over an item of the zone.
   */
  zoneProps(zone: string): ReorderZoneProps;

  /**
   * The hint the handles point at and the live region announcing a keyboard move ("<name>, position n
   * of m"); the control renders it once, next to the list.
   */
  announcer: React.ReactElement;
}

/** The `data-tl-state` word of an item's drag state (drag.css), `undefined` while it takes no part. */
export function reorderState(state: ReorderItemState): 'dragging' | 'drop-before' | 'drop-after' | undefined {
  return state.dragging ? 'dragging' : state.dropBefore ? 'drop-before' : state.dropAfter ? 'drop-after' : undefined;
}

/**
 * Drag-and-drop reordering for a list of items addressed by their position.
 *
 * @param options Which list is reordered and what a completed gesture does, see
 *        {@link ListReorderOptions}.
 */
export function useListReorder(options: ListReorderOptions): ListReorder {
  const {
    axis = 'vertical', onMove, enabled = true, handle = false, count, labelOf, keyOf, accepts, reportUnmoved = false,
    zoneOf, acceptsZone, onZoneDrop, onStep,
  } = options;
  const t = useI18N(I18N_KEYS);
  const hintId = useId();
  const [announcement, setAnnouncement] = useState('');

  // The dragged item and the item it hovers over are held in refs as well as in state: the state
  // drives the drop indicator, while the drop handler reads the refs, so it sees the last hover even
  // if no render happened between the two events.
  const dragIndexRef = useRef<number | null>(null);
  const dropTargetRef = useRef<ReorderDropTarget | null>(null);
  const dropZoneRef = useRef<string | null>(null);
  const [dragIndex, setDragIndexState] = useState<number | null>(null);
  const [dropTarget, setDropTargetState] = useState<ReorderDropTarget | null>(null);
  const [dropZone, setDropZoneState] = useState<string | null>(null);

  // The reaction to a move is read at the time of the drop, so that the handlers keep their identity
  // over a render that renews the callback (a list it closes over has changed).
  const onMoveRef = useRef(onMove);
  onMoveRef.current = onMove;
  const countRef = useRef(count);
  countRef.current = count;
  const labelOfRef = useRef(labelOf);
  labelOfRef.current = labelOf;
  const keyOfRef = useRef(keyOf);
  keyOfRef.current = keyOf;
  const acceptsRef = useRef(accepts);
  acceptsRef.current = accepts;
  const reportUnmovedRef = useRef(reportUnmoved);
  reportUnmovedRef.current = reportUnmoved;
  const zoneOfRef = useRef(zoneOf);
  zoneOfRef.current = zoneOf;
  const acceptsZoneRef = useRef(acceptsZone);
  acceptsZoneRef.current = acceptsZone;
  const onZoneDropRef = useRef(onZoneDrop);
  onZoneDropRef.current = onZoneDrop;
  const onStepRef = useRef(onStep);
  onStepRef.current = onStep;

  const setDragIndex = useCallback((index: number | null) => {
    dragIndexRef.current = index;
    setDragIndexState(index);
  }, []);

  const setDropTarget = useCallback((target: ReorderDropTarget | null) => {
    dropTargetRef.current = target;
    setDropTargetState(target);
  }, []);

  const setDropZone = useCallback((zone: string | null) => {
    dropZoneRef.current = zone;
    setDropZoneState(zone);
  }, []);

  // The item armed by a press on its handle, which is the only one that is draggable while the
  // gesture starts from a handle. Held in a ref as well, for the same reason as the drag itself.
  const armedIndexRef = useRef<number | null>(null);
  const [armedIndex, setArmedIndexState] = useState<number | null>(null);

  // Removes the listeners watching for the end of the press that armed an item, or `null` while no
  // item is armed.
  const releasePressRef = useRef<(() => void) | null>(null);

  const disarm = useCallback(() => {
    if (releasePressRef.current !== null) {
      releasePressRef.current();
      releasePressRef.current = null;
    }
    if (armedIndexRef.current !== null) {
      armedIndexRef.current = null;
      setArmedIndexState(null);
    }
  }, []);

  const arm = useCallback((index: number) => {
    if (releasePressRef.current !== null) {
      releasePressRef.current();
      releasePressRef.current = null;
    }
    armedIndexRef.current = index;
    setArmedIndexState(index);
    // A press that ends without a drag - a click on the handle, a press the pointer gives up - leaves
    // the item undraggable again, so the arming lasts no longer than the gesture it belongs to. A
    // press that did start a drag is disarmed when that drag ends.
    const pressEnded = () => {
      if (dragIndexRef.current === null) {
        disarm();
      }
    };
    document.addEventListener('pointerup', pressEnded, { once: true });
    document.addEventListener('pointercancel', pressEnded, { once: true });
    releasePressRef.current = () => {
      document.removeEventListener('pointerup', pressEnded);
      document.removeEventListener('pointercancel', pressEnded);
    };
  }, [disarm]);

  // An item armed as the control disappears - a dialog closed mid-press - leaves no listener behind.
  useEffect(() => () => {
    if (releasePressRef.current !== null) {
      releasePressRef.current();
      releasePressRef.current = null;
    }
  }, []);

  const handleDragStart = useCallback((index: number, event: React.DragEvent) => {
    // dragover reads the ref at once; the item is marked dragged only after the drag image is taken.
    dragIndexRef.current = index;
    afterDragImage(() => {
      if (dragIndexRef.current === index) {
        setDragIndexState(index);
      }
    });
    event.dataTransfer.effectAllowed = 'move';
    // Firefox starts no drag at all without payload.
    event.dataTransfer.setData('text/plain', String(index));
  }, []);

  // The pointer over a zone, on one of its items or on an element standing for it: the zone is the target if it
  // takes the dragged item. A zone has no insertion mark, so the target of an item is gone.
  const handleZoneDragOver = useCallback((zone: string, event: React.DragEvent) => {
    setDropTarget(null);
    const dragged = dragIndexRef.current;
    if (dragged === null || !onZoneDropRef.current || acceptsZoneRef.current?.(dragged, zone) === false) {
      // As for an item the list does not accept: the event keeps its default, the pointer shows no drop.
      setDropZone(null);
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
    setDropZone(zone);
  }, [setDropTarget, setDropZone]);

  const handleDragOver = useCallback((index: number, event: React.DragEvent) => {
    const zone = zoneOfRef.current?.(index) ?? null;
    if (zone !== null) {
      handleZoneDragOver(zone, event);
      return;
    }
    setDropZone(null);
    const dragged = dragIndexRef.current;
    if (dragged === null || dragged === index || acceptsRef.current?.(dragged, index) === false) {
      // Nothing of this list is being dragged, the item hovers over itself, or the list does not accept the position:
      // no drop here, so the event keeps its default and the pointer shows that the gesture would do nothing.
      setDropTarget(null);
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
    const rect = event.currentTarget.getBoundingClientRect();
    const side: ReorderSide = axis === 'vertical'
      ? (event.clientY < rect.top + rect.height / 2 ? 'before' : 'after')
      : (event.clientX < rect.left + rect.width / 2 ? 'before' : 'after');
    setDropTarget({ index, side });
  }, [axis, handleZoneDragOver, setDropTarget, setDropZone]);

  const handleDragEnd = useCallback(() => {
    setDragIndex(null);
    setDropTarget(null);
    setDropZone(null);
    disarm();
  }, [disarm, setDragIndex, setDropTarget, setDropZone]);

  const handleDrop = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    const from = dragIndexRef.current;
    const target = dropTargetRef.current;
    const zone = dropZoneRef.current;
    setDragIndex(null);
    setDropTarget(null);
    setDropZone(null);
    disarm();
    if (from === null) {
      return;
    }
    if (zone !== null) {
      onZoneDropRef.current?.(from, zone);
      return;
    }
    if (target === null) {
      return;
    }
    let to = target.side === 'before' ? target.index : target.index + 1;
    // The dragged item leaves its position before it is inserted again, so a target behind it moves
    // up by one.
    if (from < to) {
      to--;
    }
    // A drop at the item's own position changes nothing, unless the side of the drop is a state of the list.
    if (to !== from || reportUnmovedRef.current) {
      onMoveRef.current(from, to, target);
    }
  }, [disarm, setDragIndex, setDropTarget, setDropZone]);

  const itemProps = useCallback((index: number): ReorderItemProps => {
    if (!enabled) {
      return { draggable: false };
    }
    return {
      draggable: handle ? armedIndex === index : true,
      onDragStart: (event) => handleDragStart(index, event),
      onDragOver: (event) => handleDragOver(index, event),
      onDrop: handleDrop,
      onDragEnd: handleDragEnd,
    };
  }, [enabled, handle, armedIndex, handleDragStart, handleDragOver, handleDrop, handleDragEnd]);

  const zoneProps = useCallback((zone: string): ReorderZoneProps => {
    if (!enabled) {
      return {};
    }
    return {
      onDragOver: (event) => handleZoneDragOver(zone, event),
      onDrop: handleDrop,
    };
  }, [enabled, handleZoneDragOver, handleDrop]);

  // -- After a keyboard move the focus returns to the moved item's handle (3b §8). A list keyed by
  //    value moves the item's DOM node once the new order arrives, and the browser drops the focus of
  //    a node it moves; a list keyed by position keeps its nodes, and the moved value now sits under
  //    the handle at the new position.
  //
  //    A list keyed by value shows the moved item where it was until the server's new order arrives, so a
  //    step pressed meanwhile would start from a stale position and repeat the one before. It is queued
  //    instead (`queued`, steps back counting negative) and sent once the item stands where the step before
  //    put it, from there - the server stays the one source of the order. An order arriving without the item
  //    at that position (the server refused the move) drops the queued steps; a step the list does not take
  //    from the new position (an end, a block line, a step the control refuses) drops them as well. A step the
  //    control makes itself (onStep) waits the same way, for the item to stand at the position the control named. --
  const handlesRef = useRef(new Map<string, HTMLElement>());
  const pendingFocusRef = useRef<{ key: string; to: number; queued: number; order: string } | null>(null);
  const keyAt = useCallback((index: number) => keyOfRef.current?.(index) ?? String(index), []);

  // The order of the items by their keys, to tell a new order of the server from a render for another reason; a list
  // keyed by position has none of its own.
  const order = keyOf ? Array.from({ length: count }, (_, index) => keyOf(index)).join('\u0000') : '';
  const orderRef = useRef(order);
  orderRef.current = order;

  // One step of the item at `from` in the given direction, announcing where the item lands; `false` where no step
  // is made and nothing is sent. The control's own step comes first (onStep), so it also decides on the ends of
  // the list and on positions `accepts` refuses; without one, the step is the same move a drop to the neighbouring
  // position makes, where the list has that position and accepts it.
  const step = useCallback((from: number, direction: ReorderDirection, queued: number): boolean => {
    // Taken before the control acts: a control showing the step at once changes its keys with the next render.
    const key = keyOfRef.current?.(from);
    const own = onStepRef.current?.(from, direction);
    if (own !== undefined) {
      if (own === null) {
        return false;
      }
      pendingFocusRef.current = { key: key ?? String(own.to), to: own.to, queued, order: orderRef.current };
      setAnnouncement(own.announcement);
      return true;
    }
    const to = from + direction;
    const total = countRef.current;
    if (to < 0 || to >= total || acceptsRef.current?.(from, to) === false) {
      return false;
    }
    pendingFocusRef.current = { key: key ?? String(to), to, queued, order: orderRef.current };
    onMoveRef.current(from, to);
    const name = labelOfRef.current?.(from) ?? '';
    setAnnouncement(name
      ? format(t['js.reorder.moved'], name, String(to + 1), String(total))
      : format(t['js.reorder.position'], String(to + 1), String(total)));
    return true;
  }, [t]);

  useLayoutEffect(() => {
    const pending = pendingFocusRef.current;
    if (pending === null) {
      return;
    }
    const target = handlesRef.current.get(pending.key);
    if (keyAt(pending.to) === pending.key) {
      pendingFocusRef.current = null;
      if (target?.isConnected) {
        target.focus();
      }
      if (pending.queued !== 0) {
        const direction: ReorderDirection = pending.queued < 0 ? -1 : 1;
        step(pending.to, direction, pending.queued - direction);
      }
      return;
    }
    const active = document.activeElement;
    if (pending.order !== orderRef.current) {
      // A new order without the item where it was sent: the server refused the move. The focus stays with the item.
      pendingFocusRef.current = null;
      if (target?.isConnected && (active === null || active === document.body)) {
        target.focus();
      }
      return;
    }
    // The user went on before the new order arrived: the focus is not the list's to take back.
    if (active !== null && active !== document.body && active !== target) {
      pendingFocusRef.current = null;
    }
  });

  // Alt + arrow moves the item by one position and announces where it landed. Back and forth follow the axis:
  // up/down in a column, left/right in a row.
  const keyboardMove = useCallback((index: number, event: React.KeyboardEvent) => {
    if (!event.altKey || event.ctrlKey || event.metaKey || event.shiftKey) {
      return;
    }
    const back = axis === 'vertical' ? 'ArrowUp' : 'ArrowLeft';
    const forth = axis === 'vertical' ? 'ArrowDown' : 'ArrowRight';
    if (event.key !== back && event.key !== forth) {
      return;
    }
    // The list takes Alt + arrow over from the page: no scrolling, no step in the browser history.
    event.preventDefault();
    event.stopPropagation();
    const direction: ReorderDirection = event.key === back ? -1 : 1;
    const pending = pendingFocusRef.current;
    if (pending !== null && keyOfRef.current && pending.key === keyOfRef.current(index)) {
      // The item waits for the server to place it: the step follows from there.
      pending.queued += direction;
      return;
    }
    step(index, direction, 0);
  }, [axis, step]);

  const handleProps = useCallback((index: number): ReorderHandleProps => {
    if (!enabled) {
      return {};
    }
    const name = labelOfRef.current?.(index);
    return {
      // The primary button starts a drag; a context menu or a middle click does not.
      onPointerDown: handle ? (event) => { if (event.button === 0) arm(index); } : undefined,
      onKeyDown: (event) => keyboardMove(index, event),
      'aria-describedby': hintId,
      ...(name ? { 'aria-label': format(t['js.reorder.handle'], name) } : {}),
      ref: (element) => {
        if (element) {
          handlesRef.current.set(keyAt(index), element);
        }
      },
    };
  }, [arm, enabled, handle, hintId, keyAt, keyboardMove, t]);

  const itemState = useCallback((index: number): ReorderItemState => ({
    dragging: dragIndex === index,
    dropBefore: dropTarget !== null && dropTarget.index === index && dropTarget.side === 'before',
    dropAfter: dropTarget !== null && dropTarget.index === index && dropTarget.side === 'after',
  }), [dragIndex, dropTarget]);

  const containerProps: ReorderContainerProps = enabled ? { onDrop: handleDrop } : {};

  const announcer = React.createElement(React.Fragment, null,
    React.createElement('span', { id: hintId, hidden: true }, t['js.reorder.hint']),
    React.createElement('span', { role: 'status', className: 'tl-visually-hidden' }, announcement));

  return { dragIndex, dropTarget, dropZone, itemProps, handleProps, itemState, containerProps, zoneProps, announcer };
}
