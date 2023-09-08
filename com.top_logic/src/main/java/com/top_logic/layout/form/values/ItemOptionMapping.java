/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * {@link OptionMapping} for {@link ConfigurationItem}-valued properties that selects from
 * configuration types (not configuration instances).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ItemOptionMapping implements OptionMapping {

	/** Singleton {@link ItemOptionMapping} instance. */
	public static final ItemOptionMapping INSTANCE = new ItemOptionMapping();

	private ItemOptionMapping() {
		// singleton instance
	}

	@Override
	public Object toSelection(Object option) {
		if (option == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Class<? extends ConfigurationItem> configType =
			(Class<? extends ConfigurationItem>) option;
		return TypedConfiguration.newConfigItem(configType);
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		if (selection == null) {
			return null;
		}
		return ((ConfigurationItem) selection).getConfigurationInterface();
	}
}
