/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.active;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;

/**
 * Whether a {@link Task} is {@link Scheduler#isActive(Task) active}, and whether this state is its
 * {@link Scheduler#isConfiguredActive(Task) configured state} or an
 * {@link Scheduler#hasActiveOverride(Task) override of an administrator}.
 *
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public enum ActiveState {

	/**
	 * The task is active by configuration, and it either has no subtasks or all of them are
	 * active.
	 */
	@Label("Active (configured)")
	ACTIVE_CONFIGURED,

	/**
	 * An administrator has activated the task, which is inactive by configuration, and it either
	 * has no subtasks or all of them are active.
	 */
	@Label("Active (administrator)")
	ACTIVE_ADMINISTRATOR,

	/**
	 * The task is active, but a (direct or indirect) subtask is inactive.
	 */
	@Label("Active, subtask inactive")
	CHILD_INACTIVE,

	/** The task is inactive by configuration. */
	@Label("Inactive (configured)")
	INACTIVE_CONFIGURED,

	/** An administrator has deactivated the task, which is active by configuration. */
	@Label("Inactive (administrator)")
	INACTIVE_ADMINISTRATOR;

	/**
	 * The {@link ActiveState} of the given {@link Task}.
	 */
	public static ActiveState of(Task task) {
		Scheduler scheduler = Scheduler.getSchedulerInstance();
		boolean override = scheduler.hasActiveOverride(task);
		if (!scheduler.isActive(task)) {
			return override ? INACTIVE_ADMINISTRATOR : INACTIVE_CONFIGURED;
		}
		if (hasInactiveChild(scheduler, task)) {
			return CHILD_INACTIVE;
		}
		return override ? ACTIVE_ADMINISTRATOR : ACTIVE_CONFIGURED;
	}

	private static boolean hasInactiveChild(Scheduler scheduler, Task task) {
		if (!(task instanceof CompositeTask)) {
			return false;
		}
		for (Task child : ((CompositeTask) task).getChildren()) {
			if (!scheduler.isActive(child) || hasInactiveChild(scheduler, child)) {
				return true;
			}
		}
		return false;
	}

}
