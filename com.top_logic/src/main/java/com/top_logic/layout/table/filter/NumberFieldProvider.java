/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.text.Format;
import java.text.NumberFormat;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link FilterFieldProvider} of {@link ComplexField}s, holding float numbers.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class NumberFieldProvider implements FilterFieldProvider {

	/** Default instance of {@link NumberFieldProvider} */
	public static final FilterFieldProvider INSTANCE = new NumberFieldProvider(createDefaultFormat());

	private Format _fieldFormat;

	/**
	 * Create a new {@link NumberFieldProvider}.
	 */
	public NumberFieldProvider(Format fieldFormat) {
		_fieldFormat = fieldFormat;
	}

	@Override
	public FormField createField(String name, Object value) {
		return FormFactory.newComplexField(name, _fieldFormat, value, false);
	}

	private static Format createDefaultFormat() {
		NumberFormat format = (NumberFormat) HTMLFormatter.getInstance().getNumberFormat().clone();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(7); // float precision is maximum used
		return format;
	}
}
