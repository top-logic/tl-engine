/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.util.WaitOnStopRunnableProxy;

/**
 * A handle for stopping and restarting a periodical {@link Runnable} of the
 * {@link SchedulerService}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SchedulerServiceHandle {

	private final WaitOnStopRunnableProxy _wrappedRunnable;

	private ScheduledFuture<?> _handle;

	/**
	 * Creates a new {@link SchedulerServiceHandle}. The {@link Runnable} will not start
	 * automatically. Call {@link #start(long, TimeUnit)} to start it.
	 * 
	 * @throws NullPointerException
	 *         If the given inner {@link Runnable} is null.
	 */
	public SchedulerServiceHandle(Runnable inner) throws NullPointerException {
		_wrappedRunnable = new WaitOnStopRunnableProxy(inner);
	}

	/**
	 * Schedule the {@link Runnable} with the given interval.
	 * <p>
	 * The first execution will be after the given interval.
	 * </p>
	 */
	public void start(long interval, TimeUnit timeUnit) {
		_wrappedRunnable.start();
		setHandle(schedule(interval, timeUnit));
	}

	/**
	 * Stop the {@link Runnable}.
	 * <p>
	 * If it is currently running, wait for it to finish, until the given time is up. When the time
	 * is up, a warning will be logged and the method returns normally. <br/>
	 * The {@link Runnable} can be re-enabled by calling {@link #start(long, TimeUnit)} again.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *         If it has not been started. {@link #start(long, TimeUnit)} and
	 *         {@link #stop(long, TimeUnit)} always have to be called symmetrically.
	 */
	public void stop(long time, TimeUnit timeUnit) throws IllegalStateException {
		ScheduledFuture<?> handle = getHandle();
		if (handle == null) {
			throw createStopWithoutStartException();
		}
		handle.cancel(false);
		_wrappedRunnable.stop(time, timeUnit);
	}

	private IllegalStateException createStopWithoutStartException() {
		return new IllegalStateException(this + " cannot be stopped, as it has not even been started."
			+ " 'start()' and 'stop()' always have to be called symetrically."
			+ " That means, there is probably a 'start()' missing somewhere.");
	}

	private ScheduledFuture<?> schedule(long interval, TimeUnit timeUnit) {
		SchedulerService scheduler = SchedulerService.getInstance();
		return scheduler.scheduleWithFixedDelay(_wrappedRunnable, interval, interval, timeUnit);
	}

	private synchronized void setHandle(ScheduledFuture<?> newValue) {
		_handle = newValue;
	}

	private synchronized ScheduledFuture<?> getHandle() {
		return _handle;
	}

}
