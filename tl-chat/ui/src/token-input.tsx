import { React } from 'tl-react-bridge'
import { parseDraft, referenceSummary, serializeReference, type ReferenceToken } from './reference'

const { useCallback, useEffect, useImperativeHandle, useRef } = React

/**
 * Zero-width space kept at the very end of the editor.
 *
 * Without it the caret cannot be placed after a token that ends the draft, and a trailing newline
 * is invisible. It is stripped when the draft is read, so nobody ever sends one.
 */
const TAIL = '​'

/** What a host or the composer itself can do to the editor from the outside. */
export interface TokenInputHandle {
  focus: () => void
  /** Inserts one atomic reference at the caret, or at the end when the editor is not focused. */
  insertReference: (token: ReferenceToken) => void
  /** Replaces the text between `from` and the caret, e.g. the "@query" that opened a picker. */
  replaceBefore: (length: number, text: string) => void
  /** The word being typed directly before the caret, for the "@" picker. */
  wordBeforeCaret: () => string
}

/**
 * The composer's text field: plain words with atomic inline reference tokens between them.
 *
 * A `contenteditable` rather than a `textarea`, because a reference belongs *in* the sentence -
 * "shorten [this] and expand [that]" needs two of them in two places, which a chip above the field
 * cannot express. Each token is one `contenteditable=false` element: the caret steps over it,
 * Backspace deletes it whole, and a screen reader reads it as one named thing.
 *
 * The DOM is the value here. React renders the element once and writes into it only when the
 * incoming `value` differs from what the editor already holds - re-rendering the content on every
 * keystroke would move the caret to the start on every letter.
 */
