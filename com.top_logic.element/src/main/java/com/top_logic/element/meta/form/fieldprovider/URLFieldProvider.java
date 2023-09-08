/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.URLControlProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.URLConstraint;

/**
 * Create field for an URL.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class URLFieldProvider extends StringFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		FormMember field = super.getFormField(editContext, fieldName);
		field.setControlProvider(new URLControlProvider());
		return field;
	}

	@Override
	protected Constraint createLengthConstraint(int minLength, int maxLength) {
		return new URLConstraint(minLength, maxLength);
	}
}