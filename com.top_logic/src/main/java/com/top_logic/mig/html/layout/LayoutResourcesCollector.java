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
 * Collector of all {@link LayoutReference} resources inside a {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutResourcesCollector extends DescendingConfigurationItemVisitor {

	private Collection<String> _resources = new HashSet<>();

	/**
	 * Collects all layout resources used in the given {@link ConfigurationItem}.
	 * 
	 * @param configuration
	 *        To be searched {@link ConfigurationItem}.
	 * 
	 * @return Collection of all found {@link LayoutReference} resources.
	 */
	public Collection<String> collectAll(ConfigurationItem configuration) {
		handleProperties(configuration);

		return _resources;
	}

	@Override
	protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
		// Nothing to do.
	}

	@Override
	protected void handleListProperty(ConfigurationItem config, PropertyDescriptor property,
			List<? extends ConfigurationItem> listValue) {
		handleConfigurations(listValue);
	}

	private void handleConfigurations(Collection<? extends ConfigurationItem> listValue) {
		for (ConfigurationItem entry : listValue) {
			handleConfiguration(entry);
		}
	}

	private void handleConfiguration(ConfigurationItem entry) {
		if (entry instanceof LayoutReference) {
			_resources.add(((LayoutReference) entry).getResource());
		}

		handleProperties(entry);
	}

	@Override
	protected void handleMapProperty(ConfigurationItem config, PropertyDescriptor property,
			Map<Object, ConfigurationItem> mapValue) {
		handleConfigurations(mapValue.values());
	}

	@Override
	protected void handleItemProperty(ConfigurationItem config, PropertyDescriptor property,
			ConfigurationItem itemValue) {
		handleConfiguration(itemValue);
	}
}
