/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.timer;

import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.CollectionUtil;

/**
 * {@link ScheduledExecutorService} utility for computing a sequence of timeout intervals
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class UIExecutorService extends AbstractExecutorService implements ScheduledExecutorService {

	private final PriorityQueue<Task<?>> _tasks = new PriorityQueue<>();

	@Override
	public void shutdown() {
		// Ignore.
	}

	@Override
	public List<Runnable> shutdownNow() {
		return Collections.emptyList();
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public void execute(Runnable command) {
		command.run();
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return addTask(new RunnableTask(delayOnce(delay, unit), command));
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return addTask(new CallableTask<>(delayOnce(delay, unit), callable));
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return addTask(new RunnableTask(fixedRate(initialDelay, period, unit), command));
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return addTask(new RunnableTask(fixedDelay(initialDelay, delay, unit), command));
	}

	private <T> ScheduledFuture<T> addTask(Task<T> task) {
		long before = nextDelay();
		_tasks.add(task);
		long after = nextDelay();

		if (before != after) {
			onScheduleChanged(after);
		}

		return task;
	}

	/**
	 * Hook that is called whenever the schedule changes.
	 * 
	 * @param nextDelayMillis
	 *        The delay in milliseconds after which {@link #process()} must be called to execute all
	 *        scheduled tasks in time.
	 */
	protected void onScheduleChanged(long nextDelayMillis) {
		// Hook for sub-classes.
	}

	/**
	 * The number of milliseconds to wait before a call of {@link #process()} is required to allow
	 * executing all scheduled tasks in time.
	 * 
	 * @return The wait time in milliseconds, or <code>-1</code>, if {@link #process()} needs not to
	 *         be called at all.
	 * 
	 * @see #onScheduleChanged(long)
	 */
	public long nextDelay() {
		while (true) {
			Task<?> next = _tasks.poll();
			if (next == null) {
				return -1;
			}
			if (next.isCancelled()) {
				continue;
			}
			_tasks.add(next);
			long nextDelay = next.getDelay(TimeUnit.MILLISECONDS);
			return Math.max(1, nextDelay);
		}
	}

	/**
	 * Processes tasks that are due.
	 * 
	 * @return The new schedule, see {@link #nextDelay()}.
	 */
	public long process() {
		while (true) {
			Task<?> next = _tasks.poll();
			if (next == null) {
				return -1;
			}
			if (next.isCancelled()) {
				continue;
			}

			long nextDelay = next.getDelay(TimeUnit.MILLISECONDS);
			if (nextDelay > 0) {
				addTask(next);
				return nextDelay;
			}

			next.run();

			boolean again = next.reschedule();
			if (again) {
				addTask(next);
			}
		}
	}

	private Schedule delayOnce(long delay, TimeUnit unit) {
		return new DelayOnce(TimeUnit.MILLISECONDS.convert(delay, unit));
	}

	private Schedule fixedRate(long initialDelay, long period, TimeUnit unit) {
		return new FixedRate(
			TimeUnit.MILLISECONDS.convert(initialDelay, unit),
			TimeUnit.MILLISECONDS.convert(period, unit));
	}

	private Schedule fixedDelay(long initialDelay, long delay, TimeUnit unit) {
		return new FixedDelay(
			TimeUnit.MILLISECONDS.convert(initialDelay, unit),
			TimeUnit.MILLISECONDS.convert(delay, unit));
	}

	private abstract static class Schedule {

		public Schedule() {
			super();
		}

		public abstract long getDelay(TimeUnit unit);

		public abstract boolean next();

	}

	private static class DelayOnce extends Schedule {

		private final long _time;

		public DelayOnce(long delayMillis) {
			_time = System.currentTimeMillis() + delayMillis;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(_time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public boolean next() {
			return false;
		}
	}

	private static class FixedRate extends Schedule {

		private long _initialSchedule;
		private final long _periodMillis;

		public FixedRate(long offsetMillis, long periodMillis) {
			_initialSchedule = System.currentTimeMillis() + offsetMillis;
			_periodMillis = periodMillis;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(_initialSchedule - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public boolean next() {
			_initialSchedule += _periodMillis;
			return true;
		}

	}

	private static class FixedDelay extends Schedule {

		private long _initialSchedule;

		private final long _delayMillis;

		public FixedDelay(long offsetMillis, long delayMillis) {
			_delayMillis = delayMillis;
			_initialSchedule = System.currentTimeMillis() + offsetMillis;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(_initialSchedule - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public boolean next() {
			_initialSchedule = System.currentTimeMillis() + _delayMillis;
			return true;
		}

	}

	private abstract static class Task<V> implements ScheduledFuture<V> {

		private final Schedule _schedule;

		private boolean _canceled;

		private boolean _completed;

		private V _result;

		private Throwable _problem;

		public Task(Schedule schedule) {
			_schedule = schedule;
		}

		boolean reschedule() {
			if (_canceled) {
				return false;
			}
			boolean result = _schedule.next();
			if (result) {
				_completed = false;
			}
			return result;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return _schedule.getDelay(unit);
		}

		@Override
		public int compareTo(Delayed other) {
			long otherDelay = other.getDelay(TimeUnit.MILLISECONDS);
			return CollectionUtil.compareLong(getDelay(TimeUnit.MILLISECONDS), otherDelay);
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (_completed) {
				return false;
			}
			_canceled = true;
			return true;
		}

		@Override
		public boolean isCancelled() {
			return _canceled;
		}

		@Override
		public boolean isDone() {
			return _completed || _canceled;
		}

		protected void setResult(V result) {
			_result = result;
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			run();

			if (_problem != null) {
				throw new ExecutionException(_problem);
			}
			return _result;
		}

		final void run() {
			if (_completed) {
				return;
			}

			try {
				internalRun();
			} catch (Throwable ex) {
				_problem = ex;
			} finally {
				_completed = true;
			}
		}

		protected abstract void internalRun() throws Exception;

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return get();
		}

	}

	static class RunnableTask extends Task<Void> {

		private final Runnable _command;

		public RunnableTask(Schedule schedule, Runnable command) {
			super(schedule);
			_command = command;
		}

		@Override
		public void internalRun() {
			_command.run();
		}

	}

	static class CallableTask<V> extends Task<V> {

		private final Callable<V> _command;

		public CallableTask(Schedule schedule, Callable<V> command) {
			super(schedule);
			_command = command;
		}

		@Override
		public void internalRun() throws Exception {
			setResult(_command.call());
		}

	}
}
