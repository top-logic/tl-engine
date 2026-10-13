/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.util.sched.task.impl.StateHandlingTask;

/**
 * {@link StateHandlingTask} whose work is a callback set by the test.
 * 
 * <p>
 * Lets a test run arbitrary code inside the run of a scheduled task, e.g. to call the
 * {@link com.top_logic.util.sched.Scheduler} API that is meant to be called by the running task.
 * Without a callback, the run does nothing. The task runs on every cluster node.
 * </p>
 * 
 * @see #setCallback(Consumer)
 */
public class CallbackTestTask extends StateHandlingTask<CallbackTestTask.Config<?>> {

	/**
	 * Configuration options for {@link CallbackTestTask}.
	 */
	public interface Config<I extends CallbackTestTask> extends StateHandlingTask.Config<I> {

		@Override
		@ClassDefault(CallbackTestTask.class)
		Class<? extends I> getImplementationClass();

		/** @see #isNeedingMaintenanceMode() */
		void setNeedingMaintenanceMode(boolean value);

		/** @see #getMaintenanceModeDelay() */
		void setMaintenanceModeDelay(long value);

	}

	private volatile Consumer<? super CallbackTestTask> _callback;

	/**
	 * Creates a {@link CallbackTestTask} from configuration.
	 */
	@CalledByReflection
	public CallbackTestTask(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Sets the code to run inside the run of this task.
	 * 
	 * @param callback
	 *        Receives this task. <code>null</code> for a run that does nothing.
	 */
	public void setCallback(Consumer<? super CallbackTestTask> callback) {
		_callback = callback;
	}

	@Override
	protected void runHook() {
		Consumer<? super CallbackTestTask> callback = _callback;
		if (callback != null) {
			callback.accept(this);
		}
	}

	@Override
	public boolean isNodeLocal() {
		return true;
	}

}
