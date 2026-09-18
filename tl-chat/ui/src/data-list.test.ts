import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { resetTLBridge, setTLState } from '../test/tl-react-bridge'
import { TLDataList } from './data-list'

afterEach(() => resetTLBridge())

/** The state a `<data-list>` control writes, as the browser reads it. */
function listState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    language: 'en',
    search: false,
    collapsible: false,
    title: '',
    emptyText: 'Nothing yet',
    selectedId: '',
    buttons: [],
    rows: [
      { id: 'r1', lines: [{ text: 'Read the invoice' }, { text: 'Done', emphasis: 'secondary' }], cssClass: 'is-done' },
      { id: 'r2', lines: [{ text: 'Book it' }, { text: 'In progress', emphasis: 'secondary' }], cssClass: 'is-running' },
      { id: 'r3', lines: [{ text: 'File it' }, { text: 'Pending', emphasis: 'secondary' }] },
    ],
    ...overrides,
  }
}

function render(state: Record<string, unknown>): string {
  setTLState(state)
  return renderToStaticMarkup(React.createElement(TLDataList, { controlId: 'list', state }))
}

describe('the rendered data list', () => {
  it('renders every row in order, with all of its lines', () => {
    const html = render(listState())
    expect(html.indexOf('Read the invoice')).toBeLessThan(html.indexOf('Book it'))
    expect(html.indexOf('Book it')).toBeLessThan(html.indexOf('File it'))
    expect(html).toContain('rail-line rail-line-secondary">In progress')
  })

  /**
   * The state hook: what the view's `css-class` expression computed lands on the row element, which
   * is what lets a progress list look like a progress list without a chat-specific component.
   */
  it('puts a row’s computed classes on its row element', () => {
    const html = render(listState())
    expect(html).toContain('class="rail-item is-done"')
    expect(html).toContain('class="rail-item is-running"')
    expect(html).toContain('class="rail-item"')
  })

  it('keeps the selection class beside the computed ones', () => {
    const html = render(listState({ selectedId: 'r2' }))
    expect(html).toContain('class="rail-item is-active is-running"')
  })

  it('says the configured empty text when there are no rows', () => {
    const html = render(listState({ rows: [] }))
    expect(html).toContain('Nothing yet')
  })
})
