import { React } from 'tl-react-bridge'
import DOMPurify from 'dompurify'
import { marked } from 'marked'
import { excerptFields, kindIcon, sourceDetail, sourceHref, sourceOrigin } from './citations'
import { highlight, languageLabel } from './highlight'
import type { bundle } from './i18n'
import type { CitationRender, PaneOpenOptions } from './extensions'
import type { ChatMessage, Citation } from './types'

const { memo, useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } = React

type Labels = ReturnType<typeof bundle>

/**
 * Above this many characters in the *unfinished* block the answer is shown as plain text while it
 * streams.
 *
 * What is re-parsed on every flush is the tail behind the last block boundary (#510), so that is
 * what the ceiling is about: one block that large is a code fence or a paragraph nobody closed, and
 * for those the split can do nothing. An answer that is merely long streams as markdown, however
 * long it gets, and the moment it finishes it renders as markdown like every other one.
 */
const LIVE_MARKDOWN_LIMIT = 60_000

/** The space between a marker and its popover. */
const GAP = 4

/**
 * The box a popover has to stay inside: the scrolling ancestor of the marker, or the window when
 * it has none.
 *
 * What bounds it is the transcript, not the viewport - the composer sits over the bottom of the
 * page, so a popover on the last answer is cut off long before the window ends.
 */
function visibleBox(el: HTMLElement): { top: number; bottom: number } {
  for (let node = el.parentElement; node; node = node.parentElement) {
    const overflow = window.getComputedStyle(node).overflowY
    if (overflow === 'auto' || overflow === 'scroll') return node.getBoundingClientRect()
  }
  return { top: 0, bottom: window.innerHeight }
}

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
 * only thing standing between that text and the page: every route into the answer's container
 * below goes through here. The two widenings are the code-block header this file writes itself: the
 * copy button, and the attributes its click handlers read.
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
 * What a block of an answer is, as far as splitting the answer in two is concerned.
 *
 * Only the two distinctions `merges` needs; everything else is `text`. A fenced code block is
 * `text` - two of them side by side are two blocks, and what keeps a boundary out of the *inside*
 * of a fence is the scanner in `stablePrefixEnd`, not a kind - and so are a heading, a table and a
 * quotation.
 */
type BlockKind = 'text' | 'list' | 'html'

/**
 * Where a block starts in the source, what it is, how far it is indented, and whether its first
 * line is still being written.
 *
 * A block's kind is read from its first line, so a line the stream has not finished has no settled
 * kind yet: `2` is a paragraph and `2.` is a list. Such a block is no boundary, which is what makes
 * the boundary move forwards only - see `stablePrefixEnd`.
 */
type Block = { at: number; kind: BlockKind; indent: number; open: boolean }

/**
 * Whether two neighbouring blocks are one block when they are read together - which is what a
 * boundary between them would render as two.
 *
 * Put to `marked` as a matrix over every block shape an answer is made of, three pairs come back
 * merged, and only three: two lists (a blank line between items makes one *loose* list, not two
 * lists), a list and an indented block behind it (which is the item's own content), and two
 * indented code blocks (one code block with a blank line in it). Two quotations and two tables are
 * not among them. Raw HTML is, whichever side it is on: an unbalanced block is closed at the end of
 * each half by the sanitizer, so the second half lands outside the element instead of inside it.
 * `markdown.test.ts` puts the same matrix to this function.
 */
function merges(before: Block, after: Block): boolean {
  // Anything indented can be the content of the list item or the code block in front of it - which
  // is also both indented-code cases, an indented code block never starting at the left margin.
  if (after.indent > 0) return true
  if (before.kind === 'html' || after.kind === 'html') return true
  return before.kind === 'list' && after.kind === 'list'
}

