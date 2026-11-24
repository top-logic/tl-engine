import React from 'react';
import { getApiBase, getWindowName, isContextKnown, onMountContext } from './page-context';

/**
 * Client-side i18n service for React controls.
 *
 * Lazily fetches translations from the server and caches them. Uses microtask-batched
 * fetching so that multiple components requesting keys in the same render cycle trigger
 * only a single HTTP request.
 *
 * The first request reads the context path and the window name of the page at once (see
 * page-context.ts), also before the first mount(). Only on a page whose body does not name its
 * context, keys wait until the first mount() gives a context path, and are then requested together.
 */

/** Resolved translations keyed by resource key. */
const _cache = new Map<string, string>();

/** Keys waiting to be fetched in the next microtask. */
const _pending = new Set<string>();

/**
 * Keys requested from the server whose answer has not arrived yet. A component rendering again
 * before the answer requests none of them a second time: every answer re-renders all components
 * using translations, so each duplicate request would cause another round of renders.
 */
const _inFlight = new Set<string>();

/** Incremented by {@link clearI18NCache}: an answer to a request sent before is discarded. */
let _generation = 0;

/** Whether a microtask has been scheduled to flush pending keys. */
let _fetchScheduled = false;

/** Incremented on every cache mutation to trigger useSyncExternalStore re-renders. */
let _cacheVersion = 0;

/** React re-render callbacks registered via useSyncExternalStore. */
const _subscribers = new Set<() => void>();

// A mount() can make the context known: the keys that wait for it go out in the next microtask,
// together with the keys that the components of the same mount() request.
onMountContext(() => {
  if (_pending.size > 0 && !_fetchScheduled) {
    _fetchScheduled = true;
    queueMicrotask(flushPending);
  }
});

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
  if (_inFlight.has(key)) {
    return;
  }
  _pending.add(key);
  if (!_fetchScheduled) {
    _fetchScheduled = true;
    queueMicrotask(flushPending);
  }
}

async function flushPending(): Promise<void> {
  _fetchScheduled = false;
  if (_pending.size === 0 || !isContextKnown()) {
    // Without a known context path, the request would go to an address relative to the page. The
    // keys stay pending until a mount() gives the context path.
    return;
  }

  const keys = Array.from(_pending);
  _pending.clear();
  for (const key of keys) {
    _inFlight.add(key);
  }
  const generation = _generation;

  let data: Record<string, string>;
  try {
    // The window name lets the server answer in the locale of the session of the window. The page
    // context is read now and not at the first mount(): the busy overlay asks for its text before
    // any control is mounted.
    const url = getApiBase() + 'react-api/i18n?keys=' + encodeURIComponent(keys.join(','))
      + '&windowName=' + encodeURIComponent(getWindowName());
    const resp = await fetch(url);
    if (!resp.ok) {
      console.error('[TLReact] i18n fetch failed:', resp.status);
      return;
    }
    data = await resp.json() as Record<string, string>;
  } catch (e) {
    console.error('[TLReact] i18n fetch error:', e);
    return;
  } finally {
    if (generation === _generation) {
      for (const key of keys) {
        _inFlight.delete(key);
      }
    }
  }
  if (generation !== _generation) {
    // The cache was cleared while the request was under way; the keys are requested anew.
    return;
  }

  let changed = false;
  for (const [k, v] of Object.entries(data)) {
    if (_cache.get(k) !== v) {
      _cache.set(k, v);
      changed = true;
    }
  }
  if (changed) {
    _cacheVersion++;
    notifySubscribers();
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
  _inFlight.clear();
  _generation++;
  _cacheVersion++;
  notifySubscribers();
}
