/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Color;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} for {@link Color} fields.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ColorControlProvider implements ControlProvider {

	/**
	 * Singleton {@link ColorControlProvider} instance.
	 */
	public static final ColorControlProvider INSTANCE = new ColorControlProvider();

	private ColorControlProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createControl(Object model, String style) {
		return new ColorChooserControl((FormField) model);
	}

}