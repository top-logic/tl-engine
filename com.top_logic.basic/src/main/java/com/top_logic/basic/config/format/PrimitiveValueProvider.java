/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} that parses primitive values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PrimitiveValueProvider extends AbstractConfigurationValueProvider<Object> {

	/**
	 * Creates a {@link PrimitiveValueProvider}.
	 *
	 * @param type
	 *        See {@link #getValueType()}
	 */
	public PrimitiveValueProvider(Class<?> type) {
		super(type);
	}

	@Override
	public String getSpecificationNonNull(Object configValue) {
		if (configValue == null) {
			return "";
		}
		
		return configValue.toString();
	}
	
	@Override
	public boolean isLegalValue(Object value) {
		if (value == null) {
			return !getValueType().isPrimitive();
		}
		return super.isLegalValue(value);
	}
	
}