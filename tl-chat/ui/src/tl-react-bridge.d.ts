declare module 'tl-react-bridge' {
  import type * as ReactTypes from 'react'

  export interface TLCellProps {
    controlId: string
    state: Record<string, unknown>
    config?: Record<string, unknown>
  }

  export const React: typeof ReactTypes

  export function register(
    name: string,
    component: ReactTypes.ComponentType<TLCellProps>,
  ): void

  /**
   * Renders a child control the server put into this control's state.
   *
   * A container element's Java control serializes each child it displays as `{controlId, module,
   * state}`; this component looks the module up, gives the child its own state store and SSE
   * subscription, and renders it. That is what lets a chat element host arbitrary configured view
   * elements - `<chat-rail>`'s sections, for one.
   */
  export const TLChild: ReactTypes.FC<{ control: unknown }>

  export function useTLState(): Record<string, unknown>

  export function useTLCommand(): (
    command: string,
    args?: Record<string, unknown>,
  ) => Promise<unknown>
}
