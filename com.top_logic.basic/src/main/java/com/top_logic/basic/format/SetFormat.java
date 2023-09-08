/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.top_logic.basic.col.ComparableComparator;

/**
 * {@link Format} parsing a set of items (unordered collection without multiple occurrences of the
 * same item) by searching a string for delimiters and passing substrings between those delimiters
 * to an inner format.
 * 
 * <p>
 * This format is only for simple cases, where no quoting of delimiters is required.
 * </p>
 */
public class SetFormat extends ListFormat {

	/**
	 * Creates a {@link SetFormat}.
	 *
	 * @param innerFormat
	 *        The format to parse/format list elements with.
	 */
	public SetFormat(Format innerFormat, char delimiter) {
		super(innerFormat, delimiter);
	}

	/**
	 * Creates a {@link SetFormat}.
	 *
	 * @param innerFormat
	 *        The format to parse/format list elements with.
	 * @param delimiterPattern
	 *        The pattern matching a list value delimiter.
	 * @param formatDelimiter
	 *        The delimiter to insert between list items during formatting.
	 * @param trim
	 *        Whether white space is trimmed from matches before they are passed to the inner
	 *        format.
	 */
	public SetFormat(Format innerFormat, Pattern delimiterPattern, String formatDelimiter, boolean trim) {
		super(innerFormat, delimiterPattern, formatDelimiter, trim);
	}

	/**
	 * Creates a {@link SetFormat}.
	 *
	 * @param innerFormat
	 *        The format to parse/format list elements with.
	 * @param formatDelimiter
	 *        The delimiter to insert between list items during formatting.
	 * @param trim
	 *        Whether white space is trimmed from matches before they are passed to the inner
	 *        format.
	 */
	public SetFormat(Format innerFormat, String parseDelimiter, String formatDelimiter, boolean trim) {
		super(innerFormat, parseDelimiter, formatDelimiter, trim);
	}

	@Override
	protected Collection<Object> createCollection() {
		return new HashSet<>();
	}

	@Override
	protected Collection<?> prepareFormat(Collection<?> obj) {
		ArrayList<?> result = new ArrayList<>(obj);
		result.sort(ComparableComparator.INSTANCE);
		return result;
	}

}
