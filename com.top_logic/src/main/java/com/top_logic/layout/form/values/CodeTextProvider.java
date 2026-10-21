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
import com.top_logic.layout.form.values.edit.annotation.Code;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;

/**
 * {@link AnnotationControlProvider} displaying a property annotated with {@link Code} with a
 * {@link MultiLineText}.
 *
 * <p>
 * The source code is edited as plain text, without regard to its language. The number of rows is
 * taken from a {@link MultiLine} annotation of the property, if there is one.
 * </p>
 */
public class CodeTextProvider implements AnnotationControlProvider<Code> {

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			Code annotation) {
		MultiLine multiLine = editorFactory.getAnnotation(property, MultiLine.class);
		return new MultiLineText(multiLine != null ? multiLine.rows() : MultiLineText.DEFAULT_ROWS);
	}

}
