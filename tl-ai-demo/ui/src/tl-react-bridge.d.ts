declare module 'tl-react-bridge' {
  import type * as ReactTypes from 'react'

  export interface TLCellProps {
    controlId: string
    state?: Record<string, unknown>
    config?: Record<string, unknown>
  }

  export const React: typeof ReactTypes

  export function register(
    name: string,
    component: ReactTypes.ComponentType<TLCellProps>,
  ): void

  export function useTLState(): Record<string, unknown>
}
