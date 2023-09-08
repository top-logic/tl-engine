/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * {@link Format} wrapper that ensures that a parsed {@link Number} is a {@link Long} value.
 * 
 * @see DoubleFormat
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class LongFormat extends NumberFormat {

	/**
	 * Creates a {@link LongFormat} wrapped around the given implementation {@link Format}.
	 */
	public static NumberFormat newInstance(NumberFormat impl) {
		return new LongFormat(impl);
	}

	private final NumberFormat _impl;

	private LongFormat(NumberFormat impl) {
		_impl = impl;
	}

	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		return _impl.format(number, toAppendTo, pos);
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		return _impl.format(number, toAppendTo, pos);
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		int start = parsePosition.getIndex();
		Number result = _impl.parse(source, parsePosition);
		if (result == null) {
			return null;
		}
		if (result instanceof Long) {
			return result;
		}

		// Note: An error is only reported, if the index is reset.
		parsePosition.setIndex(start);

		parsePosition.setErrorIndex(start);
		return Long.valueOf(result.longValue());
	}
}