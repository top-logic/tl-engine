/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.task.schedule;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.col.Maybe;
import com.top_logic.util.sched.task.schedule.DailyPeriodicallySchedule;

/**
 * Tests for: {@link DailyPeriodicallySchedule}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestDailyPeriodicallySchedule extends ScheduleTestsCommon {

	private static final Maybe<Calendar> NO_LAST_SCHEDULE = Maybe.<Calendar> none();

	public void testDailyPeriodicallySchedule() {
		DailyPeriodicallySchedule<?> schedule = createDailyPeriodicallySchedule(
			timeOfDay(4, 00), timeOfDay(20, 00), 60 * 60 * 1000);

		// Multiple days and therefore schedule cycles ago.
		Maybe<Calendar> longAgo = Maybe.some(previousDay(previousDay(previousDay(calendar(12, 00)))));

		// One day ago.
		Maybe<Calendar> oneDayAgo = Maybe.some(previousDay(calendar(12, 00)));

		// at the same day before the start time.
		Maybe<Calendar> previousDayAfterStopAgo = Maybe.some(previousDay(calendar(22, 00)));

		// at the same day before the start time.
		Maybe<Calendar> sameDayBeforeStartAgo = Maybe.some(calendar(2, 00));

		// multiple schedule ago, but at the same day.
		Maybe<Calendar> multipleScheduleAgo = Maybe.some(calendar(9, 00));

		// Exactly on time for the previous schedule.
		Maybe<Calendar> oneScheduleAgo = Maybe.some(calendar(11, 00));

		// Somewhere between the previous schedule and now.
		Maybe<Calendar> someTimeAgo = Maybe.some(calendar(11, 30));

		@SuppressWarnings("unchecked")
		List<Maybe<Calendar>> lastSchedules =
			Arrays.asList(NO_LAST_SCHEDULE, longAgo, oneDayAgo, previousDayAfterStopAgo, sameDayBeforeStartAgo,
				multipleScheduleAgo, oneScheduleAgo, someTimeAgo);

		for (Maybe<Calendar> lastSchedule : lastSchedules) {
			// Before the scheduling window
			assertNextSchedule(schedule, calendar(11, 40), lastSchedule, calendar(12, 0));
			assertNextSchedule(schedule, calendar(11, 59), lastSchedule, calendar(12, 0));
			assertNextSchedule(schedule, calendar(11, 59, 59), lastSchedule, calendar(12, 0));
			// In the scheduling window
			assertNextSchedule(schedule, calendar(12, 0), lastSchedule, calendar(12, 0));
			assertNextSchedule(schedule, calendar(12, 1), lastSchedule, calendar(13, 0));
			// After the scheduling window
			assertNextSchedule(schedule, calendar(12, 1, 01), lastSchedule, calendar(13, 0));
			assertNextSchedule(schedule, calendar(12, 2), lastSchedule, calendar(13, 0));
			assertNextSchedule(schedule, calendar(12, 20), lastSchedule, calendar(13, 0));
		}
		/* The 'lastSchedule' must not be after 'now'. The earliest value for now is '0:00' for the
		 * following tests. Therefore, don't use values for 'lastSchedule' that are before that. */
		@SuppressWarnings("unchecked")
		List<Maybe<Calendar>> lastSchedulesForEdgeCases =
			Arrays.asList(NO_LAST_SCHEDULE, longAgo, oneDayAgo, previousDayAfterStopAgo, sameDayBeforeStartAgo);
		for (Maybe<Calendar> lastSchedule : lastSchedulesForEdgeCases) {
			// Before and at the first scheduling window
			assertNextSchedule(schedule, calendar(0, 0), lastSchedule, calendar(4, 0));
			assertNextSchedule(schedule, calendar(3, 0), lastSchedule, calendar(4, 0));
			assertNextSchedule(schedule, calendar(4, 0), lastSchedule, calendar(4, 0));
			// At and after the last scheduling window
			assertNextSchedule(schedule, calendar(20, 0), lastSchedule, calendar(20, 0));
			assertNextSchedule(schedule, calendar(21, 0), lastSchedule, nextDay(calendar(4, 0)));
			assertNextSchedule(schedule, calendar(23, 59), lastSchedule, nextDay(calendar(4, 0)));
		}
	}

	private DailyPeriodicallySchedule<?> createDailyPeriodicallySchedule(
			Date startTime, Date stopTime, int interval) {
		DailyPeriodicallySchedule.Config<?> config = createConfig(DailyPeriodicallySchedule.Config.class);
		setProperty(config, DailyPeriodicallySchedule.Config.PROPERTY_NAME_START_TIME, startTime);
		setProperty(config, DailyPeriodicallySchedule.Config.PROPERTY_NAME_STOP_TIME, stopTime);
		setProperty(config, DailyPeriodicallySchedule.Config.PROPERTY_NAME_INTERVAL, interval);
		return createInstance(config);
	}

	/**
	 * All of the tests in this class with the necessary setup wrapped around them.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDailyPeriodicallySchedule.class);
	}

}
