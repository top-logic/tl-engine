/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.DescendingConfigurationItemVisitor;
import com.top_logic.layout.editor.annotation.AvailableTemplates;

/**
 * Computes all typed template group names where this template is usable in.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllAvailableTemplateGroupNames extends DescendingConfigurationItemVisitor {

	Collection<String> _availableTemplateGroups;

	/**
	 * Creates an {@link AllAvailableTemplateGroupNames}.
	 */
	public AllAvailableTemplateGroupNames() {
		_availableTemplateGroups = new HashSet<>();
	}
	
	/**
	 * Finds all {@link AvailableTemplates} annotations and extract the typed template group
	 * names.
	 * 
	 * @param config
	 *        {@link ConfigurationItem} which is inspected.
	 */
	public Collection<String> getGroupNames(ConfigurationItem config) {
		handleProperties(config);

		return _availableTemplateGroups;
	}

	@Override
	protected void handleMapProperty(ConfigurationItem config, PropertyDescriptor property,
			Map<Object, ConfigurationItem> mapValue) {
		for (ConfigurationItem configItem : mapValue.values()) {
			if (configItem instanceof AvailableTemplates) {
				String[] templateGroups = ((AvailableTemplates) configItem).value();
				_availableTemplateGroups.addAll(Arrays.asList(templateGroups));
			} else {
				handleProperties(configItem);
			}
		}
	}

	@Override
	protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
		// Nothing to do.
	}

}
