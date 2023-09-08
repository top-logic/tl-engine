/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.task.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.util.sched.task.schedule.AbstractSchedulingAlgorithm.Config;
import com.top_logic.util.sched.task.schedule.DailySchedule;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithmCombinator;

/**
 * Tests for: {@link SchedulingAlgorithmCombinator}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestSchedulingAlgorithmCombinator extends ScheduleTestsCommon {

	private static final Maybe<Calendar> NO_LAST_SCHEDULE = Maybe.<Calendar> none();

	private static final Maybe<Calendar> NO_NEXT_SCHEDULE = Maybe.<Calendar> none();

	public void testNoChildren() {
		List<PolymorphicConfiguration<? extends SchedulingAlgorithm>> noChildren =
			Collections.<PolymorphicConfiguration<? extends SchedulingAlgorithm>> emptyList();
		SchedulingAlgorithm schedule = createSchedule(noChildren);
		// Without children, there is never a next schedule.
		assertNextSchedule(schedule, calendar(0, 0), NO_LAST_SCHEDULE, NO_NEXT_SCHEDULE);
	}

	public void testSingleChild() {
		DailySchedule.Config<?> childConfig = createDailySchedule(6, 30);
		List<PolymorphicConfiguration<? extends SchedulingAlgorithm>> singleChild =
			Collections.<PolymorphicConfiguration<? extends SchedulingAlgorithm>> singletonList(childConfig);
		SchedulingAlgorithm schedule = createSchedule(singleChild);

		// Before the scheduling window
		assertNextSchedule(schedule, calendar(6, 0), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 29, 59), NO_LAST_SCHEDULE, calendar(6, 30));
		// In the scheduling window
		assertNextSchedule(schedule, calendar(6, 30), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 31), NO_LAST_SCHEDULE, nextDay(calendar(6, 30)));
		// After the scheduling window
		assertNextSchedule(schedule, calendar(6, 31, 1), NO_LAST_SCHEDULE, nextDay(calendar(6, 30)));
		assertNextSchedule(schedule, calendar(7, 0), NO_LAST_SCHEDULE, nextDay(calendar(6, 30)));
	}

	public void testMultipleChildren() {
		List<Config<?>> children = new ArrayList<>();
		children.add(createDailySchedule(5, 30));
		children.add(createDailySchedule(6, 30));
		children.add(createDailySchedule(7, 30));
		SchedulingAlgorithm schedule = createSchedule(children);

		// Before the first scheduling window
		assertNextSchedule(schedule, calendar(5, 0), NO_LAST_SCHEDULE, calendar(5, 30));
		assertNextSchedule(schedule, calendar(5, 29, 59), NO_LAST_SCHEDULE, calendar(5, 30));
		// In the first scheduling window
		assertNextSchedule(schedule, calendar(5, 30), NO_LAST_SCHEDULE, calendar(5, 30));
		assertNextSchedule(schedule, calendar(5, 31), NO_LAST_SCHEDULE, calendar(6, 30));
		// Between the first and the second scheduling window
		assertNextSchedule(schedule, calendar(5, 31, 1), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 0), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 29, 59), NO_LAST_SCHEDULE, calendar(6, 30));
		// In the second scheduling window
		assertNextSchedule(schedule, calendar(6, 30), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 31), NO_LAST_SCHEDULE, calendar(7, 30));
		// Between the second and the third scheduling window
		assertNextSchedule(schedule, calendar(6, 31, 1), NO_LAST_SCHEDULE, calendar(7, 30));
		assertNextSchedule(schedule, calendar(7, 0), NO_LAST_SCHEDULE, calendar(7, 30));
		assertNextSchedule(schedule, calendar(7, 29, 59), NO_LAST_SCHEDULE, calendar(7, 30));
		// In the third scheduling window
		assertNextSchedule(schedule, calendar(7, 30), NO_LAST_SCHEDULE, calendar(7, 30));
		assertNextSchedule(schedule, calendar(7, 31), NO_LAST_SCHEDULE, nextDay(calendar(5, 30)));
		// After the third and last scheduling window
		assertNextSchedule(schedule, calendar(7, 31, 1), NO_LAST_SCHEDULE, nextDay(calendar(5, 30)));
		assertNextSchedule(schedule, calendar(8, 0), NO_LAST_SCHEDULE, nextDay(calendar(5, 30)));
	}

	public void testOverlappingSchedulingWindows() {
		List<Config<?>> children = new ArrayList<>();
		children.add(createDailySchedule(6, 30));
		children.add(createDailySchedule(6, 32));
		SchedulingAlgorithm schedule = createSchedule(children);

		// Before the scheduling window
		assertNextSchedule(schedule, calendar(6, 0), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 29, 59), NO_LAST_SCHEDULE, calendar(6, 30));
		// In the scheduling window
		assertNextSchedule(schedule, calendar(6, 30), NO_LAST_SCHEDULE, calendar(6, 30));
		assertNextSchedule(schedule, calendar(6, 32), NO_LAST_SCHEDULE, calendar(6, 32));
		assertNextSchedule(schedule, calendar(6, 35), NO_LAST_SCHEDULE, nextDay(calendar(6, 30)));
		assertNextSchedule(schedule, calendar(6, 35, 1), NO_LAST_SCHEDULE, nextDay(calendar(6, 30)));
		assertNextSchedule(schedule, calendar(6, 37), NO_LAST_SCHEDULE, nextDay(calendar(6, 30)));
		// After the scheduling window
		assertNextSchedule(schedule, calendar(6, 37, 1), NO_LAST_SCHEDULE, nextDay(calendar(6, 30)));
		assertNextSchedule(schedule, calendar(7, 0), NO_LAST_SCHEDULE, nextDay(calendar(6, 30)));
	}

	private DailySchedule.Config<?> createDailySchedule(int hour, int minute) {
		DailySchedule.Config<?> config = createConfig(DailySchedule.Config.class);
		setProperty(config, DailySchedule.Config.PROPERTY_NAME_TIME_OF_DAY, timeOfDay(hour, minute));
		return config;
	}

	protected SchedulingAlgorithm createSchedule(
			List<? extends PolymorphicConfiguration<? extends SchedulingAlgorithm>> childConfigs) {
		return SchedulingAlgorithmCombinator.combine(getInstantiationContext(), childConfigs);
	}

	/**
	 * All of the tests in this class with the necessary setup wrapped around them.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestSchedulingAlgorithmCombinator.class);
	}

}
