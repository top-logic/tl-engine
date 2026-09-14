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
}

/** One collapsible block in the right rail. */
export interface RailSection {
  id: string
  /** Title shown in the section header, already localised. */
  title: string
  /** Small caption on the right of the header, naming the contributor. */
  contributor?: string
  /**
   * Count that feeds the Leiste badge.
   *
   * The toggle shows the sum of every section's current count minus the count last seen when
   * the rail was opened. Fortschritt counts steps waiting for approval; a host section counts
   * whatever it considers new.
   */
  badge?: () => number
  /** Host sections are accent-tinted; the framework's Fortschritt is not. */
  accent?: boolean
  render: (ctx: RailRenderContext) => ReactNode
}

/** What a rail section may read and the two actions it may take. */
export interface RailRenderContext {
  messages: ChatMessage[]
  /** True while the agent is working on the current run. */
  busy: boolean
  openInPane: (content: ReactNode, options: PaneOpenOptions) => void
  addComposerChip: (chip: ComposerChip) => void
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

/** Empty extensions: {@link App} behaves as {@code TLAgentChat} does today. */
export const defaultExtensions: ChatExtensions = {}

/** Whether the host passed any rail section of its own. */
export function hostRailSections(extensions: ChatExtensions | undefined): RailSection[] {
  return extensions?.railSections ?? []
}
