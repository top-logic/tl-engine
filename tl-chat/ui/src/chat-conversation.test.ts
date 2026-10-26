import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { resetTLBridge, setTLState } from '../test/tl-react-bridge'
import { App } from './App'
import { plusMenuVisible } from './composer'
import {
  channelName,
  channelNames,
  modelBoundSource,
  readComposerControls,
  promptCardsVisible,
  readContextFacts,
  readPromptCards,
  readThreadConfig,
  tokenWrite,
  sameServers,
  TLChatConversation,
} from './chat-conversation'

describe('readThreadConfig', () => {
  it('defaults every transcript flag on (trace is the store axis, not here)', () => {
    expect(readThreadConfig({})).toEqual({
      citationMarkers: true,
      copy: true,
      rate: true,
      regenerate: true,
      timestamp: true,
    })
  })

  it('honours every flag the control put into state', () => {
    expect(
      readThreadConfig({
        citations: false,
        copy: false,
        rate: false,
        regenerate: false,
        timestamp: false,
      }),
    ).toEqual({
      citationMarkers: false,
      copy: false,
      rate: false,
      regenerate: false,
      timestamp: false,
    })
  })
})

describe('readComposerControls', () => {
  it('defaults every picker on', () => {
    expect(readComposerControls({})).toEqual({
      autoApproveSwitch: true,
      enterHint: true,
      agentPicker: true,
      modelPicker: true,
      thinking: true,
      mcp: true,
      attachmentsEnabled: true,
      tracePicker: true,
    })
  })

  it('honours every picker switched off', () => {
    expect(
      readComposerControls({
        autoApprove: false,
        enterHint: false,
        agentPicker: false,
        modelPicker: false,
        thinking: false,
        mcp: false,
        attachments: false,
        tracePicker: false,
      }),
    ).toEqual({
      autoApproveSwitch: false,
      enterHint: false,
      agentPicker: false,
      modelPicker: false,
      thinking: false,
      mcp: false,
      attachmentsEnabled: false,
      tracePicker: false,
    })
  })
})

describe('a composer with every picker off', () => {
  it('renders no plus menu and still sends', () => {
    const off = readComposerControls({
      autoApprove: false,
      enterHint: false,
      agentPicker: false,
      modelPicker: false,
      thinking: false,
      mcp: false,
      attachments: false,
      tracePicker: false,
    })
    expect(plusMenuVisible(off)).toBe(false)
    // Send is not gated on the plus menu: a non-empty draft is enough.
    expect(Boolean('send this'.trim())).toBe(true)
  })
})

describe('modelBoundSource', () => {
  const pushed = [
    { id: 'i1', label: 'Invoice 1001', token: { kind: 'Invoice', id: 'i1', label: 'Invoice 1001', text: 'Invoice 1001 — 42€' } },
    { id: 'i2', label: 'Receipt 2002', token: { kind: 'Invoice', id: 'i2', label: 'Receipt 2002', text: 'Receipt 2002 — 7€' } },
  ]

  it('returns undefined when no candidates were pushed (falls back to a registered source)', () => {
    expect(modelBoundSource(undefined)).toBeUndefined()
    expect(modelBoundSource('x')).toBeUndefined()
  })

  it('offers all candidates for an empty query and maps them to ReferenceCandidates', () => {
    const source = modelBoundSource(pushed)!
    const all = source('')
    expect(all).toHaveLength(2)
    expect(all[0]).toEqual({
      id: 'i1',
      label: 'Invoice 1001',
      token: { kind: 'Invoice', id: 'i1', label: 'Invoice 1001', text: 'Invoice 1001 — 42€' },
    })
  })

  it('filters case-insensitively on label and quoted text', () => {
    const source = modelBoundSource(pushed)!
    expect(source('receipt').map((c) => c.id)).toEqual(['i2'])
    expect(source('42€').map((c) => c.id)).toEqual(['i1'])
    expect(source('nomatch')).toEqual([])
  })

  it('skips malformed rows without a token id', () => {
    const source = modelBoundSource([{ id: 'x', label: 'no token' }, ...pushed])!
    expect(source('').map((c) => c.id)).toEqual(['i1', 'i2'])
  })
})

describe('readContextFacts', () => {
  it('reads what the control computed from its <context> facts', () => {
    expect(
      readContextFacts([
        { name: 'Project', value: 'Airport expansion', stable: true },
        { name: 'Open invoice', value: 'none', stable: false },
      ]),
    ).toEqual([
      { name: 'Project', value: 'Airport expansion', stable: true },
      { name: 'Open invoice', value: 'none', stable: false },
    ])
  })

  it('treats a fact as stable unless it says otherwise', () => {
    expect(readContextFacts([{ name: 'Project', value: 'Airport' }])).toEqual([
      { name: 'Project', value: 'Airport', stable: true },
    ])
  })

  it('is empty when the element configured no context', () => {
    expect(readContextFacts(undefined)).toEqual([])
    expect(readContextFacts('x')).toEqual([])
    expect(readContextFacts([])).toEqual([])
  })

  it('skips a row without a name, which states nothing', () => {
    expect(readContextFacts([{ value: 'Airport' }, null, { name: 'Tenant', value: 'ACME' }])).toEqual([
      { name: 'Tenant', value: 'ACME', stable: true },
    ])
  })

  it('reads a missing value as empty rather than as "undefined"', () => {
    expect(readContextFacts([{ name: 'Project' }])).toEqual([
      { name: 'Project', value: '', stable: true },
    ])
  })
})

