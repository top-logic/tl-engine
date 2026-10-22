import { React } from 'tl-react-bridge'

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

/**
 * Where a source comes from: the web host for a URL, otherwise the kind in the reader's language.
 *
 * `kind` is the model's vocabulary and stays the same English word in every installation, so a
 * German chat used to say `section` and `material` under its sources. The host declares a label
 * beside the kind and that is what a reader sees; without one we still show the kind.
 */
export function sourceOrigin(citation: {
  kind: string
  kindLabel?: string
  url: string
  id: string
}): string {
  const href = sourceHref(citation)
  if (href) {
    try {
      return new URL(href).hostname
    } catch {
      return href
    }
  }
  return citation.kindLabel || citation.kind || ''
}

/** The address a source can be opened at, or the empty string when it has none. */
export function sourceHref(citation: { url: string; id: string }): string {
  return citation.url || (/^https?:\/\//i.test(citation.id) ? citation.id : '')
}

/**
 * Which part of the source this citation points at, beside the origin: the page of a site, the id
 * of a record.
 *
 * The reader needs it because a label alone does not say *which* one - two sections of one document
 * and two pages of one site carry the same origin, and the popover would describe both the same
 * way. Empty when the source has nothing to add: the id is already the label, or is the URL whose
 * host is the origin.
 */
export function sourceDetail(citation: { url: string; id: string; label: string }): string {
  const href = sourceHref(citation)
  if (href) {
    try {
      const path = new URL(href).pathname
      return path === '/' ? '' : path
    } catch {
      return ''
    }
  }
  const id = citation.id ?? ''
  return id && id !== citation.label ? id : ''
}

/**
 * The `label: value` lines of a structured source's excerpt, or `null` when it is prose.
 *
 * A kind declared `passage="false"` has fields rather than a sentence, and the server's excerpt for
 * it is one `label: value` per line. Rendered as text that was a run of grey lines with nothing to
 * look at (#495); as a field list the reader sees the record. Prose is never treated this way: a
 * citation that carries a passage is a sentence, whatever colons it contains.
 */
export function excerptFields(excerpt: string, passage: string): { name: string; value: string }[] | null {
  if (passage.trim() || !excerpt) return null
  const lines = excerpt.split('\n').map((line) => line.trim()).filter(Boolean)
  if (lines.length < 2) return null
  const fields: { name: string; value: string }[] = []
  for (const line of lines) {
    const found = /^([^:]{1,32}):[ \t]+(.+)$/.exec(line)
    if (!found) return null
    fields.push({ name: found[1].trim(), value: found[2].trim() })
  }
  return fields
}

/** A document for a host kind, a globe for the web. */
export function kindIcon(kind: string): React.ReactNode {
  if (!kind || kind === 'web') {
    return (
      <svg viewBox="0 0 16 16" aria-hidden="true">
        <circle cx="8" cy="8" r="5.8" />
        <path d="M2.4 8h11.2M8 2.2c1.6 1.7 2.4 3.6 2.4 5.8s-.8 4.1-2.4 5.8c-1.6-1.7-2.4-3.6-2.4-5.8s.8-4.1 2.4-5.8z" />
      </svg>
    )
  }
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M4 2.5h5.2L12 5.3v8.2H4zM9 2.6v3h3M6 8.4h4M6 10.8h4" />
    </svg>
  )
}
