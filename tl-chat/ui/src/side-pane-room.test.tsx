// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { flushSync } from 'react-dom'
import { createRoot, type Root } from 'react-dom/client'
import { resetTLBridge } from '../test/tl-react-bridge'
import { railRoom, SidePane } from './side-pane'
import { bundle } from './i18n'

/**
 * How wide a `<chat-rail>` pane may become in the row it stands in (#586).
 *
 * The cap used to be the host width less a fixed 296 px, which left out the session list, the gaps
 * and the conversation's own floor: at 1440 px the Home key widened the pane to 887 px and the row
 * overflowed by 392. The room is now asked of the row itself, and jsdom lays nothing out, so the
 * row here is a small flex model: the demo's list (264 px, does not shrink), a conversation panel
 * that fills what is left down to its 320 px floor, the rail's 56 px strip and the pane, 24 px gaps.
 */

const HOST = 1183
const LIST = 264
const FLOOR = 320
const STRIP = 56
const GAP = 24

/** Runs `work` and commits what it caused, effects included; React's production build has no `act`. */
function act(work: () => void): void {
  flushSync(work)
}

let root: Root | null = null
let container: HTMLElement | null = null

beforeEach(() => {
  resetTLBridge()
})

afterEach(() => {
  if (root) act(() => root!.unmount())
  container?.remove()
  root = null
  container = null
})

/** The host row, laid out by {@link layout}; the pane goes into `rail`. */
function row(list = LIST) {
  const host = document.createElement('div')
  host.style.display = 'flex'
  host.style.flexDirection = 'row'
  host.style.columnGap = `${GAP}px`
  const sessions = document.createElement('div')
  const panel = document.createElement('div')
  panel.style.flexGrow = '1'
  const rail = document.createElement('div')
  const strip = document.createElement('nav')
  rail.append(strip)
  host.append(sessions, panel, rail)
  document.body.append(host)
  Object.defineProperty(host, 'clientWidth', { configurable: true, get: () => HOST })

  const paneOf = () => rail.querySelector<HTMLElement>('.side-pane')
  const paneWidth = () => {
    const pane = paneOf()
    return pane ? Math.max(360, parseFloat(pane.style.width) || 0) : 0
  }
  const box = (width: () => number) => () => ({ width: width() }) as DOMRect
  sessions.getBoundingClientRect = box(() => list)
  panel.getBoundingClientRect = box(() => Math.max(FLOOR, HOST - list - STRIP - paneWidth() - 2 * GAP))
  rail.getBoundingClientRect = box(() => STRIP + paneWidth())
  const layoutPane = () => {
    const pane = paneOf()
    if (pane) pane.getBoundingClientRect = box(paneWidth)
  }
  return { host, rail, panel, paneOf, layoutPane }
}

describe('railRoom', () => {
  it('leaves the list, the gaps, the strip and the conversation at its floor', () => {
    const { rail, layoutPane, paneOf } = row()
    const pane = document.createElement('div')
    pane.className = 'side-pane'
    pane.style.width = '420px'
    rail.append(pane)
    layoutPane()

    expect(railRoom(pane)).toBe(HOST - LIST - FLOOR - STRIP - 2 * GAP)
    expect(paneOf()!.style.width).toBe('420px')
  })

  it('grows by what the list gives up when it folds into its strip', () => {
    const { rail, layoutPane } = row(52)
    const pane = document.createElement('div')
    pane.className = 'side-pane'
    pane.style.width = '420px'
    rail.append(pane)
    layoutPane()

    expect(railRoom(pane)).toBe(HOST - 52 - FLOOR - STRIP - 2 * GAP)
  })

  it('measures nothing for a pane that is not laid out', () => {
    const { rail } = row()
    const pane = document.createElement('div')
    rail.append(pane)
    pane.getBoundingClientRect = () => ({ width: 0 }) as DOMRect

    expect(railRoom(pane)).toBe(0)
  })
})

describe('SidePane in a rail', () => {
  function mount(onWidth: (width: number) => void, width = 0) {
    const shape = row()
    container = shape.rail
    root = createRoot(shape.rail)
    // The rail is the root's container, so the pane's parent is the rail and its grandparent the row.
    act(() =>
      root!.render(
        React.createElement(
          SidePane as never,
          {
            title: 'Invoice',
            width,
            onWidth,
            onBack: () => undefined,
            labels: bundle('en'),
            placement: 'rail',
          },
          React.createElement('p', null, 'content'),
        ),
      ),
    )
    return shape
  }

  it('widens to the room on Home, not past it', () => {
    const widths: number[] = []
    const shape = mount((w) => widths.push(w))
    shape.layoutPane()
    const splitter = shape.paneOf()!.querySelector<HTMLElement>('.side-splitter')!
    act(() => {
      splitter.dispatchEvent(new KeyboardEvent('keydown', { key: 'Home', bubbles: true }))
    })

    expect(widths).toEqual([HOST - LIST - FLOOR - STRIP - 2 * GAP])
    expect(splitter.getAttribute('aria-valuemax')).toBe(String(HOST - LIST - FLOOR - STRIP - 2 * GAP))
  })

  it('grants a remembered width only as far as the row allows', () => {
    const shape = mount(() => undefined, 887)
    shape.layoutPane()
    // A key press measures the row; the render that follows clamps the remembered width.
    const splitter = shape.paneOf()!.querySelector<HTMLElement>('.side-splitter')!
    act(() => {
      splitter.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowLeft', bubbles: true }))
    })

    expect(shape.paneOf()!.style.width).toBe(`${HOST - LIST - FLOOR - STRIP - 2 * GAP}px`)
  })
  /**
   * A drag moves the pane on every pointer move but hands the store one width, whole pixels, when
   * the pointer lets go: every width the store receives is a preference write, and a fraction is
   * one the server refuses (#588).
   */
  it('stores a dragged width once, rounded, when the pointer lets go', async () => {
    const widths: number[] = []
    const shape = mount((w) => widths.push(w))
    shape.layoutPane()
    const splitter = shape.paneOf()!.querySelector<HTMLElement>('.side-splitter')!
    splitter.setPointerCapture = () => undefined
    shape.host.getBoundingClientRect = () => ({ right: HOST, width: HOST }) as DOMRect
    const pointer = (type: string, clientX = 0) =>
      act(() => {
        splitter.dispatchEvent(new MouseEvent(type, { bubbles: true, clientX }))
      })

    pointer('pointerdown')
    pointer('pointermove', HOST - 380.0333251953125)
    pointer('pointermove', HOST - 401.36669921875)
    // A pointer move is a continuous event, which React renders after the event, not inside it.
    await new Promise((resolve) => setTimeout(resolve, 0))
    expect(shape.paneOf()!.style.width).toBe('401.36669921875px')
    pointer('pointermove', HOST - 422.70001220703125)
    expect(widths).toEqual([])

    pointer('pointerup')
    expect(widths).toEqual([423])
  })

  it('stores nothing for a click on the splitter that did not move it', () => {
    const widths: number[] = []
    const shape = mount((w) => widths.push(w))
    shape.layoutPane()
    const splitter = shape.paneOf()!.querySelector<HTMLElement>('.side-splitter')!
    splitter.setPointerCapture = () => undefined
    act(() => {
      splitter.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true }))
    })
    act(() => {
      splitter.dispatchEvent(new MouseEvent('pointerup', { bubbles: true }))
    })

    expect(widths).toEqual([])
  })
})
