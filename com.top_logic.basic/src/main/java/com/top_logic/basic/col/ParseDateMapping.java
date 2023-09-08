/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * {@link Mapping} of {@link String} to {@link Date} through a {@link DateFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseDateMapping implements Mapping<String, Date> {

	private final DateFormat format;

	/**
	 * Creates a {@link ParseDateMapping}.
	 *
	 * @param format The parser to use.
	 */
	public ParseDateMapping(DateFormat format) {
		this.format = format;
	}
	
	@Override
	public Date map(String input) {
	    try {
	        return format.parse(input);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Input does not match date format: '" + input + "'");
		}
	}

}
