/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.AnnotationControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.Html;

/**
 * {@link AnnotationControlProvider} displaying a property annotated with {@link Html} with a
 * {@link StructuredTextControl} offering the annotated feature set.
 *
 * <p>
 * The feature set is one defined in the {@link StructuredTextConfigService}. Without a feature set,
 * the standard editor for an HTML text is used.
 * </p>
 */
public class HtmlEditorProvider implements AnnotationControlProvider<Html> {

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			Html annotation) {
		String featureSet = annotation.features();
		if (featureSet.isEmpty()) {
			return StructuredTextControlProvider.INSTANCE;
		}
		return new StructuredTextControlProvider(featureSet);
	}

}
