/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * {@link BarrierTestTask} that runs code set by the test when it is
 * {@link Task#attachTo(Scheduler) attached} to a {@link Scheduler}.
 *
 * <p>
 * Lets a test block the attaching of a {@link Task}, e.g. to call the {@link Scheduler} API
 * concurrently while the {@link Task} is being attached.
 * </p>
 *
 * @see #setAttachHook(Runnable)
 */
public class AttachHookTestTask extends BarrierTestTask {

	private volatile Runnable _attachHook;

	/** Called by the typed configuration for creating a {@link AttachHookTestTask}. */
	@CalledByReflection
	public AttachHookTestTask(InstantiationContext context, BarrierTestTaskConfig config) {
		super(context, config);
	}

	/**
	 * Sets the code to run at the end of {@link Task#attachTo(Scheduler)}.
	 *
	 * @param attachHook
	 *        <code>null</code> for attaching without additional code.
	 */
	public void setAttachHook(Runnable attachHook) {
		_attachHook = attachHook;
	}

	@Override
	protected void onAttachToScheduler() {
		super.onAttachToScheduler();
		Runnable attachHook = _attachHook;
		if (attachHook != null) {
			attachHook.run();
		}
	}

}
