/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} that parses a milliseconds duration in a human readable
 * format, e.g. <code>30min 5s</code>.
 * <p>
 * Supported units: d, h, min, s, ms
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMillisFormat<T> extends AbstractConfigurationValueProvider<T> {

	/**
	 * Creates a {@link AbstractMillisFormat}.
	 */
	public AbstractMillisFormat(Class<?> type) {
		super(type);
	}

	private static final String MILLISECOND = "ms";

	private static final String SECOND = "s";

	private static final String MINUTE = "min";

	private static final String HOUR = "h";

	private static final String DAY = "d";

	private static final String S = "\\s*";

	private static final Pattern PART = Pattern.compile(S + "(?:(\\d+)(?:[\\.\\,](\\d+))?)" + S + "([a-zA-Z]+)?" + S);

	/**
	 * Implementation of {@link #getValueNonEmpty(String, CharSequence)}
	 */
	protected long internalValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		int length = propertyValue.length();

		long result = 0;
		Matcher matcher = PART.matcher(propertyValue);
		while (true) {
			boolean found = matcher.lookingAt();
			if (!found) {
				throw new ConfigurationException(I18NConstants.INVALID_DURATION_FORMAT__VALUE.fill(propertyValue),
					propertyName, propertyValue);
			}
			double amount = Long.parseLong(matcher.group(1));
			String fragment = matcher.group(2);
			if (fragment != null) {
				amount += Double.parseDouble("0." + fragment);
			}
			String unit = matcher.group(3);
			if (unit == null) {
				unit = MILLISECOND;
			}
			switch (unit) {
				case DAY:
					result += Math.round(amount * DateUtil.DAY_MILLIS);
					break;
				case HOUR:
					result += Math.round(amount * DateUtil.HOUR_MILLIS);
					break;
				case MINUTE:
					result += Math.round(amount * DateUtil.MINUTE_MILLIS);
					break;
				case SECOND:
					result += Math.round(amount * DateUtil.SECOND_MILLIS);
					break;
				case MILLISECOND:
					result += Math.round(amount);
					break;
				default:
					throw new ConfigurationException(I18NConstants.NO_SUCH_TIME_UNIT__VALUE.fill(unit), propertyName,
						propertyValue);
			}

			int end = matcher.end();
			if (end == length) {
				break;
			}
			matcher.region(end, length);
		}
		return result;
	}

	/**
	 * Implementation of {@link #getSpecificationNonNull(Object)}.
	 */
	protected String internalSpecificationNonNull(long value) {
		StringBuilder result = new StringBuilder();
		boolean found = false;

		long days = value / DateUtil.DAY_MILLIS;
		if (days != 0) {
			value %= DateUtil.DAY_MILLIS;
			result.append(days);
			result.append(DAY);
			found = true;
		}
		long hours = value / DateUtil.HOUR_MILLIS;
		if (hours != 0) {
			if (found) {
				result.append(' ');
			}
			value %= DateUtil.HOUR_MILLIS;
			result.append(hours);
			result.append(HOUR);
			found = true;
		}
		long minutes = value / DateUtil.MINUTE_MILLIS;
		if (minutes != 0) {
			if (found) {
				result.append(' ');
			}
			value %= DateUtil.MINUTE_MILLIS;
			result.append(minutes);
			result.append(MINUTE);
			found = true;
		}
		long seconds = value / DateUtil.SECOND_MILLIS;
		if (seconds != 0) {
			if (found) {
				result.append(' ');
			}
			value %= DateUtil.SECOND_MILLIS;
			result.append(seconds);
			result.append(SECOND);
			found = true;
		}
		if (value != 0 || !found) {
			if (found) {
				result.append(' ');
			}
			result.append(value);
			result.append(MILLISECOND);
		}
		return result.toString();
	}

}
