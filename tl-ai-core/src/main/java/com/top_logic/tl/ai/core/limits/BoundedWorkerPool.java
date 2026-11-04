/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;

/**
 * A thread pool whose size and queue an administrator configures in the {@code <capacity>} of
 * {@link AgentRuntimeService}.
 *
 * <p>
 * The pool reads its ceiling and its queue length from two suppliers - typically accessors of
 * {@link RunLimitSettings} - every time it is used, and builds a new pool when they answer
 * differently from the pool in place. Tasks already accepted by the previous pool still run there;
 * the previous pool is shut down once they are done.
 * </p>
 *
 * <ul>
 * <li>A ceiling of {@code 0} builds a cached pool: an idle worker is handed the next task, without
 * one a new worker is started, and one that has been idle for a minute is let go. Nothing waits and
 * nothing is refused.</li>
 * <li>A positive ceiling builds a pool of that many workers. A task that finds every worker busy
 * waits in the queue: of no fixed length for {@link #UNBOUNDED_QUEUE}, of the given length for a
 * positive number, and none at all for {@link RunLimitSettings#NO_QUEUE}. A task arriving at a full
 * queue is refused with a {@link RejectedExecutionException}. Idle workers are let go after a
 * minute as well.</li>
 * </ul>
 *
 * <p>
 * The workers are daemon threads. A worker knows the pool it belongs to ({@link #isOwnWorker()}), so
 * that a task can tell whether waiting for another task of the same pool could wait forever - see
 * {@link #submitNested(Callable)}.
 * </p>
 */
public final class BoundedWorkerPool {

	/** Queue length for a queue of no fixed length: a task beyond the ceiling always waits. */
	public static final int UNBOUNDED_QUEUE = 0;

	/** Ceiling for a pool without one: a worker for every task. */
	public static final int NO_CEILING = 0;

	private static final long IDLE_SECONDS = 60L;

	/** The pool whose worker the current thread is, or {@code null}. */
	private static final ThreadLocal<BoundedWorkerPool> WORKER_OF = new ThreadLocal<>();

	private final Class<?> _logCategory;

	private final String _subject;

	private final String _task;

	private final String _setting;

	private final IntFunction<String> _threadName;

	private final IntSupplier _ceiling;

	private final IntSupplier _queueLength;

	private final AtomicInteger _threadCount = new AtomicInteger();

	/** The pool in place, built on first use so that the configuration is read once the services are up. */
	private volatile Built _built;

	/**
	 * Creates a {@link BoundedWorkerPool}.
	 *
	 * @param logCategory
	 *        The class the log lines about the pool's size are written for.
	 * @param subject
	 *        What runs on the pool, in plural, for the log, e.g. "Chat agent runs".
	 * @param task
	 *        One task, with its article, for the log, e.g. "a send".
	 * @param setting
	 *        The name of the {@code <capacity>} setting that bounds the pool, for the log.
	 * @param threadName
	 *        The name of the n-th thread the pool starts (counted from {@code 1}).
	 * @param ceiling
	 *        How many tasks run at once; {@link #NO_CEILING} for no ceiling.
	 * @param queueLength
	 *        How many tasks may wait beyond the ceiling: {@link #UNBOUNDED_QUEUE}, a positive length,
	 *        or {@link RunLimitSettings#NO_QUEUE}.
	 */
	public BoundedWorkerPool(Class<?> logCategory, String subject, String task, String setting,
			IntFunction<String> threadName, IntSupplier ceiling, IntSupplier queueLength) {
		_logCategory = logCategory;
		_subject = subject;
		_task = task;
		_setting = setting;
		_threadName = threadName;
		_ceiling = ceiling;
		_queueLength = queueLength;
	}

	/**
	 * Runs the given task on a worker, or queues it for the next free one.
	 *
	 * @return The task's future, backed by a {@link FutureTask}: cancelling it before the task
	 *         starts takes the task off the queue, so it never runs.
	 * @throws RejectedExecutionException
	 *         If every worker is busy and the queue has no room either.
	 */
	public <T> Future<T> submit(Callable<T> task) {
		return pool().submit(task);
	}

	/**
	 * Runs the given task on a worker, or queues it for the next free one.
	 *
	 * @see #submit(Callable)
	 */
	public Future<?> submit(Runnable task) {
		return pool().submit(task);
	}

	/**
	 * Like {@link #submit(Callable)}, except that a task submitted from one of this pool's own
	 * workers while the pool has a ceiling runs at once on the submitting thread.
	 *
	 * <p>
	 * For a caller that submits from inside a task of this pool and then waits for the result. With
	 * a ceiling, every worker could be such a caller, waiting for tasks that no free worker is left to
	 * start. Running the task on the waiting worker itself keeps the ceiling and never waits on the
	 * pool. Without a ceiling there is always a worker, and the task is submitted as usual.
	 * </p>
	 *
	 * @return The task's future; for a task that ran on the submitting thread, an already completed
	 *         one.
	 */
	public <T> Future<T> submitNested(Callable<T> task) {
		if (isOwnWorker() && pool().getMaximumPoolSize() != Integer.MAX_VALUE) {
			FutureTask<T> inline = new FutureTask<>(task);
			inline.run();
			return inline;
		}
		return submit(task);
	}

	/** Whether the current thread is a worker of this pool. */
	public boolean isOwnWorker() {
		return WORKER_OF.get() == this;
	}

