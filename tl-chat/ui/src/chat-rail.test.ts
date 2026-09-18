import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { resetTLBridge, setTLState } from '../test/tl-react-bridge'
import { TLChatRail } from './chat-rail'
import {
  collapsedFromSections,
  paneIsOpen,
  railBadgeTotal,
  readRailSections,
  readSectionBadges,
} from './rail-model'

afterEach(() => resetTLBridge())

const PROGRESS = { controlId: 'c2', module: 'TLDataList', state: {} }
const INVOICES = { controlId: 'c3', module: 'TLDataList', state: {} }

/** The state a `<chat-rail/>` control writes, as the browser reads it. */
function railState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    chatId: 'render-test',
    windowId: 'w1',
    authenticated: true,
    language: 'en',
    // The bootstrap the control writes: the user's remembered rail state, and the collapse seed.
    railOpen: true,
    collapsedSections: ['invoices'],
    strip: true,
    open: true,
    paneMode: 'card',
    sections: [
      { name: 'progress', label: 'Progress', icon: 'css:bi bi-list-check', open: true, content: PROGRESS },
      { name: 'invoices', label: 'Invoices', icon: '', open: false, content: INVOICES },
    ],
    sectionBadges: { progress: 2 },
    ...overrides,
  }
}

/** Renders the rail against a control state, without a browser. */
function render(state: Record<string, unknown>): string {
  setTLState(state)
  return renderToStaticMarkup(
    React.createElement(TLChatRail, { controlId: state.chatId as string, state }),
  )
}

describe('readRailSections', () => {
  it('reads what the control serialized, in order', () => {
    const sections = readRailSections(railState())
    expect(sections.map((section) => section.name)).toEqual(['progress', 'invoices'])
    expect(sections[0].label).toBe('Progress')
    expect(sections[0].icon).toBe('css:bi bi-list-check')
    expect(sections[0].content).toBe(PROGRESS)
  })

  it('is empty rather than broken when there is no section list', () => {
    expect(readRailSections({})).toEqual([])
  })

  it('falls back to the technical name when a section has no label', () => {
    const sections = readRailSections({ sections: [{ name: 'plan', content: null }] })
    expect(sections[0].label).toBe('plan')
    expect(sections[0].open).toBe(false)
  })
})

describe('the badge total', () => {
  it('sums the sections that report a count', () => {
    const sections = readRailSections(railState())
    const badges = readSectionBadges(railState({ sectionBadges: { progress: 2, invoices: 3 } }))
    expect(railBadgeTotal(sections, badges)).toBe(5)
  })

  /**
   * A section with no badge channel has no notion of "new"; counting its rows would put a number on
   * the conversation's button that never goes away.
   */
  it('ignores a section that reports nothing', () => {
    const sections = readRailSections(railState())
    expect(railBadgeTotal(sections, readSectionBadges(railState()))).toBe(2)
  })

  it('drops values that are not a positive number', () => {
    expect(readSectionBadges({ sectionBadges: { a: 0, b: -1, c: 'x', d: '4' } })).toEqual({ d: 4 })
  })
})

describe('collapsedFromSections', () => {
  it('collapses every section that does not start open', () => {
    expect(collapsedFromSections(readRailSections(railState()))).toEqual(['invoices'])
  })

  it('has nothing to seed for an empty rail', () => {
    expect(collapsedFromSections([])).toBeUndefined()
  })
})

