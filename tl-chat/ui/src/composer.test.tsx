// @vitest-environment jsdom
import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { Composer } from './composer'
import { bundle } from './i18n'
import type { ReferenceCandidate } from './extensions'

/**
 * The composer under input that arrives faster than React commits (#419).
 *
 * A browser delivers each keystroke in its own task and React commits the state change in the
 * microtask that follows, so a burst of keystrokes is a burst of commits with nothing but
 * microtasks in between. React counts a `setState` that runs *after* such a commit - from a passive
 * effect, say - as a nested update, and fifty in a row without an idle frame is
 * "maximum update depth exceeded" (the minified error #185 the ticket reports). The composer must
 * therefore leave no follow-up state update behind per keystroke, whatever it is configured with.
 *
 * This is the one suite in the module that needs a live DOM: the fault only exists between a real
 * commit and the passive effects that follow it, which `react-dom/server` never performs. Hence
 * `jsdom` - a dev dependency of this file alone, declared by the docblock above.
 */

const LONG = 'Schreibe bitte den Abschnitt "Beschreibung des Konzepts" für das Angebot an die Stadt.'

/** The zero-width space the editor keeps at its end; a typed character goes in front of it. */
const TAIL = '\u200b'

function baseProps(extra: Record<string, unknown> = {}) {
  return {
    windowId: 'w1',
    agents: [],
    models: [],
    agent: 'assistant',
    model: 'claude-opus-5',
    onAgent: () => {},
    onModel: () => {},
    mcpServers: [],
    selectedMcp: [],
    onToggleMcp: () => {},
    traceMode: 'none',
    onTraceMode: () => {},
    effort: '',
    effortLevels: [],
    onEffort: () => {},
    labels: bundle('en'),
    autoApprove: false,
    onToggleAutoApprove: () => {},
    pendingApprovals: [],
    pendingQuestions: [],
    dispatch: () => {},
    promptImprovementEnabled: false,
    subagents: [],
    onCancelSubagent: async () => {},
    onStopSubagents: async () => {},
    onLoadSubagentTranscript: async () => [],
    busy: false,
    connection: 'open',
    notice: '',
    noticeDetail: '',
    noticeTone: 'info',
    onDismissNotice: () => {},
    onSend: async () => true,
    onStop: () => {},
    ...extra,
  }
}

let mounted: { root: Root; container: HTMLElement; off: () => void } | null = null

/** Renders a composer into the document and collects every error the page would have reported. */
async function mount(extra: Record<string, unknown> = {}) {
  const container = document.createElement('div')
  document.body.append(container)
  const root = createRoot(container)
  const errors: string[] = []
  const onError = (event: ErrorEvent) => errors.push(String(event.error ?? event.message))
  window.addEventListener('error', onError)
  mounted = { root, container, off: () => window.removeEventListener('error', onError) }

  root.render(React.createElement(Composer, baseProps(extra) as never))
  await settle()

  const field = container.querySelector('.token-input') as HTMLElement
  expect(field).toBeTruthy()
  return { field, errors, draft: () => (field.textContent ?? '').split(TAIL).join('') }
}

afterEach(() => {
  mounted?.off()
  mounted?.root.unmount()
  mounted?.container.remove()
  mounted = null
})

/** A full turn of the event loop, the way a pause between two keystrokes gives React one. */
function settle(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 20))
}

/** What the browser does for one keystroke: the character lands at the caret, then `input` fires. */
function press(field: HTMLElement, character: string) {
  insertAtCaret(field, character)
  field.dispatchEvent(new window.InputEvent('input', { bubbles: true, data: character }))
}

/** Writes `text` at the end of the field and leaves the caret behind it, as an engine would. */
function insertAtCaret(field: HTMLElement, text: string) {
  const last = field.lastChild
  if (last && last.nodeType === Node.TEXT_NODE) {
    const content = last.textContent ?? ''
    const tail = content.endsWith(TAIL)
    const head = tail ? content.slice(0, -1) : content
    last.textContent = head + text + (tail ? TAIL : '')
    caretAt(last, head.length + text.length)
    return
  }
  const node = document.createTextNode(text)
  field.append(node)
  caretAt(node, text.length)
}

function caretAt(node: Node, offset: number) {
  const range = document.createRange()
  range.setStart(node, offset)
  range.collapse(true)
  const selection = window.getSelection()
  selection?.removeAllRanges()
  selection?.addRange(range)
}

/**
 * Types `text` with nothing but a microtask between the characters.
 *
 * That is the gap a driver typing at zero delay leaves, and the one the ticket measured: React
 * commits each keystroke, but the follow-up work a commit schedules - which runs on a macrotask -
 * never gets its turn.
 */
