/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.text.Format;
import java.util.Locale;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.format.NormalizedParsingDecimalFormat;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.util.TLContext;

/**
 * {@link FormatProvider}, that creates a {@link Format} out of a given {@link Format} pattern.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class PatternFormatProvider implements FormatProvider {

	private String _formatPattern;
	private String _patternSource;

	/**
	 * Create a new {@link PatternFormatProvider}.
	 */
	public PatternFormatProvider(String formatPattern) {
		_formatPattern = formatPattern;
		_patternSource = null;
	}

	/**
	 * @param patternSource
	 *        from where this pattern was taken (e.g. xml location).
	 */
	public void setPatternSource(String patternSource) {
		_patternSource = patternSource;
	}

	@Override
	public Format getFormat(ColumnConfiguration column) {
		Locale locale = TLContext.getContext().getCurrentLocale();
		if (!StringServices.isEmpty(_formatPattern)) {
			try {
				return NormalizedParsingDecimalFormat.getNormalizingInstance(_formatPattern, locale);
			} catch (IllegalArgumentException ex) {
				// thrown by DecimalFormat if pattern is invalid
				handleIllegalDecimalFormatPattern();
			}
		}
		return null;
	}

	private void handleIllegalDecimalFormatPattern() {
		StringBuilder error = new StringBuilder();
		error.append("Invalid format pattern for table filter");
		if (!StringServices.isEmpty(_patternSource)) {
			error.append(" in '");
			error.append(_patternSource);
			error.append("'");
		}
		error.append(": ");
		error.append(_formatPattern);
		Logger.warn(error.toString(), PatternFormatProvider.class);
	}
}
