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
 * {@link ConfigurationValueProvider} for properties of type {@link Float}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class FloatWrapperFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link FloatWrapperFormat} instance.
	 */
	public static final FloatWrapperFormat INSTANCE = new FloatWrapperFormat();

	/** {@link GenericArrayFormat} for arrays of type {@link Float}. */
	public static final GenericArrayFormat<Float[]> ARRAY_FORMAT =
		new GenericArrayFormat<>(Float[].class, INSTANCE);

	private FloatWrapperFormat() {
		super(Float.class);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getFloat(propertyName, propertyValue);
	}
}