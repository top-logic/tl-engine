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
import com.top_logic.util.sched.task.schedule.DailySchedule;

/**
 * Tests for: {@link DailySchedule}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestDailySchedule extends ScheduleTestsCommon {

	private static final Maybe<Calendar> NO_LAST_SCHEDULE = Maybe.<Calendar> none();

	public void testDailySchedule() {
		DailySchedule<?> schedule = createDailySchedule(6, 30);

		// Multiple days and therefore schedule cycles ago.
		Maybe<Calendar> longAgo = Maybe.some(previousDay(previousDay(previousDay(calendar(6, 30)))));

		// Exactly on time for the previous schedule.
		Maybe<Calendar> oneScheduleAgo = Maybe.some(previousDay(calendar(6, 30)));

		// Somewhere between the previous schedule and now.
		Maybe<Calendar> someHoursAgo = Maybe.some(calendar(4, 0));

		@SuppressWarnings("unchecked")
		List<Maybe<Calendar>> lastSchedules = Arrays.asList(NO_LAST_SCHEDULE, longAgo, oneScheduleAgo, someHoursAgo);

		for (Maybe<Calendar> lastSchedule : lastSchedules) {
			// Before the scheduling window
			assertNextSchedule(schedule, calendar(0, 0), lastSchedule, calendar(6, 30));
			assertNextSchedule(schedule, calendar(6, 29), lastSchedule, calendar(6, 30));
			assertNextSchedule(schedule, calendar(6, 29, 59), lastSchedule, calendar(6, 30));
			// In the scheduling window
			assertNextSchedule(schedule, calendar(6, 30), lastSchedule, calendar(6, 30));
			assertNextSchedule(schedule, calendar(6, 31), lastSchedule, nextDay(calendar(6, 30)));
			// After the scheduling window
			assertNextSchedule(schedule, calendar(6, 31, 01), lastSchedule, nextDay(calendar(6, 30)));
			assertNextSchedule(schedule, calendar(6, 32), lastSchedule, nextDay(calendar(6, 30)));
			assertNextSchedule(schedule, calendar(7, 00), lastSchedule, nextDay(calendar(6, 30)));
			assertNextSchedule(schedule, calendar(23, 59), lastSchedule, nextDay(calendar(6, 30)));
		}
	}

	public void testNextPeriod() {
		DailySchedule<?> schedule = createDailySchedule(6, 30);
		assertNextSchedule(schedule, calendar(6, 30), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 29), Maybe.some(calendar(6, 30)), nextDay(calendar(6, 30)));
	}

	private DailySchedule<?> createDailySchedule(int hour, int minute) {
		DailySchedule.Config<?> config = createConfig(DailySchedule.Config.class);
		setProperty(config, DailySchedule.Config.PROPERTY_NAME_TIME_OF_DAY, timeOfDay(hour, minute));
		return createInstance(config);
	}

	/**
	 * All of the tests in this class with the necessary setup wrapped around them.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDailySchedule.class);
	}

}
