/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * Format for {@link ExtID}'s.
 * 
 * <h4><a name="synchronization">Synchronization</a></h4>
 *
 * <p>
 * This format is thread safe.
 * </p>
 * 
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExtIDFormat extends Format {

	private static final int TO_STRING_RADIX = Character.MAX_RADIX;

	private static final char SEPARATOR_CHAR = '.';

	/** Singleton {@link ExtIDFormat} instance. */
	public static final ExtIDFormat INSTANCE = new ExtIDFormat();

	private ExtIDFormat() {
		// singleton instance
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof ExtID)) {
			throw new IllegalArgumentException("Only " + ExtID.class.getName() + " can be formatted. Given: " + obj);
		}
		ExtID id = (ExtID) obj;
		return toAppendTo.append(toString(id.systemId())).append(SEPARATOR_CHAR).append(toString(id.objectId()));
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		int from = pos.getIndex();
		int to = source.length();
		int separatorIndex = source.indexOf(SEPARATOR_CHAR, from);
		if (separatorIndex == -1) {
			pos.setErrorIndex(findErrorIndex(source, from, to));
			return null;
		}
		long systemId;
		try {
			systemId = parseLong(source.substring(from, separatorIndex));
		} catch (NumberFormatException ex) {
			pos.setErrorIndex(findErrorIndex(source, from, separatorIndex));
			return null;
		}
		long objectId;
		try {
			objectId = parseLong(source.substring(separatorIndex + 1, to));
		} catch (NumberFormatException ex) {
			pos.setErrorIndex(findErrorIndex(source, separatorIndex + 1, to));
			return null;
		}
		pos.setIndex(to);
		return new ExtID(systemId, objectId);
	}

	private int findErrorIndex(String source, int from, int to) {
		int tmp = from;
		for (; tmp < to; tmp++) {
			try {
				parseLong(source.substring(from, tmp + 1));
			} catch (NumberFormatException ex) {
				return tmp;
			}
		}
		return tmp;
	}

	private static String toString(long l) {
		return Long.toString(l, TO_STRING_RADIX);
	}

	private static long parseLong(String s) {
		return Long.parseLong(s, TO_STRING_RADIX);
	}

	/**
	 * Parses the given formatted {@link ExtID}.
	 * 
	 * <p>
	 * Actually the same as {@link #parseObject(String)} but throwing an
	 * {@link IllegalArgumentException} instead of {@link ParseException} in case of error.
	 * </p>
	 * 
	 * @param extID
	 *        A {@link #format(Object) formatted} {@link ExtID}.
	 * 
	 * @see #parseObject(String)
	 */
	public ExtID parseExtID(String extID) {
		ParsePosition pos = new ParsePosition(0);
		Object result = parseObject(extID, pos);
		if (pos.getIndex() == 0) {
			throw new IllegalArgumentException("Format.parseObject(String) failed at position " + pos.getErrorIndex());
		}
		return (ExtID) result;
	}

}

