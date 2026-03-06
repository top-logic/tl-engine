/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * Maps {@link TLStructuredTypePart} types to lean React input controls.
 *
 * <p>
 * Uses only tl-core model APIs. No dependency on {@code com.top_logic.element}.
 * </p>
 */
public class FieldControlFactory {

	/**
	 * Creates the appropriate input control for the given attribute.
	 *
	 * @param part
	 *        The model attribute.
	 * @param value
	 *        The current value.
	 * @param editable
	 *        Whether the field should be editable.
	 * @return A React control for the field input widget.
	 */
	public static ReactControl createFieldControl(TLStructuredTypePart part, Object value,
			boolean editable) {
		TLType type = part.getType();

		if (type instanceof TLPrimitive) {
			TLPrimitive primitive = (TLPrimitive) type;
			switch (primitive.getKind()) {
				case BOOLEAN:
					return createCheckbox(value, editable);
				case INT:
					return createNumberInput(value, editable, 0);
				case FLOAT:
					return createNumberInput(value, editable, 2);
				case DATE:
					return createDatePicker(value, editable);
				case STRING:
				case TRISTATE:
				case BINARY:
				case CUSTOM:
				default:
					return createTextInput(value, editable);
			}
		}

		// Reference types and unknown: display as text for now.
		return createTextInput(asLabel(value), editable);
	}

	private static ReactControl createTextInput(Object value, boolean editable) {
		return new ViewTextInputControl(value != null ? value.toString() : "", editable);
	}

	private static ReactControl createCheckbox(Object value, boolean editable) {
		return new ViewCheckboxControl(Boolean.TRUE.equals(value), editable);
	}

	private static ReactControl createNumberInput(Object value, boolean editable, int decimals) {
		return new ViewNumberInputControl(value instanceof Number ? (Number) value : null,
			editable, decimals);
	}

	private static ReactControl createDatePicker(Object value, boolean editable) {
		return new ViewDatePickerControl(value, editable);
	}

	private static String asLabel(Object value) {
		if (value == null) {
			return "";
		}
		return MetaLabelProvider.INSTANCE.getLabel(value);
	}
}
