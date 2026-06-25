/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for {@code I18NString} attributes.
 *
 * <p>
 * Delegates to the self-contained {@link ReactI18NStringInputControl#createEditor editor}, which
 * already bundles the inline current-locale input with the all-languages dialog button.
 * </p>
 */
public class I18NStringControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return ReactI18NStringInputControl.createEditor(context, model,
			ReactFieldControlProvider.multilineRows(part));
	}

}
