// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { flushSync } from 'react-dom'
import { createRoot, type Root } from 'react-dom/client'
import { resetTLBridge, setTLState, tlCommands } from '../test/tl-react-bridge'
import { TLDataList } from './data-list'

/**
 * `<data-list>` in a host too narrow for its column (#548).
 *
 * Below `drawerBelow` rem of host width a collapsible list keeps its strip and opens as a drawer
 * over its neighbour. What decides it is the host's width as a `ResizeObserver` reports it, so the
 * run needs a live DOM - hence jsdom - and an observer it can drive: jsdom lays nothing out, so the
 * host's `clientWidth` is stated here and the observer is told when it changed.
 */

/**
 * Runs `work` and commits what it caused, effects included, before returning. The bundle is tested
 * against React's production build, which has no `act`; `flushSync` does the same for this suite.
 */
function act(work: () => void): void {
  flushSync(work)
}

/** The observers the component created, so a test can say "the host was resized". */
const observers: FakeResizeObserver[] = []

class FakeResizeObserver {
  constructor(private readonly callback: () => void) {
    observers.push(this)
  }
  observe(): void {}
  unobserve(): void {}
  disconnect(): void {
    observers.splice(observers.indexOf(this), 1)
  }
  fire(): void {
    this.callback()
  }
}

/** The state a `<data-list>` control writes for a chat session list. */
function listState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    language: 'en',
    search: true,
    collapsible: true,
    collapsed: false,
    drawerBelow: 44,
    title: '',
    emptyText: 'No chats yet',
    selectedId: 'r2',
    buttons: [{ label: 'New chat', icon: 'css:bi bi-plus-lg' }],
    rows: [
      { id: 'r1', lines: [{ text: 'Invoice' }] },
      { id: 'r2', lines: [{ text: 'Booking' }] },
    ],
    ...overrides,
  }
}

let host: HTMLDivElement
let root: Root
let hostWidth = 0

/** Mounts the list into a host of the given width (px, at a 16 px rem, so 44 rem = 704 px). */
function mount(width: number, overrides: Record<string, unknown> = {}): void {
  hostWidth = width
  setTLState(listState(overrides))
  act(() => root.render(React.createElement(TLDataList, { controlId: 'list', state: listState(overrides) })))
}

/** The host becomes this wide, and the observer says so. */
function resize(width: number): void {
  hostWidth = width
  act(() => observers.forEach((observer) => observer.fire()))
}

function list(): HTMLElement {
  return host.querySelector<HTMLElement>('.tl-data-list')!
}

