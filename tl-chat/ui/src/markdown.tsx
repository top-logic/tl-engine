import { React } from 'tl-react-bridge'
import DOMPurify from 'dompurify'
import { marked } from 'marked'
import { highlight, languageLabel } from './highlight'
import type { bundle } from './i18n'
import type { CitationRender, PaneOpenOptions } from './extensions'
import type { ChatMessage, Citation } from './types'

const { useCallback, useEffect, useMemo, useRef, useState } = React

type Labels = ReturnType<typeof bundle>

/**
 * Above this many characters the answer is shown as plain text while it streams.
 *
 * Markdown is re-parsed on every flush, so the cost grows with the square of the answer length.
 * Long answers are rare, and the moment they finish they render as markdown like every other one.
 */
const LIVE_MARKDOWN_LIMIT = 60_000

const renderer = new marked.Renderer()

/**
 * A code block with its language named and a copy button, highlighted at parse time.
 *
 * Built here rather than by walking the DOM afterwards: the markup is produced once per parse and
 * sanitized with everything else, so nothing re-decorates a node React may have replaced.
 */
/**
 * The words the code-block header needs, read at render time.
 *
 * `marked`'s renderer is a plain function with no access to the component's props, so the current
 * bundle is parked here by the component before it parses. A module-level value is safe because
 * one language is in force for the whole page.
 */
let codeLabels: Pick<Labels, 'copyShort' | 'copied' | 'copyCode'> = {
  copyShort: 'Copy',
  copied: 'Copied',
  copyCode: 'Copy code',
}

