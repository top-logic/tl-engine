/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configEdit;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.Accessor;

/**
 * Generic {@link Accessor} for values of {@link ConfigurationItem}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurationPropertyAccessor implements Accessor<ConfigurationItem> {

	/**
	 * Singleton {@link ConfigurationPropertyAccessor} instance.
	 */
	public static final ConfigurationPropertyAccessor INSTANCE = new ConfigurationPropertyAccessor();

	private ConfigurationPropertyAccessor() {
		// Singleton constructor.
	}

	@Override
	public Object getValue(ConfigurationItem object, String name) {
		PropertyDescriptor property = object.descriptor().getProperty(name);
		if (property == null) {
			return null;
		}
		return object.value(property);
	}

	@Override
	public void setValue(ConfigurationItem object, String name, Object value) {
		PropertyDescriptor property = object.descriptor().getProperty(name);
		object.update(property, value);
	}

}
