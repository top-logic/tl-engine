import { describe, expect, it, afterEach } from 'vitest'
import { App } from './App'
import { TLChatRail } from './chat-rail'
import {
  collapsedFromConfig,
  paneIsOpen,
  readRailSections,
  resolveRailSections,
} from './rail-order'
import {
  registerRailSection,
  registeredRailSection,
  registeredRailSectionNames,
  resetRegistries,
} from './registry'
import type { RailRenderContext, RailSection } from './extensions'

afterEach(() => resetRegistries())

const stubCtx = {} as RailRenderContext

const framework = {
  progress: { id: 'progress', title: 'Fortschritt', render: () => null } satisfies RailSection,
  context: { id: 'context', title: 'Kontext', render: () => null } satisfies RailSection,
  sources: { id: 'sources', title: 'Quellen', render: () => null } satisfies RailSection,
}

describe('resolveRailSections', () => {
  it('uses Fortschritt, host, Kontext, Quellen when the list is empty', () => {
    registerRailSection('host-belege', () => ({
      id: 'host-belege',
      title: 'Belege',
      render: () => null,
    }))
    const host = registeredRailSection('host-belege', stubCtx)!
    const { sections, dropped } = resolveRailSections([], framework, [host])
    expect(dropped).toEqual([])
    expect(sections.map((section) => section.id)).toEqual([
      'progress',
      'host-belege',
      'context',
      'sources',
    ])
  })

  it('places a registered section at its configured position', () => {
    registerRailSection('host-belege', () => ({
      id: 'host-belege',
      title: 'Belege',
      render: () => null,
    }))
    const host = registeredRailSection('host-belege', stubCtx)!
    const { sections } = resolveRailSections(
      [
        { name: 'quellen', id: 'sources', open: true, badge: true },
        { name: 'host-belege', id: 'host-belege', open: true, badge: true },
        { name: 'fortschritt', id: 'progress', open: false, badge: true },
      ],
      framework,
      [host],
    )
    expect(sections.map((section) => section.id)).toEqual(['sources', 'host-belege', 'progress'])
  })

  it('drops an unregistered name without throwing', () => {
    expect(() => {
      const { sections, dropped } = resolveRailSections(
        [
          { name: 'not-a-section', id: 'not-a-section', open: false, badge: true },
          { name: 'kontext', id: 'context', open: true, badge: true },
        ],
        framework,
        [],
      )
      expect(dropped).toEqual(['not-a-section'])
      expect(sections.map((section) => section.id)).toEqual(['context'])
    }).not.toThrow()
  })
})

describe('collapsedFromConfig', () => {
  it('leaves the store default when the list is empty', () => {
    expect(collapsedFromConfig([])).toBeUndefined()
  })

  it('collapses every section that is not open', () => {
    expect(
      collapsedFromConfig([
        { name: 'fortschritt', id: 'progress', open: false, badge: true },
        { name: 'kontext', id: 'context', open: true, badge: true },
      ]),
    ).toEqual(['progress'])
  })
})

describe('readRailSections', () => {
  it('reads the rows the control put into state', () => {
    expect(
      readRailSections({
        sections: [{ name: 'fortschritt', id: 'progress', open: false, badge: true }],
      }),
    ).toEqual([{ name: 'fortschritt', id: 'progress', open: false, badge: true }])
  })
})

describe('openInPane on a rail of its own', () => {
  it('opens the pane when a section asks', () => {
    registerRailSection('host-belege', (ctx) => ({
      id: 'host-belege',
      title: 'Belege',
      render: () => {
        ctx.openInPane('invoice-node', { title: 'Invoice', mode: 'card' })
        return null
      },
    }))
    let opened: { content: unknown; title: string } | null = null
    const ctx: RailRenderContext = {
      sessionId: 's1',
      messages: [],
      busy: false,
      pendingApprovals: [],
      openInPane: (content, options) => {
        opened = { content, title: options.title }
      },
      closePane: () => undefined,
      insertReference: () => undefined,
    }
    const section = registeredRailSection('host-belege', ctx)!
    section.render(ctx)
    expect(opened).toEqual({ content: 'invoice-node', title: 'Invoice' })
    expect(paneIsOpen(true, 'invoice-node')).toBe(true)
    expect(paneIsOpen(true, null)).toBe(false)
  })
})

describe('TLChatRail and App', () => {
  it('are functions over the same store API', () => {
    expect(typeof TLChatRail).toBe('function')
    expect(typeof App).toBe('function')
    expect(registeredRailSectionNames()).toEqual([])
  })
})
