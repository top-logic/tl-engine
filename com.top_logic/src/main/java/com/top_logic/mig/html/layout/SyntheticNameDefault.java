/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.model.provider.DefaultProvider;

/**
 * {@link DefaultProvider} to create a synthetic name for components.
 * 
 * @see LayoutConstants#isSyntheticName(ComponentName)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SyntheticNameDefault extends DefaultValueProvider {

	/** Singleton {@link SyntheticNameDefault} instance. */
	public static final SyntheticNameDefault INSTANCE = new SyntheticNameDefault();

	private final AtomicInteger _nextComponentId = new AtomicInteger(0);

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		return ComponentName.newName(MainLayout.UNIQUE_PREFIX, String.valueOf(_nextComponentId.incrementAndGet()));
	}

}

