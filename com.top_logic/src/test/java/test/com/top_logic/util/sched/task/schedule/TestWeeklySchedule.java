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

import com.top_logic.basic.Day;
import com.top_logic.basic.col.Maybe;
import com.top_logic.util.sched.task.schedule.WeeklySchedule;

/**
 * Tests for: {@link WeeklySchedule}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestWeeklySchedule extends ScheduleTestsCommon {

	private static final Maybe<Calendar> NO_LAST_SCHEDULE = Maybe.<Calendar> none();

	public void testWeeklySchedule() {
		WeeklySchedule<?> schedule = createWeeklySchedule(Day.SATURDAY, 6, 30);

		// Multiple days and therefore schedule cycles ago.
		Maybe<Calendar> longAgo = Maybe.some(previousWeek(previousWeek(previousWeek(calendar(6, 30)))));

		// Exactly on time for the previous schedule.
		Maybe<Calendar> oneScheduleAgo = Maybe.some(previousWeek(calendar(6, 30)));

		// Yesterday: Between the previous schedule and now.
		Maybe<Calendar> oneDayAgo = Maybe.some(previousDay(calendar(6, 30)));

		// Today between the previous schedule and now.
		Maybe<Calendar> someHoursAgo = Maybe.some(calendar(4, 0));

		List<Maybe<Calendar>> lastSchedules =
			Arrays.asList(NO_LAST_SCHEDULE, longAgo, oneScheduleAgo, oneDayAgo, someHoursAgo);

		for (Maybe<Calendar> lastSchedule : lastSchedules) {
			// One day before the scheduling window
			assertNextSchedule(schedule, previousDay(calendar(6, 0)), lastSchedule, calendar(6, 30));
			assertNextSchedule(schedule, previousDay(calendar(6, 30)), lastSchedule, calendar(6, 30));
			assertNextSchedule(schedule, previousDay(calendar(7, 0)), lastSchedule, calendar(6, 30));
			// Before the scheduling window, but at the same day
			assertNextSchedule(schedule, calendar(0, 0), lastSchedule, calendar(6, 30));
			assertNextSchedule(schedule, calendar(6, 29), lastSchedule, calendar(6, 30));
			assertNextSchedule(schedule, calendar(6, 29, 59), lastSchedule, calendar(6, 30));
			// In the scheduling window
			assertNextSchedule(schedule, calendar(6, 30), lastSchedule, calendar(6, 30));
			assertNextSchedule(schedule, calendar(6, 31), lastSchedule, nextWeek(calendar(6, 30)));
			// After the scheduling window, but at the same day
			assertNextSchedule(schedule, calendar(6, 31, 01), lastSchedule, nextWeek(calendar(6, 30)));
			assertNextSchedule(schedule, calendar(6, 32), lastSchedule, nextWeek(calendar(6, 30)));
			assertNextSchedule(schedule, calendar(7, 00), lastSchedule, nextWeek(calendar(6, 30)));
			assertNextSchedule(schedule, calendar(23, 59), lastSchedule, nextWeek(calendar(6, 30)));
			// One day after the scheduling window
			assertNextSchedule(schedule, nextDay(calendar(6, 0)), lastSchedule, nextWeek(calendar(6, 30)));
			assertNextSchedule(schedule, nextDay(calendar(6, 30)), lastSchedule, nextWeek(calendar(6, 30)));
			assertNextSchedule(schedule, nextDay(calendar(7, 0)), lastSchedule, nextWeek(calendar(6, 30)));
		}
	}

	public void testNextPeriod() {
		WeeklySchedule<?> schedule = createWeeklySchedule(Day.SATURDAY, 6, 30);
		assertNextSchedule(schedule, calendar(6, 30), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 29), Maybe.some(calendar(6, 30)), nextWeek(calendar(6, 30)));
	}

	private WeeklySchedule<?> createWeeklySchedule(Day day, int hour, int minute) {
		WeeklySchedule.Config<?> config = createConfig(WeeklySchedule.Config.class);
		setProperty(config, WeeklySchedule.Config.PROPERTY_NAME_DAY_OF_WEEK, day);
		setProperty(config, WeeklySchedule.Config.PROPERTY_NAME_TIME_OF_DAY, timeOfDay(hour, minute));
		return createInstance(config);
	}

	/**
	 * All of the tests in this class with the necessary setup wrapped around them.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestWeeklySchedule.class);
	}

}
