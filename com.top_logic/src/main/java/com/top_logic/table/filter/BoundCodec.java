/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.top_logic.basic.format.NumberFormats;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;

/**
 * The two ways a bound of a {@link ComparableColumnFilter} is written.
 *
 * <p>
 * A bound is read and written by a person, and it is stored. Those are different texts and must not
 * be confused: what a person types is written in that person's language - {@code 37,5} for a German
 * user, {@code 01.02.2026} for a date - while what is stored outlives the session that wrote it and
 * is read back by whoever opens the table next, in whatever language. A stored bound is therefore
 * written in a form that means the same everywhere: a number as a number, a point in time as an
 * ISO-8601 text.
 * </p>
 *
 * <p>
 * {@link #format(Object)} and {@link #parse(String)} are the person's half, {@link #toJson(Object)}
 * and {@link #fromJson(Object)} the stored half. All four answer {@code null} for {@code null}, and
 * {@link #parse(String)} answers {@code null} for text that is not a value.
 * </p>
 *
 * @param <V>
 *        The type of the bound.
 */
public interface BoundCodec<V> {

	/**
	 * The bound as the text a person reads, in that person's language.
	 *
	 * @param value
	 *        The bound to write, may be {@code null}.
	 */
	String format(V value);

	/**
	 * The bound a person typed, or {@code null} if the text is not a bound.
	 *
	 * @param text
	 *        The text as typed, may be {@code null} or blank.
	 */
	V parse(String text);

	/**
	 * The bound in its stored form: a value that means the same in every language.
	 *
	 * @param value
	 *        The bound to store, may be {@code null}.
	 */
	Object toJson(V value);

	/**
	 * The bound read back from its {@link #toJson(Object) stored form}, or {@code null} if the
	 * stored value is not a bound.
	 *
	 * @param json
	 *        The stored value, may be {@code null}.
	 */
	V fromJson(Object json);

	/**
	 * A {@link BoundCodec} that writes a bound with {@link Object#toString()} and reads it with the
	 * given parser, in the same way for a person and for storage.
	 *
	 * <p>
	 * Correct only where {@link Object#toString()} is independent of the language and is what the
	 * parser reads - a whole number, an identifier. A value whose text depends on the reader (a
	 * decimal fraction, a point in time) needs a codec that keeps the two halves apart, see
	 * {@link #numbers(Format)} and {@link #dates(List, List)}.
	 * </p>
	 *
	 * @param parser
	 *        Reads a bound from its text, answering {@code null} or throwing for text that is none.
	 */
	static <V> BoundCodec<V> text(Function<String, ? extends V> parser) {
		return new BoundCodec<>() {
			@Override
			public String format(V value) {
				return value == null ? null : value.toString();
			}

			@Override
			public V parse(String text) {
				if (text == null) {
					return null;
				}
				String trimmed = text.trim();
				if (trimmed.isEmpty()) {
					return null;
				}
				try {
					return parser.apply(trimmed);
				} catch (RuntimeException ex) {
					return null;
				}
			}

			@Override
			public Object toJson(V value) {
				return format(value);
			}

			@Override
			public V fromJson(Object json) {
				return json == null ? null : parse(json.toString());
			}
		};
	}

	/**
	 * A {@link BoundCodec} for numeric bounds, written and read in the given format.
	 *
	 * <p>
	 * A stored bound is the number itself, so that it survives a change of the reader's language:
	 * the text {@code 37,5} and the text {@code 37.5} both stand for the number 37.5, and that is
	 * what is written. A bound stored as text by an earlier version is still read, in the
	 * language-independent form {@link Double#valueOf(String)} accepts.
	 * </p>
	 *
	 * <p>
	 * Bounds are {@link NumberFormats#normalize(Format, Number) normalized} to the value type the
	 * format works in, so that a bound written and read again equals the one that was written.
	 * </p>
	 *
	 * @param numberFormat
	 *        The format the numbers of the column are written in. Need not write digits: a duration
	 *        is a number of milliseconds written as {@code 1h 30min}, and its bounds are entered in
	 *        that text.
	 */
	static BoundCodec<Number> numbers(Format numberFormat) {
		return new BoundCodec<>() {
			@Override
			public String format(Number value) {
				return value == null ? null : numberFormat.format(value);
			}

			@Override
			public Number parse(String text) {
				return NumberFormats.normalize(numberFormat, NumberFormats.parse(numberFormat, text));
			}

			@Override
			public Object toJson(Number value) {
				return value;
			}

			@Override
			public Number fromJson(Object json) {
				if (json instanceof Number number) {
					return NumberFormats.normalize(numberFormat, number);
				}
				if (json == null) {
					return null;
				}
				return NumberFormats.normalize(numberFormat, decimal(json.toString().trim()));
			}

			private Number decimal(String text) {
				if (text.isEmpty()) {
					return null;
				}
				try {
					return Double.valueOf(text);
				} catch (NumberFormatException ex) {
					return null;
				}
			}
		};
	}

	/**
	 * A {@link BoundCodec} for bounds that are a point in time.
	 *
	 * <p>
	 * A stored bound is an ISO-8601 text in the reader's time zone, so that a bound stored in one
	 * language is read back as the same moment in another.
	 * </p>
	 *
	 * @param userFormats
	 *        The formats a person may type a bound in; the first one also writes it. See
	 *        {@link DateFormat}.
	 * @param isoPatterns
	 *        The ISO-8601 patterns a stored bound may be written in; the first one writes it.
	 */
	static BoundCodec<Date> dates(List<DateFormat> userFormats, List<String> isoPatterns) {
		return new BoundCodec<>() {
			@Override
			public String format(Date value) {
				return value == null ? null : userFormats.get(0).format(value);
			}

			@Override
			public Date parse(String text) {
				return read(text, userFormats);
			}

			@Override
			public Object toJson(Date value) {
				return value == null ? null : isoFormat(isoPatterns.get(0)).format(value);
			}

			@Override
			public Date fromJson(Object json) {
				if (json == null) {
					return null;
				}
				List<DateFormat> formats = isoPatterns.stream().map(BoundCodec::isoFormat).toList();
				return read(json.toString(), formats);
			}

			private Date read(String text, List<DateFormat> formats) {
				if (text == null) {
					return null;
				}
				String trimmed = text.trim();
				if (trimmed.isEmpty()) {
					return null;
				}
				for (DateFormat format : formats) {
					try {
						return format.parse(trimmed);
					} catch (ParseException ex) {
						// Try the next accepted format; text that fits none is not a bound.
					}
				}
				return null;
			}
		};
	}

	/**
	 * A format for the ISO-8601 form a stored bound is written in.
	 *
	 * <p>
	 * In the reader's time zone, matching the text a person sees: a bound entered as 14:30 is stored
	 * as {@code 14:30} and comes back as 14:30, rather than as the same moment in another zone.
	 * </p>
	 */
	private static DateFormat isoFormat(String pattern) {
		SimpleDateFormat result = CalendarUtil.newSimpleDateFormat(pattern, Locale.ROOT);
		result.setTimeZone(ThreadContext.getTimeZone());
		result.setLenient(false);
		return result;
	}

}
