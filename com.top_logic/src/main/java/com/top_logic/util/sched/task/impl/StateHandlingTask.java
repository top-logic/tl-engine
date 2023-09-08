/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.impl;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.sched.I18NConstants;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.composite.CompositeTaskImpl;
import com.top_logic.util.sched.task.impl.StateHandlingTask.Config;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * {@link TaskImpl} that takes care of the code (of {@link #runHook()}) being executed in a
 * {@link ThreadContext#inSystemContext(Class, com.top_logic.basic.util.ComputationEx2) system
 * context} and storing whether the {@link Task} succeeded or failed.
 * <p>
 * <b>This is the recommended superclass for implementing a new {@link Task},</b> as it takes
 * already care of stuff that is easily forgotten to implement.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class StateHandlingTask<T extends Config<?>> extends TaskImpl<T> {

	/**
	 * Typed configuration for the {@link StateHandlingTask}.
	 * <p>
	 * Empty for now. Introduced as there are only a few implementations of
	 * {@link StateHandlingTask} now, and introducing it later when there are more would be more
	 * work and require a code migration.
	 * </p>
	 */
	public static interface Config<I extends StateHandlingTask<?>> extends TaskImpl.Config<I> {

		// See JavaDoc

	}

	private final T _config;

	/** Used by the typed configuration to instantiate the {@link CompositeTaskImpl}. */
	@CalledByReflection
	public StateHandlingTask(InstantiationContext context, T config) {
		super(context, config);
		_config = config;
	}

	/** Getter for the {@link Config} that was used to instantiate this {@link StateHandlingTask}. */
	protected final T getConfig() {
		return _config;
	}

	@Override
	public final void run() {
		super.run();
		ThreadContext.inSystemContext(getClass(), this::runWithSystemContext);
	}

	private void runWithSystemContext() {
		getLog().taskStarted();
		try {
			runHook();
			cleanupOnTaskReturn();
		} catch (Throwable exception) {
			cleanupOnThrowable(exception);
		}
	}

	/**
	 * The actual work of the task. Everything else is just management stuff.
	 * <p>
	 * Runs in a
	 * {@link ThreadContext#inSystemContext(Class, com.top_logic.basic.util.ComputationEx2) thread
	 * context}.
	 * </p>
	 * 
	 * <p>
	 * Before this method is called, {@link TaskLog#taskStarted()} is called, and when it returns
	 * {@link TaskLog#taskEnded(ResultType, ResKey)} is called. More precisely: <br/>
	 * When this method returns or throws any {@link Throwable} and the current result is not
	 * {@link ResultType#NOT_FINISHED}, the result is untouched. (The {@link ResultType} cannot be
	 * changed, once it is set.)
	 * </p>
	 * 
	 * <p>
	 * Otherwise:
	 * </p>
	 * <ul>
	 * <li>When this method returns and the state is {@link TaskState#CANCELING}, the result is set
	 * to {@link ResultType#CANCELED}.</li>
	 * <li>When this method returns and there are no warnings, the result is set to
	 * {@link ResultType#SUCCESS}.</li>
	 * <li>When this method returns and there are warnings, the result is set to
	 * {@link ResultType#WARNING}.</li>
	 * <li>When this method throws any {@link Throwable}, the result is set to
	 * {@link ResultType#ERROR}.</li>
	 * </ul>
	 * <p>
	 * The thrown exception is only logged if it's a {@link RuntimeException}.
	 * </p>
	 */
	protected abstract void runHook();

	private void cleanupOnTaskReturn() {
		TaskResult currentResult = getLog().getCurrentResult();
		if (currentResult.getResultType() == ResultType.NOT_FINISHED) {
			if (getLog().getState() == TaskState.CANCELING || getShouldStop()) {
				getLog().taskEnded(ResultType.CANCELED, ResultType.CANCELED.getMessageI18N());
			} else if (currentResult.hasWarnings()) {
				getLog().taskEnded(ResultType.WARNING, ResultType.WARNING.getMessageI18N());
			} else {
				getLog().taskEnded(ResultType.SUCCESS, ResultType.SUCCESS.getMessageI18N());
			}
			return;
		}
	}

	private void cleanupOnThrowable(Throwable exception) {
		if (getLog().getCurrentResult().getResultType() == ResultType.NOT_FINISHED) {
			getLog().taskEnded(ResultType.ERROR, I18NConstants.UNEXPECTED_ERROR, exception);
		}
	}

}
