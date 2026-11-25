import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
// @ts-expect-error The type check of the browser bundle knows no Node modules; the test runs on Node.
import { readFileSync } from 'node:fs'
import { escapeHandlers, resetTLBridge, setTLState, tlCommands } from '../test/tl-react-bridge'
import { ARG_OPEN, CMD_SET_OPEN, DOCK_BADGE, launcherMark, readDockState, TLChatDock } from './chat-dock'
import { chatStore, type ChatBootstrap } from './chat-store'

afterEach(() => resetTLBridge())

/** The one `<chat>` an application normally nests in the dock, as the browser sees it. */
const CHAT = { controlId: 'c2', module: 'TLStack', state: {} }

/** The state a `<chat-dock/>` control writes. */
function dockState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    chatId: 'dock-test',
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
    authenticated: true,
    userName: 'User',
    welcomeName: '',
    language: 'en',
    traceMode: 'summary',
    autoApprove: false,
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
      badge: null,
    })
  })

  it('takes the panel state from dockOpen', () => {
    expect(readDockState({ dockOpen: true }).open).toBe(true)
    expect(readDockState({ open: true }).open).toBe(false)
  })

  it('reads the badge the server sends', () => {
    expect(readDockState({ [DOCK_BADGE]: { content: '4', label: 'New' } }).badge).toEqual({
      content: '4',
      label: 'New',
    })
    expect(readDockState({ [DOCK_BADGE]: null }).badge).toBeNull()
  })

  it('is empty rather than broken when the control wrote nothing', () => {
    expect(readDockState({})).toEqual({ icon: '', label: '', open: false, badge: null })
  })
})

describe('launcherMark', () => {
  it('shows the run before the count, and neither while the panel is open', () => {
    const count = { content: '3' }
    expect(launcherMark(false, true, count)).toBe('busy')
    expect(launcherMark(false, false, count)).toBe('badge')
    expect(launcherMark(false, false, null)).toBe('none')
    expect(launcherMark(true, true, count)).toBe('none')
  })
})

describe('Escape in the dock', () => {
  /**
   * Escape reaches the dock through the keyboard dispatcher's stack of scopes: the panel is a scope
   * of its own, binding Escape to closing the dock, so a layer opened inside it - the drawer of a
   * side rail, a popover - is a scope above it and answers Escape first. That the inner layer wins is
   * the dispatcher's rule, tested with the drawer in the engine (TLDrawer.test.tsx).
   */
  it('binds Escape in the scope of its panel to closing the dock', () => {
    render(dockState({ dockOpen: true }))
    expect(escapeHandlers).toHaveLength(1)

    escapeHandlers[0]()
    expect(tlCommands().map((sent) => [sent.name, sent.args])).toContainEqual([CMD_SET_OPEN, { [ARG_OPEN]: false }])
  })
})

describe('the panel of the dock', () => {
  /**
   * A drawer opened inside the dock - the drawer of a side rail - is anchored at the nearest
   * positioned element around it. That must be the panel, which clips it, not the dock with its
   * launcher: otherwise the drawer and its scrim overflow the panel and cover the launcher.
   */
  it('is the positioned, clipping anchor of what opens inside it', () => {
    const css: string = readFileSync(new URL('./styles.css', import.meta.url), 'utf8')
    const rule = /\.tl-chat-dock__panel\s*\{([^}]*)\}/.exec(css)?.[1] ?? ''
    expect(rule).toMatch(/position:\s*relative/)
    expect(rule).toMatch(/overflow:\s*hidden/)
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
   * hand the user a new, empty chat on every open. That the content is then mounted inside it, from
   * the second commit on, is what `chat-dock-mount.test.tsx` shows in a live DOM.
   */
  it('keeps the panel mounted and hides it while the dock is closed', () => {
    const html = render(dockState())
    expect(html).toMatch(/class="tl-chat-dock__panel"[^>]*hidden/)
  })

  it('writes the css-class of the element on its root, after its own classes', () => {
    const html = render(dockState({ cssClass: 'appDock' }))
    expect(html).toMatch(/^<div id="dock-test" class="tl-chat-dock appDock"/)
  })

  /** On the first commit the dock cannot know yet whether the page shows its conversation. */
  it('mounts nothing of its content on the first commit', () => {
    expect(render(dockState({ chatId: 'dock-first-commit' }))).not.toContain('data-module')
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

  /** The badge the server sends stands on the launcher of a closed dock, not beside the open panel. */
  it('shows the badge of the server on the launcher of a closed dock', () => {
    const badge = { content: '4', label: 'New' }
    const html = render(dockState({ chatId: 'dock-badge', [DOCK_BADGE]: badge }))
    expect(html).toContain('aria-label="Open Albert, New: 4"')
    expect(html).toContain('<tl-badge data-content="4" data-label="New" data-alone="true"></tl-badge>')

    const open = render(dockState({ chatId: 'dock-badge', [DOCK_BADGE]: badge, dockOpen: true }))
    expect(open).not.toContain('tl-badge')
    expect(render(dockState({ chatId: 'dock-badge' }))).not.toContain('tl-badge')
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
   * Two views of one conversation give its draft channel two composers. The content is unmounted
   * rather than hidden, so the conversation inside the dock stops competing with the page's.
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

    release()
  })

  /** The dock is a frame; a dock with nothing in it is a configuration, not a crash. */
  it('renders an empty panel when nothing is configured inside', () => {
    const html = render(dockState({ content: undefined, chatId: 'dock-empty' }))
    expect(html).toContain('class="tl-chat-dock__body"')
    expect(html).not.toContain('data-module')
  })
})
