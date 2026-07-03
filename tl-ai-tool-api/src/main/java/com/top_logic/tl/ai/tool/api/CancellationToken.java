/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.concurrent.CancellationException;

/**
 * Cooperative cancellation token for long-running tool operations.
 *
 * <p>Tools can check {@link #isCancelled()} periodically during execution,
 * or call {@link #checkCancelled()} to throw if cancellation was requested.
 * The agent loop calls {@link #cancel()} when the user cancels the run.</p>
 *
 * <p>Thread-safe: the cancelled flag is volatile.</p>
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

	/** Whether cancellation has been requested. */
	public boolean isCancelled() {
		return _cancelled;
	}

	/** Requests cancellation of the associated agent run. */
	public void cancel() {
		_cancelled = true;
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
