/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Algorithm for creating and updating listeners on a property some computation depends on.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class PropertyDependency extends Updater {

	final PropertyDescriptor _property;

	final List<PropertyDependency> _valueDependencies;

	/**
	 * Creates a {@link PropertyDependency}.
	 * 
	 * @param property
	 *        The property some computation depends on.
	 * @param valueDependencies
	 *        Further dependencies on the (item) value of the given property.
	 */
	public PropertyDependency(PropertyDescriptor property, List<PropertyDependency> valueDependencies) {
		assert property != null : "Undefined property.";
		_property = property;
		_valueDependencies = valueDependencies;
	}

	/**
	 * Implemented directly for storage optimization reasons only.
	 * 
	 * @deprecated Must not be called directly, use {@link #createUpdater()}.
	 */
	@Deprecated
	@Override
	public void install(ConfigurationItem item, ConfigurationListener updateListener) {
		item.addConfigurationListener(_property, updateListener);
	}

	/**
	 * Implemented directly for storage optimization reasons only.
	 * 
	 * @deprecated Must not be called directly, use {@link #createUpdater()}.
	 */
	@Deprecated
	@Override
	public void uninstall(ConfigurationItem item, ConfigurationListener updateListener) {
		item.removeConfigurationListener(_property, updateListener);
	}

	/**
	 * Creates the {@link Updater} that handles listener management.
	 */
	public final Updater createUpdater() {
		if (hasValueDependencies()) {
			return new ValueUpdater(_property, createValueUpdaters());
		} else {
			return this;
		}
	}

	private boolean hasValueDependencies() {
		return !_valueDependencies.isEmpty();
	}

	private List<Updater> createValueUpdaters() {
		List<Updater> result = new ArrayList<>(_valueDependencies.size());
		for (PropertyDependency dependency : _valueDependencies) {
			result.add(dependency.createUpdater());
		}
		return result;
	}

}
