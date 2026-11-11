/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.AbortExecutionException;

/**
 * Cooperative cancellation of a piece of work that may run on one or more threads.
 *
 * <p>
 * The work checks {@link #isCancelled()} or {@link #checkCancelled()} at points where it can stop
 * cleanly. Whoever owns the work calls {@link #cancel()}.
 * </p>
 *
 * <p>
 * <b>Cancelling never interrupts a thread at an arbitrary point.</b> An interrupt that reaches a
 * thread while it reads a file through an NIO channel (an {@link java.nio.channels.InterruptibleChannel})
 * closes that channel for every thread of the process - for H2, the database file: the store panics
 * and the whole application loses its database. A blocking call that only an interrupt can end early
 * (a sleep, an HTTP request, waiting for a future or a latch) is therefore run as an interruptible
 * section with {@link #interruptibly(BlockingCall)}: {@link #cancel()} interrupts the thread while,
 * and only while, it is inside that section. Entering a section in a cancelled scope fails at once,
 * and leaving a section takes back an interrupt the scope caused, under the same lock that
 * {@link #cancel()} holds, so the interrupt never reaches the code that follows - which may well read
 * the database.
 * </p>
 *
 * <p>
 * Blocking code that cannot take a {@link CancelScope} as an argument finds the scope of its thread
 * through {@link #current()}; the owner of the work binds it with {@link #bind(CancelScope)}. The
 * binding is a {@link ThreadLocalBinding}, so a {@link BindingSnapshot} carries it onto worker
 * threads.
 * {@link #runInterruptibly(BlockingCall)} and {@link #sleep(long)} run a blocking call through the
 * current scope, and plainly when the thread has none.
 * </p>
 *
 * <p>
 * A cancelled scope signals the cancellation with the exception of
 * {@link #createCancelledException(String, Throwable)}: an {@link AbortExecutionException} unless a
 * subclass chooses otherwise.
 * </p>
 *
 * <p>
 * Thread-safe: the cancelled flag is volatile, listener and section bookkeeping is synchronized on
 * the scope.
 * </p>
 */
public class CancelScope {

	private static final ThreadLocalBinding<CancelScope> CURRENT = new ThreadLocalBinding<>();

	private volatile boolean _cancelled;

	/** Listeners to notify on {@link #cancel()}; guarded by {@code this}. */
	private final List<Runnable> _listeners = new ArrayList<>(2);

	/** Threads inside {@link #interruptibly(BlockingCall)}; guarded by {@code this}. */
	private final List<Section> _sections = new ArrayList<>(2);

	/**
	 * The scope bound to the current thread, or {@code null} if the thread runs in none.
	 *
	 * @see #bind(CancelScope)
	 */
	public static CancelScope current() {
		return CURRENT.get();
	}

	/**
	 * Binds the given scope to the current thread.
	 *
	 * @param scope
	 *        The scope to bind; {@code null} binds none.
	 * @return A handle whose {@link ThreadLocalBinding.Handle#close() close()} restores the scope
	 *         bound before (or its absence).
	 */
	public static ThreadLocalBinding<CancelScope>.Handle bind(CancelScope scope) {
		return CURRENT.bind(scope);
	}

	/**
	 * Whether the current thread runs in a {@link #current() scope} that has been cancelled.
	 */
	public static boolean isCurrentCancelled() {
		CancelScope scope = CURRENT.get();
		return scope != null && scope.isCancelled();
	}

	/**
	 * Runs a blocking call as an {@link #interruptibly(BlockingCall) interruptible section} of the
	 * {@link #current() current scope}; without a scope, the call simply runs.
	 *
	 * @see #interruptibly(BlockingCall)
	 */
	public static <T, E extends Exception> T runInterruptibly(BlockingCall<T, E> call) throws E, InterruptedException {
		CancelScope scope = CURRENT.get();
		return scope == null ? call.call() : scope.interruptibly(call);
	}

	/**
	 * Sleeps in the {@link #current() current scope}, so that cancelling the scope ends the sleep.
	 *
	 * <p>
	 * Without a scope this is {@link Thread#sleep(long)}.
	 * </p>
	 *
	 * @param millis
	 *        The time to sleep in milliseconds.
	 * @throws InterruptedException
	 *         If somebody other than the scope interrupted the thread.
	 * @throws RuntimeException
	 *         The scope's {@link #createCancelledException(String, Throwable) cancellation} if the
	 *         scope is cancelled before or during the sleep.
	 */
	public static void sleep(long millis) throws InterruptedException {
		runInterruptibly(() -> {
			Thread.sleep(millis);
			return null;
		});
	}

	/** Whether cancellation has been requested. */
	public boolean isCancelled() {
		return _cancelled;
	}

	/**
	 * Throws the {@link #createCancelledException(String, Throwable) cancellation} of this scope if
	 * cancellation has been requested.
	 */
	public void checkCancelled() {
		if (_cancelled) {
			throw createCancelledException(cancelledMessage(), null);
		}
	}

	/**
	 * The message of the exception {@link #checkCancelled()} throws.
	 */
	protected String cancelledMessage() {
		return "Execution cancelled.";
	}

	/**
	 * Creates the exception that signals the cancellation of this scope.
	 *
	 * @param message
	 *        What was cancelled.
	 * @param cause
	 *        The failure of an {@link #interruptibly(BlockingCall) interruptible section} that the
	 *        cancellation ended, or {@code null}.
	 * @return An {@link AbortExecutionException}. A subclass returns the exception its callers
	 *         expect.
	 */
	protected RuntimeException createCancelledException(String message, Throwable cause) {
		return new AbortExecutionException(message, cause);
	}

