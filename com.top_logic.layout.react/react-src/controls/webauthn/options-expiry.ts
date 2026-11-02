/**
 * The expiry of the WebAuthn options a server supplies: the challenge they carry is accepted only for
 * the `publicKey.timeout` of the options, so a button left alone for a long time asks for fresh
 * options shortly before.
 */

import { PUBLIC_KEY } from './webauthn-json';

/**
 * The part of the timeout of the options after which fresh options are requested, leaving the rest
 * for the round trip to the server.
 */
export const REFRESH_FRACTION = 0.85;

/**
 * The timeout of the given options in milliseconds, `null` for options without one (or options
 * that cannot be read).
 */
export function optionsTimeout(optionsJson: string | null | undefined): number | null {
  if (!optionsJson) {
    return null;
  }
  try {
    const timeout = (JSON.parse(optionsJson) as Record<string, { timeout?: unknown } | undefined>)[PUBLIC_KEY]?.timeout;
    return typeof timeout === 'number' && timeout > 0 ? timeout : null;
  } catch {
    return null;
  }
}

/**
 * The time at which fresh options are requested for options received at the given time, `null` if
 * the options do not expire.
 *
 * @param optionsJson The options of the server.
 * @param receivedAt When the options were received, in milliseconds since the epoch.
 */
export function refreshTime(optionsJson: string | null | undefined, receivedAt: number): number | null {
  const timeout = optionsTimeout(optionsJson);
  return timeout === null ? null : receivedAt + timeout * REFRESH_FRACTION;
}

/**
 * Calls `refresh` once at the {@link refreshTime} of the given options: by a timer, and - since a
 * browser delays the timers of a hidden page - also when the page becomes visible again past that
 * time.
 *
 * @param optionsJson The options of the server.
 * @param receivedAt When the options were received, in milliseconds since the epoch.
 * @param refresh Requests fresh options.
 * @param now The current time, in milliseconds since the epoch.
 * @return Stops the watch.
 */
export function watchExpiry(
  optionsJson: string | null | undefined,
  receivedAt: number,
  refresh: () => void,
  now: () => number = Date.now,
): () => void {
  const due = refreshTime(optionsJson, receivedAt);
  if (due === null) {
    return () => {};
  }

  let stopped = false;
  const stop = () => {
    stopped = true;
    clearTimeout(timer);
    document.removeEventListener('visibilitychange', onVisibilityChange);
  };
  const fire = () => {
    if (!stopped) {
      stop();
      refresh();
    }
  };
  const onVisibilityChange = () => {
    if (document.visibilityState === 'visible' && now() >= due) {
      fire();
    }
  };
  const timer = setTimeout(fire, Math.max(0, due - now()));
  document.addEventListener('visibilitychange', onVisibilityChange);
  return stop;
}
