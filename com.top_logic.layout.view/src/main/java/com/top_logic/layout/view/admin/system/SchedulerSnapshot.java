/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.Comparator;
import java.util.List;

import com.top_logic.util.TLCollator;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * The top-level tasks of the {@link Scheduler}, as read at one point in time - the value a table of
 * the tasks displays.
 *
 * <p>
 * The tasks themselves are singletons whose state changes in place: activating, starting or
 * running a task leaves the task the same object. A snapshot therefore stands for the reading, not
 * for the tasks: two snapshots are never equal, even over the same tasks, so that writing a new one
 * to a channel always tells its listeners to read the state of the tasks again.
 * </p>
 */
public final class SchedulerSnapshot {

	private final List<Task> _tasks;

	private SchedulerSnapshot(List<Task> tasks) {
		_tasks = tasks;
	}

	/**
	 * A snapshot of the top-level tasks of the {@link Scheduler}, sorted by their
	 * {@link Scheduler#getTaskLabel(Task) label}.
	 */
	public static SchedulerSnapshot current() {
		return new SchedulerSnapshot(sorted(Scheduler.getSchedulerInstance().getKnownTopLevelTasks()));
	}

	/**
	 * The given tasks in the order of their {@link Scheduler#getTaskLabel(Task) labels}.
	 */
	static List<Task> sorted(List<? extends Task> tasks) {
		Comparator<String> collator = new TLCollator();
		return tasks.stream()
			.sorted(Comparator.comparing(Scheduler::getTaskLabel, collator))
			.map(Task.class::cast)
			.toList();
	}

	/**
	 * The top-level tasks of this snapshot, sorted by their {@link Scheduler#getTaskLabel(Task)
	 * label}.
	 */
	public List<Task> getTasks() {
		return _tasks;
	}

	@Override
	public String toString() {
		return "SchedulerSnapshot(" + _tasks.size() + " tasks)";
	}

}
