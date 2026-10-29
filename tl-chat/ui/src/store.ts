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

/** A subscribable snapshot of one chat's current run. */
export type RunStore = ReturnType<typeof createStore<RunSnapshot>>

/** Creates a run store of a chat's own. */
export function createRunStore(): RunStore {
  return createStore<RunSnapshot>(EMPTY_RUN)
}

/**
 * The run snapshot of *a* chat, for a host that has one.
 *
 * Every `ChatStore` mirrors its run into this, so a window holding two conversations leaves
 * whichever of them ran last standing here - which is why nothing inside this bundle renders from
 * it any more: each chat element reads the store of the chat it joined (`ChatHandle.runs`), and
 * this stays as the single-chat host seam it has always been.
 */
export const runStore = createRunStore()

const TRACE_KEY = 'tl-chat-trace'

const EFFORT_KEY = 'tl-chat-effort'

const ACTIVE_SESSION_KEY = 'tl-chat-session'

function isTraceMode(value: unknown): value is TraceMode {
  return value === 'none' || value === 'summary' || value === 'detailed'
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

/**
 * Where one chat's open conversation is remembered.
 *
 * **Per `chat-id`.** It used to be one key for the whole tab, which is wrong the moment a window
 * holds two conversations - a `<chat-dock>` beside a chat page, say: each store wrote the key on
 * every session change, and on the next reload both read the last writer's id and restored the
 * *same* conversation into both places.
 */
function activeSessionKey(chatId: string): string {
  const id = chatId.trim()
  return id ? `${ACTIVE_SESSION_KEY}:${id}` : ACTIVE_SESSION_KEY
}

/**
 * The conversation this browser window had open on the given chat, or `null` when it had none.
 *
 * Held in `sessionStorage` rather than `localStorage` on purpose: that is per tab and survives a
 * reload, which is exactly the distinction wanted here - pressing F5 returns to the conversation
 * you were reading, while a newly opened tab starts on the empty state instead of inheriting
 * another tab's chat.
 */
export function readActiveSession(chatId: string): string | null {
  try {
    const stored = window.sessionStorage.getItem(activeSessionKey(chatId))
    return stored && stored.trim() ? stored : null
  } catch {
    return null
  }
}

/** Remembers the open conversation of one chat. Failing to store it must not break the chat. */
export function writeActiveSession(chatId: string, sessionId: string | null): void {
  try {
    const key = activeSessionKey(chatId)
    if (sessionId) window.sessionStorage.setItem(key, sessionId)
    else window.sessionStorage.removeItem(key)
  } catch {
    /* the open chat simply does not survive a reload */
  }
}
