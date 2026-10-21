/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.control.ValueDisplayControl;
import com.top_logic.layout.form.control.ValueDisplayControl.ValueDisplay;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.AnnotationControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;

/**
 * {@link AnnotationControlProvider} displaying the value of a property that cannot be set with the
 * labels of its {@link OptionLabels} annotation.
 *
 * <p>
 * A property that cannot be set - a derived property, or the configuration interface of an item -
 * offers no options to choose from. Its value is displayed read-only, labelled the same way as the
 * options of a property that can be set. A property that can be set keeps its control, a select
 * field, for instance, labels its options and its selection itself.
 * </p>
 *
 * <p>
 * The value is displayed with a {@link ValueDisplayControl}.
 * </p>
 */
public class OptionLabelsDisplayProvider implements AnnotationControlProvider<OptionLabels> {

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			OptionLabels annotation) {
		if (property.canHaveSetter()) {
			return null;
		}
		LabelProvider labels = Fields.optionLabelsOrNull(editorFactory.formOptions(property));
		if (labels == null) {
			return null;
		}
		return new ValueDisplay(ResourceRenderer.newResourceRenderer(labels));
	}

}
