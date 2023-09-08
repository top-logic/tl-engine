/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import com.top_logic.layout.form.model.FormGroup;

/**
 * {@link SchedulingAlgorithm} that never creates a schedule.
 * 
 * <p>
 * This algorithm should never be configured explicitly. It is only implicitly used, if a task has
 * an empty schedule, see {@link com.top_logic.util.sched.task.Task.Config#getSchedules()}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class NeverSchedule implements SchedulingAlgorithm {

	/**
	 * Singleton {@link NeverSchedule} instance.
	 */
	public static final NeverSchedule INSTANCE = new NeverSchedule();

	private NeverSchedule() {
		// Singleton constructor.
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		return NO_SCHEDULE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		// No properties.
	}

}