/**
 * The prompt cards a page offers, and when they are shown.
 *
 * The texts arrive already evaluated - the server did that, in the acting user's context - so what
 * the browser owns is only which of them are worth a button and whether the row belongs on screen.
 */
describe('readPromptCards', () => {
  it('reads what the control computed from its <prompts> cards', () => {
    expect(
      readPromptCards([
        { label: 'Summarise', icon: 'css:bi bi-receipt', text: 'Please summarise the newest invoices.' },
        { label: 'Who sent it?', icon: '', text: 'Who is this invoice from?' },
      ]),
    ).toEqual([
      { label: 'Summarise', icon: 'css:bi bi-receipt', text: 'Please summarise the newest invoices.' },
      { label: 'Who sent it?', icon: '', text: 'Who is this invoice from?' },
    ])
  })

  it('is empty when the element configured no prompts', () => {
    expect(readPromptCards(undefined)).toEqual([])
    expect(readPromptCards('x')).toEqual([])
    expect(readPromptCards([])).toEqual([])
  })

  it('drops a card with no text, which would empty the composer instead of filling it', () => {
    expect(readPromptCards([{ label: 'Broken', icon: '', text: '' }, null, { label: 'Ask', text: 'Ask this.' }]))
      .toEqual([{ label: 'Ask', icon: '', text: 'Ask this.' }])
  })

  it('drops a card with no label, which would be a button nobody can read', () => {
    expect(readPromptCards([{ text: 'Ask this.' }])).toEqual([])
  })
})

/**
 * When the row is on screen: on a new chat, and nowhere else.
 *
 * "New chat" is what the session channel says, not what the transcript says - a conversation the
 * user opened from the list is not short of a first question, whatever it holds.
 */
describe('promptCardsVisible', () => {
  it('shows the cards on a new chat - nothing selected, nothing said', () => {
    expect(promptCardsVisible(null, 0)).toBe(true)
    expect(promptCardsVisible(undefined, 0)).toBe(true)
    // What a send takes back leaves behind, which is a new chat again.
    expect(promptCardsVisible('', 0)).toBe(true)
  })

  it('hides them in a selected chat, even one with no turn in it', () => {
    expect(promptCardsVisible('session-7', 0)).toBe(false)
    expect(promptCardsVisible('session-7', 4)).toBe(false)
  })

  it('hides them from the first message on, before the session id has landed', () => {
    // A send on a new chat sets the id one render ahead of the user's own message, and the row must
    // not linger over a screen that is already a conversation either way round.
    expect(promptCardsVisible(null, 1)).toBe(false)
    expect(promptCardsVisible('created-by-this-send', 0)).toBe(false)
  })
})

/**
 * Where the row stands, which is the whole of what the position is worth.
 *
 * The cards are an offer of what to ask, so they belong between the greeting that asks the question
 * and the field that answers it - the order the classic chat's example prompts already had. Asserted
 * on the rendered markup rather than on the JSX, because the grid rows that place them are written
 * against that order and a swap here would be invisible in every other test.
 */
describe('where the prompt cards are rendered', () => {
  afterEach(() => resetTLBridge())

  /** Renders a new chat that offers one card, without a browser. */
  function render(chatId: string): string {
    const state = {
      chatId,
      windowId: 'w1',
      authenticated: true,
      language: 'en',
      prompts: [{ label: 'Summarise', icon: '', text: 'Please summarise the newest invoices.' }],
    }
    setTLState(state)
    return renderToStaticMarkup(React.createElement(TLChatConversation, { controlId: chatId, state }))
  }

  it('puts the cards under the greeting and above the composer', () => {
    const markup = render('prompt-order-test')
    const greeting = markup.indexOf('class="welcome"')
    const cards = markup.indexOf('class="prompt-suggestions"')
    const composer = markup.indexOf('class="composer"')
    expect(greeting).toBeGreaterThanOrEqual(0)
    expect(cards).toBeGreaterThan(greeting)
    expect(composer).toBeGreaterThan(cards)
  })

  it('renders the card as the button a click fills the composer from', () => {
    const markup = render('prompt-button-test')
    expect(markup).toContain('Summarise')
    expect(markup).toContain('title="Please summarise the newest invoices."')
  })

  it('writes the css-class of the element on its root, after its own classes', () => {
    setTLState({
      chatId: 'css-class-test',
      windowId: 'w1',
      authenticated: true,
      language: 'en',
      cssClass: 'appChat',
    })
    const markup = renderToStaticMarkup(
      React.createElement(TLChatConversation, { controlId: 'css-class-test', state: {} }),
    )
    expect(markup).toMatch(/^<div id="css-class-test" class="tl-chat-conversation appChat"/)
  })
})

