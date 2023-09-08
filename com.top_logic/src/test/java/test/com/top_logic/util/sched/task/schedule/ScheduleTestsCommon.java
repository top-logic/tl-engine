/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.task.schedule;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * Code useful for all the {@link SchedulingAlgorithm} tests.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class ScheduleTestsCommon extends TestCase {

	/**
	 * Is the next schedule at the expected time?
	 */
	protected void assertNextSchedule(SchedulingAlgorithm schedule, Calendar now,
			Maybe<Calendar> lastSchedule, Calendar nextScheduleExpected) {
		assertNextSchedule(schedule, now, lastSchedule, Maybe.some(nextScheduleExpected));
	}

	/**
	 * Is the next schedule at the expected time?
	 */
	protected void assertNextSchedule(SchedulingAlgorithm schedule, Calendar now,
			Maybe<Calendar> lastSchedule, Maybe<Calendar> nextScheduleExpected) {
		Maybe<Calendar> nextScheduleActual = schedule.nextSchedule(now, lastSchedule);
		if (nextScheduleExpected.hasValue() && !nextScheduleActual.hasValue()) {
			fail("Expected a next schedule, but there is none. Expected: " + readable(nextScheduleExpected)
				+ "; Last Schedule: " + readable(lastSchedule));
		}
		if (nextScheduleActual.hasValue() && !nextScheduleExpected.hasValue()) {
			fail("Expected no next schedule, but there is one. Actual: " + readable(nextScheduleActual)
				+ "; Last Schedule: " + readable(lastSchedule));
		}
		if ((!nextScheduleExpected.hasValue()) && (!nextScheduleActual.hasValue())) {
			return;
		}
		if (!nextScheduleExpected.get().equals(nextScheduleActual.get())) {
			fail("Unexpected next schedule. Expected: " + readable(nextScheduleExpected) + "; Actual: "
				+ readable(nextScheduleActual) + "; Last Schedule: " + readable(lastSchedule));
		}
	}

	/**
	 * Writes the {@link Calendar} to a {@link String} by converting it to a date first, as
	 * {@link Calendar#toString()} writes way too much information.
	 * <p>
	 * If the given value is {@link Maybe#none()}, the result is "None".
	 * </p>
	 */
	protected String readable(Maybe<Calendar> maybeCalendar) {
		if (!maybeCalendar.hasValue()) {
			return "None";
		}
		return readable(maybeCalendar.get());
	}

	/**
	 * Writes the {@link Calendar} to a {@link String} by converting it to a date first, as
	 * {@link Calendar#toString()} writes way too much information.
	 */
	protected String readable(Calendar calendar) {
		// Calendar.toString() is unreadable, Date.toString() is much better.
		return calendar.getTime().toString();
	}

	/**
	 * Creates a date with the given time.
	 * <p>
	 * The date (i.e. the day) is unspecified. The second and millisecond part of the time is set to
	 * 0.
	 * </p>
	 */
	protected Date timeOfDay(int hour, int minute) {
		Date timeOfDay = new Date(0);
		timeOfDay.setHours(hour);
		timeOfDay.setMinutes(minute);
		return timeOfDay;
	}

	/**
	 * Move the {@link Calendar} forward by one week.
	 * <p>
	 * Returns the given {@link Calendar} for easier method chaining.
	 * </p>
	 */
	protected Calendar nextWeek(Calendar calendar) {
		calendar.add(Calendar.WEEK_OF_YEAR, 1);
		return calendar;
	}

	/**
	 * Move the {@link Calendar} backward by one week.
	 * <p>
	 * Returns the given {@link Calendar} for easier method chaining.
	 * </p>
	 */
	protected Calendar previousWeek(Calendar calendar) {
		calendar.add(Calendar.WEEK_OF_YEAR, -1);
		return calendar;
	}

	/**
	 * Move the {@link Calendar} forward by one day.
	 * <p>
	 * Returns the given {@link Calendar} for easier method chaining.
	 * </p>
	 */
	protected Calendar nextDay(Calendar calendar) {
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		return calendar;
	}

	/**
	 * Move the {@link Calendar} backward by one day.
	 * <p>
	 * Returns the given {@link Calendar} for easier method chaining.
	 * </p>
	 */
	protected Calendar previousDay(Calendar calendar) {
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		return calendar;
	}

	/**
	 * Creates a {@link Calendar} with the date set to 1.1.2000, which was a Saturday.
	 * <p>
	 * The second and millisecond part of the time is set to 0.
	 * </p>
	 */
	protected Calendar calendar(int hour, int minute) {
		return calendar(hour, minute, 0);
	}

	/**
	 * Creates a {@link Calendar} with the date set to 1.1.2000, which was a Saturday.
	 * <p>
	 * The millisecond part of the time is set to 0.
	 * </p>
	 */
	protected Calendar calendar(int hour, int minute, int second) {
		return calendar(2000, Calendar.JANUARY, 1, hour, minute, second);
	}

	/**
	 * Creates a {@link Calendar}.
	 * <p>
	 * The millisecond part of the time is set to 0.
	 * </p>
	 */
	protected Calendar calendar(int year, int month, int day, int hour, int minute, int second) {
		Calendar result = CalendarUtil.createCalendar();
		result.set(Calendar.YEAR, year);
		result.set(Calendar.MONTH, month);
		result.set(Calendar.DAY_OF_MONTH, day);
		result.set(Calendar.HOUR_OF_DAY, hour);
		result.set(Calendar.MINUTE, minute);
		result.set(Calendar.SECOND, second);
		result.set(Calendar.MILLISECOND, 0);
		return result;
	}

	/**
	 * Convenience shortcut for {@link #getInstantiationContext()}.
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration) getInstance}.
	 */
	protected <T> T createInstance(PolymorphicConfiguration<T> config) {
		return getInstantiationContext().getInstance(config);
	}

	/** Convenience shortcut for {@link TypedConfiguration#newConfigItem(Class)}. */
	protected <T extends ConfigurationItem> T createConfig(Class<T> configClass) {
		return TypedConfiguration.newConfigItem(configClass);
	}

	/** Sets the property with the given name to the given value. */
	protected void setProperty(ConfigurationItem config, String propertyName, Object newValue) {
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);
		config.update(property, newValue);
	}

	/** Convenience shortcut for {@link SimpleInstantiationContext#CREATE_ALWAYS_FAIL_IMMEDIATELY}. */
	protected InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

}
