/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.SelectField;

/**
 * {@link FieldProvider} for a reference attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrapperFieldProvider extends AbstractWrapperFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		boolean isMultiple = editContext.isMultiple();
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isSearch = editContext.isSearchUpdate();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON : null;

		boolean isOrdered = editContext.isOrdered();

		SelectField result = newSelectWrapperSetField(editContext, fieldName, isMultiple,
			isOrdered, isMandatory, isSearch, mandatoryChecker, isDisabled, false);
		return result;
	}

}
