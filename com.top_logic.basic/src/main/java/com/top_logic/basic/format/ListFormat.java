/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link Format} parsing a list of items by searching a string for delimiters and passing
 * substrings between those delimiters to an inner format.
 * 
 * <p>
 * This format is only for simple cases, where no quoting of delimiters is required.
 * </p>
 */
public class ListFormat extends Format {

	private final Format _innerFormat;

	private final Pattern _delimiterPattern;

	private final String _formatDelimiter;

	private boolean _trim;

	/**
	 * Creates a {@link ConfigurationValueProvider} for a {@link List} valued type.
	 * 
	 * @param delimiter
	 *        the delimiter character. Formatting uses the given delimiter character plus a space.
	 */
	public ListFormat(Format innerFormat, char delimiter) {
		this(innerFormat, Character.toString(delimiter), delimiter + " ", true);
	}

	/**
	 * Creates a {@link ConfigurationValueProvider} for a {@link List} valued type.
	 * 
	 * @param innerFormat
	 *        The format to parse/format list elements with.
	 * @param parseDelimiter
	 *        The list value delimiter string.
	 * @param formatDelimiter
	 *        The delimiter to insert between list items during formatting.
	 * @param trim
	 *        Whether white space is trimmed from matches before they are passed to the inner
	 *        format.
	 */
	public ListFormat(Format innerFormat, String parseDelimiter, String formatDelimiter, boolean trim) {
		this(innerFormat, Pattern.compile(Pattern.quote(parseDelimiter)), formatDelimiter, trim);
	}

	/**
	 * Creates a {@link ConfigurationValueProvider} for a {@link List} valued type.
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
	public ListFormat(Format innerFormat, Pattern delimiterPattern, String formatDelimiter, boolean trim) {
		_innerFormat = innerFormat;
		_delimiterPattern = delimiterPattern;
		_formatDelimiter = formatDelimiter;
		_trim = trim;
	}

	@Override
	public Object parseObject(String source) throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		Object result = parseObject(source, pos);
		if (pos.getErrorIndex() >= 0) {
			throw new ParseException("Invalid format at position " + pos.getErrorIndex() + ".", pos.getErrorIndex());
		}
		return result;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Collection<Object> values = createCollection();

		int start = pos.getIndex();
		int length = source.length();

		Matcher matcher = _delimiterPattern.matcher(source);
		while (start < length) {
			int stop;
			int nextStart;
			if (matcher.find(start)) {
				stop = matcher.start();
				nextStart = matcher.end();
			} else {
				stop = length;
				nextStart = length;
			}

			if (stop > start) {
				start = skipWhiteSpace(source, stop, start);

				int end = skipWhiteSpaceBackwards(source, start, stop);

				if (end > start) {
					pos.setIndex(0);
					values.add(_innerFormat.parseObject(source.substring(start, end), pos));
					if (pos.getErrorIndex() >= 0) {
						pos.setErrorIndex(start + pos.getErrorIndex());
						return null;
					}

					int next = skipWhiteSpace(source, stop, start + pos.getIndex());

					if (next < stop) {
						pos.setErrorIndex(next);
						return null;
					}
				}
			}

			start = nextStart;
		}
		pos.setIndex(start);

		return values;
	}

	/**
	 * Creates the collection to be filled during parse.
	 */
	protected Collection<Object> createCollection() {
		return new ArrayList<>();
	}

	private int skipWhiteSpaceBackwards(String source, int limit, int pos) {
		if (!_trim) {
			return pos;
		}

		int end = pos;
		while (end > limit && Character.isWhitespace(source.charAt(end - 1))) {
			end--;
		}
		return end;
	}

	private int skipWhiteSpace(String source, int limit, int pos) {
		if (!_trim) {
			return pos;
		}

		while (pos < limit && Character.isWhitespace(source.charAt(pos))) {
			pos++;
		}
		return pos;
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		boolean first = true;
		for (Object value : asCollection(obj)) {
			if (first) {
				first = false;
			} else {
				toAppendTo.append(_formatDelimiter);
			}
			_innerFormat.format(value, toAppendTo, pos);
		}
		return toAppendTo;
	}

	private Collection<?> asCollection(Object obj) {
		if (obj instanceof Collection<?>) {
			return prepareFormat((Collection<?>) obj);
		} else if (obj != null) {
			return Collections.singletonList(obj);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Potentially sorts the given collection value before formatting.
	 */
	protected Collection<?> prepareFormat(Collection<?> obj) {
		return obj;
	}

}
