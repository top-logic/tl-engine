/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.Arrays;

import com.top_logic.basic.col.Equality;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.provider.BooleanLabelProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.util.Resources;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link Boolean}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON
			: null;

		BooleanField result =
			FormFactory.newBooleanField(fieldName, null, isMandatory, isDisabled, mandatoryChecker);
		// Note: Derived attributes of type boolean have no BooleanAttributeConfig.
		if (AttributeOperations.getBooleanDisplay(editContext) == BooleanPresentation.SELECT) {
			SelectFieldUtils.setOptions(result, Arrays.asList(true, false));
			SelectFieldUtils.setOptionLabelProvider(result, BooleanLabelProvider.INSTANCE);
			SelectFieldUtils.setOptionComparator(result, Equality.INSTANCE);
			SelectFieldUtils.setEmptySelectionLabelImmutable(result,
				Resources.getInstance().getString(I18NConstants.NONE_LABEL));
		}
		return result;
	}

}
