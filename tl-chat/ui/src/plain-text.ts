/**
 * Model text as a plain line: the Markdown markers a model writes, taken out.
 *
 * For what a card shows of a subagent's report or of the text it is writing - its own words, which
 * may repeat what it read, and which a card shows as text and never renders (handover 9.5). The
 * result is meant for a text node: nothing here makes it safe as markup, nor needs to.
 */
export function plainText(markdown: string): string {
  return markdown
    .replace(/```[^\n]*\n?/g, '')
    .replace(/!\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/^\s{0,3}#{1,6}\s+/gm, '')
    .replace(/^\s{0,3}>\s?/gm, '')
    .replace(/^\s*(?:[-*+]|\d+[.)])\s+/gm, '')
    .replace(/(\*\*|__)(.+?)\1/g, '$2')
    .replace(/(^|[^*\w])\*(?!\s)([^*\n]+?)\*(?!\w)/g, '$1$2')
    .replace(/(^|[^_\w])_(?!\s)([^_\n]+?)_(?!\w)/g, '$1$2')
    .replace(/`([^`]*)`/g, '$1')
    .replace(/\*\*|__/g, '')
    .replace(/[ \t]+/g, ' ')
    .trim()
}

/** How long an argument summary of a step may be. */
const MAX_ARGS = 80

/**
 * The arguments of a tool call as a plain line: the values the call was made with, without the JSON
 * around them - `getInstance GenericObject:2974, GenericObject:281` rather than
 * `getInstance {"tids":["GenericObject:2974", …`.
 *
 * Key names are left out (the tool's id says what the values are for), nested objects too. What is
 * not a JSON object is shown as the text it is.
 */
export function argsSummary(argsJson: string | undefined): string {
  const text = (argsJson ?? '').trim()
  if (!text) return ''
  let args: unknown
  try {
    args = JSON.parse(text)
  } catch {
    return cut(text.replace(/\s+/g, ' '))
  }
  if (args === null || typeof args !== 'object' || Array.isArray(args)) return cut(valueText(args))
  const values = Object.values(args as Record<string, unknown>)
    .map(valueText)
    .filter((value) => value !== '')
  return cut(values.join(', '))
}

function valueText(value: unknown): string {
  if (typeof value === 'string') return value.replace(/\s+/g, ' ').trim()
  if (typeof value === 'number' || typeof value === 'boolean') return String(value)
  if (Array.isArray(value)) return value.map(valueText).filter((item) => item !== '').join(', ')
  return ''
}

function cut(text: string): string {
  return text.length > MAX_ARGS ? `${text.slice(0, MAX_ARGS)}…` : text
}
