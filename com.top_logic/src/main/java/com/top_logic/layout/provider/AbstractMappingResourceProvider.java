/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.ResourceProvider;

/**
 * Dispatching {@link ResourceProvider} that transforms the target object before dispatch.
 * 
 * @see #mapValue(Object)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMappingResourceProvider<C extends AbstractMappingResourceProvider.Config<?>>
		extends AbstractMappingResourceProviderBase implements ConfiguredInstance<C> {

	private C _config;

	/**
	 * Configuration options for {@link AbstractMappingResourceProvider}.
	 */
	public interface Config<I extends AbstractMappingResourceProvider<?>> extends PolymorphicConfiguration<I> {

		/** Name of the configuration {@link #getTargetResourceProvider}. */
		String TARGET_RESOURCE_PROVIDER_NAME = "impl";

		/**
		 * The {@link ResourceProvider} to delegate
		 * {@link AbstractMappingResourceProvider#mapValue(Object) mapped} objects to.
		 */
		@Name(TARGET_RESOURCE_PROVIDER_NAME)
		@InstanceFormat
		@InstanceDefault(MetaResourceProvider.class)
		ResourceProvider getTargetResourceProvider();

		/**
		 * @see #getTargetResourceProvider()
		 */
		void setTargetResourceProvider(ResourceProvider value);
	}
	
	/**
	 * Creates a {@link AbstractMappingResourceProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractMappingResourceProvider(InstantiationContext context, C config) {
		super(config.getTargetResourceProvider());
		_config = config;
	}
	
	@Override
	public C getConfig() {
		return _config;
	}

}

