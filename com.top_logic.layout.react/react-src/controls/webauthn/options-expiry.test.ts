import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { optionsTimeout, refreshTime, watchExpiry, REFRESH_FRACTION } from './options-expiry';

/** Options with the given timeout in milliseconds. */
function options(timeout?: number): string {
  return JSON.stringify({ publicKey: { challenge: 'AQ', ...(timeout === undefined ? {} : { timeout }) } });
}

/** Lets the page report the given visibility and announces the change. */
function setVisibility(state: DocumentVisibilityState): void {
  Object.defineProperty(document, 'visibilityState', { configurable: true, get: () => state });
  document.dispatchEvent(new Event('visibilitychange'));
}

describe('optionsTimeout', () => {
  it('reads the timeout of the public key options', () => {
    expect(optionsTimeout(options(60000))).toBe(60000);
  });

  it('is null for options without timeout, no options and unreadable options', () => {
    expect(optionsTimeout(options())).toBeNull();
    expect(optionsTimeout(null)).toBeNull();
    expect(optionsTimeout('{')).toBeNull();
    expect(optionsTimeout(options(0))).toBeNull();
  });

  it('places the refresh at the refresh fraction of the timeout', () => {
    expect(refreshTime(options(1000), 5000)).toBe(5000 + 1000 * REFRESH_FRACTION);
    expect(refreshTime(options(), 5000)).toBeNull();
  });
});

describe('watchExpiry', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(100000);
  });

  afterEach(() => {
    vi.useRealTimers();
    setVisibility('visible');
  });

  it('refreshes once shortly before the timeout', () => {
    const refresh = vi.fn();
    watchExpiry(options(10000), Date.now(), refresh);

    vi.advanceTimersByTime(10000 * REFRESH_FRACTION - 1);
    expect(refresh).not.toHaveBeenCalled();
    vi.advanceTimersByTime(1);
    expect(refresh).toHaveBeenCalledTimes(1);

    vi.advanceTimersByTime(100000);
    setVisibility('visible');
    expect(refresh).toHaveBeenCalledTimes(1);
  });

  it('counts the timeout from the receipt of the options', () => {
    const refresh = vi.fn();
    watchExpiry(options(10000), Date.now() - 8000, refresh);

    vi.advanceTimersByTime(500);
    expect(refresh).toHaveBeenCalledTimes(1);
  });

  it('refreshes when the page becomes visible past the refresh time', () => {
    const refresh = vi.fn();
    let time = Date.now();
    watchExpiry(options(10000), time, refresh, () => time);

    // A hidden page whose timers the browser held back.
    setVisibility('hidden');
    time += 9000;
    expect(refresh).not.toHaveBeenCalled();

    setVisibility('visible');
    expect(refresh).toHaveBeenCalledTimes(1);
  });

  it('does not refresh when the page becomes visible before the refresh time', () => {
    const refresh = vi.fn();
    watchExpiry(options(10000), Date.now(), refresh);

    setVisibility('visible');
    expect(refresh).not.toHaveBeenCalled();
  });

  it('does not refresh options without timeout', () => {
    const refresh = vi.fn();
    watchExpiry(options(), Date.now(), refresh);

    vi.advanceTimersByTime(1000000);
    setVisibility('visible');
    expect(refresh).not.toHaveBeenCalled();
  });

  it('does not refresh after the watch is stopped', () => {
    const refresh = vi.fn();
    const stop = watchExpiry(options(10000), Date.now(), refresh);

    stop();
    vi.advanceTimersByTime(100000);
    setVisibility('visible');
    expect(refresh).not.toHaveBeenCalled();
  });
});