renderer.code = ({ text, lang }) => {
  const language = (lang ?? '').trim().split(/\s+/)[0]
  const { html, used } = highlight(text, language)
  const label = languageLabel(used || language)
  return (
    '<figure class="code-block">' +
    '<figcaption class="code-head">' +
    `<span class="code-lang">${escapeHtml(label)}</span>` +
    `<button class="code-copy" type="button" data-copy aria-label="${escapeHtml(codeLabels.copyCode)}">` +
    `${escapeHtml(codeLabels.copyShort)}</button>` +
    '</figcaption>' +
    `<pre><code class="hljs${used ? ` language-${escapeHtml(used)}` : ''}">${html}</code></pre>` +
    '</figure>'
  )
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

/**
 * What the citation renderer needs while `marked` parses, parked here for the length of one parse.
 *
 * `marked`'s renderer is a plain function with no access to the component's props. The value is
 * therefore set immediately before `marked.parse()` and read by the renderer during that same
 * synchronous call, so two `Markdown` instances can never see each other's map - which they could
 * when it was set during render and read later from inside a `useMemo`.
 */
let citationLabels: Pick<Labels, 'citationMarker'> = { citationMarker: (n) => `Source ${n}` }
let citationIds = new Map<number, string>()

marked.use({
  extensions: [
    {
      name: 'citation',
      level: 'inline',
      start(src: string) {
        // The space before a marker belongs to the marker: cut the text token in front of it, so
        // the tokenizer below can swallow it and leave no line-break opportunity behind.
        const found = /[ \t]*\[\d+\]/.exec(src)
        return found ? found.index : undefined
      },
      tokenizer(src: string) {
        const match = /^[ \t]*\[(\d+)\]/.exec(src)
        if (!match) return undefined
        // Only a number this message actually cites becomes a chip. Everything else in brackets is
        // the author's own text - an array index, a footnote marker - and stays as written.
        if (!citationIds.has(Number(match[1]))) return undefined
        return { type: 'citation', raw: match[0], index: match[1] }
      },
      renderer(token) {
        const n = Number((token as { index?: string }).index)
        const id = citationIds.get(n) ?? ''
        // A small grey number after the sentence, not a coloured chip: it must say where the fact
        // comes from without pulling the eye away from the text.
        // No whitespace in front of it - the tokenizer ate it - so there is no break opportunity
        // between the word and its marker, and the gap is the button's own margin.
        return (
          `<button type="button" class="cite-marker" data-citation-index="${n}"` +
          ` data-citation-id="${escapeHtml(id)}" aria-label="${escapeHtml(citationLabels.citationMarker(n))}">` +
          `${n}</button>`
        )
      },
    },
  ],
})

marked.setOptions({ gfm: true, breaks: true, renderer })

/**
 * Parses one answer and filters the result.
 *
 * The text is model output, and a model writes down what `webFetch` and the tool results put in
 * front of it, so the markup `marked` produces from it is attacker-controlled. `DOMPurify` drops
 * everything a browser could act on - scripts, event handlers, `javascript:` targets - and is the
 * only thing standing between that text and the `dangerouslySetInnerHTML` below. The two widenings
 * are the code-block header this file writes itself: the copy button, and the attributes its click
 * handlers read.
 */
export function renderMarkdown(text: string, labels: Labels, citations?: Citation[]): string {
  // Set here rather than during render: the parse below is the only reader, and it runs
  // synchronously on the next line.
  codeLabels = labels
  citationLabels = labels
  citationIds = new Map((citations ?? []).map((item) => [item.index, item.id]))
  const raw = marked.parse(text || '') as string
  return DOMPurify.sanitize(raw, {
    ADD_TAGS: ['button'],
    ADD_ATTR: ['data-copy', 'data-citation-index', 'data-citation-id', 'type', 'aria-label'],
  })
}

/**
 * Renders markdown, sanitized.
 *
 * While `streaming` is set, the source is an unfinished document: an open fence or list is normal,
 * and the parser handles it, so the same rendering path is used and the reader sees formatted text
 * as it arrives rather than raw markup that reflows once at the end.
 */
export function Markdown({
  text,
  streaming,
  labels,
  citations,
  message,
  renderAnnotation,
  openInPane,
  onOpenSource,
}: {
  text: string
  streaming?: boolean
  labels: Labels
  citations?: Citation[]
  message?: ChatMessage
  renderAnnotation?: CitationRender
  openInPane?: (content: React.ReactNode, options: PaneOpenOptions) => void
  /** Overrides what a click on a marker does; the default asks the host, then the web. */
  onOpenSource?: (citation: Citation) => void
}) {
  const container = useRef<HTMLDivElement>(null)
  /** The marker the pointer or the focus is on: what the popover describes. */
  const [open, setOpen] = useState<number | null>(null)
  /** The marker whose source was actually opened - the host's node is mounted for it. */
  const [activated, setActivated] = useState<number | null>(null)
  /** Where the marker sits inside the answer, so the popover opens at it and not below. */
  const [anchor, setAnchor] = useState<{ top: number; left: number } | null>(null)
  const plain = streaming === true && text.length > LIVE_MARKDOWN_LIMIT

  const html = useMemo(
    () => (plain ? '' : renderMarkdown(text, labels, citations)),
    [plain, text, labels, citations],
  )

  const noop = useCallback((_content: React.ReactNode, _options: PaneOpenOptions) => undefined, [])

  const find = useCallback(
    (marker: number | null) =>
      marker == null ? undefined : (citations ?? []).find((item) => item.index === marker),
    [citations],
  )

  const show = useCallback((marker: number, el: HTMLElement) => {
    const wrap = container.current?.parentElement
    if (wrap) {
      const box = wrap.getBoundingClientRect()
      const at = el.getBoundingClientRect()
      setAnchor({
        top: at.bottom - box.top + 4,
        // Clamped, so a marker at the right edge does not push the popover out of the answer.
        left: Math.max(0, Math.min(at.left - box.left, box.width - 320)),
      })
    }
    setOpen(marker)
  }, [])

  /** What a click on a marker does: hand the source to the host, or open the web page. */
  const openSource = useCallback(
    (marker: number) => {
      const source = find(marker)
      if (!source) return
      if (onOpenSource) {
        onOpenSource(source)
        return
      }
      // The host decides what "open" means for its own kinds. When it has nothing for this one it
      // returns nothing, and a web source falls back to its page.
      if (renderAnnotation && message && renderAnnotation(marker, source, message, openInPane ?? noop)) {
        setActivated(marker)
        return
      }
      const href = source.url || (/^https?:\/\//i.test(source.id) ? source.id : '')
      if (href) window.open(href, '_blank', 'noopener,noreferrer')
    },
    [find, message, onOpenSource, openInPane, renderAnnotation],
  )

  // One delegated set of listeners rather than one per marker: the blocks are replaced on every
  // parse, so nothing may be bound to a node React will throw away.
  useEffect(() => {
    const el = container.current
    if (!el) return
    const marker = (event: Event) =>
      (event.target as HTMLElement | null)?.closest<HTMLButtonElement>('[data-citation-index]') ?? null

    const onOver = (event: MouseEvent) => {
      const hit = marker(event)
      if (hit) show(Number(hit.dataset.citationIndex), hit)
    }
    const onOut = (event: MouseEvent) => {
      const to = event.relatedTarget as HTMLElement | null
      // The popover is a sibling of the answer, so leaving the marker for it must not close it.
      if (to?.closest('.cite-anchor')) return
      if (marker(event)) setOpen(null)
    }
    const onFocus = (event: FocusEvent) => {
      const hit = marker(event)
      if (hit) show(Number(hit.dataset.citationIndex), hit)
    }
    const onClick = (event: MouseEvent) => {
      const target = event.target as HTMLElement | null
      const hit = marker(event)
      if (hit) {
        event.preventDefault()
        openSource(Number(hit.dataset.citationIndex))
        return
      }
      const button = target?.closest<HTMLButtonElement>('[data-copy]')
      if (!button) return
      const code = button.closest('.code-block')?.querySelector('code')?.textContent ?? ''
      void navigator.clipboard
        .writeText(code)
        .then(() => {
          button.textContent = codeLabels.copied
          button.classList.add('is-done')
          window.setTimeout(() => {
            button.textContent = codeLabels.copyShort
            button.classList.remove('is-done')
          }, 1500)
        })
        .catch(() => {
          /* the clipboard may be blocked; the code stays selectable */
        })
    }
    el.addEventListener('mouseover', onOver)
    el.addEventListener('mouseout', onOut)
    el.addEventListener('focusin', onFocus)
    el.addEventListener('focusout', onOut as EventListener)
    el.addEventListener('click', onClick)
    return () => {
      el.removeEventListener('mouseover', onOver)
      el.removeEventListener('mouseout', onOut)
      el.removeEventListener('focusin', onFocus)
      el.removeEventListener('focusout', onOut as EventListener)
      el.removeEventListener('click', onClick)
    }
  }, [openSource, show])

  // Escape closes the popover, like every other overlay in the chat.
  useEffect(() => {
    if (open == null) return undefined
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(null)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open])

  /** Whether anything can actually open this source: a host node, or a web address. */
  const canOpen = useCallback(
    (source: Citation) => {
      if (onOpenSource) return true
      if (renderAnnotation && message && renderAnnotation(source.index, source, message, noop)) return true
      return Boolean(source.url || /^https?:\/\//i.test(source.id))
    },
    [message, noop, onOpenSource, renderAnnotation],
  )

  const hovered = find(open)
  const opened = find(activated)
  const hostNode =
    opened && message && renderAnnotation
      ? renderAnnotation(opened.index, opened, message, openInPane ?? noop)
      : null

  if (plain) {
    return (
      <div className="md md-plain" ref={container}>
        {text}
      </div>
    )
  }
  return (
    <div className="md-wrap">
      <div className="md" ref={container} dangerouslySetInnerHTML={{ __html: html }} />
      {hovered ? (
        <div
          className="cite-anchor"
          style={anchor ? { top: anchor.top, left: anchor.left } : undefined}
          onMouseLeave={() => setOpen(null)}
        >
          <SourcePopover
            citation={hovered}
            labels={labels}
            canOpen={canOpen(hovered)}
            onOpen={() => openSource(hovered.index)}
          />
        </div>
      ) : null}
      {hostNode}
    </div>
  )
}

/**
 * What a marker says when the reader looks at it: the source, where it comes from, and the sentence
 * the fact rests on - inside the window of the source it was read in.
 */
export function SourcePopover({
  citation,
  labels,
  onOpen,
  canOpen = true,
}: {
  citation: Citation
  labels: Labels
  onOpen: () => void
  /** False when nothing in this application can open the source; the action is then not offered. */
  canOpen?: boolean
}) {
  return (
    <div className="cite-popover" role="dialog" aria-label={citation.label || citation.id}>
      <p className="cite-popover-title">{citation.label || citation.id}</p>
      <p className="cite-popover-origin">{sourceOrigin(citation)}</p>
      {citation.excerpt ? (
        <p className="cite-popover-excerpt">
          <Excerpt excerpt={citation.excerpt} passage={citation.passage ?? ''} />
        </p>
      ) : null}
      {canOpen ? (
        <button type="button" className="cite-popover-open" onClick={onOpen}>
          {labels.openSource}
        </button>
      ) : null}
    </div>
  )
}

/**
 * Collapses whitespace runs to one space and lowercases, keeping a map from each normalized
 * character back to its index in the original text. The trailing entry is the original length, so a
 * match that ends at the last character still has an end boundary.
 */
function normalizeWithMap(text: string): { norm: string; map: number[] } {
  let norm = ''
  const map: number[] = []
  let prevSpace = false
  for (let i = 0; i < text.length; i++) {
    const ch = text[i]
    if (/\s/.test(ch)) {
      if (norm.length > 0 && !prevSpace) {
        norm += ' '
        map.push(i)
        prevSpace = true
      }
      continue
    }
    norm += ch.toLowerCase()
    map.push(i)
    prevSpace = false
  }
  map.push(text.length)
  return { norm, map }
}

/**
 * The excerpt with the cited sentence marked inside it.
 *
 * The match is tolerant: the server's verbatim passage and the excerpt window it was cut from can
 * differ in whitespace or case, and an exact substring search then silently found nothing and showed
 * no highlight. We match on a whitespace-collapsed, lowercased form, then highlight the corresponding
 * slice of the original text. A genuinely absent passage still degrades to the plain excerpt.
 */
function Excerpt({ excerpt, passage }: { excerpt: string; passage: string }) {
  const needle = passage.replace(/\s+/g, ' ').trim().toLowerCase()
  if (!needle) return <>{excerpt}</>
  const { norm, map } = normalizeWithMap(excerpt)
  const at = norm.indexOf(needle)
  if (at < 0) return <>{excerpt}</>
  const start = map[at]
  const end = map[at + needle.length] ?? excerpt.length
  return (
    <>
      {excerpt.slice(0, start)}
      <mark className="cite-passage">{excerpt.slice(start, end)}</mark>
      {excerpt.slice(end)}
    </>
  )
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
  const href = citation.url || (/^https?:\/\//i.test(citation.id) ? citation.id : '')
  if (href) {
    try {
      return new URL(href).hostname
    } catch {
      return href
    }
  }
  return citation.kindLabel || citation.kind || ''
}
