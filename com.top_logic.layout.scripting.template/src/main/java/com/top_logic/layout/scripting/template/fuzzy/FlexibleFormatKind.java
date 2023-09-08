/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.fuzzy;

import java.text.ParseException;
import java.util.Date;

import com.top_logic.basic.io.TLFormat;
import com.top_logic.layout.scripting.template.excel.format.FlexibleBooleanFormat;
import com.top_logic.layout.scripting.template.excel.format.FlexibleDateFormat;

/**
 * Enumeration of possible flexible {@link TLFormat}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum FlexibleFormatKind {

	/** Expects that the result of the parsing is a {@link Boolean} */
	BOOLEAN(Boolean.class, FlexibleBooleanFormat.INSTANCE),

	/** Expects that the result of the parsing is a {@link Date} */
	DATE(Date.class, FlexibleDateFormat.INSTANCE);

	private final Class<?> _resultClass;

	private final TLFormat<?> _format;

	private FlexibleFormatKind(Class<?> resultClass, TLFormat<?> format) {
		_resultClass = resultClass;
		_format = format;
	}

	/** The type of the parse result. */
	public Class<?> getResultClass() {
		return _resultClass;
	}

	/**
	 * Parses a value.
	 * 
	 * @see TLFormat#parse(String)
	 */
	public Object parseValue(String value) throws ParseException {
		return _format.parse(value);
	}

}
