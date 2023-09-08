/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Calendar;

/**
 * Default implementation of the interface {@link CalendarMarker}.
 * <p>
 * Defines the CSS-Classes to mark a specific field (day, month, year etc.) in a date picker
 * ({@link CalendarControl}). Uses {@link #_lowerBound} and {@link #_upperBound} to define a time
 * range.
 * </p>
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public class DefaultCalendarMarker implements CalendarMarker {

	private final Calendar _lowerBound;

	private final Calendar _upperBound;

	private final boolean _disabled;

	private final String _outOfRange;

	private final String _inRange;

	private final boolean _overlap;

	/**
	 * Creates an object of {@link DefaultCalendarMarker}.
	 * <p>
	 * Adds one day to {@link #_upperBound} so that every date under it (and over
	 * {@link #_lowerBound} will be in range.
	 * </p>
	 * <p>
	 * If the upper and the lower bound are null, every date will be in range.
	 * </p>
	 * 
	 * @param lowerBound
	 *        {@link Calendar} that defines the lower bound of the time range. If null, the time
	 *        range contains all dates before {@link #_upperBound}.
	 * @param upperBound
	 *        {@link Calendar} that defines the upper bound of the time range. If null, the time
	 *        range contains all dates after {@link #_lowerBound}.
	 * @param disabled
	 *        When true fields out of range will be disabled and can't be selected in the date
	 *        picker. Adds a special CSS-Class to the {@link CalendarControl} field.
	 * @param inRange
	 *        CSS-Class for fields in the time range.
	 * @param outOfRange
	 *        CSS-Class for fields out of the time range.
	 * @param overlap
	 *        When true only one {@link Calendar} has to be in the range to be marked as "in range".
	 *        When false both {@link Calendar} have to be in the range between {@link #_lowerBound}
	 *        and {@link #_upperBound} to be "in range".
	 */
	public DefaultCalendarMarker(Calendar lowerBound, Calendar upperBound, boolean disabled, String inRange,
			String outOfRange, boolean overlap) {

		_lowerBound = lowerBound;
		_upperBound = upperBound;
		if (_upperBound != null) {
			_upperBound.add(Calendar.DAY_OF_MONTH, 1);
		}
		_disabled = disabled;
		_inRange = inRange;
		_outOfRange = _disabled ? outOfRange + " " + DISABLED : outOfRange;
		_overlap = overlap;
	}

	/**
	 * Gets the CSS-Classes for a {@link CalendarControl} field
	 * <p>
	 * Returns the CSS-Classes to mark a specific field (day, month, year etc.) in a date picker by
	 * comparing calStart and calEnd with {@link #_lowerBound} and {@link #_upperBound}.
	 * </p>
	 * <p>
	 * If the compared {@link Calendar} and the bound have the same size there are two cases: If the
	 * {@link Calendar} is the start of the range (calStart) of the {@link CalendarControl} field
	 * and the bound to compare with is the {@link #_upperBound} or the {@link Calendar} is the end
	 * of the range (calEnd) of the {@link CalendarControl} field and the bound to compare with is
	 * the {@link #_lowerBound} an equality between the Calendars will result as out of range.
	 * </p>
	 * <p>
	 * If the {@link Calendar} is the start of the range (calStart) of the {@link CalendarControl}
	 * field and the bound to compare with is the {@link #_lowerBound} or the {@link Calendar} is
	 * the end of the range (calEnd) of the {@link CalendarControl} field and the bound to compare
	 * with is the {@link #_upperBound} an equality between the Calendars will result as in range.
	 * </p>
	 * <p>
	 * Explanation: {@link #_upperBound} and calEnd are not defined as the last moment of the end of
	 * the {@link CalendarControl} but as the first moment after. So they are exclusive and not in
	 * range. When comparing calStart with {@link #_upperBound} results in equality, calStart is out
	 * of range because {@link #_upperBound} is out of range itself. The same happens when calEnd is
	 * compared with {@link #_lowerBound} as calEnd is already out of range of the
	 * {@link CalendarControl} field that has to be marked.
	 * </p>
	 * <p>
	 * If calStart and {@link #_lowerBound} are compared and it turns out they are the same, the
	 * field is in range. That's because the value of calStart is inclusive and defines the first
	 * moment in the range between calStart and calEnd. The same applies to {@link #_lowerBound} in
	 * the range between both bounds. If calStart == {@link #_lowerBound} it means the field is in
	 * the range between the bounds and has to be marked accordingly. The same applies to comparing
	 * calEnd and {@link #_upperBound}.
	 * </p>
	 * 
	 * 
	 * @param calStart
	 *        Start of the range in {@link CalendarControl} that should be marked.
	 * @param calEnd
	 *        End of the range in {@link CalendarControl} that should be marked.
	 * 
	 * @return A String of all CSS-Classes separated by space.
	 * 
	 */
	@Override
	public String getCss(Calendar calStart, Calendar calEnd) {
		String css = _inRange;
		if (_overlap) {
			if (greaterOrEqual(calStart, _upperBound) || greaterOrEqual(_lowerBound, calEnd)) {
				css = _outOfRange;
			}

		} else {
			if (greaterOrEqual(calStart, _upperBound) || greaterThan(_lowerBound, calStart)
				|| greaterOrEqual(_lowerBound, calEnd) || greaterThan(calEnd, _upperBound)) {
				css = _outOfRange;
			}
		}
		return css;
	}

	/**
	 * Checks if cal1 is greater than cal2 by comparing day, month and year.
	 * <p>
	 * If {@link #_lowerBound} is null the time range contains all dates before {@link #_upperBound}
	 * and will be marked as in range. If {@link #_upperBound} is null the time range contains all
	 * dates after {@link #_lowerBound} and will be marked as in range. If {@link #_upperBound} and
	 * {@link #_lowerBound} is null every date will be in range.
	 * </p>
	 * 
	 * @param cal1
	 *        The first {@link Calendar} to compare with.
	 * @param cal2
	 *        The second {@link Calendar} to compare with.
	 * @return result if cal1 > cal2
	 */
	private boolean greaterThan(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			return false;
		}
		int cal1Year = cal1.get(Calendar.YEAR);
		int cal2Year = cal2.get(Calendar.YEAR);
		int cal1Month = cal1.get(Calendar.MONTH);
		int cal2Month = cal2.get(Calendar.MONTH);
		int cal1Day = cal1.get(Calendar.DAY_OF_MONTH);
		int cal2Day = cal2.get(Calendar.DAY_OF_MONTH);

		if (cal1Year > cal2Year) {
			return true;
		} else if (cal1Year < cal2Year) {
			return false;
		}

		if (cal1Month > cal2Month) {
			return true;
		} else if ((cal1Month < cal2Month)) {
			return false;
		}

		if (cal1Day > cal2Day) {
			return true;
		} else if ((cal1Day < cal2Day)) {
			return false;
		}
		return false;
	}

	/**
	 * Checks if cal1 is >= cal2
	 * <p>
	 * If {@link #_lowerBound} is null the time range contains all dates before {@link #_upperBound}
	 * and will be marked as in range. If {@link #_upperBound} is null the time range contains all
	 * dates after {@link #_lowerBound} and will be marked as in range. If {@link #_upperBound} and
	 * {@link #_lowerBound} is null every date will be in range.
	 * </p>
	 * 
	 * @param cal1
	 *        The first {@link Calendar} to compare with.
	 * @param cal2
	 *        The second {@link Calendar} to compare with.
	 * @return result of comparison.
	 */
	private boolean greaterOrEqual(Calendar cal1, Calendar cal2) {
		if (cal1 != null && cal2 != null) {
			if (greaterThan(cal1, cal2) || equals(cal1, cal2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if cal1 == cal2 by comparing day, month and year.
	 * 
	 * @param cal1
	 *        The first {@link Calendar} to compare with.
	 * @param cal2
	 *        The second {@link Calendar} to compare with.
	 * @return result of comparison.
	 */
	private boolean equals(Calendar cal1, Calendar cal2) {

		int cal1Year = cal1.get(Calendar.YEAR);
		int cal2Year = cal2.get(Calendar.YEAR);
		int cal1Month = cal1.get(Calendar.MONTH);
		int cal2Month = cal2.get(Calendar.MONTH);
		int cal1Day = cal1.get(Calendar.DAY_OF_MONTH);
		int cal2Day = cal2.get(Calendar.DAY_OF_MONTH);

		if (cal1Year == cal2Year && cal1Month == cal2Month && cal1Day == cal2Day) {
			return true;
		}

		return false;
	}

}
