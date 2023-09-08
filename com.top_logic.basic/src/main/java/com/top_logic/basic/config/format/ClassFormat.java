/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} that resolves {@link Class} object by
 * qualified name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClassFormat extends AbstractConfigurationValueProvider<Class<?>> {
	
	/**
	 * Singleton {@link ClassFormat} instance.
	 */
	public static final ClassFormat INSTANCE = new ClassFormat();

	/** {@link GenericArrayFormat} for arrays of type {@link Class}. */
	/* Can not deliver Class<?>[] as type to GenericArrayFormat */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final GenericArrayFormat<Class<?>[]> ARRAY_FORMAT = new GenericArrayFormat(Class[].class, INSTANCE);

	/**
	 * Creates a new {@link ClassFormat}.
	 */
	protected ClassFormat() {
		super(Class.class);
	}

	@Override
	protected String getSpecificationNonNull(Class<?> configValue) {
		return configValue.getName();
	}

	@Override
	protected Class<?> getValueNonEmpty(String propertyName, CharSequence className) throws ConfigurationException {
		return ConfigUtil.getClassForName(Object.class, propertyName, className, null);
	}
}