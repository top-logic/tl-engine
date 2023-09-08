/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.text.ParseException;

import com.top_logic.basic.config.annotation.Format;

/**
 * A {@link TLFormat} can {@link #parse(String)} and {@link TLFormat} a value.
 * <p>
 * It has to guarantee that <code>parse(format(value)).equals(value)</code><br/>
 * (If the value is <code>null</code>, the guarantee is: <code>parse(format(null)) == null</code>
 * </p>
 * 
 * @see TLFormatAdapter
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Format(TLFormatAdaptingValueProvider.class)
public interface TLFormat<T> {

	/**
	 * Parses the given formatted value.
	 * <p>
	 * <b>Returns <code>null</code> only, if the formattedValue represents <code>null</code>.</b>
	 * </p>
	 * 
	 * @throws ParseException
	 *         iff the given formatted value can not be parsed to a valid value.
	 */
	T parse(String formattedValue) throws ParseException;

	/**
	 * Formats the given value.
	 * <p>
	 * <b>Returns <code>null</code> only, if the value is <code>null</code>.</b>
	 * </p>
	 */
	String format(T value);

}
