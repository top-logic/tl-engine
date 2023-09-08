/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

/**
 * Delegate for a setter method.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class AbstractProperty<T> {

	private final String _configName;

	protected AbstractProperty(String configName) {
		_configName = configName;
	}

	public String getConfigName() {
		return _configName;
	}

	/**
	 * Whether this property is extracted from a configuration for applying settings to another
	 * configuration. If so, then the property must not be copied, due to the previously applied
	 * settings to the other configuration shall be used.
	 * 
	 * @param self
	 *        The configuration.
	 */
	public boolean isCopyableProperty(T self) {
		return true;
	}

	public abstract Object get(T self);

	public void copy(T self, Object value) {
		set(self, value);
	}

	public abstract void set(T self, Object value);

}
