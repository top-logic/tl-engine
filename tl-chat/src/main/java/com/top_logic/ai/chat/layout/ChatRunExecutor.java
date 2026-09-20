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
 * As many as arrive, unless an administrator says otherwise. That is what
 * {@code Executors.newCachedThreadPool()} in {@code ChatProcessor} did, and what an installation
 * upgrading past Gitea #343 must still find: this product is deployed into applications with a
 * thousand users, and any ceiling it picked for them would be wrong for most. So
 * {@link RunLimitSettings#chatWorkers()} is {@code 0} until somebody names a number.
 * </p>
 *
 * <p>
 * When a number is named, a send that finds every worker busy <b>waits</b>: the queue is of no
 * fixed length unless {@link RunLimitSettings#chatQueue()} gives it one. Somebody who asked a
 * question wants the answer, late rather than not at all, and what waits is one task object rather
 * than a run. An installation that would rather say "not now" gives the queue a length it accepts,
 * or sets it to {@link RunLimitSettings#NO_QUEUE} for no waiting room at all; a send that arrives
 * at a full queue is then refused with {@link NoRoom}, which the caller turns into a sentence the
 * user can act on.
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
	 * Runs the given task on a chat worker, or queues it for the next free one.
	 *
	 * @return The task's future, for cancellation.
	 * @throws NoRoom
	 *         If every worker is busy and the queue has no room either - which only an installation
	 *         that configured both a ceiling and a bounded queue can reach. The caller answers the
	 *         user; nothing is started.
	 */
	static Future<?> submit(Runnable task) {
		try {
			return pool().submit(task);
		} catch (RejectedExecutionException ex) {
			throw new NoRoom(noRoomDescription());
		}
	}

	/**
	 * Why a send found no room, for the log.
	 *
	 * <p>
	 * Names the ceilings as they are configured, because that is what an administrator reading the
	 * line has to decide about.
	 * </p>
	 */
	static String noRoomDescription() {
		int queue = RunLimitSettings.chatQueue();
		return "all " + workers() + " chat workers are busy and "
			+ (queue > 0 ? "the queue of " + queue + " is full" : "this installation keeps no queue")
			+ ".";
	}

	/**
	 * Whether a send would find room right now - a free worker, or a place to wait.
	 *
	 * <p>
	 * Always {@code true} unless this installation configured a ceiling <em>and</em> bounded the
	 * queue behind it. Where it can say no, it is advisory and deliberately so: it is asked before
	 * the request has changed anything, so that the ordinary "we are full" answer costs the user
	 * nothing more than a sentence. Between this answer and the submit another request can take the
	 * last place, which {@link #submit(Runnable)} then reports - the rare path, where the user's
	 * message is already in the conversation.
	 * </p>
	 */
	static boolean hasRoom() {
		ThreadPoolExecutor pool = pool();
		if (pool.getActiveCount() < pool.getMaximumPoolSize()) {
			return true;
		}
		return pool.getQueue().remainingCapacity() > 0;
	}

	/**
	 * How many runs this installation carries at the same time; {@code 0} when it carries as many as
	 * arrive.
	 */
	static int workers() {
		int max = pool().getMaximumPoolSize();
		return max == Integer.MAX_VALUE ? 0 : max;
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
		return workers > 0 ? boundedPool(workers, RunLimitSettings.chatQueue()) : unboundedPool();
	}

	/**
	 * A worker for every send, which is what an installation that named no ceiling gets.
	 *
	 * <p>
	 * Exactly what {@code Executors.newCachedThreadPool()} builds, spelled out so that the pool
	 * stays a {@link ThreadPoolExecutor} and {@link #hasRoom()} can ask it the same questions it
	 * asks a bounded one. An idle worker is handed the next send; when there is none, a new worker
	 * is started, and one that has been idle for a minute is let go.
	 * </p>
	 */
	private static ThreadPoolExecutor unboundedPool() {
		Logger.info("Chat agent runs are not limited in number."
			+ " Set chat-workers in <run-limits> to bound them.", ChatRunExecutor.class);
		return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
			new SynchronousQueue<>(), new WorkerThreads());
	}

	/**
	 * This many runs at a time, and the waiting room the installation asked for behind them.
	 *
	 * @param queueLength
	 *        {@code 0} for a queue of no fixed length, a positive number for one of that length,
	 *        {@link RunLimitSettings#NO_QUEUE} for none at all.
	 */
	private static ThreadPoolExecutor boundedPool(int workers, int queueLength) {
		BlockingQueue<Runnable> queue;
		String waiting;
		if (queueLength == RunLimitSettings.NO_QUEUE) {
			// A SynchronousQueue hands a task straight to a waiting worker and refuses when there is
			// none, which with core == max is exactly "this many at once, and no waiting room".
			queue = new SynchronousQueue<>();
			waiting = "with no queue: a send that finds them busy is refused";
		} else if (queueLength == 0) {
			queue = new LinkedBlockingQueue<>();
			waiting = "with a queue of no fixed length: a send that finds them busy waits";
		} else {
			queue = new LinkedBlockingQueue<>(queueLength);
			waiting = "with room for " + queueLength + " waiting";
		}
		ThreadPoolExecutor pool = new ThreadPoolExecutor(workers, workers, 60L, TimeUnit.SECONDS,
			queue, new WorkerThreads());
		// The threads are idle between conversations far more often than they are busy; letting them
		// go keeps an installation that is quiet at night from holding the pool's worth of stacks.
		pool.allowCoreThreadTimeOut(true);
		Logger.info("Chat agent runs are bounded to " + workers + " at a time, " + waiting + ".",
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
