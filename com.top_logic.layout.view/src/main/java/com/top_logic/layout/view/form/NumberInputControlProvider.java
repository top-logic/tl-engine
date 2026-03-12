/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * {@link ReactFieldControlProvider} for integer and floating-point attributes.
 *
 * <p>
 * Inspects the attribute's {@link com.top_logic.model.TLPrimitive.Kind} to determine the number of
 * decimal places: {@link com.top_logic.model.TLPrimitive.Kind#INT} uses 0,
 * {@link com.top_logic.model.TLPrimitive.Kind#FLOAT} uses 2.
 * </p>
 */
public class NumberInputControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		int decimalPlaces = 2;
		TLType type = part.getType();
		if (type instanceof TLPrimitive) {
			TLPrimitive primitive = (TLPrimitive) type;
			if (primitive.getKind() == TLPrimitive.Kind.INT) {
				decimalPlaces = 0;
			}
		}
		return new ReactNumberInputControl(context, model, decimalPlaces);
	}

}
