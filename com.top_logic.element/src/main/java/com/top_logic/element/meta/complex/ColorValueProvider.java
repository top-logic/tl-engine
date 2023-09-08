/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.complex;

import java.awt.Color;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.element.meta.ComplexValueProvider;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.layout.form.format.ColorFormat;

/**
 * {@link ConfigurationValueProvider} allowing to store {@link Color} values in configurations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColorValueProvider implements ComplexValueProvider<Color> {

	/**
	 * Singleton {@link ColorValueProvider} instance.
	 */
	public static final ColorValueProvider INSTANCE = new ColorValueProvider();

	private ColorValueProvider() {
		super();
	}

	@Override
	public Class<Color> getApplicationType() {
		return Color.class;
	}

	@Override
	public Color getBusinessObject(Object aStorageObject) {
		if (aStorageObject == null) {
			return null;
		}
		return ColorFormat.parseColor((String) aStorageObject);
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		return ColorFormat.formatColor((Color) aBusinessObject);
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Color;
	}

	@Override
	public OptionProvider getOptionProvider() {
		return null;
	}

}
