/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.awt.Color;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactColorInputControl;

/**
 * {@link ConfigControlProvider} editing a {@link Color} property with the color chooser of
 * {@link ReactColorInputControl}.
 *
 * <p>
 * Registered in the {@link ConfigControlService} configuration as the provider for the value type
 * {@link Color}. The mapping claims the property, so the control is bound to a
 * {@link ConfigFieldModel} holding the {@link Color} itself, which {@link ReactColorInputControl}
 * encodes for the client and decodes from the client's value.
 * </p>
 */
public class ColorInputConfigProvider implements ConfigControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		return new ReactColorInputControl(context, model);
	}

}
