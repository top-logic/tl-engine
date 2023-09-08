/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.text.Format;

import com.top_logic.basic.format.IdentityFormat;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.constraints.ListConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.format.StringTokenFormat;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link LegacyTypeCodes#TYPE_STRING_SET}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringSetFieldProvider extends AbstractWrapperFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isSearch = editContext.isSearchUpdate();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON
			: null;
		if (editContext.isRestricted() && !isSearch) {
			SelectField theSelect =
				newSelectWrapperSetField(editContext, fieldName, true, false, isMandatory, isSearch,
					mandatoryChecker,
					isDisabled, false);
			theSelect.setOptionLabelProvider(MetaLabelProvider.INSTANCE);
			return theSelect;
		}

		return getStandardStringSetField(editContext, fieldName);
	}

	/**
	 * Get the FormField for a non-restricted StringSetMetaAttribute
	 * 
	 * @param editContext
	 *        the AttributeUpdate for the attribute
	 * @param fieldName
	 *        the name of the field
	 * @return the field
	 */
	protected AbstractFormField getStandardStringSetField(EditContext editContext,
			String fieldName) {
		boolean isDisabled = editContext.isDisabled();

		Format format = new StringTokenFormat(IdentityFormat.INSTANCE, String.valueOf(','), null, true);
		ComplexField result = FormFactory.newComplexField(fieldName, format, true, false, isDisabled, null);
		
		result.addConstraint(new ListConstraint(new StringLengthConstraint(0, 100)));
		
		return result;
	}

}
