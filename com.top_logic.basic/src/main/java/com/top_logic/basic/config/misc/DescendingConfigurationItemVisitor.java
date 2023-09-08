/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.misc;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;

/**
 * Class providing helper methods to process each {@link ConfigurationItem} in a configuration
 * "tree".
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DescendingConfigurationItemVisitor {

	/**
	 * Service method to switch over {@link ConfigurationDescriptor#getProperties() properties} of
	 * the given config and calls the corresponding specialised method.
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} to handle properties may be <code>null</code>.
	 * 
	 * @see #handleArrayProperty(ConfigurationItem, PropertyDescriptor, Object[])
	 * @see #handleListProperty(ConfigurationItem, PropertyDescriptor, List)
	 * @see #handleMapProperty(ConfigurationItem, PropertyDescriptor, Map)
	 * @see #handleItemProperty(ConfigurationItem, PropertyDescriptor, ConfigurationItem)
	 * @see #handlePlainProperty(ConfigurationItem, PropertyDescriptor)
	 * @see #handleOtherProperty(ConfigurationItem, PropertyDescriptor)
	 */
	protected void handleProperties(ConfigurationItem config) {
		if (config == null) {
			return;
		}
		config.descriptor().getProperties().forEach(property -> {
			switch (property.kind()) {
				case LIST:
					if (!property.isInstanceValued()) {
						@SuppressWarnings("unchecked")
						List<ConfigurationItem> typedValue = (List<ConfigurationItem>) config.value(property);
						handleListProperty(config, property, typedValue);
					}
					break;
				case ARRAY:
					if (!property.isInstanceValued()) {
						Object[] typedValue = (Object[]) config.value(property);
						handleArrayProperty(config, property, typedValue);
					}
					break;
				case ITEM:
					if (!property.isInstanceValued()) {
						ConfigurationItem typedValue = (ConfigurationItem) config.value(property);
						handleItemProperty(config, property, typedValue);
					}
					break;
				case MAP:
					if (!property.isInstanceValued()) {
						@SuppressWarnings("unchecked")
						Map<Object, ConfigurationItem> typedValue =
							(Map<Object, ConfigurationItem>) config.value(property);
						handleMapProperty(config, property, typedValue);
					}
					break;
				case COMPLEX:
				case PLAIN:
					handlePlainProperty(config, property);
					break;
				default:
					handleOtherProperty(config, property);
					break;
			}
		});
	}

	/**
	 * Handles the given property of the given configuration.
	 * 
	 * <p>
	 * The property is of other type than {@link PropertyKind#PLAIN},
	 * {@link PropertyKind#ARRAY},{@link PropertyKind#LIST},{@link PropertyKind#MAP}, and
	 * {@link PropertyKind#ITEM}.
	 * </p>
	 * 
	 * <p>
	 * This implementation makes nothing.
	 * </p>
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} with the given property.
	 * @param property
	 *        The {@link PropertyDescriptor} to handle.
	 * 
	 * @see #handleProperties(ConfigurationItem)
	 */
	protected void handleOtherProperty(ConfigurationItem config, PropertyDescriptor property) {
		// Nothing to do in general.
	}

	/**
	 * Handles the given property of the given configuration.
	 * 
	 * <p>
	 * The property is of type {@link PropertyKind#LIST}.
	 * </p>
	 * 
	 * <p>
	 * Calls {@link #handleProperties(ConfigurationItem)} for each value.
	 * </p>
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} with the given property.
	 * @param property
	 *        The {@link PropertyDescriptor} to handle.
	 * @param listValue
	 *        The value of the given property in the given configuration.
	 * 
	 * @see #handleProperties(ConfigurationItem)
	 */
	protected void handleListProperty(ConfigurationItem config, PropertyDescriptor property,
			List<? extends ConfigurationItem> listValue) {
		listValue.forEach(this::handleProperties);
	}

	/**
	 * Handles the given property of the given configuration.
	 * 
	 * <p>
	 * The property is of type {@link PropertyKind#ARRAY}.
	 * </p>
	 * 
	 * <p>
	 * Calls {@link #handleProperties(ConfigurationItem)} for each value.
	 * </p>
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} with the given property.
	 * @param property
	 *        The {@link PropertyDescriptor} to handle.
	 * @param arrayValue
	 *        The value of the given property in the given configuration. For technical reasons,
	 *        this is an {@link Object} array. Although it contains {@link ConfigurationItem}s.
	 * 
	 * @see #handleProperties(ConfigurationItem)
	 */
	protected void handleArrayProperty(ConfigurationItem config, PropertyDescriptor property, Object[] arrayValue) {
		for (Object singleValue : arrayValue) {
			handleProperties((ConfigurationItem) singleValue);
		}
	}

	/**
	 * Handles the given property of the given configuration.
	 * 
	 * <p>
	 * The property is of type {@link PropertyKind#ITEM}.
	 * </p>
	 * 
	 * <p>
	 * Calls {@link #handleProperties(ConfigurationItem)} with the given item.
	 * </p>
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} with the given property.
	 * @param property
	 *        The {@link PropertyDescriptor} to handle.
	 * @param itemValue
	 *        The value of the given property in the given configuration.
	 * 
	 * @see #handleProperties(ConfigurationItem)
	 */
	protected void handleItemProperty(ConfigurationItem config, PropertyDescriptor property, ConfigurationItem itemValue) {
		handleProperties(itemValue);
	}

	/**
	 * Handles the given property of the given configuration.
	 * 
	 * <p>
	 * The property is of type {@link PropertyKind#MAP}.
	 * </p>
	 * 
	 * <p>
	 * Calls {@link #handleProperties(ConfigurationItem)} for each value.
	 * </p>
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} with the given property.
	 * @param property
	 *        The {@link PropertyDescriptor} to handle.
	 * @param mapValue
	 *        The value of the given property in the given configuration.
	 * 
	 * @see #handleProperties(ConfigurationItem)
	 */
	protected void handleMapProperty(ConfigurationItem config, PropertyDescriptor property,
			Map<Object, ConfigurationItem> mapValue) {
		mapValue.values().forEach(this::handleProperties);
	}

	/**
	 * Handles the given property of the given configuration.
	 * 
	 * <p>
	 * The property is of type {@link PropertyKind#PLAIN}.
	 * </p>
	 * 
	 * @param config
	 *        The {@link ConfigurationItem} with the given property.
	 * @param property
	 *        The {@link PropertyDescriptor} to handle.
	 * 
	 * @see #handleProperties(ConfigurationItem)
	 */
	protected abstract void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property);

}

