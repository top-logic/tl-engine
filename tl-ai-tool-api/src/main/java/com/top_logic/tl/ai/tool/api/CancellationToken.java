/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
 * <p><b>Cancelling never interrupts a thread at an arbitrary point.</b> An interrupt that reaches a
 * thread while it reads a file through an NIO channel closes that channel for every thread of the
 * process - for H2, the database file, so the whole application loses its database. A blocking call
 * that only an interrupt can end early (an HTTP request, waiting for a future) is therefore run with
 * {@link #whileBlocked(BlockingCall)}: a cancellation interrupts the thread while, and only while, it
 * is inside that call, and the interrupt is gone again when the call returns.</p>
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

	/** Threads inside {@link #whileBlocked(BlockingCall)}; guarded by {@code this}. */
	private final List<Blocked> _blocked = new ArrayList<>(2);

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
	 *
	 * <p>Interrupts the threads that are inside {@link #whileBlocked(BlockingCall)} at this moment,
	 * and no other thread.</p>
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
			// Under the lock: a thread leaves whileBlocked only by taking it, so each of these
			// threads is at most between its blocking call and that lock - where it reads nothing -
			// when the interrupt reaches it, and takes the interrupt back there.
			for (Blocked blocked : _blocked) {
				blocked.interrupt();
			}
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
	 * Runs a blocking call that only a thread interrupt can end early, so that cancelling this token
	 * ends it.
	 *
	 * <p>
	 * While {@code call} runs, {@link #cancel()} interrupts the calling thread; before it starts and
	 * after it ended, it does not. An interrupt this token caused is cleared before this method
	 * returns, so the thread never carries it into the code that follows - which may well read the
	 * database.
	 * </p>
	 *
	 * <p>
	 * <b>The call must not touch the knowledge base or any other NIO file channel</b>: an interrupt
	 * that reaches such a read closes the channel for good. Wrap the wait alone - the
	 * {@code HttpClient.send}, the {@link Future#get()} - not the code around it.
	 * </p>
	 *
	 * @param call
	 *        The blocking call.
	 * @return What {@code call} returned.
	 * @throws CancellationException
	 *         If this token is cancelled before or while {@code call} runs, unless the call
	 *         completed normally anyway.
	 * @throws InterruptedException
	 *         If somebody else interrupted the thread; that interrupt is not this token's to clear.
	 * @throws E
	 *         What {@code call} throws, while this token is not cancelled.
	 */
	public <T, E extends Exception> T whileBlocked(BlockingCall<T, E> call) throws E, InterruptedException {
		Blocked blocked = enterBlocking();
		Throwable failure = null;
		try {
			return call.call();
		} catch (Throwable ex) {
			failure = ex;
			throw ex;
		} finally {
			leaveBlocking(blocked, failure);
		}
	}

	/**
	 * Waits for a future of another thread, so that cancelling this token ends the wait.
	 *
	 * <p>
	 * {@link #whileBlocked(BlockingCall)} for {@link Future#get(long, TimeUnit)}. The future itself
	 * is left alone: the work it stands for goes on, or is cancelled by its owner.
	 * </p>
	 *
	 * @throws CancellationException
	 *         If this token is cancelled before or during the wait, unless the future completed
	 *         anyway.
	 */
	public <T> T await(Future<T> future, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		Blocked blocked = enterBlocking();
		Throwable failure = null;
		try {
			return future.get(timeout, unit);
		} catch (Throwable ex) {
			failure = ex;
			throw ex;
		} finally {
			leaveBlocking(blocked, failure);
		}
	}

	private Blocked enterBlocking() {
		Blocked blocked = new Blocked(Thread.currentThread());
		synchronized (this) {
			checkCancelled();
			_blocked.add(blocked);
		}
		return blocked;
	}

	/**
	 * Ends a blocking call: clears the interrupt this token caused, and turns the call's failure
	 * into a {@link CancellationException} when that interrupt is what ended it.
	 */
	private void leaveBlocking(Blocked blocked, Throwable failure) {
		boolean byUs;
		synchronized (this) {
			_blocked.remove(blocked);
			byUs = blocked.isInterrupted();
		}
		if (byUs) {
			// The interrupt was this token's: take it back. A foreign interrupt that came at the same
			// time is lost with it, which only means the call ended for the reason it did.
			Thread.interrupted();
			if (failure != null) {
				CancellationException cancelled = new CancellationException("Cancelled while blocked.");
				cancelled.initCause(failure);
				throw cancelled;
			}
		}
	}

	/**
	 * A blocking call for {@link CancellationToken#whileBlocked(BlockingCall)}.
	 *
	 * @param <T>
	 *        What the call returns.
	 * @param <E>
	 *        What the call throws besides {@link InterruptedException}.
	 */
	@FunctionalInterface
	public interface BlockingCall<T, E extends Exception> {

		/** Runs the call. */
		T call() throws E, InterruptedException;
	}

	/** A thread inside {@link #whileBlocked(BlockingCall)}; guarded by the token. */
	private static final class Blocked {

		private final Thread _thread;

		private boolean _interrupted;

		Blocked(Thread thread) {
			_thread = thread;
		}

		void interrupt() {
			_interrupted = true;
			_thread.interrupt();
		}

		boolean isInterrupted() {
			return _interrupted;
		}
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