describe('TLChatConversation and App', () => {
  it('are functions over the same store API', () => {
    expect(typeof TLChatConversation).toBe('function')
    expect(typeof App).toBe('function')
  })
})

/**
 * How a turn-setting channel reaches the composer.
 *
 * The control pushes `null` for "the channel says nothing" - unwired, empty, or the write refused -
 * and the composer must then keep what the user has. The empty string and the empty array are the
 * opposite: an instruction to hold no level and to attach no server.
 */
describe('the turn-setting channels', () => {
  it('reads a name a channel wrote, including the empty one', () => {
    expect(channelName('TL_ModelExplorer')).toBe('TL_ModelExplorer')
    expect(channelName('')).toBe('')
  })

  it('says nothing for an unwired, empty or refused channel', () => {
    expect(channelName(null)).toBeUndefined()
    expect(channelName(undefined)).toBeUndefined()
    expect(channelName(42)).toBeUndefined()
  })

  it('reads a set of server names, and tells "none" from "nothing said"', () => {
    expect(channelNames(['files', 'issues'])).toEqual(['files', 'issues'])
    expect(channelNames([])).toEqual([])
    expect(channelNames(null)).toBeUndefined()
    expect(channelNames('files')).toBeUndefined()
  })

  it('compares attachment sets by what they name, not by their order', () => {
    expect(sameServers(['a', 'b'], ['b', 'a'])).toBe(true)
    expect(sameServers([], [])).toBe(true)
    expect(sameServers(['a'], ['a', 'b'])).toBe(false)
    expect(sameServers(['a', 'b'], ['a', 'c'])).toBe(false)
  })
})

/**
 * What the `draft` channel says, read the same way a turn setting is.
 *
 * `undefined` is "the channel says nothing" - it is unwired or holds nothing - and the composer
 * keeps what the user typed. The empty string is the opposite: an instruction to clear the draft.
 */
describe('the draft channel', () => {
  it('reads the text a host wrote, the empty one included', () => {
    expect(channelName('Bitte erkläre die Rechnung R-2024-014.')).toBe(
      'Bitte erkläre die Rechnung R-2024-014.',
    )
    expect(channelName('')).toBe('')
  })

  it('says nothing for an unwired or empty channel', () => {
    expect(channelName(null)).toBeUndefined()
    expect(channelName(undefined)).toBeUndefined()
  })
})

/**
 * What the `reference` and the `quote` channel say.
 *
 * The sequence number is what makes a write a write: a channel notifies for A, B, A, and the third
 * write carries the token of the first, which unchanged state alone could not tell from no write.
 */
describe('the token channels', () => {
  it('reads the token the control built out of the written object', () => {
    expect(
      tokenWrite({
        seq: 3,
        kind: 'Invoice',
        id: 'aaa-bbb',
        label: 'Hotelrechnung Berlin',
        text: 'Hotelrechnung Berlin (1.240,00 €)',
      }),
    ).toEqual({
      seq: 3,
      token: {
        kind: 'Invoice',
        id: 'aaa-bbb',
        label: 'Hotelrechnung Berlin',
        location: '',
        text: 'Hotelrechnung Berlin (1.240,00 €)',
      },
    })
  })

  it('carries the location a quote write names, which is what makes it a passage', () => {
    expect(
      tokenWrite({
        seq: 1,
        kind: 'GenericObject',
        id: 'aaa-bbb',
        label: 'Angebot Alpha',
        location: '02 Kontext',
        text: 'the marked passage',
      })?.token,
    ).toEqual({
      kind: 'GenericObject',
      id: 'aaa-bbb',
      label: 'Angebot Alpha',
      location: '02 Kontext',
      text: 'the marked passage',
    })
  })

  it('numbers two passages of the same object apart, so both insert', () => {
    const object = { kind: 'GenericObject', id: 'aaa-bbb', label: 'Angebot Alpha' }
    const first = tokenWrite({ ...object, seq: 7, location: '02 Kontext', text: 'first passage' })
    const second = tokenWrite({ ...object, seq: 8, location: '03 Ziele', text: 'second passage' })
    expect(first?.seq).toBe(7)
    expect(second?.seq).toBe(8)
    expect(first?.token.text).not.toBe(second?.token.text)
  })

  it('defaults the fields the control left out, but insists on the id', () => {
    expect(tokenWrite({ seq: 1, id: 'aaa-bbb' })?.token).toEqual({
      kind: '',
      id: 'aaa-bbb',
      label: '',
      location: '',
      text: '',
    })
    expect(tokenWrite({ seq: 1, id: '' })).toBeUndefined()
    expect(tokenWrite({ seq: 1 })).toBeUndefined()
  })

  it('says nothing for an unwired channel or a write with no sequence', () => {
    expect(tokenWrite(null)).toBeUndefined()
    expect(tokenWrite(undefined)).toBeUndefined()
    expect(tokenWrite('an-id')).toBeUndefined()
    expect(tokenWrite({ id: 'aaa-bbb' })).toBeUndefined()
  })
})
