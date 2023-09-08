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
 * {@link ConfigurationValueProvider} for properties of type {@link Integer}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IntegerWrapperFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link IntegerWrapperFormat} instance.
	 */
	public static final IntegerWrapperFormat INSTANCE = new IntegerWrapperFormat();

	/** {@link GenericArrayFormat} for arrays of type {@link Integer}. */
	public static final GenericArrayFormat<Integer[]> ARRAY_FORMAT =
		new GenericArrayFormat<>(Integer[].class, INSTANCE);

	private IntegerWrapperFormat() {
		super(Integer.class);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getInteger(propertyName, propertyValue);
	}
}