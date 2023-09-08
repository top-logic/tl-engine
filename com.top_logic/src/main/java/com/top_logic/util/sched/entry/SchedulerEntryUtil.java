/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.entry;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.util.sched.task.Task;

/**
 * Utilities for working with {@link SchedulerEntry}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SchedulerEntryUtil {

	/**
	 * Convert the given {@link Collection} of {@link SchedulerEntry}s to a {@link List} of
	 * {@link Task}s.
	 * 
	 * @param entries
	 *        Is allowed to be <code>null</code>.
	 * @return Never null. Is modifiable and resizeable.
	 */
	public static List<Task> toTasks(Collection<SchedulerEntry> entries) {
		List<Task> tasks = new ArrayList<>();
		for (SchedulerEntry entry : nonNull(entries)) {
			tasks.add(entry.getTask());
		}
		return tasks;
	}

	/**
	 * Converts the {@link Maybe} of {@link SchedulerEntry} to a {@link Maybe} of {@link Task}.
	 */
	public static Maybe<Task> toTask(Maybe<SchedulerEntry> entry) {
		if (entry.hasValue()) {
			return Maybe.<Task> some(entry.get().getTask());
		}
		return Maybe.none();
	}

}
