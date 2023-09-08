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
 * {@link ConfigurationValueProvider} for properties of type {@link Long}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class LongWrapperFormat extends PrimitiveValueProvider {

	/**
	 * Singleton {@link LongWrapperFormat} instance.
	 */
	public static final LongWrapperFormat INSTANCE = new LongWrapperFormat();

	/** {@link GenericArrayFormat} for arrays of type {@link Long}. */
	public static final GenericArrayFormat<Long[]> ARRAY_FORMAT = new GenericArrayFormat<>(Long[].class, INSTANCE);

	private LongWrapperFormat() {
		super(Long.class);
	}

	@Override
	public Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return ConfigUtil.getLong(propertyName, propertyValue);
	}
}