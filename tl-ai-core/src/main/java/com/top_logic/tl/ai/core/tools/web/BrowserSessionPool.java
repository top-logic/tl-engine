/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.LongSupplier;

import com.top_logic.basic.Logger;

/**
 * Bounded pool of keyed, reusable heavy resources (browser sessions) with fail-fast admission
 * and idle eviction.
 *
 * <p>
 * This class is deliberately free of any Playwright dependency: it manages only the concurrency
 * policy so it can be unit-tested with a lightweight fake resource. It replaces the browser
 * tool's former single global {@code ReentrantLock}. The guarantees are:
 * </p>
 * <ul>
 * <li><b>Bounded live sessions.</b> At most {@code maxSessions} resources exist at once. The
 * bound protects memory (each live browser context costs RAM); it is <em>not</em> a throughput
 * throttle - independent sessions run fully concurrently.</li>
 * <li><b>Reuse by key.</b> {@link #run(Object, Function)} returns the existing resource for a
 * key, so a session sticks across many tool calls.</li>
 * <li><b>Fail-fast admission.</b> Creating a new session waits at most {@code acquireTimeout} for
 * a free slot (reclaiming an idle session by LRU if possible); if none frees up it throws
 * {@link BrowserBusyException} rather than blocking the caller thread indefinitely.</li>
 * <li><b>Idle eviction.</b> A background sweep closes sessions idle longer than
 * {@code idleTimeout}, and an admission that finds the pool full evicts the least-recently-used
 * <em>idle</em> session to make room. A session executing a call is never evicted.</li>
 * <li><b>Clean teardown.</b> {@link #close()} stops the sweeper and destroys every remaining
 * resource.</li>
 * </ul>
 *
 * <p>
 * The pool is thread-safe. Resource creation and destruction happen outside the internal lock so
 * a slow browser launch never blocks unrelated sessions; destruction is asynchronous.
 * </p>
 *
 * @param <S>
 *        The managed resource type (a browser session).
 */
final class BrowserSessionPool<S> implements AutoCloseable {

	/** Creates the heavy resource for a key. May block (e.g. launching a browser). */
	interface Factory<S> {
		/** Creates a resource for the given key. */
		S create(Object key);
	}

	/** Destroys a resource (e.g. closing a browser). May block; called off the pool lock. */
	interface Destroyer<S> {
		/** Releases all resources held by the session. */
		void destroy(S session);
	}

	private static final class Entry<S> {
		final Object _key;

		final S _session;

		int _inUse;

		long _lastAccessNanos;

		boolean _removed;

		boolean _broken;

		Entry(Object key, S session, long nowNanos) {
			_key = key;
			_session = session;
			_lastAccessNanos = nowNanos;
		}
	}

	private final int _maxSessions;

	private final long _idleTimeoutNanos;

	private final long _acquireTimeoutNanos;

	private final LongSupplier _clock;

	private final Factory<S> _factory;

	private final Destroyer<S> _destroyer;

	private final ReentrantLock _lock = new ReentrantLock();

	/** Signalled whenever a slot frees up (session removed or reservation released). */
	private final Condition _slotFreed = _lock.newCondition();

	private final Map<Object, Entry<S>> _entries = new HashMap<>();

	/** Live sessions plus in-flight reservations; the quantity bounded by {@link #_maxSessions}. */
	private int _activeCount;

	private boolean _closed;

	private final ScheduledExecutorService _maintenance;