async function burst(field: HTMLElement, text: string) {
  for (const character of text) {
    press(field, character)
    await Promise.resolve()
    await Promise.resolve()
  }
}

describe('the composer under a burst of input', () => {
  it('survives a sentence typed with no frame between the keystrokes', async () => {
    const { field, errors, draft } = await mount()

    await burst(field, LONG)
    await settle()

    expect(errors).toEqual([])
    expect(draft()).toBe(LONG)
  })

  it('survives a burst far past the nested-update limit React allows', async () => {
    const { field, errors, draft } = await mount()

    // Well over the fifty React allows: the ticket saw one error per fifty characters.
    const text = LONG.repeat(4)
    await burst(field, text)
    await settle()

    expect(errors).toEqual([])
    expect(draft()).toBe(text)
  })

  it('survives the burst with the prompt improver armed', async () => {
    // The improver's pause is what used to write state on every keystroke.
    const { field, errors, draft } = await mount({ promptImprovementEnabled: true })

    await burst(field, LONG)
    await settle()

    expect(errors).toEqual([])
    expect(draft()).toBe(LONG)
  })

  it('survives the burst while a host follows the draft', async () => {
    const reported: string[] = []
    const { field, errors } = await mount({
      onDraft: (text: string) => reported.push(text),
    })

    await burst(field, LONG)
    await settle()

    expect(errors).toEqual([])
    // The host heard the sentence, not fifty fragments that ended mid-word.
    expect(reported[reported.length - 1]).toBe(LONG)
  })

  it('survives a paste of two kilobytes followed by more typing', async () => {
    const { field, errors, draft } = await mount()
    const pasted = 'Absatz. '.repeat(256)

    const paste = new window.Event('paste', { bubbles: true, cancelable: true }) as Event & {
      clipboardData: { files: File[]; getData: () => string }
    }
    paste.clipboardData = { files: [], getData: () => pasted }
    // The caret has to stand in the field for the paste to have somewhere to go.
    insertAtCaret(field, '')
    field.dispatchEvent(paste)
    await Promise.resolve()

    await burst(field, LONG)
    await settle()

    expect(errors).toEqual([])
    expect(draft()).toBe(pasted + LONG)
  })

  it('survives a composition committed character by character', async () => {
    const { field, errors, draft } = await mount()
    // Longer than the fifty nested updates React allows, so a regression shows here too.
    const composed = 'これは日本語の文章です。'.repeat(6)

    field.dispatchEvent(new window.Event('compositionstart', { bubbles: true }))
    for (const character of composed) {
      press(field, character)
      field.dispatchEvent(new window.Event('compositionupdate', { bubbles: true }))
      await Promise.resolve()
      await Promise.resolve()
    }
    field.dispatchEvent(new window.Event('compositionend', { bubbles: true }))
    await settle()

    expect(errors).toEqual([])
    expect(draft()).toBe(composed)
  })

  it('offers the improve chip after the pause and withdraws it on the next keystroke', async () => {
    // The chip is what the removed per-keystroke `setState` used to drive; its timing is the
    // behaviour that had to survive the fix.
    const { field, errors } = await mount({ promptImprovementEnabled: true })
    const chip = () =>
      Array.from(document.querySelectorAll('.overlay-chip')).find(
        (element) => element.textContent?.includes(bundle('en').improvePrompt),
      )

    await burst(field, 'Fasse den Bericht zusammen.')
    expect(chip()).toBeUndefined()

    await new Promise((resolve) => setTimeout(resolve, 900))
    expect(chip()).toBeTruthy()

    await burst(field, ' Kurz.')
    expect(chip()).toBeUndefined()
    expect(errors).toEqual([])
  })

  it('survives an "@" mention opened and taken in the middle of a burst', async () => {
    const candidate: ReferenceCandidate = {
      id: 'doc-1',
      label: 'Konzept.docx',
      token: { kind: 'document', id: 'doc-1', label: 'Konzept.docx', location: '', text: '' },
    }
    const { field, errors } = await mount({
      referenceCandidates: (query: string) =>
        'konzept.docx'.includes(query.toLocaleLowerCase()) ? [candidate] : [],
    })

    await burst(field, 'Bitte ')
    await burst(field, '@Konz')
    await settle()

    const option = document.querySelector('.mention-popover button') as HTMLButtonElement
    expect(option).toBeTruthy()
    // The row takes the candidate on mousedown, so the editor keeps its caret.
    option.dispatchEvent(new window.MouseEvent('mousedown', { bubbles: true, cancelable: true }))
    await settle()

    await burst(field, ' kürzen und den Rest der Seite so lassen, wie er im Entwurf steht, bitte.')
    await settle()

    expect(errors).toEqual([])
    expect(field.querySelectorAll('.ref-token').length).toBe(1)
  })
})

