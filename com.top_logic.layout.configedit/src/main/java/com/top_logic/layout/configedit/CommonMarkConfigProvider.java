/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.layout.form.values.edit.annotation.CommonMark;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactRenderedTextControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;

/**
 * {@link ConfigAnnotationControlProvider} for a property whose text is written in
 * {@link CommonMark} syntax.
 *
 * <p>
 * While the property can be edited, its text is edited in a text area, whose number of rows is
 * taken from a {@link MultiLine} annotation of the property. While it cannot, the formatted text is
 * displayed.
 * </p>
 *
 * <p>
 * Only a value {@link ConfigControlService#holdsText(ConfigFieldModel) held as text} is edited this
 * way. Any other property is left to the control the {@link ConfigControlService} chooses without
 * the annotation.
 * </p>
 */
public class CommonMarkConfigProvider implements ConfigAnnotationControlProvider<CommonMark> {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model, CommonMark annotation) {
		if (!ConfigControlService.holdsText(model)) {
			return null;
		}
		int rows = MultiLineTextConfigProvider.rows(model.getProperty());
		return new ReactRenderedTextControl(context, model, () -> {
			ReactTextInputControl input = ConfigControlService.createTextInput(context, model);
			input.setMultiline(rows);
			return input;
		}, com.top_logic.layout.form.format.CommonMark::toHtml);
	}

}
