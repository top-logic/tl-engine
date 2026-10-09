/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;
import com.top_logic.layout.react.field.FieldControlRegistry;

/**
 * {@link ReactFieldControlProvider} for {@code I18NString} attributes.
 *
 * <p>
 * Delegates to the self-contained {@link ReactI18NStringInputControl#createEditor editor}, which
 * already bundles the inline current-locale input with the all-languages dialog button.
 * </p>
 *
 * <p>
 * A text displayed on {@link FieldControlRegistry#isMultiline(FieldSpec) several rows} is
 * {@link #isLarge(FieldSpec) large}: where it has no room, the first line of the text in the
 * user's language stands for it.
 * </p>
 */
public class I18NStringControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return ReactI18NStringInputControl.createEditor(context, model,
			field.getMultilineRows(), field.getLabel());
	}

	@Override
	public boolean isLarge(FieldSpec field) {
		return FieldControlRegistry.isMultiline(field);
	}
}
