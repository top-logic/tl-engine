import type { ReactNode } from 'react'
import type { ChatMessage, Citation } from './types'
import type { ReferenceToken } from './reference'

/**
 * How a host application composes the chat without forking it.
 *
 * Every field is optional. {@link App} with no `extensions` (and {@code TLAgentChat}) keeps the
 * shipped behaviour: the session rail, the thread, the composer, and the Fortschritt section as
 * the only right-rail contribution. File splits inside `ui/src` are not part of this contract.
 */
export interface ChatExtensions {
  /** Extra sections below Fortschritt, in the order given. */
  railSections?: RailSection[]
  /** Chips and extra "+" menu items the host contributes to the composer. */
  composerAddons?: ComposerAddons
  /** How a citation chip opens; the default is a web-link popover. */
  messageAnnotations?: MessageAnnotations
  /** Extra buttons under a finished answer, after copy / rate / regenerate. */
  messageActions?: MessageAction[]
  /** Header caption and the `<context/>` prefix prepended to a send. */
  contextHook?: ContextHook
  /**
   * Extra lines in the Kontext section, after the ones the framework contributes
   * (language, agent, model, session).
   */
  contextFacts?: (ctx: ContextHookArg) => ContextFact[]
  /**
   * Lists the host puts beside the framework's `Quellen` list, as further tabs of that section.
   */
  sourceTabs?: SourceTab[]
}

/** One tab beside the framework's list of cited sources. */
export interface SourceTab {
  id: string
  /** Already localised. */
  label: string
  render: (ctx: RailRenderContext) => ReactNode
}

/** One collapsible block in the right rail. */
export interface RailSection {
  id: string
  /** Title shown in the section header, already localised. */
  title: string
  /**
   * Icon shown on the 56 px strip while an artefact is open.
   *
   * A 16x16 `<svg viewBox="0 0 16 16">` fits the strip button. Without one the strip shows a
   * generic section glyph, never a letter.
   */
  icon?: ReactNode
  /**
   * Small caption on the right of the header.
   *
   * Empty by default, including on the framework's own sections: the accent tint already says whose
   * a card is, and a caption on every one of them is noise. A host may name itself on its own.
   */
  contributor?: string
  /**
   * Count that feeds the Leiste badge: how much of this section is *new*.
   *
   * The toggle shows the sum over all sections minus the total last seen when the rail was opened,
   * so a section that has no notion of news - a list of rows that simply exist - contributes none
   * and leaves the badge to the sections that do. Fortschritt counts the steps that still want
   * something: pending, running, or waiting for an approval.
   */
  badge?: () => number
  /**
   * Count shown next to the section's own title. Defaults to {@link badge}.
   *
   * Separate from the badge because the two answer different questions: this one is "how many are
   * there", the badge is "how many are new since you last looked".
   */
  count?: () => number
  /** Host sections are accent-tinted; the framework's Fortschritt is not. */
  accent?: boolean
  render: (ctx: RailRenderContext) => ReactNode
}

/** What a rail section may read and the actions it may take. */
export interface RailRenderContext {
  /** Id of the open chat session, or `null` before the first send. */
  sessionId: string | null
  /**
   * The current run's trace: every message of the open session, user turns and agent runs alike,
   * in the order the thread shows them.
   */
  messages: ChatMessage[]
  /** True while the agent is working on the current run. */
  busy: boolean
  /** Tool calls the running turn has approvals open for; empty when nothing waits. */
  pendingApprovals: PendingApproval[]
  /** Replaces the rail with the host's own content, in the same place. */
  openInPane: (content: ReactNode, options: PaneOpenOptions) => void
  /** Closes the pane and shows the rail again. */
  closePane: () => void
  /** Puts an atomic reference token at the composer's caret. */
  insertReference: (token: ReferenceToken) => void
}

/** One tool call of the running turn that is waiting for the user's approval. */
export interface PendingApproval {
  id: string
  toolName: string
}

/**
 * How wide the pane opens.
 *
 * `card` is a record beside the thread - the default, 420 px with a 360 px floor. `document` is
 * something to read: the pane takes about two thirds of the workspace and the thread the rest,
 * never narrower than its own composer. Below 1280 px both are a sheet over the thread.
 */
export type PaneMode = 'card' | 'document'

/** Header of the side pane when an artefact is open. */
export interface PaneOpenOptions {
  title: string
  /** Back-link label, e.g. "Artefakte". */
  backLabel?: string
  /** Host-defined tabs on the right of the pane header. */
  tabs?: PaneTab[]
  /** Which section's icon is highlighted on the 56 px strip. */
  sectionId?: string
  /** Defaults to `card`. */
  mode?: PaneMode
  /** Shown next to the name in the pane header, e.g. "Version 2". */
  version?: string
  /** Host actions in the pane header, right of the name. */
  actions?: PaneAction[]
}

/** One host action in the pane header. */
export interface PaneAction {
  id: string
  label: string
  icon?: ReactNode
  onSelect: () => void
}

export interface PaneTab {
  id: string
  label: string
  active?: boolean
  onSelect?: () => void
}

export type SidePaneState = 'closed' | 'rail' | 'content'

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

export interface ComposerAddons {
  extraMenuItems?: ComposerMenuItem[]
  /**
   * What the empty field invites the user with, already localised.
   *
   * The framework's own wording addresses the user informally in German, which an application
   * whose every other surface says "Sie" cannot use (#420). A host states its own invitation here;
   * it replaces both framework variants, the one that mentions "@" included.
   *
   * Idle only: while the agent works the field keeps the framework's "type your next message",
   * which is about the run rather than about the application. An `<agent-chat placeholder="…">`
   * states the same thing declaratively and wins over this one, being the installation's decision
   * rather than the host code's default.
   */
  placeholder?: string
  /**
   * What typing "@" in the composer offers, filtered by what has been typed after it.
   *
   * The framework knows of no sources of its own, so without this seam "@" offers nothing and the
   * placeholder does not mention it.
   */
  referenceCandidates?: (query: string) => ReferenceCandidate[]
}

export type CitationRender = (
  marker: number,
  citation: Citation | undefined,
  message: ChatMessage,
  openInPane: (content: ReactNode, options: PaneOpenOptions) => void,
) => ReactNode

export interface MessageAnnotations {
  /**
   * Renders the popover (or other UI) for a citation chip.
   *
   * The default shows kind, label, and a link when `citation.url` is set. A host may instead
   * open the source in the side pane via `openInPane`.
   */
  render: CitationRender
}

export interface MessageAction {
  id: string
  label: string
  icon?: ReactNode
  action: (message: ChatMessage) => void
}

export interface ContextHook {
  /** Optional line in the chat header, already localised. */
  header?: (ctx: ContextHookArg) => ReactNode
  /**
   * When set, prepended to the send (and to regenerate) as
   * {@code <context>…</context>} on {@code ChatSendRequest.text}.
   */
  contextTag?: (ctx: ContextHookArg) => string | undefined
}

export interface ContextHookArg {
  messages: ChatMessage[]
}

/** One line of the Kontext section: what the agent receives with every turn. */
export interface ContextFact {
  id: string
  /** Already localised. */
  label: string
  value: string
}

/** Empty extensions: {@link App} behaves as {@code TLAgentChat} does today. */
export const defaultExtensions: ChatExtensions = {}

/** Whether the host passed any rail section of its own. */
export function hostRailSections(extensions: ChatExtensions | undefined): RailSection[] {
  return extensions?.railSections ?? []
}
