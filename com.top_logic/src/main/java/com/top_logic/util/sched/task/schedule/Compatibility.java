/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.util.Calendar;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Helpers for implementing deprecated methods such as
 * {@link SchedulingAlgorithm#nextSchedule(Calendar, Maybe)}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class Compatibility {

	static long unwrap(Calendar now) {
		return now.getTimeInMillis();
	}

	static long unwrap(Maybe<Calendar> lastSchedule) {
		return lastSchedule.hasValue() ? unwrap(lastSchedule.get()) : SchedulingAlgorithm.NO_SCHEDULE;
	}

	static Maybe<Calendar> wrapOptional(long nextSchedule) {
		return nextSchedule == SchedulingAlgorithm.NO_SCHEDULE ? Maybe.none()
			: Maybe.some(wrap(nextSchedule));
	}

	public static Calendar wrap(long nextSchedule) {
		return CalendarUtil.createCalendar(nextSchedule);
	}

}
