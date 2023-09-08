/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.impl.StateHandlingTask;

/**
 * A {@link StateHandlingTask} which uses a {@link CyclicBarrier} for synchronization with the JUnit
 * thread.
 * <p>
 * Calls {@link CyclicBarrier#await(long, TimeUnit)} twice when executed. Twice and not once, so
 * tests can for example simulate long running {@link Task}s and examine the Scheduler while this
 * {@link Task} is "running".
 * </p>
 * 
 * @see BarrierTestTaskConfig
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class BarrierTestTask extends StateHandlingTask<BarrierTestTask.BarrierTestTaskConfig> {

	/** The config interface for the {@link BarrierTestTask}. */
	public interface BarrierTestTaskConfig extends StateHandlingTask.Config<BarrierTestTask> {

		/**
		 * The timeout in milliseconds for the first call to
		 * {@link CyclicBarrier#await(long, TimeUnit)}.
		 */
		@LongDefault(1000)
		public long getFirstBarrierTimeout();

		/** @see #getFirstBarrierTimeout() */
		public void setFirstBarrierTimeout(long value);

		/**
		 * The timeout in milliseconds for the second call to
		 * {@link CyclicBarrier#await(long, TimeUnit)}.
		 */
		@LongDefault(1000)
		public long getSecondBarrierTimeout();

		/** @see #getSecondBarrierTimeout() */
		public void setSecondBarrierTimeout(long value);

		/**
		 * Configuration value of {@link Task#isPersistent()}
		 */
		boolean isPersistent();

		/** @see #isPersistent() */
		void setPersistent(boolean b);

	}

	/* All fields are volatile, as they can be read or set from the main JUnit Thread. */
	private volatile CyclicBarrier _barrier = new CyclicBarrier(2);

	private volatile boolean _firstBarrierSuccess = false;

	private volatile boolean _secondBarrierSuccess = false;

	private final boolean _persistent;

	/** Called by the typed configuration for creating a {@link BarrierTestTask}. */
	@CalledByReflection
	public BarrierTestTask(InstantiationContext context, BarrierTestTaskConfig config) {
		super(context, config);
		_persistent = config.isPersistent();
	}

	/** The CyclicBarrier. The initial one is configured for two parties. */
	public CyclicBarrier getBarrier() {
		return _barrier;
	}

	@Override
	public boolean isPersistent() {
		return _persistent;
	}

	/**
	 * Override the (initial) {@link CyclicBarrier}.
	 * 
	 * @param barrier
	 *        Is not allowed to be null.
	 */
	public void setBarrier(CyclicBarrier barrier) {
		if (barrier == null) {
			throw new NullPointerException("Barrier is not allowed to be null.");
		}
		_barrier = barrier;
	}

	/** Did the first {@link CyclicBarrier#await(long, TimeUnit)} return normally? */
	public boolean isFirstBarrierSuccess() {
		return _firstBarrierSuccess;
	}

	/** Did the second {@link CyclicBarrier#await(long, TimeUnit)} return normally? */
	public boolean isSecondBarrierSuccess() {
		return _secondBarrierSuccess;
	}

	@Override
	protected void runHook() {
		try {
			_barrier.await(getConfig().getFirstBarrierTimeout(), TimeUnit.MILLISECONDS);
			_firstBarrierSuccess = true;
			_barrier.await(getConfig().getSecondBarrierTimeout(), TimeUnit.MILLISECONDS);
			_secondBarrierSuccess = true;
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (BrokenBarrierException ex) {
			throw new RuntimeException(ex);
		} catch (TimeoutException ex) {
			throw new RuntimeException(ex);
		}
	}

}
