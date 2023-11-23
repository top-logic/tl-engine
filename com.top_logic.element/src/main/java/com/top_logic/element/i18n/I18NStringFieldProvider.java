/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.i18n.I18NField;
import com.top_logic.layout.form.i18n.I18NStringField;
import com.top_logic.model.annotate.AllLanguagesInViewMode;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.ui.MultiLine;

/**
 * {@link FieldProvider} for {@link I18NStringField}.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStringFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean mandatory = editContext.isMandatory();
		boolean immutable = editContext.isDisabled();
		boolean multiLine = AttributeOperations.isMultiline(editContext.getAnnotation(MultiLine.class));

		I18NStringField field = I18NStringField.newI18NStringField(fieldName, mandatory, immutable, multiLine);
		AllLanguagesInViewMode annotation = editContext.getAnnotation(AllLanguagesInViewMode.class);
		if (annotation != null && annotation.getValue()) {
			field.set(I18NField.DISPLAY_ALL_LANGUAGES_IN_VIEW_MODE, true);
		}
		if (!editContext.isDerived()) {
			Constraint constraint;
			TLSize size = editContext.getAnnotation(TLSize.class);
			int minLength = AttributeOperations.getLowerBound(size);
			int maxLength = AttributeOperations.getUpperBound(size);
			constraint = new StringLengthConstraint(minLength, maxLength);
			field.getLanguageFields().forEach(languageField -> languageField.addConstraint(constraint));
		}
		return field;
		
	}

}
