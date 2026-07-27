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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
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
 * <li><b>Bounded live sessions.</b> At most {@code maxSessions} resources <em>exist</em> at once -
 * counting one whose teardown has been started but not finished (see below). The bound protects
 * memory (each live browser context costs RAM); it is <em>not</em> a throughput throttle -
 * independent sessions run fully concurrently.</li>
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
 * <b>A slot is held until the resource is really gone.</b> Removing an entry from the key map is
 * not the same as releasing the memory it stands for: closing a browser context means shutting down
 * an OS process, which takes time and is done off the caller's thread. A slot therefore stays
 * reserved from admission until the destroyer has <em>returned</em>, so an eviction-and-recreate can
 * never leave two browser processes alive under a cap of one. Freeing the slot at removal time
 * instead - which is what this pool used to do - made {@code max-concurrent-sessions} bound the
 * bookkeeping rather than the memory it exists to bound: with a cap of 1, two conversations
 * alternating tool calls each evicted the other and genuinely ran two Chromium processes at once.
 * Teardown consequently runs on its own executor rather than the maintenance thread, so several
 * slow closes proceed in parallel instead of queueing behind each other and behind the idle sweep.
 * </p>
 *
 * <p>
 * The pool is thread-safe. Resource creation and destruction happen outside the internal lock so a
 * slow browser launch or close never blocks unrelated sessions.
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

	/**
	 * Signalled whenever the capacity picture changes in a way a waiter could act on: a slot was
	 * released (a resource finished being destroyed) or a session became idle and so evictable.
	 */
	private final Condition _slotFreed = _lock.newCondition();

	private final Map<Object, Entry<S>> _entries = new HashMap<>();

	/**
	 * The quantity bounded by {@link #_maxSessions}: every resource that exists or is about to.
	 *
	 * <p>
	 * That is registered sessions, plus in-flight creation reservations, plus sessions already removed
	 * from {@link #_entries} whose destroyer has not returned yet. The last group is the point: a
	 * browser context that is being closed still owns its process and its memory, so its slot must not
	 * be handed to a new session. Incremented by {@link #reserveSlot()} and released only by
	 * {@link #releaseSlot()}, which runs after the destroyer completes.
	 * </p>
	 */
	private int _activeCount;

	/**
	 * How many resources have been handed to the destroyer but have not come back yet.
	 *
	 * <p>
	 * Part of {@link #_activeCount}. Read by {@link #reserveSlot()} to start at most one eviction at a
	 * time: a teardown in flight will free a slot on its own, so evicting another session would just
	 * destroy a usable browser.
	 * </p>
	 */
	private int _pendingTeardownCount;

	private boolean _closed;

	/** Runs the periodic idle sweep only, so it cannot be delayed behind a slow teardown. */
	private final ScheduledExecutorService _maintenance;

	/**
	 * Runs resource destruction, off both the caller's thread and the sweeper's.
	 *
	 * <p>
	 * Its own executor because a teardown holds a slot until it returns: queueing several slow closes
	 * onto one thread would serialise the very waits that admission depends on. Threads are created on
	 * demand and reaped when idle, and their number is naturally bounded by the session cap.
	 * </p>
	 */
	private final ExecutorService _teardown;

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
		_teardown = Executors.newCachedThreadPool(daemon("browser-session-teardown"));
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
			// Nothing was created, so the reservation is released immediately.
			releaseSlot();
			throw ex;
		}

		_lock.lock();
		try {
			Entry<S> race = _entries.get(key);
			if (race != null && !race._removed) {
				// A concurrent caller created the same key; keep theirs, drop ours. Our slot stays
				// reserved until the surplus resource has actually been destroyed.
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

	/**
	 * Reserves one of the {@link #_maxSessions} slots, evicting an idle session and/or waiting as
	 * needed. Caller holds the lock.
	 *
	 * <p>
	 * At most one eviction is started at a time. A teardown already in flight is going to free a slot
	 * the moment its destroyer returns, so evicting a second session as well would destroy a perfectly
	 * usable browser for nothing - and with a cap of one, would destroy the pool's only session on
	 * every admission.
	 * </p>
	 */
	private void reserveSlot() {
		long deadline = _clock.getAsLong() + _acquireTimeoutNanos;
		while (_activeCount >= _maxSessions) {
			if (_pendingTeardownCount == 0) {
				reclaimIdleLocked();
			}
			long remaining = deadline - _clock.getAsLong();
			if (remaining <= 0) {
				throw new BrowserBusyException(
					"The browser is busy: all " + _maxSessions + " concurrent sessions are in use. Retry shortly.");
			}
			try {
				// Woken by releaseSlot() as soon as a resource is really gone, and by release() as
				// soon as a session becomes idle and therefore evictable.
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
	 * Starts evicting the least-recently-used idle (not in-use) session. Caller holds the lock.
	 *
	 * <p>
	 * Returns {@code true} when an eviction was <em>started</em> - not when a slot became free. The
	 * evicted resource still holds its slot while it is being destroyed, so the caller must go back to
	 * waiting on {@link #_slotFreed}; this is exactly the wait that keeps a cap of one from ever
	 * running two browsers at once.
	 * </p>
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

	/**
	 * Unregisters {@code entry} and starts destroying its resource. Caller holds the lock.
	 *
	 * <p>
	 * The slot is deliberately <em>not</em> released here: the resource still exists until its
	 * destroyer returns. {@link #releaseSlot()} does that.
	 * </p>
	 */
	private void removeLocked(Entry<S> entry) {
		entry._removed = true;
		_entries.remove(entry._key);
		destroyAsync(entry._session);
	}

	private void release(Entry<S> entry, boolean discard) {
		_lock.lock();
		try {
			entry._inUse--;
			entry._lastAccessNanos = _clock.getAsLong();
			if ((discard || entry._broken || _closed) && entry._inUse <= 0 && !entry._removed) {
				removeLocked(entry);
			} else if (entry._inUse <= 0) {
				// The session just became idle, so it is now evictable: wake anyone waiting for a
				// slot instead of letting them sit out their whole acquire timeout.
				_slotFreed.signalAll();
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

	/**
	 * The number of registered (reusable) sessions. Excludes in-flight reservations and sessions whose
	 * teardown has been started - use {@link #liveResourceCount()} for what the cap actually bounds.
	 * Package-visible for tests.
	 */
	int liveSessionCount() {
		_lock.lock();
		try {
			return _entries.size();
		} finally {
			_lock.unlock();
		}
	}

	/**
	 * The number of resources that <em>exist</em>, which is what {@code maxSessions} bounds: registered
	 * sessions, plus in-flight creations, plus sessions still being destroyed. Never exceeds the cap.
	 * Package-visible for tests.
	 */
	int liveResourceCount() {
		_lock.lock();
		try {
			return _activeCount;
		} finally {
			_lock.unlock();
		}
	}

	/**
	 * Destroys {@code session} off the caller's thread, releasing its slot only once the destroyer has
	 * returned. Caller holds the lock.
	 */
	private void destroyAsync(S session) {
		_pendingTeardownCount++;
		try {
			_teardown.execute(() -> {
				try {
					_destroyer.destroy(session);
				} catch (RuntimeException ex) {
					Logger.warn("Failed to destroy a browser session cleanly.", ex, BrowserSessionPool.class);
				} finally {
					finishTeardown();
				}
			});
		} catch (RejectedExecutionException shuttingDown) {
			// The pool is closing and will destroy what remains synchronously; do not leak the slot.
			_pendingTeardownCount--;
			_activeCount = Math.max(0, _activeCount - 1);
			_slotFreed.signalAll();
		}
	}

	/** Marks one teardown as complete and frees the slot its resource occupied. */
	private void finishTeardown() {
		_lock.lock();
		try {
			_pendingTeardownCount = Math.max(0, _pendingTeardownCount - 1);
			_activeCount = Math.max(0, _activeCount - 1);
			_slotFreed.signalAll();
		} finally {
			_lock.unlock();
		}
	}

	/** Frees a slot that was reserved but never turned into a resource (a failed creation). */
	private void releaseSlot() {
		_lock.lock();
		try {
			_activeCount = Math.max(0, _activeCount - 1);
			_slotFreed.signalAll();
		} finally {
			_lock.unlock();
		}
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
			_pendingTeardownCount = 0;
			_slotFreed.signalAll();
		} finally {
			_lock.unlock();
		}
		// Destroy synchronously on shutdown so nothing leaks past service teardown. In-flight
		// teardowns run to completion on their own threads; their slot accounting is moot now that
		// the pool is closed and admits nothing.
		for (Entry<S> entry : remaining) {
			try {
				_destroyer.destroy(entry._session);
			} catch (RuntimeException ex) {
				Logger.warn("Failed to destroy a browser session during shutdown.", ex, BrowserSessionPool.class);
			}
		}
		_maintenance.shutdownNow();
		_teardown.shutdown();
	}
}
