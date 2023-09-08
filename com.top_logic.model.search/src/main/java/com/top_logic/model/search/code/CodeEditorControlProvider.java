/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.code;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.model.search.annotate.CodeEditorLanguage;

/**
 * {@link DisplayProvider} for code editor attributes.
 */
public class CodeEditorControlProvider extends AbstractFormFieldControlProvider {

	/**
	 * Singleton {@link CodeEditorControlProvider} instance.
	 */
	public static final CodeEditorControlProvider INSTANCE = new CodeEditorControlProvider();

	private CodeEditorControlProvider() {
		// Singleton constructor.
	}

	@Override
	protected Control createInput(FormMember member) {
		AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(member);

		String languageMode = CodeEditorControl.MODE_TEXT;
		CodeEditorLanguage languageAnnotation = update.getAnnotation(CodeEditorLanguage.class);
		if (languageAnnotation != null) {
			languageMode = languageAnnotation.getValue();
		}

		return new CodeEditorControl((FormField) member, languageMode);
	}

}
