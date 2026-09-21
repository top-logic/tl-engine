import { describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
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

  it('offers no expander for a payload that fits', () => {
    expect(card(approval())).not.toContain('prompt-args-more')
  })

  it('says which tool approving for the chat will cover', () => {
    const html = card(approval())
    expect(html).toContain('Record statement')
    expect(html).toContain('jeden weiteren Aufruf')
  })

  /* An accented button is the one a reader answers without looking at what is above it. */
  /* A restored request carries whatever the server listed as its header, which may be nothing. */
  it('says nothing about scope for a request that names no tool', () => {
    const html = card(approval({ toolName: '' }))
    expect(html).not.toContain('prompt-card-note')
  })

  it('accents neither answer', () => {
    expect(card(approval())).not.toContain('prompt-continue')
  })

  it('says why it is asking, in the language of the reader', () => {
    expect(card(approval())).toContain('Ändert gemeinsame Anwendungsdaten.')
    expect(card(approval(), 'en')).toContain('Changes shared application data.')
  })
})
