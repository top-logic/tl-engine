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
import com.top_logic.layout.react.control.form.ReactColorInputControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * Maps {@link TLStructuredTypePart} types to React input controls backed by {@link FieldModel}.
 */
public class FieldControlFactory {

	/**
	 * Creates the appropriate input control for the given attribute.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param part
	 *        The model attribute.
	 * @param model
	 *        The field model providing value, editability, and change notifications.
	 * @return A React control for the field input widget.
	 */
	public static ReactControl createFieldControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		TLType type = part.getType();

		if (type instanceof TLPrimitive) {
			TLPrimitive primitive = (TLPrimitive) type;
			switch (primitive.getKind()) {
				case BOOLEAN:
					return new ReactCheckboxControl(context, model);
				case INT:
					return new ReactNumberInputControl(context, model, 0);
				case FLOAT:
					return new ReactNumberInputControl(context, model, 2);
				case DATE:
					return new ReactDatePickerControl(context, model);
				case STRING:
				case TRISTATE:
				case BINARY:
					return new ReactTextInputControl(context, model);
				case CUSTOM:
					if (isColorType(primitive)) {
						return new ReactColorInputControl(context, model);
					}
					return new ReactTextInputControl(context, model);
				default:
					return new ReactTextInputControl(context, model);
			}
		}

		return new ReactTextInputControl(context, model);
	}

	private static boolean isColorType(TLPrimitive primitive) {
		return java.awt.Color.class.getName().equals(
			primitive.getStorageMapping().getApplicationType().getName());
	}
}
