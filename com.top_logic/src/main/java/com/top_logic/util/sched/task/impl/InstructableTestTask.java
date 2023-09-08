/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTaskImpl;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * A dummy {@link Task} for tests.
 * <p>
 * Useful for example for tests and experiments. Is not located in the test package, as it is
 * sometimes useful for experiments that use the running application.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class InstructableTestTask<C extends InstructableTestTask.Config<?>> extends StateHandlingTask<C> {

	/** The configuration for the {@link InstructableTestTask}. */
	public interface Config<I extends InstructableTestTask<?>> extends StateHandlingTask.Config<I> {

		/**
		 * Whether this task runs independently on every cluster node.
		 * 
		 * <p>
		 * In some cases it may be needful, if tasks are scheduled only on one cluster node (e.g.
		 * refresh of external user data). Those tasks are <b>not</b> node-local.
		 * </p>
		 * 
		 * @see Task#isNodeLocal()
		 */
		@BooleanDefault(true)
		boolean isNodeLocal();

		/**
		 * Whether this {@link Task} is persisted.
		 * 
		 * @see Task#isPersistent()
		 */
		@BooleanDefault(false)
		boolean isPersistent();

		/** The seed for the {@link Random} number generator. */
		@LongDefault(42)
		long getRandomSeed();

		/** The minimal working time (in milliseconds). */
		long getWorkingTimeMin();

		/** The maximal working time (in milliseconds). */
		long getWorkingTimeMax();

		/**
		 * How often (between 0 and 1) should this {@link InstructableTestTask} generate
		 * {@link Task#logGuiWarning(String) gui warnings}?
		 * <p>
		 * If the task does have at least one warning, the probability is additionally used to
		 * generate the number of warnings by getting new random numbers until one of them is lower
		 * than this value. <br/>
		 * This probability is independent of the other rates, when it comes to the sum of
		 * probabilities that must not be larger than 1.
		 * </p>
		 */
		@FloatDefault(0)
		float getRateGuiWarnings();

		/**
		 * How often (between 0 and 1) should this {@link InstructableTestTask} end with
		 * {@link ResultType#FAILURE}?
		 * <p>
		 * The sum of all possibilities has to be <code>&lt;= 1</code>. Otherwise, an
		 * {@link ConfigurationError} is thrown when the task is instantiated. If the sum is exactly
		 * 1, the task will never succeed.
		 * </p>
		 * 
		 * <p>
		 * This is only the probability for the {@link ResultType#FAILURE} to be explicitly set
		 * without some error or exception being thrown. If one of the rates for throwing something
		 * is higher than 0, that might increase the number of observed {@link ResultType#FAILURE}
		 * results, depending on how the super classes react on thrown errors and exceptions.
		 * </p>
		 */
		@FloatDefault(0)
		float getRateFailureResult();

		/**
		 * How often (between 0 and 1) should this {@link InstructableTestTask} end with
		 * {@link ResultType#ERROR}?
		 * <p>
		 * The sum of all possibilities has to be <code>&lt;= 1</code>. Otherwise, an
		 * {@link ConfigurationError} is thrown when the task is instantiated. If the sum is exactly
		 * 1, the task will never succeed.<br/>
		 * This is only the probability for the {@link ResultType#ERROR} to be explicitly set
		 * without some error or exception being thrown. If one of the rates for throwing something
		 * is higher than 0, that might increase the number of observed {@link ResultType#ERROR}
		 * results, depending on how the super classes react on thrown errors and exceptions.
		 * </p>
		 */
		@FloatDefault(0)
		float getRateErrorResult();

		/**
		 * How often (between 0 and 1) should this {@link InstructableTestTask} throw a
		 * {@link RuntimeException}?
		 * <p>
		 * The sum of all possibilities has to be <code>&lt;= 1</code>. Otherwise, an
		 * {@link ConfigurationError} is thrown when the task is instantiated. If the sum is exactly
		 * 1, the task will never succeed.<br/>
		 * </p>
		 */
		@FloatDefault(0)
		float getRateThrowRuntimeException();

		/**
		 * How often (between 0 and 1) should this {@link InstructableTestTask} throw an (anonymous
		 * inner class) {@link Exception}?
		 * <p>
		 * The sum of all possibilities has to be <code>&lt;= 1</code>. Otherwise, an
		 * {@link ConfigurationError} is thrown when the task is instantiated. If the sum is exactly
		 * 1, the task will never succeed.<br/>
		 * </p>
		 */
		@FloatDefault(0)
		float getRateThrowException();

		/**
		 * How often (between 0 and 1) should this {@link InstructableTestTask} throw an (anonymous
		 * inner class) {@link Error}?
		 * <p>
		 * The sum of all possibilities has to be <code>&lt;= 1</code>. Otherwise, an
		 * {@link ConfigurationError} is thrown when the task is instantiated. If the sum is exactly
		 * 1, the task will never succeed.<br/>
		 * </p>
		 */
		@FloatDefault(0)
		float getRateThrowError();

		/**
		 * How often (between 0 and 1) should this {@link InstructableTestTask} throw an (anonymous
		 * inner class) {@link Throwable}?
		 * <p>
		 * The sum of all possibilities has to be <code>&lt;= 1</code>. Otherwise, an
		 * {@link ConfigurationError} is thrown when the task is instantiated. If the sum is exactly
		 * 1, the task will never succeed.<br/>
		 * </p>
		 */
		@FloatDefault(0)
		float getRateThrowThrowable();

	}

	private enum InstructedTaskResult {

		STATE_FAILURE {
			@Override
			void causeTaskResult(Task task) {
				TaskLog taskLog = task.getLog();
				RuntimeException exception =
					new RuntimeException("Task " + task.getName() + ": Instructed Failure State");
				taskLog.taskEnded(ResultType.FAILURE, ResultType.FAILURE.getMessageI18N(), exception);
			}
		},
		STATE_ERROR {
			@Override
			void causeTaskResult(Task task) {
				TaskLog taskLog = task.getLog();
				RuntimeException exception =
					new RuntimeException("Task " + task.getName() + ": Instructed Error State");
				taskLog.taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), exception);
			}
		},
		THROW_RUNTIME_EXCEPTION {
			@Override
			void causeTaskResult(Task task) {
				throw new RuntimeException("Task " + task.getName() + ": Instructed RuntimeException");
			}
		},
		THROW_EXCEPTION {
			@Override
			void causeTaskResult(Task task) {
				throwException("Task " + task.getName() + ": Instructed Exception");
			}
		},
		THROW_ERROR {
			@Override
			void causeTaskResult(Task task) {
				throw new Error("Task " + task.getName() + ": Instructed Error");
			}
		},
		THROW_THROWABLE {
			@Override
			void causeTaskResult(Task task) {
				throwThrowable("Task " + task.getName() + ": Instructed Throwable");
			}
		},
		SUCCESS {
			@Override
			void causeTaskResult(Task task) {
				// Happens automatically
			}
		};

		abstract void causeTaskResult(Task task);

		void throwException(final String message) {
			// This method is a hack, but the only way to test
			// how code reacts on an OutOfMemoryError and similar stuff.
			class ExceptionThrower {
				@SuppressWarnings("unused")
				public ExceptionThrower() throws Exception {
					throw new Exception(message);
				}
			}
			try {
				// The newInstance method circumvents the
				// "checked exceptions" checks of the compiler.
				ExceptionThrower.class.newInstance();
			} catch (InstantiationException ex) {
				throw new RuntimeException(ex);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}

		synchronized void throwThrowable(final String message) {
			// This method is a hack, but the only way to test
			// how code reacts on an OutOfMemoryError and similar stuff.
			try {
				// The newInstance method circumvents the
				// "checked exceptions" checks of the compiler.
				ThrowableThrower.message = message;
				ThrowableThrower.class.newInstance();
			} catch (InstantiationException ex) {
				throw new RuntimeException(ex);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			} finally {
				ThrowableThrower.message = null;
			}
		}

		/**
		 * This class has to be static because the constructor must not have a parameter, not even
		 * an implicit 'this' parameter.
		 */
		private static class ThrowableThrower {

			static String message = null;

			@SuppressWarnings("unused")
			public ThrowableThrower() throws Throwable {
				throw new Throwable(message);
			}
		}
	}

	private final Random _random;

	/** Used by the typed configuration to instantiate the {@link CompositeTaskImpl}. */
	@CalledByReflection
	public InstructableTestTask(InstantiationContext context, C config) {
		super(context, config);
		checkWorkingTime(config);
		checkRates(config);
		_random = new Random(config.getRandomSeed());
	}

	private void checkWorkingTime(Config config) {
		if (config.getWorkingTimeMin() < 0) {
			throw new ConfigurationError("Task " + getName() + ": Working time must not be negative.");
		}
		if (config.getWorkingTimeMin() > config.getWorkingTimeMax()) {
			throw new ConfigurationError("Task " + getName()
				+ ": Minimal working time must not be greater than the maximal working time.");
		}
	}

	private void checkRates(Config config) {
		checkRate(config.getRateGuiWarnings(), "gui warnings");
		checkRate(config.getRateFailureResult(), "failure state");
		checkRate(config.getRateErrorResult(), "error state");
		checkRate(config.getRateThrowRuntimeException(), "throw RuntimeException");
		checkRate(config.getRateThrowException(), "throw Exception");
		checkRate(config.getRateThrowError(), "throw Error");
		checkRate(config.getRateThrowThrowable(), "throw Throwable");
		checkRatesSum(config);
	}

	private void checkRate(float probability, String rateName) {
		if (probability < 0) {
			throw new ConfigurationError("Task " + getName() + ": The '" + rateName + "' rate must not be negative.");
		}
		if (probability > 1) {
			throw new ConfigurationError("Task " + getName() + ": The '" + rateName + "' rate has to be <= 1.");
		}
	}

	private void checkRatesSum(Config config) {
		float rateSum = config.getRateFailureResult()
			+ config.getRateErrorResult()
			+ config.getRateThrowRuntimeException()
			+ config.getRateThrowException()
			+ config.getRateThrowError()
			+ config.getRateThrowThrowable();
		if (rateSum > 1) {
			throw new ConfigurationError("Task " + getName() + ": Sum of probabilities has to be <= 1. But it is: "
				+ rateSum);
		}
	}

	@Override
	protected void runHook() {
		checkMaintenanceMode();
		long workingTime = calcWorkingTime();
		InstructedTaskResult result = calcResult();
		Logger.info("Task " + getName() + ": Started. Execution will take: " + workingTime + " ms and end with: "
			+ result, getClass());
		try {
			List<String> warnings = getWarnings();
			long sleepTime = workingTime / (1 + warnings.size());
			sleep(sleepTime);
			for (String warning : warnings) {
				logGuiWarning(warning);
				sleep(sleepTime);
			}
			result.causeTaskResult(this);
		} finally {
			Logger.info("Task " + getName() + ": Finished.", getClass());
		}
	}

	private void checkMaintenanceMode() {
		if (needsMaintenanceMode() && !isInMaintenanceMode()) {
			Logger.error("Task '" + getName() + "' needs the maintenance mode, but it is not active!", this);
		}
		if (isInMaintenanceMode() && !(isMaintenanceModeSafe() || needsMaintenanceMode())) {
			Logger.error("Task '" + getName() + "' is not safe to run in maintenance mode, but it is active!", this);
		}
	}

	private boolean isInMaintenanceMode() {
		return getMaintenanceModeState() == MaintenanceWindowManager.IN_MAINTENANCE_MODE;
	}

	private int getMaintenanceModeState() {
		return MaintenanceWindowManager.getInstance().getMaintenanceModeState();
	}

	private void sleep(long workingTime) {
		try {
			if (workingTime > 0) {
				Thread.sleep(workingTime);
			}
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private long calcWorkingTime() {
		long min = getConfig().getWorkingTimeMin();
		long max = getConfig().getWorkingTimeMax();
		float randomFloat = getRandom().nextFloat();
		return Math.round(min + randomFloat * (max - min));
	}

	private InstructedTaskResult calcResult() {
		float randomFloat = getRandom().nextFloat();
		randomFloat -= getConfig().getRateFailureResult();
		if (randomFloat < 0) {
			return InstructedTaskResult.STATE_FAILURE;
		}
		randomFloat -= getConfig().getRateErrorResult();
		if (randomFloat < 0) {
			return InstructedTaskResult.STATE_ERROR;
		}
		randomFloat -= getConfig().getRateThrowRuntimeException();
		if (randomFloat < 0) {
			return InstructedTaskResult.THROW_RUNTIME_EXCEPTION;
		}
		randomFloat -= getConfig().getRateThrowException();
		if (randomFloat < 0) {
			return InstructedTaskResult.THROW_EXCEPTION;
		}
		randomFloat -= getConfig().getRateThrowError();
		if (randomFloat < 0) {
			return InstructedTaskResult.THROW_ERROR;
		}
		randomFloat -= getConfig().getRateThrowThrowable();
		if (randomFloat < 0) {
			return InstructedTaskResult.THROW_THROWABLE;
		}
		return InstructedTaskResult.SUCCESS;
	}

	private List<String> getWarnings() {
		float rate = getConfig().getRateGuiWarnings();
		if (getRandom().nextFloat() >= rate) {
			return Collections.emptyList();
		}
		List<String> warnings = new ArrayList<>();
		int i = 1;
		warnings.add("This is the " + i + ". warning.");
		while (getRandom().nextFloat() < rate) {
			i++;
			warnings.add("This is the " + i + ". warning.");
		}
		return warnings;
	}

	/** Getter for the {@link Random}. */
	protected Random getRandom() {
		return _random;
	}

	@Override
	protected boolean signalStopHook() {
		// This task will try to stop as soon as possible,
		// but there is no guarantee, how long this can take.
		return false;
	}

	@Override
	public boolean isNodeLocal() {
		return getConfig().isNodeLocal();
	}

	@Override
	public boolean isPersistent() {
		return getConfig().isPersistent();
	}

}