/** The code fence a line opens or closes, if it is one. */
function fenceRun(line: string): { char: string; len: number; rest: string } | null {
  const found = /^ {0,3}(`{3,}|~{3,})(.*)$/.exec(line)
  return found ? { char: found[1][0], len: found[1].length, rest: found[2] } : null
}

/** What kind of block a line opens, and how far it is indented. */
function classify(line: string): { kind: BlockKind; indent: number } {
  const lead = /^[ \t]*/.exec(line)?.[0] ?? ''
  const indent = lead.replace(/\t/g, '    ').length
  const body = line.slice(lead.length)
  if (/^(?:[-*+]|\d{1,9}[.)])(?:[ \t]|$)/.test(body)) return { kind: 'list', indent }
  if (body.startsWith('<')) return { kind: 'html', indent }
  return { kind: 'text', indent }
}

/** A line that defines a link label - which a block anywhere else in the answer may use. */
const LINK_DEFINITION = /^ {0,3}\[[^\]\n]+\]:/

/**
 * Where the finished part of a streamed answer ends.
 *
 * An answer is append-only until its last delta, so everything in front of the last block boundary
 * is final and has to be rendered only once. A boundary is a blank line outside a code fence, and
 * only one the answer can be parsed across:
 *
 * - the blocks on either side of it must not be blocks that are one block when read together
 *   (`merges`, which is also where the indented case is),
 * - its first line must be finished, or its kind is not settled yet,
 * - and no line may define a link label, which blocks on both sides read.
 *
 * Every one of those reads lines the stream has already finished, and what a line says never
 * changes afterwards, so the boundary of a growing answer only ever moves forwards. A link
 * definition is the exception, and the only one: it turns text written earlier into a link, so an
 * answer that writes one is rendered again from the start.
 *
 * Returns 0 when there is no such boundary; the answer is then rendered whole, as it always was.
 */
export function stablePrefixEnd(text: string): number {
  const blocks: Block[] = []
  let fence: { char: string; len: number } | null = null
  let opens = true
  let at = 0
  for (;;) {
    const brk = text.indexOf('\n', at)
    const end = brk < 0 ? text.length : brk
    const line = text.slice(at, end)
    if (fence) {
      const run = fenceRun(line)
      if (run && run.char === fence.char && run.len >= fence.len && run.rest.trim() === '') fence = null
    } else if (line.trim() === '') {
      opens = true
    } else {
      if (LINK_DEFINITION.test(line)) return 0
      if (opens) {
        blocks.push({ at, ...classify(line), open: brk < 0 })
        opens = false
      }
      const run = fenceRun(line)
      if (run) fence = { char: run.char, len: run.len }
    }
    if (brk < 0) break
    at = brk + 1
  }
  for (let i = blocks.length - 1; i > 0; i--) {
    if (blocks[i].open) continue
    if (merges(blocks[i - 1], blocks[i])) continue
    return blocks[i].at
  }
  return 0
}

/**
 * What a container holds, so the next update can extend it instead of rebuilding it.
 *
 * The answer's nodes are not React's: the whole point is that a delta must not hand React a new
 * string for the whole answer, so what is in the container is tracked here instead.
 */
type Rendered = {
  /** The element the rest of this record describes; a different one starts over. */
  el: HTMLElement | null
  /** Whether the container holds one render of the whole answer rather than a prefix and a tail. */
  whole: boolean
  /** The markdown of the committed prefix - the blocks whose nodes are final. */
  source: string
  /** The HTML that prefix produced. */
  html: string
  /** How many of the container's children the prefix produced; everything after them is the tail. */
  count: number
  /** The HTML the tail produced, so an unchanged tail is not written again. */
  tail: string
  /** The labels the content was rendered with; new ones invalidate all of it. */
  labels: Labels | null
  /** The citation markers the content was rendered with, as `index:id` pairs. */
  cites: string
}

/**
 * The box the answer is written into, rendered exactly once.
 *
 * React is given the first render of the answer, so the markup carries it before any effect has run
 * - a component rendered to a string on a server has no other chance - and is then told to leave
 * the element alone for good. The comparator is what says that: without it React re-applies
 * `dangerouslySetInnerHTML` on every render of the parent and wipes the nodes `applyStreamed` put
 * there, which is not something a React version is obliged to spare us.
 */
const Surface = memo(
  function Surface({ html, forward }: { html: string; forward: React.RefObject<HTMLDivElement | null> }) {
    return <div className="md" ref={forward} dangerouslySetInnerHTML={{ __html: html }} />
  },
  () => true,
)

/** The nodes `html` parses to, as a fragment that inserts in one DOM mutation. */
function fragmentOf(html: string): DocumentFragment {
  const template = document.createElement('template')
  template.innerHTML = html
  return template.content
}

/**
 * Writes the finished answer, rendered in one piece.
 *
 * The two halves the stream left behind are expected to say exactly this, and then nothing is
 * written. That check is what makes the split safe: an answer whose halves ever parsed differently
 * from the whole is corrected here, once, the moment it is finished.
 */
function applyWhole(el: HTMLElement, state: Rendered, html: string, labels: Labels, cites: string) {
  if (
    state.el !== el ||
    state.labels !== labels ||
    state.cites !== cites ||
    state.html + state.tail !== html
  ) {
    el.innerHTML = html
  }
  state.el = el
  state.whole = true
  state.source = ''
  state.html = ''
  state.count = 0
  state.tail = html
  state.labels = labels
  state.cites = cites
}

/**
 * Extends a streamed answer: the blocks that are finished are written once and then left alone,
 * and only the unfinished tail behind them is replaced.
 */
function applyStreamed(
  el: HTMLElement,
  state: Rendered,
  prefix: string,
  tail: string,
  labels: Labels,
  citations: Citation[] | undefined,
  cites: string,
) {
  if (
    state.el !== el ||
    state.whole ||
    state.labels !== labels ||
    state.cites !== cites ||
    // A regenerated answer is not an extension of the one before it.
    !prefix.startsWith(state.source) ||
    // Nothing but this function may write the container, but if something did, the record is not a
    // description of it any more and the committed nodes are rebuilt rather than half-replaced.
    el.childNodes.length < state.count
  ) {
    el.replaceChildren()
    state.el = el
    state.whole = false
    state.source = ''
    state.html = ''
    state.count = 0
    state.tail = ''
    state.labels = labels
    state.cites = cites
  }
  if (prefix.length > state.source.length) {
    const added = renderMarkdown(prefix.slice(state.source.length), labels, citations)
    const fragment = fragmentOf(added)
    const count = fragment.childNodes.length
    el.insertBefore(fragment, el.childNodes[state.count] ?? null)
    state.source = prefix
    state.html += added
    state.count += count
  }
  const html = renderMarkdown(tail, labels, citations)
  if (html === state.tail) return
  if (el.childNodes.length > state.count) {
    // One mutation for the whole tail rather than one per node.
    const range = document.createRange()
    range.setStart(el, state.count)
    range.setEnd(el, el.childNodes.length)
    range.deleteContents()
  }
  el.appendChild(fragmentOf(html))
  state.tail = html
}

/**
 * Renders markdown, sanitized.
 *
 * While `streaming` is set, the source is an unfinished document: an open fence or list is normal,
 * and the parser handles it, so the same rendering path is used and the reader sees formatted text
 * as it arrives rather than raw markup that reflows once at the end. What it is *not* is a new
 * document per delta: the finished blocks are written once and the unfinished tail behind them is
 * the only thing a delta touches - see `stablePrefixEnd` and `applyStreamed`.
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
  /**
   * Where the marker sits inside the answer, so the popover opens at it and not below.
   *
   * `height` is the marker's own; `room` and `above` are the space under and over it inside the
   * transcript, which is what decides whether the popover opens downwards or upwards.
   */
  const [anchor, setAnchor] = useState<
    { top: number; left: number; height: number; room: number; above: number } | null
  >(null)
  /** The popover container, measured once rendered to decide whether it still fits below. */
  const popover = useRef<HTMLDivElement>(null)
  /**
   * Whether the popover opens above its marker.
   *
   * The last answer of a thread sits right over the composer, so a popover that opens downwards
   * there is cut off at the bottom and its "open the source" action is unreachable (#495).
   */
  const [flipped, setFlipped] = useState(false)
  /**
   * Where the answer's finished blocks end while it streams.
   *
   * Everything in front of it is written into the container once; only the tail behind it is
   * re-rendered per delta, so the DOM mutations of a delta stop growing with the answer (#510).
   */
  const boundary = useMemo(() => (streaming === true ? stablePrefixEnd(text) : 0), [streaming, text])
  /** Kept as strings, so an unchanged half compares equal however often the component renders. */
  const prefixSource = text.slice(0, boundary)
  const tailSource = text.slice(boundary)
  const plain = streaming === true && tailSource.length > LIVE_MARKDOWN_LIMIT
  /** What decides the markup besides the text: a marker's number and the id behind it. */
  const citeKey = useMemo(
    () => (citations ?? []).map((item) => `${item.index}:${item.id}`).join(','),
    [citations],
  )
  /** The finished answer in one piece - see `applyWhole`; `null` for as long as it streams. */
  const wholeHtml = useMemo(
    () => (streaming === true ? null : renderMarkdown(text, labels, citations)),
    [streaming, text, labels, citations],
  )
  /**
   * The answer as the container is first given it.
   *
   * React writes this once, when the element is created, and never again - what it compares the
   * value against never changes - so `applyWhole` and `applyStreamed` own the container from the
   * first layout effect on. It is here because the markup has to carry the answer before any effect
   * has run: rendered on a server, or read by a host that renders this component to a string, an
   * empty box is not the answer.
   */
  const initial = useRef<string | null>(null)
  if (initial.current === null) initial.current = plain ? '' : renderMarkdown(text, labels, citations)
  /** What the container holds, so a delta extends it instead of rebuilding it. */
  const rendered = useRef<Rendered>({
    el: null,
    // What React wrote is one render of the whole text, and it is all tail: whichever of the two
    // writers runs first therefore finds the container already saying what it was about to say.
    whole: false,
    source: '',
    html: '',
    count: 0,
    tail: initial.current,
    labels,
    cites: citeKey,
  })

  // The answer is written into the container by hand rather than through `dangerouslySetInnerHTML`:
  // React hands the DOM the whole string again whenever it changes, and a delta changes it, which is
  // the rebuild #510 measured. The container's children stay exactly what one render of the whole
  // text produces, so `.md > *:first-child` / `:last-child` and the streaming caret on the last
  // block see the same DOM as before.
  useLayoutEffect(() => {
    const el = container.current
    if (!el || plain) return
    // The first effect of a mounted component adopts what React wrote from `initial`, rather than
    // writing the same answer a second time.
    if (rendered.current.el === null) rendered.current.el = el
    if (wholeHtml != null) applyWhole(el, rendered.current, wholeHtml, labels, citeKey)
    else applyStreamed(el, rendered.current, prefixSource, tailSource, labels, citations, citeKey)
  }, [citations, citeKey, labels, plain, prefixSource, tailSource, wholeHtml])

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
      const visible = visibleBox(el)
      setAnchor({
        top: at.bottom - box.top + GAP,
        // Clamped, so a marker at the right edge does not push the popover out of the answer.
        left: Math.max(0, Math.min(at.left - box.left, box.width - 320)),
        height: at.height,
        room: visible.bottom - at.bottom,
        above: at.top - visible.top,
      })
    }
    setFlipped(false)
    setOpen(marker)
  }, [])

  // Measured after it rendered, because nothing knows how tall an excerpt makes it beforehand.
  useLayoutEffect(() => {
    const el = popover.current
    if (open == null || !anchor || !el) return
    const height = el.getBoundingClientRect().height
    setFlipped(height + GAP > anchor.room && height + GAP < anchor.above)
  }, [open, anchor])

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
      const href = sourceHref(source)
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
      return Boolean(sourceHref(source))
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
      <Surface html={initial.current} forward={container} />
      {hovered ? (
        <div
          ref={popover}
          className="cite-anchor"
          style={
            anchor
              ? {
                  top: anchor.top,
                  left: anchor.left,
                  // Flipped without measuring twice: from the position under the marker, "above"
                  // is its own height plus the marker's plus the two gaps.
                  ...(flipped
                    ? { transform: `translateY(calc(-100% - ${anchor.height + 2 * GAP}px))` }
                    : null),
                }
              : undefined
          }
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
  const passage = citation.passage ?? ''
  const detail = sourceDetail(citation)
  const fields = excerptFields(citation.excerpt ?? '', passage)
  return (
    <div className="cite-popover" role="dialog" aria-label={citation.label || citation.id}>
      <div className="cite-popover-head">
        <span className="source-icon" aria-hidden="true">
          {kindIcon(citation.kind)}
        </span>
        <span className="cite-popover-heading">
          <span className="cite-popover-title">{citation.label || citation.id}</span>
          <span className="cite-popover-origin">
            {sourceOrigin(citation)}
            {detail ? <span className="cite-popover-detail">{detail}</span> : null}
          </span>
        </span>
      </div>
      {fields ? (
        // A structured source has fields, not a sentence: shown as the record it is rather than as
        // a run of grey lines nothing can be highlighted in.
        <dl className="cite-popover-fields">
          {fields.map((field, at) => (
            <div className="cite-popover-field" key={`${field.name}-${at}`}>
              <dt>{field.name}</dt>
              <dd>{field.value}</dd>
            </div>
          ))}
        </dl>
      ) : citation.excerpt ? (
        <p className="cite-popover-excerpt">
          <Excerpt excerpt={citation.excerpt} passage={passage} />
        </p>
      ) : null}
      {canOpen || citation.archiveUrl ? (
        <div className="cite-popover-actions">
          {canOpen ? (
            // The label carries its own arrow ("Quelle öffnen →"); nothing is added to it here.
            <button type="button" className="cite-popover-open" onClick={onOpen}>
              {labels.openSource}
            </button>
          ) : null}
          {citation.archiveUrl ? (
            // What the page said when the agent read it - the live page may have changed since.
            <a
              className="cite-popover-archive"
              href={citation.archiveUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              {labels.archivedCopy(citation.archivedAt ?? '')}
            </a>
          ) : null}
        </div>
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

// Where a source says it comes from lives beside the citation model, so that `sources.tsx` and this
// file do not import each other. Re-exported because it has always been part of this module's API.
export { sourceOrigin } from './citations'
