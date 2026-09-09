/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * Reading a number a user typed, in the format the same number is displayed in.
 *
 * <p>
 * A number reaches the user as text formatted in the user's locale, and comes back as the text the
 * user typed. Both directions go through one {@link NumberFormat}, so that a value shown as
 * {@code 12,5} is also entered as {@code 12,5} - in an input field, and as the bound of a table
 * filter.
 * </p>
 */
public class NumberFormats {

	/**
	 * The number the given text denotes in the given format, or {@code null} if the text is not a
	 * number in that format.
	 *
	 * <p>
	 * The whole text must be consumed: trailing characters the format stops at mean the text is not
	 * a number, rather than a number followed by something to ignore. Surrounding whitespace is not
	 * part of the number and is dropped.
	 * </p>
	 *
	 * <p>
	 * The result is the number the format produces, which is where its value type comes from: a
	 * format declared to deliver a {@link Double} does so for a whole number as well, while a plain
	 * decimal format answers a {@link Long} for a text without a fraction.
	 * </p>
	 *
	 * @param format
	 *        The format the number is written in.
	 * @param text
	 *        The text to read.
	 */
	public static Number parse(NumberFormat format, String text) {
		if (text == null) {
			return null;
		}
		String trimmed = text.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		ParsePosition position = new ParsePosition(0);
		Number result = format.parse(trimmed, position);
		if (result == null || position.getIndex() < trimmed.length()) {
			return null;
		}
		return result;
	}

	/**
	 * Whether values in the given format carry a fraction.
	 *
	 * @param format
	 *        The format the number is written in.
	 */
	public static boolean isFractional(NumberFormat format) {
		return format.getMaximumFractionDigits() > 0;
	}

	/**
	 * The given number in the value type the given format works in: a {@link Double} for a format
	 * that {@link #isFractional(NumberFormat) carries a fraction}, a {@link Long} otherwise.
	 *
	 * <p>
	 * For a number that has passed through a representation which does not keep its type - a
	 * persisted filter bound read back from JSON, say - so that a value written and read again is
	 * the value that was written.
	 * </p>
	 *
	 * @param format
	 *        The format the number is written in.
	 * @param value
	 *        The number to normalize, may be {@code null}.
	 */
	public static Number normalize(NumberFormat format, Number value) {
		if (value == null) {
			return null;
		}
		if (isFractional(format)) {
			return Double.valueOf(value.doubleValue());
		}
		return Long.valueOf(value.longValue());
	}

}
