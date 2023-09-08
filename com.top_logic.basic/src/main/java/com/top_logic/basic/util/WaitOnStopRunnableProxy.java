/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.top_logic.basic.Logger;

/**
 * A proxy for waiting for a {@link Runnable} to stop.
 * <p>
 * Wait for an asynchronously running {@link Runnable} to finish, if it might already be running.
 * <em>It is guaranteed that the {@link Runnable} will not run and not start any more after {@link #stop(long, TimeUnit)} returns.</em>
 * This is useful to stop the {@link Runnable}, if it is scheduled to run asynchronously, but the
 * scheduler in use does not allow to wait for a task to stop. After {@link #stop(long, TimeUnit)}
 * has been called, the {@link Runnable} can be started again with {@link #start()}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class WaitOnStopRunnableProxy extends RunnableProxy<Runnable> {

	private final ReentrantLock _lock = new ReentrantLock();

	/**
	 * Volatile to ensure update are visible without locks.
	 * <p>
	 * See implementation of {@link #stop(long, TimeUnit)} for details why it has to be
	 * volatile.
	 * </p>
	 */
	private volatile boolean _stop;

	/**
	 * Creates a {@link RunnableProxy}.
	 * 
	 * @throws NullPointerException
	 *         If the given inner {@link Runnable} is null.
	 */
	public WaitOnStopRunnableProxy(Runnable inner) throws NullPointerException {
		super(inner);
	}

	@Override
	public void run() {
		_lock.lock();
		try {
			if (_stop) {
				return;
			}
			getInner().run();
		} finally {
			_lock.unlock();
		}
	}

	/** Allow the {@link Runnable} to run again. */
	public void start() {
		_stop = false;
	}

	/**
	 * Stop the {@link Runnable}.
	 * <p>
	 * Prevent it from starting. And if it is already started, wait for it to finish. <br/>
	 * Guarantee: When this method returns, either the wait timed out, or the {@link Runnable} is
	 * not running and will not start again.
	 * </p>
	 */
	public void stop(long time, TimeUnit timeUnit) {
		// The field is volatile. Therefore, the new value is visible,
		// whether the lock below succeeds or fails.
		_stop = true;
		try {
			/* Wait for the runnable to complete. It is protected by the lock. As soon as the
			 * runnable is done, the lock is acquired. That means, if this lock succeeds, it is
			 * guaranteed that the runnable is done or not yet started. Additionally, the lock
			 * establishes a "happens-before" guarantee for the "_stop" field: If the runnable
			 * starts, it will see the new value and stop before doing anything. */
			if (_lock.tryLock(time, timeUnit)) {
				_lock.unlock();
			} else {
				Logger.warn("Failed to wait for " + getClass().getName() + " to stop. Lock timed out after "
					+ time + " " + timeUnit.toString() + ".", WaitOnStopRunnableProxy.class);
				/* As the lock timed out, there is no "happens-before" guarantee. Therefore, the
				 * _stop field has to be volatile. */
			}
		} catch (InterruptedException ex) {
			Logger.info("Stopped waiting for " + getClass().getName() + ". Wait for lock was interrupted. Cause: "
				+ ex.getMessage(), ex, WaitOnStopRunnableProxy.class);
		}
	}

}
