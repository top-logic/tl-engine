import { describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { excerptFields, sourceDetail } from './citations'
import { bundle } from './i18n'
import { SourcePopover, sourceOrigin } from './markdown'
import { citedSources, latestAnswerSources } from './sources'
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

function answer(citations: Citation[], id = 'm1'): ChatMessage {
  return {
    type: 'assistant_text',
    id,
    text: 'Der teuerste Beleg [1].',
    timestamp: 0,
    citations,
  } as ChatMessage
}

function popover(cited: Citation): string {
  return renderToStaticMarkup(
    <SourcePopover citation={cited} labels={bundle('de')} onOpen={() => {}} />,
  )
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

  it('shows a structured source as its fields, not as a run of grey lines', () => {
    const html = popover(citation())
    expect(html).toContain('cite-popover-fields')
    expect(html).toContain('<dt>Betrag</dt>')
    expect(html).toContain('<dd>184.90</dd>')
  })

  it('says which record it is, beside the origin', () => {
    expect(popover(citation())).toContain('INV-12')
  })

  it('says which page of the site it is', () => {
    const html = popover(
      citation({ kind: 'web', kindLabel: '', url: 'https://quotes.example/page/2/', label: 'Page 2' }),
    )
    expect(html).toContain('quotes.example')
    expect(html).toContain('/page/2/')
  })

  it('highlights the passage inside the excerpt', () => {
    const html = popover(
      citation({
        kind: 'section',
        kindLabel: 'Abschnitt',
        passage: 'Die Versandkosten trägt der Besteller.',
        excerpt: 'Versand Die Versandkosten trägt der Besteller. Eine Ausnahme gilt für Teillieferungen.',
      }),
    )
    expect(html).toContain('<mark class="cite-passage">Die Versandkosten trägt der Besteller.</mark>')
  })

  it('offers the way to the source', () => {
    expect(popover(citation())).toContain('cite-popover-open')
  })
})

describe('what a structured excerpt is made of', () => {
  it('reads the server\'s `label: value` lines as fields', () => {
    expect(excerptFields('id: INV-12\nBetrag: 184.90', '')).toEqual([
      { name: 'id', value: 'INV-12' },
      { name: 'Betrag', value: '184.90' },
    ])
  })

  it('leaves prose alone, whatever colons it carries', () => {
    const prose = 'Versand: der Besteller zahlt.\nEine Ausnahme: Teillieferungen.'
    expect(excerptFields(prose, 'der Besteller zahlt')).toBeNull()
  })

  it('needs more than one line to be a record', () => {
    expect(excerptFields('Betrag: 184.90', '')).toBeNull()
  })

  it('refuses a block where one line is not a field', () => {
    expect(excerptFields('id: INV-12\nDies ist ein ganzer Satz ohne Doppelpunkt.', '')).toBeNull()
  })
})

describe('which source a citation points at, beside its origin', () => {
  it('is the path for a web page', () => {
    expect(sourceDetail({ url: 'https://quotes.example/page/2/', id: '', label: 'Page 2' })).toBe(
      '/page/2/',
    )
  })

  it('is nothing when the page is the site itself', () => {
    expect(sourceDetail({ url: 'https://quotes.example/', id: '', label: 'Quotes' })).toBe('')
  })

  it('is the id of a record, unless the id is already the label', () => {
    expect(sourceDetail({ url: '', id: 'INV-12', label: 'Reise Berlin' })).toBe('INV-12')
    expect(sourceDetail({ url: '', id: 'INV-12', label: 'INV-12' })).toBe('')
  })
})

describe('what the Quellen badge counts', () => {
  it('is what the newest answer cited, not the whole chat', () => {
    const messages = [answer([citation()], 'm1'), answer([citation({ id: 'INV-13' })], 'm2')]
    expect(citedSources(messages)).toHaveLength(2)
    expect(latestAnswerSources(messages)).toHaveLength(1)
    expect(latestAnswerSources(messages)[0].id).toBe('INV-13')
  })

  it('clears as soon as an answer cites nothing', () => {
    const messages = [answer([citation()], 'm1'), answer([], 'm2')]
    expect(citedSources(messages)).toHaveLength(1)
    expect(latestAnswerSources(messages)).toEqual([])
  })

  it('counts nothing before the first answer', () => {
    expect(latestAnswerSources([])).toEqual([])
  })
})
