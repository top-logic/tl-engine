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

/**
 * The per-row action trigger.
 *
 * It is the whole client side of `<row-commands>`: the menu itself is the application's, opened by
 * the Java control through the frame's context-menu opener. What the browser owns is whether the
 * trigger is there, what it is called, and that a click says which row and where.
 */
describe('the row action trigger', () => {
  it('is absent until the server says there is a menu', () => {
    expect(render(listState())).not.toContain('rail-row-menu')
    expect(render(listState({ rowActions: 'hover' }))).not.toContain('rail-row-menu')
  })

  /*
   * `hasRowMenu` is false where the frame has no menu to open - a list outside an `<app-shell>`.
   * A trigger that opens nothing is worse than none, so nothing is rendered.
   */
  it('is absent where there is no menu to open, whatever is configured', () => {
    const html = render(listState({ hasRowMenu: false, rowActions: 'always' }))
    expect(html).not.toContain('rail-row-menu')
  })

  it('names the row it belongs to, so every trigger is not called the same thing', () => {
    const html = render(listState({ hasRowMenu: true, rowActions: 'hover' }))
    expect(html).toContain('aria-label="Actions: Read the invoice"')
    expect(html).toContain('aria-label="Actions: Book it"')
    expect(html).toContain('aria-haspopup="menu"')
  })

  it('renders one trigger per row', () => {
    const html = render(listState({ hasRowMenu: true, rowActions: 'hover' }))
    expect(html.split('rail-row-menu').length - 1).toBe(3)
  })

  /* `hover` leaves the reveal to the stylesheet; `always` says so in the markup. */
  it('marks the always mode and leaves hover to the stylesheet', () => {
    expect(render(listState({ hasRowMenu: true, rowActions: 'hover' })))
      .toContain('class="icon-button tiny rail-row-menu"')
    expect(render(listState({ hasRowMenu: true, rowActions: 'always' })))
      .toContain('class="icon-button tiny rail-row-menu is-always"')
  })

  it('renders nothing in the none mode', () => {
    expect(render(listState({ hasRowMenu: true, rowActions: 'none' }))).not.toContain('rail-row-menu')
  })

  /*
   * What a click does - the `rowMenu` command with the row id and the pointer position - and where
   * focus lands after the row it belonged to is deleted both need a live DOM, and this run renders
   * to a string (there is no jsdom in the module's test setup and adding one for two assertions is
   * a heavier dependency than it buys). Both are walked in the browser instead; see the PR.
   */
})
