import type { RunSnapshot } from './types'

function createStore<T>(initial: T) {
  let state = initial
  const listeners = new Set<() => void>()
  return {
    get(): T {
      return state
    },
    set(next: T | ((prev: T) => T)): void {
      state = typeof next === 'function' ? (next as (prev: T) => T)(state) : next
      listeners.forEach((listener) => listener())
    },
    subscribe(listener: () => void): () => void {
      listeners.add(listener)
      return () => {
        listeners.delete(listener)
      }
    },
  }
}

export const runStore = createStore<RunSnapshot>({
  busy: false,
  usedTokens: 0,
  contextWindow: 0,
  model: '',
})

export const TRACE_KEY = 'tl-chat-trace'
