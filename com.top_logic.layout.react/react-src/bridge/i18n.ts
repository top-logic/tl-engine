import React from 'react';

/**
 * Client-side i18n service for React controls.
 *
 * Lazily fetches translations from the server and caches them. Uses microtask-batched
 * fetching so that multiple components requesting keys in the same render cycle trigger
 * only a single HTTP request.
 */

/** Resolved translations keyed by resource key. */
const _cache = new Map<string, string>();

/** Keys waiting to be fetched in the next microtask. */
const _pending = new Set<string>();

/** Whether a microtask has been scheduled to flush pending keys. */
let _fetchScheduled = false;

/** Incremented on every cache mutation to trigger useSyncExternalStore re-renders. */
let _cacheVersion = 0;

/** React re-render callbacks registered via useSyncExternalStore. */
const _subscribers = new Set<() => void>();

/** Base URL for the i18n endpoint (e.g. "/demo/react-api/i18n"). */
let _apiBase = '';

/** Window name to include in i18n requests so the server resolves the correct locale. */
let _windowName = '';

/**
 * Sets the API base URL. Called once from tl-react-bridge.ts during mount().
 */
export function setI18NApiBase(base: string): void {
  _apiBase = base;
}

/**
 * Sets the window name for i18n requests. Called from tl-react-bridge.ts on every mount().
 */
export function setI18NWindowName(name: string): void {
  _windowName = name;
}

function notifySubscribers(): void {
  for (const cb of _subscribers) {
    cb();
  }
}

function subscribeI18N(callback: () => void): () => void {
  _subscribers.add(callback);
  return () => _subscribers.delete(callback);
}

function getI18NVersion(): number {
  return _cacheVersion;
}

function scheduleFetch(key: string): void {
  _pending.add(key);
  if (!_fetchScheduled) {
    _fetchScheduled = true;
    queueMicrotask(flushPending);
  }
}

async function flushPending(): Promise<void> {
  _fetchScheduled = false;
  if (_pending.size === 0) {
    return;
  }

  const keys = Array.from(_pending);
  _pending.clear();

  try {
    const url = _apiBase + 'react-api/i18n?keys=' + encodeURIComponent(keys.join(','))
      + '&windowName=' + encodeURIComponent(_windowName);
    const resp = await fetch(url);
    if (!resp.ok) {
      console.error('[TLReact] i18n fetch failed:', resp.status);
      return;
    }
    const data = await resp.json() as Record<string, string>;
    for (const [k, v] of Object.entries(data)) {
      _cache.set(k, v);
    }
    _cacheVersion++;
    notifySubscribers();
  } catch (e) {
    console.error('[TLReact] i18n fetch error:', e);
  }
}

/**
 * Hook that returns translated strings for the given keys.
 *
 * @param keys  An object mapping resource keys to fallback English strings.
 * @returns     An object with the same keys, values replaced by cached translations
 *              (or the fallback if not yet fetched).
 */
export function useI18N(keys: Record<string, string>): Record<string, string> {
  React.useSyncExternalStore(subscribeI18N, getI18NVersion);

  const result: Record<string, string> = {};
  for (const [key, fallback] of Object.entries(keys)) {
    const cached = _cache.get(key);
    if (cached !== undefined) {
      result[key] = cached;
    } else {
      result[key] = fallback;
      scheduleFetch(key);
    }
  }
  return result;
}

/**
 * Clears the entire i18n cache and triggers re-renders so that all useI18N hooks
 * re-fetch their translations lazily.
 */
export function clearI18NCache(): void {
  _cache.clear();
  _cacheVersion++;
  notifySubscribers();
}
