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
 * and it is what the caller forwards unchanged.
 *
 * An item whose content is a text field is dragged from a handle ({@link ListReorderOptions.handle}):
 * a permanently draggable element hands its whole content to the drag gesture, so selecting text
 * with the mouse inside a field within it would start a drag of the item instead of selecting. Such
 * a list therefore arms the drag on a press of the item's handle and keeps every item undraggable
 * until then, while the item as a whole stays the area a drop is released on.
 */

import React, { useCallback, useEffect, useRef, useState } from 'react';

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

/** What {@link useListReorder} is told about the list it reorders. */
export interface ListReorderOptions {

  /** The direction the items follow each other in; decides which half of an item is its front. */
  axis?: ReorderAxis;

  /**
   * Called for a completed gesture that moves an item.
   *
   * @param from The position the item is dragged from.
   * @param to The position it ends up at, within the list as it is after the move.
   */
  onMove(from: number, to: number): void;

  /** Whether items can be dragged at all; a list that is only displayed passes `false`. */
  enabled?: boolean;

  /**
   * Whether an item becomes draggable only while its handle is pressed, see {@link handleProps}.
   *
   * A list whose items contain a text field passes `true`, so that the mouse selects text within the
   * field instead of picking the item up.
   */
  handle?: boolean;
}

/** What {@link useListReorder} hands back to the control. */
export interface ListReorder {

  /** Position of the item being dragged, or `null` while no drag is going on. */
  dragIndex: number | null;

  /** Where a drop would currently land, or `null` while the pointer is over no item. */
  dropTarget: ReorderDropTarget | null;

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
}

/**
 * Drag-and-drop reordering for a list of items addressed by their position.
 *
 * @param options Which list is reordered and what a completed gesture does, see
 *        {@link ListReorderOptions}.
 */
export function useListReorder(options: ListReorderOptions): ListReorder {
  const { axis = 'vertical', onMove, enabled = true, handle = false } = options;

  // The dragged item and the item it hovers over are held in refs as well as in state: the state
  // drives the drop indicator, while the drop handler reads the refs, so it sees the last hover even
  // if no render happened between the two events.
  const dragIndexRef = useRef<number | null>(null);
  const dropTargetRef = useRef<ReorderDropTarget | null>(null);
  const [dragIndex, setDragIndexState] = useState<number | null>(null);
  const [dropTarget, setDropTargetState] = useState<ReorderDropTarget | null>(null);

  // The reaction to a move is read at the time of the drop, so that the handlers keep their identity
  // over a render that renews the callback (a list it closes over has changed).
  const onMoveRef = useRef(onMove);
  onMoveRef.current = onMove;

  const setDragIndex = useCallback((index: number | null) => {
    dragIndexRef.current = index;
    setDragIndexState(index);
  }, []);

  const setDropTarget = useCallback((target: ReorderDropTarget | null) => {
    dropTargetRef.current = target;
    setDropTargetState(target);
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
    setDragIndex(index);
    event.dataTransfer.effectAllowed = 'move';
    // Firefox starts no drag at all without payload.
    event.dataTransfer.setData('text/plain', String(index));
  }, [setDragIndex]);

  const handleDragOver = useCallback((index: number, event: React.DragEvent) => {
    const dragged = dragIndexRef.current;
    if (dragged === null || dragged === index) {
      // Nothing of this list is being dragged, or the item hovers over itself: no drop here, so the
      // event keeps its default and the pointer shows that the gesture would do nothing.
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
  }, [axis, setDropTarget]);

  const handleDragEnd = useCallback(() => {
    setDragIndex(null);
    setDropTarget(null);
    disarm();
  }, [disarm, setDragIndex, setDropTarget]);

  const handleDrop = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    const from = dragIndexRef.current;
    const target = dropTargetRef.current;
    setDragIndex(null);
    setDropTarget(null);
    disarm();
    if (from === null || target === null) {
      return;
    }
    let to = target.side === 'before' ? target.index : target.index + 1;
    // The dragged item leaves its position before it is inserted again, so a target behind it moves
    // up by one.
    if (from < to) {
      to--;
    }
    if (to !== from) {
      onMoveRef.current(from, to);
    }
  }, [disarm, setDragIndex, setDropTarget]);

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

  const handleProps = useCallback((index: number): ReorderHandleProps => {
    if (!enabled || !handle) {
      return {};
    }
    return {
      onPointerDown: (event) => {
        // The primary button starts a drag; a context menu or a middle click does not.
        if (event.button === 0) {
          arm(index);
        }
      },
    };
  }, [arm, enabled, handle]);

  const itemState = useCallback((index: number): ReorderItemState => ({
    dragging: dragIndex === index,
    dropBefore: dropTarget !== null && dropTarget.index === index && dropTarget.side === 'before',
    dropAfter: dropTarget !== null && dropTarget.index === index && dropTarget.side === 'after',
  }), [dragIndex, dropTarget]);

  const containerProps: ReorderContainerProps = enabled ? { onDrop: handleDrop } : {};

  return { dragIndex, dropTarget, itemProps, handleProps, itemState, containerProps };
}
