import { React } from 'tl-react-bridge'
import DOMPurify from 'dompurify'

const { useEffect, useRef, useSyncExternalStore } = React

/**
 * What `<chat-conversation working-indicator="…">` asks for, as the control encoded it.
 *
 * The control sends an icon class in its `css:` / `colored:` form and an image as the URL it is
 * served under, context path included; the empty string - also what an element that configures
 * nothing sends - keeps the chat's own dots.
 */
export type WorkingIndicatorForm =
  | { kind: 'dots' }
  | { kind: 'icon'; className: string }
  | { kind: 'svg'; url: string }
  | { kind: 'image'; url: string }

/** Reads the configured indicator; anything this bundle does not understand keeps the dots. */
export function workingIndicatorForm(raw: unknown): WorkingIndicatorForm {
  const encoded = typeof raw === 'string' ? raw.trim() : ''
  if (!encoded) return { kind: 'dots' }
  if (encoded.startsWith('css:')) return iconForm(encoded.substring(4))
  if (encoded.startsWith('colored:')) return iconForm(encoded.substring(8))
  if (encoded.startsWith('/') || /^https?:\/\//.test(encoded)) {
    // The path decides, not the query: `logo.svg?v=2` is still an SVG.
    const path = encoded.split(/[?#]/, 1)[0]
    return path.toLowerCase().endsWith('.svg') ? { kind: 'svg', url: encoded } : { kind: 'image', url: encoded }
  }
  return { kind: 'dots' }
}

function iconForm(className: string): WorkingIndicatorForm {
  const trimmed = className.trim()
  return trimmed ? { kind: 'icon', className: trimmed } : { kind: 'dots' }
}

/**
 * The SVG as markup that may go into the page.
 *
 * The file is the application's own resource, not model output, but it is still markup written into
 * the chat's DOM, so it passes the same sanitizer the answers do - scripts and event handlers go,
 * while `<style>` and the SMIL elements stay, because they are what animates it. Anything that is not
 * one `<svg>` is refused: a stray HTML page (a login redirect, a 404 page) must not end up in the
 * transcript.
 */
export function sanitizeIndicatorSvg(markup: string): string {
  const clean = DOMPurify.sanitize(markup, {
    USE_PROFILES: { svg: true, svgFilters: true },
    ADD_TAGS: ['style', 'animate', 'animateTransform', 'animateMotion', 'set', 'mpath'],
  })
  const trimmed = clean.trim()
  return /^<svg[\s>]/i.test(trimmed) && /<\/svg>$/i.test(trimmed) ? trimmed : ''
}

/** Per URL: the sanitized markup, `null` while it loads, `false` when it could not be had. */
const svgs = new Map<string, string | null | false>()
const listeners = new Set<() => void>()

function settle(url: string, value: string | false) {
  svgs.set(url, value)
  for (const listener of listeners) listener()
}

/**
 * Starts loading an SVG indicator, once per URL and page.
 *
 * Called when the conversation mounts, not when the agent starts working: fetched only then, the
 * first answer would show the dots for as long as the request takes and then swap.
 */
export function preloadWorkingIndicator(form: WorkingIndicatorForm): void {
  if (form.kind !== 'svg' || svgs.has(form.url)) return
  svgs.set(form.url, null)
  fetch(form.url, { credentials: 'same-origin' })
    .then((response) => {
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      return response.text()
    })
    .then((text) => {
      const markup = sanitizeIndicatorSvg(text)
      if (!markup) throw new Error('not an SVG document')
      settle(form.url, markup)
    })
    .catch((error: unknown) => {
      // The dots stay. Said once per URL, because the page is fine and this is a configuration
      // mistake an administrator has to see, not a user.
      console.warn(`tl-chat: working indicator ${form.url} could not be loaded, keeping the dots.`, error)
      settle(form.url, false)
    })
}

function useSvg(url: string): string | null | false | undefined {
  return useSyncExternalStore(
    (listener) => {
      listeners.add(listener)
      return () => listeners.delete(listener)
    },
    () => svgs.get(url),
    () => svgs.get(url),
  )
}

/** The chat's own three dots - what every conversation shows that configures nothing. */
function Dots() {
  return (
    <span className="answer-dots" aria-hidden="true">
      <i />
      <i />
      <i />
    </span>
  )
}

/**
 * What stands where the answer will appear while the agent works.
 *
 * Decorative in every form: the thread's live region already says that an answer is being generated,
 * and a logo has nothing to add to that sentence.
 */
export function WorkingIndicator({ form }: { form: WorkingIndicatorForm }) {
  switch (form.kind) {
    case 'icon':
      return (
        <span className="answer-working is-custom" aria-hidden="true">
          <i className={form.className} />
        </span>
      )
    case 'image':
      return (
        <span className="answer-working is-custom" aria-hidden="true">
          <img src={form.url} alt="" />
        </span>
      )
    case 'svg':
      return <SvgIndicator url={form.url} />
    default:
      return <Dots />
  }
}

function SvgIndicator({ url }: { url: string }) {
  const markup = useSvg(url)
  const box = useRef<HTMLSpanElement>(null)
  useEffect(() => {
    if (!markup) preloadWorkingIndicator({ kind: 'svg', url })
  }, [markup, url])
  useEffect(() => {
    // CSS animations are stopped by the chat's reduced-motion rule, which reaches into inline SVG.
    // SMIL animations are not CSS, so they are paused here, on their first frame.
    const svg = box.current?.querySelector('svg')
    if (!svg || typeof window.matchMedia !== 'function') return
    if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    if (typeof svg.pauseAnimations === 'function') {
      svg.pauseAnimations()
      svg.setCurrentTime(0)
    }
  }, [markup])
  if (!markup) return <Dots />
  return (
    <span
      ref={box}
      className="answer-working is-custom is-svg"
      aria-hidden="true"
      // Sanitized by `sanitizeIndicatorSvg`, and the application's own configured resource - never
      // model text, which only ever reaches the page through `markdown.tsx`.
      dangerouslySetInnerHTML={{ __html: markup }}
    />
  )
}
