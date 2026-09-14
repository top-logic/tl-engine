/**
 * Citation markers of the form {@code [n]} in assistant markdown.
 *
 * The markdown renderer turns each of these into a chip. The server has already
 * dropped invented ids and rewritten the numbers; this only finds what to chip.
 */
export function citationMarkerIndexes(text: string): number[] {
  const seen: number[] = []
  const match = /\[(\d+)\]/g
  let found: RegExpExecArray | null
  while ((found = match.exec(text))) {
    const n = Number(found[1])
    if (!seen.includes(n)) seen.push(n)
  }
  return seen
}
