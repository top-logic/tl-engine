/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.text.DateFormat;
import java.text.Format;
import java.util.TimeZone;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link FilterFieldProvider} of date fields.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DateFieldProvider implements FilterFieldProvider {

	/** Default instance of {@link DateFieldProvider} */
	public static final FilterFieldProvider INSTANCE = new DateFieldProvider(null);

	private Format _fieldFormat;

	/**
	 * Create a new {@link DateFieldProvider}.
	 */
	public DateFieldProvider(Format fieldFormat) {
		_fieldFormat = fieldFormat;
	}

	@Override
	public FormField createField(String name, Object value) {
		ComplexField dateField = FormFactory.newDateField(name, value, false);
		if (_fieldFormat != null) {
			dateField.setFormatAndParser(_fieldFormat);
		}
		setShortcutParser(dateField);
		return dateField;
	}

	/**
	 * Sets a parser that allows to set dates in short form.
	 * 
	 * <p>
	 * If the {@link ComplexField#getFormat()} has a different {@link DateFormat#getTimeZone()} that
	 * the short cut format. Nothing happens.
	 * </p>
	 */
	public static void setShortcutParser(ComplexField field) {
		TimeZone formatTimeZone = ((DateFormat) field.getFormat()).getTimeZone();
		DateFormat shortDateFormat = HTMLFormatter.getInstance().getShortDateFormat();
		if (!shortDateFormat.getTimeZone().hasSameRules(formatTimeZone)) {
			// Do not mix formats with different time zones.
			shortDateFormat = (DateFormat) shortDateFormat.clone();
			shortDateFormat.setTimeZone(formatTimeZone);
		}

		// Allow to enter dates in short form and e.g. automatically
		// expand an abbreviated year in a reasonable way.
		field.setParser(shortDateFormat);
	}
}
