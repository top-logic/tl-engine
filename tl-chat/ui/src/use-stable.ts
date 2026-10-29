import { React } from 'tl-react-bridge'

const { useRef } = React

/**
 * `next`, or the value of the previous render when `same` finds nothing in it changed.
 *
 * A value derived afresh on every render - a filtered list, a resolved set - is a new object each
 * time even when it holds what it held before, and a memoised child sees every new object as a
 * change. The thread re-renders on every streamed word, so that was every finished turn, every time.
 */
export function useStable<T>(next: T, same: (previous: T, next: T) => boolean): T {
  const kept = useRef(next)
  if (kept.current !== next && !same(kept.current, next)) kept.current = next
  return kept.current
}

/** Whether two lists hold the same items, in the same order. */
export function sameItems<T>(a: readonly T[], b: readonly T[]): boolean {
  return a.length === b.length && a.every((item, index) => item === b[index])
}

/** Whether two sets hold the same members. */
export function sameMembers<T>(a: ReadonlySet<T>, b: ReadonlySet<T>): boolean {
  if (a.size !== b.size) return false
  for (const item of a) if (!b.has(item)) return false
  return true
}

/** Whether two objects hold the same values under the same keys - one level deep. */
export function sameFields(a: object, b: object): boolean {
  const left = a as Record<string, unknown>
  const right = b as Record<string, unknown>
  const keys = Object.keys(left)
  return keys.length === Object.keys(right).length && keys.every((key) => left[key] === right[key])
}
