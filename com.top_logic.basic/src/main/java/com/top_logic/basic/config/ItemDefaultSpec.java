/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;

/**
 * {@link DefaultSpec} that produces an instance of a {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ItemDefaultSpec extends DefaultSpec {

	private final Class<?> _configurationInterface;

	public ItemDefaultSpec(Class<?> configurationInterface) {
		if (configurationInterface == null) {
			throw new NullPointerException("'configurationInterface' must not be 'null'.");
		}
		_configurationInterface = configurationInterface;
	}

	@Override
	public Object getDefaultValue(PropertyDescriptor property) throws ConfigurationException {
		Class<?> configInterface;
		Class<?> propertyType = property.getType();
		if (_configurationInterface == Void.class) {
			configInterface = propertyType;
		} else {
			configInterface = _configurationInterface;
		}
		ConfigurationItem configItem;
		if (PolymorphicConfiguration.class.isAssignableFrom(configInterface)) {
			configItem = newPolymorphicConfig((Class) configInterface, property);
		} else if (ConfigurationItem.class.isAssignableFrom(configInterface)) {
			configItem = newSimpleConfig((Class) configInterface);
		} else {
			configItem = TypedConfiguration.createConfigItemForImplementationClass(configInterface);
		}
		return configItem;
	}

	private PolymorphicConfiguration<?> newPolymorphicConfig(
			Class<? extends PolymorphicConfiguration<?>> configInterface,
			PropertyDescriptor property) throws ConfigurationException {
		ConfigurationDescriptor expectedDescriptor = TypedConfiguration.getConfigurationDescriptor(configInterface);
		PropertyDescriptor implClassProperty = expectedDescriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		Class<?> implementationClass = (Class<?>) implClassProperty.getDefaultValue();
		ImplementationClassDefault annotation = property.getAnnotation(ImplementationClassDefault.class);
		if (annotation != null) {
			implementationClass = annotation.value();
		}
		return TypedConfiguration.createConfigItemForImplementationClass(implementationClass, expectedDescriptor);
	}

	private ConfigurationItem newSimpleConfig(Class<? extends ConfigurationItem> configInterface) {
		return TypedConfiguration.newConfigItem(configInterface);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _configurationInterface.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemDefaultSpec other = (ItemDefaultSpec) obj;
		if (!_configurationInterface.equals(other._configurationInterface))
			return false;
		return true;
	}

}
