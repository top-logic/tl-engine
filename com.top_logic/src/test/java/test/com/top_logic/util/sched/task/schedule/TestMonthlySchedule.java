/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.task.schedule;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.col.Maybe;
import com.top_logic.util.sched.task.schedule.MonthlySchedule;

/**
 * Tests for: {@link MonthlySchedule}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestMonthlySchedule extends ScheduleTestsCommon {

	private static final Maybe<Calendar> NO_LAST_SCHEDULE = Maybe.<Calendar> none();

	public void testMonthlySchedule() {
		MonthlySchedule<?> schedule = createMonthlySchedule(6, 6, 30);

		// Multiple days and therefore schedule cycles ago.
		Maybe<Calendar> longAgo = Maybe.some(previousMonth(previousMonth(previousMonth(calendar(6, 6, 30, 0)))));

		// Exactly on time for the previous schedule.
		Maybe<Calendar> oneScheduleAgo = Maybe.some(previousMonth(calendar(6, 6, 30, 0)));

		// Yesterday: Between the previous schedule and now.
		Maybe<Calendar> oneDayAgo = Maybe.some(previousDay(calendar(6, 6, 30, 0)));

		// Today between the previous schedule and now.
		Maybe<Calendar> someHoursAgo = Maybe.some(calendar(6, 4, 0, 0));

		List<Maybe<Calendar>> lastSchedules =
			Arrays.asList(NO_LAST_SCHEDULE, longAgo, oneScheduleAgo, oneDayAgo, someHoursAgo);

		for (Maybe<Calendar> lastSchedule : lastSchedules) {
			// One day before the scheduling window
			assertNextSchedule(schedule, previousDay(calendar(6, 6, 0, 0)), lastSchedule, calendar(6, 6, 30, 0));
			assertNextSchedule(schedule, previousDay(calendar(6, 6, 30, 0)), lastSchedule, calendar(6, 6, 30, 0));
			assertNextSchedule(schedule, previousDay(calendar(6, 7, 0, 0)), lastSchedule, calendar(6, 6, 30, 0));
			// Before the scheduling window, but at the same day
			assertNextSchedule(schedule, calendar(6, 0, 0, 0), lastSchedule, calendar(6, 6, 30, 0));
			assertNextSchedule(schedule, calendar(6, 6, 29, 0), lastSchedule, calendar(6, 6, 30, 0));
			assertNextSchedule(schedule, calendar(6, 6, 29, 59), lastSchedule, calendar(6, 6, 30, 0));
			// In the scheduling window
			assertNextSchedule(schedule, calendar(6, 6, 30, 0), lastSchedule, calendar(6, 6, 30, 0));
			assertNextSchedule(schedule, calendar(6, 6, 31, 0), lastSchedule, nextMonth(calendar(6, 6, 30, 0)));
			// After the scheduling window, but at the same day
			assertNextSchedule(schedule, calendar(6, 6, 31, 01), lastSchedule, nextMonth(calendar(6, 6, 30, 0)));
			assertNextSchedule(schedule, calendar(6, 6, 32, 0), lastSchedule, nextMonth(calendar(6, 6, 30, 0)));
			assertNextSchedule(schedule, calendar(6, 7, 00, 0), lastSchedule, nextMonth(calendar(6, 6, 30, 0)));
			assertNextSchedule(schedule, calendar(6, 23, 59, 0), lastSchedule, nextMonth(calendar(6, 6, 30, 0)));
			// One day after the scheduling window
			assertNextSchedule(schedule, nextDay(calendar(6, 6, 0, 0)), lastSchedule, nextMonth(calendar(6, 6, 30, 0)));
			assertNextSchedule(schedule, nextDay(calendar(6, 6, 30, 0)), lastSchedule,
				nextMonth(calendar(6, 6, 30, 0)));
			assertNextSchedule(schedule, nextDay(calendar(6, 7, 0, 0)), lastSchedule, nextMonth(calendar(6, 6, 30, 0)));
		}
	}

	public void testNextPeriod() {
		MonthlySchedule<?> schedule = createMonthlySchedule(7, 6, 30);
		assertNextSchedule(schedule, calendar(7, 6, 30, 0), NO_LAST_SCHEDULE, calendar(7, 6, 30, 0));
		assertNextSchedule(schedule, calendar(7, 6, 29, 0), Maybe.some(calendar(7, 6, 30, 0)),
			nextMonth(calendar(7, 6, 30, 0)));
	}

	public void testBorderCases() {
		MonthlySchedule<?> schedule = createMonthlySchedule(30, 6, 0);
		assertNextSchedule(schedule,
			calendar(2000, Calendar.JANUARY, 31, 6, 0, 0), NO_LAST_SCHEDULE,
			calendar(2000, Calendar.FEBRUARY, 29, 6, 0, 0));
		assertNextSchedule(schedule,
			calendar(2001, Calendar.JANUARY, 31, 6, 0, 0), NO_LAST_SCHEDULE,
			calendar(2001, Calendar.FEBRUARY, 28, 6, 0, 0));
	}

	private MonthlySchedule<?> createMonthlySchedule(int day, int hour, int minute) {
		MonthlySchedule.Config<?> config = createConfig(MonthlySchedule.Config.class);
		setProperty(config, MonthlySchedule.Config.PROPERTY_NAME_DAY_OF_MONTH, day);
		setProperty(config, MonthlySchedule.Config.PROPERTY_NAME_TIME_OF_DAY, timeOfDay(hour, minute));
		return createInstance(config);
	}

	/**
	 * All of the tests in this class with the necessary setup wrapped around them.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestMonthlySchedule.class);
	}

}
