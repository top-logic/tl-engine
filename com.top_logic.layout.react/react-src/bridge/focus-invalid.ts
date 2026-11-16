/**
 * Where the focus goes after a command was refused because of input the user has to correct: to
 * the first field showing an error in the control the refusal names - the form a Save command
 * stores - or else in the control the command was sent to.
 *
 * <p>The refusal is decided on the server, and the fields learn that they show an error from the
 * events the command wrote - e.g. a mandatory field nobody had touched yet, revealed by the check
 * that refused the command. So the focus waits until the stream has brought these events and the
 * page has rendered them.</p>
 *
 * <p>A field behind a modal surface - a form behind the dialog asking whether to save its changes
 * - cannot take the focus while the surface is open, so it gets the focus once the surface has
 * closed.</p>
 */
import { isBehindTrap, onTrapReleased } from './focus-trap';
import { lastEventSeq } from './sse-client';

/**
 * Selector of an element showing an error. The fields of the design system carry their error as
 * `aria-invalid` (see `fieldStateAttrs`).
 */
const INVALID_SELECTOR = '[aria-invalid="true"]';

/** How long the focus waits for the events of the refusal and for their rendering. */
const FOCUS_WAIT_MS = 2_000;

/** Runs the given function with the next frame the page renders. */
function nextFrame(fn: () => void): void {
  if (typeof requestAnimationFrame === 'function') {
    requestAnimationFrame(() => fn());
  } else {
    setTimeout(fn, 16);
  }
}

/** Whether the given element is rendered, so that it can take the focus. */
function isVisible(element: HTMLElement): boolean {
  // An environment without the check (e.g. a test DOM without layout) takes every element as
  // rendered.
  return typeof element.checkVisibility !== 'function' || element.checkVisibility();
}

/**
 * The first rendered element showing an error within the element of the given control, in
 * document order; `null` if there is none.
 */
export function firstInvalidField(controlId: string): HTMLElement | null {
  const root = document.getElementById(controlId);
  if (!root) {
    return null;
  }
  for (const element of Array.from(root.querySelectorAll<HTMLElement>(INVALID_SELECTOR))) {
    if (isVisible(element)) {
      return element;
    }
  }
  return null;
}

/**
 * Moves the focus to the {@link firstInvalidField first field showing an error} within the given
 * control and scrolls it into view.
 *
 * <p>Waits until the stream has brought the event with the given number - the last one the server
 * wrote while it processed the refused command - and the page has rendered it. Gives up when no
 * such field shows up within {@link FOCUS_WAIT_MS}, or when the control leaves the page.</p>
 *
 * @param controlId the control the refused command was sent to.
 * @param lastEvent the number of the last event the server wrote for the command, `undefined` if
 *        not known.
 */
export function focusFirstInvalidField(controlId: string, lastEvent?: number): void {
  const deadline = Date.now() + FOCUS_WAIT_MS;
  const attempt = (): void => {
    if (!document.getElementById(controlId)) {
      return;
    }
    // Events are dispatched as they arrive; the page renders them with a later frame.
    const delivered = lastEvent === undefined || lastEventSeq() >= lastEvent;
    const field = delivered ? firstInvalidField(controlId) : null;
    if (field) {
      if (isBehindTrap(field)) {
        focusAfterModal(controlId);
      } else {
        focusField(field);
      }
      return;
    }
    if (Date.now() < deadline) {
      nextFrame(attempt);
    }
  };
  nextFrame(attempt);
}

/** Moves the focus to the given field and scrolls it into view. */
function focusField(field: HTMLElement): void {
  field.focus({ preventScroll: true });
  // Not every environment lays out the page (e.g. a test DOM).
  field.scrollIntoView?.({ block: 'nearest', inline: 'nearest' });
}

/**
 * Moves the focus to the first field showing an error within the given control once no modal
 * surface covers it any more.
 *
 * <p>A closing surface gives the focus back to where it was before it opened right after releasing
 * its trap, so the field takes the focus with the next frame. Gives up when the control leaves the
 * page or no longer shows an error - the user discarded the input, say.</p>
 */
function focusAfterModal(controlId: string): void {
  let done = false;
  const stop = onTrapReleased(() => {
    nextFrame(() => {
      if (done) {
        return;
      }
      const field = document.getElementById(controlId) ? firstInvalidField(controlId) : null;
      if (field && isBehindTrap(field)) {
        // Another surface still covers the field.
        return;
      }
      done = true;
      stop();
      if (field) {
        focusField(field);
      }
    });
  });
}
