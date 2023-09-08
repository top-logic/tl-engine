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
 * {@link ConfigurationValueProvider} for properties of type {@link Short}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ShortWrapperFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link ShortWrapperFormat} instance.
	 */
	public static final ShortWrapperFormat INSTANCE = new ShortWrapperFormat();

	/** {@link GenericArrayFormat} for arrays of type {@link Short}. */
	public static final GenericArrayFormat<Short[]> ARRAY_FORMAT = new GenericArrayFormat<>(Short[].class, INSTANCE);

	private ShortWrapperFormat() {
		super(Short.class);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getShort(propertyName, propertyValue);
	}
}