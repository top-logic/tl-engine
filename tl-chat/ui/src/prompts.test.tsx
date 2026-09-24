// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { createRoot } from 'react-dom/client'
import { PromptStack } from './prompts'
import { bundle, type Language } from './i18n'
import type { ApprovalRequest } from './types'

/** An approval request as the store builds one, with whatever the test is about overwritten. */
function approval(over: Partial<ApprovalRequest> = {}): ApprovalRequest {
  return {
    type: 'approval_request',
    id: 'a1',
    approvalRequestId: 'a1',
    toolCallId: 't1',
    toolName: 'consulting.recordStatement',
    argsJson: '{"text":"FACT-004 Das Service-Center hat 19 VZÄ.","code":"FACT-004"}',
    displayReason: 'Modifies shared application data.',
    toolMode: 'DATA_WRITE',
    outcome: 'pending',
    timestamp: 0,
    ...over,
  }
}

/** The card as the browser renders it. */
function card(item: ApprovalRequest, language: Language = 'de'): string {
  return renderToStaticMarkup(
    React.createElement(PromptStack, {
      approvals: [item],
      questions: [],
      at: 'w1',
      labels: bundle(language),
      dispatch: () => undefined,
    }),
  )
}

/**
 * What the card has to say before anybody can answer it on the merits (Gitea #511).
 *
 * The captured card that the issue was filed about named the tool and a generic sentence about
 * shared data; the statement it was standing in front of carried a wrong number, and no reader
 * could have seen that.
 */
describe('the approval card', () => {
  it('shows what the tool will be given', () => {
    const html = card(approval())
    expect(html).toContain('FACT-004 Das Service-Center hat 19 VZ')
    expect(html).toContain('prompt-args')
  })

  it('shows the arguments as text, never as markup', () => {
    const html = card(approval({ argsJson: '{"text":"<img src=x onerror=alert(1)> **bold**"}' }))
    expect(html).toContain('&lt;img src=x onerror=alert(1)&gt; **bold**')
    expect(html).not.toContain('<img src=x')
    expect(html).not.toContain('<strong>bold</strong>')
  })

  /*
   * The body of a section the agent is about to write is the payload a reader most needs to judge,
   * and `JSON.stringify` would hand it over as one run-on line of `\n` escapes.
   */
  it('keeps the lines of a value that has them', () => {
    const html = card(approval({ argsJson: JSON.stringify({ section: 'Konzept', body: 'Erste Zeile\nZweite Zeile' }) }))
    expect(html).toContain('section: Konzept')
    expect(html).toContain('body:\nErste Zeile\nZweite Zeile')
    expect(html).not.toContain('\\n')
  })

  it('keeps a payload it cannot parse rather than dropping it', () => {
    const html = card(approval({ argsJson: 'not json at all' }))
    expect(html).toContain('not json at all')
  })

  it('has no arguments block when the call carries none', () => {
    expect(card(approval({ argsJson: '{}' }))).not.toContain('prompt-args')
    expect(card(approval({ argsJson: '' }))).not.toContain('prompt-args')
  })

  /* The card sits over the composer: a payload of any length must not grow it into the page. */
  it('clamps a long payload and offers the rest', () => {
    const long = JSON.stringify({ lines: Array.from({ length: 40 }, (_, i) => `Zeile ${i}`) })
    const html = card(approval({ argsJson: long }))
    expect(html).toContain('prompt-args-more')
    expect(html).toContain('…')
    expect(html).not.toContain('Zeile 39')
  })

  /*
   * One line of 800 characters passes the line budget and wraps into twenty rows at the card's width
   * (#551): the block is bounded in rendered rows by its class, and opening it must not lift that.
   */
  it('keeps the block bounded while the expander shows the rest', async () => {
    const tail = 'ENDE-DES-TEXTES'
    const container = document.createElement('div')
    document.body.append(container)
    const root = createRoot(container)
    root.render(
      React.createElement(PromptStack, {
        approvals: [approval({ argsJson: JSON.stringify({ newText: 'weiterer Text. '.repeat(55) + tail }) })],
        questions: [],
        at: 'w1',
        labels: bundle('de'),
        dispatch: () => undefined,
      }),
    )
    const settle = () => new Promise((resolve) => setTimeout(resolve, 20))
    await settle()
    const pre = () => container.querySelector('.prompt-args pre')!
    const more = () => container.querySelector<HTMLButtonElement>('.prompt-args-more')!
    try {
      expect(pre().className).toBe('prompt-args-text')
      expect(pre().textContent).not.toContain(tail)
      expect(more().getAttribute('aria-expanded')).toBe('false')

      more().click()
      await settle()
      expect(pre().className).toBe('prompt-args-text')
      expect(pre().textContent).toContain(tail)
      expect(more().getAttribute('aria-expanded')).toBe('true')
      expect(more().textContent).toBe('Weniger anzeigen')

      more().click()
      await settle()
      expect(pre().textContent).not.toContain(tail)
      expect(more().getAttribute('aria-expanded')).toBe('false')
    } finally {
      root.unmount()
      container.remove()
    }
  })

  it('offers no expander for a payload that fits', () => {
    expect(card(approval())).not.toContain('prompt-args-more')
  })

  it('says which tool approving for the chat will cover', () => {
    const html = card(approval())
    expect(html).toContain('jeden weiteren Aufruf')
    expect(html).toContain('<code class="tool-id">consulting.recordStatement</code>')
  })

  /*
   * #550: the tool is named by its technical id, set as code - never by an English phrase made up
   * from it ("Edit text"), which the German card showed as if it were German.
   */
  it('names the tool by its id, as code, in the title and in the scope sentence', () => {
    for (const language of ['de', 'en'] as const) {
      const root = document.createElement('div')
      root.innerHTML = card(approval({ toolName: 'editText' }), language)
      const title = root.querySelector('.prompt-card-title')!
      expect(title.getAttribute('title')).toBe('editText')
      expect(title.querySelector('code.tool-id')?.textContent).toBe('editText')
      expect(title.textContent).toBe('editText')
      const note = root.querySelector('.prompt-card-note')!
      expect(note.querySelector('code.tool-id')?.textContent).toBe('editText')
      expect(root.textContent).not.toContain('Edit text')
    }
  })

  it('says the scope in the exact wording', () => {
    const note = (language: Language) => {
      const root = document.createElement('div')
      root.innerHTML = card(approval({ toolName: 'editText' }), language)
      return root.querySelector('.prompt-card-note')!.textContent
    }
    expect(note('de')).toBe(
      'Gilt in diesem Chat für jeden weiteren Aufruf des Werkzeugs editText - für kein anderes Werkzeug dieses Zuges.',
    )
    expect(note('en')).toBe(
      'Covers every further call of the tool editText in this chat - no other tool of this turn.',
    )
  })

  /* An accented button is the one a reader answers without looking at what is above it. */
  /* A restored request carries whatever the server listed as its header, which may be nothing. */
  it('says nothing about scope for a request that names no tool', () => {
    const html = card(approval({ toolName: '' }))
    expect(html).not.toContain('prompt-card-note')
    expect(html).not.toContain('tool-id')
  })

  it('accents neither answer', () => {
    expect(card(approval())).not.toContain('prompt-continue')
  })

  it('says why it is asking, in the language of the reader', () => {
    expect(card(approval())).toContain('Ändert gemeinsame Anwendungsdaten.')
    expect(card(approval(), 'en')).toContain('Changes shared application data.')
  })
})
