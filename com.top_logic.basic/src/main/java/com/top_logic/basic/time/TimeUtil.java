/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.time;

import java.text.Format;
import java.time.Duration;
import java.time.Instant;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.util.StopWatch;

/**
 * Utilities for working with "time" that don't belong to {@link DateUtil}, {@link CalendarUtil} or
 * anywhere else.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TimeUtil {

	/**
	 * Prints the given number of milliseconds in the most appropriate units. (Between millisecond
	 * and day)
	 * <p>
	 * Rules:
	 * <ol>
	 * <li>If the number is 0, the output is '0ms'.</li>
	 * <li>If the value for a unit is 0, that unit is not printed.</li>
	 * </ol>
	 * Examples:
	 * <ul>
	 * <li><code>1234d 12h 12min 12s 123ms</code></li>
	 * <li><code>23d</code></li>
	 * <li><code>-2d 6h</code></li>
	 * <li><code>1h 7s</code></li>
	 * <li><code>0ms</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @see DebugHelper#formatTime(long)
	 * @see DebugHelper#toDuration(long)
	 * @see StopWatch#toStringMillis(long)
	 * @see StopWatch#toStringNanos(long)
	 * @see Format type hierarchy
	 * 
	 * @return Never <code>null</code>.
	 */
	public static String formatMillisAsTime(long timeInMillis) {
		return format(Duration.ofMillis(timeInMillis));
	}

	/**
	 * Prints the {@link Duration} in a human readable form.
	 * <p>
	 * The output can contain the following units: d, h, min, s, ms, ns. It does not include year
	 * and month, as their size in days varies. (28-31, 365/366) It does not include microseconds,
	 * as most Java APIs use either milliseconds or nanoseconds, but almost never microseconds.
	 * </p>
	 * <p>
	 * Rules:
	 * <ol>
	 * <li>If the number is 0, the output is '0ns'.</li>
	 * <li>If the value for a unit is 0, that unit is not printed.</li>
	 * </ol>
	 * Examples:
	 * <ul>
	 * <li><code>1234d 12h 12min 12s 123456789ns</code></li>
	 * <li><code>1234d 12h 12min 12s 123ms</code></li>
	 * <li><code>-23h 59min 59s 999999999ns</code></li>
	 * <li><code>23d</code></li>
	 * <li><code>1h 7s</code></li>
	 * <li><code>0ns</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @see DebugHelper#formatTime(long)
	 * @see DebugHelper#toDuration(long)
	 * @see StopWatch#toStringMillis(long)
	 * @see StopWatch#toStringNanos(long)
	 * @see Format type hierarchy
	 * 
	 * @return Never <code>null</code>.
	 */
	public static String format(Duration duration) {
		if (duration.isZero()) {
			return "0ns";
		}
		if (duration.isNegative()) {
			return "-" + format(duration.abs());
		}
		String result = "";
		long days = duration.toDaysPart();
		if (days != 0) {
			result += days + "d ";
		}
		int hours = duration.toHoursPart();
		if (hours != 0) {
			result += hours + "h ";
		}
		int minutes = duration.toMinutesPart();
		if (minutes != 0) {
			result += minutes + "min ";
		}
		int seconds = duration.toSecondsPart();
		if (seconds != 0) {
			result += seconds + "s ";
		}
		int nanos = duration.toNanosPart();
		if (nanos != 0) {
			if ((nanos % 1_000_000 == 0)) {
				/* Don't print in nanosecond precision, if it has only millisecond precision. */
				result += duration.toMillisPart() + "ms";
			} else {
				result += nanos + "ns";
			}
		}
		if (result.charAt(result.length() - 1) == ' ') {
			return result.substring(0, result.length() - 1);
		}
		return result;
	}

	/**
	 * Writes the given timestamp milliseconds in ISO-8601 format.
	 * <p>
	 * Null is formatted as the empty String.
	 * </p>
	 * <p>
	 * See {@link Instant#toString()} for details.
	 * </p>
	 * 
	 * @param timestamp
	 *        A timestamp as given for example by {@link System#currentTimeMillis()}.
	 */
	public static String toStringEpoche(Long timestamp) {
		if (timestamp == null) {
			return "";
		}
		return Instant.ofEpochMilli(timestamp).toString();
	}

}
