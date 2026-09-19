import { describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { bundle } from './i18n'
import { SourcePopover, sourceOrigin } from './markdown'
import { citedSources } from './sources'
import type { ChatMessage, Citation } from './types'

/** A citation as the server puts it on `message_complete`. */
function citation(overrides: Partial<Citation> = {}): Citation {
  return {
    index: 1,
    toolName: 'cite',
    toolCallId: 'c1',
    kind: 'invoice',
    kindLabel: 'Beleg',
    id: 'INV-12',
    label: 'Reise Berlin',
    url: '',
    passage: '',
    excerpt: 'id: INV-12\nBetrag: 184.90',
    ...overrides,
  }
}

function answer(citations: Citation[]): ChatMessage {
  return {
    type: 'assistant_text',
    id: 'm1',
    text: 'Der teuerste Beleg [1].',
    timestamp: 0,
    citations,
  } as ChatMessage
}

describe('where a source says it comes from', () => {
  it('shows the label a host declared for the kind, not the kind', () => {
    expect(sourceOrigin(citation())).toBe('Beleg')
  })

  it('falls back to the kind when the host declared no label', () => {
    expect(sourceOrigin(citation({ kindLabel: '' }))).toBe('invoice')
    expect(sourceOrigin(citation({ kindLabel: undefined }))).toBe('invoice')
  })

  it('keeps the host name of the URL for a web source', () => {
    expect(
      sourceOrigin(citation({ kind: 'web', kindLabel: 'Webseite', url: 'https://quotes.example/page/2/' })),
    ).toBe('quotes.example')
  })

  it('puts the same label on the Quellen row', () => {
    expect(citedSources([answer([citation()])])[0].origin).toBe('Beleg')
  })
})

describe('the marker popover', () => {
  it('shows the kind label as the origin line', () => {
    const html = renderToStaticMarkup(
      <SourcePopover citation={citation()} labels={bundle('de')} onOpen={() => {}} />,
    )
    expect(html).toContain('>Beleg<')
    expect(html).not.toContain('>invoice<')
  })

  it('shows the excerpt the server produced', () => {
    const html = renderToStaticMarkup(
      <SourcePopover citation={citation()} labels={bundle('de')} onOpen={() => {}} />,
    )
    expect(html).toContain('Betrag: 184.90')
  })

  it('highlights the passage inside the excerpt', () => {
    const cited = citation({
      kind: 'section',
      kindLabel: 'Abschnitt',
      passage: 'Die Versandkosten trägt der Besteller.',
      excerpt: 'Versand Die Versandkosten trägt der Besteller. Eine Ausnahme gilt für Teillieferungen.',
    })
    const html = renderToStaticMarkup(
      <SourcePopover citation={cited} labels={bundle('de')} onOpen={() => {}} />,
    )
    expect(html).toContain('<mark class="cite-passage">Die Versandkosten trägt der Besteller.</mark>')
  })
})
