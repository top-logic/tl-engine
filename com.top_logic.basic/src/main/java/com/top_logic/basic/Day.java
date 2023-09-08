/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Calendar;

import com.top_logic.basic.config.CommaSeparatedEnum;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ExternallyNamed;

/**
 * Representation of the days in a week.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum Day implements ExternallyNamed {

	/** Representation of Monday */
	MONDAY("Monday", Calendar.MONDAY),

	/** Representation of Tuesday */
	TUESDAY("Tuesday", Calendar.TUESDAY),

	/** Representation of Wednesday */
	WEDNESDAY("Wednesday", Calendar.WEDNESDAY),

	/** Representation of Thursday */
	THURSDAY("Thursday", Calendar.THURSDAY),

	/** Representation of Friday */
	FRIDAY("Friday", Calendar.FRIDAY),

	/** Representation of Saturday */
	SATURDAY("Saturday", Calendar.SATURDAY),

	/** Representation of Sunday */
	SUNDAY("Sunday", Calendar.SUNDAY),
	
	;

	/**
	 * {@link ConfigurationValueProvider} to process comma separated lists of {@link Day}s.
	 * 
	 * @since 5.7.3
	 * 
	 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class CommaSeparatedDays extends CommaSeparatedEnum<Day> {

		/** Sole instance of {@link CommaSeparatedDays} */
		public static final CommaSeparatedDays INSTANCE = new CommaSeparatedDays();

		private CommaSeparatedDays() {
			super(Day.class);
		}
	}

	private final String _externalName;

	private final int _calendarConstant;

	private Day(String externalName, int calendarConstant) {
		this._externalName = externalName;
		_calendarConstant = calendarConstant;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Get the corresponding {@link Calendar} constant:
	 * <p>
	 * <ul>
	 * <li>{@link Calendar#MONDAY}</li>
	 * <li>{@link Calendar#TUESDAY}</li>
	 * <li>{@link Calendar#WEDNESDAY}</li>
	 * <li>{@link Calendar#THURSDAY}</li>
	 * <li>{@link Calendar#FRIDAY}</li>
	 * <li>{@link Calendar#SATURDAY}</li>
	 * <li>{@link Calendar#SUNDAY}</li>
	 * </ul>
	 * These constants are used for example in {@link Calendar#get(int)},
	 * {@link Calendar#set(int, int)}, {@link Calendar#add(int, int)} and
	 * {@link Calendar#roll(int, boolean)}
	 * </p>
	 */
	public int getCalendarConstant() {
		return _calendarConstant;
	}

	/**
	 * Calls {@link Calendar#set(int, int)} with {@link Calendar#DAY_OF_WEEK} and the day of week
	 * represented by this value.
	 */
	public void applyTo(Calendar calendar) {
		calendar.set(Calendar.DAY_OF_WEEK, getCalendarConstant());
	}

	/**
	 * The {@link Day} represented by the given {@link Calendar}.
	 */
	public static Day fromCalendar(Calendar calendar) {
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		for (Day day : values()) {
			if (day.getCalendarConstant() == dayOfWeek) {
				return day;
			}
		}
		throw new UnreachableAssertion("Unknown day of week value: " + dayOfWeek);
	}

	@Override
	public String toString() {
		return getExternalName();
	}
}
