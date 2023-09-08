/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.fieldprovider;

import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.wysiwyg.ui.StructuredTextFieldFactory;
import com.top_logic.model.wysiwyg.annotation.StructuredTextEditorConfig;

/**
 * {@link FieldProvider} for attributes of type <code>Html</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredTextFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		if (editContext.isSearchUpdate()) {
			return FormFactory.newStringField(fieldName);
		} else {
			return createField(editContext, fieldName);
		}
	}

	private FormField createField(EditContext editContext, String fieldName) {
		StructuredTextEditorConfig annotation = editContext.getAnnotation(StructuredTextEditorConfig.class);

		FormField result;
		if (annotation != null) {
			result = StructuredTextFieldFactory.create(fieldName, null, annotation.getFeatures(),
				annotation.getTemplateFiles(), annotation.getTemplates());
		} else {
			result = StructuredTextFieldFactory.create(fieldName, null);
		}
		// Note: If a field has a control provider set, this cannot be overridden in table context
		// with an edit control provider.
		result.setControlProvider(null);
		return result;
	}

}
