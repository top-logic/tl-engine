/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.internal.ItemFactory;

/**
 * {@link ItemFactory} that allocates generic {@link ConfigurationItem} implemented by reflection.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public final class GenericConfigFactory extends ItemFactory {

	private final AbstractConfigurationDescriptor _descriptor;

	/**
	 * Creates a {@link GenericConfigFactory}.
	 */
	public GenericConfigFactory(ConfigurationDescriptor descriptor) {
		_descriptor = (AbstractConfigurationDescriptor) descriptor;
	}

	@Override
	public ConfigurationItem createCopy(ConfigBuilder other) {
		/* Create a Map with the optimal size. As many ConfigItems are never modified after
		 * construction, they will never grow and the initial size is the final size. */
		Map<NamedConstant, Object> values = new HashMap<>(((ConfigBuilderImpl) other).values());
		MutableConfigItem invocationHandler = new MutableConfigItem(_descriptor, values, other.location());
		ConfigurationItem result = _descriptor.newInstance(invocationHandler);
		invocationHandler.initValues();
		_descriptor.initInstance(invocationHandler);
		return result;
	}

	@Override
	public ConfigurationItem createNew(Location location) {
		/* Most ConfigItems are small. Therefore, use a small initial size. Requesting size 4 will
		 * result in a Map of size 4 that will be rehashed to size 8 when the 4. element is inserted. */
		Map<NamedConstant, Object> values = new HashMap<>(4);
		MutableConfigItem invocationHandler = new MutableConfigItem(_descriptor, values, location);
		ConfigurationItem result = _descriptor.newInstance(invocationHandler);
		invocationHandler.initValues();
		_descriptor.initInstance(invocationHandler);
		return result;
	}
}