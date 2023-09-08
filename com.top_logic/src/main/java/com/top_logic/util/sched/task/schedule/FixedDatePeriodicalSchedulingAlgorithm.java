/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.util.Calendar;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Base class for {@link SchedulingAlgorithm}s that schedule at a fixed time with a fixed period.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class FixedDatePeriodicalSchedulingAlgorithm<C extends AbstractSchedulingAlgorithm.Config<?>>
		extends AbstractSchedulingAlgorithm<C> {

	/**
	 * Creates a {@link FixedDatePeriodicalSchedulingAlgorithm} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FixedDatePeriodicalSchedulingAlgorithm(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		Calendar result;
		if (lastSchedule != NO_SCHEDULE) {
			result = CalendarUtil.createCalendar(lastSchedule);
			setScheduleTime(result);
			if (result.getTimeInMillis() <= lastSchedule) {
				addPeriod(result);
			}
			long nextMillis = result.getTimeInMillis();
			if (nextMillis >= notBefore) {
				return nextMillis;
			}

			result.setTimeInMillis(notBefore);
		} else {
			result = CalendarUtil.createCalendar(notBefore);
		}

		setScheduleTime(result);
		if (result.getTimeInMillis() < notBefore) {
			addPeriod(result);
		}
		return result.getTimeInMillis();
	}

	/**
	 * Sets the scheduling time in the given calendar.
	 * 
	 * <p>
	 * Do not care whether this moves the time foreward or backwards.
	 * </p>
	 *
	 * @param result
	 *        The {@link Calendar} to modify.
	 */
	protected abstract void setScheduleTime(Calendar result);

	/**
	 * Adds the scheduling period to the given {@link Calendar}.
	 *
	 * @param result
	 *        The {@link Calendar} to modify.
	 */
	protected abstract void addPeriod(Calendar result);

}
