/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.fieldprovider;

import com.top_logic.element.i18n.I18NField;
import com.top_logic.element.i18n.I18NFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextField;
import com.top_logic.model.annotate.AllLanguagesInViewMode;
import com.top_logic.model.wysiwyg.annotation.StructuredTextEditorConfig;

/**
 * {@link FieldProvider} for {@link I18NStructuredTextField}.
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class I18NStructureTextFieldProvider extends I18NFieldProvider {

	@Override
	protected I18NStructuredTextField createField(EditContext editContext, String fieldName, boolean mandatory,
			boolean immutable, boolean multiLine, Constraint constraint) {

		StructuredTextEditorConfig annotation = editContext.getAnnotation(StructuredTextEditorConfig.class);

		I18NStructuredTextField field;
		/* Ignore "multiLine", as single line StructuredText fields are not supported. */
		if (annotation != null) {
			field = I18NStructuredTextField.new18NStructuredTextField(fieldName, mandatory, immutable, constraint,
				annotation.getFeatures(), annotation.getTemplateFiles(), annotation.getTemplates());
		} else {
			field =
				I18NStructuredTextField.new18NStructuredTextField(fieldName, mandatory, immutable, constraint, null);
		}
		AllLanguagesInViewMode allLanguagesAnnotation = editContext.getAnnotation(AllLanguagesInViewMode.class);
		if (allLanguagesAnnotation != null && allLanguagesAnnotation.getValue()) {
			field.set(I18NField.DISPLAY_ALL_LANGUAGES_IN_VIEW_MODE, true);
		}
		return field;
	}

	@Override
	protected Constraint createLengthConstraint(int minLength, int maxLength) {
		return null; // The length cannot be constrained.
	}

}
