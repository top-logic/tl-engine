/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;

/**
 * The threads the chat's agent runs execute on, and how many of them there are.
 *
 * <p>
 * Bounded. It used to be {@code Executors.newCachedThreadPool()}, which starts a thread for every
 * send that arrives: an installation serving many users had no ceiling at all on how many runs it
 * carried, how much memory they held or how much they spent with the provider at the same moment
 * (Gitea #343). The bound is {@link RunLimitSettings#chatWorkers()}, and a send that finds no room
 * is {@link #REJECTED refused with a sentence the user can act on} rather than queued behind an
 * unbounded backlog.
 * </p>
 *
 * <p>
 * <b>Why this is a class and not a TopLogic service.</b> A pool has no lifecycle of its own beyond
 * the application's, nothing to inject and nothing for another component to look up; making it a
 * {@code ConfiguredManagedClass} would add a service every application has to register in order to
 * get a thread pool it cannot do without. What it does need is a <em>configured</em> size, and that
 * lives where the installation's other ceilings live - the {@code <run-limits>} section of
 * {@code LLMService} - so an administrator sets all of them in one place. What this replaces is the
 * {@code static} field in {@code ChatProcessor}, a layout class, which is where the pool did not
 * belong.
 * </p>
 */
final class ChatRunExecutor {

	/** The pool, built on first use so that the configuration is read after the services are up. */
	private static volatile ThreadPoolExecutor _pool;

	private ChatRunExecutor() {
		// Static utility.
	}

	/** What a run that found no room answers with. */
	static final class NoRoom extends RuntimeException {

		NoRoom(String message) {
			super(message);
		}
	}

	/**
	 * Runs the given task on a chat worker.
	 *
	 * @return The task's future, for cancellation.
	 * @throws NoRoom
	 *         If every worker is busy and the queue is full. The caller answers the user; nothing is
	 *         started.
	 */
	static Future<?> submit(Runnable task) {
		try {
			return pool().submit(task);
		} catch (RejectedExecutionException ex) {
			throw new NoRoom("No chat worker is free and the queue is full (" + workers()
				+ " workers, queue " + RunLimitSettings.chatQueue() + ").");
		}
	}

	/**
	 * Whether a send would find room right now.
	 *
	 * <p>
	 * Advisory, and deliberately so: it is asked before the request has changed anything, so that the
	 * ordinary "we are full" answer costs the user nothing more than a sentence. Between this answer
	 * and the submit another request can take the last worker, which {@link #submit(Runnable)} then
	 * reports - the rare path, where the user's message is already in the conversation.
	 * </p>
	 */
	static boolean hasRoom() {
		ThreadPoolExecutor pool = pool();
		if (pool.getActiveCount() < pool.getMaximumPoolSize()) {
			return true;
		}
		return pool.getQueue().remainingCapacity() > 0;
	}

	/** How many runs this installation carries at the same time. */
	static int workers() {
		return pool().getMaximumPoolSize();
	}

	/**
	 * Throws the pool away, so that the next submit builds one from the configuration as it is now.
	 *
	 * <p>
	 * <b>A test seam.</b> The size is read once, when the pool is first used, which is what an
	 * installation wants and what a test that sets a different size has to get past. Nothing in
	 * production calls it.
	 * </p>
	 */
	static void resetForTest() {
		synchronized (ChatRunExecutor.class) {
			ThreadPoolExecutor previous = _pool;
			_pool = null;
			if (previous != null) {
				previous.shutdownNow();
			}
		}
	}

	private static ThreadPoolExecutor pool() {
		ThreadPoolExecutor existing = _pool;
		if (existing != null) {
			return existing;
		}
		synchronized (ChatRunExecutor.class) {
			if (_pool == null) {
				_pool = newPool();
			}
			return _pool;
		}
	}

	private static ThreadPoolExecutor newPool() {
		int workers = RunLimitSettings.chatWorkers();
		int queueLength = RunLimitSettings.chatQueue();
		// A SynchronousQueue hands a task straight to a waiting worker and refuses when there is
		// none, which with core == max is exactly "this many at once, and no waiting room".
		BlockingQueue<Runnable> queue = queueLength > 0
			? new LinkedBlockingQueue<>(queueLength)
			: new SynchronousQueue<>();
		ThreadPoolExecutor pool = new ThreadPoolExecutor(workers, workers, 60L, TimeUnit.SECONDS,
			queue, new WorkerThreads());
		// The threads are idle between conversations far more often than they are busy; letting them
		// go keeps an installation that is quiet at night from holding the pool's worth of stacks.
		pool.allowCoreThreadTimeOut(true);
		Logger.info("Chat agent runs are bounded to " + workers + " at a time"
			+ (queueLength > 0 ? ", with room for " + queueLength + " waiting." : ", with no queue."),
			ChatRunExecutor.class);
		return pool;
	}

	/** Daemon threads, named so a thread dump says which run is which. */
	private static final class WorkerThreads implements ThreadFactory {

		private final AtomicInteger _counter = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("ChatProcessor-AI-Worker-" + _counter.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		}
	}
}
