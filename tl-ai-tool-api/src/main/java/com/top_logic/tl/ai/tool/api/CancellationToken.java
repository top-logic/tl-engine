/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.concurrent.CancellationException;

import com.top_logic.basic.thread.CancelScope;

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
 * <p>Cancelling never interrupts a thread at an arbitrary point: a blocking call that only an
 * interrupt can end early runs through {@link #whileBlocked(BlockingCall)}, see {@link CancelScope}
 * for why. The token signals its cancellation with a {@link CancellationException}.</p>
 *
 * @see ToolExecutionContext#cancellationToken()
 */
public class CancellationToken extends CancelScope {

	/** Token that can never be cancelled. For use in test/internal contexts. */
	public static final CancellationToken UNCANCELLABLE = new CancellationToken() {
		@Override
		public void cancel() {
			// Ignored — this token cannot be cancelled.
		}
	};

	@Override
	protected String cancelledMessage() {
		return "Tool execution cancelled.";
	}

	/**
	 * A {@link CancellationException}, the cancellation tools expect.
	 */
	@Override
	protected RuntimeException createCancelledException(String message, Throwable cause) {
		CancellationException result = new CancellationException(message);
		if (cause != null) {
			result.initCause(cause);
		}
		return result;
	}

	/**
	 * Runs a blocking call that only a thread interrupt can end early, so that cancelling this token
	 * ends it.
	 *
	 * <p>
	 * {@link #interruptibly(BlockingCall)}: the call must not touch the knowledge base.
	 * </p>
	 *
	 * @throws CancellationException
	 *         If this token is cancelled before or while {@code call} runs, unless the call
	 *         completed normally anyway.
	 * @throws InterruptedException
	 *         If somebody else interrupted the thread; that interrupt is not this token's to clear.
	 */
	public <T, E extends Exception> T whileBlocked(BlockingCall<T, E> call) throws E, InterruptedException {
		return interruptibly(call);
	}

	/**
	 * Sleeps for {@code durationMs}, re-checking this token every {@code pollMs} so a
	 * cancellation (or thread interrupt) ends the wait promptly instead of sitting out the
	 * full duration.
	 *
	 * <p>
	 * The sleep is no {@link #interruptibly(BlockingCall) interruptible section}: the token never
	 * interrupts the sleeping thread, so the interrupt flag this method may leave set is always a
	 * foreign one.
	 * </p>
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