	/**
	 * Whether a task would find room right now - a free worker, or a place to wait.
	 *
	 * <p>
	 * Always {@code true} unless a ceiling and a bounded queue are configured. Advisory: between this
	 * answer and a submit another caller may take the last place.
	 * </p>
	 */
	public boolean hasRoom() {
		ThreadPoolExecutor pool = pool();
		if (pool.getActiveCount() < pool.getMaximumPoolSize()) {
			return true;
		}
		return pool.getQueue().remainingCapacity() > 0;
	}

	/** How many tasks the pool runs at the same time; {@link #NO_CEILING} when it runs as many as arrive. */
	public int workers() {
		int max = pool().getMaximumPoolSize();
		return max == Integer.MAX_VALUE ? NO_CEILING : max;
	}

	/**
	 * The place of the given task in the queue of the pool in place, counted from {@code 1}.
	 *
	 * @param task
	 *        A future returned by this pool.
	 * @return {@code 0} if the task does not wait: it runs, has ended, was cancelled, or is unknown
	 *         to the pool in place.
	 */
	public int positionOf(Future<?> task) {
		Built built = _built;
		if (built == null) {
			return 0;
		}
		int position = 0;
		for (Iterator<Runnable> it = built._pool.getQueue().iterator(); it.hasNext();) {
			position++;
			if (it.next() == task) {
				return position;
			}
		}
		return 0;
	}

	/**
	 * Throws the pool away, interrupting what runs on it, so that the next use builds one from the
	 * configuration as it is now.
	 *
	 * <p>
	 * <b>A test seam.</b> Nothing in production calls it.
	 * </p>
	 */
	public void reset() {
		Built previous;
		synchronized (this) {
			previous = _built;
			_built = null;
		}
		if (previous != null) {
			previous._pool.shutdownNow();
		}
	}

	private ThreadPoolExecutor pool() {
		int ceiling = Math.max(NO_CEILING, _ceiling.getAsInt());
		int queueLength = normalizedQueue(_queueLength.getAsInt());
		Built existing = _built;
		if (existing != null && existing.matches(ceiling, queueLength)) {
			return existing._pool;
		}
		synchronized (this) {
			Built current = _built;
			if (current == null || !current.matches(ceiling, queueLength)) {
				_built = new Built(ceiling, queueLength, newPool(ceiling, queueLength));
				if (current != null) {
					// Lets the tasks it accepted end; then its threads go.
					current._pool.shutdown();
				}
			}
			return _built._pool;
		}
	}

	private static int normalizedQueue(int configured) {
		return configured < 0 ? RunLimitSettings.NO_QUEUE : configured;
	}

	private ThreadPoolExecutor newPool(int ceiling, int queueLength) {
		return ceiling > NO_CEILING ? boundedPool(ceiling, queueLength) : unboundedPool();
	}

	private ThreadPoolExecutor unboundedPool() {
		Logger.info(_subject + " are not limited in number."
			+ " Set " + _setting + " in <capacity> of AgentRuntimeService to bound them.", _logCategory);
		return new ThreadPoolExecutor(0, Integer.MAX_VALUE, IDLE_SECONDS, TimeUnit.SECONDS,
			new SynchronousQueue<>(), new WorkerThreads());
	}

	private ThreadPoolExecutor boundedPool(int ceiling, int queueLength) {
		BlockingQueue<Runnable> queue;
		String waiting;
		if (queueLength == RunLimitSettings.NO_QUEUE) {
			// A SynchronousQueue hands a task straight to a waiting worker and refuses when there is
			// none, which with core == max is exactly "this many at once, and no waiting room".
			queue = new SynchronousQueue<>();
			waiting = "with no queue: " + _task + " that finds them busy is refused";
		} else if (queueLength == UNBOUNDED_QUEUE) {
			queue = new LinkedBlockingQueue<>();
			waiting = "with a queue of no fixed length: " + _task + " that finds them busy waits";
		} else {
			queue = new LinkedBlockingQueue<>(queueLength);
			waiting = "with room for " + queueLength + " waiting";
		}
		ThreadPoolExecutor pool = new ThreadPoolExecutor(ceiling, ceiling, IDLE_SECONDS, TimeUnit.SECONDS,
			queue, new WorkerThreads());
		// The threads are idle far more often than they are busy; letting them go keeps an
		// installation that is quiet at night from holding the pool's worth of stacks.
		pool.allowCoreThreadTimeOut(true);
		Logger.info(_subject + " are bounded to " + ceiling + " at a time, " + waiting + ".", _logCategory);
		return pool;
	}

	/** A built pool and the configuration it was built from. */
	private static final class Built {

		final int _ceiling;

		final int _queueLength;

		final ThreadPoolExecutor _pool;

		Built(int ceiling, int queueLength, ThreadPoolExecutor pool) {
			_ceiling = ceiling;
			_queueLength = queueLength;
			_pool = pool;
		}

		boolean matches(int ceiling, int queueLength) {
			// Without a ceiling the queue length is of no consequence.
			return _ceiling == ceiling && (ceiling == NO_CEILING || _queueLength == queueLength);
		}
	}

	/** Daemon threads that know the pool they work for, named so a thread dump says which is which. */
	private final class WorkerThreads implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(() -> {
				WORKER_OF.set(BoundedWorkerPool.this);
				r.run();
			});
			thread.setName(_threadName.apply(_threadCount.incrementAndGet()));
			thread.setDaemon(true);
			return thread;
		}
	}
}