describe('the rendered rail', () => {
  it('renders one header and one child control per configured section, in order', () => {
    const html = render(railState())
    expect(html.indexOf('Progress')).toBeLessThan(html.indexOf('Invoices'))
    expect(html).toContain('data-module="TLDataList"')
    expect(html).toContain('class="tl-chat-rail"')
  })

  /** Hidden, not dropped: a collapsed section keeps reporting the badge the strip shows. */
  it('keeps a collapsed section mounted and hides its body', () => {
    const html = render(railState())
    expect(html).toContain('class="side-section-body is-hidden"')
    expect(html).toContain('data-module="TLDataList"')
  })

  it('shows the badge beside the title and on the strip icon', () => {
    const html = render(railState())
    expect(html).toContain('<span class="side-section-badge">2</span>')
    expect(html).toContain('<span class="side-strip-badge">2</span>')
  })

  it('renders the configured icon and a fallback glyph for a section without one', () => {
    const html = render(railState())
    expect(html).toContain('class="bi bi-list-check"')
    expect(html).toContain('<svg viewBox="0 0 16 16"')
  })

  it('leaves the strip out when it is turned off', () => {
    expect(render(railState({ strip: false }))).not.toContain('side-strip')
  })

  /** An empty `<chat-rail/>` is a legitimate configuration: the shell ships no sections. */
  it('says so when nothing is configured', () => {
    const html = render(railState({ sections: [], sectionBadges: {} }))
    expect(html).toContain('side-empty')
    expect(html).not.toContain('data-module')
  })

  it('shows the sign-in note instead of a rail in a session that is not signed in', () => {
    const html = render(railState({ authenticated: false, chatId: 'signed-out-rail' }))
    expect(html).toContain('is-signed-out')
    expect(html).not.toContain('data-module')
  })

  /**
   * The pane is the rail's other face: `paneOpen` swaps which of the two the user sees, and the
   * pane's element stays mounted in both, so reopening it shows the object as it is now.
   */
  it('swaps the sections for the pane while the input channel holds an object', () => {
    const pane = { controlId: 'c9', module: 'TLForm', state: {} }
    const closed = render(railState({ pane, paneOpen: false, chatId: 'pane-closed' }))
    expect(closed).toContain('class="rail-column"')
    expect(closed).toContain('data-module="TLForm"')
    expect(closed).toMatch(/class="side-pane"[^>]*hidden/)

    const open = render(railState({ pane, paneOpen: true, paneTitle: 'Invoice 4711', chatId: 'pane-open' }))
    expect(open).toContain('class="rail-column is-hidden"')
    expect(open).toContain('Invoice 4711')
    expect(open).not.toMatch(/class="side-pane"[^>]*hidden/)
  })

  /**
   * One object is a form, several are a list, and no element renders both - so the rail picks the
   * pane child by how many objects the channel holds (`paneMany`, written by the control).
   */
  it('shows the list pane instead of the pane while several objects stand on the channel', () => {
    const pane = { controlId: 'c9', module: 'TLForm', state: {} }
    const listPane = { controlId: 'c10', module: 'TLDataList', state: {} }

    // Both stay mounted, as every other child of the rail does; one of the two is hidden.
    const one = render(railState({ pane, listPane, paneOpen: true, paneMany: false, chatId: 'pane-one' }))
    expect(one).toMatch(/class="is-hidden"><tl-child data-control-id="c10"/)
    expect(one).toMatch(/<div><tl-child data-control-id="c9"/)

    const many = render(railState({
      pane, listPane, paneOpen: true, paneMany: true, paneTitle: '3 objects', chatId: 'pane-many',
    }))
    expect(many).toMatch(/class="is-hidden"><tl-child data-control-id="c9"/)
    expect(many).toMatch(/<div><tl-child data-control-id="c10"/)
    expect(many).toContain('3 objects')
  })

  /** A rail that configured only a `<pane>` shows it for whatever stands on the channel. */
  it('falls back to the one pane child that is configured', () => {
    const pane = { controlId: 'c9', module: 'TLForm', state: {} }

    const html = render(railState({ pane, paneOpen: true, paneMany: true, chatId: 'pane-only-one' }))
    expect(html).toMatch(/<div><tl-child data-control-id="c9"/)
    expect(html).not.toContain('is-hidden"><tl-child data-control-id="c9"')
  })
})

describe('the chat bundle', () => {
  /**
   * React is the host's, handed to this bundle by `tl-react-bridge`.
   *
   * Importing it directly bundles a second copy, and hooks called against the copy that did not
   * render the tree fail at runtime with "useState is null". Until this run had a real React of its
   * own, the absent dependency was what enforced that; now it is this test. A type-only import is
   * erased by the build and harmless.
   */
  it('takes React from the bridge, never from react itself', () => {
    const sources = import.meta.glob('./*.{ts,tsx}', { query: '?raw', import: 'default', eager: true })
    const offenders: string[] = []
    for (const [name, source] of Object.entries(sources)) {
      if (name.endsWith('.test.ts') || name.endsWith('.test.tsx')) continue
      for (const line of String(source).split('\n')) {
        if (/^\s*import\s+(?!type\b)[^;]*from\s+'react(-dom)?'/.test(line)) {
          offenders.push(`${name}: ${line}`)
        }
      }
    }
    expect(offenders).toEqual([])
  })
})

describe('the chat-owned pane', () => {
  it('is open only while the rail is open and something was put in it', () => {
    expect(paneIsOpen(true, 'invoice-node')).toBe(true)
    expect(paneIsOpen(true, null)).toBe(false)
    expect(paneIsOpen(false, 'invoice-node')).toBe(false)
  })
})
