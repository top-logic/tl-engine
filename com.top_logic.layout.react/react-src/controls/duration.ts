/**
 * Formatting of a duration for the displays that count one: a count-down towards an announced
 * moment, the time a running job has been at work.
 */

/** Tick interval of a display counting seconds, in ms. */
export const TICK_MS = 1000;

/**
 * Formats a duration in ms as `m:ss`, or `h:mm:ss` once it reaches an hour.
 *
 * A duration below zero reads as `0:00`: a count-down stops at its end rather than running
 * negative, and an elapsed time measured against a clock that is slightly ahead starts there.
 */
export function formatDuration(millis: number): string {
  const total = Math.max(0, Math.floor(millis / 1000));
  const seconds = total % 60;
  const minutes = Math.floor(total / 60) % 60;
  const hours = Math.floor(total / 3600);
  const pad = (value: number) => (value < 10 ? `0${value}` : `${value}`);
  return hours > 0
    ? `${hours}:${pad(minutes)}:${pad(seconds)}`
    : `${minutes}:${pad(seconds)}`;
}
