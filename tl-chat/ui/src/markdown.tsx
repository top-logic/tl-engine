import { React } from 'tl-react-bridge'
import DOMPurify from 'dompurify'
import { marked } from 'marked'
import { highlight, languageLabel } from './highlight'
import type { bundle } from './i18n'

const { useEffect, useMemo, useRef } = React

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

marked.setOptions({ gfm: true, breaks: true, renderer })

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
}: {
  text: string
  streaming?: boolean
  labels: Labels
}) {
  const container = useRef<HTMLDivElement>(null)
  const plain = streaming === true && text.length > LIVE_MARKDOWN_LIMIT
  codeLabels = labels

  const html = useMemo(() => {
    if (plain) return ''
    const raw = marked.parse(text || '') as string
    return DOMPurify.sanitize(raw)
  }, [plain, text, labels])

  // One delegated listener rather than one per block: the blocks are replaced on every parse.
  useEffect(() => {
    const el = container.current
    if (!el) return
    const onClick = (event: MouseEvent) => {
      const button = (event.target as HTMLElement | null)?.closest<HTMLButtonElement>('[data-copy]')
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
    el.addEventListener('click', onClick)
    return () => el.removeEventListener('click', onClick)
  }, [])

  if (plain) {
    return (
      <div className="md md-plain" ref={container}>
        {text}
      </div>
    )
  }
  return <div className="md" ref={container} dangerouslySetInnerHTML={{ __html: html }} />
}