	/**
	 * Creates a pool.
	 *
	 * @param maxSessions
	 *        Maximum number of concurrent live sessions (>= 1).
	 * @param idleTimeoutMillis
	 *        Close a session after it has been idle this long.
	 * @param acquireTimeoutMillis
	 *        Maximum time to wait for a free slot before failing fast.
	 * @param clockNanos
	 *        Monotonic nanosecond clock (inject a fake for tests; use {@code System::nanoTime}).
	 * @param factory
	 *        Creates a resource for a key.
	 * @param destroyer
	 *        Releases a resource.
	 */
	BrowserSessionPool(int maxSessions, long idleTimeoutMillis, long acquireTimeoutMillis,
			LongSupplier clockNanos, Factory<S> factory, Destroyer<S> destroyer) {
		_maxSessions = Math.max(1, maxSessions);
		_idleTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(1_000, idleTimeoutMillis));
		_acquireTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(0, acquireTimeoutMillis));
		_clock = clockNanos;
		_factory = factory;
		_destroyer = destroyer;

		long sweepPeriodMillis = Math.max(1_000, idleTimeoutMillis / 2);
		_maintenance = Executors.newSingleThreadScheduledExecutor(daemon("browser-session-maintenance"));
		_maintenance.scheduleWithFixedDelay(this::sweepIdleSafely, sweepPeriodMillis, sweepPeriodMillis,
			TimeUnit.MILLISECONDS);
	}

	private static ThreadFactory daemon(String name) {
		return runnable -> {
			Thread thread = new Thread(runnable, name);
			thread.setDaemon(true);
			return thread;
		};
	}

	/**
	 * Runs {@code op} against the session for {@code key}, creating the session if needed and
	 * reusing it otherwise. The session is marked in-use for the duration, so a concurrent
	 * eviction or idle sweep cannot close it mid-call.
	 *
	 * @throws BrowserBusyException
	 *         if the pool is at capacity and no idle session can be reclaimed in time.
	 */
	<R> R run(Object key, Function<S, R> op) {
		Entry<S> entry = acquire(key);
		try {
			return op.apply(entry._session);
		} finally {
			release(entry, false);
		}
	}

	/**
	 * Discards the session for {@code key}: removes it and destroys its resource. Safe to call at
	 * any time; a no-op if the key is absent. If the session is currently executing a call, it is
	 * torn down as soon as that call releases it. Used when a call reports the page hung.
	 */
	void invalidate(Object key) {
		_lock.lock();
		try {
			Entry<S> entry = _entries.get(key);
			if (entry == null || entry._removed) {
				return;
			}
			if (entry._inUse > 0) {
				entry._broken = true;
			} else {
				removeLocked(entry);
			}
		} finally {
			_lock.unlock();
		}
	}

	private Entry<S> acquire(Object key) {
		_lock.lock();
		try {
			if (_closed) {
				throw new IllegalStateException("Browser session pool is closed.");
			}
			Entry<S> existing = _entries.get(key);
			if (existing != null && !existing._removed) {
				existing._inUse++;
				existing._lastAccessNanos = _clock.getAsLong();
				return existing;
			}
			reserveSlot();
		} finally {
			_lock.unlock();
		}

		// Create the (heavy) resource outside the lock so a slow launch blocks nobody else.
		S session;
		try {
			session = _factory.create(key);
		} catch (RuntimeException | Error ex) {
			_lock.lock();
			try {
				_activeCount--;
				_slotFreed.signalAll();
			} finally {
				_lock.unlock();
			}
			throw ex;
		}

		_lock.lock();
		try {
			Entry<S> race = _entries.get(key);
			if (race != null && !race._removed) {
				// A concurrent caller created the same key; keep theirs, drop ours.
				_activeCount--;
				_slotFreed.signalAll();
				destroyAsync(session);
				race._inUse++;
				race._lastAccessNanos = _clock.getAsLong();
				return race;
			}
			Entry<S> entry = new Entry<>(key, session, _clock.getAsLong());
			entry._inUse = 1;
			_entries.put(key, entry);
			return entry;
		} finally {
			_lock.unlock();
		}
	}

	/** Reserves one of the {@link #_maxSessions} slots, waiting/evicting as needed. Caller holds the lock. */
	private void reserveSlot() {
		long deadline = _clock.getAsLong() + _acquireTimeoutNanos;
		while (_activeCount >= _maxSessions) {
			if (reclaimIdleLocked()) {
				continue;
			}
			long remaining = deadline - _clock.getAsLong();
			if (remaining <= 0) {
				throw new BrowserBusyException(
					"The browser is busy: all " + _maxSessions + " concurrent sessions are in use. Retry shortly.");
			}
			try {
				_slotFreed.awaitNanos(remaining);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new BrowserBusyException("Interrupted while waiting for a free browser session.");
			}
			if (_closed) {
				throw new IllegalStateException("Browser session pool is closed.");
			}
		}
		_activeCount++;
	}

	/**
	 * Evicts the least-recently-used idle (not in-use) session to free a slot. Caller holds the
	 * lock. Destruction is scheduled asynchronously. Returns {@code true} if a slot was freed.
	 */
	private boolean reclaimIdleLocked() {
		Entry<S> lru = null;
		for (Entry<S> entry : _entries.values()) {
			if (entry._inUse == 0 && !entry._removed) {
				if (lru == null || entry._lastAccessNanos < lru._lastAccessNanos) {
					lru = entry;
				}
			}
		}
		if (lru == null) {
			return false;
		}
		removeLocked(lru);
		return true;
	}

	private void removeLocked(Entry<S> entry) {
		entry._removed = true;
		_entries.remove(entry._key);
		_activeCount--;
		_slotFreed.signalAll();
		destroyAsync(entry._session);
	}

	private void release(Entry<S> entry, boolean discard) {
		_lock.lock();
		try {
			entry._inUse--;
			entry._lastAccessNanos = _clock.getAsLong();
			if ((discard || entry._broken || _closed) && entry._inUse <= 0 && !entry._removed) {
				removeLocked(entry);
			}
		} finally {
			_lock.unlock();
		}
	}

	private void sweepIdleSafely() {
		try {
			sweepIdle();
		} catch (RuntimeException ex) {
			Logger.warn("Browser session idle sweep failed.", ex, BrowserSessionPool.class);
		}
	}

	/** Closes every session idle beyond the idle timeout. Package-visible for tests. */
	void sweepIdle() {
		_lock.lock();
		try {
			long now = _clock.getAsLong();
			List<Entry<S>> expired = new ArrayList<>();
			for (Entry<S> entry : _entries.values()) {
				if (entry._inUse == 0 && !entry._removed
						&& now - entry._lastAccessNanos >= _idleTimeoutNanos) {
					expired.add(entry);
				}
			}
			for (Entry<S> entry : expired) {
				removeLocked(entry);
			}
		} finally {
			_lock.unlock();
		}
	}

	/** The number of live sessions (excludes in-flight reservations). Package-visible for tests. */
	int liveSessionCount() {
		_lock.lock();
		try {
			return _entries.size();
		} finally {
			_lock.unlock();
		}
	}

	private void destroyAsync(S session) {
		_maintenance.execute(() -> {
			try {
				_destroyer.destroy(session);
			} catch (RuntimeException ex) {
				Logger.warn("Failed to destroy a browser session cleanly.", ex, BrowserSessionPool.class);
			}
		});
	}

	@Override
	public void close() {
		List<Entry<S>> remaining;
		_lock.lock();
		try {
			if (_closed) {
				return;
			}
			_closed = true;
			remaining = new ArrayList<>(_entries.values());
			_entries.clear();
			_activeCount = 0;
			_slotFreed.signalAll();
		} finally {
			_lock.unlock();
		}
		// Destroy synchronously on shutdown so nothing leaks past service teardown.
		for (Entry<S> entry : remaining) {
			try {
				_destroyer.destroy(entry._session);
			} catch (RuntimeException ex) {
				Logger.warn("Failed to destroy a browser session during shutdown.", ex, BrowserSessionPool.class);
			}
		}
		_maintenance.shutdownNow();
	}
}
