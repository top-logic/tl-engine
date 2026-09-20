import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { resetTLBridge, setTLState } from '../test/tl-react-bridge'
import { closesOnKey, launcherMark, readDockState, TLChatDock } from './chat-dock'
import { chatStore, type ChatBootstrap } from './chat-store'

afterEach(() => resetTLBridge())

/** The one `<chat>` an application normally nests in the dock, as the browser sees it. */
const CHAT = { controlId: 'c2', module: 'TLStack', state: {} }

/** The state a `<chat-dock/>` control writes. */
function dockState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    chatId: 'dock-test',
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

/** Renders the dock against a control state, without a browser. */
function render(state: Record<string, unknown>): string {
  setTLState(state)
  return renderToStaticMarkup(
    React.createElement(TLChatDock, { controlId: state.chatId as string, state }),
  )
}

/**
 * Seeds the store the dock will join, so a render sees a conversation that has something to report.
 *
 * `useChat` hands out the store that already exists for a `chat-id`, and a static render reads its
 * snapshot, so driving the store here is how a render test reaches state no control state carries.
 */
function seedChat(chatId: string, boot: Partial<ChatBootstrap> = {}) {
  const store = chatStore(chatId, {
    windowId: 'w1',
    authenticated: true,
    userName: 'User',
    welcomeName: '',
    language: 'en',
    traceMode: 'summary',
    layoutKey: chatId,
    railOpen: null,
    railOpenDefault: false,
    paneWidth: 0,
    paneWidthDefault: 0,
    badgeSeen: 0,
    sectionChoices: {},
    ...boot,
  })
  const release = store.subscribe(() => undefined)
  return { store, release }
}

describe('readDockState', () => {
  it('reads the frame the control serialized', () => {
    expect(readDockState(dockState())).toEqual({
      icon: 'css:bi bi-stars',
      label: 'Albert',
      open: false,
    })
  })

  /**
   * `dockOpen`, not `open`: `open` is how `<chat-rail/>` states its own first-load default, and the
   * whole bootstrap is read out of one state map - a dock writing `open` would decide for the rail
   * inside it whether that rail starts open.
   */
  it('takes the panel state from dockOpen and ignores the rail default', () => {
    expect(readDockState({ dockOpen: true, open: false }).open).toBe(true)
    expect(readDockState({ open: true }).open).toBe(false)
  })

  it('is empty rather than broken when the control wrote nothing', () => {
    expect(readDockState({})).toEqual({ icon: '', label: '', open: false })
  })
})

describe('launcherMark', () => {
  it('shows the run before the count, and neither while the panel is open', () => {
    expect(launcherMark(false, true, 3)).toBe('busy')
    expect(launcherMark(false, false, 3)).toBe('badge')
    expect(launcherMark(false, false, 0)).toBe('none')
    expect(launcherMark(true, true, 3)).toBe('none')
  })
})

describe('closesOnKey', () => {
  /**
   * Escape closes an open dock and means nothing to a closed one. That this handler is reached at
   * all is the DOM's half of the rule - a popover inside the panel stops the key first - and is
   * checked in the browser rather than here, since the vitest run has no DOM to dispatch into.
   */
  it('closes an open dock on Escape and nothing else', () => {
    expect(closesOnKey('Escape', true)).toBe(true)
    expect(closesOnKey('Escape', false)).toBe(false)
    expect(closesOnKey('Enter', true)).toBe(false)
  })
})

describe('the rendered dock', () => {
  it('renders a launcher that says what it controls and that it is closed', () => {
    const html = render(dockState())
    expect(html).toContain('class="tl-chat-dock"')
    expect(html).toContain('aria-expanded="false"')
    expect(html).toContain('aria-controls="dock-test-panel"')
    expect(html).toContain('aria-label="Open Albert"')
    expect(html).toContain('class="bi bi-stars"')
  })

  /**
   * Hidden, not dropped.
   *
   * The conversation's event stream and its transcript live in the elements inside the panel, and
   * the store drops a chat when its last element leaves - so a dock that unmounted its panel would
   * hand the user a new, empty chat on every open.
   */
  it('keeps the panel mounted and hides it while the dock is closed', () => {
    const html = render(dockState())
    expect(html).toMatch(/class="tl-chat-dock__panel"[^>]*hidden/)
    expect(html).toContain('data-module="TLStack"')
  })

  it('shows the panel and the close wording while the dock is open', () => {
    const html = render(dockState({ dockOpen: true, chatId: 'dock-open' }))
    expect(html).toContain('class="tl-chat-dock is-open"')
    expect(html).toContain('aria-expanded="true"')
    expect(html).toContain('aria-label="Close Albert"')
    expect(html).not.toMatch(/class="tl-chat-dock__panel"[^>]*hidden/)
  })

  it('names the assistant itself when the application configured no label', () => {
    expect(render(dockState({ label: '', chatId: 'dock-unnamed' }))).toContain(
      'aria-label="Open Assistant"',
    )
  })

  /** What the rail's sections reported, minus what the user has already looked at. */
  it('counts the unseen news on the launcher of a closed dock', () => {
    const { store, release } = seedChat('dock-badge')
    store.actions.registerRail()
    store.actions.publishBadgeTotal(4)

    const html = render(dockState({ chatId: 'dock-badge' }))
    expect(html).toContain('<span class="tl-chat-dock__badge" aria-label="4 new">4</span>')

    // Opening the dock is the user looking: the watermark moves to the total, not to zero.
    store.actions.markBadgesSeen(store.getState().badgeTotal)
    expect(store.getState().badgeSeen).toBe(4)
    expect(render(dockState({ chatId: 'dock-badge' }))).not.toContain('tl-chat-dock__badge')

    release()
  })

  it('shows no badge for news the user has already seen', () => {
    const { store, release } = seedChat('dock-seen', { badgeSeen: 3 })
    store.actions.registerRail()
    store.actions.publishBadgeTotal(3)

    expect(render(dockState({ chatId: 'dock-seen' }))).not.toContain('tl-chat-dock__badge')
    release()
  })

  /** A launcher onto a chat the servlet refuses every call of is an invitation to a dead end. */
  it('offers nothing at all in a session that is not signed in', () => {
    const html = render(dockState({ authenticated: false, chatId: 'dock-signed-out' }))
    expect(html).toContain('is-stood-down')
    expect(html).not.toContain('data-module')
    expect(html).not.toContain('<button')
  })

  /**
   * The other case where a dock must not appear: the page is showing this very conversation.
   *
   * Two views of one conversation give its draft channel two composers and its layout two rails.
   * The content is unmounted rather than hidden, so the rail inside the dock stops competing with
   * the page's rail for the one remembered layout.
   */
  it('stands down entirely while a page shows its conversation', () => {
    const { store, release } = seedChat('dock-stand-down')
    const onPage = store.actions.registerConversation(false)

    const html = render(dockState({ chatId: 'dock-stand-down' }))
    expect(html).toContain('is-stood-down')
    expect(html).not.toContain('<button')
    expect(html).not.toContain('data-module')

    // The page's conversation goes away (the user navigated): the dock comes back with it.
    onPage()
    const back = render(dockState({ chatId: 'dock-stand-down' }))
    expect(back).not.toContain('is-stood-down')
    expect(back).toContain('data-module="TLStack"')

    release()
  })

  /** The dock is a frame; a dock with nothing in it is a configuration, not a crash. */
  it('renders an empty panel when nothing is configured inside', () => {
    const html = render(dockState({ content: undefined, chatId: 'dock-empty' }))
    expect(html).toContain('class="tl-chat-dock__body"')
    expect(html).not.toContain('data-module')
  })
})
