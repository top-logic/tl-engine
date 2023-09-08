/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import static com.top_logic.basic.col.factory.CollectionFactory.*;

import java.util.List;

import com.top_logic.basic.config.annotation.defaults.ListDefault;

/**
 * {@link DefaultSpec} for {@link ListDefault} annotation.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ListDefaultSpec extends DefaultSpec {

	private final List<Class<?>> _defaultEntries;

	public ListDefaultSpec(Class<?>[] defaultEntries) {
		_defaultEntries = list(defaultEntries);
	}

	@Override
	public Object getDefaultValue(PropertyDescriptor property) throws ConfigurationException {
		if (property.kind() != PropertyKind.LIST) {
			throw failNotListProperty(property);
		}
		List<Object> result = list();
		for (Class<?> defaultEntry : _defaultEntries) {
			result.add(getEntry(property, defaultEntry));
		}
		return result;
	}

	private ConfigurationException failNotListProperty(PropertyDescriptor property) throws ConfigurationException {
		StringBuilder msg = new StringBuilder();
		msg.append("Illegal annotation '");
		msg.append(ListDefault.class.getName());
		msg.append("' on property '");
		msg.append(property.getPropertyName());
		msg.append("' of '");
		msg.append(property.getDescriptor().getConfigurationInterface());
		msg.append("'. Only properties containing list of configurations are allowed for that annotation.");
		throw new ConfigurationException(msg.toString());
	}

	private Object getEntry(PropertyDescriptor property, Class<?> defaultEntry)
			throws ConfigurationException {
		if (!ConfigurationItem.class.isAssignableFrom(defaultEntry)) {
			/* Shortcut to configure PolymorphicConfiguration<X> it is possible to set X. */
			ConfigurationItem defaultConfig = newPolymorphicConfigForImplementationClass(defaultEntry);
			return property.getConfigurationAccess().createValue(getInstantiationContext(), defaultConfig);
		}
		@SuppressWarnings("unchecked")
		Class<? extends ConfigurationItem> configItemClass = (Class<? extends ConfigurationItem>) defaultEntry;
		return newSimpleConfig(configItemClass);
	}

	private ConfigurationItem newPolymorphicConfigForImplementationClass(Class<?> implementationClass)
			throws ConfigurationException {
		return TypedConfiguration.createConfigItemForImplementationClass(implementationClass);
	}

	private InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	private ConfigurationItem newSimpleConfig(Class<? extends ConfigurationItem> configInterface) {
		return TypedConfiguration.newConfigItem(configInterface);
	}

}
