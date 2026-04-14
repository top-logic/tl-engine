/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Objects;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationChange.Kind;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.model.AbstractFieldModel;

/**
 * A {@link AbstractFieldModel} that binds to a single {@link PropertyDescriptor} on a
 * {@link ConfigurationItem}.
 *
 * <p>
 * Reads and writes go through the configuration API ({@link ConfigurationItem#value(PropertyDescriptor)} /
 * {@link ConfigurationItem#update(PropertyDescriptor, Object)}). External changes to the
 * configuration are propagated to {@link com.top_logic.layout.form.model.FieldModelListener}s via
 * a {@link ConfigurationListener}.
 * </p>
 */
public class ConfigFieldModel extends AbstractFieldModel implements ConfigurationListener {

	private final ConfigurationItem _config;

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigFieldModel}.
	 *
	 * @param config
	 *        The configuration item to bind to.
	 * @param property
	 *        The property of the configuration item.
	 */
	public ConfigFieldModel(ConfigurationItem config, PropertyDescriptor property) {
		super(config.value(property));
		_config = config;
		_property = property;
		setMandatory(property.isMandatory());
		setNullable(property.isNullable());
		config.addConfigurationListener(property, this);
	}

	@Override
	public Object getValue() {
		return _config.value(_property);
	}

	@Override
	public void setValue(Object value) {
		Object oldValue = getValue();
		if (Objects.equals(oldValue, value)) {
			return;
		}
		_config.update(_property, value);
		// The ConfigurationListener callback (onChange) fires the FieldModelListener notification.
	}

	@Override
	public void onChange(ConfigurationChange change) {
		if (change.getKind() == Kind.SET) {
			fireValueChanged(change.getOldValue(), change.getNewValue());
		}
	}

	/**
	 * The property this model is bound to.
	 */
	public PropertyDescriptor getProperty() {
		return _property;
	}

	/**
	 * The configuration item this model is bound to.
	 */
	public ConfigurationItem getConfig() {
		return _config;
	}

	/**
	 * Detaches this model from the configuration by removing the listener.
	 */
	public void detach() {
		_config.removeConfigurationListener(_property, this);
	}
}
