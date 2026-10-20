/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Enters the {@link Interaction} of a browser window from a background thread, waiting for it no
 * longer than the caller can afford.
 *
 * <p>
 * The engine offers only {@link ReactWindowRegistry#beginInteraction()}, which waits for as long as
 * the browser command holding the window takes. A request thread may wait that long; an agent
 * thread may not. A browser command can itself be waiting for the agent - a script that runs an
 * agent synchronously from a button, whose tools run on worker threads - and an agent tool that then
 * waited for that command's interaction would never return.
 * </p>
 *
 * <p>
 * The interaction is therefore entered on a thread of this class, and the caller waits for that
 * thread with a timeout. When the time is up first, the work is called off: the thread still enters
 * the interaction once the command releases it, sees the work was called off, and leaves without
 * doing it. Work that has started is waited for to the end, so the caller never reports as not done
 * what is in fact done.
 * </p>
 *
 * <p>
 * <b>TEMPORARY FORK.</b> <b>Upstream target:</b> an interaction that can be begun with a timeout in
 * {@link ReactWindowRegistry}; then this class goes and its callers enter the interaction on their
 * own thread, as up to engine 8.0.0-alpha8 they took the request lock.
 * </p>
 */
final class WindowInteraction implements AutoCloseable {

	private final ExecutorService _threads = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "tl-chat-window-interaction");
		thread.setDaemon(true);
		return thread;
	});

	/**
	 * Runs {@code work} in the given window's interaction and sub-session, if that interaction can be
	 * begun within {@code timeoutMillis}.
	 *
	 * @param registry
	 *        The registry of the session the window belongs to.
	 * @param subSession
	 *        The window's sub-session, in which {@code work} runs.
	 * @param timeoutMillis
	 *        How long to wait for a browser command that holds the interaction.
	 * @param work
	 *        What to do in the window. Runs on a thread of this class; an exception it throws is
	 *        thrown here.
	 * @return Whether {@code work} ran. <code>false</code> when the interaction stayed taken, or the
	 *         calling thread was interrupted, before it began; {@code work} then never runs.
	 */
	boolean run(ReactWindowRegistry registry, TLSubSessionContext subSession, long timeoutMillis,
			Runnable work) {
		Job job = new Job(registry, subSession, work);
		Future<?> done = _threads.submit(job);
		try {
			done.get(timeoutMillis, TimeUnit.MILLISECONDS);
			return true;
		} catch (TimeoutException ex) {
			return job.callOff() ? false : awaitStarted(done);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return job.callOff() ? false : awaitStarted(done);
		} catch (ExecutionException ex) {
			throw rethrow(ex);
		}
	}

	/**
	 * Waits for a job that has begun its work, which no longer depends on a browser command.
	 */
	private static boolean awaitStarted(Future<?> done) {
		boolean interrupted = false;
		try {
			while (true) {
				try {
					done.get();
					return true;
				} catch (InterruptedException ex) {
					interrupted = true;
				} catch (ExecutionException ex) {
					throw rethrow(ex);
				}
			}
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private static RuntimeException rethrow(ExecutionException ex) {
		Throwable cause = ex.getCause();
		if (cause instanceof RuntimeException runtime) {
			throw runtime;
		}
		if (cause instanceof Error error) {
			throw error;
		}
		throw new IllegalStateException(cause);
	}

	/**
	 * Stops the threads. A thread still waiting for an interaction finishes that wait and then ends.
	 */
	@Override
	public void close() {
		_threads.shutdown();
	}

	/**
	 * One piece of work for one window, which either the waiting caller calls off or the thread of
	 * this class begins - whichever comes first, and never both.
	 */
	private static final class Job implements Runnable {

		private static final int PENDING = 0;

		private static final int STARTED = 1;

		private static final int CALLED_OFF = 2;

		private final AtomicInteger _state = new AtomicInteger(PENDING);

		private final ReactWindowRegistry _registry;

		private final TLSubSessionContext _subSession;

		private final Runnable _work;

		Job(ReactWindowRegistry registry, TLSubSessionContext subSession, Runnable work) {
			_registry = registry;
			_subSession = subSession;
			_work = work;
		}

		@Override
		public void run() {
			ThreadContextManager.inContext(_subSession, () -> {
				try (Interaction interaction = _registry.beginInteraction()) {
					if (_state.compareAndSet(PENDING, STARTED)) {
						_work.run();
					}
				}
			});
		}

		/**
		 * Calls the work off, unless it has begun.
		 *
		 * @return Whether the work is called off and will never run.
		 */
		boolean callOff() {
			return _state.compareAndSet(PENDING, CALLED_OFF);
		}
	}
}
