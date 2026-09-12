/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;

/**
 * {@link ReactFieldControlProvider} for integer and floating-point attributes.
 *
 * <p>
 * The value is displayed in and entered in the {@link FieldSpec#getNumberFormat() format the field
 * asks for} - the attribute's format annotation, where it has one - so a German user reads and types
 * {@code 12,5} where an English user reads and types {@code 12.5}.
 * </p>
 */
public class NumberInputControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return new ReactNumberInputControl(context, model, FieldControlRegistry.numberFormat(field));
	}

}
