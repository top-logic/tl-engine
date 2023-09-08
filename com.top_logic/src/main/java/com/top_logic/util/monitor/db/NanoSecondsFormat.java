/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.DebugHelper;

/**
 * {@link Format} parsing a time value <code>mm:ss,SSS</code> into a {@link Long} representing
 * nanoseconds.
 * 
 * <p>
 * Additionally, an input of <code>XXms</code> or <code>YYs</code> is accepted.
 * </p>
 * 
 * @see NanoSecondsFilterProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NanoSecondsFormat extends Format {

	private static final long MILLI_NANOS = 1000000L;

	private static final long SECOND_NANOS = 1000L * MILLI_NANOS;

	private static final long MINUTE_NANOS = 60L * SECOND_NANOS;

	private static final Pattern PATTERN =
		Pattern.compile("\\s*(?:(\\d+)\\s*ms)|(?:(\\d+)\\s*s)|(?:(?:(\\d+):)?(\\d+)(?:,(\\d{1,3}))?)\\s*");

	/**
	 * Singleton {@link NanoSecondsFormat} instance.
	 */
	public static final NanoSecondsFormat INSTANCE = new NanoSecondsFormat();

	private NanoSecondsFormat() {
		// Singleton constructor.
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (obj != null) {
			long millis = ((Number) obj).longValue() / MILLI_NANOS;
			toAppendTo.append(DebugHelper.toDuration(millis));
		}
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Matcher matcher = PATTERN.matcher(source);
		matcher.region(pos.getIndex(), source.length());
		if (matcher.matches()) {
			pos.setIndex(matcher.end());

			String seconds = matcher.group(4);
			if (seconds != null) {
				String minutes = matcher.group(3);
				String milliString = matcher.group(5);
				
				long result = Long.parseLong(seconds) * SECOND_NANOS;
				if (minutes != null) {
					result += Long.parseLong(minutes) * MINUTE_NANOS;
				}
				if (milliString != null) {
					milliString += "000".substring(0, 3 - milliString.length());
					long millis = Long.parseLong(milliString);
					result += millis * MILLI_NANOS;
				}
				
				return result;
			}

			String ms = matcher.group(1);
			if (ms != null) {
				return Long.parseLong(ms) * MILLI_NANOS;
			}
			
			String s = matcher.group(2);
			if (s != null) {
				return Long.parseLong(s) * SECOND_NANOS;
			}
		}

		pos.setErrorIndex(pos.getIndex());
		return null;
	}
}