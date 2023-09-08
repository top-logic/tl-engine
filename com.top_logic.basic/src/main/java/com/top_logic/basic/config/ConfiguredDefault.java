/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;

/**
 * {@link DefaultValueProvider} that takes the default from the application
 * configuration.
 * 
 * <p>
 * A default of property <code>p</code> of configuration interface
 * <code>T</code> is configured in entry <code>p</code> of section
 * <code>T</code>.
 * </p>
 * 
 * @see ComplexDefault Annotating this provider to a configuration property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredDefault extends DefaultValueProvider {
	
	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		return getConfiguredDefaultValue(descriptor.getProperty(propertyName));
	}

	private Object getConfiguredDefaultValue(PropertyDescriptor property) {
		ConfigurationDescriptor descriptor = property.getDescriptor();
		String propertyName = property.getPropertyName();
		Class<?> propertyType = property.getType();
		IterableConfiguration configuration = Configuration.getConfiguration(descriptor.getConfigurationInterface());
		String value = configuration.getValue(propertyName);
		if (StringServices.isEmpty(value)) {
			PropertyDescriptor[] superProperties = property.getSuperProperties();
			if (superProperties.length == 1) {
				return getConfiguredDefaultValue(superProperties[0]);
			} else {
				return getJavaDefault(propertyType);
			}
		} else {
			try {
				if (propertyType == boolean.class) {
					return ConfigUtil.getBooleanValue(propertyName, value);
				} else if (propertyType == String.class) {
					return ConfigUtil.getString(propertyName, value);
				} else if (propertyType == int.class) {
					return ConfigUtil.getIntValue(propertyName, value);
				} else if (propertyType == long.class) {
					return ConfigUtil.getLongValue(propertyName, value);
				} else if (propertyType == char.class) {
					return ConfigUtil.getCharValue(propertyName, value);
				} else if (propertyType == float.class) {
					return ConfigUtil.getFloatValue(propertyName, value);
				} else if (propertyType == double.class) {
					return ConfigUtil.getDoubleValue(propertyName, value);
				} else if (Enum.class.isAssignableFrom(propertyType)) {
					@SuppressWarnings("unchecked")
					Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) propertyType;
					return ConfigUtil.getEnum(enumType, value);
				} else {
					throw new UnsupportedOperationException();
				}
			} catch (ConfigurationException ex) {
				Logger.error("Invalid configured configuration default.", ex, ConfiguredDefault.class);
				return getJavaDefault(propertyType);
			}
		}
	}

	private Object getJavaDefault(Class<?> propertyType) {
		if (propertyType.isPrimitive()) {
			return PropertyDescriptorImpl.getPrimitiveDefault(propertyType);
		} else {
			return null;
		}
	}
	
}
