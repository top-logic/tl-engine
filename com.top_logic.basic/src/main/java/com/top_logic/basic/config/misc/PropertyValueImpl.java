/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.misc;

import static com.top_logic.basic.util.Utils.*;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * The normal implementation of {@link PropertyValue}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class PropertyValueImpl implements PropertyValue {

	private final ConfigurationItem _item;

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link PropertyValueImpl}.
	 * 
	 * @param item
	 *        Is not allowed to be null.
	 * @param property
	 *        Is not allowed to be null.
	 */
	public PropertyValueImpl(ConfigurationItem item, PropertyDescriptor property) {
		_item = requireNonNull(item);
		PropertyDescriptor dynamicProperty = resolveDynamicProperty(property, item);
		checkProperty(property, dynamicProperty, item);
		_property = dynamicProperty;
	}

	private static void checkProperty(PropertyDescriptor staticProperty, PropertyDescriptor dynamicProperty,
			ConfigurationItem item) {
		if ((dynamicProperty == null) || (dynamicProperty.identifier() != staticProperty.identifier())) {
			throw new IllegalArgumentException("Property '" + staticProperty + "' does not belong to this item: " + item);
		}
	}

	private static PropertyDescriptor resolveDynamicProperty(PropertyDescriptor staticProperty,
			ConfigurationItem item) {
		return item.descriptor().getProperty(staticProperty.getPropertyName());
	}

	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	@Override
	public ConfigurationItem getItem() {
		return _item;
	}

	@Override
	public Object get() {
		return getItem().value(getProperty());
	}

	@Override
	public boolean isSet() {
		return getItem().valueSet(getProperty());
	}

	@Override
	public Object set(Object value) {
		return getItem().update(getProperty(), value);
	}

	@Override
	public void reset() {
		getItem().reset(getProperty());
	}

	@Override
	public boolean addListener(ConfigurationListener listener) {
		return getItem().addConfigurationListener(getProperty(), requireNonNull(listener));
	}

	@Override
	public boolean removeListener(ConfigurationListener listener) {
		return getItem().removeConfigurationListener(getProperty(), requireNonNull(listener));
	}

}
