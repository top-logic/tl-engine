/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Cooperative cancellation token for long-running tool operations.
 *
 * <p>Tools can check {@link #isCancelled()} periodically during execution,
 * or call {@link #checkCancelled()} to throw if cancellation was requested.
 * The agent loop calls {@link #cancel()} when the user cancels the run.</p>
 *
 * <p>Tools that <em>block</em> (rather than poll) - most importantly the human-in-the-loop
 * ask-user tool, which parks on a future until the user answers - cannot observe the flag
 * while parked. They register an {@link #onCancel(Runnable) cancellation listener} that fires
 * when {@link #cancel()} is called, so they can unblock promptly (e.g. by completing the
 * pending future) instead of hanging until a timeout.</p>
 *
 * <p>Thread-safe: the cancelled flag is volatile and listener bookkeeping is synchronized.</p>
 *
 * @see ToolExecutionContext#cancellationToken()
 */
public class CancellationToken {

	/** Token that can never be cancelled. For use in test/internal contexts. */
	public static final CancellationToken UNCANCELLABLE = new CancellationToken() {
		@Override
		public void cancel() {
			// Ignored — this token cannot be cancelled.
		}
	};

	private volatile boolean _cancelled;

	/** Listeners to notify on {@link #cancel()}; guarded by {@code this}. */
	private final List<Runnable> _listeners = new ArrayList<>(2);

	/** Whether cancellation has been requested. */
	public boolean isCancelled() {
		return _cancelled;
	}

	/**
	 * Requests cancellation of the associated agent run and notifies all registered
	 * {@link #onCancel(Runnable) listeners} exactly once.
	 *
	 * <p>Idempotent: a second call is a no-op. A listener that throws does not prevent the
	 * others from running.</p>
	 */
	public void cancel() {
		List<Runnable> toNotify;
		synchronized (this) {
			if (_cancelled) {
				return;
			}
			_cancelled = true;
			toNotify = new ArrayList<>(_listeners);
			_listeners.clear();
		}
		for (Runnable listener : toNotify) {
			runQuietly(listener);
		}
	}

	/**
	 * Registers a listener invoked when this token is {@link #cancel() cancelled}.
	 *
	 * <p>If the token is already cancelled the listener runs immediately (on the calling
	 * thread). Otherwise it fires on the thread that later calls {@link #cancel()}. Each
	 * listener is invoked at most once.</p>
	 *
	 * @param listener
	 *        the callback to run on cancellation; must not be {@code null}
	 * @return a handle whose {@link Registration#close()} removes the listener if it has not
	 *         yet fired; closing is idempotent and safe to call from a {@code finally} block
	 */
	public Registration onCancel(Runnable listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener must not be null");
		}
		synchronized (this) {
			if (!_cancelled) {
				_listeners.add(listener);
				return () -> {
					synchronized (this) {
						_listeners.remove(listener);
					}
				};
			}
		}
		// Already cancelled: fire now, nothing to unregister.
		runQuietly(listener);
		return () -> { /* nothing to remove */ };
	}

	private static void runQuietly(Runnable listener) {
		try {
			listener.run();
		} catch (RuntimeException ex) {
			// A misbehaving listener must not break cancellation of the others.
		}
	}

	/** Handle returned by {@link CancellationToken#onCancel(Runnable)} to unregister a listener. */
	@FunctionalInterface
	public interface Registration extends AutoCloseable {
		/** Removes the associated listener if it has not yet fired. Idempotent. */
		@Override
		void close();
	}

	/**
	 * Throws {@link CancellationException} if cancellation has been requested.
	 *
	 * @throws CancellationException if {@link #isCancelled()} is {@code true}
	 */
	public void checkCancelled() {
		if (_cancelled) {
			throw new CancellationException("Tool execution cancelled.");
		}
	}

	/**
	 * Sleeps for {@code durationMs}, re-checking this token every {@code pollMs} so a
	 * cancellation (or thread interrupt) ends the wait promptly instead of sitting out the
	 * full duration.
	 *
	 * @param durationMs total time to sleep, in milliseconds
	 * @param pollMs     how often to re-check {@link #isCancelled()} while waiting
	 * @return {@code true} if the wait ended early due to cancellation or interruption (the
	 *         calling thread's interrupt flag is restored in that case — check
	 *         {@link Thread#isInterrupted()} to tell the two apart), {@code false} if the full
	 *         duration elapsed normally
	 */
	public boolean sleepCancellably(long durationMs, long pollMs) {
		long deadline = System.nanoTime() + durationMs * 1_000_000L;
		while (true) {
			if (isCancelled()) {
				return true;
			}
			long remainingMs = (deadline - System.nanoTime()) / 1_000_000L;
			if (remainingMs <= 0) {
				return false;
			}
			try {
				Thread.sleep(Math.min(pollMs, remainingMs));
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				return true;
			}
		}
	}
}
