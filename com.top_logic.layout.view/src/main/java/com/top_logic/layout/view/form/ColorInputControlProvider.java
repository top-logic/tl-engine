/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.control.form.ReactColorInputControl;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link ReactFieldControlProvider} for color attributes.
 */
@Label("Color selection")
@InApp
@TargetType(value = TLTypeKind.CUSTOM, name = TypeSpec.COLOR_TYPE)
public class ColorInputControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return new ReactColorInputControl(context, model);
	}

}
