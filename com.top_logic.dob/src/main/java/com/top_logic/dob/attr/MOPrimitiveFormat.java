/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import java.util.stream.Collectors;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for {@link MOPrimitive}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MOPrimitiveFormat extends AbstractConfigurationValueProvider<MOPrimitive> {

	/**
	 * Singleton {@link MOPrimitiveFormat} instance.
	 */
	public static final MOPrimitiveFormat INSTANCE = new MOPrimitiveFormat();

	private MOPrimitiveFormat() {
		super(MOPrimitive.class);
	}

	@Override
	public MOPrimitive getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		MOPrimitive result = MOPrimitive.getPrimitive(propertyValue.toString());
		if (result == null) {
			throw new ConfigurationException("Unknown type '" + propertyValue + "' in configuration property '"
				+ propertyName + "'. Available types are: " + MOPrimitive.getAllPrimitives().stream()
					.map(t -> t.getName()).sorted().collect(Collectors.joining(", ")));
		}
		return result;
	}

	@Override
	public String getSpecificationNonNull(MOPrimitive configValue) {
		return configValue.getName();
	}

}
