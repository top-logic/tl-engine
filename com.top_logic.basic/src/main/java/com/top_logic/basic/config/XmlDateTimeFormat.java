/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thread-safe {@link Format} implementation that parsed XML dateTime strings.
 * 
 * @see "http://www.w3.org/TR/xmlschema-2/#dateTime"
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XmlDateTimeFormat extends Format {

	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	
	private static final int YEAR_GROUP = 1;
	private static final int MONTH_GROUP = 2;
	private static final int DAY_GROUP = 3;
	private static final int HOUR_GROUP = 4;
	private static final int MINUTE_GROUP = 5;
	private static final int SECOND_GROUP = 6;
	private static final int FRACTION_GROUP = 7;
	private static final int SIGN_GROUP = 8;
	private static final int ZONE_HOUR_GROUP = 9;
	private static final int ZONE_MINUTE_GROUP = 10;
	private static final int GMT_GROUP = 11;
	private static final Pattern PATTERN;
	static {
		String yearPat = "0*" + "(" + "(?:[1-9][0-9]*)|0" + ")";
		String digit2Pat = "([0-9][0-9])";
		String fractionPat = "(" + "[0-9]*" + ")";
		String signPat = "(" + "[-+]" + ")";
		String utcPat = "(Z)";
		
		String datePat = yearPat + "-" + digit2Pat + "-" + digit2Pat;
		String timePat = digit2Pat + ":" + digit2Pat + ":" + digit2Pat + "(?:" + "\\." + fractionPat + ")?";
		String zonePat = "(?:" + signPat + digit2Pat + ":" + digit2Pat + ")" + "|" + utcPat;

		String dateTimePat = "^" + "\\s*" + datePat + "(?:" + "T" + timePat + "(?:" + zonePat + ")?" + ")?" + "\\s*" + "$";
		
		PATTERN = Pattern.compile(dateTimePat);
	}

	/**
	 * The singleton instance of {@link XmlDateTimeFormat}.
	 */
	public static final XmlDateTimeFormat INSTANCE = new XmlDateTimeFormat();

	private XmlDateTimeFormat() {
		// Singleton constructor.
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		appendDate(buffer, (Date) obj);
		return buffer;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Matcher matcher = PATTERN.matcher(source);
		int index = pos.getIndex();
		matcher.region(index, source.length());
		if (! matcher.lookingAt()) {
			pos.setErrorIndex(index);
			return null;
		}
		
		String yearString = matcher.group(YEAR_GROUP);
		String monthString = matcher.group(MONTH_GROUP);
		String dayString = matcher.group(DAY_GROUP);
		
		String hourString = matcher.group(HOUR_GROUP);
		String minuteString = matcher.group(MINUTE_GROUP);
		String secondString = matcher.group(SECOND_GROUP);
		String fractionString = matcher.group(FRACTION_GROUP);

		String signString = matcher.group(SIGN_GROUP);
		String zoneHourString = matcher.group(ZONE_HOUR_GROUP);
		String zoneMinuteString = matcher.group(ZONE_MINUTE_GROUP);
		String gmtString = matcher.group(GMT_GROUP);
		
		int year = Integer.parseInt(yearString);
		int month = Integer.parseInt(monthString) - YEAR_GROUP;
		int day = Integer.parseInt(dayString);
		
		GregorianCalendar cal = new GregorianCalendar(year, month, day);
		cal.setTimeZone(GMT);
		
		if (hourString != null) {
			int hour = Integer.parseInt(hourString);
			if (hour > 23) {
				pos.setErrorIndex(matcher.start(HOUR_GROUP));
				return null;
			}
			int minute = Integer.parseInt(minuteString);
			if (minute > 59) {
				pos.setErrorIndex(matcher.start(MINUTE_GROUP));
				return null;
			}
			int second = Integer.parseInt(secondString);
			if (second > 59) {
				pos.setErrorIndex(matcher.start(SECOND_GROUP));
				return null;
			}
			
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, second);
			
			if (fractionString != null) {
				if (fractionString.length() != 3) {
					fractionString = (fractionString + "000").substring(0, 3);
				}
				int fraction = Integer.parseInt(fractionString);
				
				cal.set(Calendar.MILLISECOND, fraction);
			} else {
				cal.set(Calendar.MILLISECOND, 0);
			}
			
			if (signString != null) {
				int zoneHour = Integer.parseInt(zoneHourString);
				if (zoneHour > 14) {
					pos.setErrorIndex(matcher.start(ZONE_HOUR_GROUP));
					return null;
				}
				
				int zoneMinute = Integer.parseInt(zoneMinuteString);
				if (zoneMinute > 59 || (zoneHour == 14 && zoneMinute != 0)) {
					pos.setErrorIndex(matcher.start(ZONE_MINUTE_GROUP));
					return null;
				}
				
				if (signString.charAt(0) == '+') {
					cal.add(Calendar.HOUR_OF_DAY, -zoneHour);
					cal.add(Calendar.MINUTE, -zoneMinute);
				} else {
					assert signString.charAt(0) == '-' : "Pattern checks that value is either + or -.";
					cal.add(Calendar.HOUR_OF_DAY, zoneHour);
					cal.add(Calendar.MINUTE, zoneMinute);
				}
			}
		}
		
		pos.setIndex(matcher.end());
		return cal.getTime();
	}

	/**
	 * Formats the time given as {@link System#currentTimeMillis()} using the
	 * {@link XmlDateTimeFormat}.
	 * 
	 * @param time
	 *        The time as {@link System#currentTimeMillis()}.
	 * @return The formatted date.
	 */
	public static String formatTimeStamp(long time) {
		StringBuffer buffer = new StringBuffer();
		appendTimeStamp(buffer, time);
		return buffer.toString();
	}

	/**
	 * Appends the given {@link Date} encoded using the
	 * {@link XmlDateTimeFormat} to the given buffer.
	 * 
	 * @param buffer
	 *        The buffer to append the encoded time to.
	 * @param date
	 *        The date to encode.
	 */
	public static void appendDate(StringBuffer buffer, Date date) {
		appendTimeStamp(buffer, date.getTime());
	}

	/**
	 * Appends the time given as {@link System#currentTimeMillis()} using the
	 * {@link XmlDateTimeFormat} to the given buffer.
	 * 
	 * @param buffer
	 *        The buffer to append the encoded time to.
	 * @param time
	 *        The time as {@link System#currentTimeMillis()}.
	 */
	public static void appendTimeStamp(StringBuffer buffer, long time) {
		GregorianCalendar cal = new GregorianCalendar(GMT);
		cal.setTimeInMillis(time);
		
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		
		int millis = cal.get(Calendar.MILLISECOND);
		
		append4Digit(buffer, year);
		buffer.append('-');
		append2Digit(buffer, month);
		buffer.append('-');
		append2Digit(buffer, day);
		
		buffer.append('T');

		append2Digit(buffer, hour);
		buffer.append(':');
		append2Digit(buffer, minute);
		buffer.append(':');
		append2Digit(buffer, second);
		buffer.append('.');
		append3Digit(buffer, millis);
		
		buffer.append('Z');
	}

	private static void append4Digit(StringBuffer buffer, int value) {
		if (value < 1000) {
			buffer.append(0);
			if (value < 100) {
				buffer.append(0);
				if (value < 10) {
					buffer.append(0);
				}
			}
		}
		buffer.append(value);
	}

	private static void append3Digit(StringBuffer buffer, int value) {
		if (value < 100) {
			buffer.append(0);
			if (value < 10) {
				buffer.append(0);
			}
		}
		buffer.append(value);
	}

	private static void append2Digit(StringBuffer buffer, int value) {
		if (value < 10) {
			buffer.append(0);
		}
		buffer.append(value);
	}
	
}
