// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { flushSync } from 'react-dom'
import { createRoot, type Root } from 'react-dom/client'
import { resetTLBridge, setTLState, tlCommands } from '../test/tl-react-bridge'
import { TLChatRail } from './chat-rail'
import { chatStore, readBootstrap } from './chat-store'

/**
 * `<chat-rail/>` in a host too narrow for its column (#565).
 *
 * Below `drawerBelow` rem of host width the rail keeps its strip and opens a section or its pane as
 * a drawer over the conversation. The host's width is what a `ResizeObserver` reports, so the run
 * needs a live DOM - hence jsdom - and an observer it can drive: jsdom lays nothing out, so the
 * host's `clientWidth` is stated here and the observer is told when it changed.
 */

/** Runs `work` and commits what it caused, effects included; React's production build has no `act`. */
function act(work: () => void): void {
  flushSync(work)
}

const observers: FakeResizeObserver[] = []

class FakeResizeObserver {
  constructor(private readonly callback: (entries: { contentRect: { width: number } }[]) => void) {
    observers.push(this)
  }
  observe(): void {}
  unobserve(): void {}
  disconnect(): void {
    observers.splice(observers.indexOf(this), 1)
  }
  fire(): void {
    this.callback([{ contentRect: { width: hostWidth } }])
  }
}

class FakeEventSource {
  readyState = 0
  addEventListener(): void {}
  removeEventListener(): void {}
  close(): void {}
}

const PROGRESS = { controlId: 'c2', module: 'TLDataList', state: {} }
const INVOICES = { controlId: 'c3', module: 'TLDataList', state: {} }
const PANE = { controlId: 'c4', module: 'TLForm', state: {} }
const TABLE = { controlId: 'c5', module: 'TLTableView', state: {} }

/** One step of the pane's history, as the server writes it. */
const FORM_STEP = { id: 1, kind: 'form', title: 'INV-1', control: PANE }

/** A chat id per test: a chat's store outlives the element, and no test may see the last one's. */
let chatId = ''
let serial = 0

/** The state a `<chat-rail/>` control writes, as the browser reads it. */
function railState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    chatId,
    windowId: 'w1',
    authenticated: true,
    language: 'en',
    // The user left the rail open on a wide page.
    railOpen: true,
    collapsedSections: [],
    strip: true,
    open: true,
    drawerBelow: 70,
    paneMode: 'card',
    paneSteps: [],
    sections: [
      { name: 'progress', label: 'Progress', icon: '', open: true, content: PROGRESS },
      { name: 'invoices', label: 'Invoices', icon: '', open: true, content: INVOICES },
    ],
    sectionBadges: {},
    ...overrides,
  }
}

let host: HTMLDivElement
let root: Root
let hostWidth = 0

/** Mounts the rail into a host of the given width (px, at a 16 px rem, so 70 rem = 1120 px). */
function mount(width: number, overrides: Record<string, unknown> = {}): void {
  hostWidth = width
  render(overrides)
}

/** Re-renders with a new control state, as a server update does. */
function render(overrides: Record<string, unknown> = {}): void {
  const state = railState(overrides)
  setTLState(state)
  act(() => root.render(React.createElement(TLChatRail, { controlId: 'rail', state })))
}

function resize(width: number): void {
  hostWidth = width
  act(() => observers.forEach((observer) => observer.fire()))
}

function rail(): HTMLElement {
  return host.querySelector<HTMLElement>('.tl-chat-rail')!
}

function stripItem(index: number): HTMLButtonElement {
  return host.querySelectorAll<HTMLButtonElement>('.side-strip-item')[index]
}

function column(): HTMLElement {
  return host.querySelector<HTMLElement>('.rail-column')!
}

function scrim(): HTMLElement | null {
  return host.querySelector<HTMLElement>('.rail-drawer-scrim')
}

function click(element: Element): void {
  act(() => (element as HTMLElement).click())
}

function key(element: Element, name: string, init: KeyboardEventInit = {}): KeyboardEvent {
  const event = new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true, ...init })
  act(() => {
    element.dispatchEvent(event)
  })
  return event
}

function store() {
  return chatStore(chatId, readBootstrap(railState()))
}

/**
 * What the drawer asks to tell a shown element from a hidden one. jsdom lays nothing out and answers
 * "no boxes" for everything, so the stand-in says what the stylesheet would: an element inside
 * something `hidden` or `.is-hidden` has none.
 */
