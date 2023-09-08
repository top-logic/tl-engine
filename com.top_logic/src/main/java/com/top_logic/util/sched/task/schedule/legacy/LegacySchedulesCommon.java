/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule.legacy;

import java.util.Calendar;
import java.util.List;

import com.top_logic.basic.Day;
import com.top_logic.basic.Log;
import com.top_logic.layout.form.FormField;
import com.top_logic.util.Resources;

/**
 * Code common to the 'LegacyFooSchedule' classes.
 * <p>
 * Deprecated as all the code in this package, as it is legacy stuff which should not be used for
 * new code, but should be removed, due to its missing quality.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public class LegacySchedulesCommon {

	/** The name prefix for all the {@link FormField}s of the legacy schedules. */
	public static final String NAME_FIELD_PREFIX = "legacySchedule";

	/**
	 * Internal method to calculate time (not date) when next schedule should start.
	 * 
	 * Must not be called when next is the current day.
	 * 
	 * @param next
	 *        Contains the correct day, but unspecified time, will be updated to some reasonable
	 *        time here.
	 */
	public static void calcStartTime(Calendar next, int hour, int minute) {
		next.set(Calendar.HOUR_OF_DAY, hour);
		next.set(Calendar.MINUTE, minute);
	}

	/**
	 * Internal method to calculate next time for PERIODICALLY
	 * 
	 * @param next
	 *        Contains the last time the Task was scheduled.
	 * @return false when end of day Period was reached or periodically is not set at all.
	 * 
	 */
	public static boolean calcNextTime(Calendar next, int daytype, long interval, int stopHour, int stopMinute) {
		if (0 == (daytype & PERIODICALLY)) {
			return false;
		}

		int curDay = next.get(Calendar.DAY_OF_YEAR);
		next.setTimeInMillis(next.getTimeInMillis() + interval);
		if (curDay != next.get(Calendar.DAY_OF_YEAR)) { // went into next day
			// roll back to previous day to avoid roll over to after tomorrow
			next.setTimeInMillis(next.getTimeInMillis() - interval);
			return false;
		}

		int curHour = next.get(Calendar.HOUR_OF_DAY);
		if (curHour > stopHour) {
			return false;
		}
		if ((curHour == stopHour) && (next.get(Calendar.MINUTE) > stopMinute)) {
			return false;
		}
		// curHour < stopHour
		return true;
	}

	/**
	 * Internal method to calculate time (not date) for DAILY tasks.
	 * 
	 * This will skip to the current hour/minute in when desired schedule is to early on same
	 * day.
	 * 
	 * @param next
	 *        Contains the correct day, but unspecified time, will be updated to some reasonable
	 *        time here.
	 */
	public static void calcDailyStartTime(Calendar next, int hour, int minute, int stopHour, int stopMinute) {
		int theHour = next.get(Calendar.HOUR_OF_DAY);
		int theMinute = next.get(Calendar.MINUTE);
		if (stopHour != 0
			&& (theHour > stopHour // to late for today
			|| (theHour == stopHour && theMinute >= stopMinute))) {
			next.add(Calendar.DAY_OF_YEAR, 1);
			next.set(Calendar.HOUR_OF_DAY, hour);
			next.set(Calendar.MINUTE, minute);
			return;
		}
		if (theHour < hour) {
			theHour = hour; // start later
			theMinute = minute;
		}
		else if (theHour == hour) // check the minute in the current hour, too
		{
			if (theMinute < minute)
				theMinute = minute; // start later in that hour
		}
		// else start immediately
		next.set(Calendar.HOUR_OF_DAY, theHour);
		next.set(Calendar.MINUTE, theMinute);
	}

	/** Converts a {@link Day} to the corresponding {@link Calendar#DAY_OF_WEEK} value. */
	public static int getDayAsInt(Day d) {
		// such strange as 0 represents Sunday which has ordinal 6
		return (d.ordinal() + 1) % 7;
	}

	/** Converts a {@link List} of {@link Day}s to a bitmask. */
	public static int calcDayMask(Log aProtocol, List<Day> days) {
		if (days.isEmpty()) {
			aProtocol.error("no days given but needed for monthly and weekly schedules.");
			return 0;
		}
		int mask = 0;
		for (Day d : days) {
			mask |= 1 << getDayAsInt(d);
		}
		return mask;
	}

	/**
	 * Formats the given time as '01:01' for example.
	 */
	public static String formatTime(int hour, int minute) {
		return String.format("%02d:%02d", hour, minute);
	}

	/**
	 * Is the {@link #PERIODICALLY} flag set in the given "daytype" field?
	 */
	public static boolean isPeriodically(int daytype) {
		return 0 != (daytype & PERIODICALLY);
	}

	/** The internationalized text for "this field has no value". */
	public static String getFieldValueNoValue() {
		return Resources.getInstance().getString(I18NConstants.NO_FIELD_VALUE);
	}

	/**
	 * Indicates that a task will run only once this or the next Day. The day the task is running is
	 * determined by the difference between the actual and the given time (e.g. if it is 12'o clock
	 * and the task should be running at 10'o clock, it will be scheduled tomorrow).
	 * 
	 * used for Day and inter Day Scheduling.
	 */
	public static final int ONCE = 0;

	/** Indicates that a task will run only once at some specific day.
	 *  Is NOT identically to ONCE, if the day is over, the task will not be
	 *  run.
	 */
	public static final int DATE = 1;

	/** Indicates that a task will run monthly */
	public static final int MONTHLY = 2;

	/** Indicates that a task will run WEEKLY */
	public static final int WEEKLY = 3;

	/** Indicates that a task will run every Day */
	public static final int DAILY = 4;

	/** Mask for Scheduling in days, leaves 255 variants */
	public static final int DAY_MASK = 0x00FF;

	/**
	 * Indicates that the task should run periodically a day.
	 * 
	 * Starting at some time and stopping at some other,
	 */
	public static final int PERIODICALLY = 0x0100;

}
