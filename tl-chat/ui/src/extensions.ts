import type { ReactNode } from 'react'
import type { ChatMessage, Citation } from './types'

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
  /** Small caption on the right of the header, naming the contributor. */
  contributor?: string
  /**
   * Count that feeds the Leiste badge.
   *
   * The toggle shows the sum of every section's current count minus the count last seen when
   * the rail was opened. Fortschritt counts tasks that are still pending or in progress; a host
   * section counts whatever it considers new.
   */
  badge?: () => number
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
  addComposerChip: (chip: ComposerChip) => void
}

/** One tool call of the running turn that is waiting for the user's approval. */
export interface PendingApproval {
  id: string
  toolName: string
}

/** Header of the side pane when an artefact is open. */
export interface PaneOpenOptions {
  title: string
  /** Back-link label, e.g. "Artefakte". */
  backLabel?: string
  /** Host-defined tabs on the right of the pane header. */
  tabs?: PaneTab[]
  /** Which section's icon is highlighted on the 56 px strip. */
  sectionId?: string
}

export interface PaneTab {
  id: string
  label: string
  active?: boolean
  onSelect?: () => void
}

export type SidePaneState = 'closed' | 'rail' | 'content'

export interface ComposerChip {
  id: string
  label: string
  /** Travels with the send as a `<quote>` prefix; defaults to the label. */
  payload?: string
}

export interface ComposerMenuItem {
  id: string
  label: string
  description?: string
  icon?: ReactNode
  action: () => void
}

export interface ComposerAddons {
  chips?: ComposerChip[]
  extraMenuItems?: ComposerMenuItem[]
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
