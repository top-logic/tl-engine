/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for string, tristate, binary, and other text-representable
 * attributes.
 *
 * <p>
 * Also serves as the ultimate fallback when no other provider matches.
 * </p>
 */
public class TextInputControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactTextInputControl(context, model);
	}

}
