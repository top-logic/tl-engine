// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { flushSync } from 'react-dom'
import { createRoot, type Root } from 'react-dom/client'
import { resetTLBridge, setTLState, tlCommands } from '../test/tl-react-bridge'
import { CMD_BACK, CMD_CLOSE, readPaneSteps, STEPS, TLPresentationPane } from './presentation-pane'

/** Runs `work` and commits what it caused; React's production build has no `act`. */
function act(work: () => void): void {
  flushSync(work)
}

/** A step of the history as the server writes it. */
function step(id: number, kind: string, title: string): Record<string, unknown> {
  return { id, kind, title, control: { controlId: `step-${id}`, module: 'TLTableView', state: {} } }
}

let host: HTMLDivElement
let root: Root

beforeEach(() => {
  host = document.createElement('div')
  document.body.append(host)
  root = createRoot(host)
})

afterEach(() => {
  act(() => root.unmount())
  host.remove()
  resetTLBridge()
})

function render(steps: Record<string, unknown>[]): void {
  const state = { [STEPS]: steps }
  setTLState(state)
  act(() => root.render(React.createElement(TLPresentationPane, { controlId: 'pane', state })))
}

function button(label: string): HTMLButtonElement | null {
  return host.querySelector<HTMLButtonElement>(`button[aria-label="${label}"]`)
}

function steps(): HTMLElement[] {
  return [...host.querySelectorAll<HTMLElement>('.pane-step')]
}

describe('the presentation pane', () => {
  it('shows the title of a single step and a close button, but no way back', () => {
    render([step(1, 'table', 'Customers (12)')])
    expect(host.querySelector('.tl-chat-presentation__title')!.textContent).toBe('Customers (12)')
    expect(button('Close')).not.toBeNull()
    expect(button('Back')).toBeNull()
  })

  it('offers the way back above a step opened from another one', () => {
    render([step(1, 'table', 'Customers (12)'), step(2, 'form', 'Anna')])
    expect(host.querySelector('.tl-chat-presentation__title')!.textContent).toBe('Anna')
    expect(button('Back')).not.toBeNull()
  })

  it('sends back and close to the server', () => {
    render([step(1, 'table', 'Customers (12)'), step(2, 'form', 'Anna')])
    act(() => button('Back')!.click())
    act(() => button('Close')!.click())
    expect(tlCommands().map((command) => command.name)).toEqual([CMD_BACK, CMD_CLOSE])
  })

  it('keeps every step mounted, shows the top one and makes the others inert', () => {
    render([step(1, 'table', 'Customers (12)'), step(2, 'form', 'Anna')])
    const [below, top] = steps()
    expect(below.className).toBe('pane-step is-table is-stashed')
    expect(below.hasAttribute('inert')).toBe(true)
    expect(below.querySelector('tl-child')!.getAttribute('data-control-id')).toBe('step-1')
    expect(top.className).toBe('pane-step is-form')
    expect(top.hasAttribute('inert')).toBe(false)
  })

  it('is hidden while the history is empty', () => {
    render([])
    expect(host.querySelector<HTMLElement>('.tl-chat-presentation')!.hidden).toBe(true)
    expect(host.querySelector('header')).toBeNull()
  })
})

describe('readPaneSteps', () => {
  it('reads only well-formed steps, a kind it does not know as a table', () => {
    expect(
      readPaneSteps({
        [STEPS]: [
          step(1, 'chart', 'Sales'),
          { id: 'x', kind: 'form', control: {} },
          { id: 2, kind: 'form' },
          { id: 3, kind: 'map', title: 'Odd', control: {} },
        ],
      }).map((entry) => [entry.id, entry.kind]),
    ).toEqual([
      [1, 'chart'],
      [3, 'table'],
    ])
    expect(readPaneSteps({})).toEqual([])
  })
})
