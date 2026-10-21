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
import com.top_logic.layout.form.values.edit.annotation.CommonMark;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;

/**
 * {@link AnnotationControlProvider} displaying a property annotated with {@link CommonMark} with a
 * {@link CommonMarkText}.
 *
 * <p>
 * The number of rows is taken from a {@link MultiLine} annotation of the same property, or is
 * {@link MultiLineText#DEFAULT_ROWS} without one.
 * </p>
 */
public class CommonMarkTextProvider implements AnnotationControlProvider<CommonMark> {

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			CommonMark annotation) {
		MultiLine multiLine = editorFactory.getAnnotation(property, MultiLine.class);
		return new CommonMarkText(multiLine != null ? multiLine.rows() : MultiLineText.DEFAULT_ROWS);
	}

}
