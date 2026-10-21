/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.CommonMark;
import com.top_logic.layout.form.values.edit.annotation.Html;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;

/**
 * {@link ConfigAnnotationControlProvider} editing the text of a property annotated with
 * {@link MultiLine} in a text area with the annotated number of rows.
 *
 * <p>
 * The input behaves like the single-line text input in every other respect - a formatted value is
 * sent when the field is left, a plain text while it is typed.
 * </p>
 *
 * <p>
 * Only a value {@link ConfigControlService#holdsText(ConfigFieldModel) held as text} is edited in
 * a text area. Any other property is left to the control the {@link ConfigControlService} chooses
 * without the annotation; a control that edits text of its own, such as the editor of an
 * internationalized text, reads the number of rows from the annotation itself.
 * </p>
 */
public class MultiLineTextConfigProvider implements ConfigAnnotationControlProvider<MultiLine> {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model, MultiLine annotation) {
		if (!ConfigControlService.holdsText(model)) {
			return null;
		}
		ReactTextInputControl input = ConfigControlService.createTextInput(context, model);
		input.setMultiline(annotation.rows());
		return input;
	}

	/**
	 * The number of rows of a multi-line input for the text of the given property.
	 *
	 * <p>
	 * The rows of a {@link MultiLine} annotation of the property, or {@link MultiLineText#DEFAULT_ROWS}
	 * for a {@link CommonMark} or {@link Html} text without one - such a text spans several lines.
	 * </p>
	 *
	 * @return The number of rows, or {@code 0} if the text of the property is a single line.
	 */
	public static int rows(PropertyDescriptor property) {
		MultiLine multiLine = ConfigControlService.annotation(property, MultiLine.class);
		if (multiLine != null) {
			return multiLine.rows();
		}
		if (ConfigControlService.annotation(property, CommonMark.class) != null
			|| ConfigControlService.annotation(property, Html.class) != null) {
			return MultiLineText.DEFAULT_ROWS;
		}
		return 0;
	}

}
