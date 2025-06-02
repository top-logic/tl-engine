/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Color;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.format.ColorFormat;

/**
 * {@link LabelProvider} for {@link Color}s printing their CSS value.
 * 
 * @see ColorFormat
 */
public class ColorLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link ColorLabelProvider} instance.
	 */
	public static final ColorLabelProvider INSTANCE = new ColorLabelProvider();

	private ColorLabelProvider() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		return ColorFormat.formatColor((Color) object);
	}

}
