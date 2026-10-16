/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.codeedit.control;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.AnnotationControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.Code;
import com.top_logic.layout.form.values.edit.annotation.CodeLanguage;

/**
 * {@link AnnotationControlProvider} displaying a property annotated with {@link Code} with a
 * {@link CodeEditorControl} for the annotated language.
 */
public class CodeEditorProvider implements AnnotationControlProvider<Code> {

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			Code annotation) {
		return new CodeEditorControl.CP(languageMode(annotation.language()));
	}

	/**
	 * The {@link CodeEditorControl#getLanguageMode() language mode} of the {@link CodeEditorControl}
	 * for the given {@link CodeLanguage}.
	 */
	public static String languageMode(CodeLanguage language) {
		switch (language) {
			case PLAIN:
				return CodeEditorControl.MODE_TEXT;
			case XML:
				return CodeEditorControl.MODE_XML;
			case HTML:
				return CodeEditorControl.MODE_HTML;
			case JSON:
				return CodeEditorControl.MODE_JSON;
			case CSS:
				return CodeEditorControl.MODE_CSS;
			case JAVASCRIPT:
				return CodeEditorControl.MODE_JS;
			case MARKDOWN:
				return CodeEditorControl.MODE_MARKDOWN;
		}
		throw new UnreachableAssertion("No such language: " + language);
	}

}
