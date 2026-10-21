/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.Code;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;

/**
 * {@link ConfigAnnotationControlProvider} editing the source code of a property annotated with
 * {@link Code} in a text area.
 *
 * <p>
 * The source code is edited as plain text, without regard to its language. The number of rows is
 * taken from a {@link MultiLine} annotation of the property, or is
 * {@link MultiLineText#DEFAULT_ROWS} without one.
 * </p>
 *
 * <p>
 * Only a value {@link ConfigControlService#holdsText(ConfigFieldModel) held as text} is edited in
 * a text area. Any other property is left to the control the {@link ConfigControlService} chooses
 * without the annotation.
 * </p>
 */
public class CodeTextConfigProvider implements ConfigAnnotationControlProvider<Code> {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model, Code annotation) {
		if (!ConfigControlService.holdsText(model)) {
			return null;
		}
		MultiLine multiLine = ConfigControlService.annotation(model.getProperty(), MultiLine.class);
		ReactTextInputControl input = ConfigControlService.createTextInput(context, model);
		input.setMultiline(multiLine != null ? multiLine.rows() : MultiLineText.DEFAULT_ROWS);
		return input;
	}

}
