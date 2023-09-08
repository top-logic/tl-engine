/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.regex.Pattern;

import com.top_logic.layout.form.CheckException;
import com.top_logic.util.Resources;

/**
 * Checks if the value matches the given regular expression.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class RegularExpressionStringConstraint extends AbstractStringConstraint {

	private Pattern _allowedStringPattern;

	/**
	 * Creates a {@link RegularExpressionStringConstraint} for the given expression.
	 */
	public RegularExpressionStringConstraint(String allowedStringRegularExpression) {
		_allowedStringPattern = Pattern.compile(allowedStringRegularExpression);
	}

	/**
	 * Creates a {@link RegularExpressionStringConstraint} for the given {@link Pattern}.
	 */
	public RegularExpressionStringConstraint(Pattern allowedStringPattern) {
		_allowedStringPattern = allowedStringPattern;
	}

	@Override
	protected boolean checkString(String value) throws CheckException {
		if (!_allowedStringPattern.matcher(value).find()) {
			String filenameNotFree = Resources.getInstance().getMessage(I18NConstants.CONTAINS_INVALID_CHARS, "");

			throw new CheckException(filenameNotFree);
		}

		return true;
	}

}
