/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.FieldMode;
import com.top_logic.layout.scripting.action.assertion.FieldModeAssertion;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Assertion for the {@link FieldMode mode} of a {@link FormMember}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FieldModeAssertionOp extends FormAssertionOp<FieldModeAssertion> {

	public FieldModeAssertionOp(InstantiationContext context, FieldModeAssertion config) {
		super(context, config);
	}

	@Override
	protected void checkAsserts(FormMember formMember, ActionContext actionContext) {
		Object expectedMode = getConfig().getMode();
		Object actualMode = FieldMode.getMode(formMember);
		ApplicationAssertions.assertEquals(config, "The field has a wrong mode.", expectedMode, actualMode);
	}

}
