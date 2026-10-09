/**
 * Dragging a draggable element with a finger, and the long press on an element.
 *
 * <p>The drag and drop of the controls is the browser's HTML drag and drop: a drag source is an
 * element with {@code draggable="true"} writing its payload in {@code dragstart}, a drop target
 * answers {@code dragover} and takes the {@code drop}. Most mobile browsers start no such drag from
 * a touch. This module plays the same drag with a finger: it dispatches {@code dragstart},
 * {@code dragenter}, {@code dragover}, {@code dragleave}, {@code drop} and {@code dragend} to the
 * elements under the finger, carrying one {@link DataTransfer} through the whole gesture, so every
 * drag source and drop target of the document works by touch without knowing of it.</p>
 *
 * <p>The gesture: a finger resting {@link LONG_PRESS_MS} on a draggable element picks it up - the
 * element shows as {@link ATTR_TOUCH_DRAG armed}. Moving the finger then starts the drag: an image
 * of the element follows the finger, and the drop is made where the finger is lifted. A finger
 * moving before the element is picked up scrolls the page as usual, and a short tap stays a click.
 * Near the edge of a scrolled container the drag scrolls it, so a drop target outside the visible
 * area is reached. A press on an interactive element within a draggable one (a button on a card)
 * is that element's and picks up nothing.</p>
 *
 * <p>A finger lifted from a picked-up element without having moved is a long press: the element
 * receives a {@link LONG_PRESS_EVENT}. An element that is not draggable takes part in the gesture
 * only for that, when it carries the attribute {@link ATTR_LONG_PRESS}; it is picked up the same
 * way, and a move after it was picked up ends the gesture.</p>
 *
 * <p>The browser's own touch drag, where it has one, is suppressed while this gesture runs, so a
 * drag source sees exactly one {@code dragstart}.</p>
 */

import { isInteractiveWithin } from './interactive';

/** Time in milliseconds a finger rests on a draggable element until it is picked up. */
const LONG_PRESS_MS = 450;

/** Distance in pixels a resting finger may wander without the press counting as a move. */
const PRESS_SLOP_PX = 8;

/** Width in pixels of the band along a scrolled container's edge in which the drag scrolls it. */
const AUTO_SCROLL_EDGE_PX = 48;

/** Pixels a container scrolls per tick with the finger at its very edge. */
const AUTO_SCROLL_MAX_STEP_PX = 18;

/**
 * Interval in milliseconds in which a running drag scrolls and repeats {@code dragover} at the
 * resting finger, as the browser repeats it for a resting pointer: a drop target's answer may
 * arrive while the finger does not move.
 */
const TICK_MS = 50;

/**
 * Attribute marking the element taking part in a touch drag: {@code armed} while it is picked up
 * and the finger has not moved yet, {@code source} while it is dragged.
 */
export const ATTR_TOUCH_DRAG = 'data-touch-drag';

/**
 * Attribute marking an element that is told of a long press on it by a {@link LONG_PRESS_EVENT},
 * also when it is not draggable.
 */
export const ATTR_LONG_PRESS = 'data-long-press';

/**
 * Event dispatched at an element a finger was lifted from without having moved since it picked the
 * element up. It bubbles, and carries no detail.
 */
export const LONG_PRESS_EVENT = 'tl-longpress';

/** Class of the image of the dragged element following the finger. */
export const TOUCH_DRAG_IMAGE_CLASS = 'tlTouchDragImage';

/** A finger resting on a draggable element, not picked up yet. */
interface Pressing {
  kind: 'pressing';
  touch: number;
  source: HTMLElement;
  startX: number;
  startY: number;
  timer: number;
}

/** A draggable element picked up by a finger that has not moved since. */
interface Armed {
  kind: 'armed';
  touch: number;
  source: HTMLElement;
  startX: number;
  startY: number;
}

/** A drag carried by a finger. */
interface Dragging {
  kind: 'dragging';
  touch: number;
  source: HTMLElement;
  startX: number;
  startY: number;
  dataTransfer: DataTransfer;
  image: HTMLElement;
  /** The element under the finger, the target of the drag events. */
  target: Element | null;
  /** Whether the target accepted the drop at the last {@code dragover}. */
  accepted: boolean;
  x: number;
  y: number;
  ticker: number;
}