/**
 * The host's own invitation for the empty field (#420).
 *
 * The framework's German says "Frag mich etwas …", which an application whose every other surface
 * addresses the user with "Sie" cannot have on its most-read control. A host states its own
 * sentence, and it replaces both framework variants - including the one that offers "@". What it
 * does *not* replace is the wording while a run is going: that sentence is about the run.
 */
describe('a host placeholder on the composer', () => {
  const HOST = 'Fragen Sie nach diesem Projekt, seinen Dokumenten oder den Beteiligten.'

  /** What the stylesheet renders as the empty field's text (`.token-input.is-empty::before`). */
  function shown(field: HTMLElement) {
    return field.getAttribute('data-placeholder')
  }

  it('stands in the idle field in place of the framework wording', async () => {
    const { field } = await mount({ placeholder: HOST })

    expect(shown(field)).toBe(HOST)
    expect(field.getAttribute('aria-label')).toBe(HOST)
  })

  it('replaces the "@" variant as well', async () => {
    const { field } = await mount({
      placeholder: HOST,
      referenceCandidates: () => [],
    })

    expect(shown(field)).toBe(HOST)
    expect(shown(field)).not.toBe(bundle('en').askAnythingWithAt)
  })

  it('gives way to the framework wording while the agent works', async () => {
    const { field } = await mount({ placeholder: HOST, busy: true })

    expect(shown(field)).toBe(bundle('en').typeNext)
  })

  it('leaves the framework wording alone when the host states none', async () => {
    const { field } = await mount()

    expect(shown(field)).toBe(bundle('en').askAnything)
  })
})

/**
 * What a row of the "+" menu tells the person reading it (#635).
 *
 * A row's hint used to end in an ellipsis wherever it did not fit beside the row's value, and there
 * was nothing - no tooltip, no description - to read the rest in. The menu's real texts are short
 * enough today, so these rows carry an agent summary of the length an installation may author: the
 * whole sentence must be in the row, and it must be the row's accessible description, apart from
 * its name, which is the label and the value.
 */
describe('a row of the "+" menu', () => {
  const SUMMARY =
    'Prüft eingehende Rechnungen gegen die Bestellungen des Projekts, markiert Abweichungen bei Menge, ' +
    'Preis und Lieferdatum und schlägt für jede Abweichung eine Rückfrage an den Lieferanten vor.'

  const agents = [
    { name: 'assistant', label: 'Albert' },
    { name: 'invoices', label: 'Rechnungsprüfung', summary: SUMMARY },
  ]

  function click(element: Element) {
    element.dispatchEvent(new window.MouseEvent('click', { bubbles: true }))
  }

  /** The text the ids of an `aria-labelledby` / `aria-describedby` point at, as a screen reader joins it. */
  function referenced(row: Element, attribute: string): string {
    return (row.getAttribute(attribute) ?? '')
      .split(' ')
      .filter(Boolean)
      .map((id) => document.getElementById(id)?.textContent ?? '')
      .join(' ')
  }

  function rows(): HTMLElement[] {
    return Array.from(mounted!.container.querySelectorAll<HTMLElement>('.plus-popover .plus-row'))
  }

  async function openMenu() {
    click(mounted!.container.querySelector('.plus-trigger')!)
    await settle()
  }

  it('describes an agent by its whole summary, however long it is', async () => {
    await mount({ agents, labels: bundle('de') })
    await openMenu()
    click(rows().find((row) => referenced(row, 'aria-labelledby').startsWith('Agent'))!)
    await settle()

    const row = rows().find((item) => referenced(item, 'aria-labelledby') === 'Rechnungsprüfung')!
    expect(row).toBeTruthy()
    expect(referenced(row, 'aria-describedby')).toBe(SUMMARY)
    expect(row.textContent).toContain(SUMMARY)
  })

  it('names a setting by its label and value and describes it by its hint', async () => {
    const de = bundle('de')
    await mount({ agents, labels: de, traceMode: 'summary' })
    await openMenu()

    const trace = rows().find((row) => referenced(row, 'aria-labelledby').startsWith(de.traceLevel))!
    expect(referenced(trace, 'aria-labelledby')).toBe(`${de.traceLevel} ${de.traceSummary}`)
    expect(referenced(trace, 'aria-describedby')).toBe(de.traceHint)
  })

  it('gives a row without a hint no description', async () => {
    await mount({ agents })
    await openMenu()
    click(rows().find((row) => referenced(row, 'aria-labelledby').startsWith('Agent'))!)
    await settle()

    const albert = rows().find((item) => referenced(item, 'aria-labelledby') === 'Albert')!
    expect(albert.hasAttribute('aria-describedby')).toBe(false)
  })
})