function arrow(): HTMLButtonElement {
  return host.querySelector<HTMLButtonElement>('.rail-collapse')!
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

beforeEach(() => {
  ;(globalThis as { ResizeObserver?: unknown }).ResizeObserver = FakeResizeObserver
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

describe('the list in a wide host', () => {
  it('is a column, and its arrow collapses and expands the column', () => {
    mount(1100)
    expect(list().className).toBe('tl-data-list')
    expect(host.querySelector('.data-list-scrim')).toBeNull()

    click(arrow())
    expect(list().className).toBe('tl-data-list is-collapsed')
    expect(arrow().getAttribute('aria-expanded')).toBe('false')

    click(arrow())
    expect(list().className).toBe('tl-data-list')
  })
})

describe('the css-class of the element', () => {
  it('is written on the root after the classes of the list itself', () => {
    mount(560, { cssClass: 'appRail' })
    expect(list().className).toBe('tl-data-list is-collapsed is-drawer appRail')
  })
})

describe('the list in a narrow host', () => {
  it('is a closed drawer: a strip in the flow, the arrow offering to open it', () => {
    mount(560)
    expect(list().className).toBe('tl-data-list is-collapsed is-drawer')
    expect(arrow().getAttribute('aria-expanded')).toBe('false')
    expect(arrow().getAttribute('aria-label')).toBe('Expand chat history')
  })

  it('switches at the configured width, measured in rem', () => {
    mount(704)
    expect(list().classList.contains('is-drawer')).toBe(false)
    resize(703)
    expect(list().classList.contains('is-drawer')).toBe(true)

    act(() => root.unmount())
    root = createRoot(host)
    mount(703, { drawerBelow: 30 })
    expect(list().classList.contains('is-drawer')).toBe(false)
  })

  it('opens over its neighbour with a scrim, and moves focus to the open row', () => {
    mount(560)
    click(arrow())
    expect(list().className).toBe('tl-data-list is-drawer is-drawer-open')
    expect(host.querySelector('.data-list-scrim')).not.toBeNull()
    expect(arrow().getAttribute('aria-expanded')).toBe('true')
    expect(document.activeElement?.textContent).toBe('Booking')
  })

  it('moves focus to the first row when none is open', () => {
    mount(560, { selectedId: '' })
    click(arrow())
    expect(document.activeElement?.textContent).toBe('Invoice')
  })

  it('closes on the arrow, handing focus back to it', () => {
    mount(560)
    click(arrow())
    click(arrow())
    expect(list().className).toBe('tl-data-list is-collapsed is-drawer')
    expect(document.activeElement).toBe(arrow())
  })

  it('closes when a row is picked, and still picks it', () => {
    mount(560)
    click(arrow())
    click(host.querySelector('.rail-open')!)
    expect(list().classList.contains('is-drawer-open')).toBe(false)
    expect(document.activeElement).toBe(arrow())
    expect(tlCommands()).toEqual([{ name: 'select', args: { id: 'r1' } }])
  })

  it('closes on a header command such as "New chat", and still runs it', () => {
    mount(560)
    click(arrow())
    click(host.querySelector('.rail-new')!)
    expect(list().classList.contains('is-drawer-open')).toBe(false)
    expect(tlCommands()).toEqual([{ name: 'button', args: { index: 0 } }])
  })

  it('closes on a click on the scrim', () => {
    mount(560)
    click(arrow())
    click(host.querySelector('.data-list-scrim')!)
    expect(list().classList.contains('is-drawer-open')).toBe(false)
    expect(host.querySelector('.data-list-scrim')).toBeNull()
    expect(document.activeElement).toBe(arrow())
  })

  it('closes on Escape, handing focus back to the arrow', () => {
    mount(560)
    click(arrow())
    const event = key(document.activeElement!, 'Escape')
    expect(event.defaultPrevented).toBe(true)
    expect(list().classList.contains('is-drawer-open')).toBe(false)
    expect(document.activeElement).toBe(arrow())
  })

  /* Escape in a search field with text clears the text first; the drawer stays. */
  it('leaves Escape to a search field that has text in it', () => {
    mount(560)
    click(arrow())
    const search = host.querySelector<HTMLInputElement>('input[type="search"]')!
    search.value = 'inv'
    key(search, 'Escape')
    expect(list().classList.contains('is-drawer-open')).toBe(true)
  })

  it('keeps Tab inside the open drawer', () => {
    mount(560)
    click(arrow())
    const focusable = [
      ...host.querySelectorAll<HTMLElement>('aside button, aside input'),
    ]
    const first = focusable[0]
    const last = focusable[focusable.length - 1]
    expect(first).toBe(arrow())

    act(() => last.focus())
    expect(key(last, 'Tab').defaultPrevented).toBe(true)
    expect(document.activeElement).toBe(first)

    expect(key(first, 'Tab', { shiftKey: true }).defaultPrevented).toBe(true)
    expect(document.activeElement).toBe(last)
  })
})

describe('a resize', () => {
  it('keeps the user’s column choice across the drawer and back', () => {
    mount(1100)
    click(arrow())
    expect(list().className).toBe('tl-data-list is-collapsed')

    resize(560)
    click(arrow())
    expect(list().className).toBe('tl-data-list is-drawer is-drawer-open')

    resize(1100)
    expect(list().className).toBe('tl-data-list is-collapsed')
    expect(host.querySelector('.data-list-scrim')).toBeNull()
  })

  it('keeps an expanded column expanded, and shuts an open drawer that became a column', () => {
    mount(560)
    click(arrow())
    resize(1100)
    expect(list().className).toBe('tl-data-list')
    resize(560)
    expect(list().className).toBe('tl-data-list is-collapsed is-drawer')
  })
})

describe('a list the drawer does not apply to', () => {
  it('is never a drawer when it cannot collapse', () => {
    mount(320, { collapsible: false })
    expect(list().className).toBe('tl-data-list')
    expect(host.querySelector('.rail-collapse')).toBeNull()
    expect(observers).toHaveLength(0)
  })

  it('is never a drawer with drawer-below 0', () => {
    mount(320, { drawerBelow: 0 })
    expect(list().className).toBe('tl-data-list')
    click(arrow())
    expect(list().className).toBe('tl-data-list is-collapsed')
  })
})
