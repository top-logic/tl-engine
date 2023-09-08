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
 * {@link ConfigurationValueProvider} for non-nullable properties of type <code>float</code>.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class PrimitiveFloatFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link PrimitiveFloatFormat} instance.
	 */
	public static final PrimitiveFloatFormat INSTANCE = new PrimitiveFloatFormat();

	/** {@link GenericArrayFormat} for arrays of type "float". */
	public static final GenericArrayFormat<float[]> ARRAY_FORMAT = new GenericArrayFormat<>(float[].class, INSTANCE);

	private PrimitiveFloatFormat() {
		super(float.class);
	}

	@Override
	protected Object getValueEmpty(String propertyName) throws ConfigurationException {
		throw new ConfigurationException(I18NConstants.ERROR_PROPERY_CANNOT_BE_NULL__NAME.fill(propertyName),
			propertyName, null);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getFloat(propertyName, propertyValue);
	}

	@Override
	public Object defaultValue() {
		return 0.0f;
	}
}