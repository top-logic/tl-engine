/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.AnnotationControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;

/**
 * {@link AnnotationControlProvider} displaying a property annotated with {@link MultiLine} with a
 * {@link MultiLineText} of the annotated number of rows.
 */
public class MultiLineTextProvider implements AnnotationControlProvider<MultiLine> {

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			MultiLine annotation) {
		return new MultiLineText(annotation.rows());
	}

}