export const TokenInput = React.forwardRef(function TokenInput(
  {
    value,
    onChange,
    onKeyDown,
    onPasteFiles,
    placeholder,
    ariaLabel,
    className,
    disabled,
  }: {
    value: string
    onChange: (next: string) => void
    onKeyDown?: (event: React.KeyboardEvent<HTMLDivElement>) => void
    /** Files pasted into the editor; the text of a paste is always inserted as plain text. */
    onPasteFiles?: (files: File[]) => void
    placeholder: string
    ariaLabel: string
    className?: string
    disabled?: boolean
  },
  ref: React.Ref<TokenInputHandle>,
) {
  const host = useRef<HTMLDivElement>(null)
  /**
   * Where the caret was when the editor last had it.
   *
   * A host inserts a reference from its own pane, so by then the editor has lost the selection, and
   * focusing a contenteditable that has none puts the caret at offset 0 - which is why the marked
   * passage used to land in front of everything the user had typed. The last place the user really
   * stood is remembered here and restored before the insert.
   */
  const lastRange = useRef<Range | null>(null)

  /**
   * Puts the caret back when the editor is about to take input with no selection at all.
   *
   * A host may clear the selection after inserting - its own pane cleans up after the gesture - and
   * Chromium then starts the next keystroke at offset 0. The editor owns its caret: whatever a host
   * does outside it, the next character lands where the user last stood.
   */
  const ensureCaret = useCallback(() => {
    const el = host.current
    const selection = window.getSelection()
    if (!el || !selection) return
    if (selection.rangeCount > 0 && el.contains(selection.getRangeAt(0).startContainer)) return
    if (!restore(el, lastRange.current)) atEnd(el)
  }, [])

  const rememberCaret = useCallback(() => {
    const el = host.current
    const selection = window.getSelection()
    if (!el || !selection || selection.rangeCount === 0) return
    const range = selection.getRangeAt(0)
    if (el.contains(range.startContainer)) lastRange.current = range.cloneRange()
  }, [])

  const read = useCallback((): string => {
    const el = host.current
    return el ? readDraft(el) : ''
  }, [])

  const emit = useCallback(() => {
    onChange(read())
  }, [onChange, read])

  // Written into the DOM only when the two have actually drifted apart - on a send that cleared the
  // draft, on an improved prompt, on a restored undo. Never on the keystroke that caused it.
  useEffect(() => {
    const el = host.current
    if (!el) return
    if (readDraft(el) === value) return
    writeDraft(el, value)
  }, [value])

  useImperativeHandle(
    ref,
    () => ({
      focus: () => host.current?.focus(),
      insertReference: (token: ReferenceToken) => {
        const el = host.current
        if (!el) return
        const remembered = restore(el, lastRange.current)
        el.focus()
        // With nothing remembered the documented fallback applies: the end of the draft, never its
        // start.
        const range = remembered ? caretIn(el) : atEnd(el)
        const fragment = document.createDocumentFragment()
        fragment.append(tokenElement(token))
        // A space after the token, so the next word is not glued to it.
        fragment.append(document.createTextNode(' '))
        range.deleteContents()
        range.insertNode(fragment)
        placeCaretAfterLast(el, range)
        ensureTail(el)
        rememberCaret()
        emit()
      },
      replaceBefore: (length: number, text: string) => {
        const el = host.current
        if (!el) return
        el.focus()
        const range = caretIn(el)
        const node = range.startContainer
        if (node.nodeType === Node.TEXT_NODE) {
          const start = Math.max(0, range.startOffset - length)
          range.setStart(node, start)
          range.deleteContents()
          if (text) {
            const inserted = document.createTextNode(text)
            range.insertNode(inserted)
            range.setStartAfter(inserted)
          }
          range.collapse(true)
          select(range)
        }
        emit()
      },
      wordBeforeCaret: () => {
        const el = host.current
        if (!el) return ''
        const selection = window.getSelection()
        if (!selection || selection.rangeCount === 0) return ''
        const range = selection.getRangeAt(0)
        if (!el.contains(range.startContainer)) return ''
        if (range.startContainer.nodeType !== Node.TEXT_NODE) return ''
        const before = (range.startContainer.textContent ?? '').slice(0, range.startOffset)
        const match = /(^|\s)(@[^\s@]*)$/.exec(before.replace(new RegExp(TAIL, 'g'), ''))
        return match ? match[2] : ''
      },
    }),
    [emit],
  )

  const onKey = useCallback(
    (event: React.KeyboardEvent<HTMLDivElement>) => {
      const el = host.current
      if (el && (event.key === 'Backspace' || event.key === 'Delete')) {
        // Explicit rather than left to the browser: a token comes out in one press, whole, in every
        // engine, and the draft the composer holds is updated in the same tick.
        const token = tokenAtCaret(el, event.key === 'Backspace' ? 'before' : 'after')
        if (token) {
          event.preventDefault()
          token.remove()
          ensureTail(el)
          emit()
          return
        }
      }
      if (el && event.key === 'Enter' && event.shiftKey) {
        // The browser would insert a <div> or a <br> depending on the engine; a newline text node
        // is what `white-space: pre-wrap` renders and what the draft reads back as.
        event.preventDefault()
        insertText(el, '\n')
        emit()
        return
      }
      onKeyDown?.(event)
    },
    [emit, onKeyDown],
  )

  const onPaste = useCallback(
    (event: React.ClipboardEvent<HTMLDivElement>) => {
      const el = host.current
      if (!el) return
      const files = Array.from(event.clipboardData?.files ?? [])
      if (files.length) {
        event.preventDefault()
        onPasteFiles?.(files)
        return
      }
      // Always plain text: a paste from a web page must not bring markup into a field whose only
      // elements are the tokens this component owns.
      event.preventDefault()
      insertText(el, event.clipboardData?.getData('text/plain') ?? '')
      emit()
    },
    [emit, onPasteFiles],
  )

  const empty = value.trim() === ''

  return (
    <div
      ref={host}
      className={`token-input${empty ? ' is-empty' : ''}${className ? ` ${className}` : ''}`}
      contentEditable={!disabled}
      suppressContentEditableWarning
      role="textbox"
      aria-multiline="true"
      aria-label={ariaLabel}
      data-placeholder={placeholder}
      spellCheck
      onBeforeInput={ensureCaret}
      onFocus={ensureCaret}
      onInput={() => {
        rememberCaret()
        emit()
      }}
      onKeyUp={rememberCaret}
      onMouseUp={rememberCaret}
      onBlur={rememberCaret}
      onKeyDown={(event) => {
        ensureCaret()
        onKey(event)
      }}
      onPaste={onPaste}
    />
  )
})

/** The draft the editor currently holds, tokens serialised back into `<quote/>` elements. */
function readDraft(el: HTMLElement): string {
  let out = ''
  const walk = (node: Node) => {
    if (node.nodeType === Node.TEXT_NODE) {
      out += (node.textContent ?? '').split(TAIL).join('')
      return
    }
    if (!(node instanceof HTMLElement)) return
    if (node.dataset.refToken != null) {
      out += serializeReference(tokenOf(node))
      return
    }
    if (node.tagName === 'BR') {
      out += '\n'
      return
    }
    // A <div> or <p> the engine produced for a line break still reads as one.
    const block = node.tagName === 'DIV' || node.tagName === 'P'
    if (block && out && !out.endsWith('\n')) out += '\n'
    node.childNodes.forEach(walk)
  }
  el.childNodes.forEach(walk)
  return out
}

/** Replaces the editor's content with `value`, tokens rebuilt from their serialised form. */
function writeDraft(el: HTMLElement, value: string) {
  el.replaceChildren()
  for (const part of parseDraft(value)) {
    if (part.type === 'text') {
      if (part.text) el.append(document.createTextNode(part.text))
    } else {
      el.append(tokenElement(part.token))
    }
  }
  ensureTail(el)
}

