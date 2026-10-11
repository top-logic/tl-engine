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

  /**
   * Listens to the patches the window's event stream addresses to `controlId`, each one as it
   * arrives. The id need not belong to a mounted control: the chat's frames travel under an id of
   * their own (`openChatStream`).
   */
  export function subscribe(controlId: string, listener: (patch: Record<string, unknown>) => void): void

  /** Stops a listener `subscribe` added. */
  export function unsubscribe(controlId: string, listener: (patch: Record<string, unknown>) => void): void

  /**
   * The class name of a control's root element: the given classes the component brings itself,
   * followed by the `css-class` the view configured for this one element. Falsy entries are left out.
   */
  export function rootClassName(
    state: Record<string, unknown>,
    ...classes: (string | false | null | undefined)[]
  ): string

  export function useTLCommand(): (
    command: string,
    args?: Record<string, unknown>,
  ) => Promise<unknown>
}
