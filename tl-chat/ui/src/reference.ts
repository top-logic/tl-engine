/**
 * A passage a host put into the composer, and how it travels.
 *
 * The reference is part of the sentence, not an attachment to it: "shorten [this] and expand
 * [that]" needs two of them at two positions in one message, which is why it is an inline token
 * rather than a chip above the field.
 */
export interface ReferenceToken {
  /** Host kind, e.g. `invoice` or `document`. Decides the icon. */
  kind: string
  /** Id of the referenced object, which travels as `ref` and identifies it for the host. */
  id: string
  /** What the reader sees: the source's name. */
  label: string
  /** Where in the source, e.g. a section title or `L87-93`. Optional. */
  location?: string
  /** The quoted passage itself - what the model is meant to read. */
  text: string
}

/** One run of a draft: typed words, or one atomic reference. */
export type DraftPart = { type: 'text'; text: string } | { type: 'ref'; token: ReferenceToken }

/** Matches one serialised reference; `location` is optional and may come in any attribute order. */
const QUOTE = /<quote\s+([^>]*?)>([\s\S]*?)<\/quote>/g

/** A `<context>…</context>` prefix the framework adds on send; never part of what the user wrote. */
const CONTEXT_PREFIX = /^<context>[\s\S]*?<\/context>\n?/

/**
 * How one reference travels to the model: at its position in the sentence, with the quoted text as
 * the element's content.
 *
 * `location` is written only when the host set one. It is not in the shape issue #407 sketches, but
 * dropping it would lose the only thing that says *which* passage of the source was meant.
 */
export function serializeReference(token: ReferenceToken): string {
  const attributes = [`label="${escapeAttribute(token.label)}"`, `ref="${escapeAttribute(token.id)}"`]
  if (token.location?.trim()) attributes.push(`location="${escapeAttribute(token.location.trim())}"`)
  if (token.kind?.trim()) attributes.push(`kind="${escapeAttribute(token.kind.trim())}"`)
  return `<quote ${attributes.join(' ')}>${escapeText(token.text)}</quote>`
}

/**
 * Splits a draft or a sent message into its typed words and its references.
 *
 * The same parser serves the composer (which rebuilds its tokens from the text), the thread (which
 * renders a sent message the same way) and the inline editor.
 */
export function parseDraft(source: string): DraftPart[] {
  const parts: DraftPart[] = []
  if (!source) return parts
  let last = 0
  QUOTE.lastIndex = 0
  let match: RegExpExecArray | null
  while ((match = QUOTE.exec(source))) {
    if (match.index > last) parts.push({ type: 'text', text: source.slice(last, match.index) })
    const attributes = readAttributes(match[1])
    parts.push({
      type: 'ref',
      token: {
        kind: attributes.kind ?? '',
        id: attributes.ref ?? '',
        label: attributes.label ?? attributes.ref ?? '',
        location: attributes.location ?? '',
        text: unescapeText(match[2]),
      },
    })
    last = match.index + match[0].length
  }
  if (last < source.length) parts.push({ type: 'text', text: source.slice(last) })
  return parts
}

/** Puts the parts back together, the inverse of {@link parseDraft}. */
export function draftToText(parts: DraftPart[]): string {
  return parts
    .map((part) => (part.type === 'text' ? part.text : serializeReference(part.token)))
    .join('')
}

/** Whether a draft says anything at all - a reference alone is a message, empty words are not. */
export function draftIsEmpty(source: string): boolean {
  return parseDraft(source).every((part) => (part.type === 'text' ? part.text.trim() === '' : false))
}

/** The message as the user wrote it: without the `<context/>` prefix the framework prepends. */
export function withoutContextPrefix(text: string): string {
  return text.replace(CONTEXT_PREFIX, '')
}

/** What a reference reads as when it has to be one line of plain words. */
export function referenceSummary(token: ReferenceToken): string {
  return token.location?.trim() ? `${token.label} ${token.location.trim()}` : token.label
}

function readAttributes(raw: string): Record<string, string> {
  const result: Record<string, string> = {}
  const attribute = /([a-zA-Z-]+)="([^"]*)"/g
  let found: RegExpExecArray | null
  while ((found = attribute.exec(raw))) {
    result[found[1]] = unescapeText(found[2])
  }
  return result
}

function escapeAttribute(value: string): string {
  return escapeText(value).replace(/"/g, '&quot;')
}

function escapeText(value: string): string {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function unescapeText(value: string): string {
  return value
    .replace(/&quot;/g, '"')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
}
