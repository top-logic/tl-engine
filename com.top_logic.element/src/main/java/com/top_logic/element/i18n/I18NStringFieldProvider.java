/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.model.annotate.AllLanguagesInViewMode;

/**
 * {@link FieldProvider} for {@link I18NStringField}.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStringFieldProvider extends I18NFieldProvider {

	@Override
	protected I18NStringField createField(EditContext editContext, String fieldName, boolean mandatory,
			boolean immutable, boolean multiLine, Constraint constraint) {
		I18NStringField field =
			I18NStringField.newI18NStringField(fieldName, mandatory, immutable, multiLine, constraint);
		AllLanguagesInViewMode annotation = editContext.getAnnotation(AllLanguagesInViewMode.class);
		if (annotation != null && annotation.getValue()) {
			field.set(I18NField.DISPLAY_ALL_LANGUAGES_IN_VIEW_MODE, true);
		}
		return field;
	}

	@Override
	protected Constraint createLengthConstraint(int minLength, int maxLength) {
		return new StringLengthConstraint(minLength, maxLength);
	}

}
