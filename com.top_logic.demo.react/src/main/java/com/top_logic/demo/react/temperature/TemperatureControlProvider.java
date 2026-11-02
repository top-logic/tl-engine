/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link ReactFieldControlProvider} creating a {@link TemperatureInputControl}.
 *
 * <p>
 * Set as input control of the type <code>demo.temperature:Temperature</code>, so that every
 * attribute of that type is entered through it:
 * </p>
 *
 * <pre>
 * &lt;input-control&gt;
 *   &lt;impl class="com.top_logic.demo.react.temperature.TemperatureControlProvider"/&gt;
 * &lt;/input-control&gt;
 * </pre>
 */
@Label("Temperature input")
@InApp
@TargetType(value = TLTypeKind.CUSTOM, name = TemperatureControlProvider.TEMPERATURE_TYPE)
public class TemperatureControlProvider implements ReactFieldControlProvider {

	/** Qualified name of the model type of the temperatures entered here. */
	public static final String TEMPERATURE_TYPE = "demo.temperature:Temperature";

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return new TemperatureInputControl(context, model);
	}

}
