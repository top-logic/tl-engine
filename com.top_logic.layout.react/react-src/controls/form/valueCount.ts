/**
 * Whether a field holding the given number of values holds the maximum its control publishes under
 * the state key `maxCount` (see `ValueCountLimit#MAX_COUNT`), so that it offers no further value.
 *
 * @param count the number of values the field holds
 * @param maxCount the `maxCount` of the control state: the maximum number of values, `0` or absent
 *        for no limit
 */
export function isFull(count: number, maxCount: unknown): boolean {
  const max = typeof maxCount === 'number' ? maxCount : 0;
  return max > 0 && count >= max;
}
