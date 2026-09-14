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

  export interface PaneOpenOptions {
    title: string
    backLabel?: string
    sectionId?: string
  }

  export interface RailRenderContext {
    openInPane: (content: ReactNode, options: PaneOpenOptions) => void
  }

  export interface ChatExtensions {
    railSections?: {
      id: string
      title: string
      contributor?: string
      badge?: () => number
      accent?: boolean
      render: (ctx: RailRenderContext) => ReactNode
    }[]
    composerAddons?: {
      chips?: { id: string; label: string; payload?: string }[]
    }
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

  export function DefaultCitationPopover(props: {
    citation: Citation | undefined
    labels: Record<string, unknown>
  }): ReactNode

  export function bundle(language: 'en' | 'de'): Record<string, unknown>

  export function toLanguage(value: unknown): 'en' | 'de'
}
