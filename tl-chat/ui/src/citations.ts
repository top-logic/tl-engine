/**
 * Citation markers of the form {@code [n]} in assistant markdown.
 *
 * Which of them become chips is decided by the message's own citation list: the server has already
 * dropped invented ids and rewritten the numbers, and a bracketed number it left alone is the
 * author's text - an array index, a footnote - not a citation.
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
