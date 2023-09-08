/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.awt.Color;
import java.text.Format;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.format.ConfigurationFormatAdapter;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ConfigurationValueProvider} adapting {@link ColorFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColorConfigFormat extends ConfigurationFormatAdapter<Color> {

	/**
	 * Singleton {@link ColorConfigFormat} instance.
	 */
	public static final ColorConfigFormat INSTANCE = new ColorConfigFormat();

	private ColorConfigFormat() {
		super(Color.class);
	}

	@Override
	protected Format format() {
		return ColorFormat.INSTANCE;
	}

	@Override
	protected ResKey errorMessage(int errorIndex) {
		return I18NConstants.ERROR_INVALID_COLOR_FORMAT__VALUE_PROPERTY_POS.fill(errorIndex);
	}

}
