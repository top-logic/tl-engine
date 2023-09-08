/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.ResourceProvider;

/**
 * Configurable {@link ProxyResourceProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredProxyResourceProvider<C extends ConfiguredProxyResourceProvider.Config<?>>
		extends ProxyResourceProvider implements ConfiguredInstance<C> {

	/** The {@link PolymorphicConfiguration} of the {@link ProxyResourceProvider}. */
	public interface Config<I extends ConfiguredProxyResourceProvider<?>> extends PolymorphicConfiguration<I> {

		/** Property name of {@link #getInner()}. */
		String INNER = "inner";

		/**
		 * The inner {@link ResourceProvider} to use.
		 *
		 * @see #getProviderImpl(Object) Must be overridden, if no value is given in the
		 *      configuration.
		 */
		@Name(INNER)
		@InstanceDefault(MetaResourceProvider.class)
		@InstanceFormat
		ResourceProvider getInner();

		/**
		 * Setter for {@link #getInner()}.
		 */
		void setInner(ResourceProvider inner);

	}

	private final C _config;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ProxyResourceProvider}.
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
	public ConfiguredProxyResourceProvider(InstantiationContext context, C config) {
		super(config.getInner());
		_config = config;

	}

	@Override
	public C getConfig() {
		return _config;
	}

}

