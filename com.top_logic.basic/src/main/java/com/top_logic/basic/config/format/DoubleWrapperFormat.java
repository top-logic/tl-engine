/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for properties of type {@link Double}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DoubleWrapperFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link DoubleWrapperFormat} instance.
	 */
	public static final DoubleWrapperFormat INSTANCE = new DoubleWrapperFormat();

	/** {@link GenericArrayFormat} for arrays of type {@link Double}. */
	public static final GenericArrayFormat<Double[]> ARRAY_FORMAT =
		new GenericArrayFormat<>(Double[].class, INSTANCE);

	private DoubleWrapperFormat() {
		super(Double.class);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getDouble(propertyName, propertyValue);
	}
}