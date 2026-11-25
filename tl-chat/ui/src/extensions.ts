import type { ReactNode } from 'react'
import type { ChatMessage, Citation } from './types'
import type { ReferenceToken } from './reference'

/*
 * The types of the seams a host fills through `registry.ts`: the items of the composer's "+" menu
 * and its "@" picker, the renderer of a citation chip and the actions under an answer.
 */

/** What a host may do to the composer from the outside. */
export interface ComposerApi {
  /**
   * Puts one atomic reference token at the caret.
   *
   * The token is part of the sentence: on send it serialises to
   * `<quote label="…" ref="…">text</quote>` **at its position**, so the model reads the reference
   * where the user put it.
   */
  insertReference: (token: ReferenceToken) => void
  /**
   * Replaces the whole draft with `text`, focuses the field and puts the caret behind it.
   *
   * What the user is looking at when a host writes the draft: the sentence, ready to be read,
   * changed and sent. The send itself stays the user's.
   */
  setDraft: (text: string) => void
  /**
   * Replaces the whole draft with `text` without taking the focus: the text of the chat the
   * composer shows now, which the user left there and comes back to.
   */
  restoreDraft: (text: string) => void
  focus: () => void
}

export interface ComposerMenuItem {
  id: string
  label: string
  description?: string
  icon?: ReactNode
  action: (composer: ComposerApi) => void
}

/** One row the "@" picker offers; choosing it inserts {@link ReferenceCandidate.token}. */
export interface ReferenceCandidate {
  id: string
  label: string
  /** Second line, e.g. the source's kind or its date. */
  hint?: string
  icon?: ReactNode
  token: ReferenceToken
}

/**
 * Renders the popover (or other UI) for a citation chip, in place of the default that shows kind,
 * label, and a link when `citation.url` is set.
 */
export type CitationRender = (
  marker: number,
  citation: Citation | undefined,
  message: ChatMessage,
) => ReactNode

export interface MessageAction {
  id: string
  label: string
  icon?: ReactNode
  action: (message: ChatMessage) => void
}
