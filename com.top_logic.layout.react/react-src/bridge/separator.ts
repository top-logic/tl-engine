import { tokenPx } from './token-px';

/**
 * The keyboard of a separator that resizes the areas beside it - the handle of a split panel, the width handle of a
 * table column (role="separator", focusable). One place for both, so that they move alike.
 *
 * <p>The arrows across the separator move it by one step, `--tl-space-md`; Home and End move it to the smallest and
 * the largest size the area before it may take. A separator between areas side by side
 * (`aria-orientation="vertical"`) moves with left and right - mirrored in a right-to-left page -, one between areas
 * on top of each other (`horizontal`) with up and down.</p>
 */
export type SeparatorOrientation = 'horizontal' | 'vertical';

/** What a key does to the size of the area before the separator. */
export type SeparatorMove = { to: 'step'; steps: number } | { to: 'min' } | { to: 'max' };

/**
 * The move a key makes, or `null` for a key that does not move the separator.
 *
 * @param element The separator; its writing direction decides which arrow grows the area before it.
 */
export function separatorMove(key: string, orientation: SeparatorOrientation, element: Element | null): SeparatorMove | null {
  if (orientation === 'vertical') {
    const rtl = element != null && getComputedStyle(element).direction === 'rtl';
    if (key === 'ArrowRight') {
      return { to: 'step', steps: rtl ? -1 : 1 };
    }
    if (key === 'ArrowLeft') {
      return { to: 'step', steps: rtl ? 1 : -1 };
    }
  } else {
    if (key === 'ArrowDown') {
      return { to: 'step', steps: 1 };
    }
    if (key === 'ArrowUp') {
      return { to: 'step', steps: -1 };
    }
  }
  if (key === 'Home') {
    return { to: 'min' };
  }
  if (key === 'End') {
    return { to: 'max' };
  }
  return null;
}

/** The length one arrow moves a separator by, in pixels: `--tl-space-md` at the separator. */
export function separatorStep(element: Element | null): number {
  return tokenPx(element, '--tl-space-md', 16);
}

/** The size of the area before the separator after the move, bounded by its smallest and largest size. */
export function movedSize(size: number, move: SeparatorMove, min: number, max: number, step: number): number {
  const next = move.to === 'min' ? min : move.to === 'max' ? max : size + move.steps * step;
  return Math.round(Math.min(max, Math.max(min, next)));
}