/** One atomic reference: an element the caret steps over and a screen reader reads as one name. */
function tokenElement(token: ReferenceToken): HTMLElement {
  const el = document.createElement('span')
  el.className = 'ref-token'
  el.contentEditable = 'false'
  el.dataset.refToken = '1'
  el.dataset.kind = token.kind ?? ''
  el.dataset.id = token.id ?? ''
  el.dataset.label = token.label ?? ''
  el.dataset.location = token.location ?? ''
  el.dataset.text = token.text ?? ''
  el.setAttribute('role', 'img')
  el.setAttribute('aria-label', referenceSummary(token))
  el.title = token.text || referenceSummary(token)

  const icon = document.createElementNS('http://www.w3.org/2000/svg', 'svg')
  icon.setAttribute('viewBox', '0 0 16 16')
  icon.setAttribute('aria-hidden', 'true')
  icon.setAttribute('class', 'ref-token-icon')
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path')
  path.setAttribute('d', 'M4 2.5h5.2L12 5.3v8.2H4zM9 2.6v3h3M6 8.4h4M6 10.8h4')
  icon.append(path)
  el.append(icon)

  const label = document.createElement('span')
  label.className = 'ref-token-label'
  label.textContent = token.label
  el.append(label)

  if (token.location?.trim()) {
    const location = document.createElement('span')
    location.className = 'ref-token-location'
    location.textContent = token.location.trim()
    el.append(location)
  }
  return el
}

function tokenOf(el: HTMLElement): ReferenceToken {
  return {
    kind: el.dataset.kind ?? '',
    id: el.dataset.id ?? '',
    label: el.dataset.label ?? '',
    location: el.dataset.location ?? '',
    text: el.dataset.text ?? '',
  }
}

/** Keeps a text node at the end, so the caret can stand behind a token that closes the draft. */
function ensureTail(el: HTMLElement) {
  const last = el.lastChild
  if (!last || last.nodeType !== Node.TEXT_NODE) {
    el.append(document.createTextNode(TAIL))
  }
}

/** Puts a remembered range back, if it still points into the editor. */
function restore(el: HTMLElement, range: Range | null): boolean {
  if (!range || !el.contains(range.startContainer) || !el.isConnected) return false
  const selection = window.getSelection()
  if (!selection) return false
  selection.removeAllRanges()
  selection.addRange(range)
  return true
}

function atEnd(el: HTMLElement): Range {
  const range = document.createRange()
  range.selectNodeContents(el)
  range.collapse(false)
  select(range)
  return range
}

function caretIn(el: HTMLElement): Range {
  const selection = window.getSelection()
  if (selection && selection.rangeCount > 0) {
    const range = selection.getRangeAt(0)
    if (el.contains(range.startContainer)) return range
  }
  const range = document.createRange()
  range.selectNodeContents(el)
  range.collapse(false)
  return range
}

function select(range: Range) {
  const selection = window.getSelection()
  selection?.removeAllRanges()
  selection?.addRange(range)
}

function placeCaretAfterLast(el: HTMLElement, range: Range) {
  range.collapse(false)
  select(range)
  el.normalize()
}

function insertText(el: HTMLElement, text: string) {
  if (!text) return
  const range = caretIn(el)
  range.deleteContents()
  const node = document.createTextNode(text)
  range.insertNode(node)
  range.setStartAfter(node)
  range.collapse(true)
  select(range)
  ensureTail(el)
}

/** The token immediately before or after a collapsed caret, or `null`. */
function tokenAtCaret(el: HTMLElement, side: 'before' | 'after'): HTMLElement | null {
  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0 || !selection.isCollapsed) return null
  const range = selection.getRangeAt(0)
  if (!el.contains(range.startContainer)) return null
  const node = range.startContainer
  const offset = range.startOffset

  if (node.nodeType === Node.TEXT_NODE) {
    const text = (node.textContent ?? '')
    if (side === 'before') {
      // Only when there is nothing left of the caret but the invisible tail.
      if (text.slice(0, offset).split(TAIL).join('') !== '') return null
      return asToken(previousNode(node, el))
    }
    if (text.slice(offset).split(TAIL).join('') !== '') return null
    return asToken(node.nextSibling)
  }
  const children = Array.from(node.childNodes)
  return asToken(side === 'before' ? children[offset - 1] : children[offset])
}

function asToken(node: Node | null | undefined): HTMLElement | null {
  return node instanceof HTMLElement && node.dataset.refToken != null ? node : null
}

function previousNode(node: Node, root: HTMLElement): Node | null {
  let current: Node | null = node
  while (current && current !== root) {
    if (current.previousSibling) return current.previousSibling
    current = current.parentNode
  }
  return null
}
