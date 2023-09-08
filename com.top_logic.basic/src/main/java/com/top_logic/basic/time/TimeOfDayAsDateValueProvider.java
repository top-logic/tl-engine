/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.time;

import java.util.Date;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Parses and formats a time in the format h:m.
 * <p>
 * The date (i.e. the day) of the returned {@link Date} is unspecified. Only the time is allowed to
 * be read from it. <br/>
 * Is thread-safe, as it has no mutable state.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TimeOfDayAsDateValueProvider extends AbstractConfigurationValueProvider<Date> {

	/** The singleton instance of the {@link TimeOfDayAsDateValueProvider}. */
	public static final TimeOfDayAsDateValueProvider INSTANCE = new TimeOfDayAsDateValueProvider();

	private TimeOfDayAsDateValueProvider() {
		super(Date.class);
	}

	@Override
	protected Date getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String value = propertyValue.toString();
		int separatorIndex = value.indexOf(':');
		if (separatorIndex < 0) {
			throw new ConfigurationException(I18NConstants.ERROR_INVALID_TIME_FORMAT__VALUE.fill(value), propertyName,
				propertyValue);
		}
		int hour = parseHour(propertyName, value, separatorIndex);
		int minute = parseMinute(propertyName, value, separatorIndex);
		return createDate(hour, minute);
	}

	private static int parseHour(String propertyName, String value, int separatorIndex) throws ConfigurationException {
		int hour = Integer.parseInt(value.substring(0, separatorIndex));
		if ((hour < 0) || (hour > 23)) {
			throw new ConfigurationException(I18NConstants.ERROR_INVALID_HOUR__VALUE.fill(hour), propertyName, value);
		}
		return hour;
	}

	private static int parseMinute(String propertyName, String value, int separatorIndex)
			throws ConfigurationException {
		int minute = Integer.parseInt(value.substring(separatorIndex + 1, value.length()));
		if ((minute < 0) || (minute > 59)) {
			throw new ConfigurationException(I18NConstants.ERROR_INVALID_MINUTE__VALUE.fill(minute), propertyName,
				value);
		}
		return minute;
	}

	private static Date createDate(int hour, int minute) {
		Date result = new Date(0);
		result.setHours(hour);
		result.setMinutes(minute);
		return result;
	}

	@Override
	protected String getSpecificationNonNull(Date date) {
		String minutes = Integer.toString(date.getMinutes());
		return date.getHours() + ":" + (minutes.length() < 2 ? "0" : "") + minutes;
	}

}