/** The touch gesture in progress, `null` while none is. */
let _gesture: Pressing | Armed | Dragging | null = null;

/** Whether {@link installTouchDrag} has installed the listeners. */
let _installed = false;

/**
 * Installs the touch drag for the whole document. Called once; further calls do nothing.
 */
export function installTouchDrag(): void {
  if (_installed || typeof window === 'undefined' || typeof DataTransfer === 'undefined') {
    return;
  }
  _installed = true;
  document.addEventListener('touchstart', handleTouchStart, { capture: true, passive: true });
  // Not passive: a touchmove of a picked-up element is cancelled, which keeps the page from scrolling.
  document.addEventListener('touchmove', handleTouchMove, { capture: true, passive: false });
  document.addEventListener('touchend', handleTouchEnd, { capture: true, passive: false });
  document.addEventListener('touchcancel', handleTouchCancel, { capture: true, passive: true });
  document.addEventListener('dragstart', suppressNativeDrag, true);
  document.addEventListener('contextmenu', suppressWhilePickedUp, true);
  document.addEventListener('selectstart', suppressWhilePickedUp, true);
}

function handleTouchStart(event: TouchEvent): void {
  if (_gesture !== null) {
    // A second finger: no drag is carried by two.
    cancelGesture();
    return;
  }
  if (event.touches.length !== 1) {
    return;
  }
  const touch = event.changedTouches[0];
  const target = event.target instanceof Element ? event.target : null;
  const source = target?.closest<HTMLElement>(`[draggable="true"], [${ATTR_LONG_PRESS}]`) ?? null;
  if (source === null || isInteractiveWithin(target, source)) {
    return;
  }
  _gesture = {
    kind: 'pressing',
    touch: touch.identifier,
    source,
    startX: touch.clientX,
    startY: touch.clientY,
    timer: window.setTimeout(pickUp, LONG_PRESS_MS),
  };
}

/** Picks up the element the finger has rested on long enough. */
function pickUp(): void {
  const gesture = _gesture;
  if (gesture === null || gesture.kind !== 'pressing') {
    return;
  }
  _gesture = {
    kind: 'armed',
    touch: gesture.touch,
    source: gesture.source,
    startX: gesture.startX,
    startY: gesture.startY,
  };
  gesture.source.setAttribute(ATTR_TOUCH_DRAG, 'armed');
  navigator.vibrate?.(15);
}

function handleTouchMove(event: TouchEvent): void {
  const gesture = _gesture;
  if (gesture === null) {
    return;
  }
  const touch = findTouch(event.changedTouches, gesture.touch);
  if (touch === null) {
    return;
  }
  const moved = Math.hypot(touch.clientX - gesture.startX, touch.clientY - gesture.startY) > PRESS_SLOP_PX;
  switch (gesture.kind) {
    case 'pressing':
      if (moved) {
        // A finger moving before the element is picked up scrolls.
        cancelGesture();
      }
      return;
    case 'armed':
      if (event.cancelable) {
        event.preventDefault();
      }
      if (moved) {
        if (gesture.source.draggable) {
          startDrag(gesture, touch.clientX, touch.clientY);
        } else {
          cancelGesture();
        }
      }
      return;
    case 'dragging':
      if (event.cancelable) {
        event.preventDefault();
      }
      moveDrag(gesture, touch.clientX, touch.clientY);
      return;
  }
}

function handleTouchEnd(event: TouchEvent): void {
  const gesture = _gesture;
  if (gesture === null || findTouch(event.changedTouches, gesture.touch) === null) {
    return;
  }
  if (gesture.kind !== 'pressing' && event.cancelable) {
    // A picked-up element released is no click.
    event.preventDefault();
  }
  if (gesture.kind === 'dragging') {
    finishDrag(gesture);
  } else if (gesture.kind === 'armed') {
    cancelGesture();
    gesture.source.dispatchEvent(new CustomEvent(LONG_PRESS_EVENT, { bubbles: true }));
  } else {
    cancelGesture();
  }
}

function handleTouchCancel(event: TouchEvent): void {
  const gesture = _gesture;
  if (gesture !== null && findTouch(event.changedTouches, gesture.touch) !== null) {
    cancelGesture();
  }
}

