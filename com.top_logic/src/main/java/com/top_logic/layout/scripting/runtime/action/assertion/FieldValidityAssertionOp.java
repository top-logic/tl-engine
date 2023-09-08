/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.FieldValidity;
import com.top_logic.layout.scripting.action.assertion.FieldValidityAssertion;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Assertion for the {@link FieldValidity validity} of a {@link FormField}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FieldValidityAssertionOp extends FieldAssertionOp<FieldValidityAssertion> {

	public FieldValidityAssertionOp(InstantiationContext context, FieldValidityAssertion config) {
		super(context, config);
	}

	@Override
	protected void checkAsserts(FormField field, ActionContext actionContext) {
		Object expectedValidity = getConfig().getValidity();
		Object actualValidity = FieldValidity.getValidity(field);
		ApplicationAssertions.assertEquals(config, "The field has a wrong validity.", expectedValidity, actualValidity);
	}

}
