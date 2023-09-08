/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative.mapping;

import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DeclarativeConfigDescriptorBuilder;
import com.top_logic.basic.config.DeclarativeConfigDescriptorBuilder.Config;
import com.top_logic.basic.config.InstantiationContext;

/**
 * A {@link Function} that returns a new {@link ConfigurationItem} for a configured
 * {@link ConfigurationDescriptor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeclarativeConfigItem extends DeclarativeConfigDescriptorBuilder<Config<?>>
		implements Function<Object, ConfigurationItem>, Supplier<ConfigurationItem> {

	/**
	 * Creates a new {@link DeclarativeConfigItem}.
	 */
	public DeclarativeConfigItem(InstantiationContext context, DeclarativeConfigDescriptorBuilder.Config<?> config) {
		super(context, config);
	}

	@Override
	public ConfigurationItem get() {
		return descriptor().factory().createNew();
	}

	@Override
	public ConfigurationItem apply(Object t) {
		return get();
	}

}

