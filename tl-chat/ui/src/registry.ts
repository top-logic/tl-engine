import type { ReactNode } from 'react'
import type {
  CitationRender,
  ComposerMenuItem,
  MessageAction,
  ReferenceCandidate,
} from './extensions'

const REFERENCE_SOURCES = new Map<string, (query: string) => ReferenceCandidate[]>()
const MENU_ITEMS = new Map<string, ComposerMenuItem>()
const ANNOTATION_RENDERERS = new Map<string, CitationRender>()
const MESSAGE_ACTIONS = new Map<string, MessageAction>()

/** Registers the candidates typing {@code @} offers, named from {@code reference-source}. */
export function registerReferenceSource(
  name: string,
  source: (query: string) => ReferenceCandidate[],
): void {
  REFERENCE_SOURCES.set(name, source)
}

/** Registers a composer "+" item, named from {@code menu-items}. */
export function registerMenuItem(name: string, item: ComposerMenuItem): void {
  MENU_ITEMS.set(name, item)
}

/** Registers how a citation chip renders, named from {@code annotations}. */
export function registerAnnotationRenderer(name: string, render: CitationRender): void {
  ANNOTATION_RENDERERS.set(name, render)
}

/** Registers a button under a finished answer, named from {@code <actions host="…"/>}. */
export function registerMessageAction(name: string, action: MessageAction): void {
  MESSAGE_ACTIONS.set(name, action)
}

export function registeredReferenceSource(
  name: string,
): ((query: string) => ReferenceCandidate[]) | undefined {
  return name ? REFERENCE_SOURCES.get(name) : undefined
}

export function resolveMenuItems(names: string[]): ComposerMenuItem[] {
  const items: ComposerMenuItem[] = []
  for (const name of names) {
    const item = MENU_ITEMS.get(name)
    if (item) items.push(item)
  }
  return items
}

export function registeredAnnotationRenderer(name: string): CitationRender | undefined {
  return name ? ANNOTATION_RENDERERS.get(name) : undefined
}

export function resolveMessageActions(names: string[]): MessageAction[] {
  const actions: MessageAction[] = []
  for (const name of names) {
    const action = MESSAGE_ACTIONS.get(name)
    if (action) actions.push(action)
  }
  return actions
}

export type { ReactNode }