const realClientRects = HTMLElement.prototype.getClientRects
beforeEach(() => {
  HTMLElement.prototype.getClientRects = function (this: HTMLElement) {
    const hidden = this.closest('[hidden], .is-hidden') != null
    return (hidden ? [] : [new DOMRect(0, 0, 1, 1)]) as unknown as DOMRectList
  }
})
afterEach(() => {
  HTMLElement.prototype.getClientRects = realClientRects
})

beforeEach(() => {
  chatId = `rail-drawer-${++serial}`
  const globals = globalThis as unknown as Record<string, unknown>
  globals.ResizeObserver = FakeResizeObserver
  globals.EventSource = FakeEventSource
  globals.fetch = vi.fn(async () => ({ ok: true, status: 200, json: async () => ({}) }))
  host = document.createElement('div')
  Object.defineProperty(host, 'clientWidth', { get: () => hostWidth })
  document.body.append(host)
  root = createRoot(host)
})

afterEach(() => {
  act(() => root.unmount())
  host.remove()
  observers.length = 0
  resetTLBridge()
})

describe('the rail in a wide host', () => {
  it('is a column, opened as the user left it, with no scrim', () => {
    mount(1200)
    expect(rail().className).toBe('tl-chat-rail')
    expect(column().classList.contains('is-hidden')).toBe(false)
    expect(scrim()).toBeNull()
  })

  it('stays a column at every width with drawer-below="0"', () => {
    mount(320, { drawerBelow: 0 })
    expect(rail().classList.contains('is-drawer')).toBe(false)
  })
})

describe('the rail in a narrow host', () => {
  it('keeps only its strip, closed, although the user left the column open', () => {
    mount(400)
    expect(rail().classList.contains('is-drawer')).toBe(true)
    expect(rail().classList.contains('is-drawer-open')).toBe(false)
    expect(column().classList.contains('is-hidden')).toBe(true)
    expect(store().getState().sideOpen).toBe(true)
  })

  it('opens a section from the strip over the conversation, with a scrim and focus inside', () => {
    mount(400)
    stripItem(1).focus()
    click(stripItem(1))
    expect(rail().classList.contains('is-drawer-open')).toBe(true)
    expect(column().classList.contains('is-hidden')).toBe(false)
    expect(scrim()).not.toBeNull()
    expect(column().contains(document.activeElement)).toBe(true)
    // The user's column choice is not what the drawer writes.
    expect(store().getState().sideOpen).toBe(true)
  })

  it('closes on Escape and hands focus back to the strip icon', () => {
    mount(400)
    stripItem(1).focus()
    click(stripItem(1))
    key(document.activeElement!, 'Escape')
    expect(rail().classList.contains('is-drawer-open')).toBe(false)
    expect(document.activeElement).toBe(stripItem(1))
  })

  it('leaves Escape to a search field that still has text', () => {
    mount(400)
    click(stripItem(0))
    const search = document.createElement('input')
    search.type = 'search'
    search.value = 'abc'
    column().append(search)
    const event = key(search, 'Escape')
    expect(event.defaultPrevented).toBe(false)
    expect(rail().classList.contains('is-drawer-open')).toBe(true)
  })

  it('closes on a click on the scrim and on a second click on the same icon', () => {
    mount(400)
    click(stripItem(0))
    click(scrim()!)
    expect(rail().classList.contains('is-drawer-open')).toBe(false)
    click(stripItem(0))
    click(stripItem(0))
    expect(rail().classList.contains('is-drawer-open')).toBe(false)
  })

  it('wraps Tab inside the rail while the drawer is open', () => {
    mount(400)
    click(stripItem(0))
    const heads = host.querySelectorAll<HTMLButtonElement>('.side-section-head')
    const last = heads[heads.length - 1]
    last.focus()
    const forward = key(last, 'Tab')
    expect(forward.defaultPrevented).toBe(true)
    expect(document.activeElement).toBe(stripItem(0))
    const back = key(stripItem(0), 'Tab', { shiftKey: true })
    expect(back.defaultPrevented).toBe(true)
    expect(document.activeElement).toBe(last)
  })

  it('opens and closes from the conversation rail button, which reports the drawer', () => {
    mount(400)
    act(() => store().actions.toggleSide(0))
    expect(rail().classList.contains('is-drawer-open')).toBe(true)
    expect(store().getState().railDrawerOpen).toBe(true)
    act(() => store().actions.toggleSide(0))
    expect(rail().classList.contains('is-drawer-open')).toBe(false)
    expect(store().getState().sideOpen).toBe(true)
  })

  it('shows the object pane in the drawer, and Escape folds the pane away with it', () => {
    mount(400)
    render({ paneSteps: [FORM_STEP] })
    expect(rail().classList.contains('is-drawer-open')).toBe(true)
    const pane = host.querySelector<HTMLElement>('.side-pane')!
    expect(pane.hidden).toBe(false)
    // A drawer pane has no splitter and never grows past the host.
    expect(host.querySelector('.side-splitter')).toBeNull()
    expect(pane.style.minWidth).toBe('0px')
    key(pane, 'Escape')
    // Folded, not emptied: a tap beside a drawer means "let me see the conversation".
    expect(tlCommands().map((command) => command.name)).not.toContain('closePane')
    expect(pane.hidden).toBe(true)
    expect(rail().classList.contains('is-drawer-open')).toBe(false)
  })

  it('takes the focus into the pane that a row pick opens, so Escape still reaches the drawer', () => {
    mount(400)
    click(stripItem(1))
    // The picked row is hidden with the column, and the browser drops the focus onto the page.
    ;(document.activeElement as HTMLElement).blur()
    render({ paneSteps: [FORM_STEP] })
    const pane = host.querySelector<HTMLElement>('.side-pane')!
    expect(pane.contains(document.activeElement)).toBe(true)
    key(document.activeElement!, 'Escape')
    expect(pane.hidden).toBe(true)
  })

  it('gives the column back, as the user left it, when the host grows wide again', () => {
    mount(400)
    click(stripItem(0))
    resize(1200)
    expect(rail().className).toBe('tl-chat-rail')
    expect(column().classList.contains('is-hidden')).toBe(false)
    expect(store().getState().railDrawer).toBe(false)
    // Narrow again: closed, not open over the conversation on its own.
    resize(400)
    expect(rail().classList.contains('is-drawer-open')).toBe(false)
  })
})

