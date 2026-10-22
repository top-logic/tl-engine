// @vitest-environment jsdom
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { flushSync } from 'react-dom'
import { createRoot, type Root } from 'react-dom/client'
import { TokenInput, type TokenInputHandle } from './token-input'
import type { ReferenceToken } from './reference'

/**
 * One Backspace takes a whole reference token (#528) - also when the caret arrived by a click at the
 * end of the draft, where what stands before it is the space the insertion put behind the token
 * (#552). The editor is a live `contenteditable`, so the run needs a DOM: jsdom.
 */

function act(work: () => void): void {
  flushSync(work)
}

const token = (id: string): ReferenceToken => ({
  kind: 'invoice',
  id,
  label: `Invoice ${id}`,
  text: `Passage ${id}`,
})

let host: HTMLDivElement
let root: Root
let handle: TokenInputHandle | null = null
let value = ''

function editor(): HTMLElement {
  return host.querySelector<HTMLElement>('[contenteditable="true"]')!
}

function tokens(): number {
  return editor().querySelectorAll('[data-ref-token]').length
}

/** Where a click at the far end of the field puts the caret: the field itself, after its last child. */
function clickAtEnd(): void {
  const range = document.createRange()
  range.setStart(editor(), editor().childNodes.length)
  range.collapse(true)
  const selection = window.getSelection()!
  selection.removeAllRanges()
  selection.addRange(range)
}

/** A Backspace as the browser delivers it; `false` when the editor left it to the browser. */
function backspace(): boolean {
  const event = new KeyboardEvent('keydown', { key: 'Backspace', bubbles: true, cancelable: true })
  act(() => {
    editor().dispatchEvent(event)
  })
  return event.defaultPrevented
}

function render(): void {
  act(() =>
    root.render(
      React.createElement(TokenInput, {
        ref: (next: TokenInputHandle | null) => {
          handle = next
        },
        value,
        onChange: (next: string) => {
          value = next
        },
        placeholder: 'Ask',
        ariaLabel: 'Message',
      }),
    ),
  )
}

beforeEach(() => {
  value = ''
  host = document.createElement('div')
  document.body.append(host)
  root = createRoot(host)
  render()
  act(() => {
    handle!.insertReference(token('1'))
    handle!.insertReference(token('2'))
  })
})

afterEach(() => {
  act(() => root.unmount())
  host.remove()
})

describe('Backspace behind a token', () => {
  it('takes the last token in one press after a click at the end of the draft', () => {
    expect(tokens()).toBe(2)
    clickAtEnd()
    expect(backspace()).toBe(true)
    expect(tokens()).toBe(1)
    expect(value).not.toContain('Passage 2')
    expect(value).toContain('Passage 1')
  })

  it('takes the next token with the next press', () => {
    clickAtEnd()
    backspace()
    expect(backspace()).toBe(true)
    expect(tokens()).toBe(0)
  })

  it('deletes a space the user typed as a space', () => {
    const typed = document.createTextNode(' ')
    editor().append(typed)
    const range = document.createRange()
    range.setStart(typed, 1)
    range.collapse(true)
    window.getSelection()!.removeAllRanges()
    window.getSelection()!.addRange(range)
    expect(backspace()).toBe(false)
    expect(tokens()).toBe(2)
  })

  it('deletes a character once the user wrote behind the inserted space', () => {
    const spaces = [...editor().childNodes].filter((node) => node.nodeType === Node.TEXT_NODE)
    const last = spaces[spaces.length - 1] as Text
    last.textContent = ' word'
    const range = document.createRange()
    range.setStart(last, last.length)
    range.collapse(true)
    window.getSelection()!.removeAllRanges()
    window.getSelection()!.addRange(range)
    expect(backspace()).toBe(false)
    expect(tokens()).toBe(2)
  })
})
