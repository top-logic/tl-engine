/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.entry.comparator;

import java.util.Comparator;

import com.top_logic.util.sched.entry.SchedulerEntry;
import com.top_logic.util.sched.task.Task;

/**
 * {@link Comparator} for {@link SchedulerEntry} that redirects to a {@link Comparator} for
 * {@link Task}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SchedulerEntryByTaskComparator implements Comparator<SchedulerEntry> {

	private final Comparator<? super Task> _taskComparator;

	/**
	 * Create a {@link SchedulerEntryByTaskComparator} that uses the given {@link Task}
	 * {@link Comparator}.
	 */
	public SchedulerEntryByTaskComparator(Comparator<? super Task> taskComparator) {
		_taskComparator = taskComparator;
	}

	@Override
	public int compare(SchedulerEntry left, SchedulerEntry right) {
		return _taskComparator.compare(left.getTask(), right.getTask());
	}

}
