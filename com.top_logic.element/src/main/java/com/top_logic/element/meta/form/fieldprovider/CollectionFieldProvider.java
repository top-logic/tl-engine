/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.Collections;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FieldProvider} for multiple unordered reference {@link TLStructuredTypePart}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CollectionFieldProvider extends AbstractSelectFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isSearch = editContext.isSearchUpdate();
		boolean isCalculated = editContext.isDerived();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON
			: null;

		if (isSearch && isCalculated) {
			return null;
		}

		boolean isOrdered = editContext.isOrdered();

		return newSelectField(fieldName, Collections.EMPTY_LIST, /* isMultiple */true, isOrdered, isSearch, isMandatory,
			mandatoryChecker, isDisabled, false);
	}

}
