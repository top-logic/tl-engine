import type { React } from 'tl-react-bridge';

/**
 * The radio pattern of a bar of segments: the arrows move the selection - right and down to the next
 * segment, left and up to the previous one - and wrap around at the ends.
 *
 * <p>For an arrow key, consumes the event (no outer scope takes it) and returns the index of the segment the
 * selection moves to, counted from {@code selected}; for any other key it leaves the event alone and returns
 * {@code null}. Focusing the segment and choosing it is the caller's.</p>
 *
 * @param selected the index of the selected segment, negative where none is: a step forward then reaches the first
 *                 segment, a step back the last one
 * @param count the number of segments
 */
export function radioArrowTarget(event: React.KeyboardEvent, selected: number, count: number): number | null {
  let step: number;
  if (event.key === 'ArrowRight' || event.key === 'ArrowDown') {
    step = 1;
  } else if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') {
    step = -1;
  } else {
    return null;
  }
  event.preventDefault();
  event.stopPropagation();
  const from = selected < 0 ? (step > 0 ? -1 : 0) : selected;
  return (from + step + count) % count;
}
