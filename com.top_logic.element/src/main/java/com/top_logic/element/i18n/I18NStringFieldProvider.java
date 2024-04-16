/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.annotate.AllLanguagesInViewMode;
import com.top_logic.model.annotate.TLSize;

/**
 * {@link FieldProvider} for {@link I18NStringField}.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStringFieldProvider extends I18NFieldProvider {

	@Override
	protected I18NStringField createField(EditContext editContext, String fieldName, boolean mandatory,
			boolean immutable, boolean multiLine) {
		Constraint constraint;
		if (!editContext.isDerived()) {
			TLSize size = editContext.getAnnotation(TLSize.class);
			int minLength = AttributeOperations.getLowerBound(size);
			int maxLength = AttributeOperations.getUpperBound(size);
			constraint = new StringLengthConstraint(minLength, maxLength);
		} else {
			constraint = FormFactory.NO_CONSTRAINT;
		}

		I18NStringField field =
			I18NStringField.newI18NStringField(fieldName, mandatory, immutable, multiLine, constraint);
		AllLanguagesInViewMode annotation = editContext.getAnnotation(AllLanguagesInViewMode.class);
		if (annotation != null && annotation.getValue()) {
			field.set(I18NField.DISPLAY_ALL_LANGUAGES_IN_VIEW_MODE, true);
		}
		return field;
	}

}
