import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { resetTLBridge } from '../test/tl-react-bridge'
import { SHEET_MAX, SidePane } from './side-pane'
import { bundle } from './i18n'

afterEach(() => resetTLBridge())

const labels = bundle('en')

function render(props: Record<string, unknown>): string {
  return renderToStaticMarkup(
    React.createElement(
      SidePane as never,
      {
        title: 'Invoice',
        width: 0,
        onWidth: () => undefined,
        onBack: () => undefined,
        labels,
        ...props,
      },
      React.createElement('p', null, 'content'),
    ),
  )
}

/**
 * Whether the pane is a column or a sheet used to be `window.matchMedia('(max-width: 1279.98px)')`,
 * read twice - here and in `App` - and true of the window rather than of the chat. It is now one
 * measurement of the chat's own root, taken by `App` and handed down, so these are the cases the
 * prop has to decide (Issue #427).
 */
describe('SidePane: column or sheet', () => {
  it('is a column while the chat is wide, with a splitter and a width of its own', () => {
    const html = render({ narrow: false })
    expect(html).not.toContain('is-sheet')
    expect(html).toContain('side-splitter')
    expect(html).toContain('width:420px')
  })

  it('is a sheet once the chat is narrow: no splitter, no width, the stylesheet places it', () => {
    const html = render({ narrow: true })
    expect(html).toContain('is-sheet')
    expect(html).not.toContain('side-splitter')
    expect(html).not.toContain('width:420px')
  })

  it('stays a column in a composed rail, which has no grid to spread a sheet over', () => {
    const html = render({ narrow: true, placement: 'rail' })
    expect(html).not.toContain('is-sheet')
    expect(html).toContain('side-splitter')
  })

  it('is a column when nobody measured, so a host that passes nothing gets the wide layout', () => {
    expect(render({})).not.toContain('is-sheet')
  })

  it('carries the same breakpoint the chat-shell container query in styles.css does', () => {
    expect(SHEET_MAX).toBe(1279.98)
  })
})
