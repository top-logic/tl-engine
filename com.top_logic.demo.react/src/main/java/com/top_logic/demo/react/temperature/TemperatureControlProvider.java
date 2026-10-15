/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;

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
public class TemperatureControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return new TemperatureInputControl(context, model);
	}

}
