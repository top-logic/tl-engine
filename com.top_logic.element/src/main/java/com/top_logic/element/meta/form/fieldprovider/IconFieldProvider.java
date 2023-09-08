/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.format.ThemeImageFormat;
import com.top_logic.layout.form.model.FormFactory;

/**
 * {@link FieldProvider} for attributes displaying icons.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IconFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {

		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON
			: null;

		return FormFactory.newComplexField(fieldName, ThemeImageFormat.INSTANCE, false, isMandatory,
			isDisabled, mandatoryChecker);
	}

}
