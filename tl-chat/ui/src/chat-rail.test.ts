import { afterEach, describe, expect, it } from 'vitest'
import { React, type BadgeJson } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { resetTLBridge, setTLState } from '../test/tl-react-bridge'
import { TLChatConversation } from './chat-conversation'
import { TLChatRail } from './chat-rail'
import {
  collapsedFromSections,
  paneIsOpen,
  railBadge,
  readPaneSteps,
  readRailSections,
  readSectionBadges,
  seenCounts,
  unseenRailBadge,
  type RailSectionState,
} from './rail-model'

afterEach(() => resetTLBridge())

const PROGRESS = { controlId: 'c2', module: 'TLDataList', state: {} }
const INVOICES = { controlId: 'c3', module: 'TLDataList', state: {} }

/** The state a `<chat-rail/>` control writes, as the browser reads it. */
function railState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    chatId: 'render-test',
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
    sectionBadges: { progress: { content: '2' } },
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

/** Sections named as given, for summing their badges. */
function sectionsNamed(...names: string[]): RailSectionState[] {
  return names.map((name) => ({ name, label: name, icon: '', open: true, content: null }))
}

describe('the rail badge', () => {
  const sections = sectionsNamed('a', 'b', 'c')
  const nothingSeen = {}
  const unseen = (badges: Record<string, Partial<BadgeJson>>, seen: Record<string, number> = nothingSeen) =>
    unseenRailBadge(railBadge(sections, badges), seen, 'New')

  it('sums the counts of the sections', () => {
    expect(unseen({ a: { content: '3' }, b: { content: '5' } })).toEqual({ content: '8', label: 'New' })
  })

  it('shows a sum above 99 as 99+', () => {
    expect(unseen({ a: { content: '60' }, b: { content: '50' } })?.content).toBe('99+')
    expect(unseen({ a: { content: '60' }, b: { content: '39' } })?.content).toBe('99')
  })

  it('shows a dot when only dots are shown', () => {
    expect(unseen({ a: { dot: true }, b: { dot: true, role: 'warning' } })).toEqual({
      dot: true,
      role: 'warning',
      label: 'New',
    })
  })

  it('shows the count rather than a dot when both are shown', () => {
    expect(unseen({ a: { dot: true, role: 'error' }, b: { content: '2' } })).toEqual({ content: '2', label: 'New' })
  })

  it('takes the most urgent role of the sections with something new', () => {
    expect(unseen({ a: { content: '1' }, b: { content: '1', role: 'warning' } })?.role).toBe('warning')
    expect(
      unseen({ a: { content: '1', role: 'warning' }, b: { content: '1', role: 'error' }, c: { content: '1' } })?.role,
    ).toBe('error')
    expect(unseen({ a: { content: '1', role: 'success' }, b: { content: '1', role: 'info' } })?.role).toBe('info')
    expect(unseen({ a: { content: '1', role: 'success' }, b: { content: '1' } })?.role).toBe('success')
  })

  it('counts nothing for a text badge', () => {
    expect(unseen({ a: { content: 'neu', role: 'error' }, b: { content: '2' } })).toEqual({ content: '2', label: 'New' })
    expect(unseen({ a: { content: 'neu' } })).toBeNull()
  })

  /**
   * A section with no badge channel has no notion of "new"; counting its rows would put a number on
   * the conversation's button that never goes away.
   */
  it('ignores a section that reports nothing', () => {
    expect(railBadge(sectionsNamed('a'), { b: { content: '4' } })).toEqual({})
  })

  it('counts per section what is new since the user last looked', () => {
    expect(unseen({ a: { content: '5', role: 'error' } }, { a: 3 })).toEqual({ content: '2', role: 'error', label: 'New' })
    expect(unseen({ a: { content: '5' } }, { a: 5 })).toBeNull()
    expect(unseen({ a: { content: '2' } }, { a: 5 })).toBeNull()
  })

  /** (a) The role is that of the sections with something new, not of every counting section. */
  it('takes the role only from sections with something new', () => {
    const badges = {
      a: { content: '7' },
      b: { content: '3', role: 'warning' as const },
      c: { content: '99+', role: 'error' as const },
    }
    expect(unseen(badges, { a: 6, b: 3, c: 100 })).toEqual({ content: '1', label: 'New' })
  })

  /** (b) A section that drops below what was seen of it does not swallow the news of another. */
  it('keeps the news of one section when another drops', () => {
    expect(unseen({ a: { content: '7' }, b: { content: '3' } }, { a: 6, b: 3, c: 100 })).toEqual({
      content: '1',
      label: 'New',
    })
  })

  /** The real count behind 99+ is unknown: new there makes the button 99+, seen there adds nothing. */
  it('shows 99+ where an overflowing section has something new', () => {
    expect(unseen({ a: { content: '99+', role: 'error' }, b: { content: '13' } }, { b: 13 })).toEqual({
      content: '99+',
      role: 'error',
      label: 'New',
    })
    expect(unseen({ a: { content: '99+' }, b: { content: '13' } }, { a: 99, b: 13 })?.content).toBe('99+')
    expect(unseen({ a: { content: '99+' }, b: { content: '14' } }, { a: 100, b: 13 })).toEqual({
      content: '1',
      label: 'New',
    })
    expect(unseen({ a: { content: '99+' } }, { a: 100 })).toBeNull()
    expect(unseen({ a: { content: '99+' }, b: { dot: true } }, { a: 100 })).toEqual({ dot: true, label: 'New' })
  })

  it('counts a section again from its seen count once it no longer overflows', () => {
    expect(unseen({ a: { content: '99' }, b: { content: '13' } }, { a: 100, b: 12 })).toEqual({
      content: '1',
      label: 'New',
    })
  })

  /** Seen is a count: a dot stays until its section clears it, and shows only once no count is new. */
  it('shows a dot only when no count is new', () => {
    const badges = { a: { content: '5' }, b: { dot: true, role: 'info' as const } }
    expect(unseen(badges, { a: 2 })).toEqual({ content: '3', label: 'New' })
    expect(unseen(badges, { a: 5 })).toEqual({ dot: true, role: 'info', label: 'New' })
  })

  it('records the counts of the sections as seen, a section counting nothing left out', () => {
    expect(
      seenCounts(railBadge(sections, { a: { content: '5' }, b: { content: '99+' }, c: { dot: true } })),
    ).toEqual({ a: 5, b: 100 })
  })

  it('reads the badge states and drops what shows nothing', () => {
    expect(
      readSectionBadges({
        sectionBadges: {
          a: { content: '3', role: 'error', label: 'New entries' },
          b: { dot: true },
          c: {},
          d: 4,
          e: { content: '1', role: 'bogus' },
        },
      }),
    ).toEqual({
      a: { content: '3', role: 'error', label: 'New entries' },
      b: { dot: true },
      e: { content: '1' },
    })
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

  it('shows the badge beside the title and on the strip icon through the part', () => {
    const html = render(railState())
    // Beside the title: read with it.
    expect(html).toContain('<span class="side-section-title">Progress</span> <tl-badge data-content="2"></tl-badge>')
    // On the strip icon: hidden, the button is named by its label and the badge.
    expect(html).toContain('aria-label="Progress, 2"')
    expect(html).toContain('<tl-badge data-content="2" data-alone="true"></tl-badge>')
    expect(html).not.toContain('side-section-badge')
    expect(html).not.toContain('side-strip-badge')
  })

  it('shows no badge for a section without one', () => {
    const html = render(railState({ sectionBadges: {} }))
    expect(html).not.toContain('tl-badge')
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

  /**
   * A page that is not signed in says so once (#643). The conversation beside the rail carries the
   * note; a rail that drew it too put the same heading twice side by side, the second in a column
   * of its own. Rendered as the demo's chat page renders them - one conversation, one rail, one
   * chat id - because "once" is a statement about the page, not about either element.
   */
  it('leaves the sign-in note to the conversation in a session that is not signed in', () => {
    const chatId = 'signed-out-page'
    const signedOut = { chatId, authenticated: false, language: 'en' }
    setTLState(signedOut)
    const conversation = renderToStaticMarkup(
      React.createElement(TLChatConversation, { controlId: 'conversation', state: signedOut }),
    )
    const rail = render(railState({ authenticated: false, chatId }))
    const page = conversation + rail

    expect(page.split('Sign in to use the chat').length - 1).toBe(1)
    expect(conversation).toContain('Sign in to use the chat')

    // The rail stands down: no note, no strip, no section, nothing that takes room in the row.
    expect(rail).toMatch(/^<div id="signed-out-page" class="tl-chat-rail is-stood-down"><\/div>$/)
  })

  /**
   * The pane is the server's history of steps (#616): nothing on the channel is no pane at all, and
   * one step replaces the section column with that step, titled by it.
   */
  it('swaps the sections for the pane while its history holds a step', () => {
    const closed = render(railState({ paneSteps: [], chatId: 'pane-closed' }))
    expect(closed).toContain('class="rail-column"')
    expect(closed).not.toContain('class="side-pane')

    const form = { controlId: 'c9', module: 'TLForm', state: {} }
    const open = render(railState({
      paneSteps: [{ id: 1, kind: 'form', title: 'Invoice 4711', control: form }],
      chatId: 'pane-open',
    }))
    expect(open).toContain('class="rail-column is-hidden"')
    expect(open).toContain('Invoice 4711')
    expect(open).not.toMatch(/class="side-pane"[^>]*hidden/)
    expect(open).toContain('data-module="TLForm"')
    // One step has nothing to go back to: the cross is the way out, not a "back" that closes.
    expect(open).not.toContain('class="side-back"')
    expect(open).toContain('class="side-pane-close"')
    expect(open).toContain('class="side-pane-collapse"')
  })

  /**
   * Every step stays mounted, so "back" finds the table as it was left - its sort, its filter, its
   * scroll position. Only the top one is shown; the ones below are stashed and inert.
   */
  it('keeps every step mounted, shows the top one and offers the way back to the one below', () => {
    const table = { controlId: 'c10', module: 'TLTableView', state: {} }
    const form = { controlId: 'c11', module: 'TLForm', state: {} }
    const html = render(railState({
      paneSteps: [
        { id: 1, kind: 'table', title: 'Veranstaltung (4)', control: table },
        { id: 2, kind: 'form', title: 'KI im Mittelstand 2026', control: form },
      ],
      chatId: 'pane-history',
    }))
    expect(html).toMatch(/class="pane-step is-table is-stashed" inert="" aria-hidden="true"><tl-child data-control-id="c10"/)
    expect(html).toMatch(/class="pane-step is-form" aria-hidden="false"><tl-child data-control-id="c11"/)
    expect(html).toContain('KI im Mittelstand 2026')
    expect(html).toContain('class="side-back"')
    // The strip's first icon is the pane's: what brings a folded pane back.
    expect(html).toMatch(/class="side-strip-item is-pane is-active"/)
  })

  it('reads only well-formed steps', () => {
    expect(readPaneSteps({ paneSteps: 'nonsense' })).toEqual([])
    expect(readPaneSteps({
      paneSteps: [
        { id: 1, kind: 'chart', title: 'Umsatz', control: { controlId: 'c1' } },
        { id: 'x', kind: 'table', control: { controlId: 'c2' } },
        { id: 3, kind: 'mystery', control: { controlId: 'c3' } },
        { id: 4, kind: 'form' },
      ],
    })).toEqual([
      { id: 1, kind: 'chart', title: 'Umsatz', control: { controlId: 'c1' } },
      { id: 3, kind: 'table', title: '', control: { controlId: 'c3' } },
    ])
  })
})

describe('the css-class of the element', () => {
  it('is written on the root after the classes of the rail itself', () => {
    const html = render(railState({ cssClass: 'appRail' }))
    expect(html).toMatch(/^<div id="render-test" class="tl-chat-rail appRail"/)
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
