/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import com.top_logic.basic.UnreachableAssertion;

/**
 * {@link Format} wrapper that ensures that a parsed application value is in its normal form
 * regarding to the wrapped format.
 * 
 * <p>
 * The normal form of a value is the value that would be parsed by a given format from a
 * serialization of the value by the same format.
 * </p>
 * 
 * <p>
 * The value <code>3.14D</code> is the normal form of e.g. the value <code>3.141D</code> for the
 * decimal format <code>0.00</code>.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NormalizingFormat extends NumberFormat {

	/**
	 * Creates a {@link NormalizingFormat} wrapped around the given implementation.
	 * 
	 * @param impl
	 *        The underlying implementation {@link Format}.
	 */
	public static NormalizingFormat newInstance(NumberFormat impl) {
		return new NormalizingFormat(impl);
	}

	private final NumberFormat _impl;

	/**
	 * Creates a {@link NormalizingFormat}.
	 * 
	 * @param impl
	 *        The underlying implementation {@link Format}.
	 */
	private NormalizingFormat(NumberFormat impl) {
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
	public Number parse(String source, ParsePosition pos) {
		Number result = _impl.parse(source, pos);
		if (pos.getErrorIndex() >= 0) {
			return result;
		}

		String normalizedSource = _impl.format(result);
		try {
			return _impl.parse(normalizedSource);
		} catch (ParseException ex) {
			// This should never happen, since the parsing format itself produced the input.
			throw new UnreachableAssertion(
				"Format could not parse its own serialization '" + normalizedSource + "' of '" + source + "'.", ex);
		}
	}

}
