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
 * {@link ConfigurationValueProvider} for non-nullable properties of type <code>int</code>.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class PrimitiveIntFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link PrimitiveIntFormat} instance.
	 */
	public static final PrimitiveIntFormat INSTANCE = new PrimitiveIntFormat();

	/** {@link GenericArrayFormat} for arrays of type "int". */
	public static final GenericArrayFormat<int[]> ARRAY_FORMAT = new GenericArrayFormat<>(int[].class, INSTANCE);

	private PrimitiveIntFormat() {
		super(int.class);
	}

	@Override
	protected Object getValueEmpty(String propertyName) throws ConfigurationException {
		throw new ConfigurationException(I18NConstants.ERROR_PROPERY_CANNOT_BE_NULL__NAME.fill(propertyName),
			propertyName, null);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getInteger(propertyName, propertyValue);
	}

	@Override
	public Object defaultValue() {
		return 0;
	}
}