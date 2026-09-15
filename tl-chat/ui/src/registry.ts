import type { ReactNode } from 'react'
import type {
  CitationRender,
  ComposerMenuItem,
  MessageAction,
  RailRenderContext,
  RailSection,
  ReferenceCandidate,
} from './extensions'

type RailSectionFactory = (ctx: RailRenderContext) => RailSection

const RAIL_SECTIONS = new Map<string, RailSectionFactory>()
const REFERENCE_SOURCES = new Map<string, (query: string) => ReferenceCandidate[]>()
const MENU_ITEMS = new Map<string, ComposerMenuItem>()
const ANNOTATION_RENDERERS = new Map<string, CitationRender>()
const MESSAGE_ACTIONS = new Map<string, MessageAction>()

/** Registers a host rail section under the name XML uses in {@code <section name="…"/>}. */
export function registerRailSection(name: string, factory: RailSectionFactory): void {
  RAIL_SECTIONS.set(name, factory)
}

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

export function registeredRailSection(name: string, ctx: RailRenderContext): RailSection | undefined {
  return RAIL_SECTIONS.get(name)?.(ctx)
}

export function registeredRailSectionNames(): string[] {
  return [...RAIL_SECTIONS.keys()]
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

/** Test helper: drop every registration. */
export function resetRegistries(): void {
  RAIL_SECTIONS.clear()
  REFERENCE_SOURCES.clear()
  MENU_ITEMS.clear()
  ANNOTATION_RENDERERS.clear()
  MESSAGE_ACTIONS.clear()
}

export type { ReactNode }
