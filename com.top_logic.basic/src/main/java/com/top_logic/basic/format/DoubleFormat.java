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
 * {@link Format} wrapper that returns {@link Number} results consistently as {@link Double}
 * instances.
 * 
 * @see LongFormat
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DoubleFormat extends NumberFormat {

	/**
	 * Creates a {@link DoubleFormat} wrapped around the given implementation {@link Format}.
	 */
	public static NumberFormat newInstance(NumberFormat impl) {
		return new DoubleFormat(impl);
	}

	private final NumberFormat _impl;

	private DoubleFormat(NumberFormat impl) {
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
		Number result = _impl.parse(source, parsePosition);
		if (result == null) {
			return null;
		}
		if (result instanceof Double) {
			return result;
		}
		return Double.valueOf(result.doubleValue());
	}
}