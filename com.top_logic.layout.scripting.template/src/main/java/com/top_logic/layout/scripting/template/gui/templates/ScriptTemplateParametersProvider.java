/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.AnnotationControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;

/**
 * {@link AnnotationControlProvider} displaying the {@link ScriptTemplateParameters} of a script
 * template as a table with the {@link TabularParameterDisplay}.
 */
public class ScriptTemplateParametersProvider implements AnnotationControlProvider<ScriptTemplateParameters> {

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			ScriptTemplateParameters annotation) {
		return TabularParameterDisplay.INSTANCE;
	}

}
