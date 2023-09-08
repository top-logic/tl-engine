/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * An unmodifiable view to a {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ConfigItemView extends ReflectiveConfigItem {

	private final ConfigurationItem _impl;

	public ConfigItemView(ConfigurationItem impl) {
		this._impl = impl;
	}

	@Override
	public boolean valueSet(PropertyDescriptor property) {
		return _impl.valueSet(property);
	}

	@Override
	public Location location() {
		return _impl.location();
	}

	@Override
	public Object update(PropertyDescriptor property, Object value) {
		throw new UnsupportedOperationException("Immutable value cannot be updated.");
	}

	@Override
	public final void reset(PropertyDescriptor property) {
		throw new UnsupportedOperationException("Immutable value cannot be reset.");
	}

	@Override
	public ConfigurationDescriptor descriptor() {
		return _impl.descriptor();
	}

	@Override
	public Object value(PropertyDescriptor property) {
		return _impl.value(property);
	}

	@Override
	ConfigurationItem container() {
		return ((ConfigPart) _impl).container();
	}

	@Override
	void updateContainer(ConfigurationItem container) {
		throw new UnsupportedOperationException("Immutable value cannot be updated.");
	}

	@Override
	public boolean addConfigurationListener(PropertyDescriptor property, ConfigurationListener listener) {
		// Listeners on immutable configurations are never called, and therefore need not to be
		// stored.
		return false;
	}

	@Override
	public boolean removeConfigurationListener(PropertyDescriptor property, ConfigurationListener listener) {
		// Listeners on immutable configurations are never called, and therefore need not to be
		// stored.
		return false;
	}

	@Override
	public void check(Log protocol) {
		_impl.check(protocol);
	}

}
