import { React } from 'tl-react-bridge'
import DOMPurify from 'dompurify'
import { marked } from 'marked'
import { highlight, languageLabel } from './highlight'

const { useEffect, useMemo, useRef } = React

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
renderer.code = ({ text, lang }) => {
  const language = (lang ?? '').trim().split(/\s+/)[0]
  const { html, used } = highlight(text, language)
  const label = languageLabel(used || language)
  return (
    '<figure class="code-block">' +
    '<figcaption class="code-head">' +
    `<span class="code-lang">${escapeHtml(label)}</span>` +
    '<button class="code-copy" type="button" data-copy aria-label="Copy code">Copy</button>' +
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
export function Markdown({ text, streaming }: { text: string; streaming?: boolean }) {
  const container = useRef<HTMLDivElement>(null)
  const plain = streaming === true && text.length > LIVE_MARKDOWN_LIMIT

  const html = useMemo(() => {
    if (plain) return ''
    const raw = marked.parse(text || '') as string
    return DOMPurify.sanitize(raw)
  }, [plain, text])

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
          button.textContent = 'Copied'
          button.classList.add('is-done')
          window.setTimeout(() => {
            button.textContent = 'Copy'
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
