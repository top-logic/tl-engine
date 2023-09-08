/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel.format;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.AbstractFormattedConfigurationValueProvider;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Flexible date formatter which can handle absolute and relative dates.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FlexibleDateFormat extends AbstractFormattedConfigurationValueProvider<Date> {

	/** Singleton instance to this class. */
	public static final FlexibleDateFormat INSTANCE = new FlexibleDateFormat();

	/**
	 * Creates a {@link FlexibleDateFormat}.
	 */
	public FlexibleDateFormat() {
		super(Date.class);
	}

	@Override
	public Date parse(String aPropertyValue) throws ParseException {
		return this.parseDate(aPropertyValue);
	}

	@Override
	public String format(Date aConfigValue) {
		return FlexibleDateFormat.getDateFormat().format(aConfigValue);
	}

	/**
	 * Parse the given string into a date.
	 * 
	 * @param aParameter
	 *        The string to be parsed, must not be <code>null</code>.
	 * @return The returned date, never <code>null</code>.
	 * @throws ParseException
	 *         When parsing the string fails for a reason.
	 */
	protected Date parseDate(String aParameter) throws ParseException {
		if (aParameter.indexOf('=') < 0) {
			return getDateFormat().parse(aParameter);
		}
		Map<String, String> valueMap = MapUtil.parse(aParameter, "=", ";");
		return parseRelativeDate(valueMap);
	}

	private Date parseRelativeDate(Map<String, String> valueMap) throws ParseException {
		Calendar cal = CalendarUtil.createCalendarInUserTimeZone();

		for (String theKey : valueMap.keySet()) {
			String theValue = valueMap.get(theKey);

			if ("Uhrzeit".equals(theKey) || "Time".equals(theKey)) {
				Pattern timePattern = Pattern.compile("(\\d?\\d):(\\d?\\d)");
				Matcher matcher = timePattern.matcher(theValue.trim());
				if (!matcher.matches()) {
					throw new IllegalArgumentException(theValue + " does not match pattern HH:mm");
				}
				DateUtil.adjustTime(cal, Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), 0, 0);
			}
			else {
				char theType = parseUnit(theKey);

				cal = DateUtil.add(cal, theValue + theType);
			}
		}
		return cal.getTime();
	}

	private char parseUnit(String unit) throws ParseException {
		if ("Tage".equals(unit) || "Days".equals(unit)) {
			return 'd';
		}
		if ("Monate".equals(unit) || "Month".equals(unit)) {
			return 'M';
		}
		if ("Jahre".equals(unit) || "Years".equals(unit)) {
			return 'y';
		}
		if ("Stunden".equals(unit) || "Hours".equals(unit)) {
			return 'h';
		}
		if ("Minuten".equals(unit) || "Minutes".equals(unit)) {
			return 'm';
		}
		if ("Sekunden".equals(unit) || "Seconds".equals(unit)) {
			return 's';
		}
		throw new ParseException("Unknown unit for date or time: '" + unit + "'.", 0);
	}

	private static DateFormat getDateFormat() {
		return CalendarUtil.newSimpleDateFormat("dd.MM.yyyy");
	}

}
