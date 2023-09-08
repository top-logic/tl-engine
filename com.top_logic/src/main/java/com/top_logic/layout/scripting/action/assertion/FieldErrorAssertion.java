/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import java.util.regex.Pattern;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.layout.form.FormField;

/**
 * {@link FieldAssertion} to assert for the error of a {@link FormField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FieldErrorAssertion extends FieldAssertion {

	/** Name of the configuration option {@link #getError()}. */
	String ERROR_NAME = "error";

	/** Name of the configuration option {@link #getErrorPattern()}. */
	String ERROR_PATTERN_NAME = "error-pattern";

	/**
	 * The expected error value of the {@link #getModelName() target field}.
	 * 
	 * @return The expected error string or empty when no assertion for the concrete error is
	 *         demanded.
	 * 
	 * @see #getErrorPattern()
	 * @see FormField#getError()
	 */
	@Name(ERROR_NAME)
	String getError();

	/**
	 * Setter for {@link #getError()}.
	 */
	void setError(String error);

	/**
	 * The {@link Pattern} which the error value of the {@link #getModelName() target field} must
	 * match.
	 * 
	 * @return May be <code>null</code>, when the exact error text is asserted.
	 * 
	 * @see #getError()
	 * @see FormField#getError()
	 */
	@Format(RegExpValueProvider.class)
	@Name(ERROR_PATTERN_NAME)
	Pattern getErrorPattern();

	/**
	 * Setter for {@link #getErrorPattern()}.
	 */
	void setErrorPattern(Pattern error);

}
