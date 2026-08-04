/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * {@link ReactFieldControlProvider} for boolean attributes.
 *
 * <p>
 * A tri-state attribute gets the checkbox' third state for "no value"; a two-valued boolean stays a
 * plain checkbox.
 * </p>
 */
public class CheckboxControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactCheckboxControl(context, model, isTriState(part));
	}

	private static boolean isTriState(TLStructuredTypePart part) {
		if (part == null) {
			return false;
		}
		TLType type = part.getType();
		return type instanceof TLPrimitive primitive && primitive.getKind() == TLPrimitive.Kind.TRISTATE;
	}

}
