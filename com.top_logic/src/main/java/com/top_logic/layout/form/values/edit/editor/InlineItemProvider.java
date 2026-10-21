/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.AnnotationControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.Inline;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;

/**
 * {@link AnnotationControlProvider} displaying an item property annotated with {@link Inline}
 * with a {@link GroupInlineControlProvider}, which shows the properties of the nested item without
 * a group around them.
 *
 * <p>
 * A property that is not displayed as nested item form - a property of another kind, an item
 * displayed as a single value, or a property with an editor of its own chosen by a
 * {@link PropertyEditor} annotation - keeps its control.
 * </p>
 */
public class InlineItemProvider implements AnnotationControlProvider<Inline> {

	private static final ControlProvider INLINE = new GroupInlineControlProvider();

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			Inline annotation) {
		if (property.kind() != PropertyKind.ITEM) {
			return null;
		}
		if (Fields.itemDisplay(editorFactory, property) == ItemDisplayType.VALUE) {
			return null;
		}
		if (editorFactory.getAnnotation(property, PropertyEditor.class) != null) {
			return null;
		}
		return INLINE;
	}

}
