/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.DateFormat;
import java.text.Format;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * {@link Format} that interprets years as {@link Integer} objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class YearFormat extends FormatConversion {

	private final Locale locale;
	
	/**
	 * Constructs a new {@link YearFormat} for the given {@link Locale}.
	 */
	public YearFormat(DateFormat targetFormat, Locale locale) {
		super(targetFormat);
		this.locale = locale;
	}

	@Override
	protected Object convertFromTargetFormat(Object parseObject) {
		if (parseObject == null) {
			// Parse error.
			return null;
		}
		GregorianCalendar date = new GregorianCalendar(locale);
		date.setTime((Date) parseObject);
		return Integer.valueOf(date.get(Calendar.YEAR));
	}

	@Override
	protected Object convertToTargetFormat(Object obj) {
		return new GregorianCalendar(((Integer) obj).intValue(), Calendar.JANUARY, 1).getTime();
	}

}