/** Keeps the browser's own drag from starting beside the drag played here. */
function suppressNativeDrag(event: DragEvent): void {
  if (event.isTrusted && _gesture !== null) {
    event.preventDefault();
    event.stopImmediatePropagation();
  }
}

/** Keeps the long-press menu and text selection off an element picked up by a finger. */
function suppressWhilePickedUp(event: Event): void {
  if (_gesture !== null && _gesture.kind !== 'pressing') {
    event.preventDefault();
  }
}

function startDrag(gesture: Armed, x: number, y: number): void {
  const source = gesture.source;
  source.removeAttribute(ATTR_TOUCH_DRAG);
  const dataTransfer = new DataTransfer();
  const start = dispatchDrag('dragstart', source, dataTransfer, gesture.startX, gesture.startY, null);
  if (start.defaultPrevented || !source.isConnected) {
    _gesture = null;
    return;
  }
  source.setAttribute(ATTR_TOUCH_DRAG, 'source');
  const dragging: Dragging = {
    kind: 'dragging',
    touch: gesture.touch,
    source,
    startX: gesture.startX,
    startY: gesture.startY,
    dataTransfer,
    image: createDragImage(source),
    target: null,
    accepted: false,
    x,
    y,
    ticker: window.setInterval(tick, TICK_MS),
  };
  _gesture = dragging;
  moveDrag(dragging, x, y);
}

/** The image of the dragged element following the finger: a copy of it, where it was grabbed. */
function createDragImage(source: HTMLElement): HTMLElement {
  const rect = source.getBoundingClientRect();
  const image = source.cloneNode(true) as HTMLElement;
  image.removeAttribute(ATTR_TOUCH_DRAG);
  image.removeAttribute('id');
  for (const withId of Array.from(image.querySelectorAll('[id]'))) {
    withId.removeAttribute('id');
  }
  image.classList.add(TOUCH_DRAG_IMAGE_CLASS);
  image.setAttribute('aria-hidden', 'true');
  const style = image.style;
  style.position = 'fixed';
  style.left = rect.left + 'px';
  style.top = rect.top + 'px';
  style.width = rect.width + 'px';
  style.height = rect.height + 'px';
  style.margin = '0';
  style.boxSizing = 'border-box';
  style.pointerEvents = 'none';
  document.body.appendChild(image);
  return image;
}

/** Moves the drag to the finger at viewport position (`x`, `y`). */
function moveDrag(gesture: Dragging, x: number, y: number): void {
  gesture.x = x;
  gesture.y = y;
  gesture.image.style.transform = `translate(${x - gesture.startX}px, ${y - gesture.startY}px)`;
  const element = document.elementFromPoint(x, y);
  if (element !== gesture.target) {
    const left = gesture.target;
    gesture.target = element;
    gesture.accepted = false;
    if (left !== null) {
      dispatchDrag('dragleave', left, gesture.dataTransfer, x, y, element);
    }
    if (element !== null) {
      dispatchDrag('dragenter', element, gesture.dataTransfer, x, y, left);
    }
  }
  dragOver(gesture);
}

/** Asks the element under the finger whether it accepts the drop, as {@code dragover} does. */
function dragOver(gesture: Dragging): void {
  const target = gesture.target;
  if (target === null) {
    gesture.accepted = false;
    return;
  }
  // A target accepts by cancelling the event. The drop effect a handler sets is no answer here:
  // the browser keeps the effect of a DataTransfer created by a script at none.
  const over = dispatchDrag('dragover', target, gesture.dataTransfer, gesture.x, gesture.y, null);
  gesture.accepted = over.defaultPrevented;
}

/** Scrolls the containers the finger is at the edge of, and repeats the {@code dragover}. */
function tick(): void {
  const gesture = _gesture;
  if (gesture === null || gesture.kind !== 'dragging') {
    return;
  }
  if (autoScroll(gesture.x, gesture.y)) {
    // The content moved under the finger.
    moveDrag(gesture, gesture.x, gesture.y);
  } else {
    dragOver(gesture);
  }
}

/**
 * Scrolls the innermost container under viewport position (`x`, `y`) that can scroll towards the
 * edge the position is near, per axis.
 *
 * @returns Whether anything scrolled.
 */
