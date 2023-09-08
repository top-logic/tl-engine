/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.PropertyValue;

/**
 * A {@link ModifiableValue} based on a property of a {@link ConfigurationItem}.
 * 
 * Use {@link Values#configurationValue(ConfigurationItem, PropertyDescriptor)} to create an
 * instance.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ConfigurationValue<T> extends AbstractModifiableValue<T> {

	private final PropertyValue _propertyValue;

	ConfigurationValue(PropertyValue propertyValue) {
		_propertyValue = propertyValue;
	}

	private ConfigurationItem getModel() {
		return _propertyValue.getItem();
	}

	private PropertyDescriptor getProperty() {
		return _propertyValue.getProperty();
	}

	@Override
	public void set(T newValue) {
		if (newValue == null && !getProperty().isNullable()) {
			_propertyValue.reset();
		} else {
			_propertyValue.set(newValue);
		}
	}

	@Override
	public T get() {
		if (getProperty().isMandatory() && !_propertyValue.isSet()) {
			return null;
		}
		@SuppressWarnings("unchecked")
		T result = (T) _propertyValue.get();
		return result;
	}

	@Override
	public ListenerBinding addListener(Listener listener) {
		ConfigurationListenerAdapter adapter = new ConfigurationListenerAdapter(this, listener);
		ConfigurationItem model = getModel();
		PropertyDescriptor property = getProperty();
		model.addConfigurationListener(property, adapter);
		return () -> model.removeConfigurationListener(property, adapter);
	}

}
