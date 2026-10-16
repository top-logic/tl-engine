/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.function.Function;

import com.top_logic.layout.form.values.edit.annotation.Html;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactRenderedTextControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;

/**
 * {@link ConfigAnnotationControlProvider} for a property whose text is {@link Html}, editing the
 * HTML source in a text area.
 *
 * <p>
 * While the property can be edited, its source is edited in a text area, whose number of rows is
 * taken from a {@link MultiLine} annotation of the property, regardless of the feature set of the
 * annotation. While it cannot, the formatted text is displayed.
 * </p>
 *
 * <p>
 * Only a value {@link ConfigControlService#holdsText(ConfigFieldModel) held as text} is edited this
 * way. Any other property is left to the control the {@link ConfigControlService} chooses without
 * the annotation; the editor of an internationalized text, for instance, edits each translation
 * the same way itself.
 * </p>
 */
public class HtmlTextConfigProvider implements ConfigAnnotationControlProvider<Html> {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model, Html annotation) {
		if (!ConfigControlService.holdsText(model)) {
			return null;
		}
		int rows = MultiLineTextConfigProvider.rows(model.getProperty());
		return new ReactRenderedTextControl(context, model, () -> {
			ReactTextInputControl input = ConfigControlService.createTextInput(context, model);
			input.setMultiline(rows);
			return input;
		}, Function.identity());
	}

}