function autoScroll(x: number, y: number): boolean {
  let scrolledX = false;
  let scrolledY = false;
  for (let element = document.elementFromPoint(x, y); element !== null && !(scrolledX && scrolledY);
    element = element.parentElement) {
    const rect = element === document.scrollingElement
      ? new DOMRect(0, 0, window.innerWidth, window.innerHeight)
      : element.getBoundingClientRect();
    const style = getComputedStyle(element);
    if (!scrolledX && scrolls(element, style.overflowX)) {
      const step = edgeStep(x, rect.left, rect.right);
      const before = element.scrollLeft;
      if (step !== 0) {
        element.scrollLeft += step;
        scrolledX = element.scrollLeft !== before;
      }
    }
    if (!scrolledY && scrolls(element, style.overflowY)) {
      const step = edgeStep(y, rect.top, rect.bottom);
      const before = element.scrollTop;
      if (step !== 0) {
        element.scrollTop += step;
        scrolledY = element.scrollTop !== before;
      }
    }
  }
  return scrolledX || scrolledY;
}

/** Whether the element scrolls its content with the given overflow along one axis. */
function scrolls(element: Element, overflow: string): boolean {
  return element === document.scrollingElement || overflow === 'auto' || overflow === 'scroll';
}

/**
 * The scroll step for a finger at `position` between the edges `start` and `end`: negative in the
 * band at the start, positive in the band at the end, the larger the closer to the edge.
 */
function edgeStep(position: number, start: number, end: number): number {
  const edge = Math.min(AUTO_SCROLL_EDGE_PX, (end - start) / 4);
  if (position < start + edge) {
    return -Math.ceil(AUTO_SCROLL_MAX_STEP_PX * (start + edge - position) / edge);
  }
  if (position > end - edge) {
    return Math.ceil(AUTO_SCROLL_MAX_STEP_PX * (position - (end - edge)) / edge);
  }
  return 0;
}

/** Drops where the finger was lifted, if the target accepts, and ends the drag. */
function finishDrag(gesture: Dragging): void {
  const target = gesture.target;
  if (target !== null && gesture.accepted) {
    dispatchDrag('drop', target, gesture.dataTransfer, gesture.x, gesture.y, null);
  } else if (target !== null) {
    dispatchDrag('dragleave', target, gesture.dataTransfer, gesture.x, gesture.y, null);
  }
  endDrag(gesture);
}

/** Ends the gesture in progress without a drop. */
function cancelGesture(): void {
  const gesture = _gesture;
  if (gesture === null) {
    return;
  }
  switch (gesture.kind) {
    case 'pressing':
      window.clearTimeout(gesture.timer);
      _gesture = null;
      return;
    case 'armed':
      gesture.source.removeAttribute(ATTR_TOUCH_DRAG);
      _gesture = null;
      return;
    case 'dragging':
        if (gesture.target !== null) {
        dispatchDrag('dragleave', gesture.target, gesture.dataTransfer, gesture.x, gesture.y, null);
      }
      endDrag(gesture);
      return;
  }
}

/** Tells the source that its drag has ended, and removes what the drag showed. */
function endDrag(gesture: Dragging): void {
  _gesture = null;
  window.clearInterval(gesture.ticker);
  gesture.image.remove();
  gesture.source.removeAttribute(ATTR_TOUCH_DRAG);
  dispatchDrag('dragend', gesture.source, gesture.dataTransfer, gesture.x, gesture.y, null);
}

function dispatchDrag(type: string, target: EventTarget, dataTransfer: DataTransfer, x: number, y: number,
    relatedTarget: EventTarget | null): DragEvent {
  const event = new DragEvent(type, {
    bubbles: true,
    cancelable: type !== 'dragleave' && type !== 'dragend',
    composed: true,
    clientX: x,
    clientY: y,
    dataTransfer,
    relatedTarget,
  });
  target.dispatchEvent(event);
  return event;
}

function findTouch(touches: TouchList, identifier: number): Touch | null {
  for (let i = 0; i < touches.length; i++) {
    if (touches[i].identifier === identifier) {
      return touches[i];
    }
  }
  return null;
}
