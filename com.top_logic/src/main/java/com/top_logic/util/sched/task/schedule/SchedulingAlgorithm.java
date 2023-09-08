/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.util.Calendar;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.util.sched.task.Task;

/**
 * Calculates the next run time for a task.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface SchedulingAlgorithm {

	/**
	 * Special time value that stands for "no value".
	 */
	long NO_SCHEDULE = 0;

	/**
	 * Calculates the next point in time for a task to run.
	 * 
	 * @param notBefore
	 *        The current time meaning that no schedules should be returned that are in the past
	 *        (before the current time).
	 * @param lastSchedule
	 *        The last run time, or {@link #NO_SCHEDULE}, if this is the first run.
	 * @return The next time to run, or {@link #NO_SCHEDULE} to indicate, that there is no next
	 *         schedule.
	 */
	long nextSchedule(long notBefore, long lastSchedule);

	/**
	 * Calculates the next run time for the given {@link Task}.
	 * <p>
	 * </p>
	 * 
	 * @param notBefore
	 *        The current time meaning that no schedules should be returned that are in the past
	 *        (before the current time).
	 * @param lastSchedule
	 *        The last time the {@link Task} was run. {@link Maybe#none()} means: The {@link Task}
	 *        has not run, yet. The given {@link Calendar} must never be manipulated.
	 * @return The next run time. {@link Maybe#none()} means: The {@link Task} should not run again.
	 *         Must return a new {@link Calendar} object, if not {@link Maybe#none()} is returned.
	 * 
	 * @deprecated Implement {@link #nextSchedule(long, long)} directly.
	 */
	@Deprecated
	default Maybe<Calendar> nextSchedule(Calendar notBefore, Maybe<Calendar> lastSchedule) {
		return Compatibility.wrapOptional(nextSchedule(Compatibility.unwrap(notBefore), Compatibility.unwrap(lastSchedule)));
	}

	/**
	 * Fills the {@link FormGroup} for the GUI representation of this {@link SchedulingAlgorithm}.
	 */
	void fillFormGroup(FormGroup group);

}
