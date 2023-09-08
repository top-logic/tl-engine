/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.DescendingConfigurationItemVisitor;

/**
 * Removes the given layout resources in the given {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutResourceRemover extends DescendingConfigurationItemVisitor {

	private Collection<String> _resources = new HashSet<>();

	/**
	 * Removes all {@link LayoutReference} in the given {@link ConfigurationItem} defined by the
	 * given resources.
	 * 
	 * @param configuration
	 *        To be searched {@link ConfigurationItem}.
	 */
	public void remove(ConfigurationItem configuration, Collection<String> resources) {
		_resources = resources;

		handleProperties(configuration);
	}

	@Override
	protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
		// Nothing to do.
	}

	@Override
	protected void handleListProperty(ConfigurationItem config, PropertyDescriptor property,
			List<? extends ConfigurationItem> listValue) {
		listValue.removeIf(item -> itemShouldBeRemoved(item));
		if (listValue.isEmpty()) {
			config.reset(property);
		}

		super.handleListProperty(config, property, listValue);
	}

	@Override
	protected void handleMapProperty(ConfigurationItem config, PropertyDescriptor property,
			Map<Object, ConfigurationItem> mapValue) {
		mapValue.values().removeIf(item -> itemShouldBeRemoved(item));
		if (mapValue.isEmpty()) {
			config.reset(property);
		}

		super.handleMapProperty(config, property, mapValue);
	}

	@Override
	protected void handleItemProperty(ConfigurationItem config, PropertyDescriptor property,
			ConfigurationItem itemValue) {
		if (itemShouldBeRemoved(itemValue)) {
			config.reset(property);

			return;
		}

		handleProperties(itemValue);
	}

	private boolean itemShouldBeRemoved(ConfigurationItem itemValue) {
		if (itemValue instanceof LayoutReference) {
			return _resources.contains(((LayoutReference) itemValue).getResource());
		}

		return false;
	}

}
