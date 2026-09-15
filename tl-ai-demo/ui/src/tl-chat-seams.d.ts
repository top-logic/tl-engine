declare module 'tl-chat-seams' {
  import type { ComponentType, ReactNode } from 'react'
  import type { TLCellProps } from 'tl-react-bridge'

  export interface Citation {
    index: number
    toolName: string
    toolCallId: string
    kind: string
    id: string
    label: string
    url: string
  }

  export type PaneMode = 'card' | 'document'

  export interface PaneAction {
    id: string
    label: string
    icon?: ReactNode
    onSelect: () => void
  }

  export interface PaneOpenOptions {
    title: string
    backLabel?: string
    sectionId?: string
    mode?: PaneMode
    version?: string
    actions?: PaneAction[]
  }

  export interface ReferenceToken {
    kind: string
    id: string
    label: string
    location?: string
    text: string
  }

  export interface ReferenceCandidate {
    id: string
    label: string
    hint?: string
    icon?: ReactNode
    token: ReferenceToken
  }

  export interface ContextFact {
    id: string
    label: string
    value: string
  }

  export interface RailRenderContext {
    sessionId: string | null
    busy: boolean
    openInPane: (content: ReactNode, options: PaneOpenOptions) => void
    closePane: () => void
    insertReference: (token: ReferenceToken) => void
  }

  export interface ChatExtensions {
    railSections?: {
      id: string
      title: string
      contributor?: string
      icon?: ReactNode
      badge?: () => number
      count?: () => number
      accent?: boolean
      render: (ctx: RailRenderContext) => ReactNode
    }[]
    composerAddons?: {
      referenceCandidates?: (query: string) => ReferenceCandidate[]
    }
    contextFacts?: (ctx: { messages: unknown[] }) => ContextFact[]
    sourceTabs?: { id: string; label: string; render: (ctx: RailRenderContext) => ReactNode }[]
    messageActions?: { id: string; label: string; icon?: ReactNode; action: (message: unknown) => void }[]
    messageAnnotations?: {
      render: (
        marker: number,
        citation: Citation | undefined,
        message: unknown,
        openInPane: (content: ReactNode, options: PaneOpenOptions) => void,
      ) => ReactNode
    }
  }

  export const App: ComponentType<TLCellProps & { extensions?: ChatExtensions }>

  export function bundle(language: 'en' | 'de'): Record<string, unknown>

  export function toLanguage(value: unknown): 'en' | 'de'
}
