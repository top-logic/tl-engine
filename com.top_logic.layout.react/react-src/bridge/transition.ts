// A change of what a control displays - the step a wizard moved to, a dialog opening, a frame
// pushed onto a drill-down stack - as something a stylesheet can animate.
//
// What all three have in common is a list of keys: one key per element displayed, and a change of
// that list is the moment to animate. The hook here marks the DOM for such a change - a class or a word in
// data-tl-state on the element that arrived, a copy of the element that left - and takes the marks away again once
// the animation is over. Which animation runs, and whether one runs at all, is the stylesheet's
// decision; the marks are the contract between the two.

import { useLayoutEffect, useRef } from 'react';
import { STATE_ATTR, addStateWord, removeStateWord } from './css';

/**
 * How long a transition mark stays at most when an animation runs but never reports its end - one
 * on an element that is hidden, for instance, or removed from the rendering.
 *
 * The marks are display state, so they must not survive the change they describe even where no
 * animation reports its end. Where nothing animates at all, the marks go right away.
 */
export const TRANSITION_FALLBACK_MS = 1000;

/**
 * Whether the viewer asked for as little movement as possible.
 *
 * Read at the moment of the change rather than once, so that a viewer switching the setting is
 * followed without a reload.
 */
function reducedMotion(): boolean {
  return typeof window.matchMedia === 'function'
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

/**
 * A CSS time list (`0.2s, 150ms`) in milliseconds.
 */
function parseTimes(value: string): number[] {
  return value.split(',').map((entry) => {
    const time = entry.trim();
    const amount = parseFloat(time);
    if (Number.isNaN(amount)) {
      return 0;
    }
    return time.endsWith('ms') ? amount : amount * 1000;
  });
}

/**
 * Whether a list of durations paired with a list of delays, both repeated as CSS repeats a shorter
 * list, takes time for any of the given indices.
 */
function takesTime(indices: readonly number[], durations: number[], delays: number[]): boolean {
  return indices.some((index) =>
    durations[index % durations.length] + delays[index % delays.length] > 0);
}

/**
 * Whether the element runs an animation or a transition that takes time, as its stylesheet says
 * right now.
 *
 * An animation counts for each name other than `none`, a transition for each property listed; each
 * counts when its duration and delay add up to more than nothing.
 */
function animates(element: HTMLElement): boolean {
  const style = window.getComputedStyle(element);

  const names = style.animationName.split(',').map((name) => name.trim());
  const animations = names
    .map((name, index) => (name === 'none' || name === '' ? -1 : index))
    .filter((index) => index >= 0);
  if (animations.length > 0
    && takesTime(animations, parseTimes(style.animationDuration), parseTimes(style.animationDelay))) {
    return true;
  }

  const durations = parseTimes(style.transitionDuration);
  return takesTime(durations.map((_, index) => index), durations, parseTimes(style.transitionDelay));
}

/**
 * Calls `done` once the element has finished animating.
 *
 * The element must carry the marks it animates against when this is called. If its stylesheet
 * animates nothing then, `done` is called before this returns. Otherwise the end is the first
 * `animationend` or `transitionend` of the element itself, or {@link TRANSITION_FALLBACK_MS} for an
 * animation that never reports its end.
 *
 * @returns A function that ends the wait right away, or `null` if the wait was already over before
 *          this returned. Calling the function more than once is harmless, and so is calling it
 *          after the wait is over.
 */
function whenFinished(element: HTMLElement, done: () => void): (() => void) | null {
  if (!animates(element)) {
    done();
    return null;
  }
  let timer = 0;
  let finished = false;
  const end = (event?: Event) => {
    if (event && event.target !== element) {
      // Something inside the element animating, not the element itself.
      return;
    }
    if (finished) {
      return;
    }
    finished = true;
    element.removeEventListener('animationend', end);
    element.removeEventListener('transitionend', end);
    window.clearTimeout(timer);
    done();
  };
  element.addEventListener('animationend', end);
  element.addEventListener('transitionend', end);
  timer = window.setTimeout(end, TRANSITION_FALLBACK_MS);
  return end;
}

/** How a change is marked on the DOM. */
interface Marks {
  enter: string;
  exit: string;
  /** The attribute the marks are written to. */
  attribute: string;
  has(element: HTMLElement, mark: string): boolean;
  add(element: HTMLElement, mark: string): void;
  remove(element: HTMLElement, mark: string): void;
}

const CLASS_MARKS = (enter: string, exit: string): Marks => ({
  enter,
  exit,
  attribute: 'class',
  has: (element, mark) => element.classList.contains(mark),
  add: (element, mark) => element.classList.add(mark),
  remove: (element, mark) => element.classList.remove(mark),
});

const STATE_MARKS: Marks = {
  enter: 'entering',
  exit: 'exiting',
  attribute: STATE_ATTR,
  has: (element, mark) => (element.getAttribute(STATE_ATTR) ?? '').split(/\s+/).includes(mark),
  add: addStateWord,
  remove: removeStateWord,
};

function marksOf(options: KeyedTransitionOptions): Marks {
  return 'state' in options ? STATE_MARKS : CLASS_MARKS(options.enterClass, options.exitClass);
}

/**
 * An inert copy of the given element, to be shown in its place while it leaves.
 *
 * The copy is a picture, not a control: the control it pictures is disposed on the server, so there
 * is nothing left to render. It is hidden from assistive technology, takes no input and carries no
 * ids, so that nothing addresses it by the ids of what it copies.
 */
function snapshot(element: HTMLElement, marks: Marks): HTMLElement {
  const copy = element.cloneNode(true) as HTMLElement;
  copy.removeAttribute('id');
  copy.querySelectorAll('[id]').forEach((inner) => inner.removeAttribute('id'));
  marks.remove(copy, marks.enter);
  marks.add(copy, marks.exit);
  copy.setAttribute('aria-hidden', 'true');
  copy.setAttribute('inert', '');
  return copy;
}

/** What a control displays: the keys, where each is shown, and where the copies of what left go. */
interface KeyedTransitionBase {

  /**
   * The keys of the elements displayed now, one per element, in no particular order.
   *
   * A key appearing in this list is an element arriving, a key disappearing from it is an element
   * leaving. A key identifies its element for as long as it is displayed, so a control key uses
   * what the server gives it - the id of the control shown, the name of the step.
   */
  keys: readonly string[];

  /**
   * The element a key is displayed in, or `null` while there is none.
   *
   * Asked after every change for the keys displayed, so that the element a key left behind is still
   * at hand when the key disappears - by then the element is out of the document, but a copy of it
   * is what can still be animated.
   */
  node(key: string): HTMLElement | null;

  /**
   * The element the copies of what left are shown in, or `null` while there is none.
   *
   * A copy is placed by the stylesheet, which reaches it through the exit mark; the container
   * only has to outlive the element it holds the copy of.
   */
  container(): HTMLElement | null;
}

/**
 * How the change is marked: by two classes (the wizard and the tile stack, until wave 4b), or by the
 * words `entering` and `exiting` in `data-tl-state` (`state: true`, the design system package).
 */
export type KeyedTransitionOptions = KeyedTransitionBase
  & ({ enterClass: string; exitClass: string } | { state: true });

/**
 * Marks the change of what a control displays with the marks a stylesheet animates against.
 *
 * An element whose key appears carries the enter mark until its
 * animation ends; an element whose key disappears is replaced by an inert copy of itself carrying
 * the exit mark until its animation ends, or until the same key is displayed again. The enter mark
 * stays while the element arrives even if the element's own renderer rewrites the attribute carrying
 * it in the meantime. Where the stylesheet animates nothing, mark or copy is taken away at once,
 * before the browser paints; an animation that never reports its end is cut off after
 * {@link TRANSITION_FALLBACK_MS}.
 *
 * Two things do not count as a change: the first display of a control - what a wizard opens on or
 * a stack starts with was not moved to - and any change at all for a viewer who asked for reduced
 * motion, who gets no class and no copy whatever the stylesheet says.
 *
 * The classes are put on the DOM rather than handed back for rendering, so that the element marked
 * may be rendered anywhere below the control noticing the change - the backdrop of a dialog, for
 * instance, belongs to the dialog while its opening and closing is the dialog manager's news.
 */
export function useKeyedTransition(options: KeyedTransitionOptions): void {
  const latest = useRef(options);
  latest.current = options;

  /** The element last seen for a key, kept after it left the document so it can still be copied. */
  const nodes = useRef(new Map<string, HTMLElement>()).current;

  /** Ends the arrival of a key. */
  const arriving = useRef(new Map<string, () => void>()).current;

  /** Drops the copy of a key that left. */
  const leaving = useRef(new Map<string, () => void>()).current;

  /** The keys displayed at the previous change, `null` before the first display. */
  const displayed = useRef<string[] | null>(null);

  // Runs before the browser paints what has changed, so that the classes and the copies are in
  // place by the time either the element arriving or the one leaving is seen.
  useLayoutEffect(() => {
    const { keys, node, container } = latest.current;
    const marks = marksOf(latest.current);
    const previous = displayed.current;
    displayed.current = [...keys];
    const quiet = reducedMotion();

    // A key displayed again has arrived; the copy of it leaving has nothing left to say.
    for (const key of keys) {
      leaving.get(key)?.();
    }

    for (const key of previous ?? []) {
      if (keys.includes(key)) {
        continue;
      }
      arriving.get(key)?.();
      const left = nodes.get(key);
      nodes.delete(key);
      const host = container();
      if (quiet || !left || !host) {
        continue;
      }
      const copy = snapshot(left, marks);
      host.appendChild(copy);
      const drop = whenFinished(copy, () => {
        leaving.delete(key);
        copy.remove();
      });
      if (drop) {
        leaving.set(key, drop);
      }
    }

    // The elements displayed at the first display were not moved to, so they arrive unmarked.
    if (previous !== null && !quiet) {
      for (const key of keys) {
        if (previous.includes(key)) {
          continue;
        }
        const arrived = node(key);
        if (!arrived) {
          continue;
        }
        marks.add(arrived, marks.enter);
        // The renderer of the element may write the attribute carrying the mark anew while it
        // arrives; the mark is put back before the browser recalculates styles, so the animation runs
        // on. Adding a mark writes the attribute even when the mark is present, hence the check.
        const keep = new MutationObserver(() => {
          if (!marks.has(arrived, marks.enter)) {
            marks.add(arrived, marks.enter);
          }
        });
        const end = whenFinished(arrived, () => {
          keep.disconnect();
          arriving.delete(key);
          marks.remove(arrived, marks.enter);
        });
        if (end) {
          keep.observe(arrived, { attributes: true, attributeFilter: [marks.attribute] });
          arriving.set(key, end);
        }
      }
    }

    // What each key is displayed in now, so that it can be copied once the key is gone.
    for (const key of keys) {
      const element = node(key);
      if (element) {
        nodes.set(key, element);
      }
    }
  });

  // A control taken down takes its transitions with it: neither a class on an element it no longer
  // owns nor a copy outliving the place it was shown in.
  useLayoutEffect(() => () => {
    for (const end of [...arriving.values()]) {
      end();
    }
    for (const drop of [...leaving.values()]) {
      drop();
    }
    nodes.clear();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
}
