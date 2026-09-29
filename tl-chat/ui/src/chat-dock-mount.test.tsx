// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { React } from 'tl-react-bridge'
import { flushSync } from 'react-dom'
import { createRoot, type Root } from 'react-dom/client'
import { resetTLBridge, setTLState } from '../test/tl-react-bridge'
import { TLChatDock } from './chat-dock'
import { chatStore, readBootstrap } from './chat-store'

/**
 * `<chat-dock/>` in a live DOM: what it mounts, and when.
 *
 * A static render performs no effect, and the question here is exactly what happens between a
 * commit and the effects that follow it: whether the dock's content - a `<chat>` with its own rail -
 * is mounted for a moment on a page that shows the dock's conversation itself. For that moment the
 * dock's rail stated its own `open="false"` over the page rail's `open="true"` (#552).
 */

/** Runs `work` and commits what it caused, effects included; React's production build has no `act`. */
function act(work: () => void): void {
  flushSync(work)
}

class FakeEventSource {
  readyState = 0
  addEventListener(): void {}
  removeEventListener(): void {}
  close(): void {}
}

const CHAT = { controlId: 'c2', module: 'TLStack', state: {} }

let chatId = ''
let serial = 0

function dockState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    chatId,
    windowId: 'w1',
    authenticated: true,
    language: 'en',
    icon: 'css:bi bi-stars',
    label: 'Albert',
    dockOpen: false,
    content: CHAT,
    ...overrides,
  }
}

/** A `<chat-conversation/>` on the page, reduced to what the dock hears of it. */
function PageConversation() {
  React.useEffect(() => {
    const store = chatStore(chatId, readBootstrap(dockState()))
    return store.actions.registerConversation(false)
  }, [])
  return null
}

let host: HTMLDivElement
let root: Root
/** Every child control that was ever put into the page, even for a moment. */
let mounted: string[] = []
let observer: MutationObserver

beforeEach(() => {
  chatId = `dock-mount-${++serial}`
  const globals = globalThis as unknown as Record<string, unknown>
  globals.EventSource = FakeEventSource
  globals.fetch = vi.fn(async () => ({ ok: true, status: 200, json: async () => ({}) }))
  host = document.createElement('div')
  document.body.append(host)
  root = createRoot(host)
  mounted = []
  observer = new MutationObserver((records) => {
    // Removed nodes count too: by the time this runs, a child that was mounted and dropped again
    // is no longer inside the node that was added, only inside the one that was removed.
    for (const record of records) {
      for (const node of [...record.addedNodes, ...record.removedNodes]) {
        if (!(node instanceof Element)) continue
        const children = node.matches('tl-child') ? [node] : [...node.querySelectorAll('tl-child')]
        mounted.push(...children.map((child) => child.getAttribute('data-module') ?? ''))
      }
    }
  })
  observer.observe(host, { subtree: true, childList: true })
})

afterEach(() => {
  act(() => root.unmount())
  observer.disconnect()
  host.remove()
  resetTLBridge()
})

function escape(): KeyboardEvent {
  return new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true })
}

/**
 * Lets the passive effects run and the commits they cause land, and the observer deliver what they
 * did: `flushSync` commits a render and its layout effects, not the effects that follow.
 */
async function records(): Promise<void> {
  for (let i = 0; i < 3; i++) await new Promise((resolve) => setTimeout(resolve, 0))
}

describe('the dock in a live page', () => {
  it('mounts its content from the second commit on, hidden while closed', async () => {
    setTLState(dockState())
    act(() => root.render(React.createElement(TLChatDock, { controlId: 'dock', state: dockState() })))
    await records()
    const child = host.querySelector('.tl-chat-dock__panel tl-child')
    expect(child?.getAttribute('data-module')).toBe('TLStack')
    expect(host.querySelector<HTMLElement>('.tl-chat-dock__panel')!.hidden).toBe(true)
  })

  it('never mounts its content beside a page that shows its conversation', async () => {
    setTLState(dockState())
    act(() =>
      root.render(
        React.createElement(
          React.Fragment,
          null,
          React.createElement(PageConversation),
          React.createElement(TLChatDock, { controlId: 'dock', state: dockState() }),
        ),
      ),
    )
    await records()
    expect(host.querySelector('.tl-chat-dock')!.className).toContain('is-stood-down')
    expect(mounted).toEqual([])
  })

  it('leaves open for an Escape that something inside it already answered', async () => {
    const state = dockState({ dockOpen: true })
    setTLState(state)
    act(() => root.render(React.createElement(TLChatDock, { controlId: 'dock', state })))
    await records()
    const inner = host.querySelector('.tl-chat-dock__panel tl-child')!
    // What the rail's drawer does with the key: it closes itself and says so.
    const answer = (event: Event) => event.preventDefault()
    inner.addEventListener('keydown', answer)
    act(() => {
      inner.dispatchEvent(escape())
    })
    expect(host.querySelector('.tl-chat-dock')!.className).toContain('is-open')

    // An Escape nothing inside answered closes the dock, as before.
    inner.removeEventListener('keydown', answer)
    act(() => {
      inner.dispatchEvent(escape())
    })
    expect(host.querySelector('.tl-chat-dock')!.className).not.toContain('is-open')
  })
})