describe('the pane history (#616)', () => {
  const TABLE_STEP = { id: 1, kind: 'table', title: 'Invoice (4)', control: TABLE }
  const ROW_STEP = { id: 2, kind: 'form', title: 'INV-1', control: PANE }

  function pane(): HTMLElement {
    return host.querySelector<HTMLElement>('.side-pane')!
  }

  it('asks the server for the step below on "back", and to empty the pane on the cross', () => {
    mount(1400, { paneSteps: [TABLE_STEP, ROW_STEP] })
    click(host.querySelector('.side-back')!)
    click(host.querySelector('.side-pane-close')!)
    expect(tlCommands().map((command) => command.name)).toEqual(['paneBack', 'closePane'])
  })

  it('shows the step below as it was left, from the same mounted element', () => {
    mount(1400, { paneSteps: [TABLE_STEP] })
    const table = host.querySelector('[data-control-id="c5"]')
    render({ paneSteps: [TABLE_STEP, ROW_STEP] })
    expect(host.querySelector('[data-control-id="c5"]')).toBe(table)
    expect(table!.parentElement!.classList.contains('is-stashed')).toBe(true)
    render({ paneSteps: [TABLE_STEP] })
    expect(host.querySelector('[data-control-id="c5"]')).toBe(table)
    expect(table!.parentElement!.classList.contains('is-stashed')).toBe(false)
  })

  it('folds away with its content kept, and the strip brings it back without a server call', () => {
    mount(1400, { paneSteps: [TABLE_STEP] })
    const table = host.querySelector('[data-control-id="c5"]')
    click(host.querySelector('.side-pane-collapse')!)
    expect(pane().hidden).toBe(true)
    // The sections the pane stood over are what the rail shows meanwhile.
    expect(column().classList.contains('is-hidden')).toBe(false)
    const expand = host.querySelector<HTMLButtonElement>('.side-strip-item.is-pane')!
    expect(expand.getAttribute('aria-expanded')).toBe('false')
    expect(expand.getAttribute('aria-label')).toContain('Invoice (4)')
    click(expand)
    expect(pane().hidden).toBe(false)
    expect(host.querySelector('[data-control-id="c5"]')).toBe(table)
    expect(tlCommands()).toEqual([])
  })

  it('shows something new even while folded away', () => {
    mount(1400, { paneSteps: [TABLE_STEP] })
    click(host.querySelector('.side-pane-collapse')!)
    render({ paneSteps: [TABLE_STEP, ROW_STEP] })
    expect(pane().hidden).toBe(false)
    expect(pane().textContent).toContain('INV-1')
  })
})

