/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import java.util.regex.Pattern;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.FieldValidity;
import com.top_logic.layout.scripting.action.assertion.FieldErrorAssertion;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link FieldAssertionOp} for {@link FieldErrorAssertion}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FieldErrorAssertionOp extends FieldAssertionOp<FieldErrorAssertion> {

	/**
	 * Creates a new {@link FieldErrorAssertionOp}.
	 */
	public FieldErrorAssertionOp(InstantiationContext context, FieldErrorAssertion config) {
		super(context, config);
	}

	@Override
	protected void checkAsserts(FormField field, ActionContext actionContext) {
		FieldValidity actualValidity = FieldValidity.getValidity(field);
		if (actualValidity != FieldValidity.ERROR) {
			ApplicationAssertions.fail(config, "The field has no error.");
		}
		String actualError = field.getError();
		String expectedError = getConfig().getError();
		if (!expectedError.isEmpty()) {
			ApplicationAssertions.assertEquals(config, "The field has a wrong error message.", expectedError,
				actualError);
		}
		Pattern expectedErrorPattern = getConfig().getErrorPattern();
		if (expectedErrorPattern != null) {
			if (!expectedErrorPattern.matcher(actualError).matches()) {
				ApplicationAssertions.fail(config,
					"Field error '" + actualError + "' does not match expected error pattern '"
						+ expectedErrorPattern.pattern() + "'.");
			}
		}
	}
}

