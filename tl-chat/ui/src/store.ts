import type { RunSnapshot, TraceMode } from './types'

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

export const EMPTY_RUN: RunSnapshot = {
  busy: false,
  measuredTokens: 0,
  estimatedTokens: 0,
  contextWindow: 0,
  model: '',
  effort: '',
}

export const runStore = createStore<RunSnapshot>(EMPTY_RUN)

const TRACE_KEY = 'tl-chat-trace'

const EFFORT_KEY = 'tl-chat-effort'

function isTraceMode(value: unknown): value is TraceMode {
  return value === 'none' || value === 'overview' || value === 'detailed'
}

/**
 * The trace level the user last chose, or `null` when they never chose one.
 *
 * Storage can be unavailable (a private window, blocked site data), and a stored value can be
 * anything, so both are treated as "no choice" rather than as an error.
 */
export function readTraceMode(): TraceMode | null {
  try {
    const stored = window.localStorage.getItem(TRACE_KEY)
    return isTraceMode(stored) ? stored : null
  } catch {
    return null
  }
}

/** Remembers the user's trace level. Failing to store it must not break the chat. */
export function writeTraceMode(mode: TraceMode): void {
  try {
    window.localStorage.setItem(TRACE_KEY, mode)
  } catch {
    /* the choice simply does not survive a reload */
  }
}

/**
 * The effort level this user last chose, or `null`.
 *
 * A session carries its own level, restored from the server; this is only what a *new* chat starts
 * with, which is a per-browser convenience rather than session state.
 */
export function readEffort(): string | null {
  try {
    const stored = window.localStorage.getItem(EFFORT_KEY)
    return stored && stored.trim() ? stored : null
  } catch {
    return null
  }
}

/** Remembers the effort level for the next new chat. */
export function writeEffort(effort: string | null): void {
  try {
    if (effort) window.localStorage.setItem(EFFORT_KEY, effort)
    else window.localStorage.removeItem(EFFORT_KEY)
  } catch {
    /* the choice simply does not survive a reload */
  }
}
