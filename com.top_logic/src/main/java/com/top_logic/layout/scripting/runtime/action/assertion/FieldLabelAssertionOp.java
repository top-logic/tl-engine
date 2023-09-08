/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.action.assertion.FieldLabelAssertion;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Assertion for the {@link FormMember#getLabel() label} of a {@link FormMember}. The comparison is
 * done case-sensitive. (As the humans using the applications usually care about it.)
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FieldLabelAssertionOp extends FormAssertionOp<FieldLabelAssertion> {

	public FieldLabelAssertionOp(InstantiationContext context, FieldLabelAssertion config) {
		super(context, config);
	}

	@Override
	protected void checkAsserts(FormMember formMember, ActionContext actionContext) {
		Object expectedLabel = getConfig().getLabel();
		Object actualLabel = formMember.getLabel();
		ApplicationAssertions.assertEquals(config, "The field has a wrong label.", expectedLabel, actualLabel);
	}

}
