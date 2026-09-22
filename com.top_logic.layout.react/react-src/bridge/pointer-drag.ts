/**
 * The press-drag-release gesture behind a drag handle: a splitter, a resize grip, a window title
 * bar, a calendar event.
 *
 * <p>The gesture is routed by pointer capture: the handle the press started on receives every
 * further event of that pointer, whatever the pointer travels over - an {@code iframe} embedding a
 * document of its own included, which would otherwise swallow the events and leave the drag
 * hanging. A shield element covering the viewport carries the cursor of the gesture, so the cursor
 * keeps naming the drag once the pointer leaves the handle, and it keeps hover effects and text
 * selection off the page while the pointer is dragging.</p>
 *
 * <p>A press only becomes a drag once the pointer has moved {@link DRAG_THRESHOLD_PX} away from
 * it: that is when the shield goes up, and it is what {@link PointerDragOptions.onEnd} is told
 * about the gesture it ends. A press that stays where it is, a click on the handle, therefore
 * changes nothing and reports nothing - and it sees no shield, since the browser aims a click at
 * what the pointer went down and came up on: an element covering the handle in between turns a
 * click on a resize grip into a click on the page behind it, and leaves the double click that
 * fits a column to its content, or maximizes a window, without a handle to happen on.</p>
 *
 * <p>The gesture ends on the pointer's release, or is cancelled when the pointer goes away without
 * one - the browser taking the pointer over for a gesture of its own, or the handle leaving the
 * document. Either way the shield and the listeners go with it: a drag never outlives the pointer
 * that drives it.</p>
 */

import type { PointerEvent as ReactPointerEvent } from 'react';

/** Class of the element that covers the viewport while a drag gesture runs. */
export const DRAG_SHIELD_CLASS = 'tlDragShield';

/** How far the pointer moves away from the press before the gesture counts as a drag. */
export const DRAG_THRESHOLD_PX = 3;

/** What {@link startPointerDrag} reports back to the control that owns the gesture. */
export interface PointerDragOptions {
  /**
   * The cursor shown for the whole viewport while the gesture runs, e.g. {@code col-resize}.
   *
   * <p>The cursor the handle declares in CSS, so that it does not change when the pointer moves
   * off the handle. Without one, the shield leaves the cursor to the page.</p>
   */
  cursor?: string;

  /**
   * The element the gesture is captured to, when the handle itself does not stay in the document
   * for the whole gesture - a dragged item that is rendered at its new place, for instance. An
   * element that encloses the gesture and outlives it, e.g. the grid the item is dragged in.
   *
   * <p>Without one, the gesture is captured to the handle the press happened on.</p>
   */
  captureOn?: HTMLElement | null;

  /** Called for every move of the dragging pointer. */
  onMove: (event: PointerEvent) => void;

  /**
   * Called for the release that ends the gesture, after the shield is gone.
   *
   * @param event The {@code pointerup} that ended the gesture.
   * @param dragged Whether the pointer ever moved {@link DRAG_THRESHOLD_PX} away from the press.
   *        A release that did not is a click on the handle: it leaves what the control shows as
   *        it is and reports nothing.
   */
  onEnd?: (event: PointerEvent, dragged: boolean) => void;

  /**
   * Called instead of {@link PointerDragOptions.onEnd} when the pointer goes away without a
   * release. Returns the control to the state it had before the press.
   */
  onCancel?: () => void;
}

/**
 * Starts a drag gesture on the handle the given press happened on.
 *
 * @param start The {@code pointerdown} that begins the gesture; its {@code currentTarget} is the
 *        handle the gesture is captured to, unless
 *        {@link PointerDragOptions.captureOn} names another element.
 * @param options What to do with the moves, the release and a cancellation.
 */
export function startPointerDrag(start: ReactPointerEvent | PointerEvent, options: PointerDragOptions): void {
  const handle = (options.captureOn ?? start.currentTarget) as HTMLElement | null;
  if (!handle) {
    return;
  }
  const pointerId = start.pointerId;

  try {
    handle.setPointerCapture(pointerId);
  } catch {
    // The pointer is not active any more, or the element it would be captured to is not in the
    // document: there is no gesture to run, and one the pointer is not routed to would never end.
    return;
  }

  const startX = start.clientX;
  const startY = start.clientY;
  let dragged = false;
  let shield: HTMLElement | null = null;

  const raiseShield = (): void => {
    shield = document.createElement('div');
    shield.className = DRAG_SHIELD_CLASS;
    if (options.cursor) {
      shield.style.cursor = options.cursor;
    }
    document.body.appendChild(shield);
  };

  // The pointer leaves in one of several ways and more than one of them is reported for the same
  // gesture (a release is followed by the loss of the capture), so the teardown decides who ends it.
  let running = true;

  const teardown = (): boolean => {
    if (!running) {
      return false;
    }
    running = false;
    handle.removeEventListener('pointermove', handleMove);
    handle.removeEventListener('pointerup', handleUp);
    handle.removeEventListener('pointercancel', handleCancel);
    handle.removeEventListener('lostpointercapture', handleLostCapture);
    shield?.remove();
    if (handle.hasPointerCapture(pointerId)) {
      try {
        handle.releasePointerCapture(pointerId);
      } catch {
        // Released along with the pointer.
      }
    }
    return true;
  };

  const handleMove = (event: PointerEvent): void => {
    if (event.pointerId !== pointerId) {
      return;
    }
    if (!dragged
      && (Math.abs(event.clientX - startX) >= DRAG_THRESHOLD_PX
        || Math.abs(event.clientY - startY) >= DRAG_THRESHOLD_PX)) {
      dragged = true;
      raiseShield();
    }
    options.onMove(event);
  };

  const handleUp = (event: PointerEvent): void => {
    if (event.pointerId !== pointerId || !teardown()) {
      return;
    }
    options.onEnd?.(event, dragged);
  };

  const cancel = (event: PointerEvent): void => {
    if (event.pointerId !== pointerId || !teardown()) {
      return;
    }
    options.onCancel?.();
  };

  const handleCancel = (event: PointerEvent): void => cancel(event);

  const handleLostCapture = (event: PointerEvent): void => cancel(event);

  handle.addEventListener('pointermove', handleMove);
  handle.addEventListener('pointerup', handleUp);
  handle.addEventListener('pointercancel', handleCancel);
  handle.addEventListener('lostpointercapture', handleLostCapture);
}