	/**
	 * Requests cancellation and notifies all registered {@link #onCancel(Runnable) listeners}
	 * exactly once.
	 *
	 * <p>
	 * Idempotent: a second call is a no-op. A listener that throws does not prevent the others from
	 * running.
	 * </p>
	 *
	 * <p>
	 * Interrupts the threads that are inside {@link #interruptibly(BlockingCall)} at this moment, and
	 * no other thread.
	 * </p>
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
			// Under the lock: a thread leaves a section only by taking it, so each of these threads
			// is at most between its blocking call and that lock - where it reads nothing - when the
			// interrupt reaches it, and takes the interrupt back there.
			for (Section section : _sections) {
				section.interrupt();
			}
		}
		for (Runnable listener : toNotify) {
			runQuietly(listener);
		}
	}

	/**
	 * Registers a listener invoked when this scope is {@link #cancel() cancelled}.
	 *
	 * <p>
	 * If the scope is already cancelled the listener runs immediately (on the calling thread).
	 * Otherwise it fires on the thread that later calls {@link #cancel()}. Each listener is invoked
	 * at most once.
	 * </p>
	 *
	 * @param listener
	 *        The callback to run on cancellation; must not be {@code null}.
	 * @return A handle whose {@link Registration#close()} removes the listener if it has not yet
	 *         fired; closing is idempotent and safe to call from a {@code finally} block.
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
		return () -> {
			/* nothing to remove */ };
	}

	private static void runQuietly(Runnable listener) {
		try {
			listener.run();
		} catch (RuntimeException ex) {
			// A misbehaving listener must not break cancellation of the others.
		}
	}

	/**
	 * Runs a blocking call that only a thread interrupt can end early, as an interruptible section
	 * of this scope, so that cancelling this scope ends it.
	 *
	 * <p>
	 * While {@code call} runs, {@link #cancel()} interrupts the calling thread; before it starts and
	 * after it ended, it does not. An interrupt this scope caused is cleared before this method
	 * returns, so the thread never carries it into the code that follows.
	 * </p>
	 *
	 * <p>
	 * <b>The call must not touch the knowledge base or any other NIO file channel</b>: an interrupt
	 * that reaches such a read closes the channel for good. Wrap the wait alone - the
	 * {@link Thread#sleep(long)}, the {@code HttpClient.send}, the {@link Future#get()} - not the
	 * code around it.
	 * </p>
	 *
	 * @param call
	 *        The blocking call.
	 * @return What {@code call} returned.
	 * @throws RuntimeException
	 *         The {@link #createCancelledException(String, Throwable) cancellation} of this scope if
	 *         it is cancelled before or while {@code call} runs, unless the call completed normally
	 *         anyway.
	 * @throws InterruptedException
	 *         If somebody other than this scope interrupted the thread; that interrupt is not this
	 *         scope's to clear.
	 * @throws E
	 *         What {@code call} throws, while this scope is not cancelled.
	 */
	public <T, E extends Exception> T interruptibly(BlockingCall<T, E> call) throws E, InterruptedException {
		Section section = enter();
		Throwable failure = null;
		try {
			return call.call();
		} catch (Throwable ex) {
			failure = ex;
			throw ex;
		} finally {
			leave(section, failure);
		}
	}

	/**
	 * Waits for a future of another thread, so that cancelling this scope ends the wait.
	 *
	 * <p>
	 * {@link #interruptibly(BlockingCall)} for {@link Future#get(long, TimeUnit)}. The future itself
	 * is left alone: the work it stands for goes on, or is cancelled by its owner.
	 * </p>
	 *
	 * @throws RuntimeException
	 *         The {@link #createCancelledException(String, Throwable) cancellation} of this scope if
	 *         it is cancelled before or during the wait, unless the future completed anyway.
	 */
	public <T> T await(Future<T> future, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		Section section = enter();
		Throwable failure = null;
		try {
			return future.get(timeout, unit);
		} catch (Throwable ex) {
			failure = ex;
			throw ex;
		} finally {
			leave(section, failure);
		}
	}

	private Section enter() {
		Section section = new Section(Thread.currentThread());
		synchronized (this) {
			checkCancelled();
			_sections.add(section);
		}
		return section;
	}

	/**
	 * Ends an interruptible section: clears the interrupt this scope caused, and turns the call's
	 * failure into the {@link #createCancelledException(String, Throwable) cancellation} when that
	 * interrupt is what ended it.
	 */
	private void leave(Section section, Throwable failure) {
		boolean byUs;
		synchronized (this) {
			_sections.remove(section);
			byUs = section.isInterrupted();
		}
		if (byUs) {
			// The interrupt was this scope's: take it back. A foreign interrupt that came at the same
			// time is lost with it, which only means the call ended for the reason it did.
			Thread.interrupted();
			if (failure != null) {
				throw createCancelledException("Cancelled while blocked.", failure);
			}
		}
	}

	/** Handle returned by {@link CancelScope#onCancel(Runnable)} to unregister a listener. */
	@FunctionalInterface
	public interface Registration extends AutoCloseable {
		/** Removes the associated listener if it has not yet fired. Idempotent. */
		@Override
		void close();
	}

	/**
	 * A blocking call for {@link CancelScope#interruptibly(BlockingCall)}.
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

	/** A thread inside {@link #interruptibly(BlockingCall)}; guarded by the scope. */
	private static final class Section {

		private final Thread _thread;

		private boolean _interrupted;

		Section(Thread thread) {
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

}
