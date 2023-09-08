/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.values;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * A {@link PolymorphicConfiguration} that serves as example value for
 * {@link PolymorphicConfiguration}-valued properties.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class DemoConfiguredInstance implements
		ConfiguredInstance<PolymorphicConfiguration<? extends DemoConfiguredInstance>> {

	private final PolymorphicConfiguration<? extends DemoConfiguredInstance> _config;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link DemoConfiguredInstance}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public DemoConfiguredInstance(InstantiationContext context,
			PolymorphicConfiguration<? extends DemoConfiguredInstance> config) {
		_config = config;
	}

	@Override
	public PolymorphicConfiguration<? extends DemoConfiguredInstance> getConfig() {
		return _config;
	}

}
