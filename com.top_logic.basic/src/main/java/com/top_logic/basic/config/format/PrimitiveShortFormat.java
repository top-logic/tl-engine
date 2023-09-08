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
 * {@link ConfigurationValueProvider} for non-nullable properties of type <code>short</code>.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class PrimitiveShortFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link PrimitiveShortFormat} instance.
	 */
	public static final PrimitiveShortFormat INSTANCE = new PrimitiveShortFormat();

	/** {@link GenericArrayFormat} for arrays of type "byte". */
	public static final GenericArrayFormat<short[]> ARRAY_FORMAT = new GenericArrayFormat<>(short[].class, INSTANCE);

	private PrimitiveShortFormat() {
		super(short.class);
	}

	@Override
	protected Object getValueEmpty(String propertyName) throws ConfigurationException {
		throw new ConfigurationException(I18NConstants.ERROR_PROPERY_CANNOT_BE_NULL__NAME.fill(propertyName),
			propertyName, null);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getShort(propertyName, propertyValue);
	}

	@Override
	public Object defaultValue() {
		return ((short) 0);
	}
}