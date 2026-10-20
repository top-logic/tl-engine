// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { renderToStaticMarkup } from 'react-dom/server'
import { Thread } from './thread'
import { chatReducer, type ChatAction } from './reducer'
import { bundle } from './i18n'
import type { ChatMessage } from './types'
import {
  preloadWorkingIndicator,
  sanitizeIndicatorSvg,
  workingIndicatorForm,
  type WorkingIndicatorForm,
} from './working-indicator'

/**
 * `<chat-conversation working-indicator="…">`: an application's own animation in place of the three
 * dots while the agent works and has written nothing yet.
 *
 * `jsdom`, because the SVG form is fetched and sanitized - DOMPurify needs a DOM - and lands in the
 * page after a commit, which `react-dom/server` never performs.
 */

const labels = bundle('en')

/** A turn that has been sent and has produced no text: the moment the indicator is for. */
const WAITING: ChatAction[] = [
  { type: 'ADD_USER', id: 'u1', text: 'Was ist die Hauptstadt?' },
  { type: 'START_RUN', runId: 'r1' },
  { type: 'ADD_ASSISTANT', id: 'a1', text: '', isStreaming: true },
]

function messages(actions: ChatAction[] = WAITING): ChatMessage[] {
  let result: ChatMessage[] = []
  for (const action of actions) result = chatReducer(result, action)
  return result
}

function thread(workingIndicator?: WorkingIndicatorForm, busy = true) {
  return React.createElement(Thread, {
    messages: messages(),
    traceMode: 'summary',
    welcomeName: 'Nutzer',
    busy,
    regenerating: false,
    feedback: [],
    labels,
    language: 'en',
    effort: '',
    onEditSend: async () => true,
    onRegenerate: () => undefined,
    onRate: () => undefined,
    onOpenRail: () => undefined,
    railCollapsed: true,
    onToggleRail: () => undefined,
    title: 'Titel',
    workingIndicator,
  })
}

describe('the configured value', () => {
  it('keeps the dots for nothing configured, and for anything it cannot read', () => {
    expect(workingIndicatorForm(undefined)).toEqual({ kind: 'dots' })
    expect(workingIndicatorForm('')).toEqual({ kind: 'dots' })
    expect(workingIndicatorForm('  ')).toEqual({ kind: 'dots' })
    expect(workingIndicatorForm('css:')).toEqual({ kind: 'dots' })
    expect(workingIndicatorForm('javascript:alert(1)')).toEqual({ kind: 'dots' })
  })

  it('tells an icon class, an SVG and any other image apart', () => {
    expect(workingIndicatorForm('css:app-working spin')).toEqual({ kind: 'icon', className: 'app-working spin' })
    expect(workingIndicatorForm('colored:app-working')).toEqual({ kind: 'icon', className: 'app-working' })
    expect(workingIndicatorForm('/app/images/working.svg')).toEqual({ kind: 'svg', url: '/app/images/working.svg' })
    // The path decides whether it is an SVG, not a cache-busting query behind it.
    expect(workingIndicatorForm('/app/images/Working.SVG?v=2')).toEqual({
      kind: 'svg',
      url: '/app/images/Working.SVG?v=2',
    })
    expect(workingIndicatorForm('/app/images/working.webp')).toEqual({
      kind: 'image',
      url: '/app/images/working.webp',
    })
  })
})

describe('the transcript while the agent works', () => {
  it('shows the three dots when nothing is configured - unchanged', () => {
    const html = renderToStaticMarkup(thread())
    expect(html).toContain('answer is-pending')
    expect(html).toContain('<span class="answer-dots" aria-hidden="true"><i></i><i></i><i></i></span>')
    expect(html).not.toContain('answer-working')
    // The status text beside it is the chat's own, whatever stands in the answer's place.
    expect(html).toContain(labels.working)
  })

  it('shows a configured icon class instead of the dots', () => {
    const html = renderToStaticMarkup(thread(workingIndicatorForm('css:app-working')))
    expect(html).toContain('answer is-pending')
    expect(html).toContain('<span class="answer-working is-custom" aria-hidden="true"><i class="app-working"></i></span>')
    expect(html).not.toContain('answer-dots')
    expect(html).toContain(labels.working)
  })

  it('shows a configured image instead of the dots, decorative', () => {
    const html = renderToStaticMarkup(thread(workingIndicatorForm('/app/images/working.webp')))
    expect(html).toContain('<img src="/app/images/working.webp" alt=""/>')
    expect(html).not.toContain('answer-dots')
  })

  it('shows neither once the agent is not working', () => {
    const html = renderToStaticMarkup(thread(workingIndicatorForm('css:app-working'), false))
    expect(html).not.toContain('answer-working')
    expect(html).not.toContain('answer-dots')
  })
})

describe('an SVG indicator', () => {
  let root: Root | undefined
  let host: HTMLDivElement | undefined

  afterEach(() => {
    root?.unmount()
    host?.remove()
    root = undefined
    host = undefined
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  function mount(form: WorkingIndicatorForm) {
    host = document.createElement('div')
    document.body.appendChild(host)
    root = createRoot(host)
    root.render(thread(form))
    return host
  }

  /** A full turn of the event loop: the fetch, its text, the store notification and the commit. */
  function settled(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 20))
  }

  it('is placed into the page, sanitized, so it can draw in the theme’s colours', async () => {
    const svg =
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10" onload="alert(1)">' +
      '<style>.app-w{animation:app-spin 1s linear infinite}</style>' +
      '<script>alert(2)</script>' +
      '<circle class="app-w" cx="5" cy="5" r="4" fill="currentColor"/></svg>'
    vi.stubGlobal('fetch', vi.fn(async () => new Response(svg, { status: 200 })))
    const form = workingIndicatorForm('/app/images/inline.svg')
    preloadWorkingIndicator(form)

    const page = mount(form)
    await settled()

    const box = page.querySelector('.answer-working.is-svg')
    expect(box).not.toBeNull()
    expect(box!.getAttribute('aria-hidden')).toBe('true')
    expect(box!.querySelector('circle')?.getAttribute('fill')).toBe('currentColor')
    // What animates it stays; what could run code does not.
    expect(box!.querySelector('style')?.textContent).toContain('app-spin')
    expect(box!.querySelector('script')).toBeNull()
    expect(box!.querySelector('svg')?.hasAttribute('onload')).toBe(false)
    expect(page.querySelector('.answer-dots')).toBeNull()
  })

  it('keeps the dots when the file cannot be had', async () => {
    vi.spyOn(console, 'warn').mockImplementation(() => undefined)
    vi.stubGlobal('fetch', vi.fn(async () => new Response('missing', { status: 404 })))
    const form = workingIndicatorForm('/app/images/missing.svg')
    preloadWorkingIndicator(form)

    const page = mount(form)
    await settled()

    expect(page.querySelector('.answer-dots')).not.toBeNull()
    expect(page.querySelector('.answer-working')).toBeNull()
    expect(console.warn).toHaveBeenCalledTimes(1)
  })

  it('refuses a document that is not one SVG - a login page answering the request, say', () => {
    expect(sanitizeIndicatorSvg('<html><body><form>Login</form></body></html>')).toBe('')
    expect(sanitizeIndicatorSvg('<svg viewBox="0 0 1 1"><rect width="1" height="1"/></svg>')).toMatch(/^<svg/)
  })
})
